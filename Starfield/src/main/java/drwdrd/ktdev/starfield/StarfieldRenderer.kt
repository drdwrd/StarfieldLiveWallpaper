package drwdrd.ktdev.starfield

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import drwdrd.ktdev.engine.*
import java.util.ArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.atan2
import kotlin.math.sign
import kotlin.math.sqrt

const val gravityFilter = 0.95f         //TODO : move to settings???


const val starfieldSampler = 0
const val starfieldAspectUniform = 1
const val starfieldTextureMatrixUniform = 2
const val starfieldOffsetUniform  = 3
const val starfieldTimeUniform = 4

const val cloudspriteSampler = 0
const val cloudspriteModelViewProjectionMatrixUniform = 1
const val cloudspriteUvRoIUniform = 2
const val cloudspriteColorUniform = 3
const val cloudspriteFadeUniform = 4

const val starspriteSampler = 0
const val starspriteNoiseSampler = 1
const val starspriteModelViewProjectionMatrixUniform = 2
const val starspriteRotationMatrixUniform = 3
const val starspriteUvRoIUniform = 4
const val starspriteFadeInUniform = 5


class StarfieldRenderer private constructor(_context: Context) : GLSurfaceView.Renderer, GLWallpaperService.WallpaperLiveCycleListener, GLWallpaperService.OnOffsetChangedListener {

    constructor(_context : Context, file : String) : this(_context) {
        SettingsProvider.load(_context, file)
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
    private val timer = Timer(0.0002 * SettingsProvider.timeScale)
    private var lastStarParticleSpawnTime = 1000.0
    private val starSprites : MutableList<Particle> = ArrayList()
    private var lastCloudParticleSpawnTime = 1000.0
    private val cloudSprites : MutableList<Particle> = ArrayList()
    private val eye = Eye()
    private var resetGyro  = true
    private var lastXOffset = 0.0f
    private var dxOffset = 0.0f;
    private var backgroundOffset = vector2f(0.0f, 0.0f)

    private val gravityVector = vector3f(0.0f, 0.0f, 1.0f)
    private var lastGravity = vector3f(0.0f, 0.0f, 0.0f)

//    private val rotationVector = vector4f(0.0f, 0.0f, 0.0f, 0.0f)
//    private val lastRotationVector = vector2f(0.0f, 0.0f)
//    private var isDeviceRotated = false

    private val accelerometerSensorEventListener = AccelerometerSensorEventListener()
//    private var gravityOffset = vector2f(0.0f, 0.0f)

    private var randomBackgroundOffset = vector2f(0.0f, 0.0f)
    private var randomBackgroundRotation = 0.0f

    //global preferences
    private var maxStarParticleSpawnTime = SettingsProvider.starParticlesSpawnTime
    private var maxCloudParticleSpawnTime = SettingsProvider.cloudParticleSpawnTime
    private var parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier

    //precession
    private var precessionSpeed = 0.0f

    private val maxStarParticlesCount = 1000        //hard limit just in case...
    private val maxCloudParticlesCount = 200        //hard limit just in case...

    inner class AccelerometerSensorEventListener : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

        override fun onSensorChanged(event: SensorEvent?) {
            when(event?.sensor?.type) {
                Sensor.TYPE_GRAVITY -> {
                    gravityVector[0] = gravityVector[0] * gravityFilter + (1.0f - gravityFilter) * event.values[0]
                    gravityVector[1] = gravityVector[1] * gravityFilter + (1.0f - gravityFilter) * event.values[1]
                    gravityVector[2] = gravityVector[2] * gravityFilter + (1.0f - gravityFilter) * event.values[2]
                }
            }
        }
    }

//    fun createGestureListener() = StarfieldGestureListener()

    fun calculateGyroEffect() : vector2f {

        var g = gravityVector.normalized()
        var roll : Float = 0.0f
        var pitch : Float

        if(g.z != 0.0f) {
            roll = atan2(g.x, g.z) * 180.0f / Math.PI.toFloat()
        }

        pitch = sqrt(g.x * g.x + g.z * g.z)

        if(pitch != 0.0f) {
            pitch = atan2(g.y, pitch) * 180.0f / Math.PI.toFloat()
        }

        var dg = vector2f()

        dg[0] = (roll - lastGravity[0])

        dg[1] = (pitch - lastGravity[1])

// if device orientation is close to vertical – rotation around x is almost undefined – skip!

        if(g.y > 0.99) dg[0] = 0.0f

// if rotation was too intensive – more than 180 degrees – skip it

        if(dg[0] > 180.0f) dg[0] = 0.0f

        if(dg[0] < -180.0f) dg[0] = 0.0f

        if(dg[1] > 180.0f) dg[1] = 0.0f

        if(dg[1] < -180.0f) dg[1] = 0.0f

        if(context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            var tmp = dg[1]
            dg[1] = dg[0]
            dg[0] = tmp
        }

        lastGravity[0] = roll
        lastGravity[1] = pitch

        return dg
    }

/*
        private val rotationMatrix = matrix4f()

        fun calculateGyroEffect(isRotated : Boolean) : vector2f {
            val g = vector3f(0.0f, 0.0f, 0.0f)
            SensorManager.getRotationMatrixFromVector(rotationMatrix.toFloatArray(), rotationVector.toFloatArray())
            SensorManager.getOrientation(rotationMatrix.toFloatArray(), g.toFloatArray())
            val roll = clamp(g[2] - lastRotationVector[0], -M_PI, M_PI)
            val pitch = clamp(g[1] - lastRotationVector[1], -0.5f * M_PI, 0.5f * M_PI)

            lastRotationVector[0] = g[2]
            lastRotationVector[1] = g[1]
            if(isRotated) {
                return vector2f(pitch, roll)
            }
            return vector2f(roll, pitch)
        }
*/

