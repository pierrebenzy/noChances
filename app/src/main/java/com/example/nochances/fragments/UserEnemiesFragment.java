package com.example.nochances.fragments;

import android.support.v4.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.view.ViewGroup;

import com.example.nochances.R;
import com.example.nochances.adapter.listAdapter;
import com.example.nochances.Model.enemiesAlarmLevel;
import com.example.nochances.utils.constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

import static com.example.nochances.EnemiesProfileActivity.GREEN;
import static com.example.nochances.EnemiesProfileActivity.enemiesEmailIntent;

public class UserEnemiesFragment extends Fragment implements listAdapter.ItemClickListener,
        SearchView.OnQueryTextListener {
    private static final String TAG = UserEnemiesFragment.class.getSimpleName() ;
    private listAdapter adapter;
    private List<enemiesAlarmLevel> alarmLevels;// list of name to be deiplayed
    private int defaultColor;
    SearchView searchView;
    private DatabaseReference database;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("list of enemies");
        return inflater.inflate(R.layout.enemies_rv_list, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)   {

        alarmLevels=new ArrayList<>();

        database= FirebaseDatabase.getInstance().getReference();
        RecyclerView recyclerView=view.findViewById(R.id.rvUser);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter= new listAdapter(getContext(), alarmLevels);
        adapter.setClickListener(this);

        //putting dividers
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                new LinearLayoutManager(getContext()).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(adapter);


    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    /**
     *
     * @param view the current  view
     * @param position where the user clicked in the recycle view
     */
    @Override
    public void onItemClick(View view, int position) {
        Log.d(TAG,"onItemClick position"+ position);
        startActivity(enemiesEmailIntent(getContext(), UserEnemiesFragment.class.getSimpleName()
                +","+alarmLevels.get(position).getEmail()+","+alarmLevels.get(position).getName()));
    }



    /**
     * inflating menu
     * @param menu to be shown
     * @param inflater to inflate menu
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu,menu);
        //find searchview in menu
        MenuItem searchItem = menu.findItem(R.id.search);
        //actionView returns a view. need to have app:actionViewClass="android.support.v7.widget.SearchView"
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);//setting a listener when the user writes something

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //send an data that tells register that the user is signed in

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //filtering the adapter
        adapter.getFilter().filter(newText);
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        populateList();
    }

    private void populateList(){
        // clear everytime resume is called because we are going to read everything again
        alarmLevels.clear();
        String email=FirebaseAuth.getInstance().getCurrentUser().getEmail();
        database.child("users_"+ constant.md5(email)).child("enemies_list").
                addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //read every children
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    /*create a "fake enenemy that is deleted so if the user has no enemies (dataSnapshot1=null)
                      then the enemy doesn't get added
                     */
                    enemiesAlarmLevel userAlarmLevel=new enemiesAlarmLevel("name",GREEN,"email",true);
                    if(dataSnapshot1!=null)
                        userAlarmLevel = dataSnapshot1.getValue(enemiesAlarmLevel.class);
                if(!userAlarmLevel.isDeleted()) alarmLevels.add(userAlarmLevel);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG,databaseError.getMessage());
            }
        });

    }

}
