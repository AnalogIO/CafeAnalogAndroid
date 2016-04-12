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

public class DayOfOpenings implements Parcelable {
    public static final int SUNDAY = 1,
                            MONDAY = 2,
                            TUESDAY = 3,
                            WEDNESDAY = 4,
                            THURSDAY = 5,
                            FRIDAY = 6,
                            SATURDAY = 7;

    private boolean morning, noon, afternoon;
    private final int dayOfMonth;
    private final int dayOfWeek;

    public DayOfOpenings(int dayOfMonth, int dayOfWeek) {
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
        this.morning = false;
        this.noon = false;
        this.afternoon = false;
    }


    private DayOfOpenings(Parcel in) {
        morning = in.readByte() != 0;
        noon = in.readByte() != 0;
        afternoon = in.readByte() != 0;
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

    public boolean getMorning() {
        return morning;
    }

    public boolean getNoon() {
        return noon;
    }

    public boolean getAfternoon() {
        return afternoon;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setMorning() {
        this.morning = true;
    }

    public void setNoon() {
        this.noon = true;
    }

    public void setAfternoon() {
        this.afternoon = true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (morning ? 1 : 0));
        dest.writeByte((byte) (noon ? 1 : 0));
        dest.writeByte((byte) (afternoon ? 1 : 0));
        dest.writeInt(dayOfWeek);
        dest.writeInt(dayOfMonth);
    }

}
