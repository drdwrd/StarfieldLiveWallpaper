package drwdrd.ktdev.starfield

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import drwdrd.ktdev.engine.*
import java.lang.ref.WeakReference
import java.util.ArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


private const val TAG = "drwdrd.ktdev.starfield.StarfieldRenderer"

private const val starfieldSampler = 0
private const val starfieldAspectUniform = 1
private const val starfieldTextureMatrixUniform = 2
private const val starfieldOffsetUniform  = 3
private const val starfieldTimeUniform = 4

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
private const val particleSpawnDistance = 10.0f
private const val cloudsSpawnTimeMultiplier = 10.0
private const val maxStarParticlesCount = 1000        //hard limit just in case...
private const val maxCloudParticlesCount = 200        //hard limit just in case...


class StarfieldRenderer private constructor(_context: Context) : GLSurfaceView.Renderer, GLWallpaperService.WallpaperLiveCycleListener, GLWallpaperService.OnOffsetChangedListener {

    private constructor(_context : Context, file : String) : this(_context) {
        SettingsProvider.load(_context, file)
        if(SettingsProvider.parallaxEffectEngineType == SettingsProvider.ParallaxEffectEngineType.Unknown) {
            SettingsProvider.parallaxEffectEngineType = getParallaxEffectEngine()
        }
        Log.info(TAG, "Parallax effect engine set to ${SettingsProvider.parallaxEffectEngineType}")
        parallaxEffectEngine = when(SettingsProvider.parallaxEffectEngineType) {
            SettingsProvider.ParallaxEffectEngineType.Gyro -> GyroParallaxEffectEngine()
            SettingsProvider.ParallaxEffectEngineType.Gravity -> GravityParallaxEffectEngine()
            SettingsProvider.ParallaxEffectEngineType.Accelerometer -> AccelerometerParallaxEffectEngine()
            else -> ScrollingWallpaperEffectEngine()
        }
        fpsCounter.onMeasureListener = object : FpsCounter.OnMeasureListener {
            override fun onMeasure(frameTime: Double) {
                if(SettingsProvider.adaptiveFPS) {
                    if (frameTime > 16.9) {
                        maxParticleSpawnTime = clamp(1.05 * maxParticleSpawnTime, 0.015, 0.25)
                    } else if (frameTime < 16.8) {
                        maxParticleSpawnTime = clamp(0.95 * maxParticleSpawnTime, 0.015, 0.25)
                    }
                }
                Log.debug(TAG, "frameTime = %.2f ms, particleSpawnTime = %.2f ms, starParticles = ${starSprites.size}, cloudParticles = ${cloudSprites.size}".format(frameTime, 1000.0 * maxParticleSpawnTime))
            }
        }
    }

    private val context : Context = _context
    private var requestRestart = false
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
    private var lastStarParticleSpawnTime = 1000.0
    private val starSprites : MutableList<Particle> = ArrayList()
    private var lastCloudParticleSpawnTime = 1000.0
    private val cloudSprites : MutableList<Particle> = ArrayList()
    private val eye = Eye()

    private lateinit var parallaxEffectEngine : ParallaxEffectEngine

    private var randomBackgroundOffset = vector2f(0.0f, 0.0f)
    private var randomBackgroundRotation = 0.0f

    //global preferences
    private var particleSpeed = SettingsProvider.particleSpeed
    private var maxParticleSpawnTime = SettingsProvider.particlesSpawnTimeMultiplier

    private fun getParallaxEffectEngine() : SettingsProvider.ParallaxEffectEngineType {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return when {
            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null -> SettingsProvider.ParallaxEffectEngineType.Gyro
            sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null -> SettingsProvider.ParallaxEffectEngineType.Gravity
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null && sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null -> SettingsProvider.ParallaxEffectEngineType.Accelerometer
            else -> SettingsProvider.ParallaxEffectEngineType.None
        }
    }

