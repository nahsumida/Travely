package com.isabellatressino.travely.audit.di

import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.audit.service.EventLogger
import com.isabellatressino.travely.audit.infrastructure.FirebaseEventLogger
import com.isabellatressino.travely.audit.store.EventStore
import com.isabellatressino.travely.audit.store.InMemoryEventStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuditModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideEventLogger(firebaseEventLogger: FirebaseEventLogger): EventLogger =
        firebaseEventLogger

    @Provides
    @Singleton
    fun provideEventStore(): EventStore = InMemoryEventStore()
}