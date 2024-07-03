package ru.kondrashen.diplomappv20.presentation.adapters

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle

import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.kondrashen.diplomappv20.presentation.fragments.DocumentInfoFragment
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfoWithKnowledge

class DocumentPagerAdapter(fm: FragmentManager, lifecycle: Lifecycle, userId: Int, documents: MutableList<DocumentInfoWithKnowledge>): FragmentStateAdapter(fm, lifecycle) {
    private var documents: MutableList<DocumentInfoWithKnowledge> = mutableListOf()
    private var idUser: Int
    init {
        this.documents = documents
        this.idUser = userId
    }
    override fun getItemCount() =documents.size
    override fun createFragment(position: Int) = DocumentInfoFragment.newInstance(documents[position].document.docId, idUser)
}