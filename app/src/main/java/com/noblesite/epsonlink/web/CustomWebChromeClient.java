package com.noblesite.epsonlink.web;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class CustomWebChromeClient extends WebChromeClient {

    private static final String TAG = "CustomWebChromeClient";

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        // Do nothing: prevent default favicon behavior
        Log.d(TAG, "Favicon request ignored.");
    }

    // Optional: override other ChromeClient events (e.g., onProgressChanged, onConsoleMessage)
}
