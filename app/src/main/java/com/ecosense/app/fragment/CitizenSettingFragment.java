package com.ecosense.app.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.activity.citizen.CitizenProfileDetail;
import com.ecosense.app.helper.UserSessionManger;

/**
 * A simple {@link Fragment} subclass.
 */
public class CitizenSettingFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = CitizenSettingFragment.class.getSimpleName();

    @BindView(R.id.tv_logout)
    TextView tv_logout;

    @BindView(R.id.tv_aboutUs)
    TextView tv_aboutUs;

    @BindView(R.id.tv_PrivacyPolicy)
    TextView tv_PrivacyPolicy;

    @BindView(R.id.tv_TermsOfUse)
    TextView tv_TermsOfUse;

    @BindView(R.id.tv_FAQ)
    TextView tv_FAQ;

    @BindView(R.id.tv_WhatIsDWMS)
    TextView tv_WhatIsDWMS;

    Toolbar toolbar;
    UserSessionManger session = null;
    public CitizenSettingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        session = new UserSessionManger(getActivity());
        Log.e(TAG, "getAppLanguage : " + session.getAppLanguage());
        session.updateAppLanguage(getActivity(), session.getAppLanguage());
        View view = inflater.inflate(R.layout.fragment_citizen_setting, container, false);
        ButterKnife.bind(this, view);

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Delivery Counter");

        tv_logout.setOnClickListener(this);
        tv_aboutUs.setOnClickListener(this);
        tv_TermsOfUse.setOnClickListener(this);
        tv_PrivacyPolicy.setOnClickListener(this);
        tv_FAQ.setOnClickListener(this);
        tv_WhatIsDWMS.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == tv_logout) {
            exitByBackKey();
        }
        if (v == tv_aboutUs) {
            Intent mIntent = new Intent(getActivity(), CitizenProfileDetail.class); // the activity that holds the fragment
            Bundle b = new Bundle();
            b.putString("SelectProfile", tv_aboutUs.getText().toString());
            mIntent.setAction("SelectProfile");
            mIntent.putExtra("SelectedNameDetail", b);
            startActivity(mIntent);
        }
        if (v == tv_PrivacyPolicy) {
            Intent mIntent = new Intent(getActivity(), CitizenProfileDetail.class); // the activity that holds the fragment
            Bundle b = new Bundle();
            b.putString("SelectProfile", tv_PrivacyPolicy.getText().toString());
            mIntent.setAction("SelectProfile");
            mIntent.putExtra("SelectedNameDetail", b);
            startActivity(mIntent);
        }

        if (v == tv_TermsOfUse) {
            Intent mIntent = new Intent(getActivity(), CitizenProfileDetail.class); // the activity that holds the fragment
            Bundle b = new Bundle();
            b.putString("SelectProfile", tv_TermsOfUse.getText().toString());
            mIntent.setAction("SelectProfile");
            mIntent.putExtra("SelectedNameDetail", b);
            startActivity(mIntent);
        }
        if (v == tv_FAQ) {
            Intent mIntent = new Intent(getActivity(), CitizenProfileDetail.class); // the activity that holds the fragment
            Bundle b = new Bundle();
            b.putString("SelectProfile", tv_FAQ.getText().toString());
            mIntent.setAction("SelectProfile");
            mIntent.putExtra("SelectedNameDetail", b);
            startActivity(mIntent);
        }
        if (v == tv_WhatIsDWMS) {
            Intent mIntent = new Intent(getActivity(), CitizenProfileDetail.class); // the activity that holds the fragment
            Bundle b = new Bundle();
            b.putString("SelectProfile", tv_WhatIsDWMS.getText().toString());
            mIntent.setAction("SelectProfile");
            mIntent.putExtra("SelectedNameDetail", b);
            startActivity(mIntent);
        }

    }
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_search, menu);
//        MenuItem searchItem = menu.findItem(R.id.action_filter_search);

//        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
//
//        if (searchItem != null) {
//            searchView = (SearchView) searchItem.getActionView();
//        }
//        if (searchView != null) {
//            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//            searchView.setOnQueryTextListener(this);

//            queryTextListener = new SearchView.OnQueryTextListener() {
//                @Override
//                public boolean onQueryTextChange(String newText) {
//                    Log.i("onQueryTextChange", newText);
//
//                    return true;
//                }
//                @Override
//                public boolean onQueryTextSubmit(String query) {
//                    Log.i("onQueryTextSubmit", query);
//
//                    return true;
//                }
//            };
//            searchView.setOnQueryTextListener(queryTextListener);
//        }
//        super.onCreateOptionsMenu(menu, inflater);
//    }


    protected void exitByBackKey() {

        AlertDialog alertbox = new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.do_you_want_to_logout))
                .setPositiveButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                        // Clear the session data
                        // This will clear all session data and
                        //  rdirect user to LoginActivity
                        FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
                        if (mFragmentManager.getBackStackEntryCount() > 0)
                            mFragmentManager.popBackStackImmediate();


//                        getActivity().finish();
//                        Intent i = new Intent(getContext(), LoginWithMobile.class);
//                        startActivity(i);
                        session.logoutUser();
//                        System.exit(0);


//                        int pid = android.os.Process.myPid();
//                        android.os.Process.killProcess(pid);
//                        Toast.makeText(getApplicationContext(), "User Is Wish To Exit", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.btn_no), new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
//                        Toast.makeText(getApplicationContext(), "User Is Not Wish To Exit", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();

    }

}
