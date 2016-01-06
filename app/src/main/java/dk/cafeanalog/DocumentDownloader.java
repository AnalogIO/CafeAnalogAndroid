package dk.cafeanalog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentDownloader {
    private Pattern nameRegex = Pattern.compile("On shift right now: ([a-zæøå,&\\s]+)\\n", Pattern.CASE_INSENSITIVE);

    public Document DownloadPage() throws IOException {
        return Jsoup.connect("http://cafeanalog.dk/").get();
    }

    public Iterable<String> GetOpenings(Document page) {
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

    public String GetNames(Document page) {
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
