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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.track.HistoryTrackResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.api.track.TrackPoint;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.activity.MainActivity;
import pers.lxt.sduinspection.activity.SplashActivity;
import pers.lxt.sduinspection.adapter.TaskDeviceAdapter;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.model.Task;
import pers.lxt.sduinspection.model.TaskDevice;
import pers.lxt.sduinspection.service.TaskService;
import pers.lxt.sduinspection.service.TokenService;
import pers.lxt.sduinspection.service.TraceService;
import pers.lxt.sduinspection.service.UserService;
import pers.lxt.sduinspection.util.ResponseCode;

public class MainHomeTaskInfoFragment extends Fragment {

    private UpdateTaskTask mUpdateTaskTask;

    private Task task;

    public Task getTask() {
        return task;
    }

    private TextureMapView mMapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_home_task_info, container, false);

        Bundle data = getArguments();
        task = Objects.requireNonNull((Task) data.getSerializable("task"));

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).back();
            }
        });

        mMapView = view.findViewById(R.id.bmapView);
        mMapView.getChildAt(0).setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ScrollView scrollView = Objects.requireNonNull(getView()).findViewById(R.id.scrollView);
                if(event.getAction() == MotionEvent.ACTION_UP){
                    scrollView.requestDisallowInterceptTouchEvent(false);
                }else{
                    scrollView.requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });
        mMapView.showZoomControls(false);
        mMapView.getMap().setMyLocationEnabled(true);
        mMapView.getMap().setMyLocationConfiguration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null));

        List<OverlayOptions> overlayOptions = new ArrayList<>();
        for (TaskDevice device : task.getDevices()){
            LatLng point = new LatLng(device.getLatitude(), device.getLongitude());
            OverlayOptions deviceOverlay = new MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place));
            overlayOptions.add(deviceOverlay);
        }
        mMapView.getMap().addOverlays(overlayOptions);

        view.findViewById(R.id.position_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTrace();
            }
        });

        return view;
    }

    private void updateTrace(){
        TraceService.getInstance(getActivity()).getTrace(task, new OnTrackListener() {
            @Override
            public void onHistoryTrackCallback(HistoryTrackResponse historyTrackResponse) {
                if(historyTrackResponse.getStatus() == 14004){
                    updateTrace();
                    return;
                }
                List<LatLng> locations = new ArrayList<>();
                List<TrackPoint> trackPoints = historyTrackResponse.getTrackPoints();
                for (TrackPoint point : trackPoints){
                    locations.add(new LatLng(point.getLocation().latitude, point.getLocation().getLongitude()));
                }
                OverlayOptions ooPolyline = new PolylineOptions().width(10)
                        .color(0xAAFF0000).points(locations);
                mMapView.getMap().addOverlay(ooPolyline);

                LatLng point = new LatLng(historyTrackResponse.getEndPoint().getLocation().latitude, historyTrackResponse.getEndPoint().getLocation().longitude);
                OverlayOptions personOverlay = new MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_pos));
                mMapView.getMap().addOverlay(personOverlay);
                mMapView.getMap().setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().target(point).zoom(15).build()));
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        View view = Objects.requireNonNull(getView());

        Button button = view.findViewById(R.id.change_state_button);
        if (!task.getAssignee().equals(UserService.getInstance(getActivity()).getCurrentUser().getPhoneNumber())) {
            button.setVisibility(View.GONE);
        }else {
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
                            List<TaskDevice> taskDevices = task.getDevices();
                            for(TaskDevice taskDevice : taskDevices){
                                if(!taskDevice.isChecked()){
                                    Toast.makeText(getActivity(), R.string.prompt_please_check_devices, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
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
                    button.setVisibility(View.GONE);
                    break;
            }
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
        devicesRecycler.setAdapter(new TaskDeviceAdapter(this, task.getDevices(), task.getState() == Task.State.D && task.getAssignee().equals(UserService.getInstance(getActivity()).getCurrentUser().getPhoneNumber())));
        devicesRecycler.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        updateTrace();
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
                        switch (mState){
                            case D:
                                TraceService.getInstance(fragment.getActivity()).startTrace();
                                break;
                            case E:
                                TraceService.getInstance(fragment.getActivity()).stopTrace();
                                break;
                        }
                        ((MainActivity) fragment.getActivity()).changeFragment(MainHomeFragment.class, null, true, null);
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
