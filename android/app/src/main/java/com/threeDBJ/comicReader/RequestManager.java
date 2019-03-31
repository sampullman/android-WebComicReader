package com.threeDBJ.comicReader;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.threeDBJ.comicReader.ComicReaderApp.ComicState;
import com.threeDBJ.comicReader.reader.Reader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class RequestManager {

    private OkHttpClient client = new OkHttpClient();

    public int running = 0;

    public RequestManager() {

    }

    /* Get a String from a URL.
       Should not be called from the main thread
     */
    public String grabString(String url) {
        Request request = new Request.Builder().url(url).build();

        try {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return null;
                }
                ResponseBody data = response.body();
                if(data == null) {
                    return null;
                } else {
                    return data.string();
                }
            }
        } catch(IOException e) {}
        return null;
    }

    /* Start a background task that grabs a comic. */
    public void grabComic(Reader context, Comic c) {
        new GetComicTask(context).execute(c);
    }

    /* Start a background task that grabs a comic's alt image. */
    public void displayAltImage(Dialog context, String imgUrl, String title) {
        new DisplayAltImageTask(context, title).execute(imgUrl);
    }

    /* Makes multiple attempts to retrieve an image if the first one doesn't work */
    private Bitmap retrieveImage(String addr) {
        Bitmap result = getImageFromURL(addr);
        if(result != null) return result;
        result = getImageFromURL(addr);
        if(result != null) return result;
        result = getImageFromURL(addr);
        return result;
    }

    /* Grabs an image from the input url String and returns it in Bitmap form
       -Do not pass a null url */
    private Bitmap getImageFromURL(String addr) {
        // This check should probably be somewhere else
        if(addr == null) {
            return null;
        }
        try {
            URL url = new URL(addr);
            URLConnection conn = url.openConnection();
            InputStream inp = conn.getInputStream();
            return BitmapFactory.decodeStream(inp);
        } catch(MalformedURLException e) {
            Timber.v("getImage-malfy %s", addr);
        } catch(IOException e) {
            Timber.v("getImage-io %s", addr);
        }
        return null;
    }

    /* Retrieves the web page at the input url.
       On 302 response, go to redirect. */
    private String makeQuery(String url) {
        Timber.d("make-query %s", url);
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();
            if(body == null) {
                Timber.e("Null response body in makeQuery");
            } else {
                return body.string();
            }
        } catch(IOException e) {
            Timber.v("make_query_io %s", e.getMessage());
        }
        return null;
    }

    /* Background task for displaying an alt image. */
    private class DisplayAltImageTask extends AsyncTask<String, Integer, Bitmap> {
        Dialog context;
        protected String title;

        DisplayAltImageTask(Dialog context, String title) {
            this.context = context;
            this.title = title;
        }

        protected Bitmap doInBackground(String... data) {
            Bitmap alt = null;
            if(data[0] != null) {
                alt = retrieveImage(data[0]);
            }
            return alt;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected void onProgressUpdate(Integer... prog) {
        }

        protected void onPostExecute(Bitmap result) {
            if(context.isShowing()) {
                ImageView image = context.findViewById(R.id.alt_image);
                image.setImageBitmap(result);
                context.setTitle(title);
            }
        }
    }

    /* Background task for grabbing a comic. */
    class GetComicTask extends AsyncTask<Comic, Integer, Comic> {
        Reader context;
        ComicState state;

        public GetComicTask(Reader context) {
            this.context = context;
            this.state = context.getState();
        }

        protected void onPreExecute() {
            running += 1;
        }

        protected Comic doInBackground(Comic... data) {
            String page, imgUrl;
            Comic comic = data[0];
            try {
                page = makeQuery(comic.getUrl());
                if(page == null) {
                    comic.setError(true);
                } else {
                    imgUrl = this.context.handleRawPage(comic, page);
                    this.context = null; // Hack so that the Reader is cleaned up quickly on orientation change
                    if(imgUrl == null) {
                        Timber.d("cmreader url: %s", comic.getInd());
                    } else {
                        comic.setComic(retrieveImage(imgUrl));
                    }
                }
            } catch(Exception e) {
                Timber.v("cmreader Comic error: %s", e.getMessage());
                comic.setError(true);
            }
            return comic;
        }

        /* Decide what to do with the comic according to the loading state. */
        protected void onPostExecute(Comic comic) {
            running -= 1;
            if(comic.getError()) {
                state.comicError(comic);
            } else {
                state.comicLoaded(comic);
            }
        }

    }
}
