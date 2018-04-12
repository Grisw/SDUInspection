package pers.lxt.sduinspection.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.activity.MainActivity;
import pers.lxt.sduinspection.activity.SplashActivity;
import pers.lxt.sduinspection.adapter.CreateTaskDeviceAdapter;
import pers.lxt.sduinspection.model.Device;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.model.User;
import pers.lxt.sduinspection.service.DeviceService;
import pers.lxt.sduinspection.service.TaskService;
import pers.lxt.sduinspection.service.TokenService;
import pers.lxt.sduinspection.service.UserService;
import pers.lxt.sduinspection.util.ResponseCode;

public class MainHomeCreateTaskFragment extends Fragment {

    private Date dueTime;
    private User assignee;
    private List<Device> devices;

    private CreateTaskDeviceAdapter createTaskDeviceAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_home_create_task, container, false);

        assignee = UserService.getInstance(getActivity()).getCurrentUser();
        ((TextView) view.findViewById(R.id.create_task_assignee)).setText(assignee.getName());
        devices = new ArrayList<>();
        createTaskDeviceAdapter = new CreateTaskDeviceAdapter(devices);

        RecyclerView recyclerView = view.findViewById(R.id.create_task_devices);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(createTaskDeviceAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).back();
            }
        });

        view.findViewById(R.id.create_task_add_due).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final Calendar current = Calendar.getInstance();
                if(dueTime != null){
                    current.setTime(dueTime);
                }
                final Calendar selected = Calendar.getInstance();
                new DatePickerDialog(getActivity(), 0, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, final int year, final int month, final int date) {
                        new TimePickerDialog(getActivity(), 0, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                                selected.set(year, month, date, hour, minute, 0);

                                if(selected.before(current)){
                                    Toast.makeText(getActivity(), R.string.prompt_due_time_reject, Toast.LENGTH_SHORT).show();
                                }else{
                                    dueTime = selected.getTime();
                                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                    ((TextView) Objects.requireNonNull(getView()).findViewById(R.id.create_task_due)).setText(format.format(dueTime));
                                }
                            }
                        }, current.get(Calendar.HOUR_OF_DAY), current.get(Calendar.MINUTE), true).show();
                    }
                }, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        view.findViewById(R.id.create_task_add_assignee).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetJuniorTask task = new GetJuniorTask(TokenService.getInstance(getActivity()).getPhone(),
                        TokenService.getInstance(getActivity()).getToken(), MainHomeCreateTaskFragment.this);
                task.execute((Void) null);
            }
        });
        view.findViewById(R.id.create_task_add_device).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetDevicesTask task = new GetDevicesTask(TokenService.getInstance(getActivity()).getPhone(),
                        TokenService.getInstance(getActivity()).getToken(), MainHomeCreateTaskFragment.this);
                task.execute((Void) null);
            }
        });
        view.findViewById(R.id.create_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText title_view = Objects.requireNonNull(getView()).findViewById(R.id.title);
                EditText description_view = getView().findViewById(R.id.description);

                String title = title_view.getText().toString();
                String description = description_view.getText().toString();
                if(title.length() < 3){
                    title_view.setError(getString(R.string.prompt_title_length));
                    title_view.requestFocus();
                    return;
                }
                if(description.length() < 5){
                    description_view.setError(getString(R.string.prompt_description_length));
                    description_view.requestFocus();
                    return;
                }

                List<Integer> deviceIds = new ArrayList<>();
                for (Device device : devices){
                    deviceIds.add(device.getId());
                }

                CreateTaskTask task = new CreateTaskTask(
                        title, description, assignee.getPhoneNumber(), dueTime, deviceIds,
                        TokenService.getInstance(getActivity()).getPhone(),
                        TokenService.getInstance(getActivity()).getToken(),
                        MainHomeCreateTaskFragment.this);
                task.execute((Void) null);
            }
        });
        return view;
    }

    private void showChooseAssigneeDialog(final List<User> users) {
        List<Map<String, String>> adapterList = new ArrayList<>();
        int index = 0;
        for(int i = 0; i < users.size(); i++){
            User user = users.get(i);
            if(assignee != null && user.getPhoneNumber().equals(assignee.getPhoneNumber())){
                index = i;
            }
            Map<String, String> map = new HashMap<>();
            map.put("name", user.getName());
            map.put("phone", user.getPhoneNumber());
            adapterList.add(map);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.prompt_main_assignee);
        builder.setSingleChoiceItems(new SimpleAdapter(getActivity(), adapterList, R.layout.adapter_dialog_assignee, new String[]{"name", "phone"}, new int[]{R.id.name, R.id.phone}),
                index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                assignee = users.get(i);
                ((TextView) Objects.requireNonNull(getView()).findViewById(R.id.create_task_assignee))
                        .setText(assignee.getName());
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(R.string.prompt_to_myself, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                assignee = UserService.getInstance(getActivity()).getCurrentUser();
                ((TextView) Objects.requireNonNull(getView()).findViewById(R.id.create_task_assignee))
                        .setText(assignee.getName());
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void showChooseDeviceDialog(final List<Device> devices) {
        Spanned[] items = new Spanned[devices.size()];
        final boolean[] checked = new boolean[items.length];
        for(int i = 0; i < items.length; i++){
            Device device = devices.get(i);
            items[i] = Html.fromHtml("\t" + device.getName() + "<br />\t<small>" + device.getId() + "</small>");
            checked[i] = false;
            if(MainHomeCreateTaskFragment.this.devices != null){
                for(Device d : MainHomeCreateTaskFragment.this.devices){
                    if(d.getId() == device.getId()){
                        checked[i] = true;
                        break;
                    }
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.prompt_main_device);
        builder.setMultiChoiceItems(items, checked, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                checked[i] = b;
            }
        });
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MainHomeCreateTaskFragment.this.devices.clear();
                for(int j = 0; j < checked.length; j++){
                    if(checked[j]){
                        MainHomeCreateTaskFragment.this.devices.add(devices.get(j));
                    }
                }
                createTaskDeviceAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    public void finishCreating(int id){
        Toast.makeText(getActivity(), "创建任务ID：" + id, Toast.LENGTH_SHORT).show();
        ((MainActivity) getActivity()).changeFragment(MainHomeFragment.class, null, true);
    }

    public static class CreateTaskTask extends AsyncTask<Void, Void, Response<Integer>> {

        private WeakReference<MainHomeCreateTaskFragment> fragmentReference;

        private final String mPhone;
        private final String mToken;
        private final String mTitle;
        private final String mDescription;
        private final String mAssignee;
        private final Date mDueTime;
        private final List<Integer> mDevices;

        CreateTaskTask(String title, String description, String assignee, Date dueTime, List<Integer> devices, String phone, String token, MainHomeCreateTaskFragment fragment) {
            mPhone = phone;
            mToken = token;
            mTitle = title;
            mDescription = description;
            mAssignee = assignee;
            mDueTime = dueTime;
            mDevices = devices;
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Response<Integer> doInBackground(Void... params) {
            MainHomeCreateTaskFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return null;

            try {
                return TaskService.getInstance(fragment.getActivity()).createTask(
                        mTitle,
                        mDescription,
                        mAssignee,
                        mDueTime,
                        mDevices,
                        mPhone,
                        mToken
                );
            } catch (InterruptedException ignore) {
                return null;
            } catch (ServiceException | JSONException e) {
                Log.e(CreateTaskTask.class.getName(), e.getCause().getMessage(), e);
                return new Response<>(e.getCause());
            }
        }

        @Override
        protected void onPostExecute(Response<Integer> response) {
            if (response == null) return;

            MainHomeCreateTaskFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return;

            if (response.getException() != null) {
                Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
            } else {
                switch (response.getCode()){
                    case ResponseCode.SUCCESS:
                        fragment.finishCreating(response.getObject());
                        break;
                    case ResponseCode.TOKEN_EXPIRED:
                        Toast.makeText(fragment.getActivity(), R.string.prompt_login_again, Toast.LENGTH_LONG).show();
                        fragment.startActivity(new Intent(fragment.getActivity(), SplashActivity.class));
                        fragment.getActivity().finish();
                        break;
                    default:
                        Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                        Log.e(CreateTaskTask.class.getName(),
                                "Unknown code: " + response.getCode() + ", message: " + response.getMessage());
                        break;
                }
            }
        }
    }

    public static class GetDevicesTask extends AsyncTask<Void, Void, Response<List<Device>>> {

        private WeakReference<MainHomeCreateTaskFragment> fragmentReference;

        private final String mPhone;
        private final String mToken;

        GetDevicesTask(String phone, String token, MainHomeCreateTaskFragment fragment) {
            mPhone = phone;
            mToken = token;
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Response<List<Device>> doInBackground(Void... params) {
            MainHomeCreateTaskFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return null;

            try {
                return DeviceService.getInstance(fragment.getActivity()).getDevices(
                        mPhone,
                        mToken
                );
            } catch (InterruptedException ignore) {
                return null;
            } catch (ServiceException e) {
                Log.e(GetDevicesTask.class.getName(), e.getCause().getMessage(), e);
                return new Response<>(e.getCause());
            }
        }

        @Override
        protected void onPostExecute(Response<List<Device>> response) {
            if (response == null) return;

            MainHomeCreateTaskFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return;

            if (response.getException() != null) {
                Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
            } else {
                switch (response.getCode()){
                    case ResponseCode.SUCCESS:
                        fragment.showChooseDeviceDialog(response.getObject());
                        break;
                    case ResponseCode.TOKEN_EXPIRED:
                        Toast.makeText(fragment.getActivity(), R.string.prompt_login_again, Toast.LENGTH_LONG).show();
                        fragment.startActivity(new Intent(fragment.getActivity(), SplashActivity.class));
                        fragment.getActivity().finish();
                        break;
                    default:
                        Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                        Log.e(GetDevicesTask.class.getName(),
                                "Unknown code: " + response.getCode() + ", message: " + response.getMessage());
                        break;
                }
            }
        }
    }

    public static class GetJuniorTask extends AsyncTask<Void, Void, Response<List<User>>> {

        private WeakReference<MainHomeCreateTaskFragment> fragmentReference;

        private final String mPhone;
        private final String mToken;

        GetJuniorTask(String phone, String token, MainHomeCreateTaskFragment fragment) {
            mPhone = phone;
            mToken = token;
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Response<List<User>> doInBackground(Void... params) {
            MainHomeCreateTaskFragment fragment = fragmentReference.get();

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

            MainHomeCreateTaskFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return;

            if (response.getException() != null) {
                Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
            } else {
                switch (response.getCode()){
                    case ResponseCode.SUCCESS:
                        fragment.showChooseAssigneeDialog(response.getObject());
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
