package rawdermapps.watoolkit.util

import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomnavigation.BottomNavigationView

/* Returns the view for tab at given index */
fun BottomNavigationView.getTabViewAt(index : Int) : View =
    (this.getChildAt(0) as ViewGroup)
        .getChildAt(index)
