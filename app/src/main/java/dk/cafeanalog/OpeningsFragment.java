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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

        RecyclerView listView = (RecyclerView) view.findViewById(R.id.list);
        listView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
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
                    setBackground(R.drawable.border, holder.morning);
                } else {
                    holder.morning.setEnabled(false);
                    setBackground(R.drawable.border_inactive, holder.morning);
                }

                if (day.getNoon()) {
                    holder.noon.setEnabled(true);
                    setBackground(R.drawable.border, holder.noon);
                } else {
                    holder.noon.setEnabled(false);
                    setBackground(R.drawable.border_inactive, holder.noon);
                }

                if (day.getAfternoon()) {
                    holder.afternoon.setEnabled(true);
                    setBackground(R.drawable.border, holder.afternoon);
                } else {
                    holder.afternoon.setEnabled(false);
                    setBackground(R.drawable.border_inactive, holder.afternoon);
                }

                holder.dayOfWeek.setText(getDayOfWeek(getContext(), day.getDayOfWeek()));
            }

            @Override
            public int getItemCount() {
                return mOpenings.size();
            }
        });

        return view;
    }

    private void setBackground(int drawable, View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            v.setBackground(ContextCompat.getDrawable(getContext(), drawable));
        } else {
            //noinspection deprecation
            v.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), drawable));
        }
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
