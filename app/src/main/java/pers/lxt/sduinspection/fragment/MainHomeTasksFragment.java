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
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.activity.LoginActivity;
import pers.lxt.sduinspection.activity.MainActivity;
import pers.lxt.sduinspection.activity.SplashActivity;
import pers.lxt.sduinspection.adapter.TaskAdapter;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.model.Task;
import pers.lxt.sduinspection.model.User;
import pers.lxt.sduinspection.service.TaskService;
import pers.lxt.sduinspection.service.TokenService;
import pers.lxt.sduinspection.util.ResponseCode;

public class MainHomeTasksFragment extends Fragment {

    private GetTasksTask mGetTasksTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_home_tasks, container, false);

        Bundle data = getArguments();
        Task.State state = Objects.requireNonNull((Task.State) data.getSerializable("state"));

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(state.getState(getActivity()));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).back();
            }
        });

        mGetTasksTask = new GetTasksTask(
                TokenService.getInstance(getActivity()).getPhone(),
                TokenService.getInstance(getActivity()).getToken(),
                state,this);
        mGetTasksTask.execute((Void) null);

        return view;
    }

    private void updateTaskList(List<Task> tasks){
        RecyclerView recyclerView = Objects.requireNonNull(getView()).findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new TaskAdapter(this, tasks));
    }

    public static class GetTasksTask extends AsyncTask<Void, Void, Response<List<Task>>> {

        private WeakReference<MainHomeTasksFragment> fragmentWeakReference;

        private final String mPhone;
        private final String mToken;
        private final Task.State mState;

        GetTasksTask(String phone, String token, Task.State state, MainHomeTasksFragment fragment) {
            mPhone = phone;
            mToken = token;
            mState = state;
            this.fragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        protected Response<List<Task>> doInBackground(Void... params) {
            MainHomeTasksFragment fragment = fragmentWeakReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return null;

            try {
                return TaskService.getInstance(fragment.getActivity()).getTasks(mPhone, mState, mPhone, mToken);
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

            MainHomeTasksFragment fragment = fragmentWeakReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return;

            fragment.mGetTasksTask = null;

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

        @Override
        protected void onCancelled() {
            MainHomeTasksFragment fragment = fragmentWeakReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return;

            fragment.mGetTasksTask = null;
        }
    }

}
