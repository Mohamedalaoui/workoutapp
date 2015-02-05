/*
* Copyright (c) 2015 Pongodev. All Rights Reserved.
*/
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
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonFloat;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.pongodev.dailyworkout.R;
import com.pongodev.dailyworkout.adapters.AdapterList;
import com.pongodev.dailyworkout.listeners.OnTapListener;
import com.pongodev.dailyworkout.utils.DBHelperPrograms;
import com.pongodev.dailyworkout.utils.DBHelperWorkouts;
import com.pongodev.dailyworkout.utils.Utils;

import java.util.ArrayList;

public class FragmentList extends Fragment implements View.OnClickListener{
    private OnSelectedListener mCallback;
    private OnClickListener mCallbackClick;

    // Declare view objects
    private RecyclerView recyclerView;
    private ProgressBarCircularIndeterminate prgLoading;
    private TextView lblNoResult;
    private ButtonFloat btnStartAll;

    // create object of custom adapter
    private AdapterList la;

    // Create arraylist variables to store data
    private ArrayList<String> ListId     = new ArrayList<>();
    private ArrayList<String> ListName   = new ArrayList<>();
    private ArrayList<String> ListImage  = new ArrayList<>();
    private ArrayList<String> ListTime   = new ArrayList<>();

    // Create variables to store selected value
    private String mSelectedID, mActivity;

    // Create interface for listener method
    public interface OnSelectedListener {
        public void onSelected(String selectedID, String selectedName);
    }

    // Create interface for listener method
    public interface OnClickListener {
        public void onClick(ArrayList<String> listId, ArrayList<String> listName, ArrayList<String> listTime);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View v = inflater.inflate(R.layout.fragment_list, container, false);

        setRetainInstance(true);

        Bundle args = getArguments();
        if (args != null) {
            // set article based on argument passed in
            mSelectedID = args.getString(Utils.ARG_ID);
            mActivity   = args.getString(Utils.ARG_PAGE);
        }

        // Connect view objects and view id on xml.
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        prgLoading   = (ProgressBarCircularIndeterminate) v.findViewById(R.id.prgLoading);
        lblNoResult  = (TextView) v.findViewById(R.id.lblNoResult);
        btnStartAll  = (ButtonFloat) v.findViewById(R.id.btnStartAll);

        btnStartAll.setOnClickListener(FragmentList.this);

        if(mActivity.equals(Utils.ARG_WORKOUT)){
            btnStartAll.setVisibility(View.GONE);
        }

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        la = new AdapterList(getActivity());
        la.setOnTapListener(new OnTapListener() {
            @Override
            public void onTapView(String ID, String selectedName) {

                mCallback.onSelected(ID, selectedName);
            }
        });

        new getDataList().execute();

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnSelectedListener) activity;
            mCallbackClick= (OnClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    // asynctask class that is used to fetch data from database in background
    private class getDataList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            // show progress dialog when fetching data from database
            super.onPreExecute();
            prgLoading.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            ListId.clear();
            ListName.clear();
            ListImage.clear();
            ListTime.clear();
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
            // if available, otherwise show label no result
            super.onPostExecute(aVoid);
            prgLoading.setVisibility(View.GONE);

            if(ListId.isEmpty()){
                btnStartAll.setVisibility(View.GONE);
            } else {
                lblNoResult.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                la.updateList(ListId, ListName, ListImage, ListTime);
            }

            if(!ListId.isEmpty() && mActivity.equals(Utils.ARG_PROGRAM) && ListId.size() > 1){
                btnStartAll.setVisibility(View.VISIBLE);
            } else {
                btnStartAll.setVisibility(View.GONE);
            }

            recyclerView.setAdapter(la);

        }
    }

    // Method to fetch data from database
    public void getDataFromDatabase() {

        // Check the data for workout or programs
        ArrayList<ArrayList<Object>> data;
        if(mActivity.equals(Utils.ARG_WORKOUT)){

            // Check database
            DBHelperWorkouts dbWorkouts = new DBHelperWorkouts(getActivity());
            dbWorkouts.checkDBWorkouts();
            data = dbWorkouts.getWorkoutListByCategory(mSelectedID);
            for (int i = 0; i < data.size(); i++) {
                ArrayList<Object> row = data.get(i);
                ListId.add(row.get(0).toString());
                ListName.add(row.get(1).toString());
                ListImage.add(row.get(2).toString());
                ListTime.add(row.get(3).toString());
            }
            dbWorkouts.close();
        } else {

            // Check database
            DBHelperPrograms dbPrograms = new DBHelperPrograms(getActivity());
            dbPrograms.checkDBPrograms();
            data = dbPrograms.getWorkoutListByDay(mSelectedID);
            for (int i = 0; i < data.size(); i++) {
                ArrayList<Object> row = data.get(i);
                ListId.add(row.get(0).toString());
                ListName.add(row.get(2).toString());
                ListImage.add(row.get(3).toString());
                ListTime.add(row.get(4).toString());
            }
            dbPrograms.close();
        }

    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnStartAll:
                mCallbackClick.onClick(ListId, ListName, ListTime);
                break;

            default:
                break;

        }

    }

}