# Water Flow 

## Overview

The app measures stream discharge (flow rate) using the mid-section method. You connect a Bluetooth flow meter, divide the stream profile into segments, capture velocity at each segment, and the app calculates total flow.

**Flow:** Home → connect device → new measurement → capture segments → complete → save

---

## Home

The main screen of the app. Shows your connection status in the top bar including a probe connection identifier.

- **Connect / Battery** — top-right button opens the Device screen. Shows battery percent when connected.
- **Probe indicator** — blue check means that probe is attached; red triangle represents probe missing (you can still navigate but cannot capture).
- **New Measurement** — starts a fresh measurement. If one is already in progress you'll be asked to discard it first.
- **Continue Measurement** — appears when a measurement is in progress; resumes where you left off.
- **Measurement History** — browse past saved measurements.
- **Settings** — profile, units, capture mode.

> You must be connected to a device before starting a measurement.

---

## Device

Scan devices and connect to the flow meter over Bluetooth.

1. grant Bluetooth permission and enable Bluetooth.
2. Tap **Scan** to discover nearby devices. Previously connected devices appear at the top.
3. Tap a device to connect. Tap it again to disconnect.

The connected device banner turns blue when connected.

---

## Measurement — Segment Capture

Captures velocity readings for one segment of the stream.

TO the screen there is **live window average** — the mean velocity over the selected time window. Min/Max values and a scrolling graph are shown below it.

**Capturing a point:** Tap the velocity reading or anywhere on the graph to record the current average as a velocity point. Points appear in the list below.

**Editing a point:** Tap any point in the list to change its depth position or delete it.

**Time window:** The gear icon (bottom-left) opens a dialog to change how many seconds the rolling average window spans.

When you have enough points, tap **Complete Segment →** to move to the next step.

**Cancel** (top-right) discards the entire measurement and returns home.

---

## Complete Segment

Review and finalize the dimensions for the current segment before saving it.

- **Estimated Segment Flow** — live preview that updates as you change dimensions.
- **Width / Depth** — enter the segment's physical dimensions.
- **Velocity Points** — tap a point to edit or delete it. Use the checkboxes to include or exclude individual points from the average; unchecked points don't contribute to the flow calculation.

**Complete** — saves the segment and goes to the Review screen (use after the last segment).  
**Next Segment →** — saves this segment and returns to Segment Capture to record the next one.

---

## Review Segments - Complete Measurement

Shows all captured segments and the **Total Stream Flow** at the top.

- Tap any segments velocity point to open an edit dialog where you can change its width, depth, or individual velocities.
- The total flow updates automatically after any edit.

Tap **Save Measurement →** when you're satisfied with all segments.  
**Cancel** discards everything and returns home.

---

## Save Measurement

The final step before the measurement is stored.

| Field | Notes                                        |
|---|----------------------------------------------|
| Total Flow / Width / Depth | Summary of all segments — read-only          |
| Location | Auto-detected GPS coordinates, shown if available |
| Name | Required. E.g. "Test measurement"            |
| Notes | Optional text notes                          |

Tap **Save Measurement** to persist the record and return home.  
**Cancel** discards the measurement.

---

## History

Browse all saved measurements.

- **Search** — filter by name and note in real time.
- **Date filter** — tap the calendar chip to filter from a specific date; tap × to clear.
- **Tap** a measurement to open its details.
- **Long-press** a measurement to enter selection mode, then select more and use the toolbar icons to **save to device** (download icon) or **share** (export CSV and share).

---

## Measurement Details

Full view of a saved measurement.

- **Edit (pencil icon)** — change the name or notes.
- **Delete (bin icon)** — permanently removes the measurement.
- **Download / Share icons** — export as CSV (save to device or share).
- **Segments list** — tap any segment to edit its dimensions or velocity points.

---

## Settings

### Profile
Enter your first name, last name, and email. These are embedded in exported CSV files as operator information. Tap **Save Profile**.

### Display Units
- **Hydrometric** — widths/depths in cm, flow in l/s
- **Metric** — widths/depths in m, flow in m³/s

Switching units affects all displays and exports immediately.

### Velocity Capture
- **Multi-point** (default) — capture velocity at multiple depths per segment; assign each point its own depth position.
- **Single-point** — one reading per segment at a fixed depth fraction. Use the slider to set the fraction.