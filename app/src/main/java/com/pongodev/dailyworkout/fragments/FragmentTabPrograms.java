package com.pongodev.dailyworkout.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.pongodev.dailyworkout.R;
import com.pongodev.dailyworkout.adapters.AdapterPrograms;
import com.pongodev.dailyworkout.utils.DBHelperPrograms;
import com.pongodev.dailyworkout.utils.OnTapListener;

import java.util.ArrayList;

/**
 * Created by keong on 12/24/2014.
 */
public class FragmentTabPrograms extends Fragment{
    private OnSelectedListener mCallback;

    // Declare view objects
    private RecyclerView recyclerView;
    private ProgressBarCircularIndeterminate prgLoading;
    private TextView lblNoResult;

    // Create object of custom adapter
    private AdapterPrograms la;
    private DBHelperPrograms dbPrograms;

    // Create arraylist variables to store data
    private ArrayList<String> programDayIds = new ArrayList<String>();
    private ArrayList<String> programDayNames = new ArrayList<String>();
    private ArrayList<String> programTotal = new ArrayList<String>();

    // Create arraylist variable to store object data from database
    private ArrayList<ArrayList<Object>> data;

    // Create interface for listener method
    public interface OnSelectedListener{
        public void onSelectedDay(String selectedID, String selectedName);
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FragmentTabPrograms newInstance() {
        FragmentTabPrograms fragment = new FragmentTabPrograms();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View v = inflater.inflate(R.layout.fragment_list, container, false);

        // Connect view objects and view id on xml.
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        prgLoading   = (ProgressBarCircularIndeterminate) v.findViewById(R.id.prgLoading);
        lblNoResult  = (TextView) v.findViewById(R.id.lblNoResult);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // Check database
        dbPrograms = new DBHelperPrograms(getActivity());
        dbPrograms.checkDBPrograms();

        // Call asynctask class to get data from database
        new getDataList().execute();

        la = new AdapterPrograms(getActivity());
        la.setOnTapListener(new OnTapListener() {
            @Override
            public void onTapView(String selectedId, String selectedName) {
                mCallback.onSelectedDay(selectedId, selectedName);
            }
        });

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    // Asynctask class that is used to fetch data from database in background
    private class getDataList extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            // show progress dialog when fetching data from database
            super.onPreExecute();
            prgLoading.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            getDataFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // TODO Auto-generated method stub
            // when finishing fetching data, close progress dialog and show data on listview
            // if available, otherwise show toast message
            super.onPostExecute(aVoid);
            prgLoading.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            la.updateList(programDayIds, programDayNames, programTotal);

            if(programDayIds.size() != 0){
                recyclerView.setAdapter(la);
            }else{
                lblNoResult.setVisibility(View.VISIBLE);
            }
        }
    }

    // method to fetch data from database
    private void getDataFromDatabase(){
        clearData();
        data = dbPrograms.getAllDays();

        // Store data to arraylist variables
        for(int i=0;i<data.size();i++){
            ArrayList<Object> row = data.get(i);

            programDayIds.add(row.get(0).toString());
            programDayNames.add(row.get(1).toString());
            programTotal.add(row.get(2).toString());
        }

    }

    private void clearData(){
        programDayIds.clear();
        programDayNames.clear();
        programTotal.clear();
    }

    public void onStart(){
        super.onStart();
        // Call asynctask class to get data from database
        new getDataList().execute();
    }
    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        dbPrograms.close();
        super.onDestroy();
    }

}
