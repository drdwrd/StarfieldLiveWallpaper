package drwdrd.ktdev.starfield

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageButton


class DownloadButton : ImageButton {

    var progress : Float = 0.0f
        set(value) {
            field = value
            invalidate()
        }

    var themeInfo : ThemeInfo? = null
    private var downloadIcon : Drawable? = null
    private var currentIcon: Drawable? = null
    private lateinit var notificationIconRect : Rect
    private lateinit var progressPaint : Paint
    private lateinit var progressRect : RectF

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
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        notificationIconRect = Rect((0.33f * w).toInt(), (0.66f * h).toInt(), (0.66f * h).toInt(), w)
        progressRect = RectF(0.33f * w, 0.3f * h, 0.66f * w, 0.66f * h)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        when {
            (progress > 0.0f && progress <= 100.0f) -> {
                canvas?.drawArc(progressRect, 0.0f, progress * 360.0f / 100.0f, false, progressPaint)
            }
            !(themeInfo?.isDownloaded ?: false) -> {
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