package dk.cafeanalog;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextSwitcher;

import org.jsoup.nodes.Document;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class IsOpenFragment extends Fragment {
    private TextSwitcher view;
    private AnalogActivityTask task;

    public IsOpenFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_is_open, container, false);

        view = (TextSwitcher) v.findViewById(R.id.text_view);
        view.setFactory(new TextSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                AppCompatTextView textView = new AppCompatTextView(v.getContext());
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                return textView;
            }
        });
        view.setCurrentText(getResources().getText(R.string.is_open_analog));
        view.setInAnimation(v.getContext(), android.R.anim.slide_in_left);
        view.setOutAnimation(v.getContext(), android.R.anim.slide_out_right);

        v.findViewById(R.id.fragment_main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatTextView tv = (AppCompatTextView) view.getNextView();

                if (task == null || task.getStatus() == AsyncTask.Status.FINISHED) {
                    tv.setTextColor(ContextCompat.getColor(v.getContext(), android.R.color.primary_text_dark));
                    view.setText(getString(R.string.refreshing_analog));
                    task = new AnalogActivityTask(view, 300);
                    task.execute();
                }
            }
        });

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    DocumentDownloader downloader = new DocumentDownloader();
                    Document page = downloader.DownloadPage();
                    String names = downloader.GetNames(page);
                    Log.d("Names", names.isEmpty() ? "No Names" : names);
                    Iterable<String> openings = downloader.GetOpenings(page);
                    Log.d("Openings", "Begin");
                    for (String s : openings) {
                        Log.d("Openings", s);
                    }
                    Log.d("Openings", "End");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();

        return v;
    }

    @Override
    public void onDestroy() {
        view = null;
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (task == null || task.getStatus() == AsyncTask.Status.FINISHED) {
            task = new AnalogActivityTask(view, 700);
            task.execute();
        }
    }

    private static class AnalogActivityTask extends AnalogTask {
        public AnalogActivityTask(final TextSwitcher view, final long timeout) {
            super(
                    new Runnable<Boolean>() {
                        @Override
                        public void run(final Boolean param) {
                            Handler handler = new Handler();
                            handler.postDelayed(new java.lang.Runnable() {
                                @Override
                                public void run() {
                                    if (view != null) { // The user might exit the application without waiting for response.
                                        AppCompatTextView tv = (AppCompatTextView) view.getNextView();
                                        if (param) {
                                            tv.setTextColor(ContextCompat.getColor(view.getContext(), android.R.color.holo_green_light));
                                            view.setText(view.getContext().getResources().getText(R.string.open_analog));
                                        } else {
                                            tv.setTextColor(ContextCompat.getColor(view.getContext(), android.R.color.holo_red_light));
                                            view.setText(view.getContext().getResources().getText(R.string.closed_analog));
                                        }
                                    }
                                }
                            }, timeout);
                        }
                    },
                    new java.lang.Runnable() {
                        @Override
                        public void run() {
                            if (view != null)
                                view.setText(view.getContext().getResources().getString(R.string.error_download));
                        }
                    }
            );
        }
    }
}
