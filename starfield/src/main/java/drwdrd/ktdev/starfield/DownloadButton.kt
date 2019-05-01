package drwdrd.ktdev.starfield

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.DynamicLayout
import android.text.Layout
import android.util.AttributeSet
import android.widget.ImageButton
import android.text.TextPaint
import android.text.StaticLayout






class DownloadButton : ImageButton {

    var totalBytesCount : Long = 0
    var bytesTransferred : Long = 0

    var themeInfo : ThemeInfo? = null
    private var downloadIcon : Drawable? = null
    private var currentIcon: Drawable? = null
    private lateinit var notificationIconRect : Rect
    private lateinit var progressPaint : Paint
    private lateinit var progressRect : RectF
    private lateinit var textPaint : TextPaint

    constructor(context : Context) : super(context, null) {
        init(context, null)
    }

    constructor(context: Context, attrs : AttributeSet) : super(context, attrs, android.R.attr.imageButtonStyle) {
        init(context, attrs)
    }

    @TargetApi(21)
    constructor(context : Context, attrs : AttributeSet, defStyleAttr : Int) : super(context, attrs, defStyleAttr, 0) {
        init(context, attrs)
    }

    @TargetApi(21)
    constructor(context: Context, attrs : AttributeSet, defStyleAttr : Int, defStyleRes : Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        setFocusable(true)
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
        progressPaint.strokeWidth = context.resources.getDimensionPixelSize(R.dimen.download_button_stroke_width).toFloat()
        progressPaint.color = Color.parseColor("#7FFFFFFF")

        textPaint = TextPaint()
        textPaint.isAntiAlias = true
        textPaint.textSize = 12.0f * resources.displayMetrics.density
        textPaint.color = Color.parseColor("#7FFFFFFF")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        notificationIconRect = Rect((0.33f * w).toInt(), (0.66f * h).toInt(), (0.66f * h).toInt(), w)
        progressRect = RectF(0.33f * w, 0.3f * h, 0.66f * w, 0.66f * h)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        when {
            (totalBytesCount > 0) -> {
                val progress = 100.0f * bytesTransferred / totalBytesCount
                canvas?.drawArc(progressRect, 0.0f, progress * 360.0f / 100.0f, false, progressPaint)
                val text = String.format("%.2f/%.2f MB", bytesTransferred / 1000000.0f, totalBytesCount / 1000000.0f)
                val width = textPaint.measureText(text).toInt()
                val dynamicLayout = DynamicLayout(text, textPaint, width, Layout.Alignment.ALIGN_CENTER, 1.0f, 0f, false)
                canvas?.save()
                canvas?.translate(10.0f, 10.0f)
                dynamicLayout.draw(canvas)
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