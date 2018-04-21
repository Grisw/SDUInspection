package pers.lxt.sduinspection.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.activity.MainActivity;
import pers.lxt.sduinspection.activity.SplashActivity;
import pers.lxt.sduinspection.adapter.DeviceAdapter;
import pers.lxt.sduinspection.model.Device;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.service.DeviceService;
import pers.lxt.sduinspection.service.TokenService;
import pers.lxt.sduinspection.util.ResponseCode;

public class MainHomeDevicesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_home_devices, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).back();
            }
        });

        GetDevicesTask mGetDevicesTask = new GetDevicesTask(
                TokenService.getInstance(getActivity()).getPhone(),
                TokenService.getInstance(getActivity()).getToken(),
                this);
        mGetDevicesTask.execute((Void) null);

        return view;
    }

    private void updateTaskList(List<Device> devices){
        RecyclerView recyclerView = Objects.requireNonNull(getView()).findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new DeviceAdapter(this, devices));
    }

    public static class GetDevicesTask extends AsyncTask<Void, Void, Response<List<Device>>> {

        private WeakReference<MainHomeDevicesFragment> fragmentReference;

        private final String mPhone;
        private final String mToken;

        GetDevicesTask(String phone, String token, MainHomeDevicesFragment fragment) {
            mPhone = phone;
            mToken = token;
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Response<List<Device>> doInBackground(Void... params) {
            MainHomeDevicesFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return null;

            try {
                return DeviceService.getInstance(fragment.getActivity()).getDevices(
                        mPhone,
                        mToken
                );
            } catch (InterruptedException ignore) {
                return null;
            } catch (ServiceException e) {
                Log.e(MainHomeCreateTaskFragment.GetDevicesTask.class.getName(), e.getCause().getMessage(), e);
                return new Response<>(e.getCause());
            }
        }

        @Override
        protected void onPostExecute(Response<List<Device>> response) {
            if (response == null) return;

            MainHomeDevicesFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return;

            if (response.getException() != null) {
                Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
            } else {
                switch (response.getCode()){
                    case ResponseCode.SUCCESS:
                        fragment.updateTaskList(response.getObject());
                        break;
                    case ResponseCode.TOKEN_EXPIRED:
                        Toast.makeText(fragment.getActivity(), R.string.prompt_login_again, Toast.LENGTH_LONG).show();
                        fragment.startActivity(new Intent(fragment.getActivity(), SplashActivity.class));
                        fragment.getActivity().finish();
                        break;
                    default:
                        Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                        Log.e(MainHomeCreateTaskFragment.GetDevicesTask.class.getName(),
                                "Unknown code: " + response.getCode() + ", message: " + response.getMessage());
                        break;
                }
            }
        }
    }

}
