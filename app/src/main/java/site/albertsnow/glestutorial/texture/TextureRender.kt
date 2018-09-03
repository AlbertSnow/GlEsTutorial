package site.albertsnow.glestutorial.texture

import android.opengl.GLES20
import site.albertsnow.glestutorial.BaseRender
import site.albertsnow.glestutorial.MyApplication
import site.albertsnow.glestutorial.R
import site.albertsnow.glestutorial.util.TextureUtils
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class TextureRender : BaseRender() {
//    var textureId = 0
    var texturePointer : Int = 0

    val mCubeBuffer : FloatBuffer

    val textureBuffer : FloatBuffer
    private var mTextureCoordPointer: Int = 0

    init {
            val triangleVerticesArray : FloatArray = floatArrayOf(
                    1f, -1f, 0f,
                    1f, 1f, 0f,
                    -1f, 1f, 0f,

                    1f, -1f, 0f,
                    -1f, 1f, 0f,
                    -1f, -1f, 0f
            )
            mCubeBuffer =  floatBuffer(triangleVerticesArray.size * FLOAT_BYTE_SIZE)
            mCubeBuffer.put(triangleVerticesArray).position(0)

        val textureVerticesArray : FloatArray = floatArrayOf(
                1f, 0f, 0f,
                1f, 1f, 0f,
                0f, 1f, 0f,

                1f, 0f, 0f,
                0f, 1f, 0f,
                0f, 0f, 0f
        )
        textureBuffer =  floatBuffer(textureVerticesArray.size * FLOAT_BYTE_SIZE)
        textureBuffer.put(textureVerticesArray).position(0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
        val textureId = TextureUtils.loadTexture2(MyApplication.getInstance(), R.drawable.gundam)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
    }

    override fun initAttributeLocation() {
        mTextureCoordPointer = GLES20.glGetAttribLocation(mProgramPointer, "texCoord")
        texturePointer = GLES20.glGetUniformLocation(mProgramPointer, "s_texture")
    }

    override fun onCreateVertexShaderSource(): String {
        return """     uniform mat4 u_MVPMatrix;
                    attribute vec4 a_Position;

                    varying vec2 v_TexCoordinate;
                    attribute vec2 texCoord;


                    void main()
                    {
                       v_TexCoordinate = texCoord;

                       gl_Position = u_MVPMatrix  * a_Position;

                    }
            """
    }

    override fun onCreateFragmentShaderSource(): String {
        return """     precision mediump float;

                    varying vec2 v_TexCoordinate;
                    uniform sampler2D s_texture;

                    void main()
                    {
                        gl_FragColor = texture2D(s_texture,v_TexCoordinate);
                    }
            """
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glUniform1i(texturePointer, 0)

        mCubeBuffer.position(0)
        GLES20.glVertexAttribPointer(mPositionPointer, 3, GLES20.GL_FLOAT,
                false, 0, mCubeBuffer)
        GLES20.glEnableVertexAttribArray(mPositionPointer)

        textureBuffer.position(0)
        GLES20.glVertexAttribPointer(mTextureCoordPointer, 3, GLES20.GL_FLOAT,
                false, 0, textureBuffer)
        GLES20.glEnableVertexAttribArray(mTextureCoordPointer)

        applyMVP()
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
    }
}