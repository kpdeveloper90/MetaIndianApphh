package com.ecosense.app.activity.citizen;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ecosense.app.R;
import com.ecosense.app.fragment.CitizenDashBoardFragment;
import com.ecosense.app.fragment.CitizenSettingFragment;
import com.ecosense.app.fragment.ComplaintFragment;
import com.ecosense.app.fragment.NotificationFragment;
import com.ecosense.app.fragment.ServiceRequestListFragment;
import com.ecosense.app.fragment.SuggestionsFragment;
import com.ecosense.app.fragment.SurveyListFragment;
import com.ecosense.app.fragment.TrackersFragment;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.menu.DrawerAdapter;
import com.ecosense.app.menu.DrawerItem;
import com.ecosense.app.menu.SimpleItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.yarolegovich.slidingrootnav.SlideGravity;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.Arrays;


public class CitizenDashBoard extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener {
    private static final String TAG = CitizenDashBoard.class.getSimpleName();
    private static final int DRAWER_HOME = 0;
    private static final int DRAWER_TAKE_A_SURVEY = 1;
    private static final int DRAWER_MY_CITY = 2;
    private static final int DRAWER_COMPLAINTS = 3;
    private static final int DRAWER_Suggestions = 4;
    private static final int DRAWER_NOTIFICATION = 5;
    private static final int DRAWER_SETTING = 6;
//    private static final int DRAWER_LOGOUT = 6;

    private String[] screenTitles;
    private Drawable[] screenIcons;

    private SlidingRootNav slidingRootNav;
    private Toolbar toolbar;
    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    UserSessionManger session = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        session = new UserSessionManger(this);
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(this, session.getAppLanguage());
        super.onCreate(savedInstanceState);

        /* Set the app into full screen mode */
//        getWindow().getDecorView().setSystemUiVisibility(flags);

        setContentView(R.layout.activity_citizen_dash_board);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);

//        toolbar = getSupportActionBar();

//        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

//        toolbar.setTitle("Home");
//        loadFragment(new CitizenDashBoardFragment());

        slidingRootNav = new SlidingRootNavBuilder(this)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false) //Initial menu opened/closed state. Default == false
                .withMenuLocked(false) //If true, a user can't open or close the menu. Default == false.
                .withDragDistance(140) //Horizontal translation of a view. Default == 180dp
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
                createItemFor(DRAWER_HOME).setChecked(true),
                createItemFor(DRAWER_TAKE_A_SURVEY),
                createItemFor(DRAWER_MY_CITY),
                createItemFor(DRAWER_COMPLAINTS),
                createItemFor(DRAWER_Suggestions),
                createItemFor(DRAWER_NOTIFICATION),
                createItemFor(DRAWER_SETTING)));
        adapter.setListener(this);


        CircularImageView img_drawer_profile = findViewById(R.id.img_drawer_profile);
