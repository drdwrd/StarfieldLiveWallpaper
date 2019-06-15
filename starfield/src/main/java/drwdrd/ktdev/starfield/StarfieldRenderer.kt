package drwdrd.ktdev.starfield

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.WindowManager
import drwdrd.ktdev.kengine.*
import java.lang.ref.WeakReference
import java.util.ArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


private const val starfieldSampler = 0
private const val starfieldAspectUniform = 1
private const val starfieldTextureMatrixUniform = 2
private const val starfieldTimeUniform = 3

private const val cloudspriteSampler = 0
private const val cloudspriteModelViewProjectionMatrixUniform = 1
private const val cloudspriteUvRoIUniform = 2
private const val cloudspriteColorUniform = 3
private const val cloudspriteFadeUniform = 4

private const val starspriteSampler = 0
private const val starspriteNoiseSampler = 1
private const val starspriteModelViewProjectionMatrixUniform = 2
private const val starspriteRotationMatrixUniform = 3
private const val starspriteUvRoIUniform = 4
private const val starspriteFadeInUniform = 5


private const val minParticleDistance = -2.5f
private const val particleSpawnDistance = 20.0f
private const val maxStarParticlesCount = 5000        //hard limit just in case...
private const val maxCloudParticlesCount = 500        //hard limit just in case...


class StarfieldRenderer private constructor(private val context: Context): GLSurfaceView.Renderer, GLWallpaperService.WallpaperLiveCycleListener, GLWallpaperService.OnOffsetChangedListener {

    enum class RendererState {
        Start,
        Render,
        Restart,
        LoadTheme
    }

    private var rendererState = RendererState.Start
    private lateinit var theme : Theme
    private val plane = Plane3D()
    private var aspect = vector2f(1.0f, 1.0f)
    private var starspriteShader = ProgramObject()
    private var cloudspriteShader = ProgramObject()
    private var starfieldShader  = ProgramObject()
    private var starfieldTexture = Texture()
    private var starspritesTexture = Texture()
    private var cloudspritesTexture = Texture()
    private var noiseTexture = Texture()
    private val timer = Timer()
    private val fpsCounter = FpsCounter(1.0)
    private var lastStarParticleSpawnTime = 0.0
    private val starSprites : MutableList<Particle> = ArrayList()
    private var lastCloudParticleSpawnTime = 0.0
    private val cloudSprites : MutableList<Particle> = ArrayList()
    private val eye = Eye()

    private val backgroundTextureMatrix = matrix3f.identity()
    private val backgroundRandomTextureMatrix = matrix3f.identity()

    private val parallaxEffectEngine : ParallaxEffectEngine

    //global preferences
    private var particleSpeed = SettingsProvider.particleSpeed
    //TODO: possible division by 0.0
    private var maxStarsSpawnTime = SettingsProvider.starsSpawnTimeMultiplier / particleSpeed
    private var maxCloudsSpawnTime = SettingsProvider.cloudsSpawnTimeMultiplier / particleSpeed

