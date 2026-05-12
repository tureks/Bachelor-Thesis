package cz.cvut.fel.android_app.fixtures

import cz.cvut.fel.android_app.domain.model.CapturedVelocityPoint
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.domain.model.StreamMeasurement
import cz.cvut.fel.android_app.domain.model.StreamMeasurementStatus
import cz.cvut.fel.android_app.domain.model.StreamSegment
import cz.cvut.fel.android_app.domain.model.UserProfile
import cz.cvut.fel.android_app.domain.model.VelocityPoint

fun measurement(
    id: Int = 1,
    name: String = "Test River",
    note: String? = null,
    measureTimestamp: Long = 1_000_000L,
    status: StreamMeasurementStatus = StreamMeasurementStatus.COMPLETE,
    totalWidth: Double? = null,
    maxDepth: Double? = null,
    totalFlow: Double? = null,
) = StreamMeasurement(
    id = id,
    name = name,
    note = note,
    measureTimestamp = measureTimestamp,
    gpsLat = null,
    gpsLong = null,
    totalWidth = totalWidth,
    maxDepth = maxDepth,
    totalFlow = totalFlow,
    status = status,
)

fun segment(
    id: Int = 1,
    measurementId: Int = 1,
    segmentNumber: Int = 1,
    segmentWidth: Double = 1.0,
    depth: Double = 0.5,
    averageVelocity: Double = 1.0,
    segmentFlow: Double = segmentWidth * depth * averageVelocity,
) = StreamSegment(
    id = id,
    measurementId = measurementId,
    segmentNumber = segmentNumber,
    segmentWidth = segmentWidth,
    depth = depth,
    averageVelocity = averageVelocity,
    segmentFlow = segmentFlow,
)

fun velocityPoint(
    id: Int = 0,
    segmentId: Int = 1,
    velocity: Double = 1.0,
    measureHeight: Double? = null,
) = VelocityPoint(id = id, segmentId = segmentId, velocity = velocity, measureHeight = measureHeight)

fun capturedPoint(velocity: Double = 1.0, measureHeight: Double? = null) =
    CapturedVelocityPoint(velocity = velocity, measureHeight = measureHeight)

fun userProfile(unit: MeasurementUnit = MeasurementUnit.HYDROMETRIC) = UserProfile(
    firstName = "Jan",
    lastName = "Novák",
    email = "jan@example.com",
    multipointMeasurement = true,
    singlePointHeight = 0.6,
    preferredUnit = unit,
)