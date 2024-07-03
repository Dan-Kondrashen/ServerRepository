package ru.kondrashen.diplomappv20.presentation.baseClasses

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.WindowInsets
import androidx.coordinatorlayout.widget.CoordinatorLayout

class CustomCoordinatorLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    CoordinatorLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {

        when {
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) -> {
                insets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()).bottom
                return super.onApplyWindowInsets(
                    insets.replaceSystemWindowInsets(
                        0,
                        0,
                        0,
                        insets.getSystemWindowInsetBottom()
                    )
                )
            }

            (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) -> {
                return super.onApplyWindowInsets(
                    insets.replaceSystemWindowInsets(
                        0,
                        0,
                        0,
                        insets.getSystemWindowInsetBottom()
                    )
                )
            }

            else -> return super.onApplyWindowInsets(
                insets.replaceSystemWindowInsets(
                    0,
                    0,
                    0,
                    insets.getSystemWindowInsetBottom()
                )
            )
        }
    }
}