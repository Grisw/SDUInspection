package pers.lxt.sduinspection.fragment;

import android.app.Fragment;
import android.content.Intent;
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
import pers.lxt.sduinspection.service.TaskService;
import pers.lxt.sduinspection.service.TokenService;
import pers.lxt.sduinspection.util.ResponseCode;

public class MainHomeTaskInfoFragment extends Fragment {

    private UpdateTaskTask mUpdateTaskTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_home_task_info, container, false);

        Bundle data = getArguments();
        final Task task = Objects.requireNonNull((Task) data.getSerializable("task"));

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).back();
            }
        });

        Button button = view.findViewById(R.id.change_state_button);
        switch (task.getState()){
            case T:
                button.setText(R.string.prompt_start_exe);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mUpdateTaskTask = new UpdateTaskTask(
                                TokenService.getInstance(getActivity()).getPhone(),
                                TokenService.getInstance(getActivity()).getToken(),
                                task, Task.State.D, MainHomeTaskInfoFragment.this
                        );
                        mUpdateTaskTask.execute((Void) null);
                    }
                });
                break;
            case D:
                button.setText(R.string.prompt_finish);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mUpdateTaskTask = new UpdateTaskTask(
                                TokenService.getInstance(getActivity()).getPhone(),
                                TokenService.getInstance(getActivity()).getToken(),
                                task, Task.State.E, MainHomeTaskInfoFragment.this
                        );
                        mUpdateTaskTask.execute((Void) null);
                    }
                });
                break;
            case E:
                button.setText(R.string.prompt_finished);
                button.setEnabled(false);
        }
        ((TextView)view.findViewById(R.id.task_info_title)).setText(task.getTitle());
        ((TextView)view.findViewById(R.id.task_info_content)).setText(task.getDescription());
        ((TextView)view.findViewById(R.id.task_info_creator)).setText(task.getCreatorName());
        ((TextView)view.findViewById(R.id.task_info_assignee)).setText(task.getAssigneeName());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        if(task.getDueTime() == null){
            ((TextView)view.findViewById(R.id.task_info_due_time)).setText(R.string.prompt_task_no_due);
        }else{
            ((TextView)view.findViewById(R.id.task_info_due_time)).setText(format.format(task.getDueTime()));
        }
        ((TextView)view.findViewById(R.id.task_info_publish_time)).setText(format.format(task.getPublishTime()));
        ((TextView)view.findViewById(R.id.task_info_state)).setText(task.getState().getState(getActivity()));
        ((TextView)view.findViewById(R.id.task_info_id)).setText(String.format(Locale.getDefault(), "%d", task.getId()));

        RecyclerView devicesRecycler = view.findViewById(R.id.task_info_devices);
        devicesRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        devicesRecycler.setAdapter(new TaskDeviceAdapter(this, task.getDevices()));
        devicesRecycler.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        return view;
    }

    public static class UpdateTaskTask extends AsyncTask<Void, Void, Response<Void>> {

        private WeakReference<MainHomeTaskInfoFragment> fragmentWeakReference;

        private final String mPhone;
        private final String mToken;
        private final Task mTask;
        private final Task.State mState;

        UpdateTaskTask(String phone, String token, Task task, Task.State state, MainHomeTaskInfoFragment fragment) {
            mPhone = phone;
            mToken = token;
            mTask = task;
            mState = state;
            this.fragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        protected Response<Void> doInBackground(Void... params) {
            MainHomeTaskInfoFragment fragment = fragmentWeakReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return null;

            try {
                return TaskService.getInstance(fragment.getActivity()).updateTaskState(mTask.getId(), mState, mPhone, mToken);
            } catch (InterruptedException ignored) {
                return null;
            } catch (ServiceException e) {
                Log.e(MainHomeTasksFragment.GetTasksTask.class.getName(), e.getCause().getMessage(), e);
                return new Response<>(e.getCause());
            } catch (JSONException e) {
                Log.e(MainHomeTasksFragment.GetTasksTask.class.getName(), e.getMessage(), e);
                return new Response<>(e);
            }
        }

        @Override
        protected void onPostExecute(Response<Void> response) {
            if (response == null) return;

            MainHomeTaskInfoFragment fragment = fragmentWeakReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return;

            fragment.mUpdateTaskTask = null;

            if (response.getException() != null) {
                Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
            } else {
                switch (response.getCode()){
                    case ResponseCode.SUCCESS:
                        mTask.setState(mState);
                        ((MainActivity) fragment.getActivity()).changeFragment(MainHomeFragment.class, null, true);
                        break;
                    case ResponseCode.TOKEN_EXPIRED:
                        Toast.makeText(fragment.getActivity(), R.string.prompt_login_again, Toast.LENGTH_LONG).show();
                        fragment.startActivity(new Intent(fragment.getActivity(), SplashActivity.class));
                        fragment.getActivity().finish();
                        break;
                    default:
                        Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                        Log.e(MainHomeTasksFragment.GetTasksTask.class.getName(),
                                "Unknown code: " + response.getCode() + ", message: " + response.getMessage());
                        break;
                }
            }
        }

        @Override
        protected void onCancelled() {
            MainHomeTaskInfoFragment fragment = fragmentWeakReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return;

            fragment.mUpdateTaskTask = null;
        }
    }
}
