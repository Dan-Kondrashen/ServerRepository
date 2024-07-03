package ru.kondrashen.diplomappv20.presentation.baseClasses

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View

class UserAnimatorHelper {
    fun createPulsarScaleItemAnimation(view: View,): AnimatorSet{
        val animatorSet = AnimatorSet()
        val scaleXUp = ObjectAnimator.ofFloat(view, "scaleX", 1.3F)
        val scaleYUp = ObjectAnimator.ofFloat(view, "scaleY", 1.3F)
        val scaleXDown = ObjectAnimator.ofFloat(view, "scaleX", 1F)
        val scaleYDown = ObjectAnimator.ofFloat(view, "scaleY", 1F)
        animatorSet.play(scaleXUp).with(scaleYUp).before(scaleXDown).before(scaleYDown)
        animatorSet.duration = 150
        return animatorSet
    }
}