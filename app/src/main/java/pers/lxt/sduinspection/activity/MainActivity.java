package pers.lxt.sduinspection.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import pers.lxt.sduinspection.R;
import pers.lxt.sduinspection.model.User;

public class MainActivity extends AppCompatActivity {

    private View vHome, vContacts, vMe;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    vHome.setVisibility(View.VISIBLE);
                    vContacts.setVisibility(View.GONE);
                    vMe.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_contacts:
                    vHome.setVisibility(View.GONE);
                    vContacts.setVisibility(View.VISIBLE);
                    vMe.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_me:
                    vHome.setVisibility(View.GONE);
                    vContacts.setVisibility(View.GONE);
                    vMe.setVisibility(View.VISIBLE);
                    return true;
            }
            return false;
        }
    };

    private User mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vHome = findViewById(R.id.home);
        vContacts = findViewById(R.id.contacts);
        vMe = findViewById(R.id.me);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mCurrentUser = (User) getIntent().getSerializableExtra("user");
        initHome();
    }

    private void initHome(){
        Toolbar toolbar = vHome.findViewById(R.id.toolbar);
        toolbar.setTitle(mCurrentUser.getName());
    }

    private void initContacts(){

    }

    private void initMe(){

    }

    private long mExitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(MainActivity.this, R.string.prompt_press_back, Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}