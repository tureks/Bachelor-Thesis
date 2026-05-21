# Water Flow 

The app measures flow rate a water courses using the mid-section method. You connect a Bluetooth flow meter, divide the stream profile into segments, capture velocity at each segment, and the app calculates total flow.

**Flow:** Home → connect device → new measurement → capture segments → complete → save

---

## Home

The main screen of the app. Shows your connection status in the top bar including a probe connection identifier.

- **Connect / Battery** — top-right button opens the Device screen. Shows battery percent when connected.
- **Probe indicator** — blue check means that probe is attached; red triangle represents probe missing (you can still navigate but cannot capture).
- **New Measurement** — starts a fresh measurement. If one is already in progress you'll be asked to discard it first.
- **Continue Measurement** — appears when a measurement is in progress; resumes where you left off.
- **Measurement History** — browse past saved measurements.
- **Settings** — profile, units, capture mode, developer mode.

> You must be connected to a device before starting a measurement.

---

## Device

Scan devices and connect to the flow meter over Bluetooth.

1. grant Bluetooth permission and enable Bluetooth.
2. **Scan** to discover nearby devices. Previously connected devices appear at the top.
3. Tap a device to connect. Tap it again to disconnect.

The connected device banner turns blue when connected.

---

## Measurement — Segment Capture

Captures velocity readings for one segment of the stream.

**live window average** — the mean velocity over the selected time window. Min/Max values and a scrolling graph shown below.

**Capturing a point:** Tap the velocity average or on the graph to record the current velocity point value. Points appear in the list below.

**Editing a point:** Tap any point in the list to change its depth or delete it.

**Time window:** Gear icon (bottom-left) opens a dialog to set time of the rolling average window.

**Complete Segment →** move to the next step.

**Cancel** (top-right) discards the measurement, returns home.

---

## Complete Segment

Review and insert the dimensions of the current segment.

- **Segment Flow** — live preview.
- **Width / Depth** — enter the segment's dimensions.
- **Velocity Points** — tap a point to edit or delete it. Use the checkboxes to include or exclude points; unchecked points aren't stored.

**Complete** — saves the segment.  
**Next Segment →** — goes to the next segment.

---

## Review Segments - Complete Measurement

Shows all captured segments and the **Total Stream Flow**.

- Tap any segments velocity point to open an edit dialog where you can change its width, depth, or individual velocities.
- The total flow updates automatically after any edit.

**Save Measurement →** to store the measurement.  
**Cancel** discards everything.

---

## Save Measurement
Provide information about the measurement.
| Field | Notes                                        |
|---|----------------------------------------------|
| Total Flow / Width / Depth | Summary of all segments — read-only          |
| Location | Auto-detected GPS coordinates, shown if available |
| Name | Required. E.g. "Test measurement"            |
| Notes | Optional text notes                          |

**Save Measurement** to save it.  
**Cancel** discards the measurement.

---

## History

Browse all saved measurements.

- **Search** — filter by name and note.
- **Date filter** — calendar allows to filter from a specific date.
- **Tap** a measurement to open its details.
- **Long-press** a measurement to select one or more measurements to **save to device** or **share** (export CSV and share).

---

## Measurement Details

Full view of a saved measurement.

- **Edit (pencil icon)** — change the name or notes.
- **Delete (bin icon)** — removes the measurement.
- **Download / Share icons** — export as CSV (save to device or share).
- **Segments** — tap any segment to edit its dimensions or velocity points.

---

## Settings

### Profile
Enter your first name, last name, and email to provide information for export. **Save Profile**.

### Display Units
- **Hydrometric** — widths/depths in cm, flow in l/s
- **Metric** — widths/depths in m, flow in m³/s

### Velocity Capture
- **Multi-point** (default) — capture velocity at multiple depths per segment.
- **Single-point** — to use standard one point measurement - predefined depth

### Velocity Capture
- **Developer mode** — allows to start measurements without connecting to a device.