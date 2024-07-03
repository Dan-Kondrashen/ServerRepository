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
import android.widget.ScrollView

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.ListItemDocumentResumeBinding
import ru.kondrashen.diplomappv20.databinding.ListItemDocumentVacancyBinding
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfo
import ru.kondrashen.diplomappv20.repository.data_class.DocResponse


class DocumentListAdapter2(documents: MutableList<DocumentInfo>, private var type: String, private var mod: String, navController: NavController, private var activity: Activity): RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
        private var documents: MutableList<DocumentInfo> = mutableListOf()
        private var docId: Int = 0
        private var documentsFilter: MutableList<DocumentInfo> = mutableListOf()
        private var docResponses: MutableList<DocResponse> = mutableListOf()
        var positionAdaptern: Int = 0
        private val navController: NavController
        private var res = 0f
        private var oreintationHIndex = 0f
        private fun clickOnItem(docId: Int, type: String, mod: String): View.OnClickListener{
            return View.OnClickListener { v ->
                val bundle = Bundle()
                bundle.putInt("docId", docId)
                bundle.putString("itemsType", type)
                bundle.putString("modKey", mod)
                v!!.setBackgroundResource(R.drawable.border_for_item_animation)
                v.isClickable = false
                v.elevation = 10000f
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
                println(toolbarHeight)
                val itemY =(((v.parent as RecyclerView).parent as ConstraintLayout).parent as ScrollView).scrollY
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
                animatorSet.interpolator = AccelerateDecelerateInterpolator()
                animatorSet.addListener(object: AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        navController.navigate(R.id.action_mainFragment_to_pagerFragment, bundle)
                    }
                })
                animatorSet.duration = 400
                animatorSet.start()
            }
        }
        init {
            this.documents = documents
//        this.docResponses = docresponses
            this.documentsFilter = documents
            this.navController = navController
        }

        inner class ViewHolderForVacancy(private val item: ListItemDocumentVacancyBinding): RecyclerView.ViewHolder(item.root), View.OnLongClickListener{
            private lateinit var document: DocumentInfo
            private var isOpened: Boolean = false

            fun bindDocument(doc: DocumentInfo){
                this.document = doc

                var salaryTextF = when(document.salaryF) {
                    in listOf(0F, null) -> ""
                    else -> "от ${document.salaryF} "
                }
                var salaryTextS = when(document.salaryS) {
                    in listOf(0F, null) -> ""
                    else -> "до ${document.salaryS} "
                }
                item.listItemDocumentTitleTextView.text = doc.title
//                item.listItemDocumentDateInfo.text =doc.date
//                item.listItemDocumentSalaryStartTextView.text = salaryTextF
//                item.listItemDocumentSalaryEndTextView.text = salaryTextS
                item.listItemDocumentAuthorTextView.text =doc.userFIO
                item.root.setOnClickListener(clickOnItem(document.docId,  type, mod))
                item.root.setOnLongClickListener(this)
            }
            override fun onLongClick(v: View?): Boolean {
//                val recyclerView = v?.parent as RecyclerView
//                recyclerView.adapter?.notifyDataSetChanged()
                return true
            }
        }
        inner class ViewHolderForResumes(private val binding: ListItemDocumentResumeBinding): RecyclerView.ViewHolder(binding.root), View.OnLongClickListener{
            private lateinit var document: DocumentInfo
            private var isOpened: Boolean = false

            fun bindDocument(doc: DocumentInfo){
                this.document = doc
                binding.listItemDocumentTitleTextView.text = doc.title
                binding.listItemDocumentDateInfo.text =doc.date
                binding.listItemDocumentSalaryStartTextView.text = doc.salaryF.toString()
                binding.listItemDocumentSalaryEndTextView.text = doc.salaryS.toString()
                binding.listItemDocumentAuthorInfoView.text =doc.userFIO
                binding.root.setOnClickListener(clickOnItem(document.docId, "resume", "new"))
                binding.root.setOnLongClickListener(this)

            }
            override fun onLongClick(v: View?): Boolean {

                return true
            }
        }


        override fun getItemViewType(position: Int): Int {
            if (documents.get(position).type=="resume")
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

        fun setDocuments(docs: MutableList<DocumentInfo>){
            this.documents = docs
            this.documentsFilter = docs
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

        override fun getFilter(): Filter {
            return  object  : Filter(){
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val resultOfFiltration = FilterResults()
                    if(constraint == null || constraint.length <0){
                        resultOfFiltration.count = documentsFilter.size
                        resultOfFiltration.values = documentsFilter
                    }else{
                        var str = constraint.toString().lowercase()
                        var docss = mutableListOf<DocumentInfo>()
                        for(item in documentsFilter){
                            if(item.title.lowercase().contains(str)||item.salaryF.toString().lowercase().contains(str)||item.salaryS.toString().lowercase().contains(str)||item.userFIO.toString().lowercase().contains(str)){
                                docss.add(item)
                            }
                        }
                        resultOfFiltration.count = docss.size
                        resultOfFiltration.values = docss
                    }
                    return resultOfFiltration
                }

                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    documents =  results!!.values as MutableList<DocumentInfo>
                    notifyDataSetChanged()
                }

            }
        }
    }