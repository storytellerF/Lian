package com.storytellerF.lian

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.storytellerF.lib.combine
import com.storytellerF.lib.lian
import com.storytellerF.lib.span

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
        drawableText.apply {
            this.maxLines = maxLines
        }
        val drawable = ContextCompat.getDrawable(this, R.drawable.fan_circle_background)!!
        val strokeDrawable = ContextCompat.getDrawable(this, R.drawable.fan_circle_storke)!!
        val combine = combine(
            text,
            drawableText.context.span(drawableRes),
            drawableText.context.span(drawableRes),
            FanCircleSpan(drawable, strokeDrawable)
        )
        drawableText.lian(combine)

        exampleText.also {
            it.maxLines = maxLines
            it.post {
                it.text = text
            }
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
                    drawableText.lian(combine)

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

