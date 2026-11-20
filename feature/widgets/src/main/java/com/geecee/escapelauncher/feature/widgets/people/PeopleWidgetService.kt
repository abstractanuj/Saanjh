package com.geecee.escapelauncher.feature.widgets.people

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.TypedValue
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.geecee.escapelauncher.core.data.repository.DirectCallManager
import com.geecee.escapelauncher.core.data.repository.PeopleRepository
import com.geecee.escapelauncher.core.data.repository.PersonContact
import com.geecee.escapelauncher.feature.widgets.R
import kotlin.math.abs

class PeopleWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return PeopleRemoteViewsFactory(applicationContext)
    }
}

private class PeopleRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private val repository = PeopleRepository(context)
    private var contacts: List<PersonContact> = emptyList()

    override fun onCreate() {
        contacts = repository.getWidgetPeopleSync()
    }

    override fun onDataSetChanged() {
        contacts = repository.getWidgetPeopleSync()
    }

    override fun onDestroy() {
        contacts = emptyList()
    }

    override fun getCount(): Int = contacts.size

    override fun getViewAt(position: Int): RemoteViews? {
        val contact = contacts.getOrNull(position) ?: return null
        val views = RemoteViews(context.packageName, R.layout.item_people_metro_tile)

        views.setTextViewText(R.id.metro_tile_name, contact.name)
        views.setTextViewText(R.id.metro_tile_last_talked, contact.relativeLastTalkedLabel())

        // Organic staggered offset per contact inside the shared Atom Cloud
        val seed = abs((contact.name.hashCode() * 31) xor contact.phoneNumber.hashCode() xor position)
        val leftPadDp = 4 + (seed % 14)
        val rightPadDp = 4 + ((seed / 7) % 14)
        val topPadDp = 2 + ((seed / 13) % 8)
        val bottomPadDp = 2 + ((seed / 19) % 8)

        views.setViewPadding(
            R.id.metro_tile_container,
            dpToPx(leftPadDp),
            dpToPx(topPadDp),
            dpToPx(rightPadDp),
            dpToPx(bottomPadDp)
        )

        // Subtle highlight for contacts you haven't talked to in 2+ weeks vs recent favorites
        if (contact.isOverTwoWeeks) {
            views.setTextColor(R.id.metro_tile_name, Color.parseColor("#FFFFFF"))
            views.setTextColor(R.id.metro_tile_last_talked, Color.parseColor("#E6ECE8E1"))
        } else {
            views.setTextColor(R.id.metro_tile_name, Color.parseColor("#A6ECE8E1"))
            views.setTextColor(R.id.metro_tile_last_talked, Color.parseColor("#78ECE8E1"))
        }

        val fillInIntent = Intent().apply {
            putExtra(DirectCallManager.EXTRA_PHONE_NUMBER, contact.phoneNumber)
            putExtra(DirectCallManager.EXTRA_CONTACT_NAME, contact.name)
        }
        views.setOnClickFillInIntent(R.id.metro_tile_container, fillInIntent)

        return views
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long =
        contacts.getOrNull(position)?.phoneNumber?.hashCode()?.toLong() ?: position.toLong()

    override fun hasStableIds(): Boolean = true
}
