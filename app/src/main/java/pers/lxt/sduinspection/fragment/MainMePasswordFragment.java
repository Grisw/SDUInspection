package pers.lxt.sduinspection.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.Objects;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.activity.MainActivity;
import pers.lxt.sduinspection.activity.SplashActivity;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.service.TokenService;
import pers.lxt.sduinspection.util.ResponseCode;

public class MainMePasswordFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_me_password, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).back();
            }
        });

        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText oldPassword_view = Objects.requireNonNull(getView()).findViewById(R.id.old_password);
                EditText newPassword_view = getView().findViewById(R.id.new_password);
                EditText newPasswordAgain_view = getView().findViewById(R.id.new_password_again);

                String oldPassword = oldPassword_view.getText().toString();
                String newPassword = newPassword_view.getText().toString();
                String newPasswordAgain = newPasswordAgain_view.getText().toString();
                if(oldPassword.length() < 5){
                    oldPassword_view.setError(getString(R.string.error_invalid_password));
                    oldPassword_view.requestFocus();
                    return;
                }
                if(newPassword.length() < 5){
                    newPassword_view.setError(getString(R.string.error_invalid_password));
                    newPassword_view.requestFocus();
                    return;
                }
                if(!newPasswordAgain.equals(newPassword)){
                    newPasswordAgain_view.setError(getString(R.string.prompt_password_disagree));
                    newPasswordAgain_view.requestFocus();
                    return;
                }

                ChangePasswordTask task = new ChangePasswordTask(
                        newPassword, oldPassword,
                        TokenService.getInstance(getActivity()).getPhone(),
                        TokenService.getInstance(getActivity()).getToken(),
                        MainMePasswordFragment.this);
                task.execute((Void) null);
            }
        });
        return view;
    }

    private void setPasswordError(){
        EditText oldPassword_view = Objects.requireNonNull(getView()).findViewById(R.id.old_password);
        oldPassword_view.setError(getString(R.string.error_incorrect_password));
        oldPassword_view.requestFocus();
    }

    public static class ChangePasswordTask extends AsyncTask<Void, Void, Response<Void>> {

        private WeakReference<MainMePasswordFragment> fragmentReference;

        private final String mPhone;
        private final String mToken;
        private final String mPassword;
        private final String mOldPassword;

        ChangePasswordTask(String password, String oldPassword, String phone, String token, MainMePasswordFragment fragment) {
            mPhone = phone;
            mToken = token;
            mPassword = password;
            mOldPassword = oldPassword;
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Response<Void> doInBackground(Void... params) {
            MainMePasswordFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return null;

            try {
                return TokenService.getInstance(fragment.getActivity()).changePassword(
                        mPassword,
                        mOldPassword,
                        mPhone,
                        mToken
                );
            } catch (InterruptedException ignore) {
                return null;
            } catch (ServiceException e) {
                Log.e(ChangePasswordTask.class.getName(), e.getCause().getMessage(), e);
                return new Response<>(e.getCause());
            } catch (JSONException e) {
                Log.e(ChangePasswordTask.class.getName(), e.getMessage(), e);
                return new Response<>(e);
            }
        }

        @Override
        protected void onPostExecute(Response<Void> response) {
            if (response == null) return;

            MainMePasswordFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return;

            if (response.getException() != null) {
                Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
            } else {
                switch (response.getCode()){
                    case ResponseCode.SUCCESS:
                        Toast.makeText(fragment.getActivity(), R.string.prompt_edit_success, Toast.LENGTH_SHORT).show();
                        ((MainActivity) fragment.getActivity()).back();
                        break;
                    case ResponseCode.WRONG_CREDENTIALS:
                        fragment.setPasswordError();
                        break;
                    case ResponseCode.TOKEN_EXPIRED:
                        Toast.makeText(fragment.getActivity(), R.string.prompt_login_again, Toast.LENGTH_LONG).show();
                        fragment.startActivity(new Intent(fragment.getActivity(), SplashActivity.class));
                        fragment.getActivity().finish();
                        break;
                    default:
                        Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                        Log.e(ChangePasswordTask.class.getName(),
                                "Unknown code: " + response.getCode() + ", message: " + response.getMessage());
                        break;
                }
            }
        }
    }
}
