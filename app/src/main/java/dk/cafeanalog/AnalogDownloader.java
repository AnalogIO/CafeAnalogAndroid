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

import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnalogDownloader {
    private static final long TIME_BETWEEN_DOWNLOADS = 10000;

    private ArrayList<Opening> mOpeningsCache;
    private long mLastGet;

    public enum AnalogStatus {
        OPEN,
        CLOSED,
        UNKNOWN
    }

    public AnalogStatus isOpen() {
        HttpURLConnection connection = null;
        JsonReader reader = null;

        try {
            URL url = new URL("http", "cafeanalog.dk", "api/open");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            reader = new JsonReader(new InputStreamReader(connection.getInputStream()));
            reader.beginObject();
            while (!reader.nextName().equals("open")) { reader.skipValue(); }
            return reader.nextBoolean() ? AnalogStatus.OPEN : AnalogStatus.CLOSED;
        } catch (IOException e) {
            return AnalogStatus.UNKNOWN;
        } finally {
            if (connection != null)
                connection.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public Opening getCurrentOpening() throws JSONException, ParseException, IOException {
        if (System.currentTimeMillis() - mLastGet > TIME_BETWEEN_DOWNLOADS || mOpeningsCache == null) {
            getOpenings(true);
        }
        Date now = new Date(System.currentTimeMillis());
        for (Opening opening : mOpeningsCache) {
            if (now.after(opening.getOpen()) && now.before(opening.getClose())) {
                return opening;
            }
        }
        return null;
    }

    private ArrayList<Opening> getOpenings(boolean forceRefresh) throws IOException, JSONException, ParseException {
        if (!forceRefresh && System.currentTimeMillis() - mLastGet < TIME_BETWEEN_DOWNLOADS && mOpeningsCache != null) {
            return mOpeningsCache;
        }
        mLastGet = System.currentTimeMillis();
        URL url = new URL("http", "cafeanalog.dk", "api/shifts");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.connect();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String s; while ((s = reader.readLine()) != null) { builder.append(s); }

        JSONArray array = new JSONArray(builder.toString());

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ", Locale.getDefault());

        ArrayList<Opening> openings = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);

            Date open = dateFormat.parse(object.getString("Open"));
            Date close = dateFormat.parse(object.getString("Close"));
            JSONArray nameArray = object.getJSONArray("Employees");
            List<String> names = new ArrayList<>();

            for (int j = 0; j < nameArray.length(); j++) {
                names.add(nameArray.getString(j));
            }

            openings.add(new Opening(open, close, names));
        }
        mOpeningsCache = openings;
        return openings;
    }

    public ArrayList<DayOfOpenings> getDaysOfOpenings(boolean forceRefresh) throws JSONException, ParseException, IOException {
        ArrayList<Opening> openings = getOpenings(forceRefresh);

        Collections.sort(openings, new Comparator<Opening>() {
            @Override
            public int compare(Opening lhs, Opening rhs) {
                return lhs.getOpen().compareTo(rhs.getOpen());
            }
        });

        ArrayList<DayOfOpenings> result = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();

        for (Opening opening : openings) {
            calendar.setTime(opening.getOpen());
            int dayOfMonth = calendar.get(Calendar.DATE);
            DayOfOpenings day;
            boolean retrieved = false;
            if (!result.isEmpty()) {
                day = result.get(result.size() - 1);
                if (day.getDayOfMonth() != dayOfMonth) {
                    day = new DayOfOpenings(dayOfMonth, calendar.get(Calendar.DAY_OF_WEEK));
                } else {
                    retrieved = true;
                }
            } else {
                day = new DayOfOpenings(dayOfMonth, calendar.get(Calendar.DAY_OF_WEEK));
            }

            int openHour = calendar.get(Calendar.HOUR_OF_DAY);
            switch (openHour) {
                case 9:
                    day.setMorning();
                    break;
                case 11:
                    day.setNoon();
                    break;
                case 14:
                    day.setAfternoon();
                    break;
                default:
                    Log.d("OpeningsTranslation", "Wrong hour: " + openHour);
            }
            calendar.setTime(opening.getClose());

            int closeHour = calendar.get(Calendar.HOUR_OF_DAY);

            if (openHour == 9 && closeHour == 14) {
                day.setNoon();
            } else if (openHour == 9 && closeHour == 17) {
                day.setNoon();
                day.setAfternoon();
            } else if (openHour == 11 && closeHour == 17) {
                day.setAfternoon();
            }

            if (!retrieved) result.add(day);
        }

        return result;
    }
}
