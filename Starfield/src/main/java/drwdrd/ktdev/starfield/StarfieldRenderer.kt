package drwdrd.ktdev.starfield

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.opengl.Matrix
import android.view.GestureDetector
import android.view.MotionEvent
import drwdrd.ktdev.engine.*
import java.util.ArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.atan2
import kotlin.math.sqrt

const val gravityFilter = 0.8f

class StarfieldRenderer private constructor(_context: Context) : GLSurfaceView.Renderer, GLWallpaperService.WallpaperLiveCycleListener, GLWallpaperService.OnOffsetChangedListener {

    constructor(_context : Context, file : String) : this(_context) {
        Settings.load(_context, file)
    }

    private val context : Context = _context
    private val simplePlane = Plane3D()
    private var aspect = vector2f(1.0f, 1.0f)
    private lateinit var starSpriteShader : ProgramObject
    private lateinit var cloudSpriteShader : ProgramObject
    private lateinit var starFieldShader : ProgramObject
    private lateinit var starFieldTexture : Texture
    private lateinit var starSpritesTexture : Texture
    private lateinit var cloudSpritesTexture : Texture
    private lateinit var noiseTexture : Texture
    private val timer = Timer(0.0002 * Settings.timeScale)
    private var lastStarParticleSpawnTime = 1000.0
    private val starSprites : MutableList<Particle> = ArrayList()
    private var lastCloudParticleSpawnTime = 1000.0
    private val cloudSprites : MutableList<Particle> = ArrayList()
    private val eye = Eye()
    private var resetGyro  = true
    private var lastXOffset = 0.0f

    private val gravityVector = vector3f(0.0f, 0.0f, 0.0f)
    private var lastGravity = vector3f(0.0f, 0.0f, 0.0f)

//    private val rotationVector = vector4f(0.0f, 0.0f, 0.0f, 0.0f)
//    private val lastRotationVector = vector2f(0.0f, 0.0f)
//    private var isDeviceRotated = false

    private val accelerometerSensorEventListener = AccelerometerSensorEventListener()
    private var gravityOffset = vector2f(0.0f, 0.0f)

    private var randomBackgroundOffset = vector2f(0.0f, 0.0f)
    private var randomBackgroundRotation = 0.0f

    //global preferences
    private var maxStarParticleSpawnTime = Settings.starParticlesSpawnTime
    private var maxCloudParticleSpawnTime = Settings.cloudParticleSpawnTime
    private var parallaxEffectScale = Settings.parallaxEffectMultiplier

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
        val dg = g - lastGravity
        lastGravity = g
        return vector2f(dg.x, dg.y)
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

        simplePlane.create()

        starSpriteShader = ProgramObject.loadFromAssets(context, "shaders/starsprite.vert", "shaders/starsprite.frag", simplePlane.vertexFormat)
        cloudSpriteShader = ProgramObject.loadFromAssets(context, "shaders/cloudsprite.vert", "shaders/cloudsprite.frag", simplePlane.vertexFormat)
        starFieldShader = ProgramObject.loadFromAssets(context, "shaders/starfield.vert", "shaders/starfield.frag", simplePlane.vertexFormat)