    init {
        if(SettingsProvider.parallaxEffectEngineType == ParallaxEffectEngineType.Unknown) {
            SettingsProvider.parallaxEffectEngineType = getParallaxEffectEngine()
        }
        logi("Parallax effect engine set to ${SettingsProvider.parallaxEffectEngineType}")
        parallaxEffectEngine = when(SettingsProvider.parallaxEffectEngineType) {
            ParallaxEffectEngineType.Gyro -> GyroParallaxEffectEngine()
            ParallaxEffectEngineType.Gravity -> GravityParallaxEffectEngine()
            ParallaxEffectEngineType.Accelerometer -> AccelerometerParallaxEffectEngine()
            else -> ScrollingWallpaperEffectEngine()
        }
        fpsCounter.onMeasureListener = object : FpsCounter.OnMeasureListener {

            private var targetFrameTime = 16.67

            override fun onStart() {
                targetFrameTime = 1000.0 / clamp((context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.refreshRate, 30.0f, 120.0f)
                logd("targetFrameTime = %.2f ms, starsSpawnTime = %.2f ms, starParticles = %d, cloudsSpawnTime = %.2f ms, cloudParticles = %d"
                    .format(targetFrameTime, 1000.0 * maxStarsSpawnTime, starSprites.size, 1000.0 * maxCloudsSpawnTime, cloudSprites.size), enclosingClass = StarfieldRenderer::class)
            }

            override fun onMeasure(frameTime: Double) {
                val defaultStarsSpawnTime = theme.starsDensity / particleSpeed
                val defaultCloudsSpawnTime = theme.cloudDensity / particleSpeed
                if(SettingsProvider.adaptiveFPS) {
                    if (frameTime > 1.15 * targetFrameTime) {
                        maxStarsSpawnTime = clamp(1.05 * maxStarsSpawnTime, defaultStarsSpawnTime, 4.0 * defaultStarsSpawnTime)
                        maxCloudsSpawnTime = clamp(1.05 * maxCloudsSpawnTime, defaultCloudsSpawnTime, 4.0 * defaultCloudsSpawnTime)
                    } else if (frameTime < 1.05 * targetFrameTime) {
                        maxStarsSpawnTime = clamp(0.95 * maxStarsSpawnTime, defaultStarsSpawnTime, 4.0 * defaultStarsSpawnTime)
                        maxCloudsSpawnTime = clamp(0.95 * maxCloudsSpawnTime, defaultCloudsSpawnTime, 4.0 * defaultCloudsSpawnTime)
                    }
                }
                logd("frameTime = %.2f ms, targetFrameTime = %.2f ms, starsSpawnTime = %.2f ms, starParticles = %d, cloudsSpawnTime = %.2f ms, cloudParticles = %d"
                    .format(frameTime, targetFrameTime, 1000.0 * maxStarsSpawnTime, starSprites.size, 1000.0 * maxCloudsSpawnTime, cloudSprites.size), enclosingClass = StarfieldRenderer::class)
            }

            override fun onTick(deltaFrameTime : Double) {

            }
        }
    }

    private fun getParallaxEffectEngine() : ParallaxEffectEngineType {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return when {
            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null -> ParallaxEffectEngineType.Gyro
            sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null -> ParallaxEffectEngineType.Gravity
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null && sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null -> ParallaxEffectEngineType.Accelerometer
            else -> ParallaxEffectEngineType.None
        }
    }

    private fun getTextureCompressionMode(version : String, extensions : String) : Flag<TextureCompressionMode> {
        val modes = Flag(TextureCompressionMode.UNKNOWN)
        if(extensions.contains("KHR_texture_compression_astc_ldr") || extensions.contains("OES_texture_compression_astc")) {
            modes.setFlag(TextureCompressionMode.ASTC)
        }
        if(version.contains("OpenGL ES 3")) {
            modes.setFlag(TextureCompressionMode.ETC1)
            modes.setFlag(TextureCompressionMode.ETC2)
        }
        if(extensions.contains("OES_compressed_ETC2_RGB8_texture") && extensions.contains("OES_compressed_ETC2_RGBA8_texture")) {
            modes.setFlag(TextureCompressionMode.ETC2)
        }
        if(version.contains("OpenGL ES 2")) {
            modes.setFlag(TextureCompressionMode.ETC1)
        }
        if(extensions.contains("OES_compressed_ETC1_RGB8_texture")) {
            modes.setFlag(TextureCompressionMode.ETC1)
        }
        return modes
    }

    private fun getTextureBaseQualityLevel() : Int {

        val maxTextureSize = IntArray(1)
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0)

        require(maxTextureSize[0] >= 512) { "Required GL_MAX_TEXTURE_SIZE >= 512" }

        return when(maxTextureSize[0]) {
            512 -> 3
            1024 -> 2
            2048 -> 1
            else -> 0       // >= 4096
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
//        Log.debug("onSurfaceCreated()")

        RandomGenerator.createSeed()

        val version = GLES20.glGetString(GLES20.GL_VERSION)
        val vendor = GLES20.glGetString(GLES20.GL_VENDOR)
        val renderer = GLES20.glGetString(GLES20.GL_RENDERER)
        val extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS)

        logi("OpenGL version: $version")
        logi("OpenGL vendor: $vendor")
        logi("OpenGL renderer: $renderer")
        logi("OpenGL extensions: $extensions")

        create(version, extensions)
    }


    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        parallaxEffectEngine.reset = true
        parallaxEffectEngine.orientation = context.resources.configuration.orientation

