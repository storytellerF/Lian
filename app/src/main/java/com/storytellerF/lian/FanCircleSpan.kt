package com.storytellerF.lian

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.style.ReplacementSpan
import android.util.Log
import androidx.core.graphics.toRect
import com.storytellerF.lib.EllipsisSafeSpan

private const val fanCircleContent = "Fan Circle"
private const val padding = 20
private const val margin = 10

class FanCircleSpan(private val drawable: Drawable) : ReplacementSpan(), EllipsisSafeSpan {
    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        Log.d(
            TAG,
            "getSize() called with: paint = $paint, text = $text, start = $start, end = $end, fm = $fm ${paint.textSize}"
        )
        val s = fanCircleContent
        val textPaint = TextPaint(paint)
        textPaint.updateForText()
        return textPaint.measureText(s).toInt() + padding * 2 + margin * 2
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
        Log.i(TAG, "draw: measure $textWidth ${paint.textSize}")

        val rectRangeStart = x + margin
        val rectRangeEnd = rectRangeStart + textWidth + padding * 2
        val rectRangeTop = top.toFloat() - paint.fontMetrics.ascent
        val rectRangeBottom = bottom.toFloat()
        val rectF = RectF(rectRangeStart, rectRangeTop, rectRangeEnd, rectRangeBottom)

        drawable.bounds = rectF.toRect()
        drawable.draw(canvas)

        drawText(canvas, textWidth, rectRangeStart + padding, y, paint)
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
        private const val TAG = "FanCircleSpan"
    }
}