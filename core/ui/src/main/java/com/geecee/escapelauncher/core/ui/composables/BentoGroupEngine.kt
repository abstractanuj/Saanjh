package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Universal Interaction Group Architecture.
 * Standardizes paging behavior, axis locking, and randomized Bento layouts.
 */
@Composable
fun <T> BentoGroup(
    items: List<T>,
    modifier: Modifier = Modifier,
    orientation: BentoOrientation = BentoOrientation.Vertical,
    pageSize: Int = 5, 
    itemContent: @Composable (T, BentoItemSize) -> Unit,
    height: Dp? = null
) {
    if (items.isEmpty()) return

    val pages = remember(items, pageSize) { items.chunked(pageSize) }
    val pagerState = rememberPagerState { pages.size }

    val pagerModifier = modifier
        .then(if (height != null) Modifier.height(height) else Modifier.fillMaxSize())

    if (orientation == BentoOrientation.Horizontal) {
        HorizontalPager(
            state = pagerState,
            modifier = pagerModifier,
            pageSpacing = 16.dp,
            beyondViewportPageCount = 1
        ) { pageIndex ->
            RandomizedBentoLayout(pages[pageIndex], pageIndex, itemContent)
        }
    } else {
        VerticalPager(
            state = pagerState,
            modifier = pagerModifier,
            pageSpacing = 16.dp,
            beyondViewportPageCount = 1
        ) { pageIndex ->
            RandomizedBentoLayout(pages[pageIndex], pageIndex, itemContent)
        }
    }
}

@Composable
fun <T> RandomizedBentoLayout(
    pageItems: List<T>,
    pageIndex: Int,
    itemContent: @Composable (T, BentoItemSize) -> Unit
) {
    // Stability: Use pageIndex to pick a layout from the 15 sets
    val layoutType = pageIndex % 15

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when (layoutType) {
            0 -> LayoutSet1(pageItems, itemContent)
            1 -> LayoutSet2(pageItems, itemContent)
            2 -> LayoutSet3(pageItems, itemContent)
            3 -> LayoutSet4(pageItems, itemContent)
            4 -> LayoutSet5(pageItems, itemContent)
            5 -> LayoutSet6(pageItems, itemContent)
            6 -> LayoutSet7(pageItems, itemContent)
            7 -> LayoutSet8(pageItems, itemContent)
            8 -> LayoutSet9(pageItems, itemContent)
            9 -> LayoutSet10(pageItems, itemContent)
            10 -> LayoutSet11(pageItems, itemContent)
            11 -> LayoutSet12(pageItems, itemContent)
            12 -> LayoutSet13(pageItems, itemContent)
            13 -> LayoutSet14(pageItems, itemContent)
            else -> LayoutSet15(pageItems, itemContent)
        }
    }
}

@Composable
private fun <T> ColumnScope.LayoutSet1(items: List<T>, content: @Composable (T, BentoItemSize) -> Unit) {
    if (items.isNotEmpty()) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[0], BentoItemSize.Medium) }
            if (items.size > 1) Box(Modifier.weight(1f)) { content(items[1], BentoItemSize.Small) }
        }
    }
    if (items.size > 2) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[2], BentoItemSize.Small) }
            if (items.size > 3) Box(Modifier.weight(1f)) { content(items[3], BentoItemSize.Medium) }
        }
    }
    if (items.size > 4) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[4], BentoItemSize.Small) }
            if (items.size > 5) Box(Modifier.weight(1f)) { content(items[5], BentoItemSize.Small) }
            if (items.size > 6) Box(Modifier.weight(1f)) { content(items[6], BentoItemSize.Small) }
        }
    }
    if (items.size > 7) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[7], BentoItemSize.Medium) }
            if (items.size > 8) Box(Modifier.weight(1f)) { content(items[8], BentoItemSize.Small) }
        }
    }
    if (items.size > 9) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[9], BentoItemSize.Small) }
        }
    }
}

