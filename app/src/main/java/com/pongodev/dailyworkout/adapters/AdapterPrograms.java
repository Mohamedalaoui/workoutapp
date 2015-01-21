package com.pongodev.dailyworkout.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pongodev.dailyworkout.R;
import com.pongodev.dailyworkout.utils.OnTapListener;

import java.util.ArrayList;

public class AdapterPrograms extends RecyclerView.Adapter<AdapterPrograms.ViewHolder>
{
    private final ArrayList<String> programDayIds;
    private final ArrayList<String> programDayNames;
    private final ArrayList<String> programTotals;

    private OnTapListener onTapListener;

    private Context mContext;

    public AdapterPrograms(Context context)
    {
        this.programDayIds = new ArrayList<String>();
        this.programDayNames = new ArrayList<String>();
        this.programTotals = new ArrayList<String>();

        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_programs, null);

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
                    onTapListener.onTapView(programDayIds.get(position), programDayNames.get(position));
            }
        });

        // set data to text view
        viewHolder.txtTitle.setText(programDayNames.get(position));

        // if day is Sunday and Saturday change color to red
        String id = programDayIds.get(position);
        if((id.equals("0")) || id.equals("6")){
            viewHolder.txtTitle.setTextColor(mContext.getResources().getColor(R.color.title_holiday_color));
        }

        int count = Integer.parseInt(programTotals.get(position));

        // if workout is more than one then add 's
        if(count > 1){
            viewHolder.txtTotal.setText(count+" "+mContext.getResources().getString(R.string.workouts));
        }else{
            viewHolder.txtTotal.setText(count+" "+mContext.getResources().getString(R.string.workout));
        }
    }

    @Override
    public int getItemCount()
    {
        return programDayIds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView txtTitle, txtTotal;
        public ViewHolder(View v)
        {
            super(v);
            // connect views object with views id on xml
            txtTitle = (TextView) v.findViewById(R.id.txtTitle);
            txtTotal = (TextView) v.findViewById(R.id.txtTotal);
        }
    }

    public void updateList(
            ArrayList<String> programDayIds,
            ArrayList<String> programDayNames,
            ArrayList<String> programTotals)
    {
        this.programDayIds.clear();
        this.programDayIds.addAll(programDayIds);

        this.programDayNames.clear();
        this.programDayNames.addAll(programDayNames);

        this.programTotals.clear();
        this.programTotals.addAll(programTotals);

        this.notifyDataSetChanged();
    }

    public void setOnTapListener(OnTapListener onTapListener)
    {
        this.onTapListener = onTapListener;
    }
}