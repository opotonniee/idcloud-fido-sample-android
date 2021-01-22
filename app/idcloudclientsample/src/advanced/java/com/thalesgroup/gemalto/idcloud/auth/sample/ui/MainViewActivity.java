package com.thalesgroup.gemalto.idcloud.auth.sample.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.thalesgroup.gemalto.idcloud.auth.sample.Progress;
import com.thalesgroup.gemalto.idcloud.auth.sample.R;

public class MainViewActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainview);

        //loading the default fragment
        loadFragment(new AuthenticateHomeFragment());

        //getting bottom navigation view and attaching the listener
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = new AuthenticateHomeFragment();
                break;

            case R.id.navigation_authenticators:
                if (Progress.sdkLock.tryAcquire()) {
                    fragment = new AuthenticatorsFragment();
                }
                break;
        }

        return fragment != null ? loadFragment(fragment) : false;
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();

            return true;
        }
        return false;
    }
}
