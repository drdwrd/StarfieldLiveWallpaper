package drwdrd.ktdev.starfield

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageButton

class DownloadButton : ImageButton {

    var progress : Float = 0.0f
        set(value) {
            field = value
            invalidate()
        }

    var isDownloaded = false
    var isCurrent = false

    private lateinit var downloadIcon : Drawable
    private lateinit var currentIcon: Drawable

    constructor(context : Context) : super(context, null) {
        init(context)
    }

    constructor(context: Context,attrs : AttributeSet) : super(context, attrs, android.R.attr.imageButtonStyle) {
        init(context)
    }

    @TargetApi(21)
    constructor(context : Context, attrs : AttributeSet, defStyleAttr : Int) : super(context, attrs, defStyleAttr, 0) {
        init(context)
    }

    @TargetApi(21)
    constructor(context: Context, attrs : AttributeSet, defStyleAttr : Int, defStyleRes : Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        setFocusable(true)
        init(context)
    }

    private fun init(context: Context) {
        downloadIcon = context.resources.getDrawable(android.R.drawable.ic_menu_add)
        currentIcon = context.resources.getDrawable(android.R.drawable.ic_menu_help)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(progress > 0.0f && progress <= 100.0f) {
            val paint = Paint()
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#7FFFFFFF")
            val rect = RectF(0.0f, 0.0f, width * progress/ 100.0f, height.toFloat())
            canvas?.drawRect(rect, paint)
        }
        if(!isDownloaded) {
            val bounds = canvas?.clipBounds
            downloadIcon.bounds = bounds
            downloadIcon.draw(canvas!!)
        } else if(isCurrent) {
            val bounds = canvas?.clipBounds
            currentIcon.bounds = bounds
            currentIcon.draw(canvas!!)
        }
    }
}