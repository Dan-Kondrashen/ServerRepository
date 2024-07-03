package ru.kondrashen.diplomappv20.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import ru.kondrashen.diplomappv20.databinding.SpinerItemDependenciesBinding
import ru.kondrashen.diplomappv20.repository.data_class.DocDependencies
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocDependenceFullInfo

class CustomSpecializationArrayAdapter(dependencies: MutableList<DocDependenceFullInfo>, context: Context, resource: Int): ArrayAdapter<DocDependenceFullInfo>(context, resource, dependencies) {
    private var layoutInflater = LayoutInflater.from(context)
    private var dependencies: MutableList<DocDependenceFullInfo> = mutableListOf()

    init {
        this.dependencies = dependencies
        println(this.dependencies)
    }

    override fun getCount(): Int {
        return dependencies.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val binding: SpinerItemDependenciesBinding =
            if (convertView != null) SpinerItemDependenciesBinding.bind(convertView)
            else SpinerItemDependenciesBinding.inflate(layoutInflater, parent, false)
        val item = dependencies[position]
        binding.specInfo.text = item.specializations.name
        return binding.specInfo
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: SpinerItemDependenciesBinding =
            if (convertView != null) SpinerItemDependenciesBinding.bind(convertView)
            else SpinerItemDependenciesBinding.inflate(layoutInflater, parent, false)
        val item = dependencies[position]
        binding.specInfo.text = item.specializations.name
        return binding.root
    }
}