        //change randomly
        backgroundRandomTextureMatrix.setParallax(RandomGenerator.randf(-1.0f, 1.0f), RandomGenerator.randf(-1.0f, 1.0f), RandomGenerator.randf(-M_PI, M_PI))

        eye.setViewport(vector2f(width.toFloat(), height.toFloat()))
        aspect = vector2f(width.toFloat() / height.toFloat(), 1.0f)
//        Log.debug("onSurfaceChanged(width = $width, height = $height)")
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        if(rendererState != RendererState.Render) {
            destroy()
            create()
        }

        timer.tick()

        parallaxEffectEngine.onTick(timer.currentTime.toFloat(), timer.deltaTime.toFloat())

        eye.rotateBy(parallaxEffectEngine.offset)


        val viewProjectionMatrix = eye.viewProjectionMatrix
        val frustum = Frustum(viewProjectionMatrix)

        plane.bind()

        //render background
        if(theme.hasBackground()) {

            val offsetMatrix = matrix3f()
            offsetMatrix.setParallax(parallaxEffectEngine.offset)

            backgroundTextureMatrix *= offsetMatrix

            starfieldShader.bind()
            starfieldTexture.bind(0)

            starfieldShader.setSampler(starfieldSampler, 0)
            starfieldShader.setUniformValue(starfieldAspectUniform, aspect * theme.backgroundScale)
            starfieldShader.setUniformValue(starfieldTextureMatrixUniform, backgroundRandomTextureMatrix * backgroundTextureMatrix)
            starfieldShader.setUniformValue(starfieldTimeUniform, timer.currentTime.toFloat())

            plane.draw()

            starfieldTexture.release(0)
            starfieldShader.release()

        }

        val eyeForward = eye.forward
        val eyePosition = eye.position

        if(theme.hasClouds()) {

            //remove all particles behind camera
            cloudSprites.removeAll { !frustum.isInDistance(it.position, minParticleDistance - it.boundingSphereRadius(theme.cloudsParticleScale) ) }

            lastCloudParticleSpawnTime += timer.deltaTime
            //if its time spawn new particle
            while (lastCloudParticleSpawnTime >= maxCloudsSpawnTime && cloudSprites.size < maxCloudParticlesCount) {
                cloudSprites.add(0, Particle.createCloud(eyeForward, eyePosition, particleSpawnDistance, theme.cloudColors[RandomGenerator.rand(theme.cloudColors.size - 1)]))
                lastCloudParticleSpawnTime -= maxCloudsSpawnTime
            }

            //render cloud sprites

            cloudspriteShader.bind()
            cloudspritesTexture.bind(0)
            noiseTexture.bind(1)

            cloudspriteShader.setSampler(cloudspriteSampler, 0)

            val cloud = cloudSprites.iterator()
            while (cloud.hasNext()) {


                val sprite = cloud.next()

                val boundingSphere = sprite.boundingSphere(theme.cloudsParticleScale)

                if (frustum.contains(boundingSphere)) {

                    //face dir
                    val dir = eyePosition - sprite.position
                    val dist = dir.length()
                    if(dist > 0.0f) {
                        dir /= dist
                    }


                    val fadeIn = smoothstep(0.0f, 3.5f, sprite.timeFromStart)
                    val fadeOut = smoothstep(1.5f, 2.5f, -dist * vector3f.dot(dir, eyeForward) )



//                    val modelMatrix = sprite.calculateBillboardModelMatrix(theme.cloudsParticleScale, dir)
                    val modelMatrix = sprite.calculateBillboardModelMatrix(theme.cloudsParticleScale, -eyeForward)

                    cloudspriteShader.setUniformValue(cloudspriteModelViewProjectionMatrixUniform, viewProjectionMatrix * modelMatrix)
                    cloudspriteShader.setUniformValue(cloudspriteUvRoIUniform, vector4f(sprite.uvRoI.left, sprite.uvRoI.top, sprite.uvRoI.width, sprite.uvRoI.height))
                    cloudspriteShader.setUniformValue(cloudspriteColorUniform, sprite.color)
                    cloudspriteShader.setUniformValue(cloudspriteFadeUniform, fadeIn * fadeOut)

                    plane.draw()
                }

                sprite.tick(eyeForward, particleSpeed, timer.deltaTime.toFloat())
            }


            cloudspritesTexture.release(0)
            cloudspriteShader.release()
        }

