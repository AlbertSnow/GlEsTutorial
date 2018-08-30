package site.albertsnow.opengltest.line

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PrimaryRender : GLSurfaceView.Renderer {
    val FLOAT_BYTE_SIZE: Int = 4

    val mTriangleBuffer : FloatBuffer

    init {
//        val triangleVerticesArray : FloatArray = floatArrayOf(
//                -1.0f, 0f, 0f,
//                1.0f, 0f, 0f, 1f,
//                0f, 1f, 0f,
//                1.0f, 0f, 0f, 1f,
//                1.0f, 0f, 0f,
//                1.0f, 0f, 0f, 1f
//        )
        val triangleVerticesArray : FloatArray = floatArrayOf(
                -0.5f, -0.25f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

                0.5f, -0.25f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                0.0f, 0.559016994f, 0.0f,
                0.0f, 1.0f, 0.0f, 1.0f
        )
        mTriangleBuffer =  ByteBuffer.allocateDirect(
                triangleVerticesArray.size * FLOAT_BYTE_SIZE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        mTriangleBuffer.put(triangleVerticesArray).position(0)
    }


    val mVertexShaderSource =
            """     uniform mat4 u_MVPMatrix;
                    attribute vec4 a_Position;
                    attribute vec4 a_Color;

                    varying vec4 v_Color;

                    void main()
                    {
                       v_Color = a_Color;

                       gl_Position = u_MVPMatrix  * a_Position;

                    }
            """
    val mFragmentShaderSource =
            """     precision mediump float;
                    varying vec4 v_Color;

                    void main()
                    {
                       gl_FragColor = v_Color;
                    }
            """

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

    var mProgramPointer: Int = 0
    var mVertexShaderPointer: Int = 0
    var mFragmentShaderPointer: Int = 0
    var mMVPMatrixPointer: Int = 0
    var mPositionPointer: Int = 0
    var mColorPointer: Int = 0

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

    fun createFragmentShader() {
        if (mVertexShaderPointer != 0) {
            mFragmentShaderPointer = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
            GLES20.glShaderSource(mFragmentShaderPointer, mFragmentShaderSource)
            GLES20.glCompileShader(mFragmentShaderPointer)
        }
    }

    fun createVertexShader() {
        mVertexShaderPointer = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        if (mVertexShaderPointer != 0) {
            GLES20.glShaderSource(mVertexShaderPointer, mVertexShaderSource)
            GLES20.glCompileShader(mVertexShaderPointer)

            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(mVertexShaderPointer, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(mVertexShaderPointer)
                mVertexShaderPointer = 0
                Log.e(TAG, "Vertex shader compile error")
            }
        }
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
//        """     uniform mat4 u_MVPMatrix;              // A constant representing the combined model/view/projection matrix.
//
//                    attribute vec4 a_Position;             // Per-vertex position information we will pass in.
//                    attribute vec4 a_Color;

        //1. generate buffer
//        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

        // Do a complete rotation every 10 seconds.
        val time = SystemClock.uptimeMillis() % 10000L
        val angleInDegrees = 360.0f / 10000.0f * time.toInt()

        // Draw the triangle facing straight on.
        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f)
        drawTriangle(mTriangleBuffer)

        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, 0f, -0.5f, 0f)
        Matrix.scaleM(mModelMatrix, 0, 1f, -1f, 1f)
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f)
        drawTriangle(mTriangleBuffer)

    }

    /** How many bytes per float.  */
    private val mBytesPerFloat = 4

    /** How many elements per vertex.  */
    private val mStrideBytes = 7 * mBytesPerFloat

    /** Offset of the position data.  */
    private val mPositionOffset = 0

    /** Size of the position data in elements.  */
    private val mPositionDataSize = 3

    /** Offset of the color data.  */
    private val mColorOffset = 3

    /** Size of the color data in elements.  */
    private val mColorDataSize = 4

    private fun drawTriangle(triangleBuffer : FloatBuffer) {

        triangleBuffer.position(0)
        GLES20.glVertexAttribPointer(mPositionPointer, 3, GLES20.GL_FLOAT,
                false, FLOAT_BYTE_SIZE * 7, triangleBuffer)
        GLES20.glEnableVertexAttribArray(mPositionPointer)

        triangleBuffer.position(3)
        GLES20.glVertexAttribPointer(mColorPointer, 4, GLES20.GL_FLOAT,
                false, FLOAT_BYTE_SIZE * 7, triangleBuffer)
        GLES20.glEnableVertexAttribArray(mColorPointer)
//
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0)

        GLES20.glUniformMatrix4fv(mMVPMatrixPointer, 1, false, mMVPMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
    }

    companion object {
        val TAG = "PrimaryRender"
    }

}
