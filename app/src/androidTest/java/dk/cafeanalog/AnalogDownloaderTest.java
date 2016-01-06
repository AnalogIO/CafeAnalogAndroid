package dk.cafeanalog;

import android.test.InstrumentationTestCase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;

import dk.cafeanalog.test.R;

public class AnalogDownloaderTest extends InstrumentationTestCase {
    private AnalogDownloader downloader;
    private Document closedNoOpeningsPage;

    @Override
    protected void setUp() throws IOException {
        downloader = new AnalogDownloader();
        InputStream inputStream =
                getInstrumentation()
                        .getContext()
                        .getResources()
                        .openRawResource(R.raw.closed_no_openings);

        closedNoOpeningsPage = Jsoup.parse(inputStream, "UTF-8", "http://cafeanalog.dk");
    }

    public void testGetNamesNoNamesReturned() {
        String names = downloader.getNames(closedNoOpeningsPage);

        assertTrue(names.isEmpty());
    }

    public void testGetOpeningsNoneReturned() {
        Iterable<String> openings = downloader.getOpenings(closedNoOpeningsPage);

        assertEquals(0, size(openings));
    }

    public void testIsOpen_Page() {
        AnalogDownloader.AnalogStatus status = downloader.isOpen(closedNoOpeningsPage);

        assertEquals(AnalogDownloader.AnalogStatus.CLOSED, status);
    }

    private static <T> int size(Iterable<T> iterable) {
        int size = 0;
        for (T ignored : iterable) {
            size++;
        }
        return size;
    }
}
