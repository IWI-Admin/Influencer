package com.socialbeat.influencer;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class AllCampaignFragmentPast extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = AllCampaignFragmentPast.class.getSimpleName();
    private CoordinatorLayout coordinatorLayout;
    Boolean isInternetPresent = false;
    // Connection detector class
    ConnectionDetector cd;
    Context context;
    private ProgressDialog pDialog;
    private List<CampValues> campValuesList = new ArrayList<CampValues>();
    private ListView listView;
    private CustomListAdapter adapter;
    String cid,url,valueofcid;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.alllivecamp, container, false);
        context = v.getContext();
        cd = new ConnectionDetector(getActivity());
        coordinatorLayout = v.findViewById(R.id.coordinatorLayout);
        isInternetPresent = cd.isConnectingToInternet();
        SharedPreferences prfs = this.getActivity().getSharedPreferences("CID_VALUE", Context.MODE_PRIVATE);
        cid = prfs.getString("valueofcid", "");
        Log.v("Cid Value : ",cid);
        swipeRefreshLayout = v.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        if (isInternetPresent) {

            listView = v.findViewById(R.id.overalllist);
            adapter = new CustomListAdapter(getActivity(), campValuesList);

            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {

                    String campid = campValuesList.get(position).getCampid();
                    String campName = campValuesList.get(position).getCampName();
                    String campImg = campValuesList.get(position).getCampImg();
                    String campCat = campValuesList.get(position).getCampCat();
                    String campShortNote = campValuesList.get(position).getCampShortNote();

                    Intent intent = new Intent(getActivity(), AllCampDetailsLive.class);
                    Bundle bund = new Bundle();
                    bund.putString("campid", campid);
                    bund.putString("campName", campName);
                    bund.putString("campImg", campImg);
                    bund.putString("campCat", campCat);
                    bund.putString("campShortNote", campShortNote);
                    intent.putExtras(bund);
                    startActivity(intent);
                }
            });

            CampaignsFunction();

        }else {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "No internet connection!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("SETTINGS", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(Settings.ACTION_SETTINGS));
                        }
                    });
            // Changing message text color
            snackbar.setActionTextColor(Color.RED);
            // Changing action button text color
            View sbView = snackbar.getView();
            TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);
            snackbar.show();
        }
        return v;
    }

    private void CampaignsFunction() {

        pDialog = new ProgressDialog(getActivity());
        // Showing progress dialog before making http request
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        String LIVE_CAMP_URL = getResources().getString(R.string.base_url)+getResources().getString(R.string.past_campaign_url);
        JsonArrayRequest campobj = new JsonArrayRequest(LIVE_CAMP_URL,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d(TAG, response.toString());
                hidePDialog();

                // Parsing json
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject obj = response.getJSONObject(i);

                        CampValues campvalue = new CampValues();
                        campvalue.setCampid(obj.getString("campid"));
                        campvalue.setCampName(obj.getString("campName"));
                        campvalue.setCampImg(obj.getString("campImg"));
                        campvalue.setCampCat(obj.getString("campCat"));
                        campvalue.setCampShortNote(obj.getString("campShortNote"));
                        campvalue.setCampShortNote(obj.getString("campShortNote"));
                        // adding contentofCampaigns to movies array
                        campValuesList.add(campvalue);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        MyApplication.getInstance().trackException(e);
                        Log.e(TAG, "Exception: " + e.getMessage());
                    }
                }
                // notifying list adapter about data changes
                // so that it renders the list view with updated data
                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                hidePDialog();
            }
        });
        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(campobj);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tracking the screen view
        MyApplication.getInstance().trackScreenView("Past Campaigns List Screen");
    }
    public static AllCampaignFragmentPast newInstance() {
        return (new AllCampaignFragmentPast());
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        AllCampaignFragmentPast fragment = new AllCampaignFragmentPast();
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
        swipeRefreshLayout.setRefreshing(false);
    }
}