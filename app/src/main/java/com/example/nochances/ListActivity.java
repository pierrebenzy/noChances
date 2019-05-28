package com.example.nochances;


import android.os.Bundle;

import com.example.nochances.adapter.ListActivityPagerAdapter;
import com.example.nochances.fragments.*;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;



import java.util.ArrayList;
import java.util.Objects;


public class ListActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private BottomNavigationView mbottomNavigationItemView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //initialize fragments
        ArrayList<Fragment> fragments;
        ListActivityPagerAdapter viewPagerAdapter;
        setTitle("MainActivity");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        fragments = new ArrayList<>();
        fragments.add(new UserEnemiesFragment());
        fragments.add(new AllUserFragment());

        viewPager = findViewById(R.id.viewpager);
        viewPagerAdapter = new ListActivityPagerAdapter(getSupportFragmentManager(), fragments);
        //making sure the right fragment is shown when a bottom navigation is used
        mbottomNavigationItemView=findViewById(R.id.navigation);
        mbottomNavigationItemView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.user_list:
                        viewPager.setCurrentItem(0);
                        mbottomNavigationItemView.getMenu().getItem(0).setChecked(true);
                        break;
                    case R.id.complete_list:
                        viewPager.setCurrentItem(1);
                        mbottomNavigationItemView.getMenu().getItem(1).setChecked(true);
                        break;
                }

                return false;
            }
        });


        viewPager.setAdapter(viewPagerAdapter);
        //a listener to make sure the right bottom Navigation is cliced when user swipe
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
              //  Toast.makeText(HomeActivity.this,
                //        "Selected page position: " + position, Toast.LENGTH_SHORT).show();
            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
                mbottomNavigationItemView.getMenu().getItem(position).setChecked(true);
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });
    }
/*
    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.main_activity_menu,menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      int id= item.getItemId();
      if(id==android.R.id.home){
          finish();
          return true;
      }
        return super.onOptionsItemSelected(item);
    }
}