//        img_drawer_profile.setImageDrawable(getCharacterImage(session.getpsName().substring(0, 1)));

        if (session.getUserProfilePic() != null) {
//                String url = session.getMyServerIP() + session.getUserProfilePic();
            if(session.getUserProfilePic().length()>0){
                Log.e(TAG,"getUserProfilePic is hear");
                Glide.with(this).load(Connection.decodeFromBase64ToBitmap(session.getUserProfilePic()))
                        .apply(RequestOptions.centerCropTransform())
                        .into(img_drawer_profile);
            }else {
                Log.e(TAG,"getUserProfilePic is size hear");
                Glide.with(this).load(R.drawable.ic_user_1)
                        .apply(RequestOptions.centerCropTransform())
                        .into(img_drawer_profile);
            }
        } else {
            Log.e(TAG,"getUserProfilePic is not hear => " +session.getUserProfilePic());
            Glide.with(this).load(R.drawable.ic_user_1)
                    .apply(RequestOptions.centerCropTransform())
                    .into(img_drawer_profile);
        }

        TextView tv_userName = findViewById(R.id.tv_userName);
        TextView tv_uMno = findViewById(R.id.tv_uMno);
        tv_userName.setText(session.getpsName());
        tv_uMno.setText("(M) : " + session.getMobileNumber());
        ImageView img_btn_edit = findViewById(R.id.img_btn_edit);

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
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        adapter.setSelected(DRAWER_HOME);

    }

    private boolean isNotificationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.ecosense.app.service.NotificationService".equals(service.service.getClassName())) {
                Log.e(TAG, " SyncService Already Running  return true =  >>>  :" + service.service.getClassName());
                return true;
            }
        }
        Log.e(TAG, " SyncService not Running  return false");
        return false;
    }

    public Drawable getCharacterImage(String s) {
        ColorGenerator generator = ColorGenerator.DEFAULT; // or use DEFAULT
// generate random color
        int color1 = generator.getRandomColor();

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
//                .withBorder(3) /* thickness in px */
//                .textColor(Color.BLACK)
//                .useFont(Typeface.DEFAULT)
//                .fontSize(30) /* size in px */
//                .bold()
                .toUpperCase()
                .endConfig()
                .buildRound(s, color1);


        return drawable;
    }

    @Override
    public void onItemSelected(int position) {

        // Navigate to the right fragment
        switch (position) {
            case DRAWER_HOME:
                setToolbarTitle("Home");
                loadFragment(new CitizenDashBoardFragment());
                break;
            case DRAWER_TAKE_A_SURVEY:
                setToolbarTitle("Take a RoutePoint");
                loadFragment(new SurveyListFragment());
                break;
            case DRAWER_MY_CITY:
                setToolbarTitle("My City");
                loadFragment(new TrackersFragment());
                break;
            case DRAWER_COMPLAINTS:
                if (session.getuserSubType().equalsIgnoreCase(AppConfig.USubType_Corporate)) {
                    setToolbarTitle("Service Request");
                    loadFragment(new ServiceRequestListFragment());
                } else {
                    setToolbarTitle("Complaints Dashboard");
                    loadFragment(new ComplaintFragment());
                }
                break;
            case DRAWER_Suggestions:
                setToolbarTitle("Suggestions");
                loadFragment(new SuggestionsFragment());
                break;
            case DRAWER_NOTIFICATION:
                setToolbarTitle("Notifications");
                loadFragment(new NotificationFragment());
                break;
            case DRAWER_SETTING:
                setToolbarTitle("DWMS");
                loadFragment(new CitizenSettingFragment());
                break;
            default:
                Toast.makeText(getApplicationContext(), getString(R.string.somthing_wrong), Toast.LENGTH_SHORT).show();
                break;
        }

        if (slidingRootNav.isMenuOpened()) {
            slidingRootNav.closeMenu(true);
        }
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
        if (session.getuserSubType().equalsIgnoreCase(AppConfig.USubType_Corporate)) {
            return getResources().getStringArray(R.array.ld_Drawer_corporate_ScreenTitles);
        } else {
            return getResources().getStringArray(R.array.ld_Drawer_individual_activityScreenTitles);
        }
    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = null;
        if (session.getuserSubType().equalsIgnoreCase(AppConfig.USubType_Corporate)) {
            ta = getResources().obtainTypedArray(R.array.ld_Drawer_corporate_activityScreenIcons);
        } else {
            ta = getResources().obtainTypedArray(R.array.ld_Drawer_individual_activityScreenIcons);
        }

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

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        Fragment fragment;
        switch (item.getItemId()) {
            case R.id.navigation_Home:
                //                    toolbar.setTitle("Home");
                fragment = new CitizenDashBoardFragment();
                //                    fragment = new News2Fragment();
                loadFragment(fragment);
                return true;
            case R.id.navigation_Trackers:
                //                    toolbar.setTitle("My City");
                fragment = new TrackersFragment();
                loadFragment(fragment);

                return true;
            case R.id.navigation_complain:
                //                    toolbar.setTitle("Complaints");
                fragment = new ComplaintFragment();
                loadFragment(fragment);
                return true;
            case R.id.navigation_notification:
                //                    toolbar.setTitle("Notification");
                fragment = new NotificationFragment();
                loadFragment(fragment);
                return true;
            case R.id.navigation_profile:
                //                    toolbar.setTitle("Profile");
                fragment = new CitizenSettingFragment();
                loadFragment(fragment);
                return true;
        }
        return false;
    };

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        exitByBackKey("Back", getString(R.string.exit_msg));
    }

}
