package cz.cvut.fel.android_app.ui.utils

import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import java.util.Locale

object UnitConverter {

    // SI → display value
    fun metersToDisplay(meters: Double, unit: MeasurementUnit): Double =
        if (unit == MeasurementUnit.HYDROMETRIC) meters * 100.0 else meters

    fun m3sToDisplay(m3s: Double, unit: MeasurementUnit): Double =
        if (unit == MeasurementUnit.HYDROMETRIC) m3s * 1000.0 else m3s

    // display → SI
    fun displayToMeters(value: Double, unit: MeasurementUnit): Double =
        if (unit == MeasurementUnit.HYDROMETRIC) value / 100.0 else value

    // Unit labels
    fun lengthLabel(unit: MeasurementUnit): String =
        if (unit == MeasurementUnit.HYDROMETRIC) "cm" else "m"

    fun flowLabel(unit: MeasurementUnit): String =
        if (unit == MeasurementUnit.HYDROMETRIC) "l/s" else "m³/s"

    // Edit input: SI meters → trimmed string in display units (no unit suffix)
    fun metersToInput(meters: Double, unit: MeasurementUnit): String {
        val v = metersToDisplay(meters, unit)
        return String.format(Locale.US, "%.3f", v).trimEnd('0').trimEnd('.')
    }

    // Formatted display strings
    fun formatLength(meters: Double, unit: MeasurementUnit): String {
        val v = metersToDisplay(meters, unit)
        return if (unit == MeasurementUnit.HYDROMETRIC)
            String.format(Locale.US, "%.0f cm", v)
        else
            String.format(Locale.US, "%.2f m", v)
    }

    fun formatFlow(m3s: Double, unit: MeasurementUnit, decimals: Int = 2): String {
        val v = m3sToDisplay(m3s, unit)
        val d = if (unit == MeasurementUnit.HYDROMETRIC) 1 else decimals
        return String.format(Locale.US, "%.${d}f ${flowLabel(unit)}", v)
    }
}