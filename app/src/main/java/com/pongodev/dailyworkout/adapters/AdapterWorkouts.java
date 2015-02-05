/*
* Copyright (c) 2015 Pongodev. All Rights Reserved.
*/
package com.pongodev.dailyworkout.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pongodev.dailyworkout.R;
import com.pongodev.dailyworkout.listeners.OnTapListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class AdapterWorkouts extends RecyclerView.Adapter<AdapterWorkouts.ViewHolder>
{
    private final ArrayList<String> workoutIds;
    private final ArrayList<String> workoutNames;
    private final ArrayList<String> workoutTotals;
    private final ArrayList<String> workoutImages;

    private OnTapListener onTapListener;

    private Context mContext;

    public AdapterWorkouts(Context context)
    {
        this.workoutIds = new ArrayList<>();
        this.workoutNames = new ArrayList<>();
        this.workoutTotals = new ArrayList<>();
        this.workoutImages = new ArrayList<>();

        mContext = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_workouts, null);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position)
    {
        viewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (onTapListener != null)
                    onTapListener.onTapView(workoutIds.get(position), workoutNames.get(position));
            }
        });

        // set data to text view
        viewHolder.txtTitle.setText(workoutNames.get(position));
        int count = Integer.parseInt(workoutTotals.get(position));

        // if workout is more than one then add 's
        if(count > 1){
            viewHolder.txtTotal.setText(count+" "+mContext.getResources().getString(R.string.workouts));
        }else{
            viewHolder.txtTotal.setText(count+" "+mContext.getResources().getString(R.string.workout));
        }

        // set data to image view
        int image = mContext.getResources().getIdentifier(workoutImages.get(position), "drawable", mContext.getPackageName());

        Picasso.with(mContext)
                .load(image)
                .into(viewHolder.imgCategory);

    }

    @Override
    public int getItemCount()
    {
        return workoutIds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView imgCategory;
        TextView txtTitle, txtTotal;

        public ViewHolder(View v)
        {
            super(v);
            // connect views object with views id on xml
            imgCategory = (ImageView) v.findViewById(R.id.imgCategory);
            txtTitle = (TextView) v.findViewById(R.id.txtTitle);
            txtTotal = (TextView) v.findViewById(R.id.txtTotal);
        }
    }

    public void updateList(
            ArrayList<String> workoutIds,
            ArrayList<String> workoutNames,
            ArrayList<String> workoutImages,
            ArrayList<String> workoutTotals)
    {
        this.workoutIds.clear();
        this.workoutIds.addAll(workoutIds);

        this.workoutNames.clear();
        this.workoutNames.addAll(workoutNames);

        this.workoutTotals.clear();
        this.workoutTotals.addAll(workoutTotals);

        this.workoutImages.clear();
        this.workoutImages.addAll(workoutImages);

        this.notifyDataSetChanged();
    }

    public void setOnTapListener(OnTapListener onTapListener)
    {
        this.onTapListener = onTapListener;
    }
}