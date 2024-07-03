package ru.kondrashen.diplomappv20.presentation.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.AddAppExpDialogFragmentBinding
import ru.kondrashen.diplomappv20.domain.ExtraInfoPageViewModel
import ru.kondrashen.diplomappv20.presentation.baseClasses.ImageFactory
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import java.io.File
import java.io.FileOutputStream

class EditAddExpDialogFragment: DialogFragment() {
    private lateinit var extraInfoModel: ExtraInfoPageViewModel
    private var _binding: AddAppExpDialogFragmentBinding? =null
    private val binding get() = _binding!!
    private var suff = ""
    private var type =""
    private var status =""
    private var choseResult: String =""
    private var typeAdapter: ArrayAdapter<String>? = null
    private var statusAdapter: ArrayAdapter<String>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = AddAppExpDialogFragmentBinding.inflate(layoutInflater)
        extraInfoModel = ViewModelProvider(requireActivity()).get(ExtraInfoPageViewModel::class.java)
        var text =""

        binding.addFileCheck.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup?)
            if (binding.addFileCheck.isChecked) {
                binding.fileInfoView.visibility = View.VISIBLE
                binding.addFileView.visibility = View.VISIBLE
                binding.buttonChoseFile.visibility = View.VISIBLE
            }
            else {
                binding.fileInfoView.visibility = View.GONE
                binding.addFileView.visibility = View.GONE
                binding.buttonChoseFile.visibility = View.GONE
            }
        }
        binding.buttonChoseFile.setOnClickListener {
            val allowTypes = arrayOf("image/*", "application/pdf")
            val intent = Intent()
                .setType("*/*")
                .putExtra(Intent.EXTRA_MIME_TYPES, allowTypes)
                .setAction(Intent.ACTION_GET_CONTENT)
            startChoseFileLauncher.launch(intent)
        }
        binding.cancelBtn.setOnClickListener{
            dismiss()
        }
        binding.saveBtn.setOnClickListener{
            val bar = Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
            val textview = bar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textview.gravity = Gravity.CENTER_HORIZONTAL
            textview.textAlignment =View.TEXT_ALIGNMENT_CENTER
            status = when(statusAdapter?.getPosition(binding.statusSelector.selectedItem.toString())){
                0 -> "confirmed"
                else -> "not confirmed"
            }
            println(status)
            type = when(typeAdapter?.getPosition(binding.typeSelector.selectedItem.toString())){
                0 -> "increase"
                else -> "decrease"
            }
            println(type)
            val status = status
            val points = binding.userPoints.text.toString().toInt()

            val type = type
            val reason = binding.expReasonInput.text.toString()
            val name = binding.fileName.text.toString()
            if (type == "") {
                text = getString(R.string.err_role_not_chosen)
                bar.setText(text)
                bar.show()
            }
            if (binding.addFileCheck.isChecked) {
                if (name == "") {
                    text = getString(R.string.err_empty_name )
                    bar.setText(text)
                    bar.show()
                }
                else if (choseResult in listOf("", null)){
                    bar.setText(R.string.forgot_a_file)
                    bar.show()
                }
                else if (choseResult =="oldfile"){
                    sendDataOldFileNewName("filename",
                        "name", name,
                        "status", status,
                        "points", points,
                        "type", type,
                        "reason", reason)
                    dismiss()
                }
                else {
                    sendData(
                        "filename",
                        choseResult,
                        "name",
                        name,
                        "suff",
                        suff,
                        "status",
                        status,
                        "points",
                        points,
                        "type",
                        type,
                        "reason",
                        reason
                    )
                    dismiss()
                }
            }
            else {
                sendDataNoFile("filename",
                    "status", status,
                    "points", points,
                    "type", type,
                    "reason", reason)
                dismiss()
            }
        }

        updateUIDialog()
        return AlertDialog.Builder(requireActivity())

            .setView(binding.root)
            .create()
    }

    private fun sendData(keyType: String, fileUri: String,
                         keyName: String, fileName: String,
                         keySuff: String, suff: String,
                         keyStatus: String, status: String,
                         keyPoints: String, points: Int,
                         keyOperationType: String, type: String,
                         keyReason: String, reason: String){
        val bundle = Bundle()
        bundle.putString(keyType, fileUri)
        bundle.putString(keyName, fileName)
        bundle.putString(keySuff, suff)
        bundle.putString(keyStatus, status)
        bundle.putInt(keyPoints, points)
        bundle.putString(keyOperationType, type)
        bundle.putString(keyReason, reason)
        setFragmentResult(keyType, bundle)
    }

    private fun sendDataOldFileNewName(keyType: String,
                                       keyName: String, fileName: String,
                                       keyStatus: String, status: String,
                                       keyPoints: String, points: Int,
                                       keyOperationType: String, type: String,
                                       keyReason: String, reason: String){
        val bundle = Bundle()
        bundle.putString(keyName, fileName)
        bundle.putString(keyStatus, status)
        bundle.putInt(keyPoints, points)
        bundle.putString(keyOperationType, type)
        bundle.putString(keyReason, reason)
        setFragmentResult(keyType, bundle)
    }

    private fun sendDataNoFile(keyType: String,
                               keyStatus: String, status: String,
                               keyPoints: String, points: Int,
                               keyOperationType: String, type: String,
                               keyReason: String, reason: String){
        val bundle = Bundle()
        bundle.putString(keyStatus, status)
        bundle.putInt(keyPoints, points)
        bundle.putString(keyOperationType, type)
        bundle.putString(keyReason, reason)
        setFragmentResult(keyType, bundle)
    }

    private val startChoseFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK) {
            val data = it.data?.data
            choseResult = data.toString()
            val contentResolver = requireContext().contentResolver
            val file2 = File.createTempFile("Image", suff)
            var outPut = FileOutputStream(file2)
            val inputStream = contentResolver.openInputStream(data!!)
            val mime = contentResolver.getType(data)
            binding.hintText.visibility = View.VISIBLE
            if (mime!!.contains("image")){
                suff =".jpeg"
                binding.image1.setImageBitmap(BitmapFactory.decodeStream(inputStream))
                binding.image1.visibility = View.VISIBLE
                binding.pdf1.visibility  = View.GONE
            }
            else {
                suff = ".pdf"
                binding.pdf1.fromUri(data)
                    .defaultPage(0)
                    .load()
                binding.image1.visibility = View.GONE
                binding.pdf1.visibility = View.VISIBLE
            }
            inputStream.use { input ->
                outPut.use { out ->
                    input!!.copyTo(out)
                }
            }
        }

    }
    private fun updateUIDialog(){
        val appExpId = arguments?.getInt("expId")
        val authUserId = arguments?.getInt(PublicConstants.ADMIN_USER_ID)
        val userId = arguments?.getInt(PublicConstants.USER_ID)
        val expTypeArray = requireContext().resources.getStringArray(R.array.app_exp_type)
        val expStatusArray = requireContext().resources.getStringArray(R.array.app_exp_status)
        binding.typeSelector.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                type = when(position){
                    0 -> "increase"
                    else -> "decrease"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
        binding.statusSelector.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                status = when(position){
                    0 -> "confirmed"
                    else -> "not confirmed"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
        typeAdapter = ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, expTypeArray)
        statusAdapter = ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, expStatusArray)
        binding.typeSelector.adapter = typeAdapter
        binding.statusSelector.adapter = statusAdapter

        appExpId?.let { expId ->
            choseResult = "oldfile"
            println(appExpId)
            extraInfoModel.getAppExpById(expId).observe(requireActivity()){ expInfo ->
                typeAdapter?.let { adapter ->
                    expInfo?.let { expAppInfo ->
                        val expType = when(expAppInfo.type){
                            "increase" -> 0
                            else -> 1
                        }
                        binding.typeSelector.setSelection(expType)
                    }
                }
                statusAdapter?.let { adapter ->
                    expInfo?.let { expAppInfo ->
                        val expStatus = adapter.getPosition(expAppInfo.status)
                        binding.statusSelector.setSelection(expStatus)
                    }
                }
                expInfo?.documents_scan_id?.let {
                    binding.addFileCheck.setText(R.string.chose_another_file)
                    activity?.let { active ->
                        ImageFactory.setFileDocPreView(binding.root, it, active)
                    }
                    binding.image1.visibility =View.VISIBLE
                }
                expInfo?.let {
                    binding.expReasonInput.setText(it.reason)
                    binding.userPoints.setText(it.points.toString())
                }
            }
        }
    }
}
