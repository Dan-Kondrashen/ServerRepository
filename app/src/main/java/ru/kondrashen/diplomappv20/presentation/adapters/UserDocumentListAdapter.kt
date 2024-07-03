package ru.kondrashen.diplomappv20.presentation.adapters

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import ru.kondrashen.diplomappv20.R

import ru.kondrashen.diplomappv20.databinding.ListItemEditableDocumentBinding
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.baseClasses.onItemClickInterface
import ru.kondrashen.diplomappv20.repository.data_class.Document
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfoWithKnowledge

class UserDocumentListAdapter(private var documents: MutableList<DocumentInfoWithKnowledge>, private var viewModel: UserAccountControlViewModel, private var activity: Activity, private var userId: Int, private var listener: onItemClickInterface<User>) : RecyclerView.Adapter<UserDocumentListAdapter.UserDocumentViewHolder>(){
    inner class UserDocumentViewHolder(private val binding: ListItemEditableDocumentBinding, private var viewModel: UserAccountControlViewModel, private var activity: Activity, private var userId: Int): RecyclerView.ViewHolder(binding.root){
        private lateinit var document: DocumentInfoWithKnowledge

        fun bindDocument(doc: DocumentInfoWithKnowledge) {
            this.document = doc
            binding.listItemDocumentViewedCountInfo.visibility = View.GONE
            doc.document.numViews?.let {
                var suff =""
                suff = when(it%10){
                    in listOf(2,3,4) -> if (it !in listOf(12,13,14))  "раза" else "раз"
                    else -> "раз"
                }
                binding.listItemDocumentViewedCountTextView.text = "Просмотрено "
                binding.listItemDocumentViewedCountInfo.text = "${it} $suff"
                binding.listItemDocumentViewedCountInfo.visibility = View.VISIBLE
            }?: run {
                binding.listItemDocumentViewedCountTextView.text = "Пока никем не просмотренно!"
            }
            if (doc.knowledge.isNotEmpty()) {
                val result = doc.knowledge.joinToString(", ") { know ->  know.name }
                binding.listItemDocumentKnowInfo.text = result
            }
            else binding.listItemDocumentKnowInfo.text = "Не указанны!"
            if (doc.specializations.isNotEmpty()) {
                val result = doc.specializations.joinToString(", ") { spec -> spec.name }
                binding.listItemDocumentSpecialTextView.text = result
            }
            else binding.listItemDocumentSpecialTextView.text = "Не указана!"
            binding.listItemDocumentTitleTextView.text = doc.document.title
            binding.listItemDocumentDateInfo.text =doc.document.date
            when(document.document.salaryF) {
                in listOf(0F, null) -> {
                    binding.listItemDocumentSalaryStartTextView.visibility = View.GONE
                }
                else -> binding.listItemDocumentSalaryStartTextView.text = "от ${document.document.salaryF} "
            }
            when(document.document.salaryS) {
                in listOf(0F, null) -> {
                    binding.listItemDocumentSalaryEndTextView.visibility = View.GONE
                }
                else -> binding.listItemDocumentSalaryEndTextView.text = "до ${document.document.salaryS} "
            }
            if (!binding.listItemDocumentSalaryStartTextView.isVisible and !binding.listItemDocumentSalaryEndTextView.isVisible){
                binding.listItemDocumentSalaryView.visibility = View.GONE
            }
            binding.deleteBtn.setOnClickListener {

                val animatorSet = AnimatorSet()
                val scaleXUp = ObjectAnimator.ofFloat(binding.deleteBtn, "scaleX", 1.3F)
                val scaleYUp = ObjectAnimator.ofFloat(binding.deleteBtn, "scaleY", 1.3F)
                val scaleXDown = ObjectAnimator.ofFloat(binding.deleteBtn, "scaleX", 1F)
                val scaleYDown = ObjectAnimator.ofFloat(binding.deleteBtn, "scaleY", 1F)
                animatorSet.play(scaleXUp).with(scaleYUp).before(scaleXDown).before(scaleYDown)
                animatorSet.duration = 150
                animatorSet.addListener(object: AnimatorListenerAdapter(){
                    override fun onAnimationStart(animation: Animator) {
                        super.onAnimationStart(animation)
                        binding.deleteBtn.setBackgroundResource(R.drawable.delete_pressed_svg)
                    }
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        binding.deleteBtn.setBackgroundResource(R.drawable.delete_svg)
                    }
                })
                animatorSet.start()
                viewModel.deleteDocRequest(doc.document.docId, userId).observe(activity as LifecycleOwner){
                    if (it in listOf("Успешно удалено!", "Данный документ не найден на сервере!")){
                        val removedItemIndex = adapterPosition
                        documents.removeAt(removedItemIndex)
                        notifyItemRemoved(removedItemIndex)
                    }
                    val snackbar = listener.onChildAdapterClickEventMessage(adapterPosition, it)
                    snackbar.show()
                }
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserDocumentViewHolder {
        val binding = ListItemEditableDocumentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return UserDocumentViewHolder(binding, viewModel, activity, userId)
    }

    override fun getItemCount(): Int {
        return documents.size
    }

    override fun onBindViewHolder(holder: UserDocumentViewHolder, position: Int) {
        val doc = documents[position]
        holder.bindDocument(doc)
        holder.itemView
    }
}