@Composable
private fun <T> ColumnScope.LayoutSet2(items: List<T>, content: @Composable (T, BentoItemSize) -> Unit) {
    if (items.isNotEmpty()) Box(Modifier.height(208.dp)) { content(items[0], BentoItemSize.Large) }
    if (items.size > 1) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[1], BentoItemSize.Small) }
            if (items.size > 2) Box(Modifier.weight(1f)) { content(items[2], BentoItemSize.Small) }
            if (items.size > 3) Box(Modifier.weight(1f)) { content(items[3], BentoItemSize.Small) }
        }
    }
    if (items.size > 4) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[4], BentoItemSize.Medium) }
            if (items.size > 5) Box(Modifier.weight(1f)) { content(items[5], BentoItemSize.Small) }
        }
    }
    if (items.size > 6) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[6], BentoItemSize.Small) }
            if (items.size > 7) Box(Modifier.weight(1f)) { content(items[7], BentoItemSize.Small) }
            if (items.size > 8) Box(Modifier.weight(1f)) { content(items[8], BentoItemSize.Small) }
        }
    }
    if (items.size > 9) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[9], BentoItemSize.Medium) }
        }
    }
}

@Composable
private fun <T> ColumnScope.LayoutSet3(items: List<T>, content: @Composable (T, BentoItemSize) -> Unit) {
    Row(Modifier.height(208.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (items.isNotEmpty()) Box(Modifier.weight(1f)) { content(items[0], BentoItemSize.Tall) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (items.size > 1) Box(Modifier.weight(1f)) { content(items[1], BentoItemSize.Small) }
            if (items.size > 2) Box(Modifier.weight(1f)) { content(items[2], BentoItemSize.Small) }
        }
    }
    if (items.size > 3) Box(Modifier.height(208.dp)) { content(items[3], BentoItemSize.Large) }
    if (items.size > 4) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[4], BentoItemSize.Small) }
            if (items.size > 5) Box(Modifier.weight(1f)) { content(items[5], BentoItemSize.Small) }
            if (items.size > 6) Box(Modifier.weight(1f)) { content(items[6], BentoItemSize.Small) }
        }
    }
    if (items.size > 7) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[7], BentoItemSize.Medium) }
            if (items.size > 8) Box(Modifier.weight(1f)) { content(items[8], BentoItemSize.Medium) }
        }
    }
    if (items.size > 9) Box(Modifier.height(100.dp)) { content(items[9], BentoItemSize.Small) }
}

@Composable
private fun <T> ColumnScope.LayoutSet4(items: List<T>, content: @Composable (T, BentoItemSize) -> Unit) {
    if (items.isNotEmpty()) Box(Modifier.height(208.dp)) { content(items[0], BentoItemSize.Large) }
    if (items.size > 1) Box(Modifier.height(208.dp)) { content(items[1], BentoItemSize.Large) }
    Row(Modifier.height(208.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (items.size > 2) Box(Modifier.weight(1f)) { content(items[2], BentoItemSize.Small) }
            if (items.size > 3) Box(Modifier.weight(1f)) { content(items[3], BentoItemSize.Small) }
        }
        if (items.size > 4) Box(Modifier.weight(1f)) { content(items[4], BentoItemSize.Tall) }
    }
    if (items.size > 5) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[5], BentoItemSize.Medium) }
            if (items.size > 6) Box(Modifier.weight(1f)) { content(items[6], BentoItemSize.Small) }
        }
    }
    if (items.size > 7) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[7], BentoItemSize.Small) }
            if (items.size > 8) Box(Modifier.weight(1f)) { content(items[8], BentoItemSize.Small) }
            if (items.size > 9) Box(Modifier.weight(1f)) { content(items[9], BentoItemSize.Small) }
        }
    }
}

@Composable
private fun <T> ColumnScope.LayoutSet5(items: List<T>, content: @Composable (T, BentoItemSize) -> Unit) {
    if (items.isNotEmpty()) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[0], BentoItemSize.Small) }
            if (items.size > 1) Box(Modifier.weight(1f)) { content(items[1], BentoItemSize.Small) }
            if (items.size > 2) Box(Modifier.weight(1f)) { content(items[2], BentoItemSize.Small) }
        }
    }
    if (items.size > 3) Box(Modifier.height(208.dp)) { content(items[3], BentoItemSize.Large) }
    if (items.size > 4) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[4], BentoItemSize.Medium) }
            if (items.size > 5) Box(Modifier.weight(1f)) { content(items[5], BentoItemSize.Medium) }
        }
    }
    if (items.size > 6) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[6], BentoItemSize.Small) }
            if (items.size > 7) Box(Modifier.weight(1f)) { content(items[7], BentoItemSize.Small) }
        }
    }
    if (items.size > 8) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[8], BentoItemSize.Small) }
            if (items.size > 9) Box(Modifier.weight(1f)) { content(items[9], BentoItemSize.Small) }
        }
    }
}

