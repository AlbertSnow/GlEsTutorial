package site.albertsnow.glestutorial

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import site.albertsnow.opengltest.line.PrimaryRender
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

abstract class BaseRender : GLSurfaceView.Renderer {
    val FLOAT_BYTE_SIZE: Int = 4

    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    val mModelMatrix = FloatArray(16)

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    val mViewMatrix = FloatArray(16)

    /** Store the projection matrix. This is used to project the scene onto a 2D viewport.  */
    val mProjectionMatrix = FloatArray(16)

    /** Allocate storage for the final combined matrix. This will be passed into the shader program.  */
    val mMVPMatrix = FloatArray(16)

    var mProgramPointer: Int = 0
    var mVertexShaderPointer: Int = 0
    var mFragmentShaderPointer: Int = 0

    var mMVPMatrixPointer: Int = 0
    var mPositionPointer: Int = 0

    protected fun floatBuffer(capacity : Int) : FloatBuffer {
        return ByteBuffer.allocateDirect(capacity)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
    }


    fun initViewMatrix() {
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


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f)

        initViewMatrix()
        createVertexShader()
        createFragmentShader()
        createProgram()

        initAttributeLocation()

        GLES20.glUseProgram(mProgramPointer)
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


    protected open fun initAttributeLocation() {
        mMVPMatrixPointer = GLES20.glGetUniformLocation(mProgramPointer, "u_MVPMatrix")
        mPositionPointer = GLES20.glGetAttribLocation(mProgramPointer, "a_Position")
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

    fun createFragmentShader() {
        if (mVertexShaderPointer != 0) {
            mFragmentShaderPointer = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
            GLES20.glShaderSource(mFragmentShaderPointer, onCreateFragmentShaderSource())
            GLES20.glCompileShader(mFragmentShaderPointer)
        }
    }

    fun createVertexShader() {
        mVertexShaderPointer = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        if (mVertexShaderPointer != 0) {
            GLES20.glShaderSource(mVertexShaderPointer, onCreateVertexShaderSource())
            GLES20.glCompileShader(mVertexShaderPointer)

            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(mVertexShaderPointer, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(mVertexShaderPointer)
                mVertexShaderPointer = 0
                Log.e(PrimaryRender.TAG, "Vertex shader compile error")
            }
        }
    }

    fun applyMVP() {
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0)

        GLES20.glUniformMatrix4fv(mMVPMatrixPointer, 1, false, mMVPMatrix, 0)
    }


    /**
     * 必须包含
     *  "u_MVPMatrix"
     *  "a_Position"
     *  "a_Color")
     */
    abstract fun onCreateVertexShaderSource(): String

    abstract fun onCreateFragmentShaderSource(): String


}
