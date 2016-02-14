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

/**
 * A simple {@link Fragment} subclass.
 */
public class IsOpenFragment extends Fragment {
    private long mLastTime;
    private TextSwitcher mOpenSwitcher, mNamesSwitcher;
    private AnalogActivityTask mIsOpenTask;
    private ShowOpening mParent;
    private boolean mVisible;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mVisible = true;
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_is_open, container, false);

        mOpenSwitcher = (TextSwitcher) v.findViewById(R.id.text_view);

        mOpenSwitcher.setFactory(new TextSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                AppCompatTextView textView = new AppCompatTextView(getContext());
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                return textView;
            }
        });
        mOpenSwitcher.setCurrentText(getResources().getText(R.string.is_open_analog));
        mOpenSwitcher.setInAnimation(v.getContext(), android.R.anim.slide_in_left);
        mOpenSwitcher.setOutAnimation(v.getContext(), android.R.anim.slide_out_right);

        mNamesSwitcher = (TextSwitcher) v.findViewById(R.id.name_view);
        mNamesSwitcher.setFactory(new TextSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                AppCompatTextView textView = new AppCompatTextView(getContext());
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                return textView;
            }
        });
        mNamesSwitcher.setInAnimation(v.getContext(), android.R.anim.slide_in_left);
        mNamesSwitcher.setOutAnimation(v.getContext(), android.R.anim.slide_out_right);

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
                    mParent.showOpening();
                }
            });
        }

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof ShowOpening) {
            mParent = (ShowOpening) context;
        } else {
            throw new RuntimeException("Context must be instance of ShowOpening");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mParent = null;
    }

    @Override
    public void onDestroyView() {
        mOpenSwitcher = null;
        mNamesSwitcher = null;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        mVisible = true;
        click();
    }

    private class AnalogActivityTask extends AnalogTask {
        public AnalogActivityTask() {
            super(
                    getContext(),
                    new Action<Boolean>() {
                        @Override
                        public void run(final Boolean param) {
                            if (mVisible) {
                                if (mOpenSwitcher != null) { // The user might exit the application without waiting for response.
                                    AppCompatTextView tv = (AppCompatTextView) mOpenSwitcher.getNextView();
                                    if (param) {
                                        tv.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_light));
                                        mOpenSwitcher.setText(getContext().getResources().getText(R.string.open_analog));
                                    } else {
                                        tv.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
                                        mOpenSwitcher.setText(getContext().getResources().getText(R.string.closed_analog));
                                    }
                                }
                            }
                        }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            if (mVisible) {
                                if (mOpenSwitcher != null)
                                    mOpenSwitcher.setText(mOpenSwitcher.getContext().getResources().getString(R.string.error_download));
                            }
                        }
                    }
            );
        }
    }

    private void click() {
        if (Math.abs(System.currentTimeMillis() - mLastTime) < 400) return;
        mLastTime = System.currentTimeMillis();

        if (mIsOpenTask == null || mIsOpenTask.getStatus() == AsyncTask.Status.FINISHED) {
            mIsOpenTask = new AnalogActivityTask();
            mIsOpenTask.execute();

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
                    if (mVisible) {
                        if (mNamesSwitcher != null) {
                            mNamesSwitcher.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (mVisible) {
                                        mNamesSwitcher.setText(s);
                                    }
                                }
                            }, 100);
                        }
                    }
                }
            }.execute();
        }
    }

    @Override
    public void onPause() {
        mVisible = false;
        super.onPause();
    }

    public interface ShowOpening {
        void showOpening();
    }
}
