package com.example.nochances.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;

public class ListActivityPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = ListActivityPagerAdapter.class.getSimpleName();
    private ArrayList<Fragment> fragments;
    public ListActivityPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        this.fragments=fragments;
    }
    public Fragment getItem(int pos){
        Log.d(TAG, "getItem " + "position" + pos);
        return fragments.get(pos);
    }
    // Return the number of views available
    public int getCount(){
        Log.d(TAG, "getCount " + "size " + fragments.size());
        return fragments.size();
    }


}