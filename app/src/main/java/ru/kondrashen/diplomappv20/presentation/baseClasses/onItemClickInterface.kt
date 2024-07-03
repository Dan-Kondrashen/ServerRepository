package ru.kondrashen.diplomappv20.presentation.baseClasses

import com.google.android.material.snackbar.Snackbar

interface onItemClickInterface<T> {
    fun onChildAdapterClickEventMessage(position: Int, text: String): Snackbar

    fun onChildClickStartPutEvent(adapterPosition: Int, itemId: Int, mod: String)

    fun onTabClickEvent(position: Int, itemId: Int, text: String)

    fun onItemInAdapterUsableClickEvent(position: Int, item: T, activatorType: String)
}