package pers.lxt.sduinspection.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.activity.MainActivity;
import pers.lxt.sduinspection.activity.SplashActivity;
import pers.lxt.sduinspection.model.Device;
import pers.lxt.sduinspection.model.Issue;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.service.DeviceService;
import pers.lxt.sduinspection.service.TokenService;
import pers.lxt.sduinspection.util.ResponseCode;

public class MainHomeCreateDeviceFragment extends Fragment {

    private TextureMapView mMapView;
    private LocationClient mLocationClient = null;
    private boolean firstLocated = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_home_create_device, container, false);

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
        mLocationClient = new LocationClient(getActivity());
        mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                        .direction(location.getDirection()).latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                mMapView.getMap().setMyLocationData(locData);
                if(firstLocated){
                    centerMyLocation();
                    firstLocated = false;
                }
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

        view.findViewById(R.id.create_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText title_view = Objects.requireNonNull(getView()).findViewById(R.id.title);
                EditText description_view = getView().findViewById(R.id.description);

                String name = title_view.getText().toString();
                String description = description_view.getText().toString();
                if(name.length() < 1){
                    title_view.setError(getString(R.string.device_name_nonnull));
                    title_view.requestFocus();
                    return;
                }
                if(description.length() < 5){
                    description_view.setError(getString(R.string.prompt_description_length));
                    description_view.requestFocus();
                    return;
                }

                LatLng center = mMapView.getMap().getMapStatus().target;
                Device device = new Device();
                device.setName(name);
                device.setDescription(description);
                device.setLatitude(center.latitude);
                device.setLongitude(center.longitude);

                CreateDeviceTask task = new CreateDeviceTask(
                        device,
                        TokenService.getInstance(getActivity()).getPhone(),
                        TokenService.getInstance(getActivity()).getToken(),
                        MainHomeCreateDeviceFragment.this);
                task.execute((Void) null);
            }
        });

        return view;
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
    }
    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        mLocationClient.stop();
    }

    public void finishCreating(Device device){
        Toast.makeText(getActivity(), "创建设备ID：" + device.getId(), Toast.LENGTH_SHORT).show();
        List<Device> devices = ((MainHomeDevicesFragment) getTargetFragment()).getDevices();
        devices.add(device);
        ((MainActivity) getActivity()).back();
    }

    public static class CreateDeviceTask extends AsyncTask<Void, Void, Response<Integer>> {

        private WeakReference<MainHomeCreateDeviceFragment> fragmentReference;

        private final String mPhone;
        private final String mToken;
        private final Device mDevice;

        CreateDeviceTask(Device device, String phone, String token, MainHomeCreateDeviceFragment fragment) {
            mPhone = phone;
            mToken = token;
            mDevice = device;
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Response<Integer> doInBackground(Void... params) {
            MainHomeCreateDeviceFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return null;

            try {
                return DeviceService.getInstance(fragment.getActivity()).createDevice(
                        mDevice,
                        mPhone,
                        mToken
                );
            } catch (InterruptedException ignore) {
                return null;
            } catch (ServiceException e) {
                Log.e(CreateDeviceTask.class.getName(), e.getCause().getMessage(), e);
                return new Response<>(e.getCause());
            } catch (JSONException e) {
                Log.e(CreateDeviceTask.class.getName(), e.getMessage(), e);
                return new Response<>(e);
            }
        }

        @Override
        protected void onPostExecute(Response<Integer> response) {
            if (response == null) return;

            MainHomeCreateDeviceFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return;

            if (response.getException() != null) {
                Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
            } else {
                switch (response.getCode()){
                    case ResponseCode.SUCCESS:
                        mDevice.setId(response.getObject());
                        mDevice.setIssues(new ArrayList<Issue>());
                        fragment.finishCreating(mDevice);
                        break;
                    case ResponseCode.TOKEN_EXPIRED:
                        Toast.makeText(fragment.getActivity(), R.string.prompt_login_again, Toast.LENGTH_LONG).show();
                        fragment.startActivity(new Intent(fragment.getActivity(), SplashActivity.class));
                        fragment.getActivity().finish();
                        break;
                    default:
                        Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                        Log.e(CreateDeviceTask.class.getName(),
                                "Unknown code: " + response.getCode() + ", message: " + response.getMessage());
                        break;
                }
            }
        }
    }

}
