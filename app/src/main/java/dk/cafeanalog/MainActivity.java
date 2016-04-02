/*
 * Copyright 2016 Analog IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dk.cafeanalog;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CafeAnalogAdapter.OnItemClickListener {
    private static final String IS_OPEN_FRAGMENT = "dk.cafeanalog.MainActivity.IS_OPEN_FRAGMENT",
                                OPENING_FRAGMENT = "dk.cafeanalog.MainActivity.OPENING_FRAGMENT";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private boolean mVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mVisible = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() == null) {
            throw new RuntimeException("Remember to use a theme with a toolbar!");
        }

        String[] menuItems = getResources().getStringArray(R.array.analog_menu_items);

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        RecyclerView mDrawerList = (RecyclerView) findViewById(R.id.left_drawer);

        if (mDrawerList == null) {
            throw new RuntimeException("Stop complaining Android Studio!");
        }

        mDrawerList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // improve performance by indicating the list is fixed size.
        mDrawerList.setHasFixedSize(true);

        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new CafeAnalogAdapter(menuItems, this));
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            // Only if the application is fresh. Otherwise, keep last state of fragment manager.
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_layout, new IsOpenFragment(), IS_OPEN_FRAGMENT)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVisible = true;
    }

    @Override
    protected void onPause() {
        mVisible = false;
        super.onPause();
    }

    private void getOpenings(final Action<List<DayOfOpenings>> resultFunction) {
        new AsyncTask<Void, Void, List<DayOfOpenings>>() {
            @Override
            protected List<DayOfOpenings> doInBackground(Void... params) {
                try {
                    AnalogDownloader downloader = new AnalogDownloader();

                    return downloader.getDaysOfOpenings(false);
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
                return new ArrayList<>();
            }

            @Override
            protected void onPostExecute(List<DayOfOpenings> openings) {
                resultFunction.run(openings);
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }
//
//    /* Called whenever we call invalidateOptionsMenu() */
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        // If the nav drawer is open, hide action items related to the content view
//        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
//        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
//        return super.onPrepareOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch (item.getItemId()) {
            /*case R.id.action_websearch:
                // create intent to perform web search for this planet
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
                // catch event that there's no activity to handle intent
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
                }
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(int position) {
        switch (position) {
            case 0: // Front page
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_layout, new IsOpenFragment())
                        .commit();
                mDrawerLayout.closeDrawers();
                break;
            case 1: // Opening Hours
                getOpenings(
                        new Action<List<DayOfOpenings>>() {
                            @Override
                            public void run(List<DayOfOpenings> openings) {
                                if (mVisible) {
                                    getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.main_layout, OpeningsFragment.newInstance(openings), OPENING_FRAGMENT)
                                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                            .addToBackStack(null)
                                            .commit();
                                    mDrawerLayout.closeDrawers();
                                }
                            }
                        }
                );
                break;
        }
    }
}
