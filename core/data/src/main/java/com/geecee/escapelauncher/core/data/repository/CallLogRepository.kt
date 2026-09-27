package com.geecee.escapelauncher.core.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import androidx.core.content.ContextCompat
import com.geecee.escapelauncher.core.model.CallLogEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallLogRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getMissedCalls(hoursLimit: Int = 12): Flow<List<CallLogEntry>> = flow {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            emit(emptyList())
            return@flow
        }

        val now = System.currentTimeMillis()
        val timeLimit = now - (hoursLimit * 60 * 60 * 1000L)

        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE
        )
        
        // Fetch all calls in the last 12 hours to determine if a missed call was returned
        val selection = "${CallLog.Calls.DATE} >= ?"
        val selectionArgs = arrayOf(timeLimit.toString())
        val sortOrder = "${CallLog.Calls.DATE} DESC"

        val allCalls = mutableListOf<CallRecord>()

        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(CallLog.Calls._ID)
            val numberColumn = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val nameColumn = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val dateColumn = cursor.getColumnIndex(CallLog.Calls.DATE)
            val typeColumn = cursor.getColumnIndex(CallLog.Calls.TYPE)

            while (cursor.moveToNext()) {
                allCalls.add(
                    CallRecord(
                        id = cursor.getLong(idColumn),
                        number = cursor.getString(numberColumn),
                        name = cursor.getString(nameColumn),
                        timestamp = cursor.getLong(dateColumn),
                        type = cursor.getInt(typeColumn)
                    )
                )
            }
        }

        // Process calls: Group by number, find latest missed call, check if returned
        val missedCallsByNumber = allCalls
            .filter { it.type == CallLog.Calls.MISSED_TYPE }
            .groupBy { it.number }
            .mapValues { it.value.first() } // Latest missed call for this number

        val result = missedCallsByNumber.map { (number, latestMissed) ->
            val isReturned = allCalls.any { 
                it.number == number && 
                it.type == CallLog.Calls.OUTGOING_TYPE && 
                it.timestamp > latestMissed.timestamp 
            }
            
            CallLogEntry(
                id = latestMissed.id,
                number = latestMissed.number,
                name = latestMissed.name,
                timestamp = latestMissed.timestamp,
                type = latestMissed.type,
                isReturned = isReturned
            )
        }.sortedByDescending { it.timestamp }

        emit(result)
    }.flowOn(Dispatchers.IO)

    fun getLastCallDetails(numbers: List<String>): Flow<Map<String, CallLogEntry>> = flow {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED || numbers.isEmpty()) {
            emit(emptyMap())
            return@flow
        }

        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE
        )
        
        val selection = if (numbers.size <= 20) {
            val placeholders = numbers.joinToString(",") { "?" }
            "${CallLog.Calls.NUMBER} IN ($placeholders)"
        } else {
            null
        }
        val selectionArgs = if (numbers.size <= 20) {
            numbers.toTypedArray()
        } else {
            null
        }

        val sortOrder = "${CallLog.Calls.DATE} DESC"
        val lastCalls = mutableMapOf<String, CallLogEntry>()

        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(CallLog.Calls._ID)
            val numberColumn = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val nameColumn = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val dateColumn = cursor.getColumnIndex(CallLog.Calls.DATE)
            val typeColumn = cursor.getColumnIndex(CallLog.Calls.TYPE)

            while (cursor.moveToNext()) {
                val number = cursor.getString(numberColumn)
                if (numbers.contains(number) && !lastCalls.containsKey(number)) {
                    lastCalls[number] = CallLogEntry(
                        id = cursor.getLong(idColumn),
                        number = number,
                        name = cursor.getString(nameColumn),
                        timestamp = cursor.getLong(dateColumn),
                        type = cursor.getInt(typeColumn)
                    )
                }
                if (lastCalls.size == numbers.size) break
            }
        }
        emit(lastCalls)
    }.flowOn(Dispatchers.IO)

    private data class CallRecord(
        val id: Long,
        val number: String,
        val name: String?,
        val timestamp: Long,
        val type: Int
    )
}
