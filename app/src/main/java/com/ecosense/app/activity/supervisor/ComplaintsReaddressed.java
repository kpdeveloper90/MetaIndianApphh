package com.ecosense.app.activity.supervisor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.fragment.CitizenComplaints;
import com.ecosense.app.fragment.DriverComplaints;
import com.ecosense.app.fragment.MyComplaints_supervisor;
import com.ecosense.app.helper.UserSessionManger;


public class ComplaintsReaddressed extends AppCompatActivity {

    private static final String TAG = ComplaintsReaddressed.class.getSimpleName();
    private Toolbar toolbar;
    private TabLayout tabLayout_comp_read;

    static ProgressDialog mProgressDialog = null;
UserSessionManger session=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaints_readdressed);

        ButterKnife.bind(this);
        toolbar = findViewById(R.id.include9);
        toolbar.setTitle("Complaints");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
        tabLayout_comp_read = (TabLayout) findViewById(R.id.tabLayout_comp_read);


        tabLayout_comp_read.addTab(tabLayout_comp_read.newTab().setText(getString(R.string.tab_sup_my_complaints)));
        tabLayout_comp_read.addTab(tabLayout_comp_read.newTab().setText(getString(R.string.tab_sup_citizen_complaints)));
        tabLayout_comp_read.addTab(tabLayout_comp_read.newTab().setText(getString(R.string.tab_sup_driver_complaints)));


        tabLayout_comp_read.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.e(TAG, "tab.getPosition() = " + tab.getPosition());
//                vp_citizenDashboard.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()) {
                    case 0:
                        loadFragment(new MyComplaints_supervisor());
                        break;
                    case 1:
                        loadFragment(new CitizenComplaints());
                        break;
                    case 2:
                        loadFragment(new DriverComplaints());
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        loadFragment(new MyComplaints_supervisor());
    }


    private void loadFragmentS(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = Objects.requireNonNull(getSupportFragmentManager().beginTransaction());
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    public void loadFragment(Fragment fragment) {

//        if (bundle != null)
//            fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment oldFragment = fragmentManager.findFragmentByTag(fragment.getClass().getName());

        //if oldFragment already exits in fragmentManager use it
        if (oldFragment != null) {
            fragment = oldFragment;
        }

            fragmentTransaction.replace(R.id.frame_container, fragment, fragment.getClass().getName());

        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume => " + TAG);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy => " + TAG);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(getApplicationContext(), SupervisorHome.class));
    }
}