@Composable
private fun <T> ColumnScope.LayoutSet6(items: List<T>, content: @Composable (T, BentoItemSize) -> Unit) {
    if (items.isNotEmpty()) Box(Modifier.height(208.dp)) { content(items[0], BentoItemSize.Large) }
    Row(Modifier.height(208.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (items.size > 1) Box(Modifier.weight(1f)) { content(items[1], BentoItemSize.Tall) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (items.size > 2) Box(Modifier.weight(1f)) { content(items[2], BentoItemSize.Small) }
            if (items.size > 3) Box(Modifier.weight(1f)) { content(items[3], BentoItemSize.Small) }
        }
    }
    if (items.size > 4) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[4], BentoItemSize.Small) }
            if (items.size > 5) Box(Modifier.weight(1f)) { content(items[5], BentoItemSize.Small) }
            if (items.size > 6) Box(Modifier.weight(1f)) { content(items[6], BentoItemSize.Small) }
        }
    }
    if (items.size > 7) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[7], BentoItemSize.Medium) }
            if (items.size > 8) Box(Modifier.weight(1f)) { content(items[8], BentoItemSize.Medium) }
        }
    }
    if (items.size > 9) Box(Modifier.height(100.dp)) { content(items[9], BentoItemSize.Small) }
}

@Composable
private fun <T> ColumnScope.LayoutSet7(items: List<T>, content: @Composable (T, BentoItemSize) -> Unit) {
    if (items.isNotEmpty()) Box(Modifier.height(208.dp)) { content(items[0], BentoItemSize.Large) }
    if (items.size > 1) Box(Modifier.height(208.dp)) { content(items[1], BentoItemSize.Large) }
    if (items.size > 2) Box(Modifier.height(208.dp)) { content(items[2], BentoItemSize.Large) }
    if (items.size > 3) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[3], BentoItemSize.Small) }
            if (items.size > 4) Box(Modifier.weight(1f)) { content(items[4], BentoItemSize.Small) }
            if (items.size > 5) Box(Modifier.weight(1f)) { content(items[5], BentoItemSize.Small) }
        }
    }
    if (items.size > 6) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[6], BentoItemSize.Medium) }
            if (items.size > 7) Box(Modifier.weight(1f)) { content(items[7], BentoItemSize.Small) }
        }
    }
    if (items.size > 8) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[8], BentoItemSize.Small) }
            if (items.size > 9) Box(Modifier.weight(1f)) { content(items[9], BentoItemSize.Small) }
        }
    }
}

@Composable
private fun <T> ColumnScope.LayoutSet8(items: List<T>, content: @Composable (T, BentoItemSize) -> Unit) {
    if (items.isNotEmpty()) Box(Modifier.height(208.dp)) { content(items[0], BentoItemSize.Large) }
    if (items.size > 1) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[1], BentoItemSize.Small) }
            if (items.size > 2) Box(Modifier.weight(1f)) { content(items[2], BentoItemSize.Small) }
            if (items.size > 3) Box(Modifier.weight(1f)) { content(items[3], BentoItemSize.Small) }
        }
    }
    if (items.size > 4) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[4], BentoItemSize.Medium) }
            if (items.size > 5) Box(Modifier.weight(1f)) { content(items[5], BentoItemSize.Small) }
        }
    }
    if (items.size > 6) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[6], BentoItemSize.Small) }
            if (items.size > 7) Box(Modifier.weight(1f)) { content(items[7], BentoItemSize.Small) }
        }
    }
    if (items.size > 8) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[8], BentoItemSize.Small) }
            if (items.size > 9) Box(Modifier.weight(1f)) { content(items[9], BentoItemSize.Small) }
        }
    }
}