    private fun getTextureCompressionMode(version : String, extensions : String) : Flag<SettingsProvider.TextureCompressionMode> {
        val modes  = Flag(SettingsProvider.TextureCompressionMode.UNKNOWN)
        if(extensions.contains("KHR_compressed_texture_astc_ldr") || extensions.contains("OES_texture_compression_astc")) {
            modes.setFlag(SettingsProvider.TextureCompressionMode.ASTC)
        }
        if(version.contains("OpenGL ES 3")) {
            modes.setFlag(SettingsProvider.TextureCompressionMode.ETC1)
            modes.setFlag(SettingsProvider.TextureCompressionMode.ETC2)
        }
        if(extensions.contains("OES_compressed_ETC2_RGB8_texture") && extensions.contains("OES_compressed_ETC2_RGBA8_texture")) {
            modes.setFlag(SettingsProvider.TextureCompressionMode.ETC2)
        }
        if(version.contains("OpenGL ES 2")) {
            modes.setFlag(SettingsProvider.TextureCompressionMode.ETC1)
        }
        if(extensions.contains("OES_compressed_ETC1_RGB8_texture")) {
            modes.setFlag(SettingsProvider.TextureCompressionMode.ETC1)
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

        Log.info(TAG, "OpenGL version: $version")
        Log.info(TAG, "OpenGL vendor: $vendor")
        Log.info(TAG, "OpenGL renderer: $renderer")
        Log.info(TAG, "OpenGL extensions: $extensions")

        if(SettingsProvider.baseTextureQualityLevel >= SettingsProvider.TEXTURE_QUALITY_UNKNOWN) {
            SettingsProvider.baseTextureQualityLevel = getTextureBaseQualityLevel()
        }

        if(SettingsProvider.textureCompressionMode.isFlag(SettingsProvider.TextureCompressionMode.UNKNOWN)) {
            SettingsProvider.textureCompressionMode = getTextureCompressionMode(version, extensions)
        }


        Log.info(TAG, "Texture compression mode set to ${SettingsProvider.textureCompressionMode.flags.toString(2)}")

        create()
    }


    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        parallaxEffectEngine.reset = true
        parallaxEffectEngine.orientation = context.resources.configuration.orientation
        randomBackgroundOffset = RandomGenerator.rand2f(-1.0f, 1.0f)
        randomBackgroundRotation = RandomGenerator.randf(-M_PI, M_PI)
        eye.setViewport(vector2f(width.toFloat(), height.toFloat()))
        aspect = vector2f(width.toFloat() / height.toFloat(), 1.0f)
//        Log.debug("onSurfaceChanged(width = $width, height = $height)")
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        if(requestRestart) {
            destroy()
            create()
            requestRestart = false
        }

        timer.tick()

        parallaxEffectEngine.onTick(timer.deltaTime.toFloat())

        eye.rotateBy(parallaxEffectEngine.offset)


        val viewProjectionMatrix = eye.viewProjectionMatrix
        val frustum = Frustum(viewProjectionMatrix)

        plane.bind()

        //render background
        if(theme.hasBackground()) {

            val backgroundTextureMatrix = matrix3f()
            backgroundTextureMatrix.loadIdentity()
            backgroundTextureMatrix.setRotationPart(randomBackgroundRotation)
            backgroundTextureMatrix.setTranslationPart(randomBackgroundOffset)


            starfieldShader.bind()
            starfieldTexture.bind(0)

            starfieldShader.setSampler(starfieldSampler, 0)
            starfieldShader.setUniformValue(starfieldAspectUniform, aspect * theme.backgroundScale)
            starfieldShader.setUniformValue(starfieldTextureMatrixUniform, backgroundTextureMatrix)
            starfieldShader.setUniformValue(starfieldOffsetUniform, parallaxEffectEngine.backgroundOffset)
            starfieldShader.setUniformValue(starfieldTimeUniform, timer.currentTime.toFloat())

            plane.draw()

            starfieldTexture.release(0)
            starfieldShader.release()

        }

        val eyeForward = eye.forward
        val eyePosition = eye.position

        if(theme.hasClouds()) {

            //remove all particles behind camera
            cloudSprites.removeAll { !frustum.isInDistance(it.position, minParticleDistance) }

            lastCloudParticleSpawnTime += timer.deltaTime
            //if its time spawn new particle
            if (lastCloudParticleSpawnTime >= cloudsSpawnTimeMultiplier * maxParticleSpawnTime && cloudSprites.size < maxCloudParticlesCount) {
                cloudSprites.add(0, Particle.createCloud(eyeForward, eyePosition, particleSpawnDistance))
                lastCloudParticleSpawnTime = 0.0
            }

            //render cloud sprites

            cloudspriteShader.bind()
            cloudspritesTexture.bind(0)

            cloudspriteShader.setSampler(cloudspriteSampler, 0)

            val cloud = cloudSprites.iterator()
            while (cloud.hasNext()) {


                val sprite = cloud.next()


                //face dir
                val dir = eyePosition - sprite.position
                dir.normalize()

                //normal
                val normal = vector3f(0.0f, 0.0f, -1.0f)

                val fadeIn = smoothstep(0.0f, 1.0f, sprite.age)
                val fadeOut = smoothstep(-1.0f, 2.5f, sprite.position.z)

                val boundingSphere = sprite.boundingSphere()

                if (frustum.contains(boundingSphere)) {

                    val modelMatrix = sprite.calculateBillboardModelMatrix(theme.cloudsParticleScale, dir, normal)

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
            //if its time spawn new particle
            if (lastStarParticleSpawnTime >= maxParticleSpawnTime && starSprites.size < maxStarParticlesCount) {
                starSprites.add(0, Particle.createStar(eyeForward, eyePosition, particleSpawnDistance))
                lastStarParticleSpawnTime = 0.0
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

                //face dir
                val dir = eyePosition - sprite.position
                dir.normalize()

                //normal
                val normal = vector3f(0.0f, 0.0f, -1.0f)

                val fadeIn = smoothstep(0.0f, 1.0f, sprite.age)

                val rotMatrix = matrix3f()
                rotMatrix.setRotation(0.1f * timer.currentTime.toFloat())

                val boundingSphere = sprite.boundingSphere()

                if (frustum.contains(boundingSphere)) {

                    val modelMatrix = sprite.calculateBillboardModelMatrix(theme.starsParticleScale, dir, normal)

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
        SettingsProvider.save(context, "starfield.ini")
    }

    override fun onResume() {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        parallaxEffectEngine.connect(sensorManager)
        particleSpeed = SettingsProvider.particleSpeed
        if(!SettingsProvider.adaptiveFPS) {
            maxParticleSpawnTime = SettingsProvider.particlesSpawnTimeMultiplier
        }
        eye.setLookAt(vector3f(0.0f, 0.0f, 0.0f), vector3f(0.0f, 0.0f, 1.0f), vector3f(0.0f, 1.0f, 0.0f))
        timer.reset()
    }

    override fun onPause() {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        parallaxEffectEngine.disconnect(sensorManager)
    }

    private fun create() {

        val textureQuality = SettingsProvider.textureQualityLevel + SettingsProvider.baseTextureQualityLevel
        Log.info(TAG, "Texture quality level set to $textureQuality")

        plane.create()

        starspriteShader = if(SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ASTC) || SettingsProvider.textureCompressionMode.hasFlag(SettingsProvider.TextureCompressionMode.ETC2)) {
            ProgramObject.loadFromAssets(context, "shaders/starsprite.vert", "shaders/starsprite_pm.frag", plane.vertexFormat)
        } else {
            ProgramObject.loadFromAssets(context, "shaders/starsprite.vert", "shaders/starsprite.frag", plane.vertexFormat)
        }
        starspriteShader.registerUniform("u_StarSprites", starspriteSampler)
        starspriteShader.registerUniform("u_Noise", starspriteNoiseSampler)
        starspriteShader.registerUniform("u_ModelViewProjectionMatrix", starspriteModelViewProjectionMatrixUniform)
        starspriteShader.registerUniform("u_RotationMatrix", starspriteRotationMatrixUniform)
        starspriteShader.registerUniform("u_uvRoI", starspriteUvRoIUniform)
        starspriteShader.registerUniform("u_FadeIn", starspriteFadeInUniform)

        cloudspriteShader = ProgramObject.loadFromAssets(context, "shaders/cloudsprite.vert", "shaders/cloudsprite.frag", plane.vertexFormat)
        cloudspriteShader.registerUniform("u_CloudSprites", starspriteSampler)
        cloudspriteShader.registerUniform("u_ModelViewProjectionMatrix", cloudspriteModelViewProjectionMatrixUniform)
        cloudspriteShader.registerUniform("u_uvRoI", cloudspriteUvRoIUniform)
        cloudspriteShader.registerUniform("u_Color", cloudspriteColorUniform)
        cloudspriteShader.registerUniform("u_Fade", cloudspriteFadeUniform)

        starfieldShader = ProgramObject.loadFromAssets(context, "shaders/starfield.vert", "shaders/starfield.frag", plane.vertexFormat)
        starfieldShader.registerUniform("u_Starfield", starfieldSampler)
        starfieldShader.registerUniform("u_Aspect", starfieldAspectUniform)
        starfieldShader.registerUniform("u_TextureMatrix", starfieldTextureMatrixUniform)
        starfieldShader.registerUniform("u_Offset", starfieldOffsetUniform)
        starfieldShader.registerUniform("u_Time", starfieldTimeUniform)

        if(theme.hasBackground()) {
            starfieldTexture = theme.starfieldTexture(context, textureQuality, SettingsProvider.textureCompressionMode)!!
        }
        if(theme.hasStars()) {
            starspritesTexture = theme.starsTexture(context, textureQuality, SettingsProvider.textureCompressionMode)!!
        }
        if(theme.hasClouds()) {
            cloudspritesTexture = theme.cloudsTexture(context, textureQuality, SettingsProvider.textureCompressionMode)!!
        }

        //misc
        noiseTexture = Texture.loadFromAssets2D(context, "themes/default/png/noise.png", textureQuality, Texture.WrapMode.Repeat, Texture.WrapMode.Repeat, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)

        eye.setPerspective(50.0f, 0.0f, 100.0f)
        eye.setLookAt(vector3f(0.0f, 0.0f, 0.0f), vector3f(0.0f, 0.0f, 1.0f), vector3f(0.0f, 1.0f, 0.0f))

        //opengl setup
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glClearDepthf(1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glBlendEquation(GLES20.GL_FUNC_ADD)

        timer.reset()
    }

    private fun destroy() {
        plane.destroy()
        starspriteShader.delete()
        cloudspriteShader.delete()
        starfieldShader.delete()
        starspritesTexture.delete()
        cloudspritesTexture.delete()
        starfieldTexture.delete()
        noiseTexture.delete()
    }

    companion object {

        var theme : Theme = DefaultTheme()
            set(value) {
                field = value
                notifyRestart()
            }

        //TODO: this is rather hackish solution...
        private var instances = ArrayList<WeakReference<StarfieldRenderer>>()

        fun createRenderer(context: Context) : StarfieldRenderer {
            val renderer = StarfieldRenderer(context, "starfield.ini")
            instances.add(WeakReference(renderer))
            return renderer
        }

        fun notifyRestart() {
            with(instances.iterator()) {
                while(hasNext()) {
                    val ref = next().get()
                    if(ref == null) {
                        remove()
                    } else {
                        ref.requestRestart = true
                    }
                }
            }
        }
    }
}


