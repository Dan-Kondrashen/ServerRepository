package ru.kondrashen.diplomappv20.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.kondrashen.diplomappv20.databinding.CheckedExperienceListItemBinding
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ExperienceInfo

class CustomExperienceRVAdapter(private var experience: MutableList<ExperienceInfo>): RecyclerView.Adapter<CustomExperienceRVAdapter.ViewHolder>(){
    private var statusList: MutableList<Int> = mutableListOf()
    override fun getItemCount(): Int {
        return experience.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CheckedExperienceListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val experience =experience[position]
        holder.setExperience(experience)
    }

    inner class ViewHolder(private var binding: CheckedExperienceListItemBinding): RecyclerView.ViewHolder(binding.root) {
        private lateinit var experienceInfo: ExperienceInfo
        fun setExperience(experience: ExperienceInfo) {
            this.experienceInfo = experience
            binding.expTimeInfo.text = experienceInfo.experienceTime
            binding.roleInfo.text = experienceInfo.role

            binding.addExpCheck.setOnCheckedChangeListener { buttonView, isChecked ->

                val itemIsChecked = experienceInfo.expId
                if (isChecked){
                    statusList.add(itemIsChecked)
                }
                else{
                    statusList.remove(itemIsChecked)
                }
            }

        }
    }
    fun getCheckedItems(): List<Int>{
        return statusList
    }
}