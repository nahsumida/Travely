package com.isabellatressino.travely.audit.store

import com.isabellatressino.travely.audit.model.InteractionEvent
import javax.inject.Inject

class InMemoryEventStore @Inject constructor(): EventStore {
    private val pendingEvents = mutableListOf<InteractionEvent>()

    override fun addEvent(event: InteractionEvent) {
        pendingEvents.add(event)
    }

    override fun getAndClearEvents(): List<InteractionEvent> {
        val events = pendingEvents.toList()
        pendingEvents.clear()
        return events
    }

    override fun getSize(): Int {
        return pendingEvents.size
    }
}