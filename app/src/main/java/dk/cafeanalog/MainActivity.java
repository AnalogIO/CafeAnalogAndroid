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

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IsOpenFragment.ShowOpening {
    private static final String IS_OPEN_FRAGMENT = "dk.cafeanalog.MainActivity.IS_OPEN_FRAGMENT",
                                OPENING_FRAGMENT = "dk.cafeanalog.MainActivity.OPENING_FRAGMENT";

    private boolean mVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mVisible = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

                    return downloader.getDaysOfOpenings();
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
    public void showOpening() {
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
                        }
                    }
                }
        );
    }
}
