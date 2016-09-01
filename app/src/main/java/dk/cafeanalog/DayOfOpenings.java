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

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class DayOfOpenings implements Parcelable {
    public static final int SUNDAY = 1,
                            MONDAY = 2,
                            TUESDAY = 3,
                            WEDNESDAY = 4,
                            THURSDAY = 5,
                            FRIDAY = 6,
                            SATURDAY = 7;

    private List<Integer> openings, closings;
    private final int dayOfMonth;
    private final int dayOfWeek;

    public DayOfOpenings(int dayOfMonth, int dayOfWeek) {
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
        openings = new ArrayList<>();
        closings = new ArrayList<>();
    }


    private DayOfOpenings(Parcel in) {
        openings = new ArrayList<>();
        closings = new ArrayList<>();

        int number = in.readInt();
        int[] buffer = new int[number];

        in.readIntArray(buffer);
        for (int opening: buffer) {
            openings.add(opening);
        }

        in.readIntArray(buffer);
        for (int closing : buffer) {
            closings.add(closing);
        }

        dayOfWeek = in.readInt();
        dayOfMonth = in.readInt();
    }

    public static final Creator<DayOfOpenings> CREATOR = new Creator<DayOfOpenings>() {
        @Override
        public DayOfOpenings createFromParcel(Parcel in) {
            return new DayOfOpenings(in);
        }

        @Override
        public DayOfOpenings[] newArray(int size) {
            return new DayOfOpenings[size];
        }
    };

    /**
     * Tells whether or not this DayOfOpenings represents today.
     * Note: This only works when limiting the retrieved days to be within the next month
     * @return True if this day of opening represents today. False otherwise.
     */
    public boolean isToday() {
        Calendar calendar = GregorianCalendar.getInstance();

        return calendar.get(Calendar.DAY_OF_MONTH) == dayOfMonth;
    }

    public List<Integer> getOpenings() {
        return openings;
    }

    public void addOpening(int opening, int closing) {
        for (int i = 0; i < openings.size(); i++) {
            int iOpening = openings.get(i);
            int iClosing = closings.get(i);
            // If they overlap in the beginning, replace existing opening with new 'extended' opening
            if (opening < iOpening && closing < iClosing && closing > iOpening) {
                openings.set(i, opening);
                return;
            }

            // If they overlap in the end, replace existing opening with new 'extended' opening
            if (opening > iOpening && opening < iClosing && closing > iClosing) {
                closings.set(i, closing);
                return;
            }

            // If the new opening overlaps in both ends, replace with new opening.
            if (opening < iOpening && closing > iClosing) {
                openings.set(i, opening);
                closings.set(i, closing);
                return;
            }

            // If the new opening is consumed by existing, just ignore.
            if (opening > iOpening && closing < iClosing) {
                return;
            }
        }
        // If none overlap, insert new opening.
        openings.add(opening);
        closings.add(closing);
    }

    public List<Integer> getClosings() {
        return closings;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(openings.size());

        int[] buffer = new int[openings.size()];

        for(int i = 0; i < buffer.length; i++) {
            buffer[i] = openings.get(i);
        }

        dest.writeIntArray(buffer);

        for(int i = 0; i < buffer.length; i++) {
            buffer[i] = closings.get(i);
        }

        dest.writeIntArray(buffer);

        dest.writeInt(dayOfWeek);
        dest.writeInt(dayOfMonth);
    }

}
