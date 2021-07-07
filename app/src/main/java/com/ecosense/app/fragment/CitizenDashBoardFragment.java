package com.ecosense.app.fragment;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import androidx.fragment.app.FragmentPagerAdapter;

import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;


import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.broadcastReceiver.ConnectionReceiver;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.UserSessionManger;

/**
 * A simple {@link Fragment} subclass.
 */
public class CitizenDashBoardFragment extends Fragment implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = CitizenDashBoardFragment.class.getSimpleName();



    @BindView(R.id.tab_citizenDashboard)
    TabLayout tab_citizenDashboard;
    static ProgressDialog mProgressDialog;

    Toolbar toolbar;
    Boolean isConnected = false;
    UserSessionManger session;

    public CitizenDashBoardFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        session = new UserSessionManger(getActivity());
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(getActivity(), session.getAppLanguage());
        View view = inflater.inflate(R.layout.fragment_citizen_dash_board, container, false);
        ButterKnife.bind(this, view);




        tab_citizenDashboard.addTab(tab_citizenDashboard.newTab().setText(getString(R.string.tab_news)));
        tab_citizenDashboard.addTab(tab_citizenDashboard.newTab().setText(getString(R.string.tab_events)));
        tab_citizenDashboard.addTab(tab_citizenDashboard.newTab().setText(getString(R.string.tab_how_to_s)));
//        tab_citizenDashboard.addTab(tab_citizenDashboard.newTab().setText(getString(R.string.tav_favorites)));

        tab_citizenDashboard.setTabGravity(TabLayout.GRAVITY_FILL);

        loadFragment(new News2Fragment());
//        setupViewPager(vp_ComplaintsDashboard);
//
//        tab_ComplaintsDashboard.setupWithViewPager(vp_ComplaintsDashboard);
//        vp_ComplaintsDashboard.setOffscreenPageLimit(tab_ComplaintsDashboard.getTabCount() - 1);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Delivery Counter");

        tab_citizenDashboard.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.e(TAG, "tab.getPosition() = " + tab.getPosition());
//                vp_citizenDashboard.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()) {
                    case 0:
                        loadFragment(new News2Fragment());
                        break;
                    case 1:
                        loadFragment(new EvenetsFragment());
                        break;
                    case 2:
                        loadFragment(new HowTosFragment());
                        break;
