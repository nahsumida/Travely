package com.isabellatressino.travely.utils

import BookingManagerClientKT
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Classe BookingHelper: Controla o envio de requisições de reserva e gerencia o fluxo de sucesso
 * e erro.
*/
class BookingHelper {
    private var lockingRequest: Boolean = false

     fun requestBooking(
         context: Context,
         authID: String,
         placeID: String,
         datetime: String,
         amount: Int,
         type: String,
         onSuccess: (List<String>) -> Unit,
         onError: (List<String>) -> Unit) {
        if (lockingRequest) return  // Já há uma requisição em andamento (evita double click)

         lockingRequest = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = BookingManagerClientKT()
                val result = client.sendBookingRequest(authID, placeID, datetime, amount, type)
                val list: List<String> = listOf(*result.split(",").toTypedArray())

                withContext(Dispatchers.Main) {
                    lockingRequest = false
                    if (result.contains("sucesso")) {
                        onSuccess(list)
                    } else {
                        onError(list)
                        Log.e("Resposta do Servidor:", result)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    lockingRequest = false
                }
            }
        }
    }
}