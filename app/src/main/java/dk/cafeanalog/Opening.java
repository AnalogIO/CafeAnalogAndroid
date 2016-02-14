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

public class Opening implements Parcelable {
    private final String mDayOfWeek, mOpen, mClose;
    private final int mDayOfMonth;

    public Opening(String dayOfWeek, int dayOfMonth, String open, String close) {
        mDayOfWeek = dayOfWeek;
        mDayOfMonth = dayOfMonth;
        mOpen = open;
        mClose = close;
    }

    private Opening(Parcel in) {
        mDayOfWeek = in.readString();
        mOpen = in.readString();
        mClose = in.readString();
        mDayOfMonth = in.readInt();
    }

    public static final Creator<Opening> CREATOR = new Creator<Opening>() {
        @Override
        public Opening createFromParcel(Parcel in) {
            return new Opening(in);
        }

        @Override
        public Opening[] newArray(int size) {
            return new Opening[size];
        }
    };

    public int getDayOfMonth() {
        return mDayOfMonth;
    }

    public String getClose() {
        return mClose;
    }

    public String getDayOfWeek() {
        return mDayOfWeek;
    }

    public String getOpen() {
        return mOpen;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDayOfWeek);
        dest.writeInt(mDayOfMonth);
        dest.writeString(mOpen);
        dest.writeString(mClose);
    }
}