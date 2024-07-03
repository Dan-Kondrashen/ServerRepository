package ru.kondrashen.diplomappv20.presentation.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale

class DateDialogFragment(private var mod: String, private var type: String, private var startDate: String?, private var  endDate: String?) : DialogFragment(){
    private  lateinit var datePicker: DatePicker
    private var text = ""
    private val cal: Calendar = Calendar.getInstance()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_date, null)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
        datePicker = view.findViewById(R.id.dialog_calendar_item)

        datePicker.minDate = format.parse("2000-01-01 00:00:00").time
        datePicker.maxDate = cal.time.time
        if (type == "start"){
            text = getString(R.string.date_info)

            startDate?.let {
                var dateStart = format.parse(it)
                cal.time = dateStart
                datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null)

            }
            endDate?.let {
                var dateEnd = format.parse(it)
                datePicker.maxDate = dateEnd.time
            }

        }
        else if (type == "end") {
            text = getString(R.string.date_info_end)

            endDate?.let{
                var dateEnd = format.parse(it)
                cal.time = dateEnd
                datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null)

            }
            startDate?.let {
                var dateStart = format.parse(it)
                datePicker.minDate = dateStart.time
            }

        }

//        else if (type == "start" && mod == "year") {
//            text = getString(R.string.date_info_year)
//        }
//
//
//        else if (type == "end" && mod == "year") {
//            text = getString(R.string.date_info_year_end)
//        }


        return AlertDialog.Builder(
            requireActivity())
            .setView(view)
            .setTitle(text)
            .setPositiveButton(android.R.string.ok){
                    item: DialogInterface, id: Int ->
                val date = format.format(GregorianCalendar(datePicker.year, datePicker.month, datePicker.dayOfMonth).time)
                sendData(PublicConstants.DATE, date)
            }
            .create()

    }
    private fun sendData(key: String, date: String){
        val bundle = Bundle()
        bundle.putString(key, date)
        setFragmentResult(key, bundle)
    }
}