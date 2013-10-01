package me.jmhend.ui.calendar_viewer;

import me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay;

/**
 * Controls and settings for CalendarViewer operations.
 * 
 * @author jmhend
 */
public interface CalendarController {
	
	/**
	 * The first day of the week.
	 * Matches the indexing of Calendar.DAY_OF_WEEK
	 * @return
	 */
	public abstract int getFirstDayOfWeek();

	/**
	 * @return The day to start showing the date for.
	 */
	public abstract CalendarDay getStartDay();

	/**
	 * @return The last day to show the date for.
	 */
	public abstract CalendarDay getEndDay();

	/**
	 * @return The currently selected dat.
	 */
	public abstract CalendarDay getSelectedDay();

	/**
	 * Callback when a Day is selected.
	 * @param year
	 * @param month
	 * @param dayOfMonth
	 */
	public abstract void onDaySelected(int year, int month, int dayOfMonth);
}
