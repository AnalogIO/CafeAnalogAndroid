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
