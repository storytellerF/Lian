package com.storytellerF.lian

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.style.ReplacementSpan
import androidx.core.graphics.toRectF
import com.storytellerF.lib.EllipsisSafeSpan

private const val fanCircleContent = "Storyteller F"
private const val padding = 20
private const val margin = 10

class FanCircleSpan(private val drawable: Drawable, private val strokeDrawable: Drawable) :
    ReplacementSpan(), EllipsisSafeSpan {
    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val textPaint = TextPaint(paint)
        textPaint.updateForText()
        return textPaint.measureText(fanCircleContent).toInt() + padding * 2 + margin * 2
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        paint as TextPaint

        paint.updateForText()
        val textWidth = paint.measureText(fanCircleContent)

        val rectRangeStart = x + margin
        val rectRangeEnd = rectRangeStart + textWidth + padding * 2
        val rectRangeTop = top.toFloat() - paint.fontMetrics.ascent
        val rectRangeBottom = bottom.toFloat()
        val rect = Rect(
            rectRangeStart.toInt(), rectRangeTop.toInt(),
            rectRangeEnd.toInt(), rectRangeBottom.toInt()
        )

        drawable.bounds = rect
        drawable.draw(canvas)

        drawStroke(canvas, rect, paint)

        drawText(canvas, textWidth, rectRangeStart + padding, y, paint)
    }

    private fun drawStroke(
        canvas: Canvas,
        rect: Rect,
        paint: TextPaint,
    ) {
        val rectRangeStart = rect.left.toFloat()
        val rectRangeTop = rect.top.toFloat()
        val rectRangeBottom = rect.bottom.toFloat()
        val rectRangeEnd = rect.right.toFloat()
        val saveLayer = canvas.saveLayer(rect.toRectF(), null)

        strokeDrawable.bounds = rect
        strokeDrawable.draw(canvas)

        paint.color = Color.BLUE
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        val height = rect.height()
        val width = rect.width()
        val f2 = 0.2f
        val f3 = 0.3f
        val f5 = 0.5f
        val offset = 10
        listOf(
            RectF(
                rectRangeStart - offset,
                height * f3 + rectRangeTop,
                rectRangeStart + offset,
                height * f5 + rectRangeTop
            ),
            RectF(
                rectRangeEnd - offset,
                height * f5 + rectRangeTop,
                rectRangeEnd + offset,
                height * (f5 + f2) + rectRangeTop
            ),
            RectF(
                rectRangeStart + width * f2,
                rectRangeTop - offset,
                rectRangeStart + width * f3,
                rectRangeTop + offset
            ),
            RectF(
                rectRangeStart + width * (1 - f3), rectRangeBottom - offset,
                rectRangeStart + width * (1 - f2), rectRangeBottom + offset
            )
        ).forEach {
            canvas.drawRect(it, paint)
        }
        paint.xfermode = null

        canvas.restoreToCount(saveLayer)
    }

    private fun drawText(
        canvas: Canvas,
        textWidth: Float,
        textStart: Float,
        baseline: Int,
        paint: TextPaint
    ) {
        paint.style = Paint.Style.FILL
        val startColor = Color.parseColor("#3AB8FF")
        val middleColor = Color.parseColor("#9387ED")
        val endColor = Color.parseColor("#FF4AD6")
        paint.shader = LinearGradient(
            textStart,
            0f,
            textStart + textWidth,
            0f,
            intArrayOf(startColor, middleColor, endColor),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawText(fanCircleContent, textStart, baseline.toFloat(), paint)
    }

    private fun TextPaint.updateForText() {
        typeface = Typeface.MONOSPACE
        textSize = textSize * 9 / 20
    }

    companion object {
    }
}