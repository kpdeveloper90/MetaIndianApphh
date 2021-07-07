package com.ecosense.app.fragment;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.helper.UserSessionManger;

/**
 * A simple {@link Fragment} subclass.
 */
public class ComplaintFragment extends Fragment {

    private static final String TAG = ComplaintFragment.class.getSimpleName();

    @BindView(R.id.tab_ComplaintsDashboard)
    TabLayout tab_ComplaintsDashboard;

    @BindView(R.id.vp_ComplaintsDashboard)
    ViewPager vp_ComplaintsDashboard;
    Toolbar toolbar;

    public ComplaintFragment() {
        // Required empty public constructor
    }

UserSessionManger session=null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        session = new UserSessionManger(getActivity());
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(getActivity(), session.getAppLanguage());
        View view = inflater.inflate(R.layout.fragment_complaint, container, false);
        ButterKnife.bind(this, view);

//        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
//        toolbar.setTitle("Complaints Dashboard");

//        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        tab_ComplaintsDashboard.addTab(tab_ComplaintsDashboard.newTab().setText(getString(R.string.tab_Dashboard)));
        tab_ComplaintsDashboard.addTab(tab_ComplaintsDashboard.newTab().setText(getString(R.string.yab_My_Activity)));
//        tab_ComplaintsDashboard.addTab(tab_ComplaintsDashboard.newTab().setText("Suggestions"));

        tab_ComplaintsDashboard.setTabGravity(TabLayout.GRAVITY_FILL);

        loadFragment(new ComplaintDashBoardFragment());
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

        tab_ComplaintsDashboard.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.e(TAG, "tab.getPosition() = " + tab.getPosition());
//                vp_citizenDashboard.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()) {
                    case 0:
                        Log.e("ComplaintDashBoard", "ComplaintDashBoardFragment");
                        loadFragment(new ComplaintDashBoardFragment());
                        break;
                    case 1:
                        Log.e("ComplaintsListFragment", "ComplaintsListFragment");
                        loadFragment(new ComplaintsListFragment());
                        break;
//                    case 2:
//                        Log.e("SuggestionsFragment", "SuggestionsFragment");
//                        loadFragment(new SuggestionsFragment());
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
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_comDashBoard_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new ComplaintDashBoardFragment(), "Dashboard");
        adapter.addFragment(new ComplaintsListFragment(), "My Activity");
        adapter.addFragment(new SuggestionsFragment(), "Suggestions");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            Log.e("ViewPagerAdapter ", "pos = " + position);
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}