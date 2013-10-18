package me.jmhend.CalendarViewer;

import java.util.Calendar;

import me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay;
import me.jmhend.CalendarViewer.WeekView.WeekRange;

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
	
	/**
	 * Returns the start CalendarDay and end CalendarDay of the week that contains 'day'.
	 * Assumes the desired first day of week is already set on 'cal'.
	 * 
	 * @param day
	 * @return
	 */
	public static WeekRange getWeekRangeForDay(Calendar cal, CalendarDay day) {
		if (cal == null) {
			cal = Calendar.getInstance();
		}
		cal.set(Calendar.YEAR, day.year);
		cal.set(Calendar.MONTH, day.month);
		cal.set(Calendar.DAY_OF_MONTH, day.dayOfMonth);
		
		int firstDayOfWeek = cal.getFirstDayOfWeek();
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		int offset = dayOfWeek - firstDayOfWeek;
		if (offset < 0) {
			offset += 7;
		}
		
		cal.add(Calendar.DAY_OF_YEAR, -offset);
		CalendarDay start = CalendarDay.fromCalendar(cal);
		
		cal.add(Calendar.DAY_OF_YEAR, 6);
		CalendarDay end = CalendarDay.fromCalendar(cal);
		
		return new WeekRange(start, end);
	}

}
