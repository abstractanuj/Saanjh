package com.geecee.escapelauncher.core.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.geecee.escapelauncher.core.data.repository.GeminiContentGenerator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ContentGenerationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val generator: GeminiContentGenerator
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            generator.generateQuestionsIfNeeded()
            generator.generateMusicIfNeeded()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
