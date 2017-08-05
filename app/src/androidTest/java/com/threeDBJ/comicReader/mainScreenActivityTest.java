package com.threeDBJ.comicReader;

//import com.threeDBJ.ComicSelectActivity.tests.QuestionableContentReaderTest;

import android.content.Intent;
import android.widget.Button;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.test.ActivityInstrumentationTestCase2;

import com.threeDBJ.comicReader.reader.ExplosmReader;
import com.threeDBJ.comicReader.reader.QuestionableContentReader;
import com.threeDBJ.comicReader.reader.Reader;
import com.threeDBJ.comicReader.reader.SMBCReader;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.threeDBJ.ComicSelectActivity.mainScreenActivityTest \
 * com.threeDBJ.ComicSelectActivity.tests/android.test.InstrumentationTestRunner
 */
public class mainScreenActivityTest extends ActivityInstrumentationTestCase2<ComicSelectActivity> {
    ComicSelectActivity activity;
    CountDownLatch signal;

    public mainScreenActivityTest() {
        super("com.threeDBJ.ComicSelectActivity", ComicSelectActivity.class);
    }

    public class TestRequestManager extends RequestManager {
        Reader pageHandler;
        public TestRequestManager(Reader pageHandler) {
            super();
            this.pageHandler = pageHandler;
        }
        public void grabComic(Reader context, Comic c) {
            if(c.getInd().equals(context.getCurComic().prevInd) || c.getInd().equals(context.getMax())) {
                new TestGetComicTask(context).execute(c);
            } else {
            }
        }
        class TestGetComicTask extends RequestManager.GetComicTask {
            public TestGetComicTask(Reader context) {
                super(context);
            }
            protected Comic doInBackground (Comic... data) {
                Reader temp = this.context;
                this.context = pageHandler;
                Comic c = super.doInBackground(data);
                this.context = temp;
                return c;
            }
            protected void onPostExecute (Comic comic) {
                super.onPostExecute(comic);
                signal.countDown();
            }
        }
    }

    Reader initReader(Reader pageHandler) throws Exception {
        signal = new CountDownLatch(1);
        Intent intent = new Intent();
        intent.putExtra("load_last_viewed", false);
        Reader reader = launchActivityWithIntent("com.threeDBJ.ComicSelectActivity", Reader.class, intent);

        pageHandler.init();
        /*
        reader.pPrev = pageHandler.pPrev;
        reader.pNext = pageHandler.pNext;
        reader.pImages = pageHandler.pImages;
        reader.pIndices = pageHandler.pIndices;
        reader.pMax = pageHandler.pMax;
        reader.pAlt = pageHandler.pAlt;

        reader.base = pageHandler.getBase();
        reader.max = pageHandler.getMax();
        reader.firstInd = pageHandler.getFirstInd();
        reader.rm = new TestRequestManager(pageHandler);
        */
        reader.loadInitial(reader.getMax());
        signal.await();
        return reader;
    }

    void testAllPrev(Reader reader) throws Exception {
        final Button prev = (Button)reader.findViewById(R.id.comic_prev);
        while(!reader.getFirstInd().equals(reader.getCurComic().getInd())) {
            signal = new CountDownLatch(1);
            reader.runOnUiThread(new Runnable() {
                public void run() {
                    prev.performClick();
                }
            });
            signal.await(5000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
    }

    public void testQuestionableContentPrev() throws Exception {
        Reader reader = initReader(new QuestionableContentReader());
        testAllPrev(reader);
    }

    public void testSMBCPrev() throws Exception {
        Reader reader = initReader(new SMBCReader());
        testAllPrev(reader);
    }

    public void testExplosmPrev() throws Exception {
        Reader reader = initReader(new ExplosmReader());
        testAllPrev(reader);
    }

}
