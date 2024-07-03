package ru.kondrashen.diplomappv20.presentation.adapters

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.ListItemDocumentAnalysisBinding
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentAnalysisInfo
import java.sql.Time
import java.util.Date


class DocumentAnalysisListAdapter(private var docAnalysis: MutableList<DocumentAnalysisInfo>,private var activity: AppCompatActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ListItemDocumentAnalysisBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return DocAnalysisViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return docAnalysis.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder1: DocAnalysisViewHolder = holder as DocAnalysisViewHolder
        val analysis = docAnalysis[position]
        holder1.bindAnalysis(analysis)
    }
    inner class DocAnalysisViewHolder(private val binding: ListItemDocumentAnalysisBinding): RecyclerView.ViewHolder(binding.root){
        private lateinit var docUsage: DocumentAnalysisInfo
        private lateinit var dataSet: PieDataSet
        fun bindAnalysis(docUsageS: DocumentAnalysisInfo){
            this.docUsage = docUsageS
            // Настройки диаграммы для правильной отрисовки пользователю
            var colors = mutableListOf<Int>()
            val items = mutableListOf<PieEntry>()
            val docViews = docUsageS.views.find { it.typeS == "view" }
            val docResponses = docUsageS.views.find { it.typeS == "response" }
            val docDismiss = docUsageS.views.find { it.typeS == "dismiss" }

            items.add(0, PieEntry((docViews?.numviews?: 0.0).toFloat(), getString(activity, R.string.numOfView)))
            colors.add(0, getColor(activity,androidx.appcompat.R.attr.colorPrimaryDark))

            items.add(1, PieEntry((docResponses?.numviews?: 0.0).toFloat(), getString(activity, R.string.numOfResp)))
            colors.add(1, getColor(activity,R.attr.colorSuccess))

            items.add(2, PieEntry((docDismiss?.numviews?: 0.0).toFloat(), getString(activity, R.string.numOfDismiss)))
            colors.add(2, getColor(activity,R.attr.colorAlert))
            dataSet = PieDataSet(items, "${getString(activity, R.string.userDocAnalysis)} ${docUsage.document.title}")
            dataSet.colors = colors
            dataSet.sliceSpace = 3F
            dataSet.selectionShift = 5F
            dataSet.valueTextSize = 11f
            dataSet.valueFormatter = PercentFormatter()
            dataSet.valueTextColor = getColor(activity, R.attr.colorPrimaryUltraDark)
            val result = (((docResponses?.numviews?: 0) - ((docDismiss?.numviews?: 0) * 0.35))/(docViews?.numviews?: 1)) * 100
            binding.apply {

                listItemDocumentTitleTextView.text = docUsage.document.title
                val data = PieData(dataSet)
                pieChartUserDocuments.data = data
                pieChartUserDocuments.rotationAngle = 0F;
                pieChartUserDocuments.isDrawHoleEnabled = true
                pieChartUserDocuments.holeRadius = 2F
                pieChartUserDocuments.transparentCircleRadius = 10F
                pieChartUserDocuments.isRotationEnabled = true;
                pieChartUserDocuments.highlightValues(null);
                if (docViews.toString() == "null" && docDismiss.toString() == "null" && docResponses.toString() == "null"){
                    analysisResultInfo.text =""
                    binding.pieChartUserDocuments.visibility = GONE
                    saveBtn.visibility = GONE
                    binding.analysisResultText.text = getString(activity, R.string.no_analytic_data_user_docs)
                }
                else{
                    analysisResultInfo.text = "${getString(activity, R.string.doc_effectivity_coff)} ${String.format("%.2f", result)}%"
                    pieChartUserDocuments.visibility = VISIBLE
                    pieChartUserDocuments.animateXY(500, 1300, Easing.EaseInOutBounce, Easing.EaseInExpo)
                    pieChartUserDocuments.invalidate()
                    saveBtn.visibility = VISIBLE
                    saveBtn.setOnClickListener {
                        pieChartUserDocuments.saveToGallery("user document: ${docUsage.document.title} graph of ${java.sql.Date(Date().time)} ${
                            Time(Date().time).toString().replace(":",".")}.jpg", 85)
                    }

                }



                pieChartUserDocuments.legend.apply {
                    textColor = getColor(activity,R.attr.colorPrimaryUltraDark)
                    textSize = 15f
                    xEntrySpace = 3f
                    yEntrySpace = 5f
                    setDrawInside(false)
                    entries
                    isWordWrapEnabled = true
                    val l1 =
                        LegendEntry(getString(activity, R.string.numOfResp), Legend.LegendForm.CIRCLE, 10f, 2f, null, getColor(activity, R.attr.colorSuccess))
                    val l2 =
                        LegendEntry(getString(activity, R.string.numOfView), Legend.LegendForm.CIRCLE, 10f, 2f, null, getColor(activity,androidx.appcompat.R.attr.colorPrimaryDark))
                    val l3 =
                        LegendEntry(getString(activity, R.string.numOfDismiss), Legend.LegendForm.CIRCLE, 10f, 2f, null, getColor(activity,R.attr.colorAlert))
                    setCustom(arrayOf(l1, l2, l3))
                }

            }
        }
    }
    fun getColor(context: Context, colorResId: Int): Int {
        val typedValue = TypedValue()
        val typedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(colorResId))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }
}