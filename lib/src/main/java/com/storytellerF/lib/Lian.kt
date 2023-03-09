package com.storytellerF.lib

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.ImageSpan
import android.text.style.ReplacementSpan
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.annotation.DrawableRes

interface EllipsisSafeSpan

class SafeImageSpan(context: Context, @DrawableRes drawable: Int) : ImageSpan(context, drawable),
    EllipsisSafeSpan

fun TextView.lian(
    combine: SpannableString
): SpannableString {
    (tag as? ViewTreeObserver.OnDrawListener)?.let {
        viewTreeObserver.removeOnDrawListener(it)
    }
    val drawListener = ViewTreeObserver.OnDrawListener {
        val lineCount = layout.lineCount
        val ellipsisCount =
            layout.getEllipsisCount(lineCount - 1)
        Log.i("SE", "draw : $ellipsisCount lineCount: $lineCount")
        if (ellipsisCount > 0) {
            safeText(combine)
        }
    }
    tag = drawListener
    viewTreeObserver.addOnDrawListener(drawListener)
    text = combine
    return combine
}

/**
 * span 的位置只能是在最后面。支持多个
 * @return 是否发生了ellipsis，以及可以正常显示的内容
 */
fun TextView.safeText(combinedContent: Spannable) {

    //获取最后一行变成...的数据的长度
    val ellipsisCount: Int = layout.getEllipsisCount(lineCount - 1)
    Log.i("SE", "ellipsis Count: $ellipsisCount")
    text = if (ellipsisCount > 0) {
        //提取spans
        val spans =
            combinedContent.getSpans(0, combinedContent.length, EllipsisSafeSpan::class.java)
                .reversed()
        val plainContent = combinedContent.substring(0, combinedContent.length - spans.size)
        val plainLength = plainContent.length

        val influencedPlainContentCount = ellipsisCount - spans.size
        val (unsafeContent, influencedSpans) = if (influencedPlainContentCount >= 0) {
            //影响到原始内容
            plainContent.substring(0, plainLength - influencedPlainContentCount) to spans
        } else {
            //未影响到原始内容
            plainContent to spans.reversed().subList(0, ellipsisCount)
        }
        val influencedSpanWidth = spanWidth(influencedSpans, combinedContent)
        val safeContent = safeTextWhenSpan(unsafeContent, influencedSpanWidth)
        Log.i(
            "SE",
            "influencedPlainContentCount: $influencedPlainContentCount span.size ${influencedSpans.size} unsafeContent: $unsafeContent safeContent: $safeContent"
        )
        val combine = combine(safeContent, *spans.toTypedArray())
        combine
    } else {
        combinedContent
    }
}

fun TextView.spanWidth(
    influencedSpans: List<EllipsisSafeSpan>,
    combinedContent: Spannable
): Int {
    val influencedSpanWidth = influencedSpans.map {
        when (it) {
            is ReplacementSpan -> it.getSize(paint, "", 0, 0, null)
            is CharacterStyle -> {
                val spanStart = combinedContent.getSpanStart(it)
                val spanEnd = combinedContent.getSpanEnd(it)
                val textPaint = TextPaint()
                it.updateDrawState(textPaint)
                textPaint.measureText(combinedContent.substring(spanStart, spanEnd)).toInt()
            }

            else -> 0
        }
    }.fold(0) { acc, t ->
        acc + t
    }
    return influencedSpanWidth
}

fun TextView.safeTextWhenSpan(
    content: String,
    spanWidth: Int,
): String {
    val targetWidth = paint.measureText("\u2026") + spanWidth + 10
    val length = content.length
    var manualEllipsisCount = 0
    while (true) {
        val start = length - manualEllipsisCount
        if (start < 0) break
        val measureText = paint.measureText(content.substring(start))
        if (measureText >= targetWidth) break
        manualEllipsisCount++
    }
    val start = length - manualEllipsisCount
    return if (start < 0) "" else content.substring(0, start) + "\u2026"
}


/**
 * 在文字后面追加指定的drawable，成为一个spannable
 */
fun combine(
    content: String,
    vararg span: Any
): SpannableString {
    val string = content + List(span.size) { " " }.joinToString("")
    val spannable = SpannableString(string)
    span.reversed().forEachIndexed { index, any ->
        spannable.setSpan(
            any,
            string.length - 1 - index,
            string.length - index,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return spannable
}

fun Context.span(drawable: Int) =
    SafeImageSpan(this, drawable)