        starSpritesTexture = Texture.loadFromAssets(context, "images/starsprites.png", Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
        cloudSpritesTexture = Texture.loadFromAssets(context, "images/cloud.png", Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
        starFieldTexture = Texture.loadFromAssets(context, "images/starfield.png", Texture.WrapMode.Repeat, Texture.WrapMode.Repeat, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
        noiseTexture = Texture.loadFromAssets(context, "images/noise.png", Texture.WrapMode.Repeat, Texture.WrapMode.Repeat, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)

        eye.setPerspective(50.0f, 0.0f, 100.0f)
        eye.setLookAt(vector3f(0.0f, 0.0f, -1.0f), vector3f(0.0f, 0.0f, 0.0f), vector3f(0.0f, 1.0f, 0.0f))

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
        if(width < height) {
            aspect = vector2f(width.toFloat() / height.toFloat(), 1.0f)
        } else {
            aspect = vector2f(1.0f, height.toFloat() / width.toFloat())
        }
        Log.debug("onSurfaceChanged(width = $width, height = $height)")
    }

    override fun onDrawFrame(p0: GL10?) {

        var culledCounter = 0

        var dg = 0.1f * parallaxEffectScale * calculateParallaxEffect()
        if(resetGyro) {
            dg = vector2f(0.0f, 0.0f)
            resetGyro = false
            gravityOffset = vector2f(0.0f, 0.0f)
        }

        gravityOffset.plusAssign(dg)

        eye.rotate(vector3f(-dg.y, dg.x, 0.0f))

        val viewProjectionMatrix = eye.viewProjectionMatrix
        val frustum = Frustum(viewProjectionMatrix)

        timer.tick()


        var backgroundTextureMatrix = matrix3f()
        backgroundTextureMatrix.loadIdentity()
        backgroundTextureMatrix.setRotationPart(randomBackgroundRotation)
        backgroundTextureMatrix.setTranslationPart(randomBackgroundOffset)

        //render background

        simplePlane.bind()

        starFieldShader.bind()
        starFieldTexture.bind(0)

        starFieldShader.setUniformValue("u_Aspect", aspect)
        starFieldShader.setSampler("u_Starfield", 0)
        starFieldShader.setUniformValue("u_TextureMatrix", backgroundTextureMatrix)
        starFieldShader.setUniformValue("u_Offset", gravityOffset)
        starFieldShader.setUniformValue("u_Time", timer.currentTime.toFloat())

        simplePlane.draw()

        starFieldTexture.release(0)
        starFieldShader.release()

        //remove all particles behind camera
        cloudSprites.removeAll { it.position.z < -1.0f }

        val spawningPoint = 5.0f * eye.forward
        val targetPoint = vector3f(0.0f, 0.0f, -1.0f)

        lastCloudParticleSpawnTime += timer.deltaTime
        //if its time spawn new particle
        if(lastCloudParticleSpawnTime >= maxCloudParticleSpawnTime && cloudSprites.size < maxCloudParticlesCount) {
            cloudSprites.add(0, Particle.createCloud(spawningPoint, targetPoint))
            lastCloudParticleSpawnTime = 0.0
            Log.debug("cloudParticlesCount = ${cloudSprites.size}")
        }

        //render cloud sprites

        cloudSpriteShader.bind()
        cloudSpritesTexture.bind(0)

        cloudSpriteShader.setSampler("u_CloudSprites", 0)

        var cloud = cloudSprites.iterator()
        while(cloud.hasNext()) {


            var sprite = cloud.next()


            //face dir
            var dir = eye.position - sprite.position
            dir.normalize()

            //normal
            var normal = vector3f(0.0f, 0.0f , -1.0f)

            var fadeIn = smoothstep(0.0f, 1.0f, sprite.age)
            var fadeOut = smoothstep(-1.0f, 2.5f, sprite.position.z)

            val boundingSphere = sprite.boundingSphere()

            if(frustum.contains(boundingSphere)) {

                val modelMatrix = sprite.calculateBillboardModelMatrix(dir, normal)

                cloudSpriteShader.setUniformValue("u_ModelViewProjectionMatrix", viewProjectionMatrix * modelMatrix)
                cloudSpriteShader.setUniformValue("u_uvRoI", vector4f(sprite.uvRoI.left, sprite.uvRoI.top, sprite.uvRoI.width, sprite.uvRoI.height))
                cloudSpriteShader.setUniformValue("u_Color", sprite.color)
                cloudSpriteShader.setUniformValue("u_Fade", fadeIn * fadeOut)
                simplePlane.draw()
            } else {
                culledCounter++
            }

            sprite.tick(eye, timer.deltaTime.toFloat())
        }


        cloudSpritesTexture.release(0)
        cloudSpriteShader.release()

        //remove all particles behind camera
        starSprites.removeAll { it.position.z < -1.0f }

        lastStarParticleSpawnTime += timer.deltaTime
        //if its time spawn new particle
        if(lastStarParticleSpawnTime >= maxStarParticleSpawnTime && starSprites.size < maxStarParticlesCount) {
            starSprites.add(0, Particle.createStar(spawningPoint, targetPoint))
            lastStarParticleSpawnTime = 0.0
            Log.debug("starParticlesCount = ${starSprites.size}")
        }

        //render stars sprites

        starSpriteShader.bind()
        starSpritesTexture.bind(0)
        noiseTexture.bind(1)

        starSpriteShader.setSampler("u_StarSprites", 0)
        starSpriteShader.setSampler("u_Noise", 1)

        var star = starSprites.iterator()
        while(star.hasNext()) {

            var sprite = star.next()

            //face dir
            var dir = eye.position - sprite.position
            dir.normalize()

            //normal
            var normal = vector3f(0.0f, 0.0f , -1.0f)

            var fadeIn = smoothstep(0.0f, 1.0f, sprite.age)

            var rotMatrix = matrix3f()
            rotMatrix.setRotation(0.4f * timer.currentTime.toFloat())

            val boundingSphere = sprite.boundingSphere()

            if(frustum.contains(boundingSphere)) {

                val modelMatrix = sprite.calculateBillboardModelMatrix(dir, normal)

                starSpriteShader.setUniformValue("u_ModelViewProjectionMatrix", viewProjectionMatrix * modelMatrix)
                starSpriteShader.setUniformValue("u_RotationMatrix", rotMatrix)
                starSpriteShader.setUniformValue("u_uvRoI", vector4f(sprite.uvRoI.left, sprite.uvRoI.top, sprite.uvRoI.width, sprite.uvRoI.height))
                starSpriteShader.setUniformValue("u_FadeIn", fadeIn)
                simplePlane.draw()
            } else {
                culledCounter++
            }

            sprite.tick(eye, timer.deltaTime.toFloat())
        }


        noiseTexture.release(1)
        starSpritesTexture.release(0)
        starSpriteShader.release()

        simplePlane.release()

        Log.debug("Culled $culledCounter sprites...")
    }

    override fun onOffsetChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {
        gravityOffset.plusAssign(vector2f(0.05f * parallaxEffectScale * (xOffset - lastXOffset), 0.0f))
        lastXOffset = xOffset
    }

    override fun onResume() {
        if(Settings.enableParallaxEffect) {
            var sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            var accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            sensorManager.registerListener(accelerometerSensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
        }

        timer.timeScale = 0.0002 * Settings.timeScale
        maxStarParticleSpawnTime = Settings.starParticlesSpawnTime
        maxCloudParticleSpawnTime = Settings.cloudParticleSpawnTime
        parallaxEffectScale = Settings.parallaxEffectMultiplier
        timer.reset()
    }

    override fun onPause() {
        if(Settings.enableParallaxEffect) {
            var sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensorManager.unregisterListener(accelerometerSensorEventListener)
        }
    }
}
