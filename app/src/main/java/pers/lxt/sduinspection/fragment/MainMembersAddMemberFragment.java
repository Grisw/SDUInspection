package pers.lxt.sduinspection.fragment;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.activity.MainActivity;
import pers.lxt.sduinspection.activity.SplashActivity;
import pers.lxt.sduinspection.model.Response;
import pers.lxt.sduinspection.model.ServiceException;
import pers.lxt.sduinspection.model.User;
import pers.lxt.sduinspection.service.TokenService;
import pers.lxt.sduinspection.service.UserService;
import pers.lxt.sduinspection.util.ResponseCode;

public class MainMembersAddMemberFragment extends Fragment {

    private Date birthday;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_members_add_member, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).back();
            }
        });

        view.findViewById(R.id.birthday_btn).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final Calendar current = Calendar.getInstance();
                if(birthday != null){
                    current.setTime(birthday);
                }
                final Calendar selected = Calendar.getInstance();
                new DatePickerDialog(getActivity(), 0, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, final int year, final int month, final int date) {
                        selected.set(year, month, date, 0, 0, 0);

                        birthday = selected.getTime();
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        ((TextView) Objects.requireNonNull(getView()).findViewById(R.id.birthday)).setText(format.format(birthday));
                    }
                }, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        view.findViewById(R.id.create_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText name_view = Objects.requireNonNull(getView()).findViewById(R.id.name);
                EditText phone_view = getView().findViewById(R.id.phone);
                RadioButton sex_male_view = getView().findViewById(R.id.sex_male);
                RadioButton sex_female_view = getView().findViewById(R.id.sex_female);
                EditText email_view = getView().findViewById(R.id.email);

                String name = name_view.getText().toString();
                String phone = phone_view.getText().toString();
                String email = email_view.getText().toString();
                User.Sex sex = User.Sex.M;
                if(name.length() > 10){
                    name_view.setError(getString(R.string.name_too_long));
                    name_view.requestFocus();
                    return;
                }
                if(phone.length() != 11){
                    phone_view.setError(getString(R.string.error_invalid_phone));
                    phone_view.requestFocus();
                    return;
                }
                if(sex_male_view.isChecked()){
                    sex = User.Sex.M;
                }else if(sex_female_view.isChecked()){
                    sex = User.Sex.F;
                }
                if(email.length() == 0){
                    email = null;
                }else if(!email.matches("^[\\w-_.+]*[\\w-_.]@([\\w]+\\.)+[\\w]+[\\w]$")){
                    email_view.setError(getString(R.string.email_invalid));
                    email_view.requestFocus();
                    return;
                }

                User user = new User();
                user.setName(name);
                user.setBirthday(birthday);
                user.setEmail(email);
                user.setSex(sex);
                user.setPhoneNumber(phone);

                AddUserTask task = new AddUserTask(
                        user,
                        TokenService.getInstance(getActivity()).getPhone(),
                        TokenService.getInstance(getActivity()).getToken(),
                        MainMembersAddMemberFragment.this);
                task.execute((Void) null);
            }
        });
        return view;
    }

    public void userExists(){
        EditText phone_view = Objects.requireNonNull(getView()).findViewById(R.id.phone);
        phone_view.setError(getString(R.string.user_exists));
        phone_view.requestFocus();
    }

    public static class AddUserTask extends AsyncTask<Void, Void, Response<Void>> {

        private WeakReference<MainMembersAddMemberFragment> fragmentReference;

        private final String mPhone;
        private final String mToken;
        private final User mUser;

        AddUserTask(User user, String phone, String token, MainMembersAddMemberFragment fragment) {
            mPhone = phone;
            mToken = token;
            mUser = user;
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Response<Void> doInBackground(Void... params) {
            MainMembersAddMemberFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return null;

            try {
                return UserService.getInstance(fragment.getActivity()).createUser(
                        mUser,
                        mPhone,
                        mToken
                );
            } catch (InterruptedException ignore) {
                return null;
            } catch (ServiceException e) {
                Log.e(AddUserTask.class.getName(), e.getCause().getMessage(), e);
                return new Response<>(e.getCause());
            } catch (JSONException e) {
                Log.e(AddUserTask.class.getName(), e.getMessage(), e);
                return new Response<>(e);
            }
        }

        @Override
        protected void onPostExecute(Response<Void> response) {
            if (response == null) return;

            MainMembersAddMemberFragment fragment = fragmentReference.get();

            if(fragment == null || fragment.isRemoving() || fragment.getActivity() == null || fragment.getActivity().isFinishing())
                return;

            if (response.getException() != null) {
                Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
            } else {
                switch (response.getCode()){
                    case ResponseCode.SUCCESS:
                        Toast.makeText(fragment.getActivity(), "创建用户成功，初始密码：" + 123456, Toast.LENGTH_LONG).show();
                        ((MainActivity) fragment.getActivity()).back();
                        break;
                    case ResponseCode.USER_EXISTS:
                        fragment.userExists();
                        break;
                    case ResponseCode.TOKEN_EXPIRED:
                        Toast.makeText(fragment.getActivity(), R.string.prompt_login_again, Toast.LENGTH_LONG).show();
                        fragment.startActivity(new Intent(fragment.getActivity(), SplashActivity.class));
                        fragment.getActivity().finish();
                        break;
                    default:
                        Toast.makeText(fragment.getActivity(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                        Log.e(AddUserTask.class.getName(),
                                "Unknown code: " + response.getCode() + ", message: " + response.getMessage());
                        break;
                }
            }
        }
    }

}
