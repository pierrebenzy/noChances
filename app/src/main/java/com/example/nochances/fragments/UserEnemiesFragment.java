package com.example.nochances.fragments;

import android.support.v4.app.Fragment;
import android.graphics.Color;
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
import com.example.nochances.Model.userAlarmLevel;

import java.util.ArrayList;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

public class UserEnemiesFragment extends Fragment implements listAdapter.ItemClickListener,
        SearchView.OnQueryTextListener {
    private static final String TAG = com.example.nochances.ListActivity.class.getSimpleName() ;
    private listAdapter adapter;
    private List<userAlarmLevel> alarmLevels;// list of name to be deiplayed
    private int defaultColor;
    SearchView searchView;


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
        alarmLevels.add(new userAlarmLevel("Pierre", Color.RED));
        alarmLevels.add(new userAlarmLevel("Themis",Color.BLUE));
        alarmLevels.add(new userAlarmLevel("Andrew",Color.BLACK));

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
        defaultColor=adapter.getItem(position).getColor();// updating default color
        Log.d(TAG,"defaultColor "+defaultColor);
        openColorPicker(position);//creates a dialog to allow user to choose color
    }


    /**
     * dialog o choose color
     * @param position of row selected
     */
    private void openColorPicker(final int position) {
        AmbilWarnaDialog colorPicker= new AmbilWarnaDialog(getContext(), defaultColor ,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        defaultColor=color;
                        adapter.getItem(position).setColor(color);
                        adapter.notifyDataSetChanged();
                    }
                });
        colorPicker.show();
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
}
