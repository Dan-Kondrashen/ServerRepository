package ru.kondrashen.diplomappv20.presentation.fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexboxLayoutManager
import ru.kondrashen.diplomappv20.R
//import com.xiaofeng.flowlayoutmanager.FlowLayoutManager


import ru.kondrashen.diplomappv20.databinding.DocumentInfoFragmentBinding
import ru.kondrashen.diplomappv20.domain.ExtraInfoPageViewModel
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.adapters.SimpleExperienceAdapter
import ru.kondrashen.diplomappv20.presentation.adapters.SimpleKnowledgeAdapter
import ru.kondrashen.diplomappv20.presentation.adapters.SimpleSpecializationAdapter
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.repository.data_class.Knowledge
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocDependenceFullInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfoWithKnowledge
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ExperienceInfo

class DocumentInfoFragment: Fragment() {
    private lateinit var dataModel: ExtraInfoPageViewModel
    private lateinit var userAccountModel: UserAccountControlViewModel
    private lateinit var docInfo: DocumentInfoWithKnowledge
    private lateinit var docDependencies: MutableList<DocDependenceFullInfo>
    private lateinit var experience: MutableList<ExperienceInfo>
    private var knowledgeAdapter: SimpleKnowledgeAdapter? = null
    private var experienceAdapter: SimpleExperienceAdapter? = null
    private var specializationAdapter: SimpleSpecializationAdapter? = null
    private var _binding: DocumentInfoFragmentBinding? = null
    private val binding get() = _binding!!

