package com.isabellatressino.travely.audit.service

import com.google.firebase.firestore.GeoPoint
import com.isabellatressino.travely.audit.model.InteractionEvent

class MapAuditLogger(
    private val eventLogger: EventLogger,
    private val userId: String
) {
    suspend fun logMarkerClick(
        placeId: String,
        placeName: String,
        geoPoint: GeoPoint,
        subtypes: List<String>
    ) {
        val event = InteractionEvent(
            userId = userId,
            type = "marker_click",
            placeId = placeId,
            placeName = placeName,
            geoPoint = geoPoint,
            subtypes = subtypes
        )
        eventLogger.log(event)
    }
}