package pers.lxt.sduinspection.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.activity.MainActivity;
import pers.lxt.sduinspection.activity.SplashActivity;
import pers.lxt.sduinspection.model.Device;
import pers.lxt.sduinspection.model.Issue;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.model.TaskDevice;
import pers.lxt.sduinspection.service.IssueService;
import pers.lxt.sduinspection.service.TokenService;
import pers.lxt.sduinspection.util.ResponseCode;

import static android.app.Activity.RESULT_OK;

public class MainHomeIssueFragment extends Fragment {

    private Issue issue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_home_issue, container, false);

        issue = Objects.requireNonNull((Issue) getArguments().getSerializable("issue"));

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).back();
            }
        });

        ((TextView)view.findViewById(R.id.issue_device_id)).setText(String.format(Locale.getDefault(), "%d", issue.getDeviceId()));
        ((TextView)view.findViewById(R.id.issue_title)).setText(issue.getTitle());
        ((TextView)view.findViewById(R.id.issue_description)).setText(issue.getDescription());
        if(issue.getTaskId() != null)
            ((TextView)view.findViewById(R.id.issue_task_id)).setText(String.format(Locale.getDefault(), "%d", issue.getTaskId()));
        ((TextView)view.findViewById(R.id.issue_creator)).setText(issue.getCreatorName());
        ((TextView)view.findViewById(R.id.issue_publish_time)).setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(issue.getPublishTime()));
        if(issue.getPicture() != null){
            view.findViewById(R.id.issue_picture).getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    if(getView() != null){
                        ImageView imageView = getView().findViewById(R.id.issue_picture);
                        if(imageView.getDrawable() == null){
                            Bitmap bm = issue.getPicture();
                            double scale = Objects.requireNonNull(bm).getHeight() / (double) bm.getWidth();
                            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                            layoutParams.width = imageView.getWidth();
                            layoutParams.height = (int) (imageView.getWidth() * scale);
                            imageView.setLayoutParams(layoutParams);
                            imageView.setImageBitmap(bm);
                        }
                    }
                    return true;
                }
            });
        }

        if(issue.getState() == Issue.State.C){
            view.findViewById(R.id.close_button).setVisibility(View.GONE);
        }else{
            view.findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CloseIssueTask task = new CloseIssueTask(
                            issue.getId(),
                            TokenService.getInstance(getActivity()).getPhone(),
                            TokenService.getInstance(getActivity()).getToken(),
                            MainHomeIssueFragment.this);
                    task.execute((Void) null);
                }
            });
        }
        return view;
    }

    public void finish(){
        Toast.makeText(getActivity(), "问题已关闭：" + issue.getId(), Toast.LENGTH_SHORT).show();
        TaskDevice taskDevice = ((MainHomeTaskDeviceFragment) getTargetFragment()).getTaskDevice();
        if(taskDevice.getIssues() == null)
            taskDevice.setIssues(new ArrayList<Issue>());
        for(Issue i : taskDevice.getIssues()){
            if(i.getId() == issue.getId()){
                i.setState(Issue.State.C);
                break;
            }
        }
        ((MainActivity) getActivity()).back();
    }

    public static class CloseIssueTask extends AsyncTask<Void, Void, Response<Void>> {

        private WeakReference<MainHomeIssueFragment> fragmentReference;

        private final String mPhone;
        private final String mToken;
        private final int mId;

        CloseIssueTask(int id, String phone, String token, MainHomeIssueFragment fragment) {
            mPhone = phone;
            mToken = token;
            mId = id;
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Response<Void> doInBackground(Void... params) {
            MainHomeIssueFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return null;

            try {
                return IssueService.getInstance(fragment.getActivity()).closeIssue(
                        mId,
                        mPhone,
                        mToken
                );
            } catch (InterruptedException ignore) {
                return null;
            } catch (ServiceException e) {
                Log.e(CloseIssueTask.class.getName(), e.getCause().getMessage(), e);
                return new Response<>(e.getCause());
            } catch (JSONException e) {
                Log.e(CloseIssueTask.class.getName(), e.getMessage(), e);
                return new Response<>(e);
            }
        }

        @Override
        protected void onPostExecute(Response<Void> response) {
            if (response == null) return;

            MainHomeIssueFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return;

            if (response.getException() != null) {
                Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
            } else {
                switch (response.getCode()){
                    case ResponseCode.SUCCESS:
                        fragment.finish();
                        break;
                    case ResponseCode.TOKEN_EXPIRED:
                        Toast.makeText(fragment.getActivity(), R.string.prompt_login_again, Toast.LENGTH_LONG).show();
                        fragment.startActivity(new Intent(fragment.getActivity(), SplashActivity.class));
                        fragment.getActivity().finish();
                        break;
                    default:
                        Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                        Log.e(CloseIssueTask.class.getName(),
                                "Unknown code: " + response.getCode() + ", message: " + response.getMessage());
                        break;
                }
            }
        }
    }
}
