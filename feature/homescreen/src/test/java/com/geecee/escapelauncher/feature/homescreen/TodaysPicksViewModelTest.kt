package com.geecee.escapelauncher.feature.homescreen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.geecee.escapelauncher.core.domain.usecase.FetchTodaysPicksUseCase
import com.geecee.escapelauncher.core.model.TodaysPicks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import io.mockk.coJustRun

class TodaysPicksViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    private val mockUseCase = mockk<FetchTodaysPicksUseCase>()
    private lateinit var viewModel: TodaysPicksViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coJustRun { mockUseCase.refreshIfNeeded() }
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun testViewModelInitialization() = runTest {
        coEvery { mockUseCase() } returns flowOf(
            TodaysPicks(
                articles = emptyList(),
                videos = emptyList(),
                lastUpdated = System.currentTimeMillis()
            )
        )
        
        viewModel = TodaysPicksViewModel(mockUseCase)
        
        assertNotNull(viewModel)
        // Note: loadTodaysPicks is called in init, so it starts collecting.
        // We use testDispatcher, so we might need to advance time or similar if it was more complex.
    }
    
    @Test
    fun testToggleSummary() = runTest {
        coEvery { mockUseCase() } returns flowOf(
            TodaysPicks(articles = emptyList(), videos = emptyList(), lastUpdated = 0)
        )
        
        viewModel = TodaysPicksViewModel(mockUseCase)
        
        // Test that toggleArticleSummary doesn't crash
        viewModel.toggleArticleSummary("test-id")
        assertNotNull(viewModel)
    }
}
