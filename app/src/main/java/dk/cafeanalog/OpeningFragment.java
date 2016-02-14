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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ListViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class OpeningFragment extends Fragment {
    private static final String OPENING_CONTENT = "Opening_Content";

    private ArrayList<Opening> mOpenings;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OpeningFragment() {
    }

    public static OpeningFragment newInstance(ArrayList<Opening> openings) {
        OpeningFragment fragment = new OpeningFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList(OPENING_CONTENT, openings);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        mOpenings = args.getParcelableArrayList(OPENING_CONTENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(OPENING_CONTENT)) {
                mOpenings = savedInstanceState.getParcelableArrayList(OPENING_CONTENT);
            }
        }

        View view = inflater.inflate(R.layout.fragment_opening_list, container, false);

        ListViewCompat listView = (ListViewCompat) view.findViewById(R.id.list);

        List<Map<String,Object>> list = new ArrayList<>();
        String[] numerals = getResources().getStringArray(R.array.numerals);

        for (Opening opening : mOpenings) {
            Map<String,Object> map = new HashMap<>();
            map.put("dayOfWeek", opening.getDayOfWeek());
            if (opening.getDayOfWeek().equals(getString(R.string.today))) {
                map.put("dayOfMonth", "");
            } else {
                map.put("dayOfMonth", numerals[opening.getDayOfMonth() - 1]);
            }
            map.put("open", opening.getOpen());
            map.put("close", opening.getClose());

            list.add(map);
        }

        listView.setAdapter(
                new SimpleAdapter(getContext(),
                        list,
                        R.layout.fragment_opening,
                        new String[]{"dayOfWeek", "dayOfMonth", "open", "close"},
                        new int[]{R.id.day_of_week, R.id.day_of_month, R.id.open, R.id.close}));
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(OPENING_CONTENT, mOpenings);
    }
}
