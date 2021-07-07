package com.ecosense.app.activity.supervisor;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.yarolegovich.slidingrootnav.SlideGravity;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.Arrays;

import com.ecosense.app.R;
import com.ecosense.app.activity.citizen.MyProfile;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.menu.DrawerAdapter;
import com.ecosense.app.menu.DrawerItem;
import com.ecosense.app.menu.SimpleItem;

public class SupervisorHome extends AppCompatActivity
        implements DrawerAdapter.OnItemSelectedListener {

    private static final String TAG = SupervisorHome.class.getSimpleName();
    Toolbar toolbar = null;
    UserSessionManger session = null;


    private static final int SUP_DRAWER_HOME = 0;
    private static final int SUP_DRAWER_VehicleDeployed = 1;
    private static final int SUP_DRAWER_RouteCoverage = 2;
    private static final int SUP_DRAWER_BinCleared = 3;
    private static final int SUP_DRAWER_Complaints = 4;
    private static final int SUP_DRAWER_service_Request = 5;
    private static final int SUP_DRAWER_Fuel_Management = 6;
    private static final int SUP_DRAWER_Expense = 7;
    private static final int SUP_DRAWER_voucherDetail = 8;

    private String[] screenTitles;
    private Drawable[] screenIcons;

    private SlidingRootNav slidingRootNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor_home);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_supervisor_home));
        setSupportActionBar(toolbar);


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

//        setupDrawer();

        setupSlidingDrawer(savedInstanceState);
    }

    private void setupSlidingDrawer(Bundle savedInstanceState) {

        slidingRootNav = new SlidingRootNavBuilder(this)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false) //Initial menu opened/closed state. Default == false
                .withMenuLocked(false) //If true, a user can't open or close the menu. Default == false.
                .withDragDistance(180) //Horizontal translation of a view. Default == 180dp
                .withRootViewScale(0.7f) //Content view's scale will be interpolated between 1f and 0.7f. Default == 0.65f;
                .withRootViewElevation(10) //Content view's elevation will be interpolated between 0 and 10dp. Default == 8.
                .withRootViewYTranslation(4) //Content view's translationY will be interpolated between 0 and 4. Default == 0
                .withContentClickableWhenMenuOpened(true) //Pretty self-descriptive. Builder Default == true
                .withGravity(SlideGravity.LEFT) //If LEFT you can swipe a menu from left to right, if RIGHT - the direction is opposite.
                .withSavedState(savedInstanceState)//If you call the method, layout will restore its opened/closed state
                .withMenuLayout(R.layout.menu_left_drawer)
                .inject();

        screenIcons = loadScreenIcons();
        screenTitles = loadScreenTitles();

        DrawerAdapter adapter = new DrawerAdapter(Arrays.asList(
                createItemFor(SUP_DRAWER_HOME).setChecked(true),
                createItemFor(SUP_DRAWER_VehicleDeployed),
                createItemFor(SUP_DRAWER_RouteCoverage),
                createItemFor(SUP_DRAWER_BinCleared),
                createItemFor(SUP_DRAWER_Complaints),
                createItemFor(SUP_DRAWER_service_Request),
                createItemFor(SUP_DRAWER_Fuel_Management),
                createItemFor(SUP_DRAWER_Expense),
                createItemFor(SUP_DRAWER_voucherDetail)));
        adapter.setListener(this);


        CircularImageView img_drawer_profile = findViewById(R.id.img_drawer_profile);
