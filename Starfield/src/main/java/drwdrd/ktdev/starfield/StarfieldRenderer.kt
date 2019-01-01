package drwdrd.ktdev.starfield

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.GestureDetector
import android.view.MotionEvent
import drwdrd.ktdev.engine.*
import java.util.ArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class StarfieldRenderer(_context: Context) : GLSurfaceView.Renderer, GLWallpaperService.WallpaperLiveCycleListener {

    private val context : Context = _context
    private val simplePlane = Plane3D()
    private var aspect = vector2f(1.0f, 1.0f)
    private lateinit var starSpriteShader : ProgramObject
    private lateinit var starFieldShader : ProgramObject
    private lateinit var starFieldTexture : Texture
    private lateinit var starSpritesTexture : Texture
    private val timer = Timer(0.0002)
    private var lastParticleSpawnTime = 1000.0
    private val sprites : MutableList<StarParticle> = ArrayList()
    private var eye = Eye()
    private var resetGyro  = true

    private val maxParticlesCount = 1000        //hard limit just in case...
    private val maxParticleSpawnTime = 0.01

    inner class StarfieldGestureListener : GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
        override fun onDown(p0: MotionEvent?): Boolean {
            return false
        }

        override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
            return false
        }

        override fun onLongPress(p0: MotionEvent?) {

        }

        override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
            return false
        }

        override fun onShowPress(p0: MotionEvent?) {

        }

        override fun onSingleTapUp(p0: MotionEvent?): Boolean {
            return false
        }

        override fun onDoubleTap(p0: MotionEvent?): Boolean {
            return false
        }

        override fun onDoubleTapEvent(p0: MotionEvent?): Boolean {
            return false
        }

        override fun onSingleTapConfirmed(p0: MotionEvent?): Boolean {
            return false
        }
    }


    fun createGestureListener() = StarfieldGestureListener()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.debug("onSurfaceCreated()")

        val version = GLES20.glGetString(GLES20.GL_VERSION)
        val vendor = GLES20.glGetString(GLES20.GL_VENDOR)
        val renderer = GLES20.glGetString(GLES20.GL_RENDERER)

        Log.info("OpenGL version: $version")
        Log.info("OpenGL vendor: $vendor")
        Log.info("OpenGL renderer: $renderer")

        simplePlane.create()

        starSpriteShader = ProgramObject.loadFromAssets(context, "shaders/starsprite.vert", "shaders/starsprite.frag", simplePlane.vertexFormat)
        starFieldShader = ProgramObject.loadFromAssets(context, "shaders/starfield.vert", "shaders/starfield.frag", simplePlane.vertexFormat)

        starSpritesTexture = Texture.loadFromAssets(context, "images/starsprites.png", Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)
        starFieldTexture = Texture.loadFromAssets(context, "images/starfield.png", Texture.WrapMode.ClampToEdge, Texture.WrapMode.ClampToEdge, Texture.Filtering.LinearMipmapLinear, Texture.Filtering.Linear)

        eye.setPerspective(50.0f, 0.0f, 100.0f)

        //TODO: z of camera position as Camera Distance settings in customization
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
        eye.setViewport(vector2f(width.toFloat(), height.toFloat()))
        if(width < height) {
            aspect = vector2f(width.toFloat() / height.toFloat(), 1.0f)
        } else {
            aspect = vector2f(1.0f, height.toFloat() / width.toFloat())
        }
        Log.debug("onSurfaceChanged(width = $width, height = $height)")
    }

    override fun onDrawFrame(p0: GL10?) {

        timer.tick()

        //render background

        starFieldShader.bind()
        starFieldTexture.bind(1)

        starFieldShader.setUniformValue("u_Aspect", aspect)
        starFieldShader.setSampler("u_Starfield", 1)
        starFieldShader.setUniformValue("u_Time", timer.currentTime.toFloat())

        simplePlane.bind()
        simplePlane.draw()
        simplePlane.release()

        starFieldTexture.release(1)
        starFieldShader.release()

        val viewProjectionMatrix = eye.viewProjectionMatrix

        //remove all particles behind camera
        sprites.removeAll { it.position.z < -1.0f }

        lastParticleSpawnTime += timer.deltaTime
        //if its time spawn new particle
        if(lastParticleSpawnTime >= maxParticleSpawnTime && sprites.size < maxParticlesCount) {
            sprites.add(0, StarParticle.createRandom(5.0f))
            lastParticleSpawnTime = 0.0
            Log.debug("particlesCount = ${sprites.size}")
        }

        //render stars sprites

        starSpriteShader.bind()
        starSpritesTexture.bind(0)

        starSpriteShader.setSampler("u_StarSprites", 0)

        simplePlane.bind()

        var it = sprites.iterator()
        while(it.hasNext()) {

            var sprite = it.next()

            //face dir
            var dir = eye.position - sprite.position
            dir.normalize()

            //normal
            var normal = vector3f(0.0f, 0.0f , -1.0f)

            var fadeIn = smoothstep(0.0f, 1.0f, sprite.age)


            starSpriteShader.setUniformValue("u_ModelViewProjectionMatrix", viewProjectionMatrix * sprite.calculateBillboardModelMatrix(dir, normal))
            starSpriteShader.setUniformValue("u_uvRoI", vector4f(sprite.uvRoI.left, sprite.uvRoI.top, sprite.uvRoI.width, sprite.uvRoI.height))
            starSpriteShader.setUniformValue("u_FadeIn", fadeIn)
            simplePlane.draw()

            sprite.tick(timer.deltaTime.toFloat())
        }

        simplePlane.release()

        starSpritesTexture.release(0)
        starSpriteShader.release()
    }

    override fun onResume() {
        timer.reset()
    }

    override fun onPause() {
    }
}
