package ru.kondrashen.diplomappv20.presentation.adapters

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.ListItemSpecializationBinding
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.baseClasses.ImageFactory
import ru.kondrashen.diplomappv20.presentation.baseClasses.onItemClickInterface
import ru.kondrashen.diplomappv20.presentation.fragments.ConfirmDialogFragment
import ru.kondrashen.diplomappv20.repository.api.APIFactory
import ru.kondrashen.diplomappv20.repository.data_class.User
//import ru.kondrashen.diplomappv20.presentation.holders.SpecializationViewHolder
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocDependenceFullInfo
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.sql.Time
import java.util.Date

class SimpleSpecializationAdapter(private var activity: AppCompatActivity, private var dependencies: MutableList<DocDependenceFullInfo>, private  var  mod: String, private var  listener: onItemClickInterface<User>?, private var viewModel: UserAccountControlViewModel, private var userId: Int?): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1){
            val binding = ListItemSpecializationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false)
            return SpecializationViewHolder(binding)
        }
        else {
            val binding = ListItemSpecializationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false)
            return SpecializationViewHolder(binding)
        }
    }

    override fun getItemCount(): Int {
        return dependencies.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder1: SpecializationViewHolder
        val docDepend= dependencies[position]
        if(holder.itemViewType == 1){
            holder1 = holder as SpecializationViewHolder
            holder1.setSpec(docDepend, mod)
        }
        else {
            holder1 = holder as SpecializationViewHolder
            holder1.setSpec(docDepend, mod)
        }
    }
    fun getCurrentIDs(): List<Int> {
        return dependencies.map { it.docDependence.id }
    }
    fun addDependence(depend: MutableList<DocDependenceFullInfo>){
        this.dependencies.addAll(depend)
    }
    inner class SpecializationViewHolder(private var binding: ListItemSpecializationBinding): RecyclerView.ViewHolder(binding.root) {
        private lateinit var _dependence: DocDependenceFullInfo
        fun setSpec(dependence: DocDependenceFullInfo, mod: String){
            this._dependence = dependence
            binding.listItemSpecializationNameInfo.text= dependence.specializations.name
            binding.idView.text = dependence.docDependence.id.toString()
            when(mod){
                "removableUsage" -> {
                    binding.removeBtn.visibility = View.VISIBLE
                    binding.removeBtn.setOnClickListener {
                        val removedItemIndex = adapterPosition
                        dependencies.removeAt(removedItemIndex)
                        notifyItemRemoved(removedItemIndex)
                        if (itemCount == 0){
                            (binding.root.parent as RecyclerView).visibility = GONE
                        }
                    }

                }
                "editableUsage" ->{
                    binding.root.setOnClickListener {
                        listener?.onChildClickStartPutEvent(adapterPosition, dependence.docDependence.id, "putDependAsync")
                    }
                    userId?.let { uId ->

                        binding.deleteBtn.visibility = VISIBLE
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
                                    viewModel.deleteDependenceRequest(
                                         uId, binding.idView.text.toString().toInt()
                                    ).observe(activity as LifecycleOwner) {
                                        if (it == "OK") {
                                            val removedItemIndex = adapterPosition
                                            dependencies.removeAt(removedItemIndex)
                                            notifyItemRemoved(removedItemIndex)
                                            val snackbar =
                                                listener?.onChildAdapterClickEventMessage(
                                                    adapterPosition,
                                                    ContextCompat.getString(
                                                        activity,
                                                        R.string.success
                                                    )
                                                )
                                            snackbar?.show()
                                        } else if (it == "No connection to server") {
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
                                    }
                                }
                            }
                        }
                    }
                }
            }
            dependence.educations?.let {
                binding.apply {
                    listItemEducationView.visibility = VISIBLE
                    listItemEducationText.visibility = VISIBLE
                    listItemFileInfo.visibility = VISIBLE
                    listItemSpecializationNameText.visibility = VISIBLE
                    listItemEducationPlaceInfo.text = it.name
                    dependence.docDependence.documentsScanId?.let{ id ->
                        listItemFileInfo.text =activity.getString(R.string.has_сonfirming_file)
                        ImageFactory.setFileOverrideSizePreView(binding.root, id, activity, image1.width)
                        imageCard.visibility = VISIBLE
                        userId?.let { userIdLoc ->
                            imageCard.setOnClickListener {
                                viewModel.getFileMimeRequest(id, userIdLoc)
                                    .observe(activity){ fileType ->
                                        CoroutineScope(Dispatchers.IO).launch {
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
                                    }


                            }
                        }

                    }?: run {
                        listItemFileInfo.text =activity.getString(R.string.no_confirming_file)
                        imageCard.visibility = GONE
                    }
                }

            }

        }
    }
    private fun saveImage(image: Bitmap?): String? {
        var savedImagePath: String? = null
        image?.let {
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

                // Сохранение картинки в галереи с временной метко, чтобы не перезаписывать существующие файлы)
                galleryAddPic(savedImagePath)
            }
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

