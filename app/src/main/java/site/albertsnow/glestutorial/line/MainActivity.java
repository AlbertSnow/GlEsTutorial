package site.albertsnow.glestutorial.line;

import android.opengl.GLSurfaceView;

import site.albertsnow.glestutorial.BaseGLActivity;
import site.albertsnow.opengltest.line.PrimaryRender;

public class MainActivity extends BaseGLActivity {

    @Override
    protected GLSurfaceView.Renderer onCreateRender() {
        return new PrimaryRender();
    }

    @Override
    protected GLSurfaceView onCreateGlSurfaceView() {
        return new GLSurfaceView(this);
    }

}
