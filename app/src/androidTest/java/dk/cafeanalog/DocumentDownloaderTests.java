package dk.cafeanalog;

import android.test.InstrumentationTestCase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
        List<String> openings = downloader.GetOpenings(document);

        assertEquals(0, openings.size());
    }
}
