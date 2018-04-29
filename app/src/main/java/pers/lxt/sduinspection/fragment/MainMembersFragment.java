package pers.lxt.sduinspection.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.activity.MainActivity;
import pers.lxt.sduinspection.activity.SplashActivity;
import pers.lxt.sduinspection.adapter.MemberAdapter;
import pers.lxt.sduinspection.adapter.DeviceAdapter;
import pers.lxt.sduinspection.model.Device;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.model.User;
import pers.lxt.sduinspection.service.DeviceService;
import pers.lxt.sduinspection.service.TokenService;
import pers.lxt.sduinspection.service.UserService;
import pers.lxt.sduinspection.util.ResponseCode;

public class MainMembersFragment extends Fragment {

    private List<User> users;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_members, container, false);

        view.findViewById(R.id.toolbar_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).changeFragment(MainMembersAddMemberFragment.class, null, false, MainMembersFragment.this);
            }
        });

        GetJuniorTask task = new GetJuniorTask(
                TokenService.getInstance(getActivity()).getPhone(),
                TokenService.getInstance(getActivity()).getToken(),
                this);
        task.execute((Void) null);

        return view;
    }

    private void updateTaskList(List<User> users){
        this.users = users;
        RecyclerView recyclerView = Objects.requireNonNull(getView()).findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new MemberAdapter(this, users));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTaskList(users);
    }

    public static class GetJuniorTask extends AsyncTask<Void, Void, Response<List<User>>> {

        private WeakReference<MainMembersFragment> fragmentReference;

        private final String mPhone;
        private final String mToken;

        GetJuniorTask(String phone, String token, MainMembersFragment fragment) {
            mPhone = phone;
            mToken = token;
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Response<List<User>> doInBackground(Void... params) {
            MainMembersFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return null;

            try {
                return UserService.getInstance(fragment.getActivity()).getJunior(
                        mPhone,
                        mPhone,
                        mToken
                );
            } catch (InterruptedException ignore) {
                return null;
            } catch (ServiceException e) {
                Log.e(GetJuniorTask.class.getName(), e.getCause().getMessage(), e);
                return new Response<>(e.getCause());
            }
        }

        @Override
        protected void onPostExecute(Response<List<User>> response) {
            if (response == null) return;

            MainMembersFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return;

            if (response.getException() != null) {
                Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
            } else {
                switch (response.getCode()){
                    case ResponseCode.SUCCESS:
                        fragment.updateTaskList(response.getObject());
                        break;
                    case ResponseCode.TOKEN_EXPIRED:
                        Toast.makeText(fragment.getActivity(), R.string.prompt_login_again, Toast.LENGTH_LONG).show();
                        fragment.startActivity(new Intent(fragment.getActivity(), SplashActivity.class));
                        fragment.getActivity().finish();
                        break;
                    default:
                        Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                        Log.e(GetJuniorTask.class.getName(),
                                "Unknown code: " + response.getCode() + ", message: " + response.getMessage());
                        break;
                }
            }
        }
    }

}
