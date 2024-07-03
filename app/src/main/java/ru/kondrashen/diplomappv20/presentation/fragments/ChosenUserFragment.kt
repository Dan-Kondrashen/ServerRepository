package ru.kondrashen.diplomappv20.presentation.fragments

import android.animation.ObjectAnimator
import android.net.Uri
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.ChosenUserFragmentBinding
import ru.kondrashen.diplomappv20.domain.MainPageViewModel
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.adapters.SimpleAppExperienceAdapter
import ru.kondrashen.diplomappv20.presentation.adapters.UserListAdapter
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.presentation.baseClasses.onItemClickInterface
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.UserExperience
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.CallUserListPref
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.UpdateUser

class ChosenUserFragment : Fragment(), onItemClickInterface<UserExperience> {
    private var _binding: ChosenUserFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataModel: MainPageViewModel
    private lateinit var userAccountModel: UserAccountControlViewModel
    private var adapter: SimpleAppExperienceAdapter? = null
    private var userType: String? = null
    private var userRoleId: Int? = null
    private var isExpOpen: Boolean = true
    private var isDocsOpen: Boolean = true
    private var adminUserId: Int? = null
    private var userId: Int? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ChosenUserFragmentBinding.inflate(inflater,container,false)
        updateUI()
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataModel = ViewModelProvider(requireActivity()).get(MainPageViewModel::class.java)
        userAccountModel = ViewModelProvider(requireActivity()).get(UserAccountControlViewModel::class.java)
    }


    private fun updateUI() {
        userId = arguments?.getInt(PublicConstants.USER_ID)
        adminUserId = arguments?.getInt(PublicConstants.ADMIN_USER_ID)
        userId?.let { uId ->
            dataModel.getUserLevelData(uId).observe(viewLifecycleOwner){ levelData ->
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
            userAccountModel.getUserData(uId).observe(viewLifecycleOwner) {
                binding.statusInfo.text =
                    (if(it?.status == "confirmed")
                        getText(R.string.confirmed_user)
                    else
                        getText(R.string.not_confirmed_user)).toString()
                userRoleId = it?.roleId
                userRoleId?.let { roleId ->

                    if (roleId == 1){
                        binding.companyView.visibility = VISIBLE
                        binding.companyInfo.text = it.lname
                        binding.userNameInfo.text = it.fname
                    }
                    else{
                        binding.companyView.visibility = GONE
                        binding.userNameInfo.text = "${it?.fname} ${it?.lname }${it?.mname?: ""}"
                    }

                }


                binding.phoneInfo.text = it?.phone.toString()
                binding.emailInfo.text = it?.email.toString()
            }
            userAccountModel.getUserAppExpRoom(uId).observe(viewLifecycleOwner) { userExp ->
                adapter = SimpleAppExperienceAdapter(activity as AppCompatActivity, userExp as MutableList<UserExperience>, "editableUsage", this, userAccountModel, adminUserId)
                binding.experienceRecycle.adapter = adapter
            }
            binding.editBtn.setOnClickListener {
                val manager = parentFragmentManager
                val bundle = Bundle()
                bundle.putInt("authUserId", adminUserId!!)
                bundle.putInt("userId", userId!!)
                val dialog = EditUserDialogFragment()
                dialog.arguments = bundle
                dialog.show(manager, "result")
                manager.setFragmentResultListener(
                    "operationStatus",
                    viewLifecycleOwner
                ) { requestKey, bundle ->
                    if (bundle.getString(requestKey) == "success") {
                        val postedEmail = bundle.getString("email").toString()
                        val postedPhone = bundle.getLong("phone")
                        val postedFname = bundle.getString("fname").toString()
                        val postedLname = bundle.getString("lname").toString()
                        val postedMname = bundle.getString("mname")
                        val postedStatus = bundle.getString("status")
                        adminUserId?.let{
                            userAccountModel.putUserServ(it, uId, UpdateUser(uId,
                                fname =  postedFname,
                                lname = postedLname,
                                mname =  postedMname,
                                email = postedEmail,
                                phone = postedPhone,
                                status = postedStatus
                                )).observe(viewLifecycleOwner){
                                    if (it == "success")
                                        Snackbar.make(binding.root, getText(R.string.success), Snackbar.LENGTH_LONG).show()
                                    else if (it == "bad response")
                                        Snackbar.make(binding.root, getText(R.string.no_connection), Snackbar.LENGTH_LONG).show()
                                    else if (it == "timeout")
                                        Snackbar.make(binding.root, getText(R.string.server_not_avilable), Snackbar.LENGTH_LONG).show()
                                    else
                                        Snackbar.make(binding.root, getText(R.string.email_in_use), Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }

            binding.buttonExpApp.setOnClickListener {
                isExpOpen = !isExpOpen
                TransitionManager.beginDelayedTransition(binding.root)
                if (!isExpOpen) {
                    binding.addAppExpBtn.visibility = VISIBLE
                    binding.experienceRecycle.visibility = VISIBLE
                    it.setBackgroundResource(R.drawable.open_list_with_arrow)
                    adminUserId?.let {aId ->
                        userAccountModel.getUserAppExpServ(aId, uId).observe(viewLifecycleOwner){
                            if (it == "success") {
                                binding.problemExp.text = getString(R.string.not_user_exp)
                                binding.problemExpImage.visibility = VISIBLE
                            }
                            else if (it == "403") {
                                binding.problemExp.text = getString(R.string.not_allowed)
                                binding.problemExpImage.visibility = GONE
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

                    }

                } else {
                    binding.addAppExpBtn.visibility = GONE
                    binding.experienceRecycle.visibility = GONE
                    it.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                }
            }
            if (adminUserId != null && userId != null) {
                binding.addAppExpBtn.setOnClickListener {
                    val manager = parentFragmentManager
                    val dialog = EditAddExpDialogFragment()
                    dialog.show(manager, "result")
                    manager.setFragmentResultListener(
                        "filename",
                        viewLifecycleOwner
                    ) { requestKey, bundle ->
                        bundle.getString(requestKey)?.let {
                            val postedString = bundle.getString(requestKey).toString()
                            val postedSuff = bundle.getString("suff").toString()
                            val postedName = bundle.getString("name").toString()
                            val postedPoints = bundle.getInt("points").toString()
                            val postedReason = bundle.getString("reason").toString()
                            val postedType = bundle.getString("type").toString()
                            val postedStatus = bundle.getString("status").toString()
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
                            val resReason = RequestBody.create(MultipartBody.FORM, postedReason)
                            val resPoints = RequestBody.create(MultipartBody.FORM, postedPoints)
                            val resType = RequestBody.create(MultipartBody.FORM, postedType)
                            val resStatus = RequestBody.create(MultipartBody.FORM, postedStatus)
                            val addFile = MultipartBody.Part.createFormData("file", fileName, body)
                            userAccountModel.postUserAppExpServ(
                                curUserId = adminUserId!!,
                                userId = userId!!,
                                addFile = addFile,
                                name = resName,
                                type = resType,
                                reason = resReason,
                                points = resPoints,
                                status = resStatus
                            ).observe(requireActivity()) {
                                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                            }
                        } ?: run {
                            val postedPoints = bundle.getInt("points").toString()
                            val postedReason = bundle.getString("reason").toString()
                            val postedType = bundle.getString("type").toString()
                            val postedStatus = bundle.getString("status").toString()
                            val body = RequestBody.create(MultipartBody.FORM, "")
                            val addFile = MultipartBody.Part.createFormData("file", "", body)
                            val resName = RequestBody.create(MultipartBody.FORM, "")
                            val resReason = RequestBody.create(MultipartBody.FORM, postedReason)
                            val resPoints = RequestBody.create(MultipartBody.FORM, postedPoints)
                            val resType = RequestBody.create(MultipartBody.FORM, postedType)
                            val resStatus = RequestBody.create(MultipartBody.FORM, postedStatus)
                            userAccountModel.postUserAppExpServ(
                                curUserId = adminUserId!!,
                                userId = userId!!,
                                addFile = addFile,
                                name = resName,
                                type = resType,
                                reason = resReason,
                                points = resPoints,
                                status = resStatus
                            ).observe(requireActivity()) {
                                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onChildAdapterClickEventMessage(position: Int, text: String): Snackbar {
        val bar = Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
        return bar
    }

    fun ProgressBar.smoothProgress(percent: Int){
        val animation = ObjectAnimator.ofInt(this, "progress", percent)
        animation.duration = 1300
        animation.interpolator =  AccelerateInterpolator()
        animation.start()
    }

    override fun onChildClickStartPutEvent(adapterPosition: Int, itemId: Int, mod: String) {

        val manager =parentFragmentManager
        when(mod) {
            "putAppExpAsync" -> {
                val bundle = Bundle()
                bundle.putInt("authUserId", adminUserId!!)
                bundle.putInt("userId", userId!!)
                bundle.putInt("expId", itemId)
                bundle.putString("mod", mod)
                val dialog = EditAddExpDialogFragment()
                dialog.arguments = bundle
                dialog.show(manager, "result")
                manager.setFragmentResultListener(
                    "filename",
                    viewLifecycleOwner
                ) { requestKey, bundle ->
                    bundle.getString(requestKey)?.let {
                        val postedString = bundle.getString(requestKey).toString()
                        val postedSuff = bundle.getString("suff").toString()
                        val postedName = bundle.getString("name").toString()
                        val postedPoints = bundle.getInt("points").toString()
                        val postedReason = bundle.getString("reason").toString()
                        val postedType = bundle.getString("type").toString()
                        val postedStatus = bundle.getString("status").toString()
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
                        val resReason = RequestBody.create(MultipartBody.FORM, postedReason)
                        val resPoints = RequestBody.create(MultipartBody.FORM, postedPoints)
                        val resType = RequestBody.create(MultipartBody.FORM, postedType)
                        val resStatus = RequestBody.create(MultipartBody.FORM, postedStatus)
                        val addFile = MultipartBody.Part.createFormData("file", fileName, body)
                        userAccountModel.putUserAppExpServ(
                            curUserId = adminUserId!!,
                            userId = userId!!,
                            expId = itemId,
                            addFile = addFile,
                            name = resName,
                            type = resType,
                            reason = resReason,
                            points = resPoints,
                            status = resStatus
                        ).observe(requireActivity()) {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        }
                    } ?: run {
                        val postedPoints = bundle.getInt("points").toString()
                        val postedReason = bundle.getString("reason").toString()
                        val postedType = bundle.getString("type").toString()
                        val postedStatus = bundle.getString("status").toString()
                        val body = RequestBody.create(MultipartBody.FORM, "")
                        val addFile = MultipartBody.Part.createFormData("file", "", body)
                        val resName = RequestBody.create(MultipartBody.FORM, "")
                        val resReason = RequestBody.create(MultipartBody.FORM, postedReason)
                        val resPoints = RequestBody.create(MultipartBody.FORM, postedPoints)
                        val resType = RequestBody.create(MultipartBody.FORM, postedType)
                        val resStatus = RequestBody.create(MultipartBody.FORM, postedStatus)
                        userAccountModel.putUserAppExpServ(
                            curUserId = adminUserId!!,
                            userId = userId!!,
                            expId = itemId,
                            addFile = addFile,
                            name = resName,
                            type = resType,
                            reason = resReason,
                            points = resPoints,
                            status = resStatus
                        ).observe(requireActivity()) {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    override fun onTabClickEvent(position: Int, itemId: Int, text: String) {

    }

    override fun onItemInAdapterUsableClickEvent(
        position: Int,
        item: UserExperience,
        activatorType: String
    ) {

    }

}