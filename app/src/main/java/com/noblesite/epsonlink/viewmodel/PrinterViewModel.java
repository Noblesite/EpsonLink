package com.noblesite.epsonlink.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.RestrictionsManager;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.noblesite.epsonlink.model.PrinterStatus;
import com.noblesite.epsonlink.repository.PrinterRepository;

import org.json.JSONObject;

public class PrinterViewModel extends AndroidViewModel {

    private static final String TAG = "PrinterViewModel";

    private final PrinterRepository printerRepository;
    private final MutableLiveData<String> webUrlToLoad = new MutableLiveData<>();
    private final MutableLiveData<String> printerStatusJson = new MutableLiveData<>();
    private final MutableLiveData<Boolean> reloadWebView = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> usbPermissionGranted = new MutableLiveData<>(false);

    public PrinterViewModel(@NonNull Application application) {
        super(application);
        printerRepository = new PrinterRepository(application.getApplicationContext());
    }

    public LiveData<String> getWebUrlToLoad() {
        return webUrlToLoad;
    }

    public LiveData<String> getPrinterStatusJson() {
        return printerStatusJson;
    }

    public LiveData<Boolean> getReloadWebView() {
        return reloadWebView;
    }

    public LiveData<Boolean> getUsbPermissionGranted() {
        return usbPermissionGranted;
    }

    public void loadAppConfig() {
        RestrictionsManager restrictionsManager =
            (RestrictionsManager) getApplication().getSystemService(Context.RESTRICTIONS_SERVICE);
        Bundle appRestrictions = restrictionsManager.getApplicationRestrictions();
        String url = appRestrictions.getString("EpsonLinkUrl", "https://Noblesite.net");
        Log.i(TAG, "loadAppConfig: Web URL set to: " + url);
        webUrlToLoad.postValue(url);
    }

    public void onIntentReceived(Intent intent) {
        if (intent != null && intent.getData() != null) {
            String url = intent.getData().toString();
            Log.i(TAG, "onIntentReceived: URL = " + url);
            webUrlToLoad.postValue(url);
        }
    }

    public void initializePrinter() {
        printerRepository.initializePrinter();
    }

    public void connectPrinter() {
        printerRepository.connectPrinter();
    }

    public void checkPrinterStatus() {
        try {
            JSONObject statusJson = printerRepository.getPrinterStatus();
            printerStatusJson.postValue(statusJson.toString());
        } catch (Exception e) {
            Log.e(TAG, "checkPrinterStatus: Failed to get printer status", e);
            printerStatusJson.postValue("{\"Connection\":0}");
        }
    }

    public void requestUsbPermission(UsbDevice device) {
        printerRepository.requestPermission(getApplication(), device);
    }

    public void setUsbPermissionGranted(boolean granted) {
        usbPermissionGranted.postValue(granted);
        if (granted) {
            initializePrinter();
        }
    }

    public void triggerReload() {
        reloadWebView.postValue(true);
    }

    public void resetReloadFlag() {
        reloadWebView.postValue(false);
    }

    public void requestUsbPermissionIfNeeded(int vendorId) {
        UsbDevice device = printerRepository.findPrinterByVendor(vendorId);
        if (device != null) {
            requestUsbPermission(device);
        } else {
            Log.w(TAG, "No USB printer found with vendor ID: " + vendorId);
        }
    }

    /**
     * Sends a print job to the printer using the Epson SDK.
     * Delegates to PrinterRepository.sendPrintJob(String).
     *
     * @param jobPayload The print job payload, typically JSON or command string.
     */
    public void sendPrintJob(String jobPayload) {
        try {
            printerRepository.sendPrintJob(jobPayload);
        } catch (Exception e) {
            Log.e(TAG, "sendPrintJob: Failed to send print job", e);
        }
    }
}

