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

package dk.cafeanalog.networking;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import dk.cafeanalog.DayOfOpenings;

/*
 * Created by HansP on 11-04-2016.
 */
class OpeningUtils {


    /**
     * Get the current opening status based on a list of openings
     * @param openings The list of openings to filter on.
     * @return An opening representing the current opening. If no such
     * opening exists, null is returned.
     */
    public static Opening getCurrentOpening(List<Opening> openings) {
        Date now = new Date(System.currentTimeMillis());
        for (Opening opening : openings) {
            if (opening.Open.before(now) && opening.Close.after(now)) {
                return opening;
            }
        }
        return null;
    }

    /**
     * Get the openings time for each day based on a list of openings
     * @param openings The list of openings to transform.
     * @return A list of DayOfOpenings which for each day represented
     * contains an entry with the opening hours for that day.
     */
    public static List<DayOfOpenings> getDaysOfOpenings(List<Opening> openings) {

        Collections.sort(openings);
        ArrayList<DayOfOpenings> result = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        for (Opening opening : openings) {
            calendar.setTime(opening.Open);
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

            calendar.setTime(opening.Close);

            int closeHour = calendar.get(Calendar.HOUR_OF_DAY);

            day.addOpening(openHour, closeHour);

            if (!retrieved) result.add(day);
        }

        return result;
    }

    public static DayOfOpenings getTodaysOpenings(List<Opening> openings) {
        List<DayOfOpenings> daysOfOpenings = getDaysOfOpenings(openings);

        for (DayOfOpenings day : daysOfOpenings) {
            if (day.isToday()) return day;
        }
        return null;
    }
}
