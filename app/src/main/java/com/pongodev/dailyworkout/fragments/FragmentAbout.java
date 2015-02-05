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

import com.pongodev.dailyworkout.R;
import com.pongodev.dailyworkout.activities.ActivityAbout;
import com.pongodev.dailyworkout.adapters.AdapterAbout;
import com.pongodev.dailyworkout.listeners.OnTapAboutListener;

import java.util.ArrayList;

public class FragmentAbout extends Fragment {

    // Create objects of views.
    RecyclerView recyclerView;
    AdapterAbout adapterAbout;
    OnItemSelectedListener mCallback;

    // Create arraylist variables to store data.
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> summaries = new ArrayList<>();

    // Create interface listener.
    public interface OnItemSelectedListener {
        public void onItemSelected(int position);
    }

    public FragmentAbout() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        setRetainInstance(true);

        // Connect view objects and view id on xml.
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // Get data with asynctask.
        new syncGetData().execute();

        adapterAbout = new AdapterAbout();

        adapterAbout.setOnTapAboutListener(new OnTapAboutListener() {
            @Override
            public void onTapView(int position) {
                mCallback.onItemSelected(position);
            }
        });

        return rootView;
    }

    public class syncGetData extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getDataFromResources();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            adapterAbout.updateList(titles, summaries);
            recyclerView.setAdapter(adapterAbout);
        }
    }

    // Get data from string resources.
    public void getDataFromResources(){
        for(int i = 0;i < ActivityAbout.titles.length;i++){
            titles.add(ActivityAbout.titles[i]);
            summaries.add(ActivityAbout.summaries[i]);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnItemSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnRecipeSelectedListener");
        }
    }

}
