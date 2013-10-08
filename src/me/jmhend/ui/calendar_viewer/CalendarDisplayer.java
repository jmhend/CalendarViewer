package me.jmhend.ui.calendar_viewer;

import me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay;

/**
 * 
 * @author jmhend
 *
 */
public interface CalendarDisplayer {
	
	/**
	 * Display the CalendarDay.
	 * @param day
	 */
	public void displayDay(CalendarDay day);

}
