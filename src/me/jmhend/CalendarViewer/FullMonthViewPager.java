package me.jmhend.CalendarViewer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jmhend on 1/6/15.
 */
public class FullMonthViewPager extends CalendarViewPager {

////====================================================================================
//// Constructor
////====================================================================================

    private static final String TAG = FullMonthViewPager.class.getSimpleName();

    public FullMonthViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullMonthViewPager(Context context) {
        super(context);
    }

////====================================================================================
//// OnDayClickListener
////====================================================================================

    /*
     * (non-Javadoc)
     * @see me.jmhend.CalendarViewer.CalendarView.OnDayClickListener#
     * onDayClick(me.jmhend.CalendarViewer.CalendarView, me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay)
     */
    @Override
    public void onDayClick(View calendarView, CalendarAdapter.CalendarDay day) {
        if (day != null) {
            if (mDayClickListener != null) {
                mDayClickListener.onDayClick(calendarView, day);
            }
        }
    }
}
