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

import static com.example.nochances.EnemiesProfileActivity.BLUE;
import static com.example.nochances.EnemiesProfileActivity.GREEN;
import static com.example.nochances.EnemiesProfileActivity.ORANGE;
import static com.example.nochances.EnemiesProfileActivity.RED;
import static com.example.nochances.EnemiesProfileActivity.YELLOW;

public class listAdapter extends RecyclerView.Adapter<listAdapter.ViewHolder> implements Filterable {

    /**
     * Tag the name of the class
     * mData data containning enemy
     * mInflater inflate View
     * Click Listener listens for a row clik
     */
    private static final String TAG = listAdapter.class.getSimpleName();
    private List<enemiesAlarmLevel> mData;
    private  List<enemiesAlarmLevel> mDisplayedData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;



    public listAdapter(Context context,List<enemiesAlarmLevel> data){
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mDisplayedData=data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.enemies_row,parent,false);
        return new ViewHolder(view);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        //get name
        String name="Enemy: "+mDisplayedData.get(position).getName();
        holder.userName.setText(name);
        String threat="Annoying";
        //colors
        int color=R.color.green;
        switch (mData.get(position).getColor()) {
            case GREEN:
                color = R.color.green;
                break;
            case BLUE:
                color = R.color.blue;
                threat="Might tolerate on a good day";
                break;

            case YELLOW:
                color = R.color.yellow;
                threat="Jerk";
                break;
            case ORANGE:
                color = R.color.orange;
                threat="doucheBag";
                break;
            case RED:
                color = R.color.red;
                threat="Asshole";
                break;
        }
        holder.alarmLevel.setText(threat);
        holder.alarmLevel.setBackgroundResource(color);
        loadSnap(holder,mData.get(position).getEmail());
    }

    @Override
    public int getItemCount() {
        return mDisplayedData.size();
    }
    public enemiesAlarmLevel getItem(int id) {
        return mDisplayedData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    /**
     *
     * @param holder of the widgets views
     * @param enemyEmail the email of the enemy to go get the  picture
     */
    private void loadSnap(final ViewHolder holder ,String enemyEmail) {
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
               Bitmap bitmap1= scaleDown(bitmap,200,true);
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


    /**
     *
     * @return filter of name
     */
    @Override
    public Filter getFilter() {

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
                if (charSequence== null || charSequence.length() == 0) {

                    // set the Original result to return
                    results.count = mData.size();
                    results.values = mData;
                } else {
                    charSequence = charSequence.toString().toLowerCase();
                    for (int i = 0; i < mData.size(); i++) {
                        String data = mData.get(i).getName();
                        if (data.toLowerCase().startsWith(charSequence.toString())) {
                            FilteredArrList.add(new enemiesAlarmLevel(
                                    mData.get(i).getName(),mData.get(i).getColor(),mData.get(i).getEmail()));
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
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

    /**
     * class that holds the vwidgets
     */

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView userName;
        TextView alarmLevel;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            alarmLevel = itemView.findViewById(R.id.color_textView);
            imageView = itemView.findViewById(R.id.image);
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
     * Scale image size to image
     * @param realImage the bitMap of the image
     * @param maxImageSize: the image size that you want
     * @param filter a boolean
     * @return a resized bitmap
     */
    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                 maxImageSize / realImage.getWidth(),
               maxImageSize / realImage.getHeight());
        int width = Math.round( ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());

        return Bitmap.createScaledBitmap(realImage, width,
                height, filter);
    }

}
