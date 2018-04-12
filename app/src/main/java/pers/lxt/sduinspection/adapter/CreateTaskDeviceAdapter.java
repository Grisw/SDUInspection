package pers.lxt.sduinspection.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.model.Device;

public class CreateTaskDeviceAdapter extends RecyclerView.Adapter<CreateTaskDeviceAdapter.ViewHolder> {

    private List<Device> mDevices;

    public CreateTaskDeviceAdapter(List<Device> devices){
        mDevices = devices;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_create_task_device, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Device device = mDevices.get(position);
        holder.view_id.setText(String.format(Locale.getDefault(), "%d", device.getId()));
        holder.view_name.setText(device.getName());
    }

    @Override
    public int getItemCount() {
        return mDevices == null? 0 : mDevices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView view_name;
        TextView view_id;

        ViewHolder(View itemView) {
            super(itemView);
            view_name = itemView.findViewById(R.id.name);
            view_id = itemView.findViewById(R.id.id);
        }
    }
}
