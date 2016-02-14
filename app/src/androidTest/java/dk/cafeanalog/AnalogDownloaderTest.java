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

import android.test.InstrumentationTestCase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;

import dk.cafeanalog.test.R;

public class AnalogDownloaderTest extends InstrumentationTestCase {
    private AnalogDownloader mDownloader;
    private Document mClosedNoOpeningsPage;
    private Document mOpenWithOpeningsPage;

    @Override
    protected void setUp() throws IOException {
        mDownloader = new AnalogDownloader(getInstrumentation().getTargetContext());
        InputStream inputStream =
                getInstrumentation()
                        .getContext()
                        .getResources()
                        .openRawResource(R.raw.closed_no_openings);

        mClosedNoOpeningsPage = Jsoup.parse(inputStream, "UTF-8", "http://cafeanalog.dk");

        inputStream =
                getInstrumentation()
                        .getContext()
                        .getResources()
                        .openRawResource(R.raw.open_with_openings);

        mOpenWithOpeningsPage = Jsoup.parse(inputStream, "UTF-8", "http://cafeanalog.dk");
    }

    public void testGetNamesNoNamesReturned() {
        String names = mDownloader.getNames(mClosedNoOpeningsPage);

        assertTrue(names.isEmpty());
    }

    public void testGetOpenings_NoneReturned() {
        Iterable<Opening> openings = mDownloader.getOpenings(mClosedNoOpeningsPage);

        assertEquals(0, size(openings));
    }

    public void testGetOpenings_EightReturned() {
        Iterable<Opening> openings = mDownloader.getOpenings(mOpenWithOpeningsPage);

        assertEquals(8, size(openings));
    }

    public void testIsOpen_Page_Closed() {
        AnalogDownloader.AnalogStatus status = mDownloader.isOpen(mClosedNoOpeningsPage);

        assertEquals(AnalogDownloader.AnalogStatus.CLOSED, status);
    }

    public void testIsOpen_Page_Open() {
        AnalogDownloader.AnalogStatus status = mDownloader.isOpen(mOpenWithOpeningsPage);

        assertEquals(AnalogDownloader.AnalogStatus.OPEN, status);
    }

    private static <T> int size(Iterable<T> iterable) {
        int size = 0;
        for (T ignored : iterable) {
            size++;
        }
        return size;
    }
}
