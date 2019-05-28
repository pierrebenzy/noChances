package com.example.nochances.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nochances.Model.enemiesAlarmLevel;
import com.example.nochances.R;
import com.example.nochances.utils.constant;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import static android.support.constraint.Constraints.TAG;
import static com.example.nochances.adapter.listAdapter.scaleDown;

public class AllUserAdapter extends RecyclerView.Adapter<AllUserAdapter.ViewHolder> implements Filterable {
    /**
     * Tag the name of the class
     * mData data containing enemy
     * mInflater inflate View
     * Click Listener listens for a row clik
     */
    private List<enemiesAlarmLevel> mData;//full Data
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private  List<enemiesAlarmLevel> mDisplayedData;


    public AllUserAdapter(Context context,List<enemiesAlarmLevel> data){
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        mDisplayedData=new ArrayList<>();
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
        String name="name: "+mDisplayedData.get(position).getName();
        holder.userName.setText(name);
        loadSnap(holder,mDisplayedData.get(position).getEmail());
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
    public enemiesAlarmLevel getItem(int id) {
        return mDisplayedData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    /**
     * loads the picture
     * @param holder that holds the view
     * @param enemyEmail of the enemy
     */
    private void loadSnap(final AllUserAdapter.ViewHolder holder , String enemyEmail) {
        //
        //take storage reference
        StorageReference storage= FirebaseStorage.getInstance().getReference();

        final long ONE_MEGABYTE = 1024 * 1024 * 10;
        StorageReference fileReference = storage.child("uploads").child(constant.md5(enemyEmail) + ".jpg");

        fileReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                Log.d(TAG, "LoadSnap: success!");
                //get bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                //put it in the image
                Bitmap bitmap1= scaleDown(bitmap,350,true);
                holder.imageView.setImageBitmap(bitmap1);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d(TAG, "LoadSnap: Failure");
                holder.imageView.setImageResource(R.drawable.ic_launcher_round);
            }
        });
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView userName;
        ImageView imageView;


        public ViewHolder(View itemView) {
            super(itemView);
            userName= itemView.findViewById(R.id.user_name);
            imageView =itemView.findViewById(R.id.image);
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

    /**
     *
     * @return filter of name
     */
    @Override
    public Filter getFilter() {
            mDisplayedData=new ArrayList<>();
        return new Filter(){

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                List<enemiesAlarmLevel> FilteredArrList = new ArrayList<>();
                if (mData == null) {
                    mData = new ArrayList<>(mDisplayedData); // saves the original data in mOriginalValues
                }
                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                /*if (charSequence== null || charSequence.length() == 0) {

                    // set the Original result to return
                    results.count = mData.size();
                    results.values = mData;
                } */
                    charSequence = charSequence.toString().toLowerCase();
                    for (int i = 0; i < mData.size(); i++) {
                        String data = mData.get(i).getName();
                        if (data.toLowerCase().equals(charSequence.toString())) {
                            FilteredArrList.add(new enemiesAlarmLevel(
                                    mData.get(i).getName(),mData.get(i).getColor(),mData.get(i).getEmail()));
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;

                return results;

            }
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mDisplayedData=(List<enemiesAlarmLevel>)filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
