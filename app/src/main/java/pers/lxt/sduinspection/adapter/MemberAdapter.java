package pers.lxt.sduinspection.adapter;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.activity.MainActivity;
import pers.lxt.sduinspection.fragment.MainMembersFragment;
import pers.lxt.sduinspection.fragment.MainMembersInfoFragment;
import pers.lxt.sduinspection.model.User;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {

    private List<User> mUsers;
    private MainMembersFragment mFragment;

    public MemberAdapter(MainMembersFragment fragment, List<User> users){
        mUsers = users;
        mFragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_member_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final User user = mUsers.get(position);
        holder.view_phone.setText(user.getPhoneNumber());
        holder.view_name.setText(user.getName());
        holder.click_area.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", user);
                ((MainActivity) mFragment.getActivity()).changeFragment(MainMembersInfoFragment.class, bundle, false, null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers == null? 0 : mUsers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView view_phone;
        TextView view_name;
        View click_area;

        ViewHolder(View itemView) {
            super(itemView);
            click_area = itemView.findViewById(R.id.click_area);
            view_name = itemView.findViewById(R.id.name);
            view_phone = itemView.findViewById(R.id.phone);
        }
    }
}
