package site.albertsnow.opengltest.line

import android.opengl.GLES20
import android.opengl.Matrix
import android.os.SystemClock
import site.albertsnow.glestutorial.BaseRender
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10

class PrimaryRender : BaseRender() {
    override fun onCreateVertexShaderSource(): String {
        return """     uniform mat4 u_MVPMatrix;
                    attribute vec4 a_Position;
                    attribute vec4 a_Color;

                    varying vec4 v_Color;

                    void main()
                    {
                       v_Color = a_Color;

                       gl_Position = u_MVPMatrix  * a_Position;

                    }
            """
    }

    override fun onCreateFragmentShaderSource(): String {
        return """     precision mediump float;
                    varying vec4 v_Color;

                    void main()
                    {
                       gl_FragColor = v_Color;
                    }
            """
    }

    val mTriangleBuffer : FloatBuffer

    init {
        val triangleVerticesArray : FloatArray = floatArrayOf(
                -0.5f, -0.25f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

                0.5f, -0.25f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                0.0f, 0.559016994f, 0.0f,
                0.0f, 1.0f, 0.0f, 1.0f
        )
        mTriangleBuffer =  floatBuffer(triangleVerticesArray.size * FLOAT_BYTE_SIZE)
        mTriangleBuffer.put(triangleVerticesArray).position(0)
    }


    override fun onDrawFrame(gl: GL10) {
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
        applyMVP()

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
    }

    companion object {
        val TAG = "PrimaryRender"
    }
}
