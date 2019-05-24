package com.example.nochances.adapter;


import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.nochances.R;
import com.example.nochances.Model.userAlarmLevel;

import java.util.ArrayList;
import java.util.List;

public class AllUserAdapter extends RecyclerView.Adapter<AllUserAdapter.ViewHolder> implements Filterable {
    private List<userAlarmLevel> mData;//full Data
    private  List<userAlarmLevel> mDisplayedData;//Data to be displayed
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    public AllUserAdapter(Context context,List<userAlarmLevel> data){
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mDisplayedData=data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.all_users_row,parent,false);
        return new ViewHolder(view);
    }

    /**
     *
     * @param holder to each widgets in the layout
     * @param position of the row item
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name=mDisplayedData.get(position).getName();
        holder.userName.setText(name);

    }
    /**
     * the size of data to be displayed
     */
    @Override
    public int getItemCount() {
        return mDisplayedData.size();
    }

    /**
     *
     * @param id of the item
     * @return the item
     */
    public userAlarmLevel getItem(int id) {
        return mDisplayedData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    /**
     *
     * @return the results of the filter
     */
    @Override
    public Filter getFilter() {
        Filter filter=new Filter(){

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                List<userAlarmLevel> FilteredArrList = new ArrayList<>();
                if (mData == null) {
                    mData = new ArrayList<>(mDisplayedData); // saves the original data in mOriginalValues
                }
                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (charSequence== null || charSequence.length() == 0) {

                    // set the Original result to return
                    results.count = mData.size();
                    results.values = mData;
                }
                else {
                    charSequence = charSequence.toString().toLowerCase();
                    for (int i = 0; i < mData.size(); i++) {
                        String data = mData.get(i).getName();
                        if (data.toLowerCase().startsWith(charSequence.toString())) {
                            FilteredArrList.add(new userAlarmLevel(
                                    mData.get(i).getName(),mData.get(i).getColor()));
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;

            }
            // getting the results to put them in the list to be displayed
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mDisplayedData=(List<userAlarmLevel>)filterResults.values;
                notifyDataSetChanged();
            }
        };
        return filter;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView userName;


        public ViewHolder(View itemView) {
            super(itemView);
            userName= itemView.findViewById(R.id.user_name);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
