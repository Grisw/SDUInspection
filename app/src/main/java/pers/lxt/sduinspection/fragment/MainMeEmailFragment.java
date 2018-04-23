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
import pers.lxt.sduinspection.service.UserService;
import pers.lxt.sduinspection.util.ResponseCode;

public class MainMeEmailFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_me_email, container, false);

        String email = getArguments().getString("email");

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).back();
            }
        });

        if(email != null){
            ((EditText) view.findViewById(R.id.email)).setText(email);
        }

        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText email_view = Objects.requireNonNull(getView()).findViewById(R.id.email);

                String email = email_view.getText().toString();
                if(!email.matches("^[\\w-_.+]*[\\w-_.]@([\\w]+\\.)+[\\w]+[\\w]$")){
                    email_view.setError(getString(R.string.email_invalid));
                    email_view.requestFocus();
                    return;
                }

                ChangeEmailTask task = new ChangeEmailTask(
                        email,
                        TokenService.getInstance(getActivity()).getPhone(),
                        TokenService.getInstance(getActivity()).getToken(),
                        MainMeEmailFragment.this);
                task.execute((Void) null);
            }
        });
        return view;
    }

    public static class ChangeEmailTask extends AsyncTask<Void, Void, Response<Void>> {

        private WeakReference<MainMeEmailFragment> fragmentReference;

        private final String mPhone;
        private final String mToken;
        private final String mEmail;

        ChangeEmailTask(String email, String phone, String token, MainMeEmailFragment fragment) {
            mPhone = phone;
            mToken = token;
            mEmail = email;
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Response<Void> doInBackground(Void... params) {
            MainMeEmailFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return null;

            try {
                return UserService.getInstance(fragment.getActivity()).changeEmail(
                        mEmail,
                        mPhone,
                        mToken
                );
            } catch (InterruptedException ignore) {
                return null;
            } catch (ServiceException e) {
                Log.e(ChangeEmailTask.class.getName(), e.getCause().getMessage(), e);
                return new Response<>(e.getCause());
            } catch (JSONException e) {
                Log.e(ChangeEmailTask.class.getName(), e.getMessage(), e);
                return new Response<>(e);
            }
        }

        @Override
        protected void onPostExecute(Response<Void> response) {
            if (response == null) return;

            MainMeEmailFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return;

            if (response.getException() != null) {
                Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
            } else {
                switch (response.getCode()){
                    case ResponseCode.SUCCESS:
                        Toast.makeText(fragment.getActivity(), R.string.prompt_edit_success, Toast.LENGTH_SHORT).show();
                        ((MainMeFragment) fragment.getTargetFragment()).updateUserEmail(mEmail);
                        ((MainActivity) fragment.getActivity()).back();
                        break;
                    case ResponseCode.TOKEN_EXPIRED:
                        Toast.makeText(fragment.getActivity(), R.string.prompt_login_again, Toast.LENGTH_LONG).show();
                        fragment.startActivity(new Intent(fragment.getActivity(), SplashActivity.class));
                        fragment.getActivity().finish();
                        break;
                    default:
                        Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                        Log.e(ChangeEmailTask.class.getName(),
                                "Unknown code: " + response.getCode() + ", message: " + response.getMessage());
                        break;
                }
            }
        }
    }
}
