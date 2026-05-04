package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import cz.cvut.fel.android_app.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportStreamMeasurementUseCase(
    private val repository: StreamMeasurementRepository,
    private val userRepository: UserRepository
) {
    /**
     * Generates a standardized CSV report for a stream measurement.
     * Respects user's preferred measurement units (Metric or Hydrometric).
     */
    suspend operator fun invoke(measurementIds: List<Int>): String {
        val user = userRepository.user.first()
        val unit = user?.preferredUnit ?: MeasurementUnit.HYDROMETRIC
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        // 1. User Info / Header
        sb.append("\"PROPERTY\",\"VALUE\"\n")
        sb.append("\"Report Type\",\"Stream Gauging Measurement Report\"\n")
        sb.append("\"Export Date\",\"${dateFormat.format(Date())}\"\n")
        user?.let {
            sb.append("\"Operator\",\"${it.firstName} ${it.lastName}\"\n")
            sb.append("\"Contact\",\"${it.email}\"\n")
        }
        sb.append("\n")

        for (measurementId in measurementIds) {
            val measurement = repository.getById(measurementId) ?: continue
            val segments = repository.getSegments(measurementId)

            sb.append("\"--- MEASUREMENT START ---\"\n")
            sb.append("\"Measurement Name\",\"${measurement.name}\"\n")
            sb.append("\"Timestamp\",\"${dateFormat.format(Date(measurement.measureTimestamp))}\"\n")
            sb.append("\"GPS Latitude\",\"${measurement.gpsLat ?: ""}\"\n")
            sb.append("\"GPS Longitude\",\"${measurement.gpsLong ?: ""}\"\n")
            sb.append("\"Note\",\"${measurement.note ?: ""}\"\n")
            sb.append("\n")

            val distLabel = if (unit == MeasurementUnit.HYDROMETRIC) "cm" else "m"
            val flowLabel = if (unit == MeasurementUnit.HYDROMETRIC) "L/s" else "m3/s"

            // 2. Totals Summary
            sb.append("\"TOTALS SUMMARY\"\n")
            sb.append("\"Total Flow ($flowLabel)\",\"Total Width ($distLabel)\",\"Max Depth ($distLabel)\"\n")
            sb.append("${formatFlow(measurement.totalFlow, unit)},${formatDist(measurement.totalWidth, unit)},${formatDist(measurement.maxDepth, unit)}\n")
            sb.append("\n")

            // 3. Segment Table
            sb.append("\"SEGMENT DATA\"\n")
            sb.append("\"Segment No\",\"Width ($distLabel)\",\"Depth ($distLabel)\",\"Avg Velocity (m/s)\",\"Segment Flow ($flowLabel)\"\n")
            segments.forEach { seg ->
                sb.append("${seg.segmentNumber},${formatDist(seg.segmentWidth, unit)},${formatDist(seg.depth, unit)},${formatVelocity(seg.averageVelocity)},${formatFlow(seg.segmentFlow, unit)}\n")
            }
            sb.append("\n")

            // 4. Raw Point Data
            sb.append("\"RAW VELOCITY READINGS\"\n")
            sb.append("\"Segment No\",\"Velocity (m/s)\",\"Measure Height (%)\"\n")
            segments.forEach { seg ->
                val points = repository.getVelocityPoints(seg.id)
                points.forEach { pt ->
                    sb.append("${seg.segmentNumber},${formatVelocity(pt.velocity)},${pt.measureHeight ?: ""}\n")
                }
            }
            sb.append("\"--- MEASUREMENT END ---\"\n")
            sb.append("\n\n")
        }

        return sb.toString()
    }

    suspend operator fun invoke(measurementId: Int): String {
        return invoke(listOf(measurementId))
    }

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
}
