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

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class OpeningsFragment extends Fragment {
    private static final String OPENING_CONTENT = "Opening_Content";

    private ArrayList<DayOfOpenings> mOpenings;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OpeningsFragment() {
    }

    public static OpeningsFragment newInstance(List<DayOfOpenings> openings) {
        OpeningsFragment fragment = new OpeningsFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList(OPENING_CONTENT, new ArrayList<>(openings));

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mOpenings = args.getParcelableArrayList(OPENING_CONTENT);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(OPENING_CONTENT)) {
                mOpenings = savedInstanceState.getParcelableArrayList(OPENING_CONTENT);
            }
        }

        View view = inflater.inflate(R.layout.fragment_opening_list, container, false);

        final SwipeRefreshLayout refresher = (SwipeRefreshLayout) view.findViewById(R.id.refresher);
        final RecyclerView listView = (RecyclerView) view.findViewById(R.id.list);
        refresher.setNestedScrollingEnabled(true);
        refresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new AsyncTask<Void, Void, ArrayList<DayOfOpenings>>() {
                    @Override
                    protected ArrayList<DayOfOpenings> doInBackground(Void... params) {
                        try {
                            return new AnalogDownloader().getDaysOfOpenings(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return new ArrayList<>();
                    }

                    @Override
                    protected void onPostExecute(ArrayList<DayOfOpenings> dayOfOpenings) {
                        super.onPostExecute(dayOfOpenings);
                        mOpenings.clear();
                        mOpenings.addAll(dayOfOpenings);
                        listView.getAdapter().notifyDataSetChanged();
                        refresher.setRefreshing(false);
                    }
                }.execute();
            }
        });


        listView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(new RecyclerView.Adapter<DayHolder>() {
            @Override
            public DayHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View day = inflater.inflate(R.layout.day, parent, false);
                return new DayHolder(day);
            }

            @Override
            public void onBindViewHolder(DayHolder holder, int position) {
                DayOfOpenings day = mOpenings.get(position);

                if (day.getMorning()) {
                    holder.morning.setEnabled(true);
                } else {
                    holder.morning.setEnabled(false);
                }

                if (day.getNoon()) {
                    holder.noon.setEnabled(true);
                } else {
                    holder.noon.setEnabled(false);
                }

                if (day.getAfternoon()) {
                    holder.afternoon.setEnabled(true);
                } else {
                    holder.afternoon.setEnabled(false);
                }

                holder.dayOfWeek.setText(getDayOfWeek(getActivity(), day.getDayOfWeek()));
            }

            @Override
            public int getItemCount() {
                return mOpenings.size();
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(OPENING_CONTENT, mOpenings);
    }

    private class DayHolder extends RecyclerView.ViewHolder {
        private final TextView morning, noon, afternoon, dayOfWeek;

        public DayHolder(View itemView) {
            super(itemView);
            morning = (TextView) itemView.findViewById(R.id.morning);
            noon = (TextView) itemView.findViewById(R.id.noon);
            afternoon = (TextView) itemView.findViewById(R.id.afternoon);
            dayOfWeek = (TextView) itemView.findViewById(R.id.day_of_week);
        }
    }

    private static String getDayOfWeek(Context context, int dayOfWeek) {
        switch (dayOfWeek) {
            case DayOfOpenings.SUNDAY: return context.getString(R.string.sunday);
            case DayOfOpenings.MONDAY: return context.getString(R.string.monday);
            case DayOfOpenings.TUESDAY: return context.getString(R.string.tuesday);
            case DayOfOpenings.WEDNESDAY: return context.getString(R.string.wednesday);
            case DayOfOpenings.THURSDAY: return context.getString(R.string.thursday);
            case DayOfOpenings.FRIDAY: return context.getString(R.string.friday);
            case DayOfOpenings.SATURDAY:
            default:                   return context.getString(R.string.saturday);
        }
    }
}
