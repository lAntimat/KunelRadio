package com.kunel.lantimat.kunelradio;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.kunel.lantimat.kunelradio.fragments.CoolRadioFragment;
import com.kunel.lantimat.kunelradio.fragments.InstagramFragment;
import com.kunel.lantimat.kunelradio.fragments.FeedFragment;
import com.kunel.lantimat.kunelradio.fragments.RadioFragment;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "MainActivity";
    static final int PAGE_COUNT = 2;

    NonSwipeableViewPager pager;
    PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar!=null) {
            setSupportActionBar(toolbar);
        }
        setupBottomBar();
        initPagerAdapter();


    }

    private void initPagerAdapter() {

        //Инициализация ViewPager
        pager = (NonSwipeableViewPager) findViewById(R.id.pager);
        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setPagingEnabled(false);
        pager.setOffscreenPageLimit(2);
    }


    private void setupBottomBar() { //Инициализация нижнего бара
        AHBottomNavigation bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);


        //Создаем айтемы
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab_1, R.drawable.ic_access_point_white_24dp, R.color.colorBottomNavigationPrimary);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab_2, R.drawable.ic_newspaper_white_24dp, R.color.colorBottomNavigationPrimary);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab_3, R.drawable.ic_instagram_white_24dp, R.color.colorBottomNavigationPrimary);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.tab_4, R.drawable.ic_dots_horizontal_white_24dp, R.color.colorBottomNavigationPrimary);

        //Добавляем в бар
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        //bottomNavigation.addItem(item3);
        //bottomNavigation.addItem(item4);

        // Manage titles
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

        //bottomNavigation.setAccentColor(ContextCompat.getColor(this, R.color.md_blue_900));

        // Set background color
        //bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#FEFEFE"));

        // Add or remove notification for each item
        //bottomNavigation.setNotification("1", 2);



        // Set listeners
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                //Toast.makeText(ctx, position + " tab clicked", Toast.LENGTH_SHORT).show();
                /*Fragment fragment = null;

                // на основании выбранного элемента меню
                // вызываем соответственный ему фрагмент
                switch (position) {
                    case 0:
                        //fragment = new RadioFragment();
                        break;
                    case 1:
                        //fragment = new FeedFragment();
                        break;
                    case 2:
                        //fragment = new InstagramFragment();
                        break;
                    case 3:
                        //fragment = new FeedFragment();
                        break;
                    default:
                        break;
                }

                if(fragment != null) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content, fragment, fragment.getTag()).commit();
                }*/

                pager.setCurrentItem(position);


                return true;
            }
        });
        bottomNavigation.setOnNavigationPositionListener(new AHBottomNavigation.OnNavigationPositionListener() {
            @Override public void onPositionChange(int y) {
                // Manage the new y position
            }
        });

        //Устанавливаем текущий элемент
        bottomNavigation.setCurrentItem(0,false);

    }


    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new CoolRadioFragment();
                case 1:
                    return new FeedFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
