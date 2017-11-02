package eli.blueeye.v1.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import eli.blueeye.v1.R;
import eli.blueeye.v1.util.Util;

/**
 * 欢迎页面
 *
 * @author eli chang
 */
public class LauncherActivity extends AppCompatActivity {

    public static final String TAG = "LauncherActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_launcher);

        boolean isFirstLauncher = Util.isFirstLauncher(this);
        if (isFirstLauncher) {
            Util.setAlreadyLauncher(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setClass(LauncherActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.anim_activity_launcher_exit, R.anim.anim_activity_main_enter);
            }
        }, 2000);
    }
}
