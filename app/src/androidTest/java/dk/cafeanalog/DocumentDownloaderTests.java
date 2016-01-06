package dk.cafeanalog;

import android.test.InstrumentationTestCase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;

import dk.cafeanalog.test.R;

public class DocumentDownloaderTests extends InstrumentationTestCase {
    private DocumentDownloader downloader;
    private Document document;

    @Override
    protected void setUp() throws IOException {
        downloader = new DocumentDownloader();
        InputStream inputStream =
                getInstrumentation()
                        .getContext()
                        .getResources()
                        .openRawResource(R.raw.closed_no_openings);

        document = Jsoup.parse(inputStream, "UTF-8", "http://cafeanalog.dk");
    }

    public void testGetNamesNoNamesReturned() {
        String names = downloader.GetNames(document);

        assertTrue(names.isEmpty());
    }

    public void testGetOpeningsNoneReturned() {
        Iterable<String> openings = downloader.GetOpenings(document);

        assertEquals(0, size(openings));
    }

    private static <T> int size(Iterable<T> iterable) {
        int size = 0;
        for (T ignored : iterable) {
            size++;
        }
        return size;
    }
}
