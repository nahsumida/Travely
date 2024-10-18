package com.isabellatressino.travely.models

import com.google.firebase.firestore.GeoPoint

class Place(
    val name: String,
    val address: String,
    val decription: String,
    val type: String,
    val rate: Double,
    val businessHours: Array<String>,
    val geopoint: GeoPoint,
    val profiles: Array<String>
) {}