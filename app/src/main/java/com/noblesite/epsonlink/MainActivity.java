package com.noblesite.epsonlink;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.noblesite.epsonlink.viewmodel.PrinterViewModel;
import com.noblesite.epsonlink.web.EpsonLinkWebViewClient;
import com.noblesite.epsonlink.web.CustomWebChromeClient;

public class MainActivity extends AppCompatActivity {

    private static final String USB_PERMISSION_ACTION = "com.noblesite.epsonlink.USB_PERMISSION";
    private PrinterViewModel viewModel;
    private final BroadcastReceiver usbPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (USB_PERMISSION_ACTION.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null && intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    Log.i("MainActivity", "USB permission granted.");
                    viewModel.setUsbPermissionGranted(true);
                } else {
                    Log.e("MainActivity", "USB permission denied.");
                    viewModel.setUsbPermissionGranted(false);
                }
            }
        }
    };

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(PrinterViewModel.class);

        IntentFilter filter = new IntentFilter(USB_PERMISSION_ACTION);
        registerReceiver(usbPermissionReceiver, filter, Context.RECEIVER_EXPORTED);

        webView = findViewById(R.id.epsonLink);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebViewClient(new EpsonLinkWebViewClient(viewModel, ""));
        webView.setWebChromeClient(new CustomWebChromeClient());

        viewModel.getWebUrlToLoad().observe(this, url -> {
            if (url != null && !url.isEmpty()) {
                webView.loadUrl(url);
            }
        });

        viewModel.loadAppConfig();
        //TODO: Update to not hardcode vendor ID
        viewModel.requestUsbPermissionIfNeeded(1208);

        viewModel.connectPrinter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbPermissionReceiver);
    }
}
