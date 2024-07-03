package ru.kondrashen.diplomappv20.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.kondrashen.diplomappv20.databinding.CheckedArchiveListItemBinding
import ru.kondrashen.diplomappv20.repository.data_class.Archive

class CustomArchiveRVAdapter(private var archives: MutableList<Archive>, private var types: List<String>): RecyclerView.Adapter<CustomArchiveRVAdapter.ViewHolder>(){
    private var statusList: MutableList<String> = mutableListOf()
    override fun getItemCount(): Int {
        return archives.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CheckedArchiveListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val archive =archives[position]
        holder.setArchive(archive)
    }

    inner class ViewHolder(private var binding: CheckedArchiveListItemBinding): RecyclerView.ViewHolder(binding.root) {
        private lateinit var archiveInfo: Archive
        fun setArchive(archive: Archive) {
            this.archiveInfo = archive
            types.find { it==archiveInfo.searchableWord }?.let {
                binding.addArchiveCheck.isChecked = true
                statusList.add(archiveInfo.searchableWord)
            }
            binding.archiveName.text = archiveInfo.name
            binding.archiveKeyWord.text = archiveInfo.searchableWord
            binding.archiveId.text = archiveInfo.id.toString()

            binding.addArchiveCheck.setOnCheckedChangeListener { buttonView, isChecked ->

                val itemIsChecked = archiveInfo.searchableWord
                if (isChecked){
                    statusList.add(itemIsChecked)
                }
                else{
                    statusList.remove(itemIsChecked)
                }
            }

        }
    }
    fun getCheckedItems(): List<String>{
        return statusList
    }
}