package site.albertsnow.glestutorial

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import site.albertsnow.glestutorial.line.LineActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    public fun onClick(clickView: android.view.View) {
        when (clickView.id) {
            R.id.main_triangle -> jumpIntent(LineActivity::class.java)
            R.id.main_cube -> jumpIntent(LineActivity::class.java)
            R.id.main_texture -> jumpIntent(LineActivity::class.java)
        }
    }

    private fun jumpIntent(jumpActivity: Class<out Activity>) {
        val intent = Intent(this, jumpActivity)
        startActivity(intent)
    }

}
