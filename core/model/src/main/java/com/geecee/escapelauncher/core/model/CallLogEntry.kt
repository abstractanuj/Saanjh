package com.geecee.escapelauncher.core.model

data class CallLogEntry(
    val id: Long,
    val number: String,
    val name: String?,
    val timestamp: Long,
    val type: Int, // From CallLog.Calls (INCOMING, OUTGOING, MISSED)
    val isReturned: Boolean = false
)
