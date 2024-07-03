package ru.kondrashen.diplomappv20.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.kondrashen.diplomappv20.databinding.SpinerItemDependenciesBinding
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocDependenceFullInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ItemCheckedStatus

class CustomSpecializationRVAdapter(private var dependencies: MutableList<DocDependenceFullInfo>): RecyclerView.Adapter<CustomSpecializationRVAdapter.ViewHolderSpec>(){
    private var dependStatusList: MutableList<Int> = mutableListOf()
    override fun getItemCount(): Int {
        return dependencies.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderSpec {
        val binding = SpinerItemDependenciesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderSpec(binding)
    }

    override fun onBindViewHolder(holder: ViewHolderSpec, position: Int) {
        var depend =dependencies[position]
        holder.setDependence(depend)
    }

    inner class ViewHolderSpec(private var binding: SpinerItemDependenciesBinding): RecyclerView.ViewHolder(binding.root) {
        private lateinit var dependence: DocDependenceFullInfo
        fun setDependence(docDepend: DocDependenceFullInfo) {
            this.dependence = docDepend
            binding.specInfo.text = docDepend.specializations.name
            dependence.educations?.let {
                binding.eduInfo.text = it.name
            }
            binding.addSpecCheck.setOnCheckedChangeListener { buttonView, isChecked ->

                val itemIsChecked = dependence.docDependence.id
                if (isChecked){
                    dependStatusList.add(itemIsChecked)
                }
                else{
                    dependStatusList.remove(itemIsChecked)
                }
            }

        }
    }
    fun getCheckedItems(): List<Int>{
        return dependStatusList
    }
}