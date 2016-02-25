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
import java.util.Date;
import java.util.List;

public class Opening implements Parcelable {
    private final Date mOpen, mClose;
    private final List<String> mNames;

    public Opening(Date open, Date close, List<String> names) {
        mOpen = open;
        mClose = close;
        mNames = names;
    }

    private Opening(Parcel in) {
        mOpen = new Date(in.readLong());
        mClose = new Date(in.readLong());
        in.readStringList(mNames = new ArrayList<>());
    }

    public static final Creator<Opening> CREATOR = new Creator<Opening>() {
        @Override
        public Opening createFromParcel(Parcel in) { return new Opening(in); }

        @Override
        public Opening[] newArray(int size) { return new Opening[size]; }
    };

    public Date getOpen() { return mOpen; }
    public Date getClose() { return mClose; }
    public List<String> getNames() { return mNames; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mOpen.getTime());
        dest.writeLong(mClose.getTime());
        dest.writeStringList(mNames);
    }
}