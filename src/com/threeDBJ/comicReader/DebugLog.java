package com.threeDBJ.comicReader;

import android.util.Log;

public final class DebugLog {
    static final String TAG = "webComicReader";

    private static boolean loggingEnabled = true;

    private DebugLog() {
    }

    public static void setDebugLogging(boolean enabled) {
        loggingEnabled = enabled;
    }

    public static int v(String tag, String msg) {
        int result = 0;
        if (loggingEnabled) {
            result = Log.v(tag, msg);
        }
        return result;
    }

    public static int v(String tag, String msg, Throwable tr) {
        int result = 0;
        if (loggingEnabled) {
            result = Log.v(tag, msg, tr);
        }
        return result;
    }

    public static int d(String tag, String msg) {
        int result = 0;
        if (loggingEnabled) {
            result = Log.d(tag, msg);
        }
        return result;
    }

    public static int d(String tag, String msg, Throwable tr) {
    	int result = 0;
        if (loggingEnabled) {
            result = Log.d(tag, msg, tr);
        }
        return result;
    }

    public static int i(String tag, String msg) {
    	int result = 0;
        if (loggingEnabled) {
            result = Log.i(tag, msg);
        }
        return result;
    }

    public static int i(String tag, String msg, Throwable tr) {
    	int result = 0;
        if (loggingEnabled) {
            result = Log.i(tag, msg, tr);
        }
        return result;
    }

    public static int w(String tag, String msg) {
    	int result = 0;
        if (loggingEnabled) {
            result = Log.w(tag, msg);
        }
        return result;
    }

    public static int w(String tag, String msg, Throwable tr) {
    	int result = 0;
        if (loggingEnabled) {
            result = Log.w(tag, msg, tr);
        }
        return result;
    }

    public static int w(String tag, Throwable tr) {
    	int result = 0;
        if (loggingEnabled) {
            result = Log.w(tag, tr);
        }
        return result;
    }

    public static int e(String msg) {
	return e(TAG, msg);
    }

    public static int e(String tag, String msg) {
    	int result = 0;
        if (loggingEnabled) {
            result = Log.e(tag, msg);
        }
        return result;
    }

    public static int e(String tag, String msg, Throwable tr) {
    	int result = 0;
        if (loggingEnabled) {
            result = Log.e(tag, msg, tr);
        }
        return result;
    }
}
