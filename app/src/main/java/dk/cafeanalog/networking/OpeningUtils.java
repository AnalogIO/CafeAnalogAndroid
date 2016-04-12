package dk.cafeanalog.networking;

import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import dk.cafeanalog.DayOfOpenings;

/**
 * Created by HansP on 11-04-2016.
 */
public class OpeningUtils {


        /**
         * Get the current opening status based on a list of openings
         * @param openings
         * @return
         */
        public static Opening getCurrentOpening(List<Opening> openings) {
            Date now = new Date(System.currentTimeMillis());
            for (Opening opening : openings) {
                if (now.after(opening.Open) && now.before(opening.Close)) {
                    return opening;
                }
            }
            return null;
        }

        /**
         * Get the openings time for each day based on a list of openings
         * @param openings
         * @return
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
                calendar.setTime(opening.Close);

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
