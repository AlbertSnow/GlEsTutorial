package site.albertsnow.glestutorial.texture

import android.opengl.GLSurfaceView
import site.albertsnow.glestutorial.BaseGLActivity

class TextureActivity : BaseGLActivity() {

    override fun onCreateRender(): GLSurfaceView.Renderer {
        return TextureRender()
    }

}