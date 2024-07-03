package ru.kondrashen.diplomappv20.presentation.adapters

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.ListItemExperienceBinding
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.baseClasses.ImageFactory
import ru.kondrashen.diplomappv20.presentation.baseClasses.onItemClickInterface
import ru.kondrashen.diplomappv20.presentation.fragments.ConfirmDialogFragment
import ru.kondrashen.diplomappv20.repository.api.APIFactory
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ExperienceInfo
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.sql.Time
import java.util.Date

class SimpleExperienceAdapter(private var activity: AppCompatActivity, private var experience: MutableList<ExperienceInfo>, private  var  mod: String, private var  listener: onItemClickInterface<User>?, private var viewModel: UserAccountControlViewModel, private var userId: Int?): RecyclerView.Adapter<SimpleExperienceAdapter.ExperienceViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperienceViewHolder {

        val binding = ListItemExperienceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return ExperienceViewHolder(binding)
    }
    override fun getItemCount(): Int {
        return experience.size
    }
    override fun onBindViewHolder(holder: ExperienceViewHolder, position: Int) {
        val exp= experience[position]
        holder.setExperience(exp, mod)
    }
    fun getCurrentIDs(): List<Int> {
        return experience.map { it.expId }
    }
    fun addExperience(experiences: MutableList<ExperienceInfo>){
        this.experience.addAll(experiences)
    }
    inner class ExperienceViewHolder(private var binding: ListItemExperienceBinding): RecyclerView.ViewHolder(binding.root) {
        private lateinit var experienceInfo: ExperienceInfo
        fun setExperience(experienceInfo: ExperienceInfo, mod: String){
            this.experienceInfo = experienceInfo
            binding.expTimeInfo.text= experienceInfo.experienceTime

            when(mod){
                "removableUsage" -> {
                    binding.removeBtn.visibility = View.VISIBLE
                    binding.removeBtn.setOnClickListener {
                        val removedItemIndex = adapterPosition
                        experience.removeAt(removedItemIndex)
                        notifyItemRemoved(removedItemIndex)
                        if (itemCount == 0){
                            (binding.root.parent as RecyclerView).visibility = View.GONE
                        }
                    }
                }
                "editableUsage" ->{
                    binding.root.setOnClickListener {
                        listener?.onChildClickStartPutEvent(adapterPosition, experienceInfo.expId, "putExpAsync")
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
                                    viewModel.deleteExperienceRequest(
                                        uId, experienceInfo.expId
                                    ).observe(activity as LifecycleOwner) {
                                        if (it == "OK") {
                                            val removedItemIndex = adapterPosition
                                            if (removedItemIndex >0) {
                                                experience.removeAt(removedItemIndex)
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
                                            experience.removeAt(removedItemIndex)
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
            experienceInfo.experience?.let { expInf->
                binding.apply {
                    listItemStatusInfo.visibility = View.VISIBLE
                    expTimeTextView.visibility = View.VISIBLE
                    roleView.visibility = View.VISIBLE
                    placeView.visibility = View.VISIBLE
                    listItemExperienceMainInfoView.visibility = View.VISIBLE
                    listItemExperienceDescInfo.text = expInf
                    roleInfo.text =
                        if (experienceInfo.role == null || experienceInfo.role =="")
                            activity.getString(R.string.not_added_her)
                        else
                            experienceInfo.role
                    placeInfo.text =
                        if (experienceInfo.place == null || experienceInfo.place =="")
                            activity.getString(R.string.not_added)
                        else
                            experienceInfo.place

                    experienceInfo.documentScanId?.let { id ->
                        imageCard.visibility = View.VISIBLE
                        ImageFactory.setFileOverrideSizePreView(binding.root, id, activity, image1.width)
                        userId?.let { userIdLoc ->
                            imageCard.setOnClickListener {
                                viewModel.getFileMimeRequest(id, userIdLoc)
                                    .observe(activity){ fileType ->
                                        println(fileType + "Djn")
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
                                                            saveImage(fileLoc)
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
    }
    private fun saveImage(image: Bitmap): String? {
        var savedImagePath: String? = null
        val imageFileName = "JPEG_${ java.sql.Date(Date().time) } ${ Time(Date().time).toString().replace(":",".")}.jpg"
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/experience_files"
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

