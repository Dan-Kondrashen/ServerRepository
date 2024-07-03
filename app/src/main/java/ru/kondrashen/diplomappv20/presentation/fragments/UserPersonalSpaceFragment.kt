package ru.kondrashen.diplomappv20.presentation.fragments

import android.animation.ObjectAnimator
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.UserPersonalSpaceFragmentBinding
import ru.kondrashen.diplomappv20.domain.ExtraInfoPageViewModel
import ru.kondrashen.diplomappv20.domain.MainPageViewModel
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.adapters.AnalysisPagerAdapter
import ru.kondrashen.diplomappv20.presentation.adapters.DocumentListAdapterForCurUser
import ru.kondrashen.diplomappv20.presentation.adapters.LinearHorizontalArrayAdapter
import ru.kondrashen.diplomappv20.presentation.adapters.SimpleExperienceAdapter
import ru.kondrashen.diplomappv20.presentation.adapters.SimpleSpecializationAdapter
import ru.kondrashen.diplomappv20.presentation.adapters.UserDocumentListAdapter
import ru.kondrashen.diplomappv20.presentation.baseClasses.ImageFactory
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.presentation.baseClasses.onItemClickInterface
import ru.kondrashen.diplomappv20.repository.data_class.Archive
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocDependenceFullInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfoWithKnowledge
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ExperienceInfo
import java.util.Date
import java.util.Locale

