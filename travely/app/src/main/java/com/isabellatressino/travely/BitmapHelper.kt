package com.isabellatressino.travely

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

// Este arquivo contém uma função para converter um vetor de recursos de imagem em um BitmapDescriptor,
// que é usado para exibir imagens em mapas no Android usando a API do Google Maps.

/**
 * Object BitmapHelper
 * @author: Isabella
 * Este objeto fornece funções auxiliares para trabalhar com bitmaps,
 * especialmente para converter vetores em bitmaps.
 */
object BitmapHelper {
    /**
     * Converte um recurso vetorial em um BitmapDescriptor.
     * Esta função recebe um contexto e um ID de recurso vetorial e retorna
     * um BitmapDescriptor correspondente ao vetor fornecido. Se o vetor não
     * puder ser carregado, retorna um marcador padrão.
     * @param context O contexto utilizado para acessar os recursos.
     * @param id O ID do recurso do vetor drawable.
     * @return Um BitmapDescriptor representando o vetor como um bitmap.
     */
    fun vectorToBitmap(
        context: Context,
        @DrawableRes id: Int,
        color: Int,
    ): BitmapDescriptor {
        // Obtém o drawable vetorial usando os recursos do contexto.
        val vectorDrawable = ResourcesCompat.getDrawable(context.resources, id, null)
        // Retorna um marcador padrão se o drawable não puder ser carregado.
            ?: return BitmapDescriptorFactory.defaultMarker()

        // Cria um bitmap com as dimensões do drawable vetorial.
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        // Cria um canvas para desenhar no bitmap.
        val canvas = Canvas(bitmap)
        // Define os limites do drawable para se ajustar ao canvas.
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        // Desenha o drawable vetorial no canvas.
        vectorDrawable.draw(canvas)
        // Retorna um BitmapDescriptor criado a partir do bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}