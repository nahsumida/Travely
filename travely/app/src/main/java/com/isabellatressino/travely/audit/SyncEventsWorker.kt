package com.isabellatressino.travely.audit

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.audit.store.EventStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

class SyncEventsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val eventStore: EventStore
): CoroutineWorker(appContext, params){

    override suspend fun doWork(): Result {
        val events = eventStore.getAndClearEvents()
        if (events.isEmpty()) return Result.success()

        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        events.forEach { e ->
            val docRef = db.collection("users")
                .document(e.userId).collection("interactionEvents")
                .document()
            batch.set(docRef, e)
        }
        try {
            batch.commit().await()
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}