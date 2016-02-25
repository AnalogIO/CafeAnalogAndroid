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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BleNotAvailableException;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements IsOpenFragment.ShowOpening, BeaconConsumer {
    private static final String IS_OPEN_FRAGMENT = "dk.cafeanalog.MainActivity.IS_OPEN_FRAGMENT",
                                OPENING_FRAGMENT = "dk.cafeanalog.MainActivity.OPENING_FRAGMENT";

    private boolean mVisible;

    //Beacon fields
    final Region region = new Region("ITU", Identifier.fromUuid(UUID.fromString("E3B54450-AB73-4D79-85D6-519EAF0F45D9")), null, null);
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private BeaconManager mBeaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mVisible = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isDualPane = findViewById(R.id.opening_layout) != null;

        Log.d("MainActivity", "DualPane: " + isDualPane);

        if (savedInstanceState != null) {
            getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_layout, new IsOpenFragment(), IS_OPEN_FRAGMENT)
                .commit();

        if (isDualPane) {
            getOpenings(
                    new Action<List<Opening>>() {
                        @Override
                        public void run(List<Opening> openings) {
                            if (mVisible) {
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.opening_layout, OpeningFragment.newInstance(openings), OPENING_FRAGMENT)
                                        .commit();
                            }
                        }
                    }
            );
        }

        //beacon init
        mBeaconManager = BeaconManager.getInstanceForApplication(this);

        try {
            if (mBeaconManager.checkAvailability()) {
                mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
                mBeaconManager.bind(this);
            }
            //Marshmallow needs the coarse location to access beacons... Damn war-driving..
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("This app needs location access");
                    builder.setMessage("Please grant location access so this app can detect beacons.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @SuppressLint("NewApi")
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        }
                    });
                    builder.show();
                }
            }
        } catch (BleNotAvailableException ignore) {}
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

    @Override
    protected void onDestroy(){
        mBeaconManager.unbind(this);
        super.onDestroy();
    }

    private void getOpenings(final Action<List<Opening>> resultFunction) {
        new AsyncTask<Void, Void, List<Opening>>() {
            @Override
            protected List<Opening> doInBackground(Void... params) {
                try {
                    AnalogDownloader downloader = new AnalogDownloader();

                    return downloader.getOpenings();
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
                return new ArrayList<>();
            }

            @Override
            protected void onPostExecute(List<Opening> openings) {
                resultFunction.run(openings);
            }
        }.execute();
    }

    @Override
    public void showOpening() {
        getOpenings(
                new Action<List<Opening>>() {
                    @Override
                    public void run(List<Opening> openings) {
                        if (mVisible) {
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.main_layout, OpeningFragment.newInstance(openings), OPENING_FRAGMENT)
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    }
                }
        );
    }

    @Override
    public void onBeaconServiceConnect() {
        final AnalogDownloader downloader = new AnalogDownloader();

        mBeaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                if (downloader.isOpen() == AnalogDownloader.AnalogStatus.OPEN) {
                    showNotification(getString(R.string.notification_beacon_open_title), getString(R.string.notification_beacon_open_text));
                }
            }

            @Override
            public void didExitRegion(Region region) {
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
            }
        });

        try {
            mBeaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException ignored) { }
    }

    public void showNotification(String title, String message) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            Intent notifyIntent = new Intent(this, MainActivity.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build();
            notification.defaults |= Notification.DEFAULT_SOUND;
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, notification);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }
                        });
                    }
                    builder.show();
                }
            }
        }
    }

}
