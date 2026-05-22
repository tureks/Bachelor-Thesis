# Firmware — BLE Flow Meter

Arduino sketch for the **Seeed XIAO nRF52840** measuring device.
Reads pulse output from the velocity probe, computes velocity, and streams data to the Android app over BLE.

---

## Hardware

| Pin | Role |
|-----|------|
| `D7` (`DEVICE_PIN`) | Probe wire detect — LOW when probe is physically attached |
| `D8` (`SIGNAL_PIN`) | Pulse input from the probe (falling-edge interrupt) |
| `D9` (`BUTTON_PIN`) | User button — short press toggles BLE advertising, long press triggers deep sleep |
| `PIN_VBAT` / `VBAT_ENABLE` | Battery ADC |

---

## Velocity Algorithm

The probe generates one pulse per full rotation of the propeller. Velocity is computed from rotation frequency:

```
velocity [m/s] = K × RPS
K = 0.02728   (probe calibration constant)
RPS = 1 / T   (rotations per second)
```

**Pulse timing:** an ISR (interrupt service routine) on `SIGNAL_PIN` records the interval between the last four falling edges in a circular buffer (`pulse_differences[4]`).

**Zero detection:** if no pulse arrives within 70 ms, velocity is set to 0.

**Send rate:** velocity is sent over BLE every 100 ms. Between sends, multiple pulse events are averaged so the transmitted value is a mean over the interval.

---

## BLE Service

Built with the **Adafruit Bluefruit** library.

| Characteristic | Format | Rate |
|---|---|---|
| Velocity | UTF-8 string `"%.2f"` m/s | 100 ms |
| Battery Level | uint8 % | 10 s |
| Probe Status | uint8 (0 = detached, 1 = attached) | 1 s |

All three characteristics use BLE notifications (CCCD).

Advertising stops after 60 s if no connection is made.

---

## Power Management

- **Deep sleep:** Disables GPIO, Serial and BLE. Pressing the button triggers a hardware reset and the sketch restarts.
- **Watchdog:** 10-second hardware watchdog. Reset every loop iteration.
- **Low battery:** below 3.0 V the device flashes the LED three times and enters deep sleep.
- **Battery gauge:** 16-sample ADC average mapped through a 21-point Li-Ion discharge curve to percentage.

---

## Build

- **IDE:** Arduino IDE or Arduino CLI
- **Board package:** Seeed nRF52 Boards
- **Required libraries:**
  - `Adafruit_TinyUSB`
  - Adafruit Bluefruit nRF52 library
- **Board target:** Seeed XIAO nRF52840