class UserPersonalSpaceFragment: Fragment(), onItemClickInterface<User> {
    private lateinit var userAccountModel: UserAccountControlViewModel
    private lateinit var extraInfoModel: ExtraInfoPageViewModel
    private lateinit var dataModel: MainPageViewModel
    private var specializationAdapter: SimpleSpecializationAdapter? = null
    private var experienceAdapter: SimpleExperienceAdapter? = null
    private var userDocumentsAdapter: UserDocumentListAdapter? = null
    private var respDocumentsAdapter: DocumentListAdapterForCurUser? = null
    private var archiveAdapter: LinearHorizontalArrayAdapter? = null
    private var archiveList: MutableList<Archive> = mutableListOf()
    private var documentsList: MutableList<DocumentInfoWithKnowledge> = mutableListOf()
    private var userDocumentsList: MutableList<DocumentInfoWithKnowledge> = mutableListOf()
    private val formater = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
    private var text =""
    private var oldText =""
    private var userId: Int? = null
    private var specFlag = 0
    private var expFlag = 0
    private var archFlag = 0
    private var userRoleId: Int? = null
    private var userType: String? = null
    private var _binding: UserPersonalSpaceFragmentBinding? =null
    private var isSpecOpen: Boolean = true
    private var isExpOpen: Boolean = true
    private var isDocsOpen: Boolean = true
    private var isRespOpen: Boolean = true
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userAccountModel = ViewModelProvider(requireActivity()).get(UserAccountControlViewModel::class.java)
        dataModel = ViewModelProvider(requireActivity()).get(MainPageViewModel::class.java)
        extraInfoModel = ViewModelProvider(requireActivity()).get(ExtraInfoPageViewModel::class.java)
        userAccountModel.getSpecializationInfoFromServ()
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bottomBar.apply {
            homePage.setImageResource(R.drawable.home_dark_svg)
            analysisPage.setOnClickListener {
                val bundle = Bundle()
                userId = arguments?.getInt(PublicConstants.USER_ID)
                bundle.putInt(PublicConstants.USER_ID, userId?: 0)
                findNavController().navigate(R.id.action_personalSpaceFragment_to_analysisPrefFragment, bundle)
            }
            chatPage.setOnClickListener {
                val bundle = Bundle()
                userId = arguments?.getInt("userId")
                bundle.putInt("userId", userId?: 0)
                findNavController().navigate(R.id.action_personalSpaceFragment_to_chatChoseFragment, bundle)
            }
            homePage.setOnClickListener {
                val bundle = Bundle()
                userId = arguments?.getInt("userId")
                bundle.putString(PublicConstants.USER_TYPE, userType?: "соискатель")
                bundle.putInt("userId", userId?: 0)
                findNavController().navigate(R.id.action_personalSpaceFragment_to_mainFragmentEmployee, bundle)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = UserPersonalSpaceFragmentBinding.inflate(layoutInflater, container, false)
        arguments?.getInt("userId")?.let { userId ->
            binding.editProfileAvatarBtn.setOnClickListener {
                Toast.makeText(requireContext(), "edit", Toast.LENGTH_SHORT).show()
            }
            binding.addDocumentBtn.setOnClickListener {
                val bundle = Bundle()
                bundle.putInt("userId", userId ?: 0)
//            binding.userRole.text.toString()
                if (userRoleId == 1) {
                    bundle.putString("docType", "vacancy")
                } else if (userRoleId == 2) {
                    bundle.putString("docType", "resume")
                } else {
                    bundle.putString("docType", "selectable")
                }
                findNavController().navigate(
                    R.id.action_personalSpaceFragment_to_editDocumentFragment,
                    bundle
                )
            }
            binding.addResponseBtn.setOnClickListener {
                val manager = parentFragmentManager
                val dialog = EditArchiveDialogFragment()
                var bundle = Bundle()
                bundle.putString("itemType", "archive")
                dialog.arguments = bundle
                dialog.show(manager, "result")
                manager.setFragmentResultListener(
                    "name",
                    viewLifecycleOwner
                ) { requestKey, bundle ->
                    val postedName = bundle.getString(requestKey).toString()
                    userAccountModel.postArchive(userId, postedName).observe(requireActivity()){
                        if (it.status.contains("success")) {
                            Snackbar.make(
                                binding.root,
                                getText(R.string.success),
                                Snackbar.LENGTH_LONG
                            ).show()
                            archiveAdapter?.let {adapter ->
                                if (it.id != null) {
                                    adapter.addItemToList(Archive(it.id, postedName,postedName, userId))
                                    adapter.notifyItemInserted(adapter.itemCount)
                                    binding.responseRecycle.smoothScrollToPosition(adapter.itemCount)
                                }
                            }
                        }
                        else if (it.status == "No connection to server")
                            Snackbar.make(binding.root,
                                getText(R.string.no_connection),
                                Snackbar.LENGTH_LONG).show()
                        else
                            Snackbar.make(binding.root, it.status, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            binding.addExperienceBtn.setOnClickListener {
                val manager = parentFragmentManager
                val dialog = EditExperienceDialogFragment()
                dialog.show(manager, "result")
                manager.setFragmentResultListener(
                    "filename",
                    viewLifecycleOwner
                ) { requestKey, bundle ->
                    bundle.getString(requestKey)?.let {
                        Toast.makeText(requireActivity(), "Есть файл!", Toast.LENGTH_SHORT).show()
                        val postedString = bundle.getString(requestKey).toString()
                        val postedSuff = bundle.getString("suff").toString()
                        val postedName = bundle.getString("name").toString()
                        val postedExpTime = bundle.getString("expTime").toString()
                        val postedExperience = bundle.getString("experience").toString()
                        val postedRole = bundle.getString("role").toString()
                        val postedPlace = bundle.getString("place").toString()
                        val uri = Uri.parse(postedString)
                        val contentResolver = requireContext().contentResolver
                        val inputStream = contentResolver.openInputStream(uri)
                        val body = RequestBody.create(
                            MediaType.parse("application/octet"),
                            inputStream!!.readBytes()
                        )
                        inputStream.close()
                        val date = formater.format(Date())
                        val fileName = "file$postedSuff"
                        val resName = RequestBody.create(MultipartBody.FORM, postedName)
                        val resRole = RequestBody.create(MultipartBody.FORM, postedRole)
                        val resPlace = RequestBody.create(MultipartBody.FORM, postedPlace)
                        val resExperience = RequestBody.create(MultipartBody.FORM, postedExperience)

                        val addFile = MultipartBody.Part.createFormData("file", fileName, body)
                        extraInfoModel.getExperienceTimeId(postedExpTime)
                            .observe(requireActivity()) {expTimeId ->
                                val resExpTime = RequestBody.create(MultipartBody.FORM, expTimeId.toString())
                                userAccountModel.postExpFile(addFile, userId, userId, resName, resRole, resPlace, resExpTime, resExperience).observe(requireActivity()) {
                                   Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                                    isExpOpen = !isExpOpen
                                    TransitionManager.beginDelayedTransition(binding.root)
                                    binding.addExperienceBtn.visibility = GONE
                                    binding.experienceRecycle.visibility = GONE
                                    binding.buttonExp.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                                }
                            }
                    } ?: run {
                        val postedExpTime = bundle.getString("expTime").toString()
                        val postedExperience = bundle.getString("experience").toString()
                        val postedRole = bundle.getString("role").toString()
                        val postedPlace = bundle.getString("place").toString()
                        extraInfoModel.getExperienceTimeId(postedExpTime)
                            .observe(requireActivity()) { expTimeId ->
                                val resExpTime = RequestBody.create(MultipartBody.FORM, expTimeId.toString())
                                val resName = RequestBody.create(MultipartBody.FORM, "")
                                val resRole = RequestBody.create(MultipartBody.FORM, postedRole)
                                val resPlace = RequestBody.create(MultipartBody.FORM, postedPlace)
                                val resExperience = RequestBody.create(MultipartBody.FORM, postedExperience)
                                val body = RequestBody.create(MultipartBody.FORM, "")
                                val addFile = MultipartBody.Part.createFormData("file", "", body)
                                userAccountModel.postExpFile(addFile, userId, userId, resName, resRole, resPlace, resExpTime, resExperience).observe(requireActivity()) {
                                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                                }
                            }
                    }
                }
            }
            binding.addSpecializationBtn.setOnClickListener {
                val manager = parentFragmentManager
                val dialog = EditSpecializationDialogFragment()
                dialog.show(manager, "result")
                manager.setFragmentResultListener(
                    "filename",
                    viewLifecycleOwner
                ) { requestKey, bundle ->
                    bundle.getString(requestKey)?.let {
                        Toast.makeText(requireActivity(), "Есть файл!", Toast.LENGTH_SHORT).show()
                        val postedString = bundle.getString(requestKey).toString()
                        val postedSuff = bundle.getString("suff").toString()
                        val postedName = bundle.getString("name").toString()
                        val postedSpecialization = bundle.getString("specialization").toString()
                        val postedEducation = bundle.getString("education").toString()
                        val uri = Uri.parse(postedString)
                        val contentResolver = requireContext().contentResolver
                        val inputStream = contentResolver.openInputStream(uri)
                        val body = RequestBody.create(
                            MediaType.parse("application/octet"),
                            inputStream!!.readBytes()
                        )
                        val date = formater.format(Date())
                        val fileName = "file$postedSuff"
                        val resName = RequestBody.create(MultipartBody.FORM, postedName)
                        val addFile = MultipartBody.Part.createFormData("file", fileName, body)
                        extraInfoModel.getSpecializationId(postedSpecialization)
                            .observe(requireActivity()) {
                                var specId = it
                                extraInfoModel.getEducationId(postedEducation)
                                    .observe(requireActivity()) { id ->
                                        var eduId = id
                                        val resSpec = RequestBody.create(MultipartBody.FORM, specId.toString())
                                        val resEdu = RequestBody.create(MultipartBody.FORM, eduId.toString())
                                        userAccountModel.postSpecFile(addFile, userId, resName, resSpec, resEdu).observe(requireActivity()) {result->
                                            isSpecOpen = !isSpecOpen
                                            TransitionManager.beginDelayedTransition(binding.root)
                                            binding.addSpecializationBtn.visibility = GONE
                                            binding.specializationRecycle.visibility = GONE
                                            binding.buttonSpec.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                                            view?.let { it1 -> Snackbar.make(it1, result, Snackbar.LENGTH_LONG).show() }
                                        }
                                    }
                            }
                    } ?: run {
                        val postedSpecialization = bundle.getString("specialization").toString()
                        val postedEducation = bundle.getString(
                            "education"
                        ).toString()
                        extraInfoModel.getSpecializationId(postedSpecialization)
                            .observe(requireActivity()) {
                                var specId = it
                                extraInfoModel.getEducationId(postedEducation)
                                    .observe(viewLifecycleOwner) { id ->
                                        var eduId = id
                                        val resName = RequestBody.create(MultipartBody.FORM, "")
                                        val body = RequestBody.create(MultipartBody.FORM, "")
                                        val addFile = MultipartBody.Part.createFormData("file", "", body)
                                        val resSpec = RequestBody.create(MultipartBody.FORM, specId.toString())
                                        val resEdu = RequestBody.create(MultipartBody.FORM, eduId.toString())
                                        userAccountModel.postSpecFile(addFile, userId, resName, resSpec, resEdu).observe(viewLifecycleOwner) {result ->
                                            view?.let { it1 -> Snackbar.make(it1, result, Snackbar.LENGTH_LONG).show() }
                                        }
                                    }
                            }
                    }
                }
            }
            binding.buttonSpec.setOnClickListener {
                isSpecOpen = !isSpecOpen
                specFlag = 1
                TransitionManager.beginDelayedTransition(binding.root)
                if (!isSpecOpen) {
                    binding.addSpecializationBtn.visibility = VISIBLE
                    binding.specializationRecycle.visibility = VISIBLE
                    it.setBackgroundResource(R.drawable.open_list_with_arrow)
                    userAccountModel.getUserDocumentDependenceServ(userId).observe(viewLifecycleOwner){

                        if (it == "success") {
                            binding.problemSpec.text = getString(R.string.not_user_specs)
                            binding.problemImage.visibility = VISIBLE
                        }
                        else if (it == "502" || it == "503") {
                            binding.problemSpec.text = getString(R.string.server_not_avilable)
                            binding.problemImage.visibility = GONE
                        }
                        else if (it == "timeout"){
                            binding.problemSpec.text = getString(R.string.no_connection)
                            binding.problemImage.visibility = GONE
                        }
                        else{
                            binding.problemSpec.text = getString(R.string.no_connection)
                            binding.problemImage.visibility = GONE
                        }
                    }
                } else {
                    binding.addSpecializationBtn.visibility = GONE
                    binding.specializationRecycle.visibility = GONE
                    it.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                }
            }

            binding.buttonExp.setOnClickListener {
                isExpOpen = !isExpOpen
                TransitionManager.beginDelayedTransition(binding.root)
                expFlag = 1
                if (!isExpOpen) {
                    binding.addExperienceBtn.visibility = VISIBLE
                    binding.experienceRecycle.visibility = VISIBLE
                    it.setBackgroundResource(R.drawable.open_list_with_arrow)
                    userAccountModel.getUserExperienceServ(userId, userId).observe(viewLifecycleOwner){
                        if (it == "success") {
                            binding.problemExp.text = getString(R.string.not_user_specs)
                            binding.problemExpImage.visibility = VISIBLE
                        }
                        else if (it == "502" || it == "503") {
                            binding.problemExp.text = getString(R.string.server_not_avilable)
                            binding.problemExpImage.visibility = GONE
                        }
                        else if (it == "timeout"){
                            binding.problemExp.text = getString(R.string.no_connection)
                            binding.problemExpImage.visibility = GONE
                        }
                        else{
                            binding.problemExp.text = getString(R.string.no_connection)
                            binding.problemExpImage.visibility = GONE
                        }
                    }
                } else {
                    binding.addExperienceBtn.visibility = GONE
                    binding.experienceRecycle.visibility = GONE
                    it.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                }
            }
            binding.buttonUserDocs.setOnClickListener {
                isDocsOpen = !isDocsOpen
                TransitionManager.beginDelayedTransition(binding.root)
                if (!isDocsOpen) {
                    binding.addDocumentBtn.visibility = VISIBLE
                    binding.documentsRecycle.visibility = VISIBLE
                    binding.progressDocuments.visibility = VISIBLE
                    it.setBackgroundResource(R.drawable.open_list_with_arrow)
                    userAccountModel.getUserDocsInfoFromServ(userId, "all").observe(viewLifecycleOwner){
                        binding.progressDocuments.visibility = GONE
                        if (it == "success") {
                            binding.adapterProblemDoc.text = getString(R.string.user_profil_documents_not_found)
                        }
                        else if (it == "502" || it == "503") {
                            binding.problemExp.text = getString(R.string.server_not_avilable)
                            binding.problemExpImage.visibility = GONE
                        }
                        else if (it == "timeout"){
                            binding.problemExp.text = getString(R.string.no_connection)
                            binding.problemExpImage.visibility = GONE
                        }
                        else{
                            binding.problemExp.text = getString(R.string.no_connection)
                            binding.problemExpImage.visibility = GONE
                        }
                    }
                } else {
                    binding.progressDocuments.visibility = GONE
                    binding.addDocumentBtn.visibility = GONE
                    binding.documentsRecycle.visibility = GONE
                    binding.adapterProblemDoc.visibility = GONE
                    it.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                }
            }
            binding.buttonUserResponse.setOnClickListener {
                isRespOpen = !isRespOpen
                TransitionManager.beginDelayedTransition(binding.root)
                if (!isRespOpen) {
                    binding.addResponseBtn.visibility = VISIBLE
                    binding.responseRecycle.visibility = VISIBLE

                    it.setBackgroundResource(R.drawable.open_list_with_arrow)
                } else {
                    binding.addResponseBtn.visibility = GONE
                    binding.adapterProblemArch.visibility = GONE
                    binding.responseRecycle.visibility = GONE
                    binding.respDocumentsRecycle.visibility = GONE
                    it.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                }
            }
            updateUI()
            return binding.root
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI(){
        userId = arguments?.getInt("userId")
        userId?.let {userId ->
            ImageFactory.setUserIcon(binding.root, userId, requireActivity())
            var listener = object : RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val visibleItem = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
//                    val visibleItem2 = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    recyclerView.post {
                        respDocumentsAdapter?.notifyItemChanged(visibleItem)
                        respDocumentsAdapter?.notifyItemChanged(visibleItem)
                    }
                }

            }
            dataModel.getRespDocsFromServ("all", userId?:0)
            dataModel.getUserLevelData(userId).observe(viewLifecycleOwner){ levelData ->
                levelData?.let {
                    binding.levelCurrentText.text = levelData.number.toString()
                    binding.levelNextText.text = levelData.nextNumber.toString()
                    val progressMax = levelData.maxPoints - levelData.minPoints
                    val progressUser = levelData.curPoints - levelData.minPoints
                    val progress =((progressUser.toFloat()/progressMax.toFloat())*100)
                    binding.userLevelBar.smoothProgress(progress.toInt())
                }?: run{
                    binding.levelCurrentText.text = "1"
                    binding.levelNextText.text = "2"
                    binding.progressText.text = getString(R.string.you_has_no_level_data)
                }
            }
            binding.respDocumentsRecycle.itemAnimator = null
            binding.respDocumentsRecycle.removeOnScrollListener(listener)
            binding.respDocumentsRecycle.addOnScrollListener(listener)

            userAccountModel.getUserData(userId).observe(viewLifecycleOwner) {
                binding.userNameInfo.text = "${it?.fname} ${it?.lname }${it?.mname?: ""}"
                userRoleId = it?.roleId
                userRoleId?.let { rId ->
                    userAccountModel.getRoleNameById(rId).observe(viewLifecycleOwner){  role ->
                        userType = role
                        binding.statusInfo.text =
                            (if(it?.status == "confirmed")
                                getText(R.string.confirmed_user)
                            else
                                getText(R.string.not_confirmed_user)).toString()
                        if (userType == "работодатель"){
                            binding.listItemUserExperienceMainViewGroup.visibility = GONE
                            binding.listItemUserSpecializationMainViewGroup.visibility = GONE
                            binding.companyView.visibility = VISIBLE
                            binding.companyInfo.text = it.lname
                            binding.userNameInfo.text = it.fname
                        }
                        else{
                            binding.companyView.visibility = GONE
                            binding.userNameInfo.text = "${it?.fname} ${it?.lname }${it?.mname?: ""}"
                        }
                    }
                }


                binding.phoneInfo.text = it?.phone.toString()
                binding.emailInfo.text = it?.email.toString()
            }
            userAccountModel.getUserDocumentDependenceRoom(userId).observe(viewLifecycleOwner) {
                it?.let{
                    if (it.isNotEmpty()) {
                        var docDependencies = it as MutableList<DocDependenceFullInfo>
                        specializationAdapter = SimpleSpecializationAdapter(
                            requireActivity() as AppCompatActivity,
                            docDependencies,
                            "editableUsage",
                            this,
                            userAccountModel,
                            userId
                        )
                        binding.specializationRecycle.adapter = specializationAdapter
                        binding.problemSpecView.visibility = GONE
                    }
                    else{
                        if (isSpecOpen && specFlag != 0) {
                            binding.problemSpecView.visibility = VISIBLE
                            binding.specializationRecycle.visibility = GONE
                        }
                        else
                            binding.problemSpecView.visibility = GONE

                    }
                }
            }
            userAccountModel.getUserExperienceRoom(userId).observe(viewLifecycleOwner) {
                it?.let {
                    if (it.isNotEmpty()) {
                        var expWithTime = it as MutableList<ExperienceInfo>
                        experienceAdapter = SimpleExperienceAdapter(
                            requireActivity() as AppCompatActivity,
                            expWithTime,
                            "editableUsage",
                            this,
                            userAccountModel,
                            userId
                        )

                        binding.experienceRecycle.adapter = experienceAdapter
                        binding.problemExpView.visibility = GONE
                    }
                    else{
                        if (isExpOpen && expFlag != 0) {
                            binding.problemExpView.visibility = VISIBLE
                            binding.experienceRecycle.visibility = GONE
                        }
                        else
                            binding.problemExpView.visibility = GONE
                    }
                }
            }
            userAccountModel.getUserArchivesRoom(userId).observe(requireActivity()){
                archiveList = it as MutableList<Archive>
                if(activity != null && isAdded) {
                    archiveAdapter = LinearHorizontalArrayAdapter(
                        archiveList,
                        userId,
                        requireActivity(),
                        userAccountModel,
                        this
                    )
                    binding.responseRecycle.adapter = archiveAdapter
                }
                (binding.responseRecycle.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            dataModel.getUserDocumentsFromRoom(userId).observe(viewLifecycleOwner) {
                userDocumentsList = it as MutableList<DocumentInfoWithKnowledge>
                if(it.isNotEmpty()) {


                    if (!isDocsOpen){
                        binding.adapterProblemDoc.visibility = GONE
                        binding.documentsRecycle.visibility = VISIBLE
                    }

                    if (activity != null && isAdded) {
                        userDocumentsAdapter = UserDocumentListAdapter(
                            userDocumentsList,
                            userAccountModel,
                            requireActivity(),
                            userId,
                            this
                        )
                        binding.documentsRecycle.adapter = userDocumentsAdapter
                    }
                }
                else{
                    if (!isDocsOpen) {
                        binding.adapterProblemDoc.visibility = VISIBLE
                        binding.documentsRecycle.visibility = GONE
                        binding.buttonUserResponse.setBackgroundResource(R.drawable.open_list_with_arrow)
                    }
                }
            }
        }
        if (!isRespOpen) {
            binding.addResponseBtn.visibility = VISIBLE
            binding.responseRecycle.visibility = VISIBLE
            binding.respDocumentsRecycle.visibility = VISIBLE
            binding.buttonUserResponse.setBackgroundResource(R.drawable.open_list_with_arrow)
        }
    }

    override fun onChildAdapterClickEventMessage(position: Int, text: String): Snackbar {
        val bar = Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
        return bar
    }

    override fun onChildClickStartPutEvent(adapterPosition: Int, itemId: Int, mod: String) {
        val manager =parentFragmentManager

        when(mod){
            "putExpAsync" -> {
                val bundle = Bundle()
                bundle.putInt("userId", userId!!)
                bundle.putInt("expId", itemId)
                bundle.putString("mod", mod)
                val dialog = EditExperienceDialogFragment()
                dialog.arguments = bundle
                dialog.show(manager, "edit_experience")
                manager.setFragmentResultListener(
                    "filename",
                    viewLifecycleOwner
                ) { requestKey, bundle1 ->
                    bundle1.getString(requestKey)?.let {
                        Toast.makeText(requireActivity(), "Есть файл!", Toast.LENGTH_SHORT).show()
                        val postedString = bundle1.getString(requestKey).toString()
                        val postedSuff = bundle1.getString("suff").toString()
                        val postedName = bundle1.getString("name").toString()
                        val postedExpTime = bundle1.getString("expTime").toString()
                        val postedExperience = bundle1.getString("experience").toString()
                        val postedRole = bundle1.getString("role").toString()
                        val postedPlace = bundle1.getString("place").toString()
                        val uri = Uri.parse(postedString)
                        val contentResolver = requireContext().contentResolver
                        val inputStream = contentResolver.openInputStream(uri)
                        val body = RequestBody.create(
                            MediaType.parse("application/octet"),
                            inputStream!!.readBytes()
                        )
                        inputStream.close()
                        val fileName = "file$postedSuff"
                        val resName = RequestBody.create(MultipartBody.FORM, postedName)
                        val resRole = RequestBody.create(MultipartBody.FORM, postedRole)
                        val resPlace = RequestBody.create(MultipartBody.FORM, postedPlace)
                        val resExperience = RequestBody.create(MultipartBody.FORM, postedExperience)

                        val addFile = MultipartBody.Part.createFormData("file", fileName, body)
                        extraInfoModel.getExperienceTimeId(postedExpTime)
                            .observe(requireActivity()) { expTimeId ->

                                val resExpTime =
                                    RequestBody.create(MultipartBody.FORM, expTimeId.toString())
                                userAccountModel.putExpFile(addFile, userId!!, itemId, resName,
                                    resRole, resPlace, resExpTime, resExperience
                                ).observe(requireActivity()) {
                                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                                }
                            }
                    } ?: run {
                        val postedName = bundle1.getString("name")
                        val postedExpTime = bundle1.getString("expTime").toString()
                        val postedExperience = bundle1.getString("experience").toString()
                        val postedRole = bundle1.getString("role").toString()
                        val postedPlace = bundle1.getString("place").toString()
                        extraInfoModel.getExperienceTimeId(postedExpTime)
                            .observe(requireActivity()) { expTimeId ->
                                val resExpTime =
                                    RequestBody.create(MultipartBody.FORM, expTimeId.toString())
                                val resName = RequestBody.create(MultipartBody.FORM, postedName?: "")
                                val resRole = RequestBody.create(MultipartBody.FORM, postedRole)
                                val resPlace = RequestBody.create(MultipartBody.FORM, postedPlace)
                                val resExperience = RequestBody.create(MultipartBody.FORM, postedExperience)
                                val body = RequestBody.create(MultipartBody.FORM, "")
                                val addFile = MultipartBody.Part.createFormData("file", "", body)
                                userAccountModel.putExpFile(addFile, userId!!, itemId, resName,
                                    resRole, resPlace, resExpTime, resExperience
                                ).observe(requireActivity()) {
                                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                                }
                            }
                    }
                }
            }
            "putDependAsync" ->{
                val bundle = Bundle()
                bundle.putInt("userId", userId!!)
                bundle.putInt("dependId", itemId)
                bundle.putString("mod", mod)
                val dialog = EditSpecializationDialogFragment()
                dialog.arguments = bundle
                dialog.show(manager, "edit_dependencies")
                manager.setFragmentResultListener(
                    "filename",
                    viewLifecycleOwner
                ) { requestKey, bundle1 ->
                    bundle1.getString(requestKey)?.let {
                        Toast.makeText(requireActivity(), "Есть файл!", Toast.LENGTH_SHORT).show()
                        val postedString = bundle1.getString(requestKey).toString()
                        val postedSuff = bundle1.getString("suff").toString()
                        val postedName = bundle1.getString("name").toString()
                        val putSpecialization = bundle1.getString("specialization").toString()
                        val putEducation = bundle1.getString("education").toString()
                        val uri = Uri.parse(postedString)
                        val contentResolver = requireContext().contentResolver
                        val inputStream = contentResolver.openInputStream(uri)
                        val body = RequestBody.create(
                            MediaType.parse("application/octet"),
                            inputStream!!.readBytes()
                        )
                        inputStream.close()
                        val fileName = "file$postedSuff"
                        val resName = RequestBody.create(MultipartBody.FORM, postedName)
                        val resEdu = RequestBody.create(MultipartBody.FORM, putEducation)

                        val addFile = MultipartBody.Part.createFormData("file", fileName, body)
                        extraInfoModel.getSpecializationId(putSpecialization)
                            .observe(requireActivity()) { specId ->

                                val resSpecId =
                                    RequestBody.create(MultipartBody.FORM, specId.toString())
                                extraInfoModel.getEducationId(putEducation)
                                    .observe(requireActivity()){ eduId ->
                                        val resEduId =
                                            RequestBody.create(MultipartBody.FORM, eduId.toString())
                                        userAccountModel.putDependFile(
                                            addFile,
                                            userId!!,
                                            itemId,
                                            resName,
                                            resSpecId,
                                            resEduId
                                        ).observe(requireActivity()) {
                                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                                        }
                                    }
                            }
                    } ?: run {
                        val putName = bundle1.getString("name")
                        val putSpecialization = bundle1.getString("specialization").toString()
                        val putEducation = bundle1.getString("education").toString()
                        extraInfoModel.getSpecializationId(putSpecialization)
                            .observe(requireActivity()) { specId ->
                                val resName = RequestBody.create(MultipartBody.FORM, putName?: "")
                                val body = RequestBody.create(MultipartBody.FORM, "")
                                val addFile = MultipartBody.Part.createFormData("file", "", body)
                                val resSpecId =
                                    RequestBody.create(MultipartBody.FORM, specId.toString())
                                extraInfoModel.getEducationId(putEducation)
                                    .observe(requireActivity()){ eduId ->
                                        val resEduId =
                                            RequestBody.create(MultipartBody.FORM, eduId.toString())
                                        userAccountModel.putDependFile(
                                            addFile,
                                            userId!!,
                                            itemId,
                                            resName,
                                            resSpecId,
                                            resEduId
                                        ).observe(requireActivity()) {
                                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                                        }
                                    }
                            }

                    }
                }
            }
        }
    }

    override fun onTabClickEvent(position: Int, itemId: Int, text: String) {
        respDocumentsAdapter?.clearDocItems()
        binding.apply {
            respDocumentsRecycle.visibility = GONE
            progressArchive.visibility = VISIBLE
            adapterProblemArch.visibility = GONE
        }
        oldText = this.text
        this.text = text
        dataModel.getRespDocsFromServ(text, userId?:0).observe(viewLifecycleOwner){ it ->
            binding.progressArchive.visibility = GONE
            archFlag =1
            if (it == "success"){
                val bar = Snackbar.make(requireActivity().findViewById(android.R.id.content), getText(R.string.success), 3500)

                bar.show()
            }
            else if (it == "timeout"){
                val bar = Snackbar.make(requireActivity().findViewById(android.R.id.content), getText(R.string.server_not_avilable), 3500)
                bar.show()
            }
            else{
                val bar = Snackbar.make(requireActivity().findViewById(android.R.id.content), getText(R.string.dismis_connection), 3500)
                bar.show()
                binding.adapterProblemArch.text =  getText(R.string.dismis_connection)
            }
        }

        dataModel.getRespDocsFromRoom(text).observeOneData(viewLifecycleOwner){
            documentsList = it as MutableList<DocumentInfoWithKnowledge>

            if (documentsList.isNotEmpty()) {
                binding.adapterProblemArch.visibility = GONE
                binding.respDocumentsRecycle.visibility = VISIBLE
                if (activity != null && isAdded) {
                    respDocumentsAdapter = DocumentListAdapterForCurUser(
                        documentsList,
                        userId!!,
                        text,
                        findNavController(),
                        requireActivity(),
                        userAccountModel,
                        this
                    )
                    respDocumentsAdapter?.notifyDataSetChanged()
                    binding.respDocumentsRecycle.adapter = respDocumentsAdapter
                    binding.scrollView.fullScroll(View.FOCUS_DOWN)
                }
            }
            else {
                if (archFlag != 0) {
                    binding.adapterProblemArch.text =
                        getText(R.string.add_document_to_archive_first)
                    binding.adapterProblemArch.visibility = VISIBLE
                    binding.respDocumentsRecycle.visibility = GONE
                }

            }
        }
    }
    override fun onItemInAdapterUsableClickEvent(position: Int, item: User, activatorType: String) {
    }
    fun ProgressBar.smoothProgress(percent: Int){
        val animation = ObjectAnimator.ofInt(this, "progress", percent)
        animation.duration = 1300
        animation.interpolator =  AccelerateInterpolator()
        animation.start()
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
    fun <T> LiveData<T>.observeDataUntilChanged(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        var currentKey: String? = text// Инициализируем с текущим значением LiveData
        observe(lifecycleOwner) { newValue ->
            if (currentKey != oldText) {
                currentKey = text // Обновляем ключ при изменении данных
                observer.onChanged(newValue)
            }
        }
    }

}