//                    case 3:
//                        loadFragment(new FavoritesFragment());
//                        break;
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

    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_comDashBoard_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onClick(View v) {

        if (v == alert_no2) {
            alert_dialog2.dismiss();
        }

        if (v == alert_yes) {
            alert_dialog1.dismiss();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
        if (v == alert_no) {
            alert_dialog1.dismiss();
        }
    }


    public class PageAdapter extends FragmentPagerAdapter {
        int mNumOfTabs;

        //        public PageAdapter(FragmentManager fm, int numTabs) {
//            super(fm);
//            this.mNumOfTabs = numTabs;
//        }
        public PageAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int position) {
            Log.e("Adapter Call ", "POSITION = " + position);
            switch (position) {
                case 0:
                    Log.e("News2Fragment", "News2Fragment");
                    News2Fragment news = new News2Fragment();
                    return news;
                case 1:
                    Log.e("EvenetsFragment", "EvenetsFragment");
                    EvenetsFragment event = new EvenetsFragment();
                    return event;
                case 2:
                    Log.e("HowTosFragment", "HowTosFragment");
                    HowTosFragment howto = new HowTosFragment();
                    return howto;

                case 3:
                    Log.e("favFragment", "favFragment");
                    FavoritesFragment fav = new FavoritesFragment();
                    return fav;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {

            //return mNumOfTabs;
            return 4;
        }


    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_search, menu);
//        if (menu != null) {
//            menu.findItem(R.id.action_filter_search).setVisible(false);
//        }
//        super.onCreateOptionsMenu(menu, inflater);
//
//    }

    Dialog alert_dialog1, alert_dialog2;
    Button alert_yes, alert_no, alert_no2;
    TextView alert_msg, alert_msg2;


    public void show_alert_two_button(String msg) {


        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.alert_dialog_custom, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        alert_dialog1 = new Dialog(getActivity());
        alert_dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert_dialog1.setContentView(root);
        alert_dialog1.setCancelable(false);
        alert_dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        alert_dialog1.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        alert_yes = (Button) alert_dialog1.findViewById(R.id.alert_close);
        alert_msg = (TextView) alert_dialog1.findViewById(R.id.alert_msg);
        alert_no = (Button) alert_dialog1.findViewById(R.id.alert_no);
        alert_yes.setText("Yes");
        alert_msg.setText(msg);
        alert_no.setVisibility(View.VISIBLE);


        alert_yes.setOnClickListener(this);
        alert_no.setOnClickListener(this);


        alert_dialog1.show();
    }

    public void show_alert_Dialog_singlebutton(String msg) {

        int counter = 0;
        LayoutInflater inflater = getLayoutInflater();
        final View root = inflater.inflate(R.layout.alert_dialog_custom, null);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        alert_dialog2 = new Dialog(getActivity());
        alert_dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert_dialog2.setContentView(root);
        alert_dialog2.setCancelable(false);
        alert_dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.popWIndow)));
        alert_dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        alert_no2 = (Button) alert_dialog2.findViewById(R.id.alert_close);
        alert_msg2 = (TextView) alert_dialog2.findViewById(R.id.alert_msg);

        alert_msg2.setText(msg);


        alert_no2.setOnClickListener(this);


        alert_dialog2.show();
    }

    // Method to manually check connection status
    private boolean checkConnection() {
        return isConnected = ConnectionReceiver.isConnected();
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = "Back-Online";
            color = Color.WHITE;
        } else {
//            message = "Sorry! Not connected to internet";
            message = "You're Offline";
            color = Color.RED;
        }

//        Snackbar snackbar = Snackbar
//                .make(findViewById(R.id.conl_login), message, Snackbar.LENGTH_LONG);

//        View sbView = snackbar.getView();
//        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
//        textView.setTextColor(color);
//        snackbar.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume => " + TAG);
        // register connection status listener
//        CookieManager manager = new CookieManager();
//        CookieHandler.setDefault(manager);
//        FindLoginDetailFromServer( "Administrator", "tspl");
        ConnactionCheckApplication.getInstance().setConnectionListener(this);
    }
    @Override
    public void onPause(){
        Log.e(TAG, "onPause => " + TAG);
        super.onPause();
        if(mProgressDialog != null)
            mProgressDialog.dismiss();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy => " + TAG);
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

}
/**
 * private void setupViewPager(ViewPager viewPager) {
 * ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager());
 * adapter.addFragment(new News2Fragment(), "News");
 * adapter.addFragment(new EvenetsFragment(), "Events");
 * adapter.addFragment(new HowTosFragment(), "How To's");
 * adapter.addFragment(new EvenetsFragment(), "Favorites");
 * viewPager.setAdapter(adapter);
 * }
 * <p>
 * class ViewPagerAdapter extends FragmentPagerAdapter {
 * private final List<Fragment> mFragmentList = new ArrayList<>();
 * private final List<String> mFragmentTitleList = new ArrayList<>();
 * <p>
 * public ViewPagerAdapter(android.support.v4.app.FragmentManager manager) {
 * super(manager);
 * }
 *
 * @Override public Fragment getItem(int position) {
 * return mFragmentList.get(position);
 * }
 * @Override public int getCount() {
 * return mFragmentList.size();
 * }
 * <p>
 * public void addFragment(Fragment fragment, String title) {
 * mFragmentList.add(fragment);
 * mFragmentTitleList.add(title);
 * }
 * @Override public CharSequence getPageTitle(int position) {
 * return mFragmentTitleList.get(position);
 * }
 * }}
 */