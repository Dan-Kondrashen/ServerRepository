package ru.kondrashen.diplomappv20.presentation.baseClasses

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveEvent<T> : MutableLiveData<T>() {
    override fun observe(owner: LifecycleOwner, observer: Observer<in T?>) {
        super.observe(owner, Observer { t ->
            if (t != null) {
                observer.onChanged(t)
                postValue(null)
            }
        })
    }
//    private val mPending = AtomicBoolean(false)
//    private var TAG = "SingletonLiveData"
//    @MainThread
//    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
//        if (hasActiveObservers()) {
//            Log.i(TAG, "New observer reg and old dead")
//        }
//        // Наблюдение за внутренним MutableLiveData
//        super.observe(owner) { t ->
//            if (mPending.compareAndSet(true, false)) {
//                observer.onChanged(t)
//            }
//        }
//    }
//    @MainThread
//    override fun setValue(t: T?) {
//        mPending.set(true)
//        super.setValue(t)
//    }
}