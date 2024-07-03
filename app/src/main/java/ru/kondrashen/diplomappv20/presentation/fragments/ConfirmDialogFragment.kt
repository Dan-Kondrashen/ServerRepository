package ru.kondrashen.diplomappv20.presentation.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import ru.kondrashen.diplomappv20.databinding.DialogConfirmFragmentBinding

class ConfirmDialogFragment: DialogFragment() {
    private var _binding: DialogConfirmFragmentBinding? =null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogConfirmFragmentBinding.inflate(layoutInflater)
        var text = ""

        binding.cancelBtn.setOnClickListener {
            sendData("canceled")
            dismiss()
        }
        binding.confirmBtn.setOnClickListener {
            sendData("success")
            dismiss()
        }
        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .create()
    }
    private fun sendData(result: String){
        val bundle = Bundle()
        bundle.putString("status", result)
        setFragmentResult("status", bundle)
    }

}