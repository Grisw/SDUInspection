package pers.lxt.sduinspection.adapter;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.activity.MainActivity;
import pers.lxt.sduinspection.fragment.MainHomeDeviceFragment;
import pers.lxt.sduinspection.fragment.MainHomeDevicesFragment;
import pers.lxt.sduinspection.model.Device;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private List<Device> mDevices;
    private MainHomeDevicesFragment mFragment;

    public DeviceAdapter(MainHomeDevicesFragment fragment, List<Device> devices){
        mDevices = devices;
        mFragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_device_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Device device = mDevices.get(position);
        holder.view_id.setText(String.format(Locale.getDefault(), "%d", device.getId()));
        holder.view_name.setText(device.getName());
        holder.click_area.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("device", device);
                ((MainActivity) mFragment.getActivity()).changeFragment(MainHomeDeviceFragment.class, bundle, false, null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDevices == null? 0 : mDevices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView view_id;
        TextView view_name;
        View click_area;

        ViewHolder(View itemView) {
            super(itemView);
            click_area = itemView.findViewById(R.id.click_area);
            view_name = itemView.findViewById(R.id.name);
            view_id = itemView.findViewById(R.id.id);
        }
    }
}
