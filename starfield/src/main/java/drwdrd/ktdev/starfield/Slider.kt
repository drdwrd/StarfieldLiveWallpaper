package drwdrd.ktdev.starfield

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import drwdrd.ktdev.engine.clamp
import java.text.DecimalFormat
import kotlin.math.ceil


class Slider : ConstraintLayout {

    interface OnValueChangedListener {
        fun onValueChanged(value : Float)
    }

    private var seekBar : SeekBar? = null
    private var captionTextView : TextView? = null
    private var valueTextView : TextView? = null
    private var leftImageView : ImageView? = null
    private var rightImageView : ImageView? = null
    private var title : CharSequence? = null
    private var leftIcon : Drawable? = null
    private var rightIcon : Drawable? = null
    private var showValueTextView : Boolean = false
    private lateinit var decimalFormat : DecimalFormat

    var minValue : Float = 0.0f
        private set

    var maxValue : Float = 100.0f
        private set

    var stepSize : Float = 0.1f
        private set

    var value : Float = 0.0f
        set(v) {
            val cv = clamp(v, minValue, maxValue)
            seekBar?.progress = ceil((cv - minValue) / stepSize).toInt()
            field = cv
        }

    var onValueChangedListener : OnValueChangedListener? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context : Context, attrs : AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context : Context, attrs : AttributeSet, defStyleAttr : Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.slider, this)

        context.obtainStyledAttributes(attrs, R.styleable.Slider, 0, 0).apply {
            title = getText(R.styleable.Slider_title)
            leftIcon = getDrawable(R.styleable.Slider_leftIcon)
            rightIcon = getDrawable(R.styleable.Slider_rightIcon)
            minValue = getFloat(R.styleable.Slider_minValue, 0.0f)
            maxValue = getFloat(R.styleable.Slider_maxValue, 100.0f)
            stepSize = getFloat(R.styleable.Slider_stepSize, 0.1f)
            value = clamp(getFloat(R.styleable.Slider_value, 0.0f), minValue, maxValue)
            showValueTextView = getBoolean(R.styleable.Slider_showValue, false)
            val format = getString(R.styleable.Slider_decimalFormat) ?: "##"
            decimalFormat = DecimalFormat(format)
            recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        seekBar = findViewById(R.id.sliderSeekBar)
        seekBar?.max = ceil((maxValue - minValue) / stepSize).toInt()
        seekBar?.progress = ceil((value - minValue) / stepSize).toInt()
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                value = minValue + progress * stepSize
                valueTextView?.text = decimalFormat.format(value)
                onValueChangedListener?.onValueChanged(value)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        captionTextView = findViewById(R.id.sliderCaptionTextView)
        if(title != null) {
            captionTextView?.text = title.toString()
        } else {
            captionTextView?.visibility = View.GONE
        }

        val textView = findViewById<TextView>(R.id.sliderValueTextView)
        if(showValueTextView) {
            textView.text = decimalFormat.format(value)
            valueTextView = textView
        } else {
            textView.visibility = View.GONE
        }

        leftImageView = findViewById(R.id.sliderLeftImageView)
        if(leftIcon != null) {
            leftImageView?.setImageDrawable(leftIcon)
        } else {
            leftImageView?.visibility = View.GONE
        }

        rightImageView = findViewById(R.id.sliderRightImageView)
        if(rightIcon != null) {
            rightImageView?.setImageDrawable(rightIcon)
        } else {
            rightImageView?.visibility = View.GONE
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        seekBar?.isEnabled = enabled
    }

}