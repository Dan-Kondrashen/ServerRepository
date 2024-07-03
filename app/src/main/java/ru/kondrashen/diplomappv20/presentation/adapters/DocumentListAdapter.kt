package ru.kondrashen.diplomappv20.presentation.adapters


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Filter
import android.widget.Filterable
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat.getString
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.ListItemDocumentResumeBinding
import ru.kondrashen.diplomappv20.databinding.ListItemDocumentVacancyBinding
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.baseClasses.ImageFactory
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.presentation.fragments.ChoseEventDialogFragment
import ru.kondrashen.diplomappv20.presentation.fragments.DocumentInfoFragment
import ru.kondrashen.diplomappv20.presentation.fragments.EditArchiveDialogFragment
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfoWithKnowledge

class DocumentListAdapter(documents: MutableList<DocumentInfoWithKnowledge>, private var userId: Int?, private var type: String, private var mod: String, navController: NavController, private var activity: Activity): RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private var documents: MutableList<DocumentInfoWithKnowledge> = mutableListOf()
    private var documentsFilter: MutableList<DocumentInfoWithKnowledge> = mutableListOf()
    private lateinit var userAccountModel: UserAccountControlViewModel
    private val navController: NavController
    private var res = 0f
    private var oreintationHIndex = 0f

    init {
        this.documents = documents
        this.documentsFilter = documents
        this.navController = navController
        userAccountModel = ViewModelProvider(activity as AppCompatActivity).get(UserAccountControlViewModel::class.java)
    }

    inner class ViewHolderForVacancy(private val binding: ListItemDocumentVacancyBinding): RecyclerView.ViewHolder(binding.root), View.OnLongClickListener{
        private lateinit var document: DocumentInfoWithKnowledge
        private var isOpened: Boolean = false
        fun bindDocument(doc: DocumentInfoWithKnowledge){
            this.document = doc
            setViewStyle(binding.root, doc)
            ImageFactory.setUserIcon(binding.root, document.document.userId, activity)
//            for (docResp in document.docResponse){
//                when(docResp.type){
//                    "Просмотр" -> {
//                        val colorArr = activity.theme.obtainStyledAttributes(intArrayOf(androidx.appcompat.R.attr.colorPrimaryDark))
//                        val colorPrimaryDark = colorArr.getColor(0, 0)
//                        colorArr.recycle()
//                        binding.root.setBackgroundResource(R.drawable.border_for_viewed_documents_item)
//                        binding.listItemDocumentTitleTextView.setBackgroundResource(R.drawable.border_for_document_preview_viewed)
//                        binding.listItemDocumentViewedCountInfo.setBackgroundResource(R.drawable.border_for_document_preview_viewed)
//                        setTextColor(binding.root, colorPrimaryDark)
//
//                    }
//                }
//            }
            binding.listItemDocumentViewedCountInfo.visibility = GONE
            doc.document.numViews?.let {
                when {
                    it <= 0 -> {
                        binding.listItemDocumentViewedCountTextView.text = activity.getString(R.string.not_viewed)
                    }
                    else ->{
                        var suff =""
                        suff = when(it%10){
                            in listOf(2,3,4) ->
                                if (it !in listOf(12,13,14))  "раз"
                                else activity.getString(R.string.times)
                            else -> "раз"
                        }
                        binding.listItemDocumentViewedCountTextView.text =
                            activity.getString(R.string.viewed)
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
            binding.listItemDocumentCompanyInfoView.text = doc.document.extraName
            if (doc.experience.isNotEmpty())
                binding.listItemDocumentExpInfo.text = getString(activity, R.string.has_exp_emr)
            else
                binding.listItemDocumentExpInfo.text = getString(activity, R.string.no_exp_emr)
            binding.root.setOnClickListener(clickOnItem(document.document.docId,  type, mod))
            binding.root.setOnLongClickListener(this)
        }
        override fun onLongClick(v: View?): Boolean {
            userId?.let {
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
    inner class ViewHolderForResumes(private val binding: ListItemDocumentResumeBinding): RecyclerView.ViewHolder(binding.root), View.OnLongClickListener{
        private lateinit var document: DocumentInfoWithKnowledge
        private var isOpened: Boolean = false

        fun bindDocument(doc: DocumentInfoWithKnowledge){
            this.document = doc
            setViewStyle(binding.root, doc)
            ImageFactory.setUserIcon(binding.root, document.document.userId, activity)
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
            binding.listItemDocumentAuthorInfoView.text =doc.document.extraName + " "+doc.document.userFIO

            if (doc.knowledge.isNotEmpty()) {
                val result = doc.knowledge.joinToString(", ") { know ->  know.name }
                binding.listItemDocumentKnowInfo.text = result
            }
            else binding.listItemDocumentKnowInfo.text = getString(activity, R.string.not_added_them)
            if (doc.specializations.isNotEmpty()) {
                val result = doc.specializations.joinToString(", ") { spec -> spec.name }
                binding.listItemDocumentSpecialTextView.text = result
            }
            else binding.listItemDocumentSpecialTextView.text = getString(activity, R.string.not_added_spec)
            if (doc.experience.isNotEmpty())
                binding.listItemDocumentExpInfo.text = getString(activity, R.string.has_exp)
            else
                binding.listItemDocumentExpInfo.text = getString(activity, R.string.no_exp)
            binding.root.setOnClickListener(clickOnItem(document.document.docId, type, mod))
            binding.root.setOnLongClickListener(this)

        }
        override fun onLongClick(v: View?): Boolean {
            userId?.let {
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

    override fun getItemCount() = documents.size ?: 0

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

    private fun setViewStyle(view: View, document: DocumentInfoWithKnowledge){
        val docResp = document.docResponse.find { it.type == "view" }
        docResp?.let {
            val colorArr = activity.theme.obtainStyledAttributes(intArrayOf(androidx.appcompat.R.attr.colorPrimaryDark))
            val colorPrimaryDark = colorArr.getColor(0, 0)
            colorArr.recycle()
            view.setBackgroundResource(R.drawable.border_for_viewed_documents_item)
            view.findViewById<TextView>(R.id.list_item_document_title_text_view).setBackgroundResource(R.drawable.border_for_document_preview_viewed)
            view.findViewById<TextView>(R.id.list_item_document_viewed_count_info).setBackgroundResource(R.drawable.border_for_document_preview_viewed)
            setTextColor(view, colorPrimaryDark)
        }?: run {
            val colorArr = activity.theme.obtainStyledAttributes(intArrayOf(R.attr.colorPrimaryUltraDark))
            val colorPrimaryUltraDark = colorArr.getColor(0, 0)
            view.setBackgroundResource(R.drawable.border_for_main_viewed_item)
            view.findViewById<TextView>(R.id.list_item_document_title_text_view).setBackgroundResource(R.drawable.border_for_document_preview)
            view.findViewById<TextView>(R.id.list_item_document_viewed_count_info).setBackgroundResource(R.drawable.border_for_document_preview)
            setTextColor(view, colorPrimaryUltraDark)
        }
    }
    fun setTextColor(view: View, color: Int) {
        if (view is TextView) {
            view.setTextColor(color)
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setTextColor(view.getChildAt(i), color)
            }
        }
    }
    fun submitList(items: MutableList<DocumentInfoWithKnowledge>){
        documents.clear()
        documents.addAll(items)
        notifyDataSetChanged()
    }

    private fun clickOnItem(docId: Int, type: String, mod: String): View.OnClickListener{
        return View.OnClickListener { v ->

            val bundle = Bundle()
            bundle.putInt("userId", userId?: 0)
            bundle.putInt("docId", docId)
            bundle.putString("itemsType", type)
            bundle.putString("modKey", mod)
            v!!.setBackgroundResource(R.drawable.border_for_item_animation)
            v.isClickable = false
            v.elevation = 10000f
            v.translationZ = 2000f
            (v.parent as RecyclerView).bringToFront()
            val resources = v.context.resources
            val orientation = resources.configuration.orientation
            val resultW = resources.displayMetrics.widthPixels
            val resultH = resources.displayMetrics.heightPixels
            val location = IntArray(2)
            v.getLocationOnScreen(location)
            val appbar = activity.findViewById<MaterialToolbar>(R.id.toolbar)
            appbar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val toolbarHeight = appbar.measuredHeight
//            val itemY =(((v.parent as RecyclerView).parent as ConstraintLayout).parent as ScrollView).scrollY
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
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(v, "X", 150f),
                ObjectAnimator.ofFloat(v, "Y", res),
                ObjectAnimator.ofFloat(v, "scaleX", ((resultW*0.96f)/v.width.toFloat())),
                ObjectAnimator.ofFloat(v, "scaleY", (((resultH - toolbarHeight)/v.height.toFloat()))),
            )
            var parentView = ((((v.parent as RecyclerView).parent as ConstraintLayout).parent as ScrollView).parent as RelativeLayout)
            var extraBar = parentView.findViewById<CoordinatorLayout>(R.id.extra_bar)
            var bottomBar = parentView.findViewById<CoordinatorLayout>(R.id.bottom_bar)
            animatorSet.interpolator = AccelerateDecelerateInterpolator()
            animatorSet.addListener(object: AnimatorListenerAdapter(){
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    extraBar.visibility = GONE
                    bottomBar.visibility = GONE
                }
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)

                    if (navController.currentDestination?.id == R.id.mainFragmentView)
                        navController.navigate(
                            R.id.action_mainFragment_to_pagerFragment, bundle)

                }
            })
            animatorSet.duration = 400
            animatorSet.start()
        }
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