    companion object{
        private const val ARG_DOCUMENT_ID = "docId"
        private const val ARG_USER_ID = "userId"
        private const val TAG = "DocInfoFragment"
        fun newInstance(docId: Int, userId: Int) = DocumentInfoFragment().apply {
            arguments = Bundle().apply{
                putInt(ARG_DOCUMENT_ID, docId)
                putInt(ARG_USER_ID, userId)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataModel = ViewModelProvider(requireActivity()).get(ExtraInfoPageViewModel::class.java)
        userAccountModel = ViewModelProvider(requireActivity()).get(UserAccountControlViewModel::class.java)

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DocumentInfoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val manager = FlowLayoutManager().maxItemsPerLine(5)
//        binding.knowledgeRecycle.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.knowledgeRecycle.layoutManager = FlexboxLayoutManager(requireContext())
        binding.specializationRecycle.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.experienceRecycle.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        updateUI()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun updateUI(){
        val userId = arguments?.getInt(ARG_USER_ID)
        val docId = arguments?.getInt(ARG_DOCUMENT_ID)?: 0
        if (isAdded) {
            dataModel.getDocumentInfoRoom(docId).observe(viewLifecycleOwner) { result ->
                result?.let {
                    docInfo = result
                    userId?.let { uId ->
                        binding.topArchiveCal.fabCallDialog.setOnClickListener {
                            val manager = (activity as AppCompatActivity).supportFragmentManager
                            val dialog = ChoseEventDialogFragment()
                            val bundle = Bundle()
                            bundle.putInt(PublicConstants.DOC_ID, docInfo.document.docId)
                            bundle.putInt(PublicConstants.USER_ID, uId)
                            dialog.arguments = bundle
                            dialog.show(manager, "result")
                        }
                    }
                    if (docInfo.document.type != "vacancy") {
                        binding.let {
                            var salaryTextF = ""
                            var salaryTextS = ""
                            docInfo.document.salaryF.let { salaryF ->
                                salaryTextF= "от ${salaryF} "
                            }
                            if(docInfo.document.salaryS != 0F){
                                salaryTextS = "до ${docInfo.document.salaryS}"
                            }

                            it.title.text = docInfo.document.title
                            it.fioInfo.text = docInfo.document.userFIO
                            it.dateInfo.text = docInfo.document.date
                            it.salaryF.text = salaryTextF
                            it.salaryS.text = salaryTextS
                            if (docInfo.docResponse.find { res -> res.type == "view" } == null)
                                userId?.let {
                                    userAccountModel.postResponseWithNoLiveData(
                                        it,
                                        listOf("view"),
                                        docId
                                    )
                                }
                            dataModel.getDocumentExperiencesRoom(docId).observe(viewLifecycleOwner){
                                experience = it as MutableList<ExperienceInfo>
                                if (isAdded) {
                                    activity?.let { active->
                                        experienceAdapter =
                                            SimpleExperienceAdapter(
                                                active as AppCompatActivity,
                                                experience,
                                                "simpleUsage",
                                                null,
                                                userAccountModel,
                                                userId
                                            )
                                        binding.experienceRecycle.adapter = experienceAdapter
                                        experienceAdapter?.let { adapt ->
                                            if (adapt.itemCount != 0) {
                                                binding.expText.visibility = VISIBLE
                                                binding.experienceRecycle.visibility = VISIBLE
                                            } else {
                                                binding.expText.visibility = GONE
                                                binding.experienceRecycle.visibility = GONE
                                            }
                                        }
                                    }
                                }
                            }
                            dataModel.getDocumentDependenceRoom(docId).observe(viewLifecycleOwner) {
                                docDependencies = it as MutableList<DocDependenceFullInfo>
                                if (isAdded) {
                                    activity?.let { active->
                                        specializationAdapter =
                                            SimpleSpecializationAdapter(
                                                active as AppCompatActivity,
                                                docDependencies,
                                                "simpleUsage",
                                                null,
                                                userAccountModel,
                                                userId
                                            )
                                        binding.specializationRecycle.adapter = specializationAdapter
                                        specializationAdapter?.let { adapt ->
                                            if (adapt.itemCount != 0) {
                                                binding.specText.visibility = VISIBLE
                                                binding.specializationRecycle.visibility = VISIBLE
                                            } else {
                                                binding.specText.visibility = GONE
                                                binding.specializationRecycle.visibility = GONE
                                            }
                                        }
                                    }
                                }

                            }
                            it.apply {
                                experienceInfo.text = getString(R.string.not_added)
                                docInfo.document.extra_info?.let { extr ->
                                    extraInfo.visibility = VISIBLE
                                    extraInfo.text = extr
                                } ?: run {
                                    extraInfo.visibility = GONE
                                    extraText.visibility = GONE
                                }
                                docInfo.document.contact_info?.let { contact ->
                                    contactInfo.visibility = VISIBLE
                                    contactInfo.text = contact
                                } ?: run {
                                    contactInfo.visibility = GONE
                                }
                            }
                            it.specialization.visibility = GONE
                            it.experience.visibility = GONE
                            knowledgeAdapter = SimpleKnowledgeAdapter(
                                docInfo.knowledge as MutableList<Knowledge>,
                                "view",
                                null
                            )
                            it.knowledgeRecycle.adapter = knowledgeAdapter
                            knowledgeAdapter?.let { adapt ->
                                if (adapt.itemCount != 0) {
                                    binding.knowledgeText.visibility = VISIBLE
                                    binding.knowledgeRecycle.visibility = VISIBLE
                                } else {
                                    binding.knowledgeText.visibility = GONE
                                    binding.knowledgeRecycle.visibility = GONE
                                }
                            }
                        }
                    }
                    else {
                        binding.let {
                            dataModel.getDocumentExperienceRoom(docId).observe(viewLifecycleOwner){
                                var experienceTime = it as MutableList<String>
                                if (isAdded) {
                                    activity?.let { active->
                                        if (experienceTime.size == 1){
                                            binding.experienceInfo.text = experienceTime[0]
                                        }
                                        else if (experienceTime.isEmpty()){
                                            binding.experienceInfo.text = getString(R.string.not_added)
                                        }
                                        else{
                                            var str = ""
                                            for (i in experienceTime){
                                                if (experienceTime.indexOf(i)+1 == experienceTime.size)
                                                    str += i
                                                else
                                                    str += "$i ${getString(R.string.or)}"
                                            }
                                        }
                                    }
                                }
                            }
                            dataModel.getDocumentDependenceRoom(docId).observe(viewLifecycleOwner) {
                                docDependencies = it as MutableList<DocDependenceFullInfo>
                                if (isAdded) {
                                    activity?.let { active->
                                        dataModel.getDocumentDependenceNamesRoom(docId).observe(viewLifecycleOwner) { depend ->
                                            var docDependencies = depend as MutableList<String>
                                            if (isAdded) {
                                                activity?.let { active->
                                                    if (docDependencies.size == 1){
                                                        binding.specializationInfo.text = docDependencies[0]
                                                    }
                                                    else if (docDependencies.isEmpty()){
                                                        binding.specializationInfo.text = getString(R.string.not_added_her)
                                                    }
                                                    else{
                                                        var str = ""
                                                        for (i in docDependencies){
                                                            if (docDependencies.indexOf(i)+1 == docDependencies.size)
                                                                str += i
                                                            else
                                                                str += "$i ${getString(R.string.or)}"
                                                        }
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }

                            }
                            it.specializationText.text = getString(R.string.role)
                            it.specialization.visibility = VISIBLE
                            it.experience.visibility = VISIBLE
                            it.expText.visibility = GONE
                            it.experienceRecycle.visibility = GONE
                            it.specText.visibility = GONE
                            it.specializationRecycle.visibility = GONE
                            var salaryTextF = ""
                            var salaryTextS = ""
                            docInfo.document.salaryF.let { salF ->
                                salaryTextF= "от ${salF} "
                            }
                            if(docInfo.document.salaryS != 0F){
                                salaryTextS = "до ${docInfo.document.salaryS}"
                            }
                            it.title.text = docInfo.document.title
                            it.fioInfo.text = docInfo.document.userFIO
                            it.dateInfo.text = docInfo.document.date

                            it.salaryF.text = salaryTextF
                            it.salaryS.text = salaryTextS
                            if (docInfo.docResponse.find { res -> res.type == "view" } == null)
                                userId?.let {
                                    userAccountModel.postResponseWithNoLiveData(
                                        it,
                                        listOf("view"),
                                        docId
                                    )
                                }
                            it.apply {
                                experienceInfo.text = getString(R.string.not_added)
                                docInfo.document.extra_info?.let { extr ->
                                    extraInfo.visibility = VISIBLE
                                    extraInfo.text = extr
                                } ?: run {
                                    extraInfo.visibility = GONE
                                    extraText.visibility = GONE
                                }
                                docInfo.document.contact_info?.let { contact ->
                                    contactInfo.visibility = VISIBLE
                                    contactInfo.text = contact
                                } ?: run {
                                    contactInfo.visibility = GONE
                                    contactText.visibility = GONE
                                }
                            }
                            knowledgeAdapter = SimpleKnowledgeAdapter(
                                docInfo.knowledge as MutableList<Knowledge>,
                                "view",
                                null
                            )
                            it.knowledgeRecycle.adapter = knowledgeAdapter
                            knowledgeAdapter?.let { adapt ->
                                if (adapt.itemCount != 0) {
                                    binding.knowledgeText.visibility = VISIBLE
                                    binding.knowledgeRecycle.visibility = VISIBLE
                                } else {
                                    binding.knowledgeText.visibility = GONE
                                    binding.knowledgeRecycle.visibility = GONE
                                }
                            }
                        }
                    }
                }
            }

        }
    }

}