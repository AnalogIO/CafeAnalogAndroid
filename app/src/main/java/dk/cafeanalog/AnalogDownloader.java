package dk.cafeanalog;

import android.content.Context;
import android.util.JsonReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalogDownloader {
    private static final Pattern nameRegex = Pattern.compile("On shift right now: ([a-zæøå,;&\\s]+) Scheduled", Pattern.CASE_INSENSITIVE);
    private final Context context;

    public AnalogDownloader(Context context) {
        this.context = context;
    }

    public Document downloadPage() throws IOException {
        return Jsoup.connect("http://cafeanalog.dk/").get();
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
            e.printStackTrace();
            return AnalogStatus.UNKNOWN;
        } finally {
            if (connection != null)
                connection.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
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

    public Iterable<OpeningParser.Opening> getOpenings(Document page) {
        final Iterable<String> openingStrings = getOpeningsInternal(page);

        return new Iterable<OpeningParser.Opening>() {
            private final Iterator<String> iterator = openingStrings.iterator();

            @Override
            public Iterator<OpeningParser.Opening> iterator() {
                return new Iterator<OpeningParser.Opening>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public OpeningParser.Opening next() {
                        return OpeningParser.ParseOpening(context, iterator.next());
                    }

                    @Override
                    public void remove() {
                        iterator.remove();
                    }
                };
            }
        };
    }

    private Iterable<String> getOpeningsInternal(Document page) {
        final Iterable<Element> elements = page.getElementById("openingHours").getElementsByTag("li");
        return new Iterable<String>() {
            private final Iterator<Element> iterator = elements.iterator();

            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public String next() {
                        return iterator.next().text();
                    }

                    @Override
                    public void remove() {
                        iterator.remove();
                    }
                };
            }
        };
    }

    public String getNames(Document page) {
        String text = page.getElementById("openingHours").text();

        Matcher matcher = nameRegex.matcher(text);
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
