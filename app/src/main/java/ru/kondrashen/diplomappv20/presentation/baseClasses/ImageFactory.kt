package ru.kondrashen.diplomappv20.presentation.baseClasses

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.repository.api.APIFactory

object ImageFactory {
    fun setUserIcon(view: View, userId: Int, activity: Activity){
        if (CatchException.hasInternetCheck(activity)) {
            var i = "intro.jpeg"
            var userAvatar = view.findViewById<ImageView>(R.id.avatar)
            Glide.with(view)
                .asDrawable()
                .load("${APIFactory.url}/users/$userId/files/${i}")
                .dontAnimate()
                .placeholder(userAvatar.drawable)
                .error(Glide.with(view).load(R.drawable.resizad_error))
                .circleCrop()
                .into(userAvatar)
        }
    }
    fun setFileDocPreView(view: View, fileId: Int, activity: Activity){
        if (CatchException.hasInternetCheck(activity)) {
            val userAvatar = view.findViewById<ImageView>(R.id.image1)
            Glide.with(view)
                .load("${APIFactory.url}/files/${fileId}")
                .placeholder(userAvatar.drawable)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .error(Glide.with(view).load(R.drawable.text_document_svg))
                .into(userAvatar)
        }
    }

    fun setFileOverrideSizePreView(view: View, fileId: Int, activity: Activity, sizeW: Int){
        if (CatchException.hasInternetCheck(activity)) {
            val preview = view.findViewById<ImageView>(R.id.image1)
            Glide.with(view)
                .asBitmap()
                .load("${APIFactory.url}/files/${fileId}")
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(preview.drawable)
                .timeout(60000)
                .error(Glide.with(view).load(R.drawable.text_document_svg))
                .override(sizeW, Target.SIZE_ORIGINAL)
                .into(preview)
        }
    }
    fun downloadFile(fileId: Int, activity: Activity): Bitmap?{
        return if (CatchException.hasInternetCheck(activity)) {
            val file = Glide.with(activity as AppCompatActivity)
                .asBitmap()
                .load("${APIFactory.url}/files/${fileId}")
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .timeout(60000)
                .placeholder(R.drawable.text_document_svg)
                .error(R.drawable.text_document_svg)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap,
                        model: Any,
                        target: Target<Bitmap>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .submit()
                .get()
            file
        } else
            null
    }
    fun setCurUserIcon(view: View, userId: Int, activity: Activity){
        if (CatchException.hasInternetCheck(activity)) {
            val i = "intro.jpeg"
            val userAvatar = view.findViewById<ImageView>(R.id.avatar)
            Glide.with(view)
                .asDrawable()
                .load("${APIFactory.url}/users/$userId/files/${i}")
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .dontAnimate()
                .placeholder(userAvatar.drawable)
                .error(Glide.with(view).load(R.drawable.resizad_error))
                .circleCrop()
                .into(userAvatar)
        }
    }
}