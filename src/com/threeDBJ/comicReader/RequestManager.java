package com.threeDBJ.comicReader;

import android.app.Activity;
import android.os.AsyncTask;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.content.Context;
import android.content.DialogInterface;
import android.app.ProgressDialog;

import android.widget.ImageView;
import android.widget.TextView;

import android.app.Dialog;
import java.net.URLConnection;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import android.util.Log;

public class RequestManager {

    public enum LoadingState {
	None, Current, Previous, Next, Done;
    }

    Pattern unichar = Pattern.compile("&#[0-9]+");

    public LoadingState state = LoadingState.None;

    public int running = 0;

    public RequestManager () {

    }

    /* Start a background task that grabs a comic. */
    public void grabComic(Reader context, String url) {
	new GetComicTask(context).execute(url);
    }

    /* Start a background task that grabs a comic's alt image. */
    public void displayAltImage(Dialog context, String imgUrl, String title) {
	new DisplayAltImageTask(context, title).execute(imgUrl);
    }

    public char to_unichr(String val) {
	return (char)Integer.parseInt(val);
    }

    /* Makes multiple attempts to retrieve an image if the first one doesn't work */
    public Bitmap retrieveImage(String addr) {
	Bitmap result = getImageFromURL(addr);
	if(result != null) return result;
	result = getImageFromURL(addr);
	if(result != null) return result;
	result = getImageFromURL(addr);
	return result;
    }

    /* Grabs an image from the input url String and returns it in Bitmap form
         -Do not pass a null url */
    public Bitmap getImageFromURL(String addr) {
	// This check should probably be somewhere else
	if(addr == null) {
	    return null;
	}
	try {
	    URL url = new URL(addr);
	    URLConnection conn = url.openConnection();
	    InputStream inp = conn.getInputStream();
	    return BitmapFactory.decodeStream(inp);
	} catch (MalformedURLException e) {
	    Log.v("getImage-malfy", addr);
	} catch (IOException e) {
	    Log.v("getImage-io", addr);
	}
	return null;
    }

    /* Retrieves the web page at the input url. 
       On 302 response, go to redirect. */
    public String makeQuery (String url) {
        try {
	    Log.v("make-query", url);
            URL addr = new URL (url);
            HttpURLConnection con=null;
	    int response = -1, i=0;
	    /* Try a few times before giving up. */
	    while(response == -1) {
		if(i == 3) {
		    return null;
		}
		con = (HttpURLConnection)(addr.openConnection ());
		con.setInstanceFollowRedirects(true);
		con.connect();
		response = con.getResponseCode();
		i += 1;
	    }
	    if(response == 302) {
		con = (HttpURLConnection)(new URL(url.substring(0,url.length()-8)+
						  con.getHeaderField("Location")).openConnection());
		con.connect();
	    }
	    InputStream inputStream = con.getInputStream();
	    response = con.getResponseCode();

	    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
	    StringBuilder res = new StringBuilder();
	    String line;
	    while((line = in.readLine()) != null) {
		res.append(line);
	    }
	    in.close();
	    return res.toString();
	} catch (MalformedURLException e) {
            Log.v ("make_query_malformed",e.getMessage ());
            return null;
        } catch (IOException e) {
            Log.v ("make_query_io",e.getMessage ());
            return null;
        } catch (Exception e) {
	    Log.v("unknown", e.getMessage());
	    return null;
	}
    }

    /* Background task for displaying an alt image. */
    private class DisplayAltImageTask extends AsyncTask<String,Integer,Bitmap> {
	protected Dialog context;
	protected String title;

	public DisplayAltImageTask (Dialog context, String title) {
	    this.context = context;
	    this.title = title;
	}

	protected Bitmap doInBackground (String... data) {
	    Bitmap alt = null;
	    if(data[0] != null) {
		alt = retrieveImage(data[0]);
	    }
	    return alt;
	}

	protected void onPreExecute() {
	    super.onPreExecute();
	}

	protected void onProgressUpdate (Integer... prog) {
	}

	protected void onPostExecute (Bitmap result) {
	    if(context.isShowing()) {
		ImageView image = (ImageView) context.findViewById(R.id.alt_image);
		image.setImageBitmap(result);
		context.setTitle(title);
	    }
	}
    }

    /* Background task for grabbing a comic. */
    private class GetComicTask extends AsyncTask<String,Integer,Bitmap> {
	protected Reader context;

	public GetComicTask(Reader context) {
	    this.context = context;
	}

	protected void onPreExecute() {
	    running += 1;
	}

	protected Bitmap doInBackground (String... data) {
	    String page, imgUrl;
	    try{
		page = makeQuery(data[0]);
		if(page == null) {
		    return null;
		}
		imgUrl = this.context.handleRawPage(page);
		return retrieveImage(imgUrl);
	    }catch(Exception e) {
		if(e.getMessage() != null)
		    Log.v("doInBg", e.getMessage());
	    }
	    return null;
        }

	/* Decide what to do with the comic according to the loading state. */
	protected void onPostExecute (Bitmap comic) {
	    running -= 1;
	    if(comic != null) {
		switch(state) {
		case None:
		    state = LoadingState.Current;
		    break;
		case Current:
		    context.setCurrentComic(comic, true);
		    break;
		case Previous:
		    context.setPreviousComic(comic);
		    break;
		case Next:
		    context.setNextComic(comic);
		    break;
		case Done:
		    break;
		}
	    } else {
	        this.context.handleComicError();
	    }
	}

    }

}