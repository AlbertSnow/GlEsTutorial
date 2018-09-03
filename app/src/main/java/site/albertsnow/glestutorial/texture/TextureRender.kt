package site.albertsnow.glestutorial.texture

import android.opengl.GLES20
import android.opengl.Matrix
import android.os.SystemClock
import site.albertsnow.glestutorial.BaseRender
import site.albertsnow.glestutorial.MyApplication
import site.albertsnow.glestutorial.R
import site.albertsnow.glestutorial.util.TextureUtils
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10

class TextureRender : BaseRender() {
    var mColorPointer: Int = 0
    var mTextureCoordPointer: Int = 0

    override fun onCreateVertexShaderSource(): String {
        return """     uniform mat4 u_MVPMatrix;
                    attribute vec4 a_Position;
//                    attribute vec4 a_Color;

                    varying vec2 v_TexCoordinate;
                    attribute vec2 texCoord;

//                    varying vec4 v_Color;

                    void main()
                    {
//                       v_Color = a_Color;
                       v_TexCoordinate = texCoord;

                       gl_Position = u_MVPMatrix  * a_Position;

                    }
            """
    }

    override fun onCreateFragmentShaderSource(): String {
        return """     precision mediump float;
//                    varying vec4 v_Color;
                      varying vec2 v_TexCoordinate;

                      uniform sampler2D s_texture;


                    void main()
                    {
//                       gl_FragColor = v_Color;
                        gl_FragColor = texture2D(s_texture,v_TexCoordinate);
                    }
            """
    }

    val mTriangleBuffer: FloatBuffer
    val textureBuffer: FloatBuffer

    init {
        val triangleVerticesArray: FloatArray = floatArrayOf(
                -0.5f, -0.5f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

                0.5f, -0.5f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                0.0f, 0.5f, 0.0f,
                0.0f, 1.0f, 0.0f, 1.0f,

                0.0f, 0.5f, 0.0f,
                0.0f, 1.0f, 0.0f, 1.0f,

                0.5f, -0.5f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                1f, 0.5f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f
        )
        mTriangleBuffer = floatBuffer(triangleVerticesArray.size * FLOAT_BYTE_SIZE)
        mTriangleBuffer.put(triangleVerticesArray).position(0)


        val textureVerticesArray: FloatArray = floatArrayOf(
                0f, 0f, 0f,
                1f, 0f, 0f,
                0f, 1f, 0f,

                0f, 1f, 0f,
                1f, 0f, 0f,
                1f, 1f, 0f
        )
        textureBuffer = floatBuffer(textureVerticesArray.size * FLOAT_BYTE_SIZE)
        textureBuffer.put(textureVerticesArray).position(0)
    }

    override fun initAttributeLocation() {
        super.initAttributeLocation()
//        mColorPointer = GLES20.glGetAttribLocation(mProgramPointer, "a_Color")
        mTextureCoordPointer = GLES20.glGetAttribLocation(mProgramPointer, "texCoord")
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

//        Matrix.setIdentityM(mModelMatrix, 0)
//        Matrix.translateM(mModelMatrix, 0, 0f, -0.5f, 0f)
//        Matrix.scaleM(mModelMatrix, 0, 1f, -1f, 1f)
//        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f)
//        drawTriangle(mTriangleBuffer)
    }

    private var textId: Int = -1

    private fun drawTriangle(triangleBuffer: FloatBuffer) {

        triangleBuffer.position(0)
        GLES20.glVertexAttribPointer(mPositionPointer, 3, GLES20.GL_FLOAT,
                false, FLOAT_BYTE_SIZE * 7, triangleBuffer)
        GLES20.glEnableVertexAttribArray(mPositionPointer)

//        triangleBuffer.position(3)
//        GLES20.glVertexAttribPointer(mColorPointer, 4, GLES20.GL_FLOAT,
//                false, FLOAT_BYTE_SIZE * 7, triangleBuffer)
//        GLES20.glEnableVertexAttribArray(mColorPointer)

        textureBuffer.position(0)
        GLES20.glVertexAttribPointer(mTextureCoordPointer, 3, GLES20.GL_FLOAT,
                false, FLOAT_BYTE_SIZE * 3, textureBuffer)
        GLES20.glEnableVertexAttribArray(mTextureCoordPointer)

        if (textId == -1) {
            val textureId = TextureUtils.loadTexture(MyApplication.getInstance(), R.drawable.gundam)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            //获取纹理s_texture
            val uTextureUnitLocation = GLES20.glGetUniformLocation(mProgramPointer, "s_texture")
            GLES20.glUniform1i(uTextureUnitLocation, 0)
            textId = textureId
        }

        applyMVP()

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
    }

    companion object {
        val TAG = "PrimaryRender"
    }
}