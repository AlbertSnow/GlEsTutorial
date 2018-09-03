package site.albertsnow.glestutorial;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    private static Application instance = null;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        instance = this;
    }

    public static Application getInstance() {
        return instance;
    }


}
