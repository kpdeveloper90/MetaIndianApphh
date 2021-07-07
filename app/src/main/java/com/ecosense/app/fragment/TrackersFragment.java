package com.ecosense.app.fragment;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.sdsmdg.tastytoast.TastyToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.activity.citizen.AssetsStatusTracking;
import com.ecosense.app.activity.citizen.Helpline;
import com.ecosense.app.activity.citizen.ToiletLocators;
import com.ecosense.app.helper.UserSessionManger;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrackersFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = TrackersFragment.class.getSimpleName();

//    @BindView(R.id.tab_Trackers)
//    TabLayout tab_Trackers;
//
//    @BindView(R.id.vp_Trackers)
//    ViewPager vp_Trackers;

    private static final int ERROR_DIALOG_REQUEST = 9001;

    @BindView(R.id.ll_my_city)
    LinearLayout ll_my_city;
    @BindView(R.id.ll_swachh_bharat)
    LinearLayout ll_swachh_bharat;
    @BindView(R.id.ll_nearby_facilities)
    LinearLayout ll_nearby_facilities;
    @BindView(R.id.ll_bin_locators)
    LinearLayout ll_bin_locators;
    @BindView(R.id.ll_toilet_locators)
    LinearLayout ll_toilet_locators;
    @BindView(R.id.ll_helpline)
    LinearLayout ll_helpline;

    public TrackersFragment() {
        // Required empty public constructor
    }


    Toolbar toolbar;
UserSessionManger session=null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        session = new UserSessionManger(getActivity());
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(getActivity(), session.getAppLanguage());
        View view = inflater.inflate(R.layout.fragment_trackers, container, false);
        ButterKnife.bind(this, view);



//        setupViewPager(vp_Trackers);

//        tab_Trackers.setupWithViewPager(vp_Trackers);
        ll_my_city.setOnClickListener(this);
        ll_swachh_bharat.setOnClickListener(this);
        ll_nearby_facilities.setOnClickListener(this);
        ll_bin_locators.setOnClickListener(this);
        ll_toilet_locators.setOnClickListener(this);
        ll_helpline.setOnClickListener(this);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Delivery Counter");

    }

    private static final int PLACE_PICKER_REQUEST = 1;

    @Override
    public void onClick(View v) {
        if (v == ll_my_city) {
//            Intent intent = new Intent(getActivity(), AboutCity.class);
//            startActivity(intent);
            alertforRediretToWebB("https://vmc.gov.in/");
        }
        if (v == ll_swachh_bharat) {
            alertforRediretToWebB("http://164.100.228.143/sbm/home/#/SBM?encryptdata=eK991SygGmX%2Bq%2FQNzbuhMQtO9tMJKhke4uxrEMrnehoRjWCfz7kBv4fGMowof1lIhEHUJQwHRzpUR85qQjlTi3VI6QYG7608Rf7s08B03aU%3D");
//            Intent intent = new Intent(getActivity(), SwachhBharat.class);
//            startActivity(intent);
        }
        if (v == ll_nearby_facilities) {
//            Uri uri = Uri.parse("http://maps.google.com/maps?saddr=" + MyLat + "," + MyLong + "&daddr=" + Lagi + "," + Long);
            Uri uri = Uri.parse("http://maps.google.com/maps");
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
            startActivity(intent);

//            Intent intent = new Intent(getActivity(), NearByPlaceTypeName.class);
//            startActivity(intent);
        }
        if (v == ll_bin_locators) {
            Intent intent = new Intent(getActivity(), AssetsStatusTracking.class);
            startActivity(intent);
//            if (isServicesOK()) {
//                Intent intent = new Intent(getActivity(), MapsActivity.class);
//                startActivity(intent);
//            }
        }
        if (v == ll_toilet_locators) {
            if (isServicesOK()) {
                Intent intent = new Intent(getActivity(), ToiletLocators.class);
                startActivity(intent);
            }
        }
        if (v == ll_helpline) {
            Intent intent = new Intent(getActivity(), Helpline.class);
            startActivity(intent);
        }
    }

    protected void alertforRediretToWebB(String url) {

        // do something when the button is clicked
        AlertDialog alertbox = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.redirect_webpage_title))
                .setIcon(getResources().getDrawable(R.drawable.ic_directions_black_24dp))
                .setMessage(getString(R.string.redirect_webpage_msg))
                .setPositiveButton(getString(R.string.btn_ok), (arg0, arg1) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                })
                .setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                })
                .show();

    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            TastyToast.makeText(getActivity(), getString(R.string.You_can_t_make_map_requests), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        if (menu != null) {
            menu.findItem(R.id.action_filter_search).setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }
/**
 private void setupViewPager(ViewPager viewPager) {
 ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
 adapter.addFragment(new FrgBinLocator(), "Map");
 adapter.addFragment(new BinStatusListFragment(), "Status");
 viewPager.setAdapter(adapter);
 }

 class ViewPagerAdapter extends FragmentPagerAdapter {
 private final List<Fragment> mFragmentList = new ArrayList<>();
 private final List<String> mFragmentTitleList = new ArrayList<>();

 public ViewPagerAdapter(android.support.v4.app.FragmentManager manager) {
 super(manager);
 }

 @Override public Fragment getItem(int position) {
 return mFragmentList.get(position);
 }

 @Override public int getCount() {
 return mFragmentList.size();
 }

 public void addFragment(Fragment fragment, String title) {
 mFragmentList.add(fragment);
 mFragmentTitleList.add(title);
 }

 @Override public CharSequence getPageTitle(int position) {
 return mFragmentTitleList.get(position);
 }
 }*/
}
