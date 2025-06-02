package com.isabellatressino.travely.audit.infrastructure

import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.audit.model.InteractionEvent
import com.isabellatressino.travely.audit.service.EventLogger
import javax.inject.Inject
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await

/*
Implements EventLogger interface for saving log into database
 */
class FirebaseEventLogger @Inject constructor(
    private val db: FirebaseFirestore
) : EventLogger {

    override suspend fun log(event: InteractionEvent) {
        val userDocument = getUserDocumentByAuthId(event.userId)
        userDocument?.let {
            addEventToUserInteractionEvents(it, event)
        } ?: run {
            Log.e("log", "Usuário não encontrado com authId: ${event.userId}")
        }
    }
    private suspend fun getUserDocumentByAuthId(authId: String): DocumentSnapshot? {
        return try {
            db.collection("users")
                .whereEqualTo("authID", authId)
                .get()
                .await()
                .documents
                .firstOrNull()
        } catch (err: Exception) {
            Log.e("GetUserDocumentByAuthId", "Erro ao buscar usuário: ${err.localizedMessage}")
            null
        }
    }

    private suspend fun addEventToUserInteractionEvents(userDocument: DocumentSnapshot, event: InteractionEvent) {
        try {
            userDocument.reference.collection("interactionEvents")
                .add(event)
                .await()
        } catch (e: Exception) {
            Log.e("addEventToUserInteractionEvents","Erro ao adicionar evento: ${e.localizedMessage}")
        }
    }
}