        if(theme.hasStars()) {
            //remove all particles behind camera
            starSprites.removeAll { !frustum.isInDistance(it.position, minParticleDistance) }

            lastStarParticleSpawnTime += timer.deltaTime
            //if its time spawn new particles
            while (lastStarParticleSpawnTime >= maxStarsSpawnTime && starSprites.size < maxStarParticlesCount) {
                starSprites.add(0, Particle.createStar(eyeForward, eyePosition, particleSpawnDistance))
                lastStarParticleSpawnTime -= maxStarsSpawnTime
            }

            //render stars sprites

            starspriteShader.bind()
            starspritesTexture.bind(0)
            noiseTexture.bind(1)

            starspriteShader.setSampler(starspriteSampler, 0)
            starspriteShader.setSampler(starspriteNoiseSampler, 1)

            val star = starSprites.iterator()
            while (star.hasNext()) {

                val sprite = star.next()

                val boundingSphere = sprite.boundingSphere(theme.starsParticleScale)

                if (frustum.contains(boundingSphere)) {

                    //face dir
                    val dir = eyePosition - sprite.position
                    dir.normalize()

                    val fadeIn = smoothstep(0.0f, 1.0f, sprite.timeFromStart)

                    val rotMatrix = matrix3f()
                    rotMatrix.setRotation(0.1f * sprite.age)

                    val modelMatrix = sprite.calculateBillboardModelMatrix(theme.starsParticleScale, dir)

                    starspriteShader.setUniformValue(starspriteModelViewProjectionMatrixUniform, viewProjectionMatrix * modelMatrix)
                    starspriteShader.setUniformValue(starspriteRotationMatrixUniform, rotMatrix)
                    starspriteShader.setUniformValue(starspriteUvRoIUniform, vector4f(sprite.uvRoI.left, sprite.uvRoI.top, sprite.uvRoI.width, sprite.uvRoI.height))
                    starspriteShader.setUniformValue(starspriteFadeInUniform, fadeIn)

                    plane.draw()
                }

                sprite.tick(eyeForward, particleSpeed, timer.deltaTime.toFloat())
            }


            noiseTexture.release(1)
            starspritesTexture.release(0)
            starspriteShader.release()
        }

        plane.release()