@Composable
private fun <T> ColumnScope.LayoutSet9(items: List<T>, content: @Composable (T, BentoItemSize) -> Unit) {
    Row(Modifier.height(208.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (items.isNotEmpty()) Box(Modifier.weight(1f)) { content(items[0], BentoItemSize.Tall) }
        if (items.size > 1) Box(Modifier.weight(1f)) { content(items[1], BentoItemSize.Tall) }
    }
    if (items.size > 2) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[2], BentoItemSize.Small) }
            if (items.size > 3) Box(Modifier.weight(1f)) { content(items[3], BentoItemSize.Small) }
            if (items.size > 4) Box(Modifier.weight(1f)) { content(items[4], BentoItemSize.Small) }
        }
    }
    if (items.size > 5) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[5], BentoItemSize.Medium) }
            if (items.size > 6) Box(Modifier.weight(1f)) { content(items[6], BentoItemSize.Medium) }
        }
    }
    if (items.size > 7) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[7], BentoItemSize.Small) }
            if (items.size > 8) Box(Modifier.weight(1f)) { content(items[8], BentoItemSize.Small) }
            if (items.size > 9) Box(Modifier.weight(1f)) { content(items[9], BentoItemSize.Small) }
        }
    }
}

@Composable
private fun <T> ColumnScope.LayoutSet10(items: List<T>, content: @Composable (T, BentoItemSize) -> Unit) {
    if (items.isNotEmpty()) Box(Modifier.height(208.dp)) { content(items[0], BentoItemSize.Large) }
    if (items.size > 1) Box(Modifier.height(208.dp)) { content(items[1], BentoItemSize.Large) }
    Row(Modifier.height(208.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (items.size > 2) Box(Modifier.weight(1f)) { content(items[2], BentoItemSize.Tall) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (items.size > 3) Box(Modifier.weight(1f)) { content(items[3], BentoItemSize.Small) }
            if (items.size > 4) Box(Modifier.weight(1f)) { content(items[4], BentoItemSize.Small) }
        }
    }
    if (items.size > 5) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[5], BentoItemSize.Small) }
            if (items.size > 6) Box(Modifier.weight(1f)) { content(items[6], BentoItemSize.Small) }
            if (items.size > 7) Box(Modifier.weight(1f)) { content(items[7], BentoItemSize.Small) }
        }
    }
    if (items.size > 8) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[8], BentoItemSize.Medium) }
            if (items.size > 9) Box(Modifier.weight(1f)) { content(items[9], BentoItemSize.Small) }
        }
    }
}

@Composable
private fun <T> ColumnScope.LayoutSet11(items: List<T>, content: @Composable (T, BentoItemSize) -> Unit) {
    if (items.isNotEmpty()) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[0], BentoItemSize.Medium) }
            if (items.size > 1) Box(Modifier.weight(1f)) { content(items[1], BentoItemSize.Small) }
        }
    }
    if (items.size > 2) Box(Modifier.height(208.dp)) { content(items[2], BentoItemSize.Large) }
    if (items.size > 3) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[3], BentoItemSize.Small) }
            if (items.size > 4) Box(Modifier.weight(1f)) { content(items[4], BentoItemSize.Medium) }
        }
    }
    if (items.size > 5) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[5], BentoItemSize.Small) }
            if (items.size > 6) Box(Modifier.weight(1f)) { content(items[6], BentoItemSize.Small) }
            if (items.size > 7) Box(Modifier.weight(1f)) { content(items[7], BentoItemSize.Small) }
        }
    }
    if (items.size > 8) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[8], BentoItemSize.Small) }
            if (items.size > 9) Box(Modifier.weight(1f)) { content(items[9], BentoItemSize.Small) }
        }
    }
}

