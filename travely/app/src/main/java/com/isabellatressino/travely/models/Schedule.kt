package com.isabellatressino.travely.models


import com.google.firebase.Timestamp

class Schedule(
    var bookingData: Timestamp,
    var placeID: String,
    var type: String,
    var preco: Float
) {
    override fun toString(): String {
        return "Schedule(bookingData=$bookingData, placeID='$placeID', compra='$type', preco=$preco)"
    }
}