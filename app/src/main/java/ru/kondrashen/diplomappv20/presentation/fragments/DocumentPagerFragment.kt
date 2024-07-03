package ru.kondrashen.diplomappv20.presentation.fragments
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.kondrashen.diplomappv20.databinding.DocumentsPagerFragmentBinding
import ru.kondrashen.diplomappv20.domain.MainPageViewModel

import ru.kondrashen.diplomappv20.presentation.adapters.DocumentPagerAdapter

import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfoWithKnowledge
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentPreference
import java.sql.Date
import java.util.Locale

class DocumentPagerFragment: Fragment() {
    private lateinit var dataModel: MainPageViewModel
    private var adapter: DocumentPagerAdapter? = null
    private var _binding: DocumentsPagerFragmentBinding? = null
    private var documents: MutableList<DocumentInfoWithKnowledge> = mutableListOf()
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataModel = ViewModelProvider(requireActivity()).get(MainPageViewModel::class.java)

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DocumentsPagerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUI()
    }
    private fun updateUI() {
        if (isAdded) {
            val userId = arguments?.getInt("userId")
            val docId = arguments?.getInt("docId")
            val type = arguments?.getString("itemsType")?: "all"
            val modification = arguments?.getString("modKey")?: "all"
            lifecycleScope.launch {
                dataModel.apply {
                    println(modification +"modmod")
                    if (modification != "desc" && modification != "asc"){
                        when(type) {
                            in listOf("vacancy", "resume")-> {
                                getMainPageData2FromRoom(
                                    type,
                                    modification
                                ).observeOneData(viewLifecycleOwner) {

                                    documents = it as MutableList<DocumentInfoWithKnowledge>
                                    adapter = DocumentPagerAdapter(
                                        requireActivity().supportFragmentManager,
                                        lifecycle,
                                        userId ?: 0,
                                        documents
                                    )
                                    binding.viewForDocPageInfo.adapter = adapter

                                    val i =
                                        documents.indexOfFirst { doc -> doc.document.docId == docId }
    //                                binding.viewForDocPageInfo.currentItem = i
                                    binding.viewForDocPageInfo.setCurrentItem(i, false)
                                }
                            }
                            else -> {
                                getRespDocsFromRoom(type).observe(viewLifecycleOwner) {
                                    documents = it as MutableList<DocumentInfoWithKnowledge>
                                    adapter = DocumentPagerAdapter(
                                        requireActivity().supportFragmentManager,
                                        lifecycle,
                                        userId ?: 0,
                                        documents
                                    )
                                    binding.viewForDocPageInfo.adapter = adapter
                                    val i =
                                        documents.indexOfFirst { doc -> doc.document.docId == docId }
                                    binding.viewForDocPageInfo.currentItem = i
                                }
                            }
                        }
                    }
                    else{
                        var number: Int? = null
                        var itemsType: String? = arguments?.getString("itemsType")
                        var orderType: String = arguments?.getString("typePref")?: "date"
                        var salaryF: Float? =
                            if (arguments?.getFloat("salaryF") != 0F && arguments?.getFloat("salaryF") != null)
                                arguments?.getFloat("salaryF")
                            else{
                                null
                            }
                        var salaryS: Float? =
                            if (arguments?.getFloat("salaryS") != 0F && arguments?.getFloat("salaryS") != null)
                                arguments?.getFloat("salaryS")
                            else{
                                null
                            }
                        println(orderType+ " type")
                        var dateStart: String? =
                            if (arguments?.getString("dateStart") != "null") {
                                arguments?.getString("dateStart")
                            }
                            else{
                                null
                            }
                        println(dateStart.toString()+ "date")
                        var dateEnd: String? =
                            if (arguments?.getString("dateEnd") != "null")
                                arguments?.getString("dateEnd")
                            else{
                                null
                            }
                        var num = arguments?.getInt("numItems")
                        val docPref = DocumentPreference(orderType,
                            salaryF,
                            salaryS,
                            dateStart,
                            dateEnd,
                            null,
                            num
                        )
                        println(docPref.toString() +"настройки")
                        itemsType?.let { docType ->
                            //заменить на type
                            dataModel.getFilteredDocumentsFromRoom(docType,
                                modification,
                                docPref
                            ).observeOneData(viewLifecycleOwner){ docList ->
                                documents = docList as MutableList<DocumentInfoWithKnowledge>
                                if (docList.isNotEmpty()) {
                                    adapter = DocumentPagerAdapter(
                                        requireActivity().supportFragmentManager,
                                        lifecycle,
                                        userId ?: 0,
                                        documents
                                    )
                                    binding.viewForDocPageInfo.adapter = adapter
                                    val i =
                                        documents.indexOfFirst { doc -> doc.document.docId == docId }
                                    binding.viewForDocPageInfo.currentItem = i
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    fun <T> LiveData<T>.observeOneData(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        var previousKey: Any? = value?: "NULL"
        observe(lifecycleOwner){
            if(previousKey == "NULL"){
                previousKey = value
                observer.onChanged(value!!)
            }
        }
    }
}