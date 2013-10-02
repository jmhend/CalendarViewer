package me.jmhend.ui.calendar_viewer;

import me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay;
import android.view.View;

/**
 * Listens for TouchEvents on calendar View days.
 * 
 * @author jmhend
 *
 */
public interface OnDayClickListener {
	
	/**
	 * Called when a day is clicked on a calendar View.
	 * @param calendarView
	 * @param day
	 */
	public void onDayClick(View calendarView, CalendarDay day);
	
	/**
	 * Called when a day is long-clicked on a calendar View.
	 * @param calendarView
	 * @param day
	 */
	public void onDayLongClick(View calendarView, CalendarDay day);
}