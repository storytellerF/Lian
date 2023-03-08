package com.example.testdrawabletext

import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.text.style.ReplacementSpan
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams

interface EllipsisSafeSpan

class SafeImageSpan(context: Context, @DrawableRes drawable: Int) : ImageSpan(context, drawable),
    EllipsisSafeSpan

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val text =
            "神龙见首不见尾神龙见首不见尾神龙见首不见尾神龙见首不见尾神"
        val maxLines = 2
        val drawableText = findViewById<TextView>(R.id.textView)
        val exampleText = findViewById<TextView>(R.id.textView3)
        val drawableRes = R.drawable.verifird_mark_snall
        val combine = combine(
            text,
            drawableText.span(drawableRes),
            drawableText.span(drawableRes)
        )
        drawableText.viewTreeObserver.addOnDrawListener {
            val ellipsisCount =
                drawableText.layout.getEllipsisCount(drawableText.layout.lineCount - 1)
            if (ellipsisCount > 0) drawableText.safeText(combine)
        }
        drawableText.also {
            it.maxLines = maxLines
            it.text = combine
        }

        exampleText.also {
            it.maxLines = maxLines
            it.post {
                it.text = text
            }
        }
        findViewById<Button>(R.id.catchInfo).setOnClickListener {
            val lineCount1 = drawableText.layout.lineCount
            val ellipsisCount1 = drawableText.layout.getEllipsisCount(lineCount1 - 1)
            Log.i("SE", "ellipsisCount1 $ellipsisCount1")
            val lineCount2 = exampleText.layout.lineCount
            val ellipsisCount2 = exampleText.layout.getEllipsisCount(lineCount2 - 1)
            Log.i("SE", "ellipsisCount2 $ellipsisCount2")
        }

        findViewById<SeekBar>(R.id.seekBar).let {
            it.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val newWidth = progress * this@MainActivity.window.decorView.width / 100
                    drawableText.updateLayoutParams {
                        width = newWidth
                    }
                    drawableText.text = combine

                    exampleText.updateLayoutParams {
                        width = newWidth
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

            })
        }
    }


}

/**
 * span 的位置只能是在最后面。支持多个
 * @return 是否发生了ellipsis，以及可以正常显示的内容
 */
private fun TextView.safeText(combinedContent: Spannable) {

    //获取最后一行变成...的数据的长度
    val ellipsisCount: Int = layout.getEllipsisCount(lineCount - 1)
    text = if (ellipsisCount > 0) {
        //提取spans
        val spans = combinedContent.getSpans(0, combinedContent.length, EllipsisSafeSpan::class.java)
        val plainContent = combinedContent.substring(0, combinedContent.length - spans.size)
        val plainLength = plainContent.length

        val offset = ellipsisCount - spans.size
        val (unsafeContent, influencedSpans) = if (offset >= 0) {
            //影响到原始内容
            plainContent.substring(0, plainLength - offset) to spans.toList()
        } else {
            //未影响到原始内容
            plainContent to spans.reversed().subList(0, ellipsisCount)
        }
        val influencedSpanWidth = influencedSpans.map {
            if (it is ReplacementSpan) {
                it.getSize(paint, "", 0, 0, null)
            } else 0
        }.fold(0) { acc, t ->
            acc + t
        }
        val safeContent = safeTextWhenSpan(unsafeContent, influencedSpanWidth)
        val combine = combine(safeContent, *spans)
        combine
    } else {
        combinedContent
    }
}

private fun TextView.safeTextWhenSpan(
    content: String,
    spanWidth: Int,
): String {
    val length = content.length
    var manualEllipsisCount = 0
    while (true) {
        val start = length - manualEllipsisCount
        if (start < 0) break
        val measureText = paint.measureText(content.substring(start))
        if (measureText >= spanWidth) break
        manualEllipsisCount++
    }
    val start = length - manualEllipsisCount
    return if (start < 0) "" else content.substring(0, start) + "…"
}


/**
 * 在文字后面追加指定的drawable，成为一个spannable
 */
private fun combine(
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

private fun TextView.span(drawable: Int) =
    SafeImageSpan(context, drawable)