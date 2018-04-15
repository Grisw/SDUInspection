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
import pers.lxt.sduinspection.model.Issue;
import pers.lxt.sduinspection.model.TaskDevice;

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.ViewHolder> {

    private List<Issue> issues;
    private Fragment mFragment;

    public IssueAdapter(Fragment fragment, List<Issue> issues){
        this.issues = issues;
        mFragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_issue, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Issue issue = issues.get(position);

        holder.view_id.setText(String.format(Locale.getDefault(), "%d", issue.getId()));
        holder.view_title.setText(issue.getTitle());
        holder.click_area.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("taskdevice", device);
//                bundle.putBoolean("editable", mEditable);
//                ((MainActivity) mFragment.getActivity()).changeFragment(MainHomeTaskDeviceFragment.class, bundle, false, mFragment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return issues == null? 0 : issues.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView view_title;
        TextView view_id;
        View click_area;

        ViewHolder(View itemView) {
            super(itemView);
            click_area = itemView.findViewById(R.id.click_area);
            view_title = itemView.findViewById(R.id.title);
            view_id = itemView.findViewById(R.id.id);
        }
    }
}
