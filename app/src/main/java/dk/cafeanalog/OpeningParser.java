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

import java.util.Calendar;
import java.util.GregorianCalendar;

@SuppressWarnings("WeakerAccess")
public class OpeningParser {
    private static final String MONDAY = "Mon",
                                TUESDAY = "Tue",
                                WEDNESDAY = "Wed",
                                THURSDAY = "Thu",
                                FRIDAY = "Fri",
                                SATURDAY = "Sat",
                                SUNDAY = "Sun";

    public static Opening parseOpening(Context context, String opening) {
        String[] tokens = opening.split("\\s");

        if (tokens.length != 5) return null;

        String dayOfWeek;

        switch (tokens[0]) {
            case MONDAY:
                dayOfWeek = context.getString(R.string.monday);
                break;
            case TUESDAY:
                dayOfWeek = context.getString(R.string.tuesday);
                break;
            case WEDNESDAY:
                dayOfWeek = context.getString(R.string.wednesday);
                break;
            case THURSDAY:
                dayOfWeek = context.getString(R.string.thursday);
                break;
            case FRIDAY:
                dayOfWeek = context.getString(R.string.friday);
                break;
            case SATURDAY:
                dayOfWeek = context.getString(R.string.saturday);
                break;
            case SUNDAY:
                dayOfWeek = context.getString(R.string.sunday);
                break;
            default:
                dayOfWeek = context.getString(R.string.unknown_day_of_week);
                break;
        }

        int dayOfMonth = Integer.parseInt(tokens[1].replaceAll("(th:)|(nd:)|(st:)", ""));

        if (new GregorianCalendar().get(Calendar.DAY_OF_MONTH) == dayOfMonth) dayOfWeek = context.getString(R.string.today);

        return new Opening(dayOfWeek, dayOfMonth, tokens[2], tokens[4]);
    }
}
