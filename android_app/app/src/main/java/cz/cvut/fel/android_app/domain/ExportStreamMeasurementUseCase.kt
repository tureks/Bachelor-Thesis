package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportStreamMeasurementUseCase(
    private val repository: StreamMeasurementRepository
) {
    /**
     * Generates a CSV report for one or more stream measurements.
     * @param measurementIds IDs of the exported measurements
     * @param unit display unit for lengths and flow values
     * @param operatorName operator name written into the report header
     * @param contactEmail contact email written into the report header
     * @return CSV string
     */
    suspend operator fun invoke(
        measurementIds: List<Int>,
        unit: MeasurementUnit,
        operatorName: String = "",
        contactEmail: String = ""
    ): String {
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        sb.append("\"PROPERTY\",\"VALUE\"\n")
        sb.append("\"Report Type\",\"Stream Gauging Measurement Report\"\n")
        sb.append("\"Export Date\",\"${dateFormat.format(Date())}\"\n")
        if (operatorName.isNotEmpty()) sb.append("\"Operator\",${csvField(operatorName)}\n")
        if (contactEmail.isNotEmpty()) sb.append("\"Contact\",${csvField(contactEmail)}\n")
        sb.append("\n")

        for (measurementId in measurementIds) {
            val measurement = repository.getById(measurementId) ?: continue
            val segments = repository.getSegments(measurementId)

            sb.append("\"--- MEASUREMENT START ---\"\n")
            sb.append("\"Measurement Name\",${csvField(measurement.name)}\n")
            sb.append("\"Timestamp\",\"${dateFormat.format(Date(measurement.measureTimestamp))}\"\n")
            sb.append("\"GPS Latitude\",\"${measurement.gpsLat ?: ""}\"\n")
            sb.append("\"GPS Longitude\",\"${measurement.gpsLong ?: ""}\"\n")
            sb.append("\"Note\",${csvField(measurement.note ?: "")}\n")
            sb.append("\n")

            val distLabel = if (unit == MeasurementUnit.HYDROMETRIC) "cm" else "m"
            val flowLabel = if (unit == MeasurementUnit.HYDROMETRIC) "L/s" else "m3/s"

            sb.append("\"TOTALS SUMMARY\"\n")
            sb.append("\"Total Flow ($flowLabel)\",\"Total Width ($distLabel)\",\"Max Depth ($distLabel)\"\n")
            sb.append("${formatFlow(measurement.totalFlow, unit)},${formatDist(measurement.totalWidth, unit)},${formatDist(measurement.maxDepth, unit)}\n")
            sb.append("\n")

            sb.append("\"SEGMENT DATA\"\n")
            sb.append("\"Segment No\",\"Width ($distLabel)\",\"Depth ($distLabel)\",\"Avg Velocity (m/s)\",\"Segment Flow ($flowLabel)\"\n")
            segments.forEach { seg ->
                sb.append("${seg.segmentNumber},${formatDist(seg.segmentWidth, unit)},${formatDist(seg.depth, unit)},${formatVelocity(seg.averageVelocity)},${formatFlow(seg.segmentFlow, unit)}\n")
            }
            sb.append("\n")

            sb.append("\"RAW VELOCITY READINGS\"\n")
            sb.append("\"Segment No\",\"Velocity (m/s)\",\"Measure Height\"\n")
            segments.forEach { seg ->
                val points = repository.getVelocityPoints(seg.id)
                points.forEach { pt ->
                    sb.append("${seg.segmentNumber},${formatVelocity(pt.velocity)},${formatHeight(pt.measureHeight)}\n")
                }
            }
            sb.append("\"--- MEASUREMENT END ---\"\n")
            sb.append("\n\n")
        }

        return sb.toString()
    }

    suspend operator fun invoke(
        measurementId: Int,
        unit: MeasurementUnit,
        operatorName: String = "",
        contactEmail: String = ""
    ): String = invoke(listOf(measurementId), unit, operatorName, contactEmail)

    private fun formatFlow(m3s: Double?, unit: MeasurementUnit): String {
        if (m3s == null) return ""
        val value = if (unit == MeasurementUnit.HYDROMETRIC) m3s * 1000.0 else m3s
        return String.format(Locale.US, "%.2f", value)
    }

    private fun formatDist(m: Double?, unit: MeasurementUnit): String {
        if (m == null) return ""
        val value = if (unit == MeasurementUnit.HYDROMETRIC) m * 100.0 else m
        return String.format(Locale.US, if (unit == MeasurementUnit.HYDROMETRIC) "%.1f" else "%.3f", value)
    }

    private fun formatVelocity(ms: Double?): String {
        if (ms == null) return ""
        return String.format(Locale.US, "%.2f", ms)
    }

    private fun formatHeight(percent: Double?): String {
        if (percent == null) return ""
        return String.format(Locale.US, "%.1f", percent / 100.0)
    }

    private fun csvField(value: String): String = "\"${value.replace("\"", "\"\"")}\""
}
