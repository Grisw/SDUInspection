package pers.lxt.sduinspection.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import pers.lxt.sduinspection.R;

/**
 * Splash界面
 */
public class SplashActivity extends AppCompatActivity {

    /**
     * 在初始化完成前提下，进入主界面最小等待时间。
     */
    private static final int MIN_DELAY = 2000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private Button usePhoneButton;
    private ImageView loginImage;
    private TextView logoTextText;

    private final Handler mDelayHandler = new Handler();

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

            //延迟启动下一界面
            delayCallNextScene();
        }
    };

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final Runnable mCallNextSceneRunnable = new Runnable() {
        @Override
        public void run() {
            //显示状态栏、导航栏
            show();

            //显示登录按钮
            showLoginView();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //设置透明通知栏和导航栏
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        //获取View
        usePhoneButton = findViewById(R.id.usePhone_button);
        usePhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
        });

        loginImage = findViewById(R.id.logo_image);
        logoTextText = findViewById(R.id.logo_text);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void hide() {
        // Schedule a runnable to remove the status and navigation bar after a delay
        mDelayHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        //防止反复调用
        mDelayHandler.removeCallbacks(mHidePart2Runnable);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mDelayHandler.removeCallbacks(mHideRunnable);
        mDelayHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void delayCallNextScene() {
        mDelayHandler.removeCallbacks(mCallNextSceneRunnable);
        mDelayHandler.postDelayed(mCallNextSceneRunnable, MIN_DELAY);
    }

    private void showLoginView() {
        Animation splashLogoAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_logo);
        loginImage.startAnimation(splashLogoAnimation);
        logoTextText.startAnimation(splashLogoAnimation);

        Animator animator = ViewAnimationUtils.createCircularReveal(usePhoneButton, 0, 0, 0,
                (float) Math.hypot(usePhoneButton.getWidth(), usePhoneButton.getHeight()));
        animator.setDuration(600);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
        usePhoneButton.setVisibility(View.VISIBLE);
    }
}
