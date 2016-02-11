package dk.cafeanalog;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class OpeningParser {
    private static final String MONDAY = "Mon",
                                TUESDAY = "Tue",
                                WEDNESDAY = "Wed",
                                THURSDAY = "Thu",
                                FRIDAY = "Fri",
                                SATURDAY = "Sat",
                                SUNDAY = "Sun";

    public static Opening ParseOpening(Context context, String opening) {
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

    public static class Opening implements Parcelable {
        private final String dayOfWeek, open, close;
        private final int dayOfMonth;

        public Opening(String dayOfWeek, int dayOfMonth, String open, String close) {
            this.dayOfWeek = dayOfWeek;
            this.dayOfMonth = dayOfMonth;
            this.open = open;
            this.close = close;
        }

        Opening(Parcel in) {
            dayOfWeek = in.readString();
            open = in.readString();
            close = in.readString();
            dayOfMonth = in.readInt();
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
            return dayOfMonth;
        }

        public String getClose() {
            return close;
        }

        public String getDayOfWeek() {
            return dayOfWeek;
        }

        public String getOpen() {
            return open;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(dayOfWeek);
            dest.writeInt(dayOfMonth);
            dest.writeString(open);
            dest.writeString(close);
        }
    }

}
