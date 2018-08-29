package site.albertsnow.glestutorial

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

abstract class BaseGLRender : GLSurfaceView.Renderer {
    val FLOAT_BYTE_SIZE: Int = 4
    public fun getFloatBuffer(capacity: Int): FloatBuffer? {
        return ByteBuffer.allocateDirect(capacity)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
    }


//    val mVertexShaderSource
//    val mFragmentShaderSource

    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private val mModelMatrix = FloatArray(16)

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private val mViewMatrix = FloatArray(16)

    /** Store the projection matrix. This is used to project the scene onto a 2D viewport.  */
    private val mProjectionMatrix = FloatArray(16)

    /** Allocate storage for the final combined matrix. This will be passed into the shader program.  */
    private val mMVPMatrix = FloatArray(16)

    public var mProgramPointer: Int = 0
    public var mVertexShaderPointer: Int = 0
    public var mFragmentShaderPointer: Int = 0
    public var mMVPMatrixPointer: Int = 0
    public var mPositionPointer: Int = 0
    public var mColorPointer: Int = 0

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f)

        initViewMatrix()
        createVertexShader()
        createFragmentShader()
        createProgram()

        initAttribLocation()

        GLES20.glUseProgram(mProgramPointer)
    }

    private fun initViewMatrix() {
        // Position the eye behind the origin.
        val eyeX = 0.0f
        val eyeY = 0.0f
        val eyeZ = 1.5f

        // We are looking toward the distance
        val lookX = 0.0f
        val lookY = 0.0f
        val lookZ = -5.0f

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        val upX = 0.0f
        val upY = 1.0f
        val upZ = 0.0f


        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ)
    }

    private fun initAttribLocation() {
        mMVPMatrixPointer = GLES20.glGetUniformLocation(mProgramPointer, "u_MVPMatrix")
        mPositionPointer = GLES20.glGetAttribLocation(mProgramPointer, "a_Position")
        mColorPointer = GLES20.glGetAttribLocation(mProgramPointer, "a_Color")
    }

    fun createProgram() {

        mProgramPointer = GLES20.glCreateProgram()
        if (mProgramPointer != 0) {
            GLES20.glAttachShader(mProgramPointer, mVertexShaderPointer)
            GLES20.glAttachShader(mProgramPointer, mFragmentShaderPointer)

            GLES20.glBindAttribLocation(mProgramPointer, 0, "a_Position")
            GLES20.glBindAttribLocation(mProgramPointer, 1, "a_Color")

            GLES20.glLinkProgram(mProgramPointer)
        }
    }


    fun doGLLogicalAndCheck(checkParamsName: Int, logicalFun: () -> Int,
                            handleErrorFun: () -> Unit = {},
                            handleSuccessFun: () -> Unit = {}) {
        val pointer = logicalFun()
        if (pointer != 0) {
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(pointer, checkParamsName, compileStatus, 0)

            if (compileStatus[0] == 0) {
                handleErrorFun()
                Log.e(TAG, "Vertex shader compile error")
            } else {
                handleSuccessFun()
            }
        }
    }

    fun createFragmentShader() {
         doGLLogicalAndCheck(GLES20.GL_COMPILE_STATUS,
                {
                    mFragmentShaderPointer = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
                    mFragmentShaderPointer
                },
                {
                    GLES20.glDeleteShader(mFragmentShaderPointer)
                })
    }

    fun createVertexShader() {
        doGLLogicalAndCheck(GLES20.GL_COMPILE_STATUS, {
            mVertexShaderPointer =  GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
            mVertexShaderPointer
        }, {
            GLES20.glDeleteShader(mVertexShaderPointer)
            mVertexShaderPointer =  0
        })

    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        val ratio = width.toFloat() / height
        val left = -ratio
        val right = ratio
        val bottom = -1.0f
        val top = 1.0f
        val near = 1.0f
        val far = 10.0f

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far)
    }

    override fun onDrawFrame(gl: GL10) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
//        doDrawFrame()
        updateDrawMatrix()
    }


    private fun updateDrawMatrix() {
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0)
        GLES20.glUniformMatrix4fv(mMVPMatrixPointer, 1, false, mMVPMatrix, 0)
    }

    companion object {
        val TAG = "PrimaryRender"
    }

}