        fpsCounter.tick(timer.deltaTime)
    }

    override fun onOffsetChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {
        parallaxEffectEngine.onOffsetChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset)
    }

    override fun onStart() {

    }

    override fun onStop() {
        SettingsProvider.save(context, BuildConfig.configFileName)
    }

    override fun onResume() {
        lastStarParticleSpawnTime = 0.0
        lastCloudParticleSpawnTime = 0.0
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        parallaxEffectEngine.connect(sensorManager)
        particleSpeed = SettingsProvider.particleSpeed
        //TODO: possible division by 0
        maxStarsSpawnTime = SettingsProvider.starsSpawnTimeMultiplier / particleSpeed
        maxCloudsSpawnTime = SettingsProvider.cloudsSpawnTimeMultiplier / particleSpeed
        fpsCounter.start()
        timer.reset()
    }

    override fun onPause() {
        SettingsProvider.starsSpawnTimeMultiplier = maxStarsSpawnTime * particleSpeed
        SettingsProvider.cloudsSpawnTimeMultiplier = maxCloudsSpawnTime * particleSpeed
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        parallaxEffectEngine.disconnect(sensorManager)
    }

    private fun create(version : String? = null, extensions: String? = null) {
        //block thread until created
        synchronized(rendererInstances) {

            //must keep local copy of instance of theme or synchronize main render loop (not very efficient)
            this.theme = rendererInstances.theme

            if (SettingsProvider.baseTextureQualityLevel >= SettingsProvider.TEXTURE_QUALITY_UNKNOWN) {
                SettingsProvider.baseTextureQualityLevel = getTextureBaseQualityLevel()
            }

            if (SettingsProvider.textureCompressionMode.isFlag(TextureCompressionMode.UNKNOWN)) {
                SettingsProvider.textureCompressionMode =
                    getTextureCompressionMode(version ?: GLES20.glGetString(GLES20.GL_VERSION), extensions ?: GLES20.glGetString(GLES20.GL_EXTENSIONS))
            }

            logi("Texture compression mode set to ${SettingsProvider.textureCompressionMode.flags.toString(2)}")

            val textureQuality = SettingsProvider.textureQualityLevel + SettingsProvider.baseTextureQualityLevel
            logi("Texture quality level set to $textureQuality")

            plane.create()

            if (theme.hasBackground()) {

                starfieldShader = theme.starfieldShader(context, plane.vertexFormat, SettingsProvider.textureCompressionMode) ?: ProgramObject()
                starfieldShader.registerUniform("u_Starfield", starfieldSampler)
                starfieldShader.registerUniform("u_Aspect", starfieldAspectUniform)
                starfieldShader.registerUniform("u_TextureMatrix", starfieldTextureMatrixUniform)
                starfieldShader.registerUniform("u_Time", starfieldTimeUniform)

                starfieldTexture = theme.starfieldTexture(context, textureQuality, SettingsProvider.textureCompressionMode) ?: Texture.emptyTexture2D()
            }
            if (theme.hasStars()) {

                starspriteShader = theme.starsShader(context, plane.vertexFormat, SettingsProvider.textureCompressionMode) ?: ProgramObject()
                starspriteShader.registerUniform("u_StarSprites", starspriteSampler)
                starspriteShader.registerUniform("u_Noise", starspriteNoiseSampler)
                starspriteShader.registerUniform("u_ModelViewProjectionMatrix", starspriteModelViewProjectionMatrixUniform)
                starspriteShader.registerUniform("u_RotationMatrix", starspriteRotationMatrixUniform)
                starspriteShader.registerUniform("u_uvRoI", starspriteUvRoIUniform)
                starspriteShader.registerUniform("u_FadeIn", starspriteFadeInUniform)

                starspritesTexture = theme.starsTexture(context, textureQuality, SettingsProvider.textureCompressionMode) ?: Texture.emptyTexture2D()
            }
            if (theme.hasClouds()) {

                cloudspriteShader = theme.cloudsShader(context, plane.vertexFormat, SettingsProvider.textureCompressionMode) ?: ProgramObject()
                cloudspriteShader.registerUniform("u_CloudSprites", starspriteSampler)
                cloudspriteShader.registerUniform("u_ModelViewProjectionMatrix", cloudspriteModelViewProjectionMatrixUniform)
                cloudspriteShader.registerUniform("u_uvRoI", cloudspriteUvRoIUniform)
                cloudspriteShader.registerUniform("u_Color", cloudspriteColorUniform)
                cloudspriteShader.registerUniform("u_Fade", cloudspriteFadeUniform)

                cloudspritesTexture = theme.cloudsTexture(context, textureQuality, SettingsProvider.textureCompressionMode) ?: Texture.emptyTexture2D()
            }

            //misc
            noiseTexture = Texture.loadFromAssets2D(
                context,
                "themes/default/png/noise.png",
                textureQuality,
                Texture.WrapMode.Repeat,
                Texture.WrapMode.Repeat,
                Texture.Filtering.LinearMipmapLinear,
                Texture.Filtering.Linear
            )

            //reset camera
            eye.setPerspective(50.0f, 0.0f, 100.0f)
            eye.setLookAt(vector3f(0.0f, 0.0f, 0.0f), vector3f(0.0f, 0.0f, 1.0f), vector3f(0.0f, 1.0f, 0.0f))
            backgroundTextureMatrix.loadIdentity()

            val eyeForward = eye.forward
            val eyePosition = eye.position

            //when load theme and on adaptiveFPS, reset density settings to default
            if(SettingsProvider.adaptiveFPS && rendererState == RendererState.LoadTheme) {
                SettingsProvider.starsSpawnTimeMultiplier = theme.starsDensity
                SettingsProvider.cloudsSpawnTimeMultiplier = theme.cloudDensity
                maxStarsSpawnTime = SettingsProvider.starsSpawnTimeMultiplier / particleSpeed
                maxCloudsSpawnTime = SettingsProvider.cloudsSpawnTimeMultiplier / particleSpeed
            }


            val starsCount = (particleSpawnDistance / SettingsProvider.starsSpawnTimeMultiplier).toInt()
            if (theme.hasStars()) {
                for (i in 0 until starsCount) {
                    starSprites.add(0, Particle.createStar(eyeForward, eyePosition, particleSpawnDistance * i / starsCount))
                }
            }

            val cloudsCount = (particleSpawnDistance / SettingsProvider.cloudsSpawnTimeMultiplier).toInt()
            if (theme.hasClouds()) {
                for (i in 0 until cloudsCount) {
                    //TODO: crashes when cloudColors.size < 2
                    cloudSprites.add(
                        0,
                        Particle.createCloud(eyeForward, eyePosition, particleSpawnDistance * i / cloudsCount, theme.cloudColors[RandomGenerator.rand(theme.cloudColors.size - 1)])
                    )
                }
            }

            //opengl setup
            GLES20.glEnable(GLES20.GL_CULL_FACE)
            GLES20.glFrontFace(GLES20.GL_CW)
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
            GLES20.glClearDepthf(1.0f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            GLES20.glBlendEquation(GLES20.GL_FUNC_ADD)

            fpsCounter.start()
            timer.reset()
            rendererState = RendererState.Render
        }
    }

    private fun destroy() {
        //block thread until deleted
        synchronized(rendererInstances) {
            plane.destroy()
            starspriteShader.delete()
            cloudspriteShader.delete()
            starfieldShader.delete()
            starspritesTexture.delete()
            cloudspritesTexture.delete()
            starfieldTexture.delete()
            noiseTexture.delete()
            starSprites.clear()
            cloudSprites.clear()
        }
    }

    companion object {

        class RendererInstances {

            private val instances = ArrayList<WeakReference<StarfieldRenderer>>()

            var theme : Theme = DefaultTheme()
                private set

            fun createRenderer(context: Context) : StarfieldRenderer {
                synchronized(this) {
                    if(instances.size == 0) {
                        //load settings from file only when no running instances
                        SettingsProvider.load(context, BuildConfig.configFileName)
                    }
                    val renderer = StarfieldRenderer(context)
                    instances.add(WeakReference(renderer))
                    return renderer
                }
            }

            fun requestLoadTheme(theme: Theme) {
                synchronized(this) {
                    this.theme = theme
                    with(instances.iterator()) {
                        while (hasNext()) {
                            val ref = next().get()
                            if (ref == null) {
                                remove()
                            } else {
                                ref.rendererState = RendererState.LoadTheme
                            }
                        }
                    }
                }
            }

            fun requestRestart() {
                synchronized(this) {
                    with(instances.iterator()) {
                        while (hasNext()) {
                            val ref = next().get()
                            if (ref == null) {
                                remove()
                            } else {
                                ref.rendererState = RendererState.Restart
                            }
                        }
                    }
                }
            }
        }

        val rendererInstances = RendererInstances()
    }
}


