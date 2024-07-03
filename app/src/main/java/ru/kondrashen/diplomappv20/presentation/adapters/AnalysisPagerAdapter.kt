package ru.kondrashen.diplomappv20.presentation.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle

import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.kondrashen.diplomappv20.presentation.fragments.DocumentInfoFragment
import ru.kondrashen.diplomappv20.presentation.fragments.GraphMainFragment
import ru.kondrashen.diplomappv20.presentation.fragments.GraphUserFragment
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfoWithKnowledge

class AnalysisPagerAdapter(fm: FragmentManager, lifecycle: Lifecycle, userId: Int, userType: String): FragmentStateAdapter(fm, lifecycle) {

    private var userId: Int
    private var userType: String
    init {
        this.userId = userId
        this.userType = userType
    }
    override fun getItemCount(): Int {
        return 2
    }
    override fun createFragment(position: Int): Fragment {
        return when(position)  {
            0 -> GraphMainFragment.newInstance(userId, userType)
            1-> GraphUserFragment.newInstance(userId, userType)
            else-> GraphMainFragment.newInstance(userId, userType)
        }
    }
}