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
import android.util.JsonReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalogDownloader {
    private static final long TIME_BETWEEN_DOWNLOADS = 10000;
    private static final Pattern NAMES_REGEX = Pattern.compile("On shift right now: ([a-zæøå,;&\\s]+) Scheduled", Pattern.CASE_INSENSITIVE);

    private final Context mContext;
    private Document mPage;
    private long mLastGet;

    public AnalogDownloader(Context context) {
        mContext = context;
    }

    public Document downloadPage() throws IOException {
        if (System.currentTimeMillis() - mLastGet < TIME_BETWEEN_DOWNLOADS || mPage == null) {
            mPage = Jsoup.connect("http://cafeanalog.dk/").get();
            mLastGet = System.currentTimeMillis();
        }
        return mPage;
    }

    public enum AnalogStatus {
        OPEN,
        CLOSED,
        UNKNOWN
    }

    public AnalogStatus isOpen() {
        HttpURLConnection connection = null;
        JsonReader reader = null;
        try {
            URL url = new URL("http", "cafeanalog.dk", "REST");
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

    public AnalogStatus isOpen(Document page) {
        String text = page.getElementById("openingHours").getElementsByTag("h1").text();
        if (text.contains("Cløsed")) {
            return AnalogStatus.CLOSED;
        } else if (text.contains("Åpen")) {
            return AnalogStatus.OPEN;
        } else {
            return AnalogStatus.UNKNOWN;
        }
    }

    public ArrayList<Opening> getOpenings(Document page) {
        Elements elements = page.getElementById("openingHours").getElementsByTag("li");

        ArrayList<Opening> result = new ArrayList<>();

        try {
            for (Element elem : elements) {
                result.add(OpeningParser.parseOpening(mContext, elem.text()));
            }
        } catch (Exception ignore) {
            return result;
        }
        return result;
    }

    public String getNames(Document page) {
        String text = page.getElementById("openingHours").text();

        Matcher matcher = NAMES_REGEX.matcher(text);
        if (matcher.find()) {
            String names = matcher.group(1);
            if (!names.contains("&")) {
                return names;
            } else {
                String[] split = names.split(" & ");
                StringBuilder result = new StringBuilder();

                for (int i = 0; i < split.length - 1; i++) {
                    result.append(split[i]).append(", ");
                }
                result.delete(result.length() - 2, result.length());
                result.append(" & ").append(split[split.length - 1]);
                return result.toString();
            }
        }
        return "";
    }
}
