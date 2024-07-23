package com.tmas.tmas

import android.content.Context
import android.content.res.Resources
import android.graphics.LinearGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout.Alignment
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.tmas.tmas.R

class AutoResizeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyle) {
    private val availableSpaceRect = RectF()
    private val sizeTester: SizeTester
    private var maxTextSize: Float = 0f
    private var minTextSize: Float = 0f
    private var widthLimit: Int = 0
    private var maxLines: Int = 0
    private var initialized = false
    private var textPaint: TextPaint

    private interface SizeTester {
        /**
         * @param suggestedSize  Size of text to be tested
         * @param availableSpace available space in which text must fit
         * @return an integer < 0 if after applying `suggestedSize` to
         * text, it takes less space than `availableSpace`, > 0
         * otherwise
         */
        fun onTestSize(suggestedSize: Int, availableSpace: RectF): Int
    }

    init {
        minTextSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 1f, resources.displayMetrics)
        maxTextSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 50f, resources.displayMetrics)
        textPaint = TextPaint(paint)
        if (maxLines == 0) maxLines = NO_LINE_LIMIT
        // prepare size tester:
        sizeTester = object : SizeTester {
            val textRect = RectF()

            override fun onTestSize(suggestedSize: Int, availableSpace: RectF): Int {
                textPaint.textSize = suggestedSize.toFloat()
                val singleLine = maxLines == 1
                if (singleLine) {
                    textRect.bottom = textPaint.fontSpacing
                    textRect.right = textPaint.measureText("a")
                } else {
                    // lines = 2
                    val text = "a\na"
                    val layout: StaticLayout =
                        StaticLayout.Builder.obtain(text, 0, text.length, textPaint, widthLimit)
                            .setAlignment(Alignment.ALIGN_NORMAL).setIncludePad(true).build()
                    textRect.bottom = layout.height.toFloat()
                }
                textRect.offsetTo(0f, 0f)
                return if (availableSpace.contains(textRect)) -1 else 1
                // else, too big
            }
        }
        initialized = true
    }

    override fun setAllCaps(allCaps: Boolean) {
        super.setAllCaps(allCaps)
        adjustTextSize()
    }

    override fun setTypeface(tf: Typeface?) {
        super.setTypeface(tf)
        adjustTextSize()
    }

    override fun setTextSize(size: Float) {
        maxTextSize = size
        adjustTextSize()
    }

    override fun setMaxLines(maxLines: Int) {
        super.setMaxLines(maxLines)
        this.maxLines = maxLines
        adjustTextSize()
    }

    override fun getMaxLines(): Int {
        return maxLines
    }

    override fun setSingleLine() {
        super.setSingleLine()
        maxLines = 1
        adjustTextSize()
    }

    override fun setSingleLine(singleLine: Boolean) {
        super.setSingleLine(singleLine)
        maxLines = if (singleLine) 1 else NO_LINE_LIMIT
        adjustTextSize()
    }

    override fun setLines(lines: Int) {
        super.setLines(lines)
        maxLines = lines
        adjustTextSize()
    }

    override fun setTextSize(unit: Int, size: Float) {
        maxTextSize = TypedValue.applyDimension(unit, size, Resources.getSystem().displayMetrics)
        adjustTextSize()
    }

    private fun adjustTextSize() {
        if (!initialized) return
        val startSize = minTextSize.toInt()
        val heightLimit = measuredHeight - compoundPaddingBottom - compoundPaddingTop
        widthLimit = measuredWidth - compoundPaddingLeft - compoundPaddingRight
        if (widthLimit <= 0) return
        textPaint = TextPaint(paint)
        availableSpaceRect.right = widthLimit.toFloat()
        availableSpaceRect.bottom = heightLimit.toFloat()
        superSetTextSize(startSize)
    }

    private fun superSetTextSize(startSize: Int) {
        val textSize = binarySearch(startSize, maxTextSize.toInt(), sizeTester, availableSpaceRect)
        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
    }

    private fun binarySearch(
        start: Int,
        end: Int,
        sizeTester: SizeTester,
        availableSpace: RectF
    ): Int {
        var lastBest = start
        var lo = start
        var hi = end - 1
        var mid: Int
        while (lo <= hi) {
            mid = (lo + hi).ushr(1)
            val midValCmp = sizeTester.onTestSize(mid, availableSpace)
            if (midValCmp < 0) {
                lastBest = lo
                lo = mid + 1
            } else if (midValCmp > 0) {
                hi = mid - 1
                lastBest = hi
            } else
                return mid
        }
        // make sure to return last best
        // this is what should always be returned
        return lastBest
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, after: Int) {
        super.onTextChanged(text, start, before, after)
        adjustTextSize()
    }

    override fun onSizeChanged(width: Int, height: Int, oldwidth: Int, oldheight: Int) {
        super.onSizeChanged(width, height, oldwidth, oldheight)
        if (width != oldwidth || height != oldheight) adjustTextSize()
    }

    companion object {
        private const val NO_LINE_LIMIT = -1
    }

    var primaryColor: Int = R.color.white
    var secondaryColor: Int = R.color.white

    fun setColors(primaryColor: Int, secondaryColor: Int){
        this.primaryColor = primaryColor
        this.secondaryColor = secondaryColor
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if(changed){
            paint.shader = LinearGradient(width.toFloat()/2, 0f, width.toFloat()/2, height.toFloat()/2,
                ContextCompat.getColor(context, primaryColor),
                ContextCompat.getColor(context, secondaryColor),
                Shader.TileMode.REPEAT)
        }

    }
}