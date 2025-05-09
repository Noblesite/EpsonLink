package com.noblesite.epsonlink.web;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.noblesite.epsonlink.viewmodel.PrinterViewModel;

public class EpsonLinkWebViewClient extends WebViewClient {

    private static final String TAG = "EpsonLinkWebClient";
    private final PrinterViewModel viewModel;
    private final String epsonLinkUrl;
    private final Handler handler = new Handler();
    private final int reloadDelayMs = 15000;

    public EpsonLinkWebViewClient(PrinterViewModel viewModel, String epsonLinkUrl) {
        this.viewModel = viewModel;
        this.epsonLinkUrl = epsonLinkUrl;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.i(TAG, "onPageStarted: " + url);
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.i(TAG, "onPageFinished: " + url);
        super.onPageFinished(view, url);
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        int statusCode = errorResponse.getStatusCode();
        String errorUrl = request.getUrl().toString();

        if (!errorUrl.contains("favicon.ico")) {
            Log.e(TAG, "onReceivedHttpError: " + statusCode + ", URL: " + errorUrl);

            String fallbackUrl = "file:///android_asset/error.html";

            switch (statusCode) {
                case 401:
                    Log.w(TAG, "Unauthorized - User may need to log in.");
                    break;
                case 403:
                    Log.w(TAG, "Forbidden - Access denied.");
                    break;
                case 404:
                    Log.w(TAG, "Not Found - The resource doesn't exist.");
                    break;
                case 408:
                    Log.w(TAG, "Request Timeout - Server took too long to respond.");
                    break;
                case 429:
                    Log.w(TAG, "Too Many Requests - Rate limiting in effect.");
                    break;
                case 500:
                    Log.w(TAG, "Internal Server Error - Something broke.");
                    break;
                case 502:
                case 503:
                case 504:
                    Log.w(TAG, "Service temporarily unavailable. Retrying...");
                    break;
                default:
                    Log.w(TAG, "Unhandled HTTP error code: " + statusCode);
                    break;
            }

            view.loadUrl(fallbackUrl);

            handler.postDelayed(() -> {
                Log.i(TAG, "Retrying URL after delay: " + epsonLinkUrl);
                view.loadUrl(epsonLinkUrl);
            }, reloadDelayMs);
        }
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        int statusCode = error.getErrorCode();
        String errorUrl = request.getUrl().toString();

        if (!errorUrl.contains("favicon.ico")) {
            Log.e(TAG, "onReceivedHttpError: " + statusCode + ", URL: " + errorUrl);

            String fallbackUrl = "file:///android_asset/error.html";

            switch (statusCode) {
                case 401:
                    Log.w(TAG, "Unauthorized - User may need to log in.");
                    break;
                case 403:
                    Log.w(TAG, "Forbidden - Access denied.");
                    break;
                case 404:
                    Log.w(TAG, "Not Found - The resource doesn't exist.");
                    break;
                case 408:
                    Log.w(TAG, "Request Timeout - Server took too long to respond.");
                    break;
                case 429:
                    Log.w(TAG, "Too Many Requests - Rate limiting in effect.");
                    break;
                case 500:
                    Log.w(TAG, "Internal Server Error - Something broke.");
                    break;
                case 502:
                case 503:
                case 504:
                    Log.w(TAG, "Service temporarily unavailable. Retrying...");
                    break;
                default:
                    Log.w(TAG, "Unhandled HTTP error code: " + statusCode);
                    break;
            }

            view.loadUrl(fallbackUrl);

            handler.postDelayed(() -> {
                Log.i(TAG, "Retrying URL after delay: " + epsonLinkUrl);
                view.loadUrl(epsonLinkUrl);
            }, reloadDelayMs);
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        Log.i(TAG, "Intercepted URL: " + url);

        if (url.contains("/action=Status?")) {
            viewModel.connectPrinter();
            viewModel.checkPrinterStatus();
            return true;
        } else if (url.contains("/action=Print&job=")) {
            String jobPayload = Uri.parse(url).getQueryParameter("job");
            if (jobPayload != null) {
                viewModel.sendPrintJob(jobPayload);
            }else{
                Log.e(TAG, "Print job data missing");
            }

            Log.i(TAG, "Intercepted Print Action: " + url);
            return true;
        }

        return super.shouldOverrideUrlLoading(view, request);
    }
}
