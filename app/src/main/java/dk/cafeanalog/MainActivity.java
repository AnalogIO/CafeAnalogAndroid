package dk.cafeanalog;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.jsoup.nodes.Document;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IsOpenFragment.ShowOpening {
    private static final String IS_OPEN_FRAGMENT = "dk.cafeanalog.MainActivity.IS_OPEN_FRAGMENT",
                                OPENING_FRAGMENT = "dk.cafeanalog.MainActivity.OPENING_FRAGMENT";
    private AsyncTask<Void, Void, ArrayList<OpeningParser.Opening>> openingsTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
            openingsTask = new AsyncTask<Void, Void, ArrayList<OpeningParser.Opening>>() {
                @Override
                protected ArrayList<OpeningParser.Opening> doInBackground(Void... params) {
                    try {
                        AnalogDownloader downloader = new AnalogDownloader(getApplicationContext());
                        Document page = downloader.downloadPage();

                        Iterable<OpeningParser.Opening> openings = downloader.getOpenings(page);

                        ArrayList<OpeningParser.Opening> opens = new ArrayList<>();
                        for (OpeningParser.Opening opening : openings) { opens.add(opening); }
                        return opens;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(ArrayList<OpeningParser.Opening> openings) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.opening_layout, OpeningFragment.newInstance(openings), OPENING_FRAGMENT)
                            .commit();
                }
            }.execute();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (openingsTask != null) {
            openingsTask.cancel(true);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void showOpening(ArrayList<OpeningParser.Opening> openings) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_layout, OpeningFragment.newInstance(openings), OPENING_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }
}
