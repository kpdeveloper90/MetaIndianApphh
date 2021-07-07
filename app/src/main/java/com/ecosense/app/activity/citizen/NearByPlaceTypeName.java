package com.ecosense.app.activity.citizen;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.ecosense.app.R;
import com.ecosense.app.adapter.NearByPlaceTypeListAdapter;
import com.ecosense.app.helper.RecyclerItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.PlaceInfo;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class NearByPlaceTypeName extends AppCompatActivity implements View.OnClickListener, SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = NearByPlaceTypeName.class.getSimpleName();
    Toolbar toolbar, searchtollbar;
    Menu search_menu;
    MenuItem item_search;
    SearchView mSearchView;

    @BindView(R.id.srl_NearbyPlaceType)
    SwipeRefreshLayout srl_NearbyPlaceType;

    @BindView(R.id.rv_NearbyPlaceType)
    RecyclerView rv_NearbyPlaceType;

    NearByPlaceTypeListAdapter nearByPlaceTypeListAdapter = null;
    List<PlaceInfo> placeInfoList = null;
    private GridLayoutManager lLayout;
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
        setContentView(R.layout.activity_near_by_place_type_name);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.Nearby_Place);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });


        ButterKnife.bind(this);
        placeInfoList = new ArrayList<>();
//
//        setSearchtollbar();

        lLayout = new GridLayoutManager(this, 1);
        rv_NearbyPlaceType.setHasFixedSize(true);
        rv_NearbyPlaceType.setLayoutManager(lLayout);
        rv_NearbyPlaceType.setItemAnimator(new DefaultItemAnimator());
        nearByPlaceTypeListAdapter = new NearByPlaceTypeListAdapter(this, placeInfoList);
        rv_NearbyPlaceType.setAdapter(nearByPlaceTypeListAdapter);

        rv_NearbyPlaceType.addOnItemTouchListener(new RecyclerItemClickListener(this, rv_NearbyPlaceType,
                new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                PlaceInfo placeItem = placeInfoList.get(position);

                Log.e(TAG, " Name = " + placeItem.getName());


                Intent mIntent = new Intent(getApplicationContext(), NearByPlaceDetail.class); // the activity that holds the fragment
                Bundle b = new Bundle();
                b.putSerializable("PlaceInfo", placeItem);
                mIntent.setAction("PlaceSelect");
                mIntent.putExtra("SelectedNameDetail", b);
                startActivity(mIntent);
                finish();
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        }));

        srl_NearbyPlaceType.setOnRefreshListener(this);
        srl_NearbyPlaceType.setColorSchemeResources(R.color.colorAccent);
        srl_NearbyPlaceType.setNestedScrollingEnabled(true);
        srl_NearbyPlaceType.post(
                new Runnable() {
                    @Override
                    public void run() {
                        fill_RV();
                    }
                }
        );
    }


    private void fill_RV() {
        final ProgressDialog progressDialog = new ProgressDialog(NearByPlaceTypeName.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();
        new android.os.Handler().postDelayed(() -> {
            for (int i = 0; i < getResources().getStringArray(R.array.place_type).length; i++) {

                PlaceInfo placeInfoItem = new PlaceInfo();
                placeInfoItem.setName(getResources().getStringArray(R.array.place_type)[i]);
                placeInfoList.add(placeInfoItem);
            }
            nearByPlaceTypeListAdapter.notifyDataSetChanged();

            progressDialog.dismiss();

        }, PROGRASS_postDelayed);

    }

    @Override
    public void onRefresh() {
        srl_NearbyPlaceType.setRefreshing(true);
        placeInfoList.clear();
        nearByPlaceTypeListAdapter.notifyDataSetChanged();
        fill_RV();
        srl_NearbyPlaceType.setRefreshing(false);
    }

    EditText txtSearch;
    private SearchView searchView = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_search, menu);


//        // Get the search menu.
        MenuItem searchMenu = menu.findItem(R.id.action_filter_search);
        searchMenu.setVisible(true);
//        // Get SearchView object.
//        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenu);
//        return true;


        searchView = (SearchView) searchMenu.getActionView();
//            searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(this);


        ImageView closeButton = (ImageView) searchView.findViewById(R.id.search_close_btn);
        closeButton.setImageResource(R.drawable.ic_baseline_close_24);

//        txtSearch.setHighlightColor(ContextCompat.getColor(this, R.color.read_linearLayoutBg));
//        txtSearch.setAllCaps(true);
        txtSearch = ((EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text));
        txtSearch.setHint(getString(R.string.search));
        txtSearch.setHintTextColor(ContextCompat.getColor(this, R.color.tab_text));
        txtSearch.setTextColor(ContextCompat.getColor(this, R.color.white));
        txtSearch.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));


        return true;
    }

   @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 //        switch (item.getItemId()) {
 //
 //            case R.id.action_search:
 //
 ////                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
 ////                    circleReveal(R.id.searchtoolbar, 1, true, true);
 ////                else
 ////                    searchtollbar.setVisibility(View.VISIBLE);
 ////                item_search.expandActionView();
 //                return true;
 //
 //            default:
 //                break;
 //        }
         return super.onOptionsItemSelected(item);
     }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
//        if (query.length() > 0) {
//            mSearchView.isIconfiedByDefault();
//            mSearchView.setQuery("", false);
//            mSearchView.clearFocus();
//            getEmpDetailByPsNo(query);
//        } else {
//            show_alert_Dialog_singlebutton("Please Enter Appointment No.");
//        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final List<PlaceInfo> filteredModelList = filter(placeInfoList, query);
        nearByPlaceTypeListAdapter.setFilter(filteredModelList);
//
        return true;
    }


    private List<PlaceInfo> filter(List<PlaceInfo> models, String query) {
        query = query.toLowerCase();

        final List<PlaceInfo> filteredModelList = new ArrayList<>();
        for (PlaceInfo model : models) {
            final String text = model.getName().toLowerCase();

            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
