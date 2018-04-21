package pers.lxt.sduinspection.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
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
import pers.lxt.sduinspection.model.Device;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.model.TaskDevice;
import pers.lxt.sduinspection.service.TaskService;
import pers.lxt.sduinspection.service.TokenService;
import pers.lxt.sduinspection.util.ResponseCode;

import static android.app.Activity.RESULT_OK;

public class MainHomeDeviceFragment extends Fragment {

    private TextureMapView mMapView;
    private LocationClient mLocationClient = null;
    private Device device;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_home_device, container, false);

        Bundle data = getArguments();
        device = Objects.requireNonNull((Device) data.getSerializable("device"));

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).back();
            }
        });

        ((TextView)view.findViewById(R.id.device_id)).setText(String.format(Locale.getDefault(), "%d", device.getId()));
        ((TextView)view.findViewById(R.id.device_description)).setText(device.getDescription());
        ((TextView)view.findViewById(R.id.device_name)).setText(device.getName());

        RecyclerView issueRecycler = view.findViewById(R.id.device_issue);
        issueRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        issueRecycler.setAdapter(new IssueAdapter(this, device.getIssues()));
        issueRecycler.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

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
                centerDeviceLocation(device);
            }
        });

        LatLng point = new LatLng(device.getLatitude(), device.getLongitude());
        OverlayOptions deviceOverlay = new MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place));
        mMapView.getMap().addOverlay(deviceOverlay);
        centerDeviceLocation(device);

        view.findViewById(R.id.device_route).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyLocationData locationData = mMapView.getMap().getLocationData();
                if(locationData == null)
                    return;
                NaviParaOption para = new NaviParaOption()
                        .startPoint(new LatLng(locationData.latitude, locationData.longitude))
                        .endPoint(new LatLng(device.getLatitude(), device.getLongitude()))
                        .startName(getString(R.string.my_position)).endName(device.getName());
                try {
                    BaiduMapNavigation.openBaiduMapWalkNavi(para, getActivity());
                } catch (BaiduMapAppNotSupportNaviException e) {
                    Toast.makeText(getActivity(), R.string.install_baidu_map, Toast.LENGTH_SHORT).show();
                }
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

    private void centerDeviceLocation(Device device){
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
    }
    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        mLocationClient.stop();
    }

}
