package com.example.nochances.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
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
import android.widget.Filter;

import com.example.nochances.Model.Profile;
import com.example.nochances.Model.enemiesAlarmLevel;
import com.example.nochances.R;
import com.example.nochances.adapter.AllUserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.nochances.EnemiesProfileActivity.GREEN;
import static com.example.nochances.EnemiesProfileActivity.enemiesEmailIntent;

// implements a custom ItemClickListener to make easy to implement whatever we want to do when a row is clicked
public class AllUserFragment extends Fragment implements AllUserAdapter.ItemClickListener,
        SearchView.OnQueryTextListener{
    private static final String TAG = AllUserFragment.class.getSimpleName() ;
    private AllUserAdapter adapter;
   // list of name
    SearchView searchView;// searchView
    LoaderManager mLoader;
    DatabaseReference database;
    private List<enemiesAlarmLevel> Data;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoader = Objects.requireNonNull(getActivity()).getSupportLoaderManager();
        getActivity().setTitle("list of enemies");
        database= FirebaseDatabase.getInstance().getReference();
        return inflater.inflate(R.layout.enemies_rv_list, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)   {


        Data=new ArrayList<>();

        RecyclerView recyclerView=view.findViewById(R.id.rvUser);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter= new AllUserAdapter(getContext(), Data);
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

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume()");
        updateDatabse();

    }
    /**
     *
     * @param view the current  view
     * @param position where the user clicked in the recycle view
     */
    @Override
    public void onItemClick(View view, int position) {
        Log.d(TAG,"onItemClick position"+ position);

        startActivity(enemiesEmailIntent(getContext(), AllUserFragment.class.getSimpleName()
                +","+adapter.getItem(position).getEmail()+","+adapter.getItem(position).getName()+","+GREEN));
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
    private void updateDatabse(){
        Data.clear();
        database.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterator<DataSnapshot> allUsers=dataSnapshot.getChildren().iterator();
                Log.d(TAG," 1 UserLoader "+dataSnapshot.getValue());
                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    Log.d(TAG,"UserLoader "+dataSnapshot1.child("profile").getKey());
                    Log.d(TAG,"UserLoader "+dataSnapshot1.child("profile").getValue());

                    Profile profile=dataSnapshot1.child("profile").getValue(Profile.class);
                    Log.d(TAG,"profile "+profile.getEmail());
                    if(!profile.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                        Log.d(TAG,"I am not the current user: "+profile.getEmail());
                        Data.add(new enemiesAlarmLevel(profile.getName(), GREEN, profile.getEmail()));
                    }
                    Log.d(TAG,"list ");
                }
                // list populated
                // we received info for all users
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG,databaseError.getMessage());
            }
        });

    }



}
