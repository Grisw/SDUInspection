package pers.lxt.sduinspection.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
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
import pers.lxt.sduinspection.model.Issue;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.model.TaskDevice;
import pers.lxt.sduinspection.model.User;
import pers.lxt.sduinspection.service.DeviceService;
import pers.lxt.sduinspection.service.IssueService;
import pers.lxt.sduinspection.service.TaskService;
import pers.lxt.sduinspection.service.TokenService;
import pers.lxt.sduinspection.service.UserService;
import pers.lxt.sduinspection.util.ResponseCode;

import static android.app.Activity.RESULT_OK;

public class MainHomeCreateIssueFragment extends Fragment {

    private TaskDevice taskDevice;
    private Issue issue;

    private static final int RQ_TAKE_PHOTO = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_home_create_issue, container, false);

        taskDevice = Objects.requireNonNull((TaskDevice) getArguments().getSerializable("device"));
        issue = new Issue();
        issue.setDeviceId(taskDevice.getDeviceId());
        issue.setTaskId(taskDevice.getTaskId());

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).back();
            }
        });

        ((TextView)view.findViewById(R.id.issue_device_name)).setText(taskDevice.getName());
        ((TextView)view.findViewById(R.id.issue_device_description)).setText(taskDevice.getDescription());
        ((TextView)view.findViewById(R.id.issue_device_id)).setText(String.format(Locale.getDefault(), "%d", taskDevice.getDeviceId()));

        view.findViewById(R.id.issue_take_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takeIntent, RQ_TAKE_PHOTO);
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

                issue.setTitle(title);
                issue.setDescription(description);

                CreateIssueTask task = new CreateIssueTask(
                        issue,
                        TokenService.getInstance(getActivity()).getPhone(),
                        TokenService.getInstance(getActivity()).getToken(),
                        MainHomeCreateIssueFragment.this);
                task.execute((Void) null);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RQ_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Bitmap bm = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                    ImageView imageView = Objects.requireNonNull(getView()).findViewById(R.id.issue_picture);
                    double scale = Objects.requireNonNull(bm).getHeight() / (double) bm.getWidth();
                    ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                    layoutParams.width = imageView.getWidth();
                    layoutParams.height = (int) (imageView.getWidth() * scale);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setImageBitmap(bm);
                    issue.setPicture(bm);
                }
                break;
        }
    }

    public void finishCreating(int id){
        Toast.makeText(getActivity(), "创建问题报告ID：" + id, Toast.LENGTH_SHORT).show();
        TaskDevice taskDevice = ((MainHomeTaskDeviceFragment) getTargetFragment()).getTaskDevice();
        if(taskDevice.getIssues() == null)
            taskDevice.setIssues(new ArrayList<Issue>());
        taskDevice.getIssues().add(issue);
        ((MainActivity) getActivity()).back();
    }

    public static class CreateIssueTask extends AsyncTask<Void, Void, Response<Integer>> {

        private WeakReference<MainHomeCreateIssueFragment> fragmentReference;

        private final String mPhone;
        private final String mToken;
        private final Issue mIssue;

        CreateIssueTask(Issue issue, String phone, String token, MainHomeCreateIssueFragment fragment) {
            mPhone = phone;
            mToken = token;
            mIssue = issue;
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Response<Integer> doInBackground(Void... params) {
            MainHomeCreateIssueFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return null;

            try {
                return IssueService.getInstance(fragment.getActivity()).createIssue(
                        mIssue,
                        mPhone,
                        mToken
                );
            } catch (InterruptedException ignore) {
                return null;
            } catch (ServiceException e) {
                Log.e(CreateIssueTask.class.getName(), e.getCause().getMessage(), e);
                return new Response<>(e.getCause());
            } catch (JSONException e) {
                Log.e(CreateIssueTask.class.getName(), e.getMessage(), e);
                return new Response<>(e);
            }
        }

        @Override
        protected void onPostExecute(Response<Integer> response) {
            if (response == null) return;

            MainHomeCreateIssueFragment fragment = fragmentReference.get();

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
                        Log.e(CreateIssueTask.class.getName(),
                                "Unknown code: " + response.getCode() + ", message: " + response.getMessage());
                        break;
                }
            }
        }
    }
}
