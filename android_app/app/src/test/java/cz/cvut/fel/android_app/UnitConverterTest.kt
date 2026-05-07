package cz.cvut.fel.android_app

import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.ui.utils.UnitConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {

    @Test
    fun metersToDisplay_metric_returnsUnchanged() {
        assertEquals(1.5, UnitConverter.metersToDisplay(1.5, MeasurementUnit.METRIC), 0.0)
    }

    @Test
    fun metersToDisplay_hydrometric_multipliesBy100() {
        assertEquals(150.0, UnitConverter.metersToDisplay(1.5, MeasurementUnit.HYDROMETRIC), 0.0)
    }

    @Test
    fun m3sToDisplay_metric_returnsUnchanged() {
        assertEquals(0.25, UnitConverter.m3sToDisplay(0.25, MeasurementUnit.METRIC), 0.0)
    }

    @Test
    fun m3sToDisplay_hydrometric_multipliesBy1000() {
        assertEquals(250.0, UnitConverter.m3sToDisplay(0.25, MeasurementUnit.HYDROMETRIC), 0.0)
    }

    @Test
    fun displayToMeters_hydrometric_roundTrip() {
        val original = 1.23
        val display = UnitConverter.metersToDisplay(original, MeasurementUnit.HYDROMETRIC)
        val back = UnitConverter.displayToMeters(display, MeasurementUnit.HYDROMETRIC)
        assertEquals(original, back, 1e-10)
    }

    @Test
    fun metersToInput_hydrometric_trimsTrailingZeros() {
        assertEquals("150", UnitConverter.metersToInput(1.5, MeasurementUnit.HYDROMETRIC))
    }

    @Test
    fun metersToInput_metric_trimsTrailingDot() {
        assertEquals("2", UnitConverter.metersToInput(2.0, MeasurementUnit.METRIC))
    }

    @Test
    fun metersToInput_metric_keepsSignificantDecimals() {
        assertEquals("1.5", UnitConverter.metersToInput(1.5, MeasurementUnit.METRIC))
    }

    @Test
    fun formatLength_hydrometric_appendsCm() {
        assertEquals("150 cm", UnitConverter.formatLength(1.5, MeasurementUnit.HYDROMETRIC))
    }

    @Test
    fun formatLength_metric_appendsM() {
        assertEquals("1.50 m", UnitConverter.formatLength(1.5, MeasurementUnit.METRIC))
    }

    @Test
    fun formatFlow_hydrometric_convertsToLitresPerSecond() {
        assertEquals("500.0 l/s", UnitConverter.formatFlow(0.5, MeasurementUnit.HYDROMETRIC))
    }

    @Test
    fun formatFlow_metric_appendsM3s() {
        assertEquals("0.50 m³/s", UnitConverter.formatFlow(0.5, MeasurementUnit.METRIC))
    }

    @Test
    fun lengthLabel_hydrometric_returnsCm() {
        assertEquals("cm", UnitConverter.lengthLabel(MeasurementUnit.HYDROMETRIC))
    }

    @Test
    fun lengthLabel_metric_returnsM() {
        assertEquals("m", UnitConverter.lengthLabel(MeasurementUnit.METRIC))
    }

    @Test
    fun flowLabel_hydrometric_returnsLs() {
        assertEquals("l/s", UnitConverter.flowLabel(MeasurementUnit.HYDROMETRIC))
    }
}
