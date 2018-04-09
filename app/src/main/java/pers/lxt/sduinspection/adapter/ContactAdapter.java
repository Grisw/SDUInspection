package pers.lxt.sduinspection.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;
import java.util.Map;

import pers.lxt.sduinspection.R;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private List<Map<String, String>> mContacts;

    public ContactAdapter(List<Map<String, String>> contacts){
        mContacts = contacts;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contacts, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.view_name.setText(mContacts.get(position).get("name"));
    }

    @Override
    public int getItemCount() {
        return mContacts == null? 0 : mContacts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView view_name;

        ViewHolder(View itemView) {
            super(itemView);
            view_name = itemView.findViewById(R.id.name);
        }
    }
}
