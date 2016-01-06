package dk.cafeanalog;

import android.util.JsonReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalogDownloader {
    private Pattern nameRegex = Pattern.compile("On shift right now: ([a-zæøå,&\\s]+)\\n", Pattern.CASE_INSENSITIVE);

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
        try {
            URL url = new URL("http", "cafeanalog.dk", "REST");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            try (JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()))) {
                reader.beginObject();
                while (!Objects.equals(reader.nextName(), "open")) { reader.skipValue(); }
                return reader.nextBoolean() ? AnalogStatus.OPEN : AnalogStatus.CLOSED;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return AnalogStatus.UNKNOWN;
        } finally {
            if (connection != null)
                connection.disconnect();
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

    public Iterable<String> getOpenings(Document page) {
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
        Matcher matcher = nameRegex.matcher(page.getElementById("openingHours").text());
        if (matcher.matches()) {
            String names = matcher.group(1);
            if (!names.contains("&")) {
                return names;
            } else {
                String[] split = names.split(" & ");
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < split.length - 1; i++) {
                    result.append(split[i]).append(", ");
                }
                result.insert(result.length() - 2, " & ").append(split[split.length - 1]);
                return result.toString();
            }
        }
        return "";
    }
}
