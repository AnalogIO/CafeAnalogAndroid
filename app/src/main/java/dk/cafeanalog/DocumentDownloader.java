package dk.cafeanalog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentDownloader {
    private Pattern nameRegex = Pattern.compile("On shift right now: ([a-zæøå,&\\s]+)\\n", Pattern.CASE_INSENSITIVE);

    public Document DownloadPage() throws IOException {
        return Jsoup.connect("http://cafeanalog.dk/").get();
    }

    public List<String> GetOpenings(Document page) {
        List<String> results = new ArrayList<>();
        for (Element elem : page.getElementById("openingHours").getElementsByTag("li")) {
            results.add(elem.text());
        }
        return results;
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
