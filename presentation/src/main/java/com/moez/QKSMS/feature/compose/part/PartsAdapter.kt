/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.feature.compose.part

import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.moez.QKSMS.common.base.QkAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.forwardTouches
import com.moez.QKSMS.databinding.MessageListItemInBinding
import com.moez.QKSMS.extensions.isSmil
import com.moez.QKSMS.extensions.isText
import com.moez.QKSMS.feature.compose.BubbleUtils.canGroup
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.model.MmsPart
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class PartsAdapter @Inject constructor(
    colors: Colors,
    fileBinder: FileBinder,
    mediaBinder: MediaBinder,
    vCardBinder: VCardBinder
) : QkAdapter<MmsPart, ViewBinding>() {

    private val partBinders = listOf(mediaBinder, vCardBinder, fileBinder)

    var theme: Colors.Theme = colors.theme()
        set(value) {
            field = value
            partBinders.forEach { binder -> binder.theme = value }
        }

    val clicks: Observable<Long> = Observable.merge(partBinders.map { it.clicks })

    private lateinit var message: Message
    private var previous: Message? = null
    private var next: Message? = null
    private var holder: QkViewHolder<MessageListItemInBinding>? = null
    private var bodyVisible: Boolean = true

    fun setData(message: Message, previous: Message?, next: Message?, holder: QkViewHolder<MessageListItemInBinding>) {
        this.message = message
        this.previous = previous
        this.next = next
        this.holder = holder
        this.bodyVisible = holder.binding.body.visibility == View.VISIBLE
        this.data = message.parts.filter { !it.isSmil() && !it.isText() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder<ViewBinding> {
        return QkViewHolder(parent, partBinders[viewType].bindingInflater).apply {
            holder?.binding?.root?.let(binding.root::forwardTouches)
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder<ViewBinding>, position: Int) {
        val part = data[position]

        val canGroupWithPrevious = canGroup(message, previous) || position > 0
        val canGroupWithNext = canGroup(message, next) || position < itemCount - 1 || bodyVisible

        partBinders.find { binder -> binder.bindPart(holder, part, message, canGroupWithPrevious, canGroupWithNext) }
    }

    override fun getItemViewType(position: Int): Int {
        val part = data[position]
        return partBinders.indexOfFirst { it.canBindPart(part) }
    }

}