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

public class MainHomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vHome = inflater.inflate(R.layout.fragment_main_home, container, false);

        Bundle initializeData = getArguments();
        if(initializeData == null){
            InitializeTask task = new InitializeTask(
                    TokenService.getInstance(getActivity()).getPhone(),
                    TokenService.getInstance(getActivity()).getToken(),
                    this
            );
            task.execute((Void) null);
        }else{
            init(vHome, initializeData);
        }

        return vHome;
    }

    private void init(View view, Bundle initializeData){
        @SuppressWarnings("unchecked")
        Map<Task.State, Integer> task_count = (Map<Task.State, Integer>) initializeData.getSerializable("task_count");
        int task_count_t = task_count != null ? task_count.get(Task.State.T) : 0;
        int task_count_d = task_count != null ? task_count.get(Task.State.D) : 0;
        int task_count_e = task_count != null ? task_count.get(Task.State.E) : 0;
        User user = (User) initializeData.getSerializable("user");
        UserService.getInstance(getActivity()).setCurrentUser(user);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(user != null ? user.getName() : "");

        ((TextView) view.findViewById(R.id.todo_count))
                .setText(String.format(Locale.getDefault(), "%d", task_count_t));

        ((TextView) view.findViewById(R.id.doing_count))
                .setText(String.format(Locale.getDefault(), "%d", task_count_d));

        ((TextView) view.findViewById(R.id.done_count))
                .setText(String.format(Locale.getDefault(), "%d", task_count_e));

        view.findViewById(R.id.todo_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFragment(Task.State.T);
            }
        });
        view.findViewById(R.id.doing_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFragment(Task.State.D);
            }
        });
        view.findViewById(R.id.done_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFragment(Task.State.E);
            }
        });
        view.findViewById(R.id.toolbar_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).changeFragment(MainHomeCreateTaskFragment.class, null, false, null);
            }
        });
    }

    private void showFragment(Task.State state){
        Bundle bundle = new Bundle();
        bundle.putSerializable("state", state);
        ((MainActivity)getActivity()).changeFragment(MainHomeTasksFragment.class, bundle, false, null);
    }

    /**
     * Represents an asynchronous task used to get the login user and some other info.
     */
    public static class InitializeTask extends AsyncTask<Void, Void, Map<String, Response>> {

        private WeakReference<MainHomeFragment> fragmentReference;

        private final String mPhone;
        private final String mToken;

        InitializeTask(String phone, String token, MainHomeFragment fragment) {
            mPhone = phone;
            mToken = token;
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Map<String, Response> doInBackground(Void... params) {
            MainHomeFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return null;

            Map<String, Response> responseMap = new HashMap<>();

            // Get user
            try {
                responseMap.put("user", UserService.getInstance(fragment.getActivity()).getUser(mPhone, mPhone, mToken));
            } catch (InterruptedException ignored) {
                return null;
            } catch (ServiceException e) {
                Log.e(SplashActivity.InitializeTask.class.getName(), e.getCause().getMessage(), e);
                responseMap.put("ex", new Response<>(e.getCause()));
                return responseMap;
            }

            // Get tasks count
            try {
                responseMap.put("task_count", TaskService.getInstance(fragment.getActivity()).getTasksCount(mPhone, mPhone, mToken));
            } catch (InterruptedException e) {
                return null;
            } catch (ServiceException e) {
                Log.e(SplashActivity.InitializeTask.class.getName(), e.getCause().getMessage(), e);
                responseMap.put("ex", new Response<>(e.getCause()));
                return responseMap;
            }
            return responseMap;
        }

        @Override
        protected void onPostExecute(Map<String, Response> responses) {
            if (responses == null) return;

            MainHomeFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return;

            if (responses.containsKey("ex")) {
                Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
            } else {
                boolean pass = true;
                Bundle bundle = new Bundle();
                Set<Map.Entry<String, Response>> entries = responses.entrySet();
                for(Map.Entry<String, Response> entry : entries){
                    switch (entry.getValue().getCode()){
                        case ResponseCode.SUCCESS:
                            bundle.putSerializable(entry.getKey(), (Serializable) entry.getValue().getObject());
                            break;
                        case ResponseCode.TOKEN_EXPIRED:
                            Toast.makeText(fragment.getActivity(), R.string.prompt_login_again, Toast.LENGTH_LONG).show();
                            fragment.startActivity(new Intent(fragment.getActivity(), SplashActivity.class));
                            fragment.getActivity().finish();
                            return;
                        default:
                            pass = false;
                            Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                            Log.e(InitializeTask.class.getName(),
                                    entry.getKey() + ": unknown code: " + entry.getValue().getCode() + ", message: " + entry.getValue().getMessage());
                            break;
                    }
                }
                if(pass){
                    fragment.init(Objects.requireNonNull(fragment.getView()), bundle);
                }
            }
        }
    }
}
