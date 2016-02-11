package dk.cafeanalog;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextSwitcher;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class IsOpenFragment extends Fragment {
    private long lastTime;
    private TextSwitcher openSwitcher, namesSwitcher;
    private AnalogActivityTask isOpenTask;
    private ShowOpening parent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_is_open, container, false);

        openSwitcher = (TextSwitcher) v.findViewById(R.id.text_view);

        openSwitcher.setFactory(new TextSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                AppCompatTextView textView = new AppCompatTextView(getContext());
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                return textView;
            }
        });
        openSwitcher.setCurrentText(getResources().getText(R.string.is_open_analog));
        openSwitcher.setInAnimation(v.getContext(), android.R.anim.slide_in_left);
        openSwitcher.setOutAnimation(v.getContext(), android.R.anim.slide_out_right);

        namesSwitcher = (TextSwitcher) v.findViewById(R.id.name_view);
        namesSwitcher.setFactory(new TextSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                AppCompatTextView textView = new AppCompatTextView(getContext());
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                return textView;
            }
        });
        namesSwitcher.setInAnimation(v.getContext(), android.R.anim.slide_in_left);
        namesSwitcher.setOutAnimation(v.getContext(), android.R.anim.slide_out_right);

        v.findViewById(R.id.fragment_main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click();
            }
        });

        Button button = (Button) v.findViewById(R.id.openings_button);

        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AsyncTask<Void, Void, ArrayList<OpeningParser.Opening>>() {
                        @Override
                        protected ArrayList<OpeningParser.Opening> doInBackground(Void... params) {
                            try {
                                AnalogDownloader downloader = new AnalogDownloader(getContext());
                                Document page = downloader.downloadPage();

                                Iterable<OpeningParser.Opening> openings = downloader.getOpenings(page);

                                ArrayList<OpeningParser.Opening> opens = new ArrayList<>();

                                for (OpeningParser.Opening opening : openings) {
                                    opens.add(opening);
                                }

                                return opens;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(ArrayList<OpeningParser.Opening> openings) {
                            super.onPostExecute(openings);
                            if (parent != null) {
                                parent.showOpening(openings);
                            }
                        }
                    }.execute();
                }
            });
        }

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof ShowOpening) {
            parent = (ShowOpening) context;
        } else {
            throw new RuntimeException("Context must be instance of ShowOpening");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        parent = null;
    }

    @Override
    public void onDestroyView() {
        openSwitcher = null;
        namesSwitcher = null;
        if (isOpenTask != null) {
            isOpenTask.cancel(true);
            isOpenTask = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        click();
    }

    private class AnalogActivityTask extends AnalogTask {
        public AnalogActivityTask() {
            super(
                    getContext(),
                    new Action<Boolean>() {
                        @Override
                        public void run(final Boolean param) {
                            if (openSwitcher != null) { // The user might exit the application without waiting for response.
                                AppCompatTextView tv = (AppCompatTextView) openSwitcher.getNextView();
                                if (param) {
                                    tv.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_light));
                                    openSwitcher.setText(getContext().getResources().getText(R.string.open_analog));
                                } else {
                                    tv.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
                                    openSwitcher.setText(getContext().getResources().getText(R.string.closed_analog));
                                }
                            }
                        }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            if (openSwitcher != null)
                                openSwitcher.setText(openSwitcher.getContext().getResources().getString(R.string.error_download));
                        }
                    }
            );
        }
    }

    private void click() {
        if (Math.abs(System.currentTimeMillis() - lastTime) < 400) return;
        lastTime = System.currentTimeMillis();

        if (isOpenTask == null || isOpenTask.getStatus() == AsyncTask.Status.FINISHED) {
            isOpenTask = new AnalogActivityTask();
            isOpenTask.execute();

            new AsyncTask<Void,Void,String>() {
                @Override
                protected String doInBackground(Void... params) {
                    try {
                        AnalogDownloader downloader = new AnalogDownloader(getContext());
                        Document page = downloader.downloadPage();
                        return downloader.getNames(page);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return "";
                }

                @Override
                protected void onPostExecute(final String s) {
                    super.onPostExecute(s);
                    if (namesSwitcher != null) {
                        namesSwitcher.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                namesSwitcher.setText(s);
                            }
                        }, 100);
                    }
                }
            }.execute();
        }
    }

    public interface ShowOpening {
        void showOpening(ArrayList<OpeningParser.Opening> openings);
    }
}
