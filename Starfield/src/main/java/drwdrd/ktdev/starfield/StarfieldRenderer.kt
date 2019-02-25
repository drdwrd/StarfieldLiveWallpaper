package drwdrd.ktdev.starfield

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.opengl.ETC1
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import drwdrd.ktdev.engine.*
import java.lang.Exception
import java.util.ArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


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


private const val particleSpawnDistance = 8.0f
private const val maxStarParticlesCount = 1000        //hard limit just in case...
private const val maxCloudParticlesCount = 200        //hard limit just in case...


class StarfieldRenderer private constructor(_context: Context) : GLSurfaceView.Renderer, GLWallpaperService.WallpaperLiveCycleListener, GLWallpaperService.OnOffsetChangedListener {

    constructor(_context : Context, file : String) : this(_context) {
        SettingsProvider.load(_context, file)
        if(SettingsProvider.parallaxEffectEngineType == SettingsProvider.ParallaxEffectEngineType.Unknown) {
            SettingsProvider.parallaxEffectEngineType = getParallaxEffectEngine()
        }
        Log.info("Parallax effect engine set to ${SettingsProvider.parallaxEffectEngineType}")
        parallaxEffectEngine = when(SettingsProvider.parallaxEffectEngineType) {
            SettingsProvider.ParallaxEffectEngineType.Gyro -> GyroParallaxEffectEngine()
            SettingsProvider.ParallaxEffectEngineType.Accelerometer -> AccelerometerParallaxEffectEngine()
            else -> EmptyParallaxEffectEngine()
        }
        parallaxEffectEngine = AccelerometerParallaxEffectEngine()
        fpsCounter.onMeasureListener = object : FpsCounter.OnMeasureListener {
            override fun onMeasure(frameTime: Double) {
                if(SettingsProvider.adaptiveFPS) {
                    if (frameTime > 16.7) {
                        maxStarParticleSpawnTime += 0.001
                        maxCloudParticleSpawnTime += 0.01
                    } else if (frameTime < 16.6) {
                        maxStarParticleSpawnTime -= 0.001
                        maxCloudParticleSpawnTime -= 0.01
                    }
                }
                Log.debug("frameTime=%.2f ms".format(frameTime))
            }
        }
    }

    private val context : Context = _context
    private val plane = Plane3D()
    private var aspect = vector2f(1.0f, 1.0f)
    private lateinit var starspriteShader : ProgramObject
    private lateinit var cloudspriteShader : ProgramObject
    private lateinit var starfieldShader : ProgramObject
    private lateinit var starfieldTexture : Texture
    private lateinit var starspritesTexture : Texture
    private lateinit var cloudspritesTexture : Texture
    private lateinit var noiseTexture : Texture
    private val timer = Timer()
    private val fpsCounter = FpsCounter(2.0)
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
    private var maxStarParticleSpawnTime = SettingsProvider.particlesSpawnTimeMultiplier
    private var maxCloudParticleSpawnTime = 10.0 * SettingsProvider.particlesSpawnTimeMultiplier

    private fun getParallaxEffectEngine() : SettingsProvider.ParallaxEffectEngineType {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return when {
            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null -> SettingsProvider.ParallaxEffectEngineType.Gyro
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null && sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null -> SettingsProvider.ParallaxEffectEngineType.Accelerometer
            else -> SettingsProvider.ParallaxEffectEngineType.None
        }
    }

    private fun getTextureCompressionMode(version : String, extensions : String) : SettingsProvider.TextureCompressionMode {
        return when {
            extensions.contains("KHR_compressed_texture_astc_ldr") || extensions.contains("OES_texture_compression_astc") -> SettingsProvider.TextureCompressionMode.ASTC
            version.contains("OpenGL ES 3") -> SettingsProvider.TextureCompressionMode.ETC2
            extensions.contains("OES_compressed_ETC2_RGB8_texture") && extensions.contains("OES_compressed_ETC2_RGBA8_texture") -> SettingsProvider.TextureCompressionMode.ETC2
            version.contains("OpenGL ES 2") -> SettingsProvider.TextureCompressionMode.ETC1
            extensions.contains("OES_compressed_ETC1_RGB8_texture") -> SettingsProvider.TextureCompressionMode.ETC1
            else -> SettingsProvider.TextureCompressionMode.NONE
        }
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
        Log.debug("onSurfaceCreated()")

        RandomGenerator.createSeed()

        val version = GLES20.glGetString(GLES20.GL_VERSION)
        val vendor = GLES20.glGetString(GLES20.GL_VENDOR)
        val renderer = GLES20.glGetString(GLES20.GL_RENDERER)
        val extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS)

