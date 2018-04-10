package pers.lxt.sduinspection.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.adapter.ContactAdapter;
import pers.lxt.sduinspection.adapter.TaskAdapter;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.model.Task;
import pers.lxt.sduinspection.model.User;
import pers.lxt.sduinspection.service.TaskService;
import pers.lxt.sduinspection.service.TokenService;
import pers.lxt.sduinspection.util.ResponseCode;

public class MainActivity extends AppCompatActivity {

    private View vHome, vContacts, vMe;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    vHome.setVisibility(View.VISIBLE);
                    vContacts.setVisibility(View.GONE);
                    vMe.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_contacts:
                    vHome.setVisibility(View.GONE);
                    vContacts.setVisibility(View.VISIBLE);
                    vMe.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_me:
                    vHome.setVisibility(View.GONE);
                    vContacts.setVisibility(View.GONE);
                    vMe.setVisibility(View.VISIBLE);
                    return true;
            }
            return false;
        }
    };

    private Bundle mInitializeData;
    private Stack<View.OnClickListener> homeBack;

    private GetTasksTask getTasksTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vHome = findViewById(R.id.home);
        vContacts = findViewById(R.id.contacts);
        vMe = findViewById(R.id.me);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mInitializeData = getIntent().getBundleExtra("initialize");
        homeBack = new Stack<>();
        homeBack.push(null);
        initHome();
        initContacts();
        initMe();
    }

    private void initHome(){
        User user = (User) mInitializeData.getSerializable("user");
        @SuppressWarnings("unchecked")
        Map<Task.State, Integer> task_count = (Map<Task.State, Integer>) mInitializeData.getSerializable("task_count");
        int task_count_t = task_count != null ? task_count.get(Task.State.T) : 0;
        int task_count_d = task_count != null ? task_count.get(Task.State.D) : 0;
        int task_count_e = task_count != null ? task_count.get(Task.State.E) : 0;

        Toolbar toolbar = vHome.findViewById(R.id.toolbar);
        toolbar.setTitle(user != null ? user.getName() : "");

        ((TextView) vHome.findViewById(R.id.todo_count))
                .setText(String.format(Locale.getDefault(), "%d", task_count_t));

        ((TextView) vHome.findViewById(R.id.doing_count))
                .setText(String.format(Locale.getDefault(), "%d", task_count_d));

        ((TextView) vHome.findViewById(R.id.done_count))
                .setText(String.format(Locale.getDefault(), "%d", task_count_e));

        vHome.findViewById(R.id.todo_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHomeTaskRecycler(R.string.prompt_waiting, Task.State.T);
            }
        });
        vHome.findViewById(R.id.doing_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHomeTaskRecycler(R.string.prompt_executing, Task.State.D);
            }
        });
        vHome.findViewById(R.id.done_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHomeTaskRecycler(R.string.prompt_finished, Task.State.E);
            }
        });
    }

    private void showHomeTaskRecycler(int title, Task.State state){
        Toolbar toolbar = vHome.findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        homeBack.push(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideHomeRecycler();
            }
        });
        toolbar.setNavigationOnClickListener(homeBack.peek());

        vHome.findViewById(R.id.home_main).setVisibility(View.GONE);
        vHome.findViewById(R.id.home_recycler).setVisibility(View.VISIBLE);

        getTasksTask = new GetTasksTask(TokenService.getInstance(this).getPhone(),
                TokenService.getInstance(this).getToken(),
                state,this);
        getTasksTask.execute((Void) null);
    }

    private void updateTaskList(List<Task> tasks){
        RecyclerView recyclerView = vHome.findViewById(R.id.home_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new TaskAdapter(this, tasks));
    }

    private void hideHomeRecycler(){
        User user = (User) mInitializeData.getSerializable("user");
        Toolbar toolbar = vHome.findViewById(R.id.toolbar);
        toolbar.setTitle(user != null ? user.getName() : "");
        toolbar.setNavigationIcon(null);
        homeBack.pop();
        toolbar.setNavigationOnClickListener(homeBack.peek());

        vHome.findViewById(R.id.home_main).setVisibility(View.VISIBLE);
        vHome.findViewById(R.id.home_recycler).setVisibility(View.GONE);
        RecyclerView recyclerView = vHome.findViewById(R.id.home_recycler);
        ((TaskAdapter)recyclerView.getAdapter()).clear();
    }

    public void showHomeTaskInfo(Task task){
        Toolbar toolbar = vHome.findViewById(R.id.toolbar);
        final String originTitle = toolbar.getTitle().toString();
        toolbar.setTitle(R.string.title_task_info);
        homeBack.push(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideHomeTaskInfo(originTitle);
            }
        });
        toolbar.setNavigationOnClickListener(homeBack.peek());

        vHome.findViewById(R.id.home_recycler).setVisibility(View.GONE);
        vHome.findViewById(R.id.home_device_info).setVisibility(View.VISIBLE);

        Button button = vHome.findViewById(R.id.change_state_button);
        switch (task.getState()){
            case T:
                button.setText(R.string.prompt_start_exe);
                button.setEnabled(true);
                break;
            case D:
                button.setText(R.string.prompt_finish);
                button.setEnabled(true);
                break;
            case E:
                button.setText(R.string.prompt_finished);
                button.setEnabled(false);
        }
        ((TextView)vHome.findViewById(R.id.task_info_title)).setText(task.getTitle());
        ((TextView)vHome.findViewById(R.id.task_info_content)).setText(task.getDescription());
        ((TextView)vHome.findViewById(R.id.task_info_creator)).setText(task.getCreatorName());
        ((TextView)vHome.findViewById(R.id.task_info_assignee)).setText(task.getAssigneeName());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH-mm", Locale.getDefault());
        if(task.getDueTime() == null){
            ((TextView)vHome.findViewById(R.id.task_info_due_time)).setText(R.string.prompt_task_no_due);
        }else{
            ((TextView)vHome.findViewById(R.id.task_info_due_time)).setText(format.format(task.getDueTime()));
        }
        ((TextView)vHome.findViewById(R.id.task_info_publish_time)).setText(format.format(task.getPublishTime()));
        ((TextView)vHome.findViewById(R.id.task_info_state)).setText(task.getState().getState(this));
    }

    private void hideHomeTaskInfo(String title){
        Toolbar toolbar = vHome.findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        homeBack.pop();
        toolbar.setNavigationOnClickListener(homeBack.peek());

        vHome.findViewById(R.id.home_recycler).setVisibility(View.VISIBLE);
        vHome.findViewById(R.id.home_device_info).setVisibility(View.GONE);
    }

    private void initContacts(){
        User user = (User) mInitializeData.getSerializable("user");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> contacts = (List<Map<String, String>>) mInitializeData.getSerializable("contacts");

        Toolbar toolbar = vContacts.findViewById(R.id.toolbar);
        toolbar.setTitle(user != null ? user.getName() : "");

        RecyclerView contactsRecyclerView = vContacts.findViewById(R.id.contacts_view);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsRecyclerView.setAdapter(new ContactAdapter(contacts));
        contactsRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void initMe(){

    }

    private long mExitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            BottomNavigationView navigation = findViewById(R.id.navigation);
            if (homeBack.peek() != null && navigation.getSelectedItemId() == R.id.navigation_home){
                homeBack.peek().onClick(null);
                return true;
            }
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(MainActivity.this, R.string.prompt_press_back, Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public static class GetTasksTask extends AsyncTask<Void, Void, Response<List<Task>>> {

        private WeakReference<MainActivity> mainActivityWeakReference;

        private final String mPhone;
        private final String mToken;
        private final Task.State mState;

        GetTasksTask(String phone, String token, Task.State state, MainActivity mainActivity) {
            mPhone = phone;
            mToken = token;
            mState = state;
            this.mainActivityWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        protected Response<List<Task>> doInBackground(Void... params) {
            MainActivity activity = mainActivityWeakReference.get();

            if(activity == null || activity.isFinishing()) return null;

            try {
                return TaskService.getInstance(activity).getTasks(mPhone, mState, mPhone, mToken);
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

            MainActivity activity = mainActivityWeakReference.get();

            if(activity == null || activity.isFinishing()) return;

            activity.getTasksTask = null;

            if (response.getException() != null) {
                Toast.makeText(activity, R.string.error_unknown, Toast.LENGTH_LONG).show();
            } else {
                switch (response.getCode()){
                    case ResponseCode.SUCCESS:
                        activity.updateTaskList(response.getObject());
                        break;
                    case ResponseCode.TOKEN_EXPIRED:
                        Toast.makeText(activity, R.string.prompt_login_again, Toast.LENGTH_LONG).show();
                        activity.startActivity(new Intent(activity, SplashActivity.class));
                        activity.finish();
                        break;
                    default:
                        Toast.makeText(activity, R.string.error_unknown, Toast.LENGTH_LONG).show();
                        Log.e(LoginActivity.UserLoginTask.class.getName(),
                                "Unknown code: " + response.getCode() + ", message: " + response.getMessage());
                        break;
                }
            }
        }

        @Override
        protected void onCancelled() {
            MainActivity activity = mainActivityWeakReference.get();

            if(activity == null || activity.isFinishing()) return;

            activity.getTasksTask = null;
        }
    }
}