    fun calculateParallaxEffect() : vector2f {
        val g = gravityVector.normalized()
        var dg : vector3f
        if(resetGyro) {
            dg = vector3f(0.0f, 0.0f, 0.0f)
            backgroundOffset = vector2f(0.0f, 0.0f)
            resetGyro = false
        } else {
            dg = g - lastGravity
        }
        lastGravity = g
        return vector2f(dg.x, dg.y)
    }



    fun getTextureBaseLevel() : Int {

        val maxTextureSize = IntArray(1)
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0)

        require(maxTextureSize[0] >= 512) { "Required GL_MAX_TEXTURE_SIZE >= 512" }

        return when(maxTextureSize[0]) {
            512 -> 3
            1024 -> 2
            2048 -> 1
            else -> 0
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.debug("onSurfaceCreated()")

        RandomGenerator.createSeed()

        val version = GLES20.glGetString(GLES20.GL_VERSION)
        val vendor = GLES20.glGetString(GLES20.GL_VENDOR)
        val renderer = GLES20.glGetString(GLES20.GL_RENDERER)

        Log.info("OpenGL version: $version")
        Log.info("OpenGL vendor: $vendor")
        Log.info("OpenGL renderer: $renderer")

        val textureQuality = SettingsProvider.textureQualityLevel + getTextureBaseLevel()

        Log.info("Texture quality level set to $textureQuality")

        plane.create()

        starspriteShader = ProgramObject.loadFromAssets(context, "shaders/starsprite.vert", "shaders/starsprite.frag", plane.vertexFormat)
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


        starspritesTexture = Texture.loadFromAssets2D(context, "images/starsprites.png", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
        cloudspritesTexture = Texture.loadFromAssets2D(context, "images/cloud.png", textureQuality, Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
        starfieldTexture = Texture.loadFromAssets2D(context, "images/starfield.png", textureQuality, Texture.WrapMode.Repeat, Texture.WrapMode.Repeat, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
        noiseTexture = Texture.loadFromAssets2D(context, "images/noise.png", textureQuality, Texture.WrapMode.Repeat, Texture.WrapMode.Repeat, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)

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
        resetGyro = true
        randomBackgroundOffset = RandomGenerator.rand2f(-1.0f, 1.0f)
        randomBackgroundRotation = RandomGenerator.randf(-M_PI, M_PI)
        eye.setViewport(vector2f(width.toFloat(), height.toFloat()))
        aspect = when(width < height) {
            true -> vector2f(width.toFloat() / height.toFloat(), 1.0f)
            false ->vector2f(1.0f, height.toFloat() / width.toFloat())
        }
        Log.debug("onSurfaceChanged(width = $width, height = $height)")
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT)

        var culledCounter = 0

        var dg = 0.1f * parallaxEffectScale * calculateParallaxEffect()

        backgroundOffset.plusAssign(dg)
        backgroundOffset.plusAssign(vector2f(dxOffset, 0.0f))

        eye.rotateBy(vector3f(-dg.y,dg.x + dxOffset, 0.0f))

        dxOffset = precessionSpeed

        val viewProjectionMatrix = eye.viewProjectionMatrix
        val frustum = Frustum(viewProjectionMatrix)

        timer.tick()


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
        starfieldShader.setUniformValue(starfieldOffsetUniform, backgroundOffset)
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
            cloudSprites.add(0, Particle.createCloud(eyeForward, eyePosition, 5.0f))
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

            sprite.tick(eyeForward, timer.deltaTime.toFloat())
        }


        cloudspritesTexture.release(0)
        cloudspriteShader.release()

        //remove all particles behind camera
        starSprites.removeAll { !frustum.isInDistance(it.position, -1.0f) }

        lastStarParticleSpawnTime += timer.deltaTime
        //if its time spawn new particle
        if(lastStarParticleSpawnTime >= maxStarParticleSpawnTime && starSprites.size < maxStarParticlesCount) {
            starSprites.add(0, Particle.createStar(eyeForward, eyePosition, 5.0f))
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
            rotMatrix.setRotation(0.4f * timer.currentTime.toFloat())

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

            sprite.tick(eyeForward, timer.deltaTime.toFloat())
        }


        noiseTexture.release(1)
        starspritesTexture.release(0)
        starspriteShader.release()

        plane.release()

        Log.info("Culled $culledCounter sprites...")
    }

    override fun onOffsetChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {
        dxOffset = 0.05f * parallaxEffectScale * (xOffset - lastXOffset)
        lastXOffset = xOffset
        precessionSpeed = sign(dxOffset) * SettingsProvider.precessionSpeed
    }

    override fun onResume() {
        if(SettingsProvider.enableParallaxEffect) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            sensorManager.registerListener(accelerometerSensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
        }
        timer.timeScale = 0.0002 * SettingsProvider.timeScale
        maxStarParticleSpawnTime = SettingsProvider.starParticlesSpawnTime
        maxCloudParticleSpawnTime = SettingsProvider.cloudParticleSpawnTime
        parallaxEffectScale = SettingsProvider.parallaxEffectMultiplier
        eye.setLookAt(vector3f(0.0f, 0.0f, 0.0f), vector3f(0.0f, 0.0f, 1.0f), vector3f(0.0f, 1.0f, 0.0f))
        timer.reset()
    }

    override fun onPause() {
        if(SettingsProvider.enableParallaxEffect) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensorManager.unregisterListener(accelerometerSensorEventListener)
        }
    }

    companion object {

        fun createRenderer(context: Context) = StarfieldRenderer(context, "starfield.ini")
    }
}
