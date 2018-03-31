package pers.lxt.sduinspection.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import pers.lxt.sduinspection.R;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vHome = findViewById(R.id.home);
        vContacts = findViewById(R.id.contacts);
        vMe = findViewById(R.id.me);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
