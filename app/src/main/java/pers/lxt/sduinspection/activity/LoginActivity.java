package pers.lxt.sduinspection.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;

import java.lang.ref.WeakReference;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.service.TokenService;
import pers.lxt.sduinspection.util.ResponseCode;

/**
 * A login screen that offers login via phone/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mPhoneView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Set up the login form.
        mPhoneView = findViewById(R.id.phone_number);
        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mPhoneView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String phone = mPhoneView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid phone address.
        if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        } else if (!isPhoneValid(phone)) {
            mPhoneView.setError(getString(R.string.error_invalid_phone));
            focusView = mPhoneView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(phone, password, this);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isPhoneValid(String phone) {
        return phone.length() == 11;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Represents an asynchronous login task used to authenticate
     * the user.
     */
    public static class UserLoginTask extends AsyncTask<Void, Void, Response<String>> {

        private WeakReference<LoginActivity> loginActivityReference;

        private final String mPhone;
        private final String mPassword;

        UserLoginTask(String phone, String password, LoginActivity loginActivity) {
            mPhone = phone;
            mPassword = password;
            this.loginActivityReference = new WeakReference<>(loginActivity);
        }

        @Override
        protected Response<String> doInBackground(Void... params) {
            LoginActivity activity = loginActivityReference.get();

            if(activity == null || activity.isFinishing()) return null;

            try {
                return TokenService.getInstance(activity).requestToken(mPhone, mPassword);
            } catch (InterruptedException ignored) {
                return null;
            } catch (JSONException e) {
                Log.e(TokenService.class.getName(), e.getMessage(), e);
                return new Response<>(e);
            } catch (ServiceException e) {
                Log.e(TokenService.class.getName(), e.getCause().getMessage(), e);
                return new Response<>(e.getCause());
            }
        }

        @Override
        protected void onPostExecute(Response<String> response) {
            if (response == null) return;

            LoginActivity activity = loginActivityReference.get();

            if(activity == null || activity.isFinishing()) return;

            activity.mAuthTask = null;
            activity.showProgress(false);

            if (response.getException() != null) {
                Log.i("Test", "Hint error!");
            } else {
                switch (response.getCode()){
                    case ResponseCode.SUCCESS:
                        Log.i("Test", "Login success!" + response.getObject());
                        TokenService.getInstance(activity).setToken(response.getObject());
                        break;
                    case ResponseCode.WRONG_CREDENTIALS:
                        activity.mPasswordView.setError(activity.getString(R.string.error_incorrect_password));
                        activity.mPasswordView.requestFocus();
                        break;
                    default:
                        Log.e(UserLoginTask.class.getName(),
                                "Unknown code: " + response.getCode() + ", message: " + response.getMessage());
                        break;
                }
            }
        }

        @Override
        protected void onCancelled() {
            LoginActivity activity = loginActivityReference.get();

            if(activity == null || activity.isFinishing()) return;

            activity.mAuthTask = null;
            activity.showProgress(false);
        }
    }
}

