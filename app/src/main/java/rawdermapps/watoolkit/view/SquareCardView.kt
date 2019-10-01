package rawdermapps.watoolkit.view

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView

/* Custom view which provides a way to
 * create 'square' cards */
class SquareCardView : CardView {
    constructor(context : Context) : super(context)
    constructor(context: Context, attr : AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, defStyle : Int) : super(context, attr, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) =
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
}