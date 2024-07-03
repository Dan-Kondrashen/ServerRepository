package ru.kondrashen.diplomappv20.presentation.adapters

import android.view.LayoutInflater
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import ru.kondrashen.diplomappv20.databinding.ListItemKnowledgeBinding
import ru.kondrashen.diplomappv20.databinding.ListItemKnowledgeCheckedBinding
import ru.kondrashen.diplomappv20.presentation.baseClasses.onItemClickInterface
import ru.kondrashen.diplomappv20.repository.data_class.Knowledge
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.KnowledgeWithAdapterPosition

class SimpleKnowledgeAdapter(private var knowledges: MutableList<Knowledge>, private var mod: String, private var  listener: onItemClickInterface<Knowledge>?): RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private var filteredKnowledge = mutableListOf<Knowledge>()
    private var resultKnowledge : MutableList<KnowledgeWithAdapterPosition> = mutableListOf()
    private var searchableKnowledgeList: MutableList<Int> = mutableListOf()
    private var clickPosition: Int = -1
    init {
        filteredKnowledge = knowledges
    }
    override fun getItemViewType(position: Int): Int {
        return when(mod){
            "removableView"-> 1
            "simpleView" -> 2
            "view" -> 3
            "checkedView" -> 4
            else -> 2
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1){
            val binding = ListItemKnowledgeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false)
            return KnowledgeRemovableViewHolder(binding)
        }
        else if (viewType == 3){
            val binding = ListItemKnowledgeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false)
            return KnowledgeSimpleViewHolder(binding)
        }
        else if (viewType == 4){
            val binding = ListItemKnowledgeCheckedBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false)
            return KnowledgeSimpleCheckedViewHolder(binding)
        }
        else {
            val binding = ListItemKnowledgeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false)
            return KnowledgeMainViewHolder(binding)
        }
    }

    override fun getItemCount(): Int {
        return knowledges.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder1: KnowledgeRemovableViewHolder
        val holder2: KnowledgeMainViewHolder
        val holder3: KnowledgeSimpleViewHolder
        val holder4: KnowledgeSimpleCheckedViewHolder
        val know = knowledges[position]
        if(holder.itemViewType == 1){
            holder1 = holder as KnowledgeRemovableViewHolder
            holder1.setKnow(know)
        }
        else if (holder.itemViewType == 3){
            holder3 = holder as KnowledgeSimpleViewHolder
            holder3.setKnow(know)
        }
        else if (holder.itemViewType == 4){
            holder4 = holder as KnowledgeSimpleCheckedViewHolder
            holder4.setKnow(know)
        }
        else {
            holder2 = holder as KnowledgeMainViewHolder
            holder2.setKnow(know)


        }
    }
    fun getCurrentIDs(): List<Int> {
        return knowledges.map { it.knowId }
    }
    inner class KnowledgeMainViewHolder(private var binding: ListItemKnowledgeBinding): RecyclerView.ViewHolder(binding.root) {
        private lateinit var knowledge: Knowledge

        fun setKnow(know: Knowledge){
            this.knowledge = know

            binding.listItemKnowledgeTextView.text =knowledge.name
            binding.root.setOnClickListener {
                clickPosition = adapterPosition
                listener?.onItemInAdapterUsableClickEvent(clickPosition, knowledge, "removeFromMain")
                knowledges.removeAt(clickPosition)
                notifyItemRemoved(clickPosition)
            }

        }
    }
    inner class KnowledgeSimpleViewHolder(private var binding: ListItemKnowledgeBinding): RecyclerView.ViewHolder(binding.root) {
        private lateinit var knowledge: Knowledge
        fun setKnow(know: Knowledge){
            this.knowledge = know
            binding.listItemKnowledgeTextView.text =knowledge.name
        }
    }
    inner class KnowledgeSimpleCheckedViewHolder(private var binding: ListItemKnowledgeCheckedBinding): RecyclerView.ViewHolder(binding.root) {
        private lateinit var knowledge: Knowledge
        fun setKnow(know: Knowledge){
            this.knowledge = know
            binding.listItemKnowledgeTextView.text =knowledge.name
            binding.checked.setOnCheckedChangeListener { buttonView, isChecked ->
                val itemIsChecked = know.knowId
                if (isChecked){
                    searchableKnowledgeList.add(itemIsChecked)
                }
                else{
                    searchableKnowledgeList.remove(itemIsChecked)
                }
            }
        }
    }
    inner class KnowledgeRemovableViewHolder(private var binding: ListItemKnowledgeBinding): RecyclerView.ViewHolder(binding.root) {
        private lateinit var knowledge: Knowledge

        fun setKnow(know: Knowledge){
            this.knowledge = know
            binding.listItemKnowledgeTextView.text =know.name
            binding.removeBtn.apply {
                visibility =VISIBLE
                setOnClickListener {
                    listener?.onItemInAdapterUsableClickEvent(clickPosition, knowledge, "removeFromChosen")
                    val removedItemIndex = adapterPosition
                    knowledges.removeAt(removedItemIndex)
                    notifyItemRemoved(removedItemIndex)

                }
            }

        }

    }

    override fun getFilter(): Filter {
        return  object  : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val resultOfFiltration = FilterResults()
                if(constraint == null || constraint.length <0){
                    resultOfFiltration.count = filteredKnowledge.size
                    resultOfFiltration.values = filteredKnowledge
                }else{
                    var str = constraint.toString().lowercase()
                    var items = mutableListOf<Knowledge>()
                    for(item in filteredKnowledge){
                        if(item.name.lowercase().contains(str)||item.description.toString().lowercase().contains(str)){
                            items.add(item)
                        }
                    }
                    resultOfFiltration.count = items.size
                    resultOfFiltration.values = items
                }
                return resultOfFiltration
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                knowledges =  results!!.values as MutableList<Knowledge>
                notifyDataSetChanged()
            }

        }
    }
    fun getClickedItem(): Int{
        return clickPosition
    }
    fun getCheckedItems(): List<Int>{
        return searchableKnowledgeList
    }
    fun getItems(): MutableList<Knowledge>{
        return knowledges
    }


    fun setItems(knowledges: MutableList<Knowledge>){
        this.knowledges = knowledges
    }
    fun addItem(know: Knowledge){
        var position = knowledges.size
        this.knowledges.add(know)
        println(knowledges)
        notifyDataSetChanged()
    }
}

