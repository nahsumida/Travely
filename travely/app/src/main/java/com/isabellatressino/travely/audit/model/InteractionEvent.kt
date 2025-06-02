package com.isabellatressino.travely.audit.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class InteractionEvent(
    val userId: String,
    val type: String,
    val timestamp: Timestamp = Timestamp.now(),
    val placeId: String,
    val placeName: String?,
    val geoPoint: GeoPoint?,
    val subtypes: List<String>?
)