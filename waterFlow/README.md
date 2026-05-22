# Firmware — BLE Flow Meter

Arduino sketch for the **nRF52840** (Adafruit Feather nRF52840) measuring device.
Reads pulse output from the velocity probe, computes velocity, and streams data to the Android app over BLE.

---

## Hardware

| Pin | Role |
|-----|------|
| `D7` (`DEVICE_PIN`) | Probe wire detect — LOW when probe is physically attached |
| `D8` (`SIGNAL_PIN`) | Pulse input from the probe (falling-edge interrupt) |
| `D9` (`BUTTON_PIN`) | User button — short press toggles BLE advertising, long press (≥ 1 s) triggers deep sleep |
| `PIN_VBAT` / `VBAT_ENABLE` | Battery ADC (nRF52 built-in) |

---

## Velocity Algorithm

The probe generates one pulse per full rotation of the impeller. Velocity is computed from rotation frequency:

```
velocity [m/s] = K × RPS
K = 0.02728   (probe calibration constant)
RPS = 1 / T   (rotations per second)
```

**Pulse timing:** an ISR on `SIGNAL_PIN` records the interval between the last four falling edges in a circular buffer (`pulse_differences[4]`). `T` is the average of those four intervals — smoothing burst noise while reacting quickly to speed changes.

**Zero detection:** if no pulse arrives within `NO_PULSE_TIMEOUT_MS = 70 ms`, velocity is set to 0. This threshold corresponds to ~0.39 m/s minimum detectable velocity.

**Send rate:** velocity is sent over BLE every 100 ms. Between sends, multiple pulse events are averaged so the transmitted value is a mean over the interval.

---

## BLE Service

Built with the **Adafruit Bluefruit** library (`bluefruit.h`).

| Characteristic | UUID | Format | Rate |
|---|---|---|---|
| Velocity | `0f6866f4-…` | UTF-8 string `"%.2f"` m/s | 100 ms |
| Battery Level | standard `0x180F / 0x2A19` | uint8 % | 10 s |
| Probe Status | `736f5af6-…` | uint8 (0 = detached, 1 = attached) | 1 s |

All three characteristics use BLE notifications (CCCD). The custom flow service UUID is `a177eaf2-c661-4f76-b07d-36826eca67bd`.

Advertising stops after `BLE_TIMEOUT_MS = 60 000 ms` if no connection is made. A user-initiated disconnect (reason `0x13`) does not restart advertising automatically — the button must be pressed again.

---

## Power Management

- **Deep sleep:** `NRF_POWER->SYSTEMOFF = 1` (≈ 0.4 µA). Wake source: `INPUT_PULLUP_SENSE` on the button pin (nRF52 GPIO sense mechanism).
- **Watchdog:** 10-second hardware watchdog (`NRF_WDT`, 32 768 Hz × 10 = 327 680 CRV). Pet in every `loop()` iteration.
- **Low battery:** below 3.0 V the device flashes the LED three times and enters deep sleep.
- **Battery gauge:** 16-sample ADC average mapped through a 21-point Li-Ion discharge curve to percentage.

---

## Build

- **IDE:** Arduino IDE or Arduino CLI
- **Board package:** Adafruit nRF52 (`adafruit:nrf52`)
- **Required libraries:**
  - `Adafruit_TinyUSB`
  - Adafruit Bluefruit nRF52 library (bundled with the board package)
- **Board target:** Adafruit Feather nRF52840 Express