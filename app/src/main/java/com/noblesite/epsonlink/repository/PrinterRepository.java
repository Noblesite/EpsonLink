package com.noblesite.epsonlink.repository;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.HashMap;

public class PrinterRepository {
    private static final String TAG = "PrinterRepository";
    private static final String USB_PERMISSION_ACTION = "com.noblesite.epsonlink.USB_PERMISSION";

    private final Context context;
    private final UsbManager usbManager;
    private UsbDevice targetDevice;
    private Printer mPrinter;
    private String usbTarget;

    public PrinterRepository(Context context) {
        this.context = context.getApplicationContext();
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }

    /**
     * Finds a USB printer by vendor ID.
     * @param vendorId The USB vendor ID to search for.
     * @return The matching UsbDevice, or null if not found.
     */
    public UsbDevice findPrinterByVendor(int vendorId) {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        for (UsbDevice device : deviceList.values()) {
            Log.i(TAG, "USB Device Found: " + device.getDeviceName());
            if (device.getVendorId() == vendorId) {
                targetDevice = device;
                return device;
            }
        }
        return null;
    }

    /**
     * Requests permission for the given USB device.
     * @param context Context for PendingIntent.
     * @param device The UsbDevice to request permission for.
     */
    public void requestPermission(Context context, UsbDevice device) {
        if (device == null) return;
        if (!usbManager.hasPermission(device)) {
            Log.i(TAG, "Requesting USB permission");
            Intent intent = new Intent(USB_PERMISSION_ACTION);
            intent.setPackage(context.getPackageName());
            PendingIntent permissionIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            usbManager.requestPermission(device, permissionIntent);

        } else {
            Log.i(TAG, "USB permission already granted.");

        }
    }

    /**
     * Initializes the Printer object.
     */
    public void initializePrinter() {
        try {
            Log.d(TAG, "Initializing printer");
            // Printer model/type may need to be parameterized
            mPrinter = new Printer(Printer.TM_T88, Printer.MODEL_ANK, null);
        } catch (Epos2Exception e) {
            Log.e(TAG, "Printer initialization failed: " + e.getErrorStatus());
        }
    }

    /**
     * Connects the printer via USB.
     */
    public void connectPrinter() {
        if (targetDevice == null) {
            Log.e(TAG, "No USB device set for connection.");
            return;
        }
        if (mPrinter == null) {
            Log.e(TAG, "Printer not initialized.");
            return;
        }
        usbTarget = "USB: " + targetDevice.getDeviceName();
        try {
            mPrinter.connect(usbTarget, Printer.PARAM_DEFAULT);
            Log.i(TAG, "Printer connected.");
        } catch (Epos2Exception e) {
            Log.e(TAG, "Printer connection failed: " + e.getErrorStatus(), e);
        }
    }

    /**
     * Checks if the printer is connected.
     * @return true if connected, false otherwise.
     */
    public boolean isConnected() {
        if (mPrinter == null) return false;
        PrinterStatusInfo status = mPrinter.getStatus();
        return status.getConnection() == Printer.TRUE;
    }

    /**
     * Gets the printer status as a JSONObject.
     * @return JSONObject with printer status.
     * @throws JSONException if JSON error occurs.
     */
    public JSONObject getPrinterStatus() throws JSONException {
        JSONObject printerStatus = new JSONObject();
        if (mPrinter == null) {
            printerStatus.put("Connection", 0);
            return printerStatus;
        }
        PrinterStatusInfo statusInfo = mPrinter.getStatus();
        printerStatus.put("Connection", statusInfo.getConnection());
        printerStatus.put("CoverOpen", statusInfo.getCoverOpen());
        printerStatus.put("Paper", statusInfo.getPaper());
        printerStatus.put("PaperFeed", statusInfo.getPaperFeed());
        printerStatus.put("ErrorStatus", statusInfo.getErrorStatus());
        printerStatus.put("AutoRecoverError", statusInfo.getAutoRecoverError());
        try {
            mPrinter.disconnect();
        } catch (Epos2Exception e) {
            Log.e(TAG, "Printer disconnect failed: " + e.getErrorStatus());
        }
        return printerStatus;
    }

    /**
     * Returns the current Printer object.
     */
    public Printer getPrinter() {
        return mPrinter;
    }

    /**
     * Sets the target USB device for this repository.
     */
    public void setTargetDevice(UsbDevice device) {
        this.targetDevice = device;
    }

    /**
     * Gets the current target USB device.
     */
    public UsbDevice getTargetDevice() {
        return targetDevice;
    }

    /**
     * Sends a print job to the printer using the Epson SDK.
     * @param jobPayload The text to print.
     */
    public void sendPrintJob(String jobPayload) {
        if (mPrinter == null || targetDevice == null) {
            Log.e(TAG, "sendPrintJob: Printer or target device not initialized");
            return;
        }

        try {
            JSONObject jobJson = new JSONObject(jobPayload);
            JSONArray lines = jobJson.getJSONArray("lines");

            for (int i = 0; i < lines.length(); i++) {
                JSONObject line = lines.getJSONObject(i);
                String type = line.optString("type", "text");

                switch (type) {
                    case "text":
                        String value = line.optString("value", "");
                        mPrinter.addText(value + "\n");
                        break;

                    case "cut":
                        mPrinter.addCut(Printer.CUT_FEED);
                        break;

                    default:
                        Log.w(TAG, "sendPrintJob: Unsupported line type: " + type);
                        break;
                }
            }

            mPrinter.sendData(Printer.PARAM_DEFAULT);
            Log.i(TAG, "sendPrintJob: Print job sent successfully.");

        } catch (Epos2Exception e) {
            Log.e(TAG, "sendPrintJob: Epos2Exception " + e.getErrorStatus(), e);
        } catch (Exception e) {
            Log.e(TAG, "sendPrintJob: Exception", e);
        }
    }
}
