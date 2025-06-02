package com.isabellatressino.travely.audit.service

import com.isabellatressino.travely.audit.model.InteractionEvent

/*
Interface responsible for recording user interaction into the audit system
 */
interface EventLogger {
    suspend fun log(event: InteractionEvent)
}