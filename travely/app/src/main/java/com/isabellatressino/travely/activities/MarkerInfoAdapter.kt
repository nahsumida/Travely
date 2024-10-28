package com.isabellatressino.travely.activities

import android.content.Context
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

/**
 *  Classe que fornece uma vizualização personalizada para os marcadores no mapa
 *  */
class MarkerInfoAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {
    override fun getInfoWindow(p0: Marker): View? = null

    override fun getInfoContents(marker: Marker): View? = null
}