package pers.lxt.sduinspection.activity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.model.User;
import pers.lxt.sduinspection.service.TokenService;
import pers.lxt.sduinspection.service.UserService;
import pers.lxt.sduinspection.util.ResponseCode;

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

    /**
     * Request code for judging if login is successful and this activity
     * is able to finish.
     */
    private static final int RC_IS_LOGIN_SUCCESS = 0;

    private GetUserTask mCheckTask = null;
    private User mCurrentUser;

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

            //启动初始化
            init(true);
        }
    };

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final Runnable mCallLoginSceneRunnable = new Runnable() {
        @Override
        public void run() {
            //显示状态栏、导航栏
            show();

            //显示登录按钮
            showLoginView();
        }
    };

    private final Runnable mCallMainSceneRunnable = new Runnable() {
        @Override
        public void run() {
            //显示状态栏、导航栏
            show();

            //显示主界面
            showMainActivity();
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
                startActivityForResult(new Intent(SplashActivity.this, LoginActivity.class), RC_IS_LOGIN_SUCCESS);
            }
        });

        loginImage = findViewById(R.id.logo_image);
        logoTextText = findViewById(R.id.logo_text);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_IS_LOGIN_SUCCESS && resultCode == RESULT_OK) {
            hideLoginView();
            init(false);
        }
    }

    private void init(boolean needDelay){
        String phone = TokenService.getInstance(SplashActivity.this).getPhone();
        String token = TokenService.getInstance(SplashActivity.this).getToken();
        if(phone == null || token == null){
            if(needDelay){
                delayCallNextScene(mCallLoginSceneRunnable, MIN_DELAY);
            }else{
                delayCallNextScene(mCallLoginSceneRunnable, 0);
            }
        }else{
            mCheckTask = new GetUserTask(phone, token, needDelay, SplashActivity.this);
            mCheckTask.execute((Void) null);
        }
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

    private void delayCallNextScene(Runnable scene, long delay) {
        mDelayHandler.removeCallbacks(mCallLoginSceneRunnable);
        mDelayHandler.removeCallbacks(mCallMainSceneRunnable);
        mDelayHandler.postDelayed(scene, delay);
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

    private void hideLoginView(){
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        loginImage.clearAnimation();
        logoTextText.clearAnimation();
        usePhoneButton.setVisibility(View.INVISIBLE);
    }

    private void showMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user", mCurrentUser);
        startActivity(intent);
        finish();
    }

    /**
     * Represents an asynchronous get user task used to get the login user.
     */
    public static class GetUserTask extends AsyncTask<Void, Void, Response<User>> {

        private WeakReference<SplashActivity> splashActivityReference;

        private final String mPhone;
        private final String mToken;
        private long mStartTime;
        private boolean mNeedDelay;

        GetUserTask(String phone, String token, boolean needDelay, SplashActivity splashActivity) {
            mPhone = phone;
            mToken = token;
            mNeedDelay = needDelay;
            this.splashActivityReference = new WeakReference<>(splashActivity);
        }

        @Override
        protected void onPreExecute() {
            mStartTime = System.currentTimeMillis();
        }

        @Override
        protected Response<User> doInBackground(Void... params) {
            SplashActivity activity = splashActivityReference.get();

            if(activity == null || activity.isFinishing()) return null;

            try {
                return UserService.getInstance(activity).getUser(mPhone, mToken);
            } catch (InterruptedException ignored) {
                return null;
            } catch (ServiceException e) {
                Log.e(GetUserTask.class.getName(), e.getCause().getMessage(), e);
                return new Response<>(e.getCause());
            }
        }

        @Override
        protected void onPostExecute(Response<User> response) {
            if (response == null) return;

            SplashActivity activity = splashActivityReference.get();

            if(activity == null || activity.isFinishing()) return;

            activity.mCheckTask = null;

            long left = (MIN_DELAY - System.currentTimeMillis() + mStartTime);
            if(left < 0 || !mNeedDelay) left = 0;

            if (response.getException() != null) {
                Toast.makeText(activity, R.string.error_unknown, Toast.LENGTH_LONG).show();
                activity.delayCallNextScene(activity.mCallLoginSceneRunnable, left);
            } else {
                switch (response.getCode()){
                    case ResponseCode.SUCCESS:
                        activity.mCurrentUser = response.getObject();
                        activity.delayCallNextScene(activity.mCallMainSceneRunnable, left);
                        break;
                    case ResponseCode.TOKEN_EXPIRED:
                        activity.delayCallNextScene(activity.mCallLoginSceneRunnable, left);
                        break;
                    default:
                        Toast.makeText(activity, R.string.error_unknown, Toast.LENGTH_LONG).show();
                        activity.delayCallNextScene(activity.mCallLoginSceneRunnable, left);
                        Log.e(GetUserTask.class.getName(),
                                "Unknown code: " + response.getCode() + ", message: " + response.getMessage());
                        break;
                }
            }
        }

        @Override
        protected void onCancelled() {
            SplashActivity activity = splashActivityReference.get();

            if(activity == null || activity.isFinishing()) return;

            activity.mCheckTask = null;

            long left = (MIN_DELAY - System.currentTimeMillis() + mStartTime);
            if(left < 0 || mNeedDelay) left = 0;

            activity.delayCallNextScene(activity.mCallLoginSceneRunnable, left);
        }
    }
}