        Log.info("OpenGL version: $version")
        Log.info("OpenGL vendor: $vendor")
        Log.info("OpenGL renderer: $renderer")
        Log.info("OpenGL extensions: $extensions")

        if(SettingsProvider.baseTextureQualityLevel >= SettingsProvider.TEXTURE_QUALITY_UNKNOWN) {
            SettingsProvider.baseTextureQualityLevel = getTextureBaseQualityLevel()
        }

        if(SettingsProvider.textureCompressionMode == SettingsProvider.TextureCompressionMode.UNKNOWN) {
            SettingsProvider.textureCompressionMode = getTextureCompressionMode(version, extensions)
        }

        val textureQuality = SettingsProvider.textureQualityLevel + SettingsProvider.baseTextureQualityLevel

        Log.info("Texture compression mode set to ${SettingsProvider.textureCompressionMode}")
        Log.info("Texture quality level set to $textureQuality")

        plane.create()

        if(SettingsProvider.textureCompressionMode == SettingsProvider.TextureCompressionMode.ASTC || SettingsProvider.textureCompressionMode == SettingsProvider.TextureCompressionMode.ETC2) {
            starspriteShader = ProgramObject.loadFromAssets(context, "shaders/starsprite.vert", "shaders/starsprite_pm.frag", plane.vertexFormat)
        } else {
            starspriteShader = ProgramObject.loadFromAssets(context, "shaders/starsprite.vert", "shaders/starsprite.frag", plane.vertexFormat)
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

        when(SettingsProvider.textureCompressionMode) {
            SettingsProvider.TextureCompressionMode.ASTC -> {
                //astc
                starspritesTexture = KTXLoader.loadFromAssets(
                    context,
                    "images/astc/starsprites.ktx",
                    textureQuality,
                    Texture.WrapMode.ClampToEdge,
                    Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear,
                    Texture.Filtering.Linear
                )
                starfieldTexture = KTXLoader.loadFromAssets(
                    context,
                    "images/astc/starfield.ktx",
                    textureQuality,
                    Texture.WrapMode.Repeat,
                    Texture.WrapMode.Repeat,
                    Texture.Filtering.LinearMipmapLinear,
                    Texture.Filtering.Linear
                )
            }
            SettingsProvider.TextureCompressionMode.ETC2 -> {
                //etc2
                starspritesTexture = KTXLoader.loadFromAssets(
                    context,
                    "images/etc2/starsprites.ktx",
                    textureQuality,
                    Texture.WrapMode.ClampToEdge,
                    Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear,
                    Texture.Filtering.Linear
                )
                starfieldTexture = KTXLoader.loadFromAssets(
                    context,
                    "images/etc2/starfield.ktx",
                    textureQuality,
                    Texture.WrapMode.Repeat,
                    Texture.WrapMode.Repeat,
                    Texture.Filtering.LinearMipmapLinear,
                    Texture.Filtering.Linear
                )
            }
            SettingsProvider.TextureCompressionMode.ETC1 -> {
                //etc
                starspritesTexture = Texture.loadFromAssets2D(
                    context,
                    "images/png/starsprites.png",
                    textureQuality,
                    Texture.WrapMode.ClampToEdge,
                    Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear,
                    Texture.Filtering.Linear
                )
                starfieldTexture = KTXLoader.loadFromAssets(
                    context,
                    "images/etc/starfield.ktx",
                    textureQuality,
                    Texture.WrapMode.Repeat,
                    Texture.WrapMode.Repeat,
                    Texture.Filtering.LinearMipmapLinear,
                    Texture.Filtering.Linear
                )
            }
            else -> {
                //png
                starspritesTexture = Texture.loadFromAssets2D(
                    context,
                    "images/png/starsprites.png",
                    textureQuality,
                    Texture.WrapMode.ClampToEdge,
                    Texture.WrapMode.ClampToEdge,
                    Texture.Filtering.LinearMipmapLinear,
                    Texture.Filtering.Linear
                )
                starfieldTexture = Texture.loadFromAssets2D(
                    context,
                    "images/png/starfield.png",
                    textureQuality,
                    Texture.WrapMode.Repeat,
                    Texture.WrapMode.Repeat,
                    Texture.Filtering.LinearMipmapLinear,
                    Texture.Filtering.Linear
                )
            }
        }

        //misc
        cloudspritesTexture = Texture.loadFromAssets2D(context, "images/png/cloud.png", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
        noiseTexture = Texture.loadFromAssets2D(context, "images/png/noise.png", textureQuality, Texture.WrapMode.Repeat, Texture.WrapMode.Repeat, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)

        eye.setPerspective(50.0f, 0.0f, 100.0f)
        eye.setLookAt(vector3f(0.0f, 0.0f, 0.0f), vector3f(0.0f, 0.0f, 1.0f), vector3f(0.0f, 1.0f, 0.0f))

        //opengl setup
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glClearDepthf(1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glBlendEquation(GLES20.GL_FUNC_ADD)

        timer.reset()
    }


    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        parallaxEffectEngine.reset = true
        parallaxEffectEngine.orientation = context.resources.configuration.orientation
        randomBackgroundOffset = RandomGenerator.rand2f(-1.0f, 1.0f)
        randomBackgroundRotation = RandomGenerator.randf(-M_PI, M_PI)
        eye.setViewport(vector2f(width.toFloat(), height.toFloat()))
        aspect = vector2f(width.toFloat() / height.toFloat(), 1.0f)
        Log.debug("onSurfaceChanged(width = $width, height = $height)")
    }

    override fun onDrawFrame(p0: GL10?) {

        var culledCounter = 0

        timer.tick()

        parallaxEffectEngine.onTick(timer.deltaTime.toFloat())

        eye.rotateBy(parallaxEffectEngine.offset)


        val viewProjectionMatrix = eye.viewProjectionMatrix
        val frustum = Frustum(viewProjectionMatrix)



        val backgroundTextureMatrix = matrix3f()
        backgroundTextureMatrix.loadIdentity()
        backgroundTextureMatrix.setRotationPart(randomBackgroundRotation)
        backgroundTextureMatrix.setTranslationPart(randomBackgroundOffset)

        //render background

        plane.bind()

        starfieldShader.bind()
        starfieldTexture.bind(0)

        starfieldShader.setSampler(starfieldSampler, 0)
        starfieldShader.setUniformValue(starfieldAspectUniform, aspect)
        starfieldShader.setUniformValue(starfieldTextureMatrixUniform, backgroundTextureMatrix)
        starfieldShader.setUniformValue(starfieldOffsetUniform, parallaxEffectEngine.backgroundOffset)
        starfieldShader.setUniformValue(starfieldTimeUniform, timer.currentTime.toFloat())

        plane.draw()

        starfieldTexture.release(0)
        starfieldShader.release()

        //remove all particles behind camera
        cloudSprites.removeAll { !frustum.isInDistance(it.position, -1.0f) }

        val eyeForward = eye.forward
        val eyePosition = eye.position

        lastCloudParticleSpawnTime += timer.deltaTime
        //if its time spawn new particle
        if(lastCloudParticleSpawnTime >= maxCloudParticleSpawnTime && cloudSprites.size < maxCloudParticlesCount) {
            cloudSprites.add(0, Particle.createCloud(eyeForward, eyePosition, particleSpawnDistance))
            lastCloudParticleSpawnTime = 0.0
            Log.info("cloudParticlesCount = ${cloudSprites.size}")
        }

        //render cloud sprites

        cloudspriteShader.bind()
        cloudspritesTexture.bind(0)

        cloudspriteShader.setSampler(cloudspriteSampler, 0)

        val cloud = cloudSprites.iterator()
        while(cloud.hasNext()) {


            val sprite = cloud.next()


            //face dir
            val dir = eyePosition - sprite.position
            dir.normalize()

            //normal
            val normal = vector3f(0.0f, 0.0f , -1.0f)

            val fadeIn = smoothstep(0.0f, 1.0f, sprite.age)
            val fadeOut = smoothstep(-1.0f, 2.5f, sprite.position.z)

            val boundingSphere = sprite.boundingSphere()

            if(frustum.contains(boundingSphere)) {

                val modelMatrix = sprite.calculateBillboardModelMatrix(dir, normal)

                cloudspriteShader.setUniformValue(cloudspriteModelViewProjectionMatrixUniform, viewProjectionMatrix * modelMatrix)
                cloudspriteShader.setUniformValue(cloudspriteUvRoIUniform, vector4f(sprite.uvRoI.left, sprite.uvRoI.top, sprite.uvRoI.width, sprite.uvRoI.height))
                cloudspriteShader.setUniformValue(cloudspriteColorUniform, sprite.color)
                cloudspriteShader.setUniformValue(cloudspriteFadeUniform, fadeIn * fadeOut)

                plane.draw()

            } else {
                culledCounter++
            }

            sprite.tick(eyeForward, particleSpeed * timer.deltaTime.toFloat())
        }


        cloudspritesTexture.release(0)
        cloudspriteShader.release()

        //remove all particles behind camera
        starSprites.removeAll { !frustum.isInDistance(it.position, -1.0f) }

        lastStarParticleSpawnTime += timer.deltaTime
        //if its time spawn new particle
        if(lastStarParticleSpawnTime >= maxStarParticleSpawnTime && starSprites.size < maxStarParticlesCount) {
            starSprites.add(0, Particle.createStar(eyeForward, eyePosition, particleSpawnDistance))
            lastStarParticleSpawnTime = 0.0
            Log.info("starParticlesCount = ${starSprites.size}")
        }

        //render stars sprites

        starspriteShader.bind()
        starspritesTexture.bind(0)
        noiseTexture.bind(1)

        starspriteShader.setSampler(starspriteSampler, 0)
        starspriteShader.setSampler(starspriteNoiseSampler, 1)

        val star = starSprites.iterator()
        while(star.hasNext()) {

            val sprite = star.next()

            //face dir
            val dir = eyePosition - sprite.position
            dir.normalize()

            //normal
            val normal = vector3f(0.0f, 0.0f , -1.0f)

            val fadeIn = smoothstep(0.0f, 1.0f, sprite.age)

            val rotMatrix = matrix3f()
            rotMatrix.setRotation(0.1f * timer.currentTime.toFloat())

            val boundingSphere = sprite.boundingSphere()

            if(frustum.contains(boundingSphere)) {

                val modelMatrix = sprite.calculateBillboardModelMatrix(dir, normal)

                starspriteShader.setUniformValue(starspriteModelViewProjectionMatrixUniform, viewProjectionMatrix * modelMatrix)
                starspriteShader.setUniformValue(starspriteRotationMatrixUniform, rotMatrix)
                starspriteShader.setUniformValue(starspriteUvRoIUniform, vector4f(sprite.uvRoI.left, sprite.uvRoI.top, sprite.uvRoI.width, sprite.uvRoI.height))
                starspriteShader.setUniformValue(starspriteFadeInUniform, fadeIn)

                plane.draw()

            } else {
                culledCounter++
            }

            sprite.tick(eyeForward, particleSpeed * timer.deltaTime.toFloat())
        }


        noiseTexture.release(1)
        starspritesTexture.release(0)
        starspriteShader.release()

        plane.release()

        fpsCounter.tick(timer.deltaTime)
//        Log.info("Culled $culledCounter sprites...")
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
        if(SettingsProvider.enableParallaxEffect) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            parallaxEffectEngine.connect(sensorManager)
        }
        particleSpeed = SettingsProvider.particleSpeed
        if(!SettingsProvider.adaptiveFPS) {
            maxStarParticleSpawnTime = SettingsProvider.particlesSpawnTimeMultiplier
            maxCloudParticleSpawnTime = 10.0 * SettingsProvider.particlesSpawnTimeMultiplier
        }
        eye.setLookAt(vector3f(0.0f, 0.0f, 0.0f), vector3f(0.0f, 0.0f, 1.0f), vector3f(0.0f, 1.0f, 0.0f))
        timer.reset()
    }

    override fun onPause() {
        if(SettingsProvider.enableParallaxEffect) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            parallaxEffectEngine.disconnect(sensorManager)
        }
    }

    companion object {

        fun createRenderer(context: Context) = StarfieldRenderer(context, "starfield.ini")
    }
}


