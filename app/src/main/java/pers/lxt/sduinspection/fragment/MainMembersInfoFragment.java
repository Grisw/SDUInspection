package pers.lxt.sduinspection.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.activity.MainActivity;
import pers.lxt.sduinspection.activity.SplashActivity;
import pers.lxt.sduinspection.adapter.TaskAdapter;
import pers.lxt.sduinspection.adapter.TaskDeviceAdapter;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.model.Task;
import pers.lxt.sduinspection.model.TaskDevice;
import pers.lxt.sduinspection.model.User;
import pers.lxt.sduinspection.service.TaskService;
import pers.lxt.sduinspection.service.TokenService;
import pers.lxt.sduinspection.util.ResponseCode;

public class MainMembersInfoFragment extends Fragment {

    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_members_info, container, false);

        Bundle data = getArguments();
        user = Objects.requireNonNull((User) data.getSerializable("user"));

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).back();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = Objects.requireNonNull(getView());

        view.findViewById(R.id.call_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + user.getPhoneNumber()));
                startActivity(intent);
            }
        });
        ((TextView)view.findViewById(R.id.name)).setText(user.getName());
        ((TextView)view.findViewById(R.id.phone)).setText(user.getPhoneNumber());
        ((TextView)view.findViewById(R.id.sex)).setText(user.getSex().getSex(getActivity()));
        if(user.getEmail() != null){
            ((TextView) Objects.requireNonNull(getView()).findViewById(R.id.email)).setText(user.getEmail());
        }else{
            ((TextView) Objects.requireNonNull(getView()).findViewById(R.id.email)).setText(R.string.unwrote);
        }
        if(user.getBirthday() != null){
            SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
            ((TextView) view.findViewById(R.id.birthday)).setText(format.format(user.getBirthday()));
        }else{
            ((TextView) view.findViewById(R.id.birthday)).setText(R.string.unwrote);
        }
        ((TextView)view.findViewById(R.id.leader)).setText(user.getLeaderName());

        GetTasksTask task = new GetTasksTask(
                TokenService.getInstance(getActivity()).getPhone(),
                TokenService.getInstance(getActivity()).getToken(),
                user.getPhoneNumber(),this);
        task.execute((Void) null);
    }

    private void updateTaskList(List<Task> tasks){
        RecyclerView devicesRecycler = Objects.requireNonNull(getView()).findViewById(R.id.tasks);
        devicesRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        devicesRecycler.setAdapter(new TaskAdapter(this, tasks));
        devicesRecycler.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
    }

    public static class GetTasksTask extends AsyncTask<Void, Void, Response<List<Task>>> {

        private WeakReference<MainMembersInfoFragment> fragmentWeakReference;

        private final String mPhone;
        private final String mToken;
        private final String mTargetPhone;

        GetTasksTask(String phone, String token, String targetPhone, MainMembersInfoFragment fragment) {
            mPhone = phone;
            mToken = token;
            this.mTargetPhone = targetPhone;
            this.fragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        protected Response<List<Task>> doInBackground(Void... params) {
            MainMembersInfoFragment fragment = fragmentWeakReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return null;

            try {
                return TaskService.getInstance(fragment.getActivity()).getTasks(mTargetPhone, Task.State.D, mPhone, mToken);
            } catch (InterruptedException ignored) {
                return null;
            } catch (ServiceException e) {
                Log.e(GetTasksTask.class.getName(), e.getCause().getMessage(), e);
                return new Response<>(e.getCause());
            }
        }

        @Override
        protected void onPostExecute(Response<List<Task>> response) {
            if (response == null) return;

            MainMembersInfoFragment fragment = fragmentWeakReference.get();

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
                        Log.e(GetTasksTask.class.getName(),
                                "Unknown code: " + response.getCode() + ", message: " + response.getMessage());
                        break;
                }
            }
        }
    }

}
