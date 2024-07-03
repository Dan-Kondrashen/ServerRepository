package ru.kondrashen.diplomappv20.presentation.adapters

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getString
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.ListItemDocumentResumeBinding
import ru.kondrashen.diplomappv20.databinding.ListItemDocumentVacancyBinding
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.presentation.fragments.ConfirmDialogFragment
import ru.kondrashen.diplomappv20.presentation.baseClasses.UserAnimatorHelper
import ru.kondrashen.diplomappv20.presentation.baseClasses.onItemClickInterface
import ru.kondrashen.diplomappv20.presentation.fragments.ChoseEventDialogFragment
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfoWithKnowledge



class DocumentListAdapterForCurUser(documents: MutableList<DocumentInfoWithKnowledge>, private var userId: Int, private var type: String, navController: NavController, private var activity: Activity, private var viewModel: UserAccountControlViewModel, private var listener: onItemClickInterface<User>): RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private var documents: MutableList<DocumentInfoWithKnowledge> = mutableListOf()
    private var documentsFilter: MutableList<DocumentInfoWithKnowledge> = mutableListOf()
    private val navController: NavController
    private var res = 0f
    private var oreintationHIndex = 0f
    init {
        this.documents = documents
        this.documentsFilter = documents
        this.navController = navController
    }
    inner class ViewHolderForVacancy(private val binding: ListItemDocumentVacancyBinding): RecyclerView.ViewHolder(binding.root), View.OnLongClickListener{
        private lateinit var document: DocumentInfoWithKnowledge
        fun bindDocument(doc: DocumentInfoWithKnowledge){
            this.document = doc
            binding.respId.text = document.docResponse.find { it.type == type }!!.id.toString()
            binding.deleteBtn.visibility = VISIBLE
            binding.chatBtn.setOnClickListener {
                println("Вот тут null ${doc.document.docId} и ${doc.document.userId}")
                val bundle = Bundle()
                bundle.putInt(PublicConstants.USER_ID, userId)
                bundle.putInt(PublicConstants.RESP_ID, binding.respId.text.toString().toInt())
                bundle.putInt(PublicConstants.DOC_ID, doc.document.docId)
                bundle.putInt(PublicConstants.ANOTHER_USER_ID, doc.document.userId)
                navController.navigate(
                    R.id.action_personalSpaceFragment_to_chatFragment,
                    bundle
                )
            }


            when(type){
                in listOf("response", "favorite") ->{
                    binding.chatBtn.visibility = VISIBLE
                }
                else -> binding.chatBtn.visibility = GONE
            }


            binding.listItemDocumentViewedCountInfo.visibility = GONE
            binding.deleteBtn.setOnClickListener {
                val manager =(activity as AppCompatActivity).supportFragmentManager
                val dialog = ConfirmDialogFragment()
                dialog.show(manager, "result")
                manager.setFragmentResultListener("status", activity as AppCompatActivity) { requestKey, bundle ->
                    val postedString = bundle.getString(requestKey).toString()
                    if (postedString == "success"){
                        viewModel.deleteDocDepRequest(binding.respId.text.toString().toInt(), userId).observe(activity as LifecycleOwner) {
                            if (it in listOf("Успешно удалено!", "Данный документ не найден на сервере!")) {
                                val removedItemIndex = adapterPosition
                                documents.removeAt(removedItemIndex)
                                notifyItemRemoved(removedItemIndex)
                            }
                            val snackbar = listener.onChildAdapterClickEventMessage(adapterPosition, it)
                            snackbar.show()
                        }

                    }
                }
                val animatorSet = UserAnimatorHelper().createPulsarScaleItemAnimation(binding.deleteBtn)
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
//                viewModel.deleteDocRequest(doc.document.docId, userId).observe(activity as LifecycleOwner){
//                    if (it in listOf("Успешно удалено!", "Данный документ не найден на сервере!")){
//                        val removedItemIndex = adapterPosition
//                        documents.removeAt(removedItemIndex)
//                        notifyItemRemoved(removedItemIndex)
//                    }
////                    val snackbar = listener.onChildAdapterClickEventMessage(adapterPosition, it)
////                    snackbar.show()
//                }
            }
            doc.document.numViews?.let {
                when {
                    it <= 0 -> {
                        binding.listItemDocumentViewedCountTextView.text = activity.getString(R.string.not_viewed)
                    }
                    else ->{
                        var suff =""
                        suff = when(it%10){
                            in listOf(2,3,4) -> if (it !in listOf(12,13,14))  "раз" else "раза"
                            else -> "раз"
                        }
                        binding.listItemDocumentViewedCountTextView.text = "Просмотрена "
                        binding.listItemDocumentViewedCountInfo.text = "${it} $suff"
                        binding.listItemDocumentViewedCountInfo.visibility = VISIBLE
                    }

                }
            }?: run {
                binding.listItemDocumentViewedCountTextView.text = activity.getString(R.string.not_viewed)
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
                    binding.listItemDocumentSalaryStartTextView.visibility = GONE
                }
                else -> binding.listItemDocumentSalaryStartTextView.text = "от ${document.document.salaryF} "
            }
            when(document.document.salaryS) {
                in listOf(0F, null) -> {
                    binding.listItemDocumentSalaryEndTextView.visibility = GONE
                }
                else -> binding.listItemDocumentSalaryEndTextView.text = "до ${document.document.salaryS} "
            }
            if (!binding.listItemDocumentSalaryStartTextView.isVisible and !binding.listItemDocumentSalaryEndTextView.isVisible){
                binding.listItemDocumentSalaryView.visibility = GONE
            }
            binding.listItemDocumentAuthorInfoView.text =doc.document.userFIO
            binding.root.setOnClickListener(clickOnItem(document.document.docId, type))
            binding.root.setOnLongClickListener(this)
        }
        override fun onLongClick(v: View?): Boolean {
            userId.let {
                val manager = (activity as AppCompatActivity).supportFragmentManager
                val dialog = ChoseEventDialogFragment()
                val bundle = Bundle()
                bundle.putInt(PublicConstants.DOC_ID, document.document.docId)
                bundle.putInt(PublicConstants.USER_ID, it)
                dialog.arguments = bundle
                dialog.show(manager, "result")
            }
            return true
        }
    }
    inner class ViewHolderForResumes(private val binding: ListItemDocumentResumeBinding): RecyclerView.ViewHolder(binding.root){
        private lateinit var document: DocumentInfoWithKnowledge
        fun bindDocument(doc: DocumentInfoWithKnowledge){
            this.document = doc
            binding.respId.text = document.docResponse.find { it.type == type }!!.id.toString()
            binding.chatBtn.visibility = VISIBLE
            binding.chatBtn.setOnClickListener {
                var bundle = Bundle()
                bundle.putInt(PublicConstants.USER_ID, userId)
                bundle.putInt(PublicConstants.RESP_ID, binding.respId.text.toString().toInt())
                bundle.putInt(PublicConstants.DOC_ID, doc.document.docId)
                bundle.putInt(PublicConstants.ANOTHER_USER_ID, doc.document.userId)
                navController.navigate(R.id.action_personalSpaceFragment_to_chatFragment, bundle)

            }
            when(type){
                in listOf("response", "favorite") ->{
                    binding.chatBtn.visibility = VISIBLE
                }
                else -> binding.chatBtn.visibility = GONE
            }
            binding.deleteBtn.visibility = VISIBLE
            binding.deleteBtn.setOnClickListener {
                val manager =(activity as AppCompatActivity).supportFragmentManager
                val dialog = ConfirmDialogFragment()
                dialog.show(manager, "result")
                manager.setFragmentResultListener("status", activity as AppCompatActivity) { requestKey, bundle ->
                    val postedString = bundle.getString(requestKey).toString()
                    if (postedString == "success"){
                        viewModel.deleteDocDepRequest(binding.respId.text.toString().toInt(), userId).observe(activity as LifecycleOwner) {
                            if (it in listOf("Успешно удалено!", "Данный документ не найден на сервере!")) {
                                val removedItemIndex = adapterPosition
                                documents.removeAt(removedItemIndex)
                                notifyItemRemoved(removedItemIndex)
                                val snackbar = listener.onChildAdapterClickEventMessage(
                                    adapterPosition,
                                    getString(activity, R.string.success)
                                )
                                snackbar.show()
                            }
                            else {

                                val snackbar = listener.onChildAdapterClickEventMessage(
                                    adapterPosition,
                                    getString(activity, R.string.server_not_avilable)
                                )
                                snackbar.show()
                            }
                        }
                    }
                }
                val animatorSet = UserAnimatorHelper().createPulsarScaleItemAnimation(binding.deleteBtn)
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
            }

            binding.listItemDocumentViewedCountInfo.visibility = GONE
            doc.document.numViews?.let {
                when {
                    it <= 0 -> {
                        binding.listItemDocumentViewedCountTextView.text = activity.getString(R.string.not_viewed)
                    }
                    else ->{
                        var suff =""
                        suff = when(it%10){
                            in listOf(2,3,4) -> if (it !in listOf(12,13,14))  "раз" else "раза"
                            else -> "раз"
                        }
                        binding.listItemDocumentViewedCountTextView.text = "Просмотрена "
                        binding.listItemDocumentViewedCountInfo.text = "${it} $suff"
                        binding.listItemDocumentViewedCountInfo.visibility = VISIBLE
                    }
                }
            }?: run {
                binding.listItemDocumentViewedCountTextView.text = activity.getString(R.string.not_viewed)
            }
            binding.listItemDocumentTitleTextView.text = doc.document.title
            binding.listItemDocumentDateInfo.text =doc.document.date
            when(document.document.salaryF) {
                in listOf(0F, null) -> {
                    binding.listItemDocumentSalaryStartTextView.visibility = GONE
                }
                else -> binding.listItemDocumentSalaryStartTextView.text = "от ${document.document.salaryF} "
            }
            when(document.document.salaryS) {
                in listOf(0F, null) -> {
                    binding.listItemDocumentSalaryEndTextView.visibility = GONE
                }
                else -> binding.listItemDocumentSalaryEndTextView.text = "до ${document.document.salaryS} "
            }
            if (!binding.listItemDocumentSalaryStartTextView.isVisible and !binding.listItemDocumentSalaryEndTextView.isVisible){
                binding.listItemDocumentSalaryView.visibility = GONE
            }
            binding.listItemDocumentAuthorInfoView.text =doc.document.userFIO

            if (doc.knowledge.isNotEmpty()) {
                val result = doc.knowledge.joinToString(", ") { know ->  know.name }
                binding.listItemDocumentKnowInfo.text = result
            }
            else binding.listItemDocumentKnowInfo.text = "Не указанны!"
            if (doc.specializations.isNotEmpty()) {
                val result = doc.specializations.joinToString(", ") { spec -> spec.name }
                binding.listItemDocumentSpecialTextView.text = result
            }
            else binding.listItemDocumentSpecialTextView.text = "Специальность не указана!"
            binding.root.setOnClickListener(clickOnItem(document.document.docId, type))
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (documents.get(position).document.type=="resume")
            return 1
        else{
            return 2
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int
    ): RecyclerView.ViewHolder {
        if (viewType == 1){
            val binding = ListItemDocumentResumeBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
            return ViewHolderForResumes(binding)
        }
        else {
            val binding = ListItemDocumentVacancyBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
            return ViewHolderForVacancy(binding)
        }
    }

    override fun getItemCount() = documents.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder1: ViewHolderForResumes
        val holder2: ViewHolderForVacancy
        val doc = documents[position]
        if(holder.itemViewType == 1){
            holder1 = holder as ViewHolderForResumes
            holder1.bindDocument(doc)
        }
        else {
            holder2 = holder as ViewHolderForVacancy
            holder2.bindDocument(doc)
        }
    }

    private fun clickOnItem(docId: Int, type: String): View.OnClickListener{
        return View.OnClickListener { v ->
            val bundle = Bundle()
            bundle.putInt(PublicConstants.USER_ID, userId?: 0)
            bundle.putInt(PublicConstants.DOC_ID, docId)
            bundle.putString("itemsType", type)
            v!!.setBackgroundResource(R.drawable.border_for_item_animation)
            v.isClickable = false
            v.elevation = 10000f
            (v.parent as RecyclerView).bringToFront()
            (v.parent as RecyclerView).clipToPadding = false
            val resources = v.context.resources
            val orientation = resources.configuration.orientation
            val resultW = resources.displayMetrics.widthPixels
            val resultH = resources.displayMetrics.heightPixels
            val location = IntArray(2)
            v.getLocationOnScreen(location)
            val appbar = activity.findViewById<MaterialToolbar>(R.id.toolbar)
            appbar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val toolbarHeight = appbar.measuredHeight
            when (orientation) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    oreintationHIndex = 0.98f
                    res = (((resultH/2)*oreintationHIndex - v.height/2 + toolbarHeight) - location[1])
                }
                Configuration.ORIENTATION_LANDSCAPE -> {
                    oreintationHIndex = 0.88f
                    res = ((((((resultH / 2) * oreintationHIndex) - (v.height / 2)) + toolbarHeight) - location[1]))
                }

                else -> oreintationHIndex = 0f
            }
            for (i in 0..(v as ViewGroup).childCount){
                if (v.getChildAt(i) != null)
                    v.getChildAt(i).animate()
                        .alpha(0f)
                        .setDuration(300)
                        .start()
            }
            var targetW =(resultW -v.width)/4
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(v, "X", (targetW).toFloat()),
                ObjectAnimator.ofFloat(v, "Y", res),
                ObjectAnimator.ofFloat(v, "scaleX", ((resultW*0.96f)/v.width.toFloat())),
                ObjectAnimator.ofFloat(v, "scaleY", (((resultH - toolbarHeight)/v.height.toFloat()))),
            )
            animatorSet.interpolator = AccelerateDecelerateInterpolator()
            animatorSet.addListener(object: AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    navController.navigate(R.id.action_personalSpaceFragment_to_pagerFragment, bundle)
                }
            })
            animatorSet.duration = 400
            animatorSet.start()
        }
    }
    fun setDocItems(documents: MutableList<DocumentInfoWithKnowledge>){
        this.documents = documents
    }
    fun clearDocItems(){
        this.documents = mutableListOf()
    }

    override fun getFilter(): Filter {
        return  object  : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val resultOfFiltration = FilterResults()
                if(constraint == null || constraint.length <0){
                    resultOfFiltration.count = documentsFilter.size
                    resultOfFiltration.values = documentsFilter
                }else{
                    var str = constraint.toString().lowercase()
                    var docss = mutableListOf<DocumentInfoWithKnowledge>()
                    for(item in documentsFilter){
                        if(item.document.title.lowercase().contains(str)||item.document.salaryF.toString().lowercase().contains(str)||item.document.salaryS.toString().lowercase().contains(str)||item.document.userFIO.lowercase().contains(str)){
                            docss.add(item)
                        }
                    }
                    resultOfFiltration.count = docss.size
                    resultOfFiltration.values = docss
                }
                return resultOfFiltration
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                documents =  results!!.values as MutableList<DocumentInfoWithKnowledge>
                notifyDataSetChanged()
            }

        }
    }
}