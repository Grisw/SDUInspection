package pers.lxt.sduinspection.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.TextureMapView;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.activity.MainActivity;
import pers.lxt.sduinspection.model.TaskDevice;

public class MainHomeTaskDeviceFragment extends Fragment {

    private TextureMapView mMapView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_home_task_device, container, false);

        Bundle data = getArguments();
        final TaskDevice device = Objects.requireNonNull((TaskDevice) data.getSerializable("taskdevice"));
        boolean editable = data.getBoolean("editable");

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).back();
            }
        });

        if(editable){
            view.findViewById(R.id.checked_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO checked device
                }
            });
            view.findViewById(R.id.task_device_take_picture).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO checked device
                }
            });
            view.findViewById(R.id.task_device_add_issue).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO checked device
                }
            });
        }else {
            view.findViewById(R.id.checked_button).setVisibility(View.GONE);
            view.findViewById(R.id.task_device_take_picture).setVisibility(View.GONE);
            view.findViewById(R.id.task_device_add_issue).setVisibility(View.GONE);
        }
        ((TextView)view.findViewById(R.id.task_device_id)).setText(String.format(Locale.getDefault(), "%d", device.getDeviceId()));
        ((TextView)view.findViewById(R.id.task_device_description)).setText(device.getDescription());
        ((TextView)view.findViewById(R.id.task_device_name)).setText(device.getName());
        if(device.getPicture() != null){
            ((ImageView)view.findViewById(R.id.task_device_picture)).setImageBitmap(BitmapFactory.decodeByteArray(device.getPicture(), 0, device.getPicture().length));
        }
        ((TextView)view.findViewById(R.id.task_device_position)).setText(String.format(Locale.getDefault(), "%f, %f", device.getLongitude(), device.getLatitude()));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        if(device.getCheckedTime() == null){
            ((TextView)view.findViewById(R.id.task_device_checked_time)).setText(R.string.prompt_unchecked);
        }else{
            ((TextView)view.findViewById(R.id.task_device_checked_time)).setText(format.format(device.getCheckedTime()));
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

        return view;
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
    }
    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }
}
