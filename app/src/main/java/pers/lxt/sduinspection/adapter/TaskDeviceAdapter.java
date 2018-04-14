package pers.lxt.sduinspection.adapter;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.activity.MainActivity;
import pers.lxt.sduinspection.fragment.MainHomeTaskDeviceFragment;
import pers.lxt.sduinspection.fragment.MainHomeTaskInfoFragment;
import pers.lxt.sduinspection.fragment.MainHomeTasksFragment;
import pers.lxt.sduinspection.model.TaskDevice;

public class TaskDeviceAdapter extends RecyclerView.Adapter<TaskDeviceAdapter.ViewHolder> {

    private List<TaskDevice> devices;
    private Fragment mFragment;
    private boolean mEditable;

    public TaskDeviceAdapter(Fragment fragment, List<TaskDevice> devices, boolean editable){
        this.devices = devices;
        mFragment = fragment;
        mEditable = editable;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_task_device, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final TaskDevice device = devices.get(position);

        holder.view_device_id.setText(String.format(Locale.getDefault(), "%d", device.getDeviceId()));
        holder.view_device_name.setText(device.getName());
        holder.checkBox.setChecked(device.isChecked());
        holder.click_area.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("taskdevice", device);
                bundle.putBoolean("editable", mEditable);
                ((MainActivity) mFragment.getActivity()).changeFragment(MainHomeTaskDeviceFragment.class, bundle, false, mFragment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices == null? 0 : devices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView view_device_id;
        TextView view_device_name;
        View click_area;
        CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);
            click_area = itemView.findViewById(R.id.click_area);
            view_device_id = itemView.findViewById(R.id.device_id);
            view_device_name = itemView.findViewById(R.id.device_name);
            checkBox = itemView.findViewById(R.id.checked);
        }
    }
}
