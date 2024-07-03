package ru.kondrashen.diplomappv20.presentation.fragments
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.EditArchiveDialogFragmentBinding
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel

class EditArchiveDialogFragment: DialogFragment() {
    private lateinit var userAccountModel: UserAccountControlViewModel
    private var _binding: EditArchiveDialogFragmentBinding? =null
    private val binding get() = _binding!!
    private var startStr = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = EditArchiveDialogFragmentBinding.inflate(layoutInflater)
        userAccountModel = ViewModelProvider(requireActivity()).get(UserAccountControlViewModel::class.java)
        var text =""

        binding.cancelBtn.setOnClickListener{
            dismiss()
        }
        binding.saveBtn.setOnClickListener{
            val itemType = arguments?.getString("itemType")
            when (itemType) {
                "archive" ->{
                    val bar = Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
                    val name = binding.etName.text.toString()
                    if (name.length < 3) {
                        text = getString(R.string.err_empty_name)
                        bar.setText(text)
                        bar.show()
                    }
                    else {
                        sendData("name", name)
                        dismiss()
                    }
                }
                "comment" ->{
                    val bar = Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
                    val content = binding.etName.text.toString()
                    if (content.length < 3) {
                        text = getString(R.string.err_empty_content)

                        bar.setText(text)
                        bar.show()
                    }
                    else {
                        sendData("content", content)
                        dismiss()
                    }
                }

            }
        }


        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .create()


    }

    override fun onResume() {
        super.onResume()
        updateUIDialog()
    }

    private fun sendData(keyName: String, name: String){
        val bundle = Bundle()
        bundle.putString(keyName, name)
        setFragmentResult(keyName, bundle)
    }

    private fun updateUIDialog(){
        val archId = arguments?.getInt("archId")
        val itemType = arguments?.getString("itemType")
        when (itemType) {
            "archive" -> {
                binding.etName.setCompoundDrawablesWithIntrinsicBounds(
                    getDrawable(
                        requireContext(),
                        R.drawable.archive_svg
                    ), null, null, null
                )
                binding.etName.setText(arguments?.getString("name"))

            }
            "comment" -> {
                binding.etName.setCompoundDrawablesWithIntrinsicBounds(
                    getDrawable(
                        requireContext(),
                        R.drawable.comments_dark_svg
                    ), null, null, null
                )
                binding.etName.setText(arguments?.getString("content"))
                binding.infoText.setText(R.string.edit_comment)
                binding.contentView.setHint(R.string.enter_message)
            }
        }
        archId?.let {
//            userAccountModel.getCurArchiveNameById(it).observe(requireActivity()) { str ->
//                binding.etName.setText(str.name)
//            }
        }
    }
}