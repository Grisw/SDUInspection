package pers.lxt.sduinspection.adapter;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.activity.MainActivity;
import pers.lxt.sduinspection.fragment.MainHomeTaskInfoFragment;
import pers.lxt.sduinspection.fragment.MainHomeTasksFragment;
import pers.lxt.sduinspection.model.Task;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<Task> mTasks;
    private MainHomeTasksFragment mFragment;

    public TaskAdapter(MainHomeTasksFragment fragment, List<Task> tasks){
        mTasks = tasks;
        mFragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_task_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Task task = mTasks.get(position);
        holder.view_content.setText(String.format(Locale.getDefault(), "%d", task.getId()));
        holder.view_title.setText(task.getTitle());
        holder.click_area.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("task", task);
                ((MainActivity) mFragment.getActivity()).changeFragment(MainHomeTaskInfoFragment.class, bundle, false);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTasks == null? 0 : mTasks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView view_title;
        TextView view_content;
        View click_area;

        ViewHolder(View itemView) {
            super(itemView);
            click_area = itemView.findViewById(R.id.click_area);
            view_title = itemView.findViewById(R.id.title);
            view_content = itemView.findViewById(R.id.content);
        }
    }
}
