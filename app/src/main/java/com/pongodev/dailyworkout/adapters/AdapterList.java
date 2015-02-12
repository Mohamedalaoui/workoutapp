/*
* Copyright (c) 2015 Pongodev. All Rights Reserved.
*/
package com.pongodev.dailyworkout.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.pongodev.dailyworkout.R;
import com.pongodev.dailyworkout.listeners.OnTapListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class AdapterList extends RecyclerView.Adapter<AdapterList.ViewHolder>
{
    private final ArrayList<String> ListId;
    private final ArrayList<String> ListName;
    private final ArrayList<String> ListTime;
    private final ArrayList<String> ListImage;

    private OnTapListener onTapListener;

    private Context mContext;

    public AdapterList(Context context)
    {

        this.ListId = new ArrayList<>();
        this.ListName = new ArrayList<>();
        this.ListTime = new ArrayList<>();
        this.ListImage = new ArrayList<>();

        mContext = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_list, null);

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
                    onTapListener.onTapView(ListId.get(position),ListName.get(position));
            }
        });

        // set data to text view
        viewHolder.txtTitle.setText(ListName.get(position));
        viewHolder.txtTime.setText(ListTime.get(position));

        // set data to image view
        int image = mContext.getResources().getIdentifier(ListImage.get(position), "drawable", mContext.getPackageName());
        Picasso.with(mContext)
                .load(image)
                .into(viewHolder.imgThumbnail);
    }

    @Override
    public int getItemCount()
    {
        return ListId.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private RoundedImageView imgThumbnail;
        private TextView txtTitle, txtTime;

        public ViewHolder(View v)
        {
            super(v);
            // connect views object with views id on xml
            imgThumbnail= (RoundedImageView) v.findViewById(R.id.imgThumbnail);
            txtTitle    = (TextView) v.findViewById(R.id.txtTitle);
            txtTime     = (TextView) v.findViewById(R.id.txtSubTitle);
        }
    }

    public void updateList(
            ArrayList<String> ListWorkoutId,
            ArrayList<String> ListName,
            ArrayList<String> ListImage,
            ArrayList<String> ListTime)
    {
        this.ListId.clear();
        this.ListId.addAll(ListWorkoutId);

        this.ListName.clear();
        this.ListName.addAll(ListName);

        this.ListTime.clear();
        this.ListTime.addAll(ListTime);

        this.ListImage.clear();
        this.ListImage.addAll(ListImage);

        this.notifyDataSetChanged();
    }

    public void setOnTapListener(OnTapListener onTapListener)
    {
        this.onTapListener = onTapListener;
    }
}