

# 🖨️ EpsonLink

**EpsonLink** is a native Android WebView wrapper application built for enterprise deployments. It bridges modern web-based kiosk apps with Epson receipt printers over USB using the official Epson ePOS2 SDK. Designed with a clean MVVM architecture, EpsonLink supports dynamic printer model and web URL configurations via managed app configs (Android Enterprise / MDM).

---

## ✨ Features

- 🔌 **USB Printer Detection & Permissions**
- 📄 **Structured JSON Print Jobs** with dynamic formatting
- 📡 **Live Status Reporting** (cover open, paper out, connection)
- 🌐 **WebView Wrapper** with error handling & default browser prompt
- 🧠 **MVVM Architecture** with LiveData + Repository separation
- 🛠️ **Fully Configurable** via `app_restrictions.xml`

---

## 🏗 Architecture Overview

```
┌────────────┐       ┌────────────┐      ┌────────────┐
│ MainActivity│◄────►│ ViewModel  │◄────►│ Repository │
└────────────┘       └────────────┘      └────────────┘
     │                  │                    │
     ▼                  ▼                    ▼
 WebView         LiveData events     Epson ePOS2 SDK
 (UI bridge)     & USB state mgmt     + USB APIs
```

---

## 🧩 Configuration

### `app_restrictions.xml`
Supports the following managed configurations:

```xml
<string name="EpsonLinkUrl">https://your-kiosk-url</string>
<string name="EpsonPrinterModel">TM-T88</string>
```

### `device_filter.xml`
Used to define vendor ID of Epson printer:
```xml
<resources>
    <usb-device vendor-id="1208" />
</resources>
```

---

## 🧾 JSON Print Job Format
EpsonLink accepts print jobs from the WebView using a custom intent-based URL like:

```
https://your.app.package/action=Print&job={...}
```

### Sample JSON:
```json
{
  "lines": [
    { "type": "text", "value": "Welcome to EpsonLink!" },
    { "type": "text", "value": "Total: $9.99" },
    { "type": "cut" }
  ]
}
```

---

## 🚫 Offline Handling
- Embedded `error.html` shown for all major HTTP errors
- Includes auto-retry and kiosk-safe messaging

---

## 🔧 Requirements
- Android 8.0+
- Epson ePOS2 SDK (`ePOS2.jar` included)
- USB host-capable device (e.g. Zebra CC605)

---

## 📁 Project Structure (MVVM)
```
├── MainActivity.java
├── viewmodel/
│   └── PrinterViewModel.java
├── repository/
│   └── PrinterRepository.java
├── web/
│   ├── EpsonLinkWebViewClient.java
│   └── CustomWebChromeClient.java
├── model/
│   └── PrinterStatus.java
└── res/
    ├── xml/app_restrictions.xml
    └── xml/device_filter.xml
```

---

## 📦 Deployment Notes
- Designed for **Android Enterprise** environments
- Set `EpsonLink` as the **default browser** via MDM
- Push `app_restrictions.xml` and `device_filter.xml` through Android Enterprise managed configs

---

## 🧠 Future Enhancements
- [ ] Barcode & QR code printing
- [ ] Offline print queue
- [ ] Retry logic with exponential backoff
- [ ] Device diagnostics screen

---

## 👨‍💻 Maintained by
Jonathon Poe — [noblesite.net](https://noblesite.net) | [@Noblesite](https://github.com/Noblesite)

---

## 📄 License
MIT License
