package fr.harmoniamk.statsmk.extension

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import fr.harmoniamk.statsmk.R

object MKHtml {

    fun fromHtml(context: Context, html: String): Spannable = parse(html).apply {
        stylize(context)
    }

    private fun parse(html: String): Spannable =
        (HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY) as Spannable)

    private fun Spannable.stylize(context: Context) {
        val bold = ResourcesCompat.getFont(context, R.font.montserrat_bold)!!
        val normal = ResourcesCompat.getFont(context, R.font.montserrat_regular)!!
        for (s in getSpans(0, length, StyleSpan::class.java)) {
            if (s.style == Typeface.BOLD)
                setSpan(bold.getTypefaceSpan(), getSpanStart(s), getSpanEnd(s), 0)
            else
                setSpan(normal.getTypefaceSpan(), getSpanStart(s), getSpanEnd(s), 0)

        }
    }
}

fun Typeface.getTypefaceSpan(): MetricAffectingSpan =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        TypefaceSpan(this)
    else CustomTypefaceSpan(this)


private class CustomTypefaceSpan(private val typeface: Typeface?) : MetricAffectingSpan() {

    override fun updateDrawState(paint: TextPaint) {
        paint.typeface = typeface
    }

    override fun updateMeasureState(paint: TextPaint) {
        paint.typeface = typeface
    }
}