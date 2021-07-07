package com.ecosense.app.activity.supervisor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.fragment.MyExpenseList;
import com.ecosense.app.helper.UserSessionManger;

public class ExpenseDetail extends AppCompatActivity {
    private static final String TAG = ExpenseDetail.class.getSimpleName();
    private Toolbar toolbar;
    private TabLayout tabLayout_expanse;
    UserSessionManger session = null;
    static ProgressDialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_deatail);

        ButterKnife.bind(this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Expenses");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
        tabLayout_expanse = (TabLayout) findViewById(R.id.tabLayout_expanse);

        tabLayout_expanse.addTab(tabLayout_expanse.newTab().setText(getString(R.string.tab_sup_expense)));
        tabLayout_expanse.addTab(tabLayout_expanse.newTab().setText(getString(R.string.tab_sup_team_expense)));

        tabLayout_expanse.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    Log.e(TAG, "tab.getPosition() = " + tab.getPosition());
//                vp_citizenDashboard.setCurrentItem(tab.getPosition());
                    switch (tab.getPosition()) {
                        case 0:
                            loadFragment(new MyExpenseList());
                            break;
                        case 1:
                            Uri uri = Uri.parse(session.getMyServerIP() + "/desk#List/Expense%20Claim/List");
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                            Objects.requireNonNull(tabLayout_expanse.getTabAt(0)).select();
//                        loadFragment(new DriverExpense());
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
        loadFragment(new MyExpenseList());
    }


    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = Objects.requireNonNull(getSupportFragmentManager().beginTransaction());
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
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
