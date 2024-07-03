package ru.kondrashen.diplomappv20.presentation.fragments

import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.DialogInterface
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.MonthPickerDialogBinding
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale


class MonthYearPickerDialogFragment(private var mod: String, private var type: String, private var startDate: String?, private var endDate: String?): DialogFragment() {
        private var listener: OnDateSetListener? = null
        private var _binding: MonthPickerDialogBinding? =null
        private var text = ""

        private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
        private val binding get() = _binding!!

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            _binding = MonthPickerDialogBinding.inflate(layoutInflater)
            // Get the layout inflater
            activity?.let {
                val cal: Calendar = Calendar.getInstance()

                binding.pickerMonth.minValue = 1
                binding.pickerMonth.maxValue = 12
                binding.pickerMonth.value = cal.get(Calendar.MONTH) + 1
                val year: Int = cal.get(Calendar.YEAR)
                binding.pickerYear.minValue = 2000
                binding.pickerYear.maxValue = year
                binding.pickerYear.value = year
                if (type == "start" && mod == "month") {
                    text = getString(R.string.date_info_month)
                    startDate?.let{
                        var date = format.parse(it)
                        cal.time = date
                        binding.pickerYear.value= cal.get(Calendar.YEAR)
                        binding.pickerMonth.value= cal.get(Calendar.MONTH)
                    }
                    endDate?.let {
                        val calEnd: Calendar = Calendar.getInstance()
                        var dateEnd = format.parse(it)
                        calEnd.time = dateEnd
                        binding.pickerYear.maxValue= calEnd.get(Calendar.YEAR)
                        binding.pickerYear.setOnValueChangedListener{ _, _, chosenYear ->
                            if (chosenYear == binding.pickerYear.maxValue){
                                binding.pickerMonth.maxValue= calEnd.get(Calendar.MONTH)
                            }
                            else{
                                binding.pickerMonth.maxValue= 12
                            }
                        }

                    }

                }
                else if (type == "end" && mod == "month") {
                    text = getString(R.string.date_info_month_end)
                    endDate?.let{
                        var date = format.parse(endDate)
                        cal.time = date
                        binding.pickerYear.value= cal.get(Calendar.YEAR)
                        binding.pickerMonth.value= cal.get(Calendar.MONTH)
                    }
                    startDate?.let {
                        val calStart: Calendar = Calendar.getInstance()
                        var dateStart = format.parse(it)
                        calStart.time = dateStart
                        binding.pickerYear.minValue= calStart.get(Calendar.YEAR)
                    }

                }
                if (type == "start" && mod == "year") {
                    text = getString(R.string.date_info_year)
                    binding.pickerMonth.visibility = GONE
                    startDate?.let{
                        var date = format.parse(it)
                        cal.time = date
                        binding.pickerYear.value= cal.get(Calendar.YEAR)
                    }
                    endDate?.let {
                        val calEnd: Calendar = Calendar.getInstance()
                        var dateEnd = format.parse(it)
                        calEnd.time = dateEnd
                        binding.pickerYear.maxValue= calEnd.get(Calendar.YEAR)


                    }

                }
                else if (type == "end" && mod == "year") {
                    text = getString(R.string.date_info_year_end)
                    binding.pickerMonth.visibility = GONE
                    endDate?.let{
                        var date = format.parse(endDate)
                        cal.time = date
                        binding.pickerYear.value= cal.get(Calendar.YEAR)

                    }
                    startDate?.let {
                        val calStart: Calendar = Calendar.getInstance()
                        var dateStart = format.parse(it)
                        calStart.time = dateStart
                        binding.pickerYear.minValue= calStart.get(Calendar.YEAR)
                        binding.pickerYear.setOnValueChangedListener{ _, _, chosenYear ->
                            if (chosenYear == binding.pickerYear.minValue){
                                binding.pickerMonth.minValue= calStart.get(Calendar.MONTH)
                            }
                            else{
                                binding.pickerMonth.maxValue= 12
                            }
                        }
                    }
                }

                binding.cancelBtn.setOnClickListener{
                    dismiss()
                }
                binding.saveBtn.setOnClickListener {
                    if (mod =="month") {
                        val date = format.format(
                            GregorianCalendar(
                                binding.pickerYear.value,
                                binding.pickerMonth.value,
                                1
                            ).time
                        )
                        sendData(PublicConstants.DATE, date)
                    }
                    else if (mod == "year"){
                        val date = format.format(
                            GregorianCalendar(
                                binding.pickerYear.value,
                               0,
                                1
                            ).time
                        )
                        sendData(PublicConstants.DATE, date)
                    }
                    dismiss()
                }

            }
            binding.titleDialog.text = text
            return android.app.AlertDialog.Builder(
                requireActivity())
                .setView(binding.root)
                .create()
        }

    private fun sendData(key: String, date: String){
        val bundle = Bundle()
        bundle.putString(key, date)
        setFragmentResult(key, bundle)
    }
}