//        img_drawer_profile.setImageDrawable(getCharacterImage(session.getpsName().substring(0, 1)));

        if (session.getUserProfilePic() != null) {
            String pic_url = session.getMyServerIP() + session.getUserProfilePic();
            Glide.with(this).load(pic_url)
                    .apply(RequestOptions.centerCropTransform())
                    .into(img_drawer_profile);
        } else {
            Glide.with(this).load(R.drawable.ic_user_1)
                    .apply(RequestOptions.centerCropTransform())
                    .into(img_drawer_profile);
        }

        TextView tv_userName = findViewById(R.id.tv_userName);
        TextView tv_uMno = findViewById(R.id.tv_uMno);
        tv_userName.setText(session.getpsName());
        tv_uMno.setText("(M) : " + session.getMobileNumber());
        ImageView img_btn_edit = findViewById(R.id.img_btn_edit);
        img_btn_edit.setVisibility(View.INVISIBLE);
        img_btn_edit.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), MyProfile.class));
            finish();
        });
       /* if (ConnectionReceiver.isConnected()) {
            if (isNotificationServiceRunning() == false) {
                startService(new Intent(this, NotificationService.class));
//                TastyToast.makeText(getApplicationContext(), "stopService", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
//                stopService(new Intent(this, NotificationService.class));
            }
        } else {
            if (isNotificationServiceRunning() == true) {
                TastyToast.makeText(getApplicationContext(), "You are Offline", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                stopService(new Intent(this, NotificationService.class));

            }
        }*/
        LinearLayout btn_Logout = findViewById(R.id.btn_Logout);
        btn_Logout.setOnClickListener(v -> exitByBackKey("Logout",getString(R.string.logout_msg)));

        RecyclerView list = findViewById(R.id.list);
        list.setNestedScrollingEnabled(true);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        adapter.setSelected(SUP_DRAWER_HOME);
    }

    @Override
    public void onItemSelected(int position) {

        // Navigate to the right fragment
        switch (position) {
            case SUP_DRAWER_HOME:
//                setToolbarTitle("Home");
                break;
            case SUP_DRAWER_VehicleDeployed:
                finish();
                startActivity(new Intent(getApplicationContext(), VehicleDeployed.class));
                break;
            case SUP_DRAWER_RouteCoverage:
                finish();
                startActivity(new Intent(getApplicationContext(), RouteCoverage.class));
                break;
            case SUP_DRAWER_BinCleared:
                finish();
                startActivity(new Intent(getApplicationContext(), POIStatus.class));
                break;
            case SUP_DRAWER_Complaints:
                finish();
                startActivity(new Intent(getApplicationContext(), ComplaintsReaddressed.class));
                break;
            case SUP_DRAWER_service_Request:
                finish();
                startActivity(new Intent(getApplicationContext(), ServiceRequestReaddressed.class));
                break;
            case SUP_DRAWER_Fuel_Management:
                finish();
                startActivity(new Intent(getApplicationContext(), FuelPriceUpdate.class));
                break;
            case SUP_DRAWER_Expense:
                finish();
                startActivity(new Intent(getApplicationContext(), ExpenseDetail.class));
                break;
            case SUP_DRAWER_voucherDetail:
                finish();
                startActivity(new Intent(getApplicationContext(), VoucherDetail.class));
                break;
            default:
                Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                break;
        }

        if (slidingRootNav.isMenuOpened()) {
            slidingRootNav.closeMenu(true);
        }
    }


    public void setToolbarTitle(String title) {
        toolbar.setTitle(title);
        toolbar.invalidate();
    }

    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(color(R.color.drawer_textColorSecondary))
                .withTextTint(color(R.color.drawer_textColorPrimary))
                .withSelectedIconTint(color(R.color.colorAccent))
                .withSelectedTextTint(color(R.color.colorAccent));
    }

    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.supervisor_activityScreenTitles);
    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.supervisor_activityScreenIcons);
        Drawable[] icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            int id = ta.getResourceId(i, 0);
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }

    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }


    @Override
    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
////            super.onBackPressed();
//        }
        exitByBackKey("Back", getString(R.string.exit_msg));
    }

    protected void exitByBackKey(String action, String msg) {

        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage(msg)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                        // Clear the session data
                        // This will clear all session data and
                        //  rdirect user to LoginActivity


//                        getActivity().finish();
//                        Intent i = new Intent(getContext(), LoginWithMobile.class);
//                        startActivity(i);
                        if (action.equalsIgnoreCase("Logout")) {
                            session.logoutUser();
                            FragmentManager mFragmentManager = getSupportFragmentManager();
                            if (mFragmentManager.getBackStackEntryCount() > 0)
                                mFragmentManager.popBackStackImmediate();

                        }
                        finish();

//                        System.exit(0);


//                        int pid = android.os.Process.myPid();
//                        android.os.Process.killProcess(pid);
//                        Toast.makeText(getApplicationContext(), "User Is Wish To Exit", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
//                        Toast.makeText(getApplicationContext(), "User Is Not Wish To Exit", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();

    }


}
