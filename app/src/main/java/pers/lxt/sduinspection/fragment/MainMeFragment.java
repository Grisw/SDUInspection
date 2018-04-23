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
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.activity.MainActivity;
import pers.lxt.sduinspection.activity.SplashActivity;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.model.Task;
import pers.lxt.sduinspection.model.User;
import pers.lxt.sduinspection.service.TaskService;
import pers.lxt.sduinspection.service.TokenService;
import pers.lxt.sduinspection.service.UserService;
import pers.lxt.sduinspection.util.ResponseCode;

public class MainMeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_me, container, false);

        init(view, UserService.getInstance(getActivity()).getCurrentUser());

        return view;
    }

    private void init(View view, final User user){
        ((TextView) view.findViewById(R.id.cap)).setText(user.getName().substring(0, 1).toUpperCase());
        ((TextView) view.findViewById(R.id.name)).setText(user.getName());
        ((TextView) view.findViewById(R.id.sex)).setText(user.getSex().getSex(getActivity()));
        ((TextView) view.findViewById(R.id.phone)).setText(user.getPhoneNumber());
        ((TextView) view.findViewById(R.id.leader)).setText(user.getLeaderName());
        if(user.getEmail() != null){
            ((TextView) view.findViewById(R.id.email)).setText(user.getEmail());
        }else{
            ((TextView) view.findViewById(R.id.email)).setText(R.string.unwrote);
        }
        if(user.getBirthday() != null){
            SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
            ((TextView) view.findViewById(R.id.birthday)).setText(format.format(user.getBirthday()));
        }else{
            ((TextView) view.findViewById(R.id.birthday)).setText(R.string.unwrote);
        }

        view.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogoutTask task = new LogoutTask(TokenService.getInstance(getActivity()).getPhone(),
                        TokenService.getInstance(getActivity()).getToken(), MainMeFragment.this);
                task.execute((Void) null);
            }
        });

        view.findViewById(R.id.change_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).changeFragment(MainMePasswordFragment.class, null, false, null);
            }
        });

        view.findViewById(R.id.email_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("email", user.getEmail());
                ((MainActivity) getActivity()).changeFragment(MainMeEmailFragment.class, bundle, false, MainMeFragment.this);
            }
        });
    }

    public void updateUserEmail(String email){
        UserService.getInstance(getActivity()).getCurrentUser().setEmail(email);
    }

    public static class LogoutTask extends AsyncTask<Void, Void, Response<Void>> {

        private WeakReference<MainMeFragment> fragmentReference;

        private final String mPhone;
        private final String mToken;

        LogoutTask(String phone, String token, MainMeFragment fragment) {
            mPhone = phone;
            mToken = token;
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Response<Void> doInBackground(Void... params) {
            MainMeFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return null;

            try {
                return TokenService.getInstance(fragment.getActivity()).deleteToken(
                        mPhone,
                        mToken
                );
            } catch (InterruptedException ignore) {
                return null;
            } catch (ServiceException e) {
                Log.e(LogoutTask.class.getName(), e.getCause().getMessage(), e);
                return new Response<>(e.getCause());
            }
        }

        @Override
        protected void onPostExecute(Response<Void> response) {
            if (response == null) return;

            MainMeFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return;

            if (response.getException() != null) {
                Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
            } else {
                switch (response.getCode()){
                    case ResponseCode.SUCCESS:
                    case ResponseCode.TOKEN_EXPIRED:
                        fragment.startActivity(new Intent(fragment.getActivity(), SplashActivity.class));
                        fragment.getActivity().finish();
                        break;
                    default:
                        Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                        Log.e(LogoutTask.class.getName(),
                                "Unknown code: " + response.getCode() + ", message: " + response.getMessage());
                        break;
                }
            }
        }
    }
}