@Composable
private fun <T> ColumnScope.LayoutSet12(items: List<T>, content: @Composable (T, BentoItemSize) -> Unit) {
    if (items.size > 1) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[0], BentoItemSize.Small) }
            Box(Modifier.weight(1f)) { content(items[1], BentoItemSize.Small) }
            if (items.size > 2) Box(Modifier.weight(1f)) { content(items[2], BentoItemSize.Small) }
        }
    }
    if (items.size > 3) Box(Modifier.height(208.dp)) { content(items[3], BentoItemSize.Large) }
    if (items.size > 4) Box(Modifier.height(208.dp)) { content(items[4], BentoItemSize.Large) }
    if (items.size > 5) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[5], BentoItemSize.Small) }
            if (items.size > 6) Box(Modifier.weight(1f)) { content(items[6], BentoItemSize.Small) }
            if (items.size > 7) Box(Modifier.weight(1f)) { content(items[7], BentoItemSize.Small) }
        }
    }
    if (items.size > 8) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[8], BentoItemSize.Medium) }
            if (items.size > 9) Box(Modifier.weight(1f)) { content(items[9], BentoItemSize.Small) }
        }
    }
}

@Composable
private fun <T> ColumnScope.LayoutSet13(items: List<T>, content: @Composable (T, BentoItemSize) -> Unit) {
    Row(Modifier.height(208.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (items.isNotEmpty()) Box(Modifier.weight(1f)) { content(items[0], BentoItemSize.Tall) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (items.size > 1) Box(Modifier.weight(1f)) { content(items[1], BentoItemSize.Small) }
            if (items.size > 2) Box(Modifier.weight(1f)) { content(items[2], BentoItemSize.Small) }
        }
    }
    Row(Modifier.height(208.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (items.size > 3) Box(Modifier.weight(1f)) { content(items[3], BentoItemSize.Small) }
            if (items.size > 4) Box(Modifier.weight(1f)) { content(items[4], BentoItemSize.Small) }
        }
        if (items.size > 5) Box(Modifier.weight(1f)) { content(items[5], BentoItemSize.Tall) }
    }
    if (items.size > 6) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[6], BentoItemSize.Small) }
            if (items.size > 7) Box(Modifier.weight(1f)) { content(items[7], BentoItemSize.Small) }
            if (items.size > 8) Box(Modifier.weight(1f)) { content(items[8], BentoItemSize.Small) }
        }
    }
    if (items.size > 9) Box(Modifier.height(100.dp)) { content(items[9], BentoItemSize.Medium) }
}

@Composable
private fun <T> ColumnScope.LayoutSet14(items: List<T>, content: @Composable (T, BentoItemSize) -> Unit) {
    if (items.isNotEmpty()) Box(Modifier.height(208.dp)) { content(items[0], BentoItemSize.Large) }
    if (items.size > 1) Box(Modifier.height(208.dp)) { content(items[1], BentoItemSize.Large) }
    if (items.size > 2) Box(Modifier.height(208.dp)) { content(items[2], BentoItemSize.Large) }
    if (items.size > 3) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[3], BentoItemSize.Small) }
            if (items.size > 4) Box(Modifier.weight(1f)) { content(items[5], BentoItemSize.Small) }
        }
    }
}

@Composable
private fun <T> ColumnScope.LayoutSet15(items: List<T>, content: @Composable (T, BentoItemSize) -> Unit) {
    Row(Modifier.height(208.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (items.isNotEmpty()) Box(Modifier.weight(1f)) { content(items[0], BentoItemSize.Tall) }
        if (items.size > 1) Box(Modifier.weight(1f)) { content(items[1], BentoItemSize.Tall) }
    }
    if (items.size > 2) Box(Modifier.height(208.dp)) { content(items[2], BentoItemSize.Large) }
    if (items.size > 3) {
        Row(Modifier.height(100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { content(items[3], BentoItemSize.Small) }
            if (items.size > 4) Box(Modifier.weight(1f)) { content(items[4], BentoItemSize.Small) }
            if (items.size > 5) Box(Modifier.weight(1f)) { content(items[5], BentoItemSize.Small) }
        }
    }
}

enum class BentoOrientation {
    Horizontal, Vertical
}

enum class BentoItemSize {
    Small, Medium, Large, Tall
}

/**
 * A static, non-paging version of the Bento layout.
 * Used for "Bento in Bento" - nesting a smaller grid inside a Large/Tall tile.
 */
@Composable
fun <T> StaticBentoGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    layoutIndex: Int = 0,
    itemContent: @Composable (T, BentoItemSize) -> Unit
) {
    Box(modifier = modifier) {
        RandomizedBentoLayout(
            pageItems = items.take(10), 
            pageIndex = layoutIndex,
            itemContent = itemContent
        )
    }
}
