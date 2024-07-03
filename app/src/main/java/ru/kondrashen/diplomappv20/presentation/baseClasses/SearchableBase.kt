package ru.kondrashen.diplomappv20.presentation.baseClasses

import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView

class SearchableBase {
    fun filterAdapters(text: String?,vararg adapters: RecyclerView.Adapter<RecyclerView.ViewHolder>?){
        for (adapt in adapters){
            if (adapt is Filterable)
                adapt.filter?.filter(text)
        }
    }
}