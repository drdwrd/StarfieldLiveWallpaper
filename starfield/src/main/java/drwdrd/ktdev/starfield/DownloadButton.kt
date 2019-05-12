package drwdrd.ktdev.starfield

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton


class DownloadButton : AppCompatImageButton {

    var totalBytesCount : Long = 0
    var bytesTransferred : Long = 0

    var themeInfo : ThemeInfo? = null
    private var downloadIcon : Drawable? = null
    private var currentIcon: Drawable? = null
    private lateinit var notificationIconRect : Rect
    private lateinit var progressPaint : Paint
    private lateinit var progressRect : RectF
    private lateinit var textPaint : TextPaint
    private lateinit var textLayout : DynamicLayout
    private lateinit var textBuilder : Editable

    constructor(context : Context) : super(context, null) {
        init(context, null)
    }

    constructor(context: Context, attrs : AttributeSet) : super(context, attrs, android.R.attr.imageButtonStyle) {
        init(context, attrs)
    }

    fun setProgress(bytesTransferred : Long, totalBytesCount : Long) {
        this.bytesTransferred = bytesTransferred
        this.totalBytesCount = totalBytesCount
        invalidate()
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.DownloadButton, 0, 0).apply {
            downloadIcon = getDrawable(R.styleable.DownloadButton_downloadIcon)
            currentIcon = getDrawable(R.styleable.DownloadButton_activeThemeIcon)
            recycle()
        }
        progressPaint =  Paint()
        progressPaint.isAntiAlias = true
        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeCap = Paint.Cap.ROUND
        progressPaint.strokeWidth = context.resources.getDimension(R.dimen.download_button_stroke_width)
        progressPaint.color = 0x7FFFFFFF

        textPaint = TextPaint()
        textPaint.isAntiAlias = true
        textPaint.textSize = context.resources.getDimension(R.dimen.download_button_text_size)
        textPaint.color = 0x7FFFFFFF

        textBuilder = SpannableStringBuilder("")
        textLayout = DynamicLayout(textBuilder, textPaint, width, Layout.Alignment.ALIGN_CENTER, 1.0f, 0f, false)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        notificationIconRect = Rect((0.33f * w).toInt(), (0.66f * h).toInt(), (0.66f * h).toInt(), w)
        progressRect = RectF(0.33f * w, 0.33f * h, 0.66f * w, 0.66f * h)
        textLayout.increaseWidthTo(w)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        when {
            (totalBytesCount > 0) -> {
                val progress = 100.0f * bytesTransferred / totalBytesCount
                canvas?.drawArc(progressRect, 0.0f, progress * 360.0f / 100.0f, false, progressPaint)
                textBuilder.replace(0, textBuilder.length, String.format("%.2f/%.2f MB", bytesTransferred / 1000000.0f, totalBytesCount / 1000000.0f))
                canvas?.save()
                canvas?.translate(0.0f, notificationIconRect.centerY().toFloat())
                textLayout.draw(canvas)
                canvas?.restore()
            }
            !(themeInfo?.isInstalled ?: false)-> {
                downloadIcon?.bounds = notificationIconRect
                downloadIcon?.draw(canvas!!)
            }
            (themeInfo?.isActive ?: false) -> {
                currentIcon?.bounds = notificationIconRect
                currentIcon?.draw(canvas!!)
            }
        }
    }
}