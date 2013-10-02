package me.jmhend.ui.calendar_viewer;

import me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay;

/**
 * Utility methods.
 * 
 * @author jmhend
 *
 */
public class Utils {
	
	private static final String TAG = Utils.class.getSimpleName();
	
////===============================================================================
////
////===============================================================================
	
	private Utils() { }
	
	/**
	 * Gets how many days are in the month.
	 * @param month
	 * @param year
	 * @return
	 */
	public static int getDaysInMonth(int month, int year) {
		switch(month) {
			case 0:
			case 2:
			case 4:
			case 6:
			case 7:
			case 9:
			case 11:
				return 31;
			case 3:
			case 5:
			case 8:
			case 10:
				return 30;
			case 1:
				if(year % 4 == 0)
					return 29;
				return 28;
			default:
				throw new IllegalArgumentException("Invalid Month");
		}
	}
	
	/**
	 * @param day
	 * @return True if the 'day' is the current day, or a day in the future.
	 */
	public static boolean isDayCurrentOrFuture(CalendarDay day) {
		CalendarDay currentDay = CalendarDay.currentDay();
		if (day.year < currentDay.year) {
			return false;
		}
		if (day.year > currentDay.year) {
			return true;
		}
		if (day.month < currentDay.month) {
			return false;
		}
		if (day.month > currentDay.month) {
			return true;
		}
		return day.dayOfMonth >= currentDay.dayOfMonth;
	}

}
