package ru.kondrashen.diplomappv20.presentation.adapters

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.ListItemAppExpBinding
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.baseClasses.ImageFactory
import ru.kondrashen.diplomappv20.presentation.baseClasses.onItemClickInterface
import ru.kondrashen.diplomappv20.presentation.fragments.ConfirmDialogFragment
import ru.kondrashen.diplomappv20.repository.api.APIFactory
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.UserExperience
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.sql.Time
import java.util.Date

class SimpleAppExperienceAdapter(private var activity: AppCompatActivity, private var userExperiences: MutableList<UserExperience>, private  var  mod: String, private var  listener: onItemClickInterface<UserExperience>?, private var viewModel: UserAccountControlViewModel, private var userId: Int?): RecyclerView.Adapter<SimpleAppExperienceAdapter.AppExpViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppExpViewHolder {

        val binding = ListItemAppExpBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return AppExpViewHolder(binding)
    }
    override fun getItemCount(): Int {
        return userExperiences.size
    }
    override fun onBindViewHolder(holder: AppExpViewHolder, position: Int) {
        val exp= userExperiences[position]
        holder.setAppExp(exp, mod)
    }


    inner class AppExpViewHolder(private var binding: ListItemAppExpBinding): RecyclerView.ViewHolder(binding.root) {
        private lateinit var userExperience: UserExperience
        fun setAppExp(experienceInfo: UserExperience, mod: String){
            this.userExperience = experienceInfo
            binding.apply {

                listItemStatusInfo.text = if (userExperience.status == "confirmed") getString(
                    activity,
                    R.string.confirmed
                ) else getString(activity,R.string.not_confirmed)
                listItemReasonDescInfo.text =
                    if (userExperience.reason == null || userExperience.reason == "") getString(
                        activity,
                        R.string.not_added_her
                    ) else userExperience.reason
                pointsInfo.text = userExperience.points.toString()
                typeInfo.text = when (userExperience.type) {
                    "increase" -> getString(activity, R.string.increas_points)
                    "decrease" -> getString(activity, R.string.decreas_points)
                    else -> getString(activity, R.string.not_added)
                }
            }
            when(mod){
                "simpleUsage" -> {
                    binding.root.setOnClickListener {
                        listener?.onChildClickStartPutEvent(adapterPosition, userExperience.id, "simpleView")
                    }
                }
                "editableUsage" ->{
                    binding.root.setOnClickListener {
                        listener?.onChildClickStartPutEvent(adapterPosition, userExperience.id, "putAppExpAsync")
                    }
                    userId?.let { uId ->
                        binding.deleteBtn.visibility = View.VISIBLE
                        binding.deleteBtn.setOnClickListener {
                            val manager = (activity).supportFragmentManager
                            val dialog = ConfirmDialogFragment()
                            dialog.show(manager, "result")
                            manager.setFragmentResultListener(
                                "status",
                                activity
                            ) { requestKey, bundle ->
                                val postedString = bundle.getString(requestKey).toString()
                                if (postedString == "success") {
                                    viewModel.deleteAppExperienceRequest(
                                        uId, userExperience.userId, userExperience.id
                                    ).observe(activity as LifecycleOwner) {
                                        if (it == "success") {
                                            val removedItemIndex = adapterPosition
                                            if (removedItemIndex >0) {
                                                userExperiences.removeAt(removedItemIndex)
                                                notifyItemRemoved(removedItemIndex)
                                            }
                                            val snackbar =
                                                listener?.onChildAdapterClickEventMessage(
                                                    adapterPosition,
                                                    ContextCompat.getString(
                                                        activity,
                                                        R.string.success
                                                    )
                                                )
                                            snackbar?.show()
                                        }
                                        else if (it == "not allowed"){
                                            val snackbar =
                                                listener?.onChildAdapterClickEventMessage(
                                                    adapterPosition,
                                                    ContextCompat.getString(
                                                        activity,
                                                        R.string.not_allowed
                                                    )
                                                )
                                            snackbar?.show()
                                        }
                                        else if (it == "not found") {
                                            val removedItemIndex = adapterPosition
                                            userExperiences.removeAt(removedItemIndex)
                                            notifyItemRemoved(removedItemIndex)
                                            val snackbar =
                                                listener?.onChildAdapterClickEventMessage(
                                                    adapterPosition,
                                                    ContextCompat.getString(
                                                        activity,
                                                        R.string.no_connection
                                                    )
                                                )

                                            snackbar?.show()
                                        }
                                        else {
                                            println(it)
                                            val snackbar =
                                                listener?.onChildAdapterClickEventMessage(
                                                    adapterPosition,
                                                    ContextCompat.getString(
                                                        activity,
                                                        R.string.server_not_avilable
                                                    )
                                                )
                                            snackbar?.show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            binding.apply {
                experienceInfo.documents_scan_id?.let { id ->
                    imageCard.visibility = View.VISIBLE
                    ImageFactory.setFileOverrideSizePreView(binding.root, id, activity, image1.width)
                    userId?.let { userIdLoc ->
                        imageCard.setOnClickListener {
                            viewModel.getFileMimeRequest(id, userIdLoc)
                                .observe(activity){ fileType ->
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            if (fileType.contains("pdf")){
                                                val download =
                                                    activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                                val pdfUri =
                                                    Uri.parse("${APIFactory.url}/users/${userIdLoc}/files/${id}/download")
                                                val getPdf = DownloadManager.Request(pdfUri)
                                                getPdf.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                                download.enqueue(getPdf)
                                                val snackbar =
                                                    listener?.onChildAdapterClickEventMessage(
                                                        adapterPosition,
                                                        ContextCompat.getString(
                                                                activity,
                                                                R.string.download_start_foto
                                                            )
                                                    )
                                                snackbar?.show()
                                            }
                                            else if (fileType.contains("image")){
                                                ImageFactory.downloadFile(id, activity)
                                                    ?.let { fileLoc ->
                                                        saveUserImage(fileLoc, "")
                                                    }
                                                val snackbar =
                                                    listener?.onChildAdapterClickEventMessage(
                                                        adapterPosition,
                                                        ContextCompat.getString(
                                                                activity,
                                                                R.string.download_start_foto
                                                            )
                                                    )
                                                snackbar?.show()
                                            }
                                            else if (fileType.contains("json")){
                                                val snackbar =
                                                    listener?.onChildAdapterClickEventMessage(
                                                        adapterPosition,
                                                        ContextCompat.getString(
                                                                activity,
                                                                R.string.not_allowed_user
                                                            )
                                                    )
                                                snackbar?.show()
                                            }
                                        }
                                        catch (e: Exception){
                                            e.printStackTrace()
                                        }
                                    }
                                }
                        }
                    }
                }
            }
        }
    }
    private fun saveUserImage(image: Bitmap, userName: String): String? {
        var savedImagePath: String? = null
        val imageFileName = "JPEG_${ userName+"_DATE_"+ java.sql.Date(Date().time) } ${ Time(Date().time).toString().replace(":",".")}.jpg"
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/user_experience_files"
        )
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }
        if (success) {
            val imageFile = File(storageDir, imageFileName)
            savedImagePath = imageFile.absolutePath
            try {
                val fOut: OutputStream = FileOutputStream(imageFile)
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Add the image to the system gallery
            galleryAddPic(savedImagePath)
            //Toast.makeText(this, "IMAGE SAVED", Toast.LENGTH_LONG).show() // to make this working, need to manage coroutine, as this execution is something off the main thread
        }
        return savedImagePath
    }
    private fun galleryAddPic(imagePath: String?) {
        imagePath?.let { path ->

            val file = File(path)
            MediaScannerConnection.scanFile(activity, arrayOf(file.toString()),
                null, null)
        }
    }
}



