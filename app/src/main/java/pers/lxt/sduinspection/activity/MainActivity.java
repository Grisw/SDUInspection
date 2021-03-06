package pers.lxt.sduinspection.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.fragment.MainMembersFragment;
import pers.lxt.sduinspection.fragment.MainHomeFragment;
import pers.lxt.sduinspection.fragment.MainMeFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Class<? extends Fragment> to = null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    to = MainHomeFragment.class;
                    break;
                case R.id.navigation_members:
                    to = MainMembersFragment.class;
                    break;
                case R.id.navigation_me:
                    to = MainMeFragment.class;
                    break;
            }
            if(to == null){
                return false;
            }
            changeFragment(to, null, true, null);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        changeFragment(MainHomeFragment.class, getIntent().getBundleExtra("initialize"), true, null);
    }

    public void changeFragment(Class<? extends Fragment> fragmentClass, Bundle data, boolean clearStack, Fragment targetFragment) {
        Fragment fragment;
        try {
            fragment = fragmentClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            Log.e(MainActivity.class.getName(), e.getMessage(), e);
            return;
        }
        fragment.setArguments(data);
        if(targetFragment != null)
            fragment.setTargetFragment(targetFragment, 0);

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment, fragment);
        if(clearStack){
            manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }else{
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public boolean back(){
        FragmentManager manager = getFragmentManager();
        if(manager.getBackStackEntryCount() > 0){
            manager.popBackStack();
            return true;
        }else{
            return false;
        }
    }

//    private void initMembers(){
//        User user = (User) mInitializeData.getSerializable("user");
//        @SuppressWarnings("unchecked")
//        List<Map<String, String>> members = (List<Map<String, String>>) mInitializeData.getSerializable("members");
//
//        Toolbar toolbar = vMembers.findViewById(R.id.toolbar);
//        toolbar.setTitle(user != null ? user.getName() : "");
//
//        RecyclerView membersRecyclerView = vMembers.findViewById(R.id.members_view);
//        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        membersRecyclerView.setAdapter(new MemberAdapter(members));
//        membersRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
//    }

    private long mExitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if(back()){
                return true;
            }
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(MainActivity.this, R.string.prompt_press_back, Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
