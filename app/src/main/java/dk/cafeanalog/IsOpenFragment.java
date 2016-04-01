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
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextSwitcher;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class IsOpenFragment extends Fragment {
    private long mLastTime;
    private TextSwitcher mOpenSwitcher, mNamesSwitcher;
    private AnalogActivityTask mIsOpenTask;
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
                AppCompatTextView textView = new AppCompatTextView(getActivity());
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
                AppCompatTextView textView = new AppCompatTextView(getActivity());
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

        return v;
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

    private class AnalogActivityTask extends AsyncTask<Void, Void, Opening> {

        @Override
        protected Opening doInBackground(Void... params) {
            try {
                return new AnalogDownloader().getCurrentOpening();
            } catch (Exception e) {
                e.printStackTrace();
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onPostExecute(final Opening opening) {
            super.onPostExecute(opening);
            if (mVisible) {
                if (mOpenSwitcher != null) { // The user might exit the application without waiting for response.
                    AppCompatTextView tv = (AppCompatTextView) mOpenSwitcher.getNextView();
                    if (opening != null) {
                        tv.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_light));
                        mOpenSwitcher.setText(getContext().getResources().getText(R.string.open_analog));

                        if (mVisible) {
                            if (mNamesSwitcher != null) {
                                mNamesSwitcher.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mVisible) {
                                            if (opening.getNames().size() == 1) {
                                                mNamesSwitcher.setText(opening.getNames().get(0));
                                                return;
                                            }

                                            StringBuilder builder = new StringBuilder();
                                            List<String> names = opening.getNames();
                                            for (int i = 0; i < names.size() - 1; i++) {
                                                builder.append(names.get(i)).append(", ");
                                            }
                                            builder.replace(builder.length() - 2, builder.length(), " &")
                                                    .append(" ").append(names.get(names.size() - 1));

                                            mNamesSwitcher.setText(builder);
                                        }
                                    }
                                }, 100);
                            }
                        }
                    } else {
                        tv.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
                        mOpenSwitcher.setText(getContext().getResources().getText(R.string.closed_analog));
                    }
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (mVisible) {
                if (mOpenSwitcher != null)
                    mOpenSwitcher.setText(mOpenSwitcher.getContext().getResources().getString(R.string.error_download));
            }
        }
    }

    private void click() {
        if (Math.abs(System.currentTimeMillis() - mLastTime) < 400) return;
        mLastTime = System.currentTimeMillis();

        if (mIsOpenTask == null || mIsOpenTask.getStatus() == AsyncTask.Status.FINISHED) {
            mIsOpenTask = new AnalogActivityTask();
            mIsOpenTask.execute();
        }
    }

    @Override
    public void onPause() {
        mVisible = false;
        super.onPause();
    }
}
