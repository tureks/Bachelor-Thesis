# Bachelor Thesis — Stream Flow Measurement System

System for measuring stream discharge (flow rate) using the mid-section velocity-area method.
Consists of two software components: an Android app and firmware for the BLE measuring device.

---

## Android App — `android_app/`

Jetpack Compose app that connects to the measuring device over BLE, guides the user through segment capture, and computes total flow.

- **User guide:** [`android_app/README.md`](android_app/README.md)
- **Code documentation (Dokka):** generate with `./gradlew :app:dokkaHtml` inside `android_app/`, then open `android_app/app/build/dokka/html/index.html`

---

## Firmware — `waterFlow/`

Arduino sketch for the nRF52-based BLE measuring device. Reads the velocity probe via pulse counting and streams data to the Android app.

- **Programmer guide:** [`waterFlow/README.md`](waterFlow/README.md)