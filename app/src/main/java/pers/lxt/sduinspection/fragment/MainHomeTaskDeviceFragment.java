package pers.lxt.sduinspection.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.NaviParaOption;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.activity.MainActivity;
import pers.lxt.sduinspection.activity.SplashActivity;
import pers.lxt.sduinspection.adapter.IssueAdapter;
import pers.lxt.sduinspection.adapter.TaskDeviceAdapter;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.model.Task;
import pers.lxt.sduinspection.model.TaskDevice;
import pers.lxt.sduinspection.service.TaskService;
import pers.lxt.sduinspection.service.TokenService;
import pers.lxt.sduinspection.util.ResponseCode;

import static android.app.Activity.RESULT_OK;

public class MainHomeTaskDeviceFragment extends Fragment {

    private TextureMapView mMapView;
    private LocationClient mLocationClient = null;
    private TaskDevice taskDevice;

    public TaskDevice getTaskDevice() {
        return taskDevice;
    }

    private static final int RQ_TAKE_PHOTO = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_home_task_device, container, false);

        Bundle data = getArguments();
        taskDevice = Objects.requireNonNull((TaskDevice) data.getSerializable("taskdevice"));
        boolean editable = data.getBoolean("editable");

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(null);
            }
        });

        if(editable){
            view.findViewById(R.id.checked_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(taskDevice.getPicture() == null){
                        Toast.makeText(getActivity(), R.string.prompt_take_picture, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    taskDevice.setChecked(true);
                    new UpdateTaskDeviceTask(
                            taskDevice,
                            TokenService.getInstance(getActivity()).getPhone(),
                            TokenService.getInstance(getActivity()).getToken(),
                            MainHomeTaskDeviceFragment.this).execute((Void) null);
                }
            });
            view.findViewById(R.id.task_device_take_picture).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takeIntent, RQ_TAKE_PHOTO);
                }
            });
            view.findViewById(R.id.task_device_add_issue).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("device", taskDevice);
                    ((MainActivity) getActivity()).changeFragment(MainHomeCreateIssueFragment.class, bundle, false, MainHomeTaskDeviceFragment.this);
                }
            });
        }else {
            view.findViewById(R.id.checked_button).setVisibility(View.GONE);
            view.findViewById(R.id.task_device_take_picture).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.task_device_add_issue).setVisibility(View.INVISIBLE);
        }
        ((TextView)view.findViewById(R.id.task_device_id)).setText(String.format(Locale.getDefault(), "%d", taskDevice.getDeviceId()));
        ((TextView)view.findViewById(R.id.task_device_description)).setText(taskDevice.getDescription());
        ((TextView)view.findViewById(R.id.task_device_name)).setText(taskDevice.getName());
        if(taskDevice.getPicture() != null){
            view.findViewById(R.id.task_device_picture).getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    if(getView() != null){
                        ImageView imageView = getView().findViewById(R.id.task_device_picture);
                        if(imageView.getDrawable() == null)
                            setPicture(taskDevice.getPicture(), imageView);
                    }
                    return true;
                }
            });
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        if(taskDevice.getCheckedTime() == null){
            ((TextView)view.findViewById(R.id.task_device_checked_time)).setText(R.string.prompt_unchecked);
        }else{
            ((TextView)view.findViewById(R.id.task_device_checked_time)).setText(format.format(taskDevice.getCheckedTime()));
        }

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
        mLocationClient = new LocationClient(getActivity());
        mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                        .direction(location.getDirection()).latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                mMapView.getMap().setMyLocationData(locData);
            }
        });
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(1000);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        mLocationClient.setLocOption(option);

        view.findViewById(R.id.position_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                centerMyLocation();
            }
        });

        view.findViewById(R.id.device_position_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                centerDeviceLocation(taskDevice);
            }
        });

        LatLng point = new LatLng(taskDevice.getLatitude(), taskDevice.getLongitude());
        OverlayOptions deviceOverlay = new MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place));
        mMapView.getMap().addOverlay(deviceOverlay);
        centerDeviceLocation(taskDevice);

        view.findViewById(R.id.task_device_route).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyLocationData locationData = mMapView.getMap().getLocationData();
                if(locationData == null)
                    return;
                NaviParaOption para = new NaviParaOption()
                        .startPoint(new LatLng(locationData.latitude, locationData.longitude))
                        .endPoint(new LatLng(taskDevice.getLatitude(), taskDevice.getLongitude()))
                        .startName(getString(R.string.my_position)).endName(taskDevice.getName());
                try {
                    BaiduMapNavigation.openBaiduMapWalkNavi(para, getActivity());
                } catch (BaiduMapAppNotSupportNaviException e) {
                    Toast.makeText(getActivity(), R.string.install_baidu_map, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void setIssues(){
        RecyclerView issueRecycler = Objects.requireNonNull(getView()).findViewById(R.id.task_device_issue);
        issueRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        issueRecycler.setAdapter(new IssueAdapter(this, taskDevice.getIssues()));
        issueRecycler.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RQ_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Bitmap bm = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                    setPicture(bm, (ImageView) Objects.requireNonNull(getView()).findViewById(R.id.task_device_picture));
                    taskDevice.setPicture(bm);
                }
                break;
        }
    }

    private void setPicture(Bitmap bm, ImageView imageView){
        double scale = Objects.requireNonNull(bm).getHeight() / (double) bm.getWidth();
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.width = imageView.getWidth();
        layoutParams.height = (int) (imageView.getWidth() * scale);
        imageView.setLayoutParams(layoutParams);
        imageView.setImageBitmap(bm);
    }

    private void centerMyLocation(){
        MyLocationData locationData = mMapView.getMap().getLocationData();
        if(locationData == null)
            return;
        centerLocation(locationData.latitude, locationData.longitude);
    }

    private void centerLocation(double lat, double lon){
        LatLng latLng = new LatLng(lat, lon);
        mMapView.getMap().setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().target(latLng).zoom(15).build()));
    }

    private void centerDeviceLocation(TaskDevice device){
        centerLocation(device.getLatitude(), device.getLongitude());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        mLocationClient.start();
        setIssues();
    }
    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        mLocationClient.stop();
    }

    private void finish(Date checkedTime){
        List<TaskDevice> taskDevices = ((MainHomeTaskInfoFragment) getTargetFragment()).getTask().getDevices();
        for(TaskDevice taskDevice : taskDevices){
            if(taskDevice.getDeviceId() == this.taskDevice.getDeviceId()){
                taskDevice.setPicture(this.taskDevice.getPicture());
                taskDevice.setChecked(this.taskDevice.isChecked());
                if(checkedTime != null)
                    taskDevice.setCheckedTime(checkedTime);
                taskDevice.setIssues(this.taskDevice.getIssues());
                break;
            }
        }
        ((MainActivity) getActivity()).back();
    }

    public static class UpdateTaskDeviceTask extends AsyncTask<Void, Void, Response<Date>> {

        private WeakReference<MainHomeTaskDeviceFragment> fragmentReference;

        private final String mPhone;
        private final String mToken;
        private final TaskDevice mTaskDevice;

        UpdateTaskDeviceTask(TaskDevice device, String phone, String token, MainHomeTaskDeviceFragment fragment) {
            mPhone = phone;
            mToken = token;
            mTaskDevice = device;
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Response<Date> doInBackground(Void... params) {
            MainHomeTaskDeviceFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return null;

            try {
                return TaskService.getInstance(fragment.getActivity()).updateTaskDevice(
                        mTaskDevice,
                        mPhone,
                        mToken
                );
            } catch (InterruptedException ignore) {
                return null;
            } catch (ServiceException e) {
                Log.e(UpdateTaskDeviceTask.class.getName(), e.getCause().getMessage(), e);
                return new Response<>(e.getCause());
            } catch (JSONException e) {
                Log.e(UpdateTaskDeviceTask.class.getName(), e.getMessage(), e);
                return new Response<>(e);
            }
        }

        @Override
        protected void onPostExecute(Response<Date> response) {
            if (response == null) return;

            MainHomeTaskDeviceFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return;

            if (response.getException() != null) {
                Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
            } else {
                switch (response.getCode()){
                    case ResponseCode.SUCCESS:
                        fragment.finish(response.getObject());
                        break;
                    case ResponseCode.TOKEN_EXPIRED:
                        Toast.makeText(fragment.getActivity(), R.string.prompt_login_again, Toast.LENGTH_LONG).show();
                        fragment.startActivity(new Intent(fragment.getActivity(), SplashActivity.class));
                        fragment.getActivity().finish();
                        break;
                    default:
                        Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                        Log.e(UpdateTaskDeviceTask.class.getName(),
                                "Unknown code: " + response.getCode() + ", message: " + response.getMessage());
                        break;
                }
            }
        }
    }
}
