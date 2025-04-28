package com.isabellatressino.travely.audit.store

import com.isabellatressino.travely.audit.model.InteractionEvent

interface EventStore {
    fun addEvent(event: InteractionEvent);
    fun getAndClearEvents(): List<InteractionEvent>;
    fun getSize(): Int
}