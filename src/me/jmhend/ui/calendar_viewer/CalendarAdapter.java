package me.jmhend.ui.calendar_viewer;

import java.util.Calendar;

import org.joda.time.DateTime;

/**
 * Abstract class for providing Views for a collection of Calendar days.
 * @author jmhend
 */
public abstract class CalendarAdapter extends RecyclingPagerAdapter {
	
	public static final String KEY_POSITION = "position";
	
////=====================================================================================
//// Abstract
////=====================================================================================
	
	/**
	 * Updates the content of the ViewGroup child at 'position', whose View is 'view'.
	 * @param position
	 * @param view
	 */
	public abstract void updateView(int position, CalendarView view);
	
	/**
	 * Returns the position in the associated ViewGroup of 'day'
	 * @param day
	 * @return
	 */
	public abstract int getPositionForDay(CalendarDay day);
	
	/**
	 * Sets 'day' as the selected Day in the ViewGroup.
	 * @param day
	 */
	public abstract void setSelectedDay(CalendarDay day);
	
////=====================================================================================
//// CalendarDay
////=====================================================================================
	
	/**
	 * Represents a day on the Calendar.
	 * 
	 * @author jmhend
	 */
	public static class CalendarDay {
		int year;
		int month;
		int dayOfMonth;
		
		/**
		 * @return CalendarDay initialized to the current day.
		 */
		public static CalendarDay currentDay() {
			return fromCalendar(Calendar.getInstance());
		}
		
		/**
		 * @param calendar
		 * @return CalendarDay initialized to the same day has 'calendar'
		 * is current set to.
		 */
		public static CalendarDay fromCalendar(Calendar calendar) {
			return new CalendarDay(calendar.get(Calendar.YEAR), 
					calendar.get(Calendar.MONTH),
					calendar.get(Calendar.DAY_OF_MONTH));
		}
		
		/**
		 * @param time
		 * @return CalendarDay initialized to the same day as 'time'.
		 */
		public static CalendarDay fromTime(long time) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(time);
			return fromCalendar(calendar);
		}
		
		/**
		 * Constructor with all args.
		 * @param year
		 * @param month
		 * @param dayOfMonth
		 */
		public CalendarDay(int year, int month, int dayOfMonth) { 
			this.year = year;
			this.month = month;
			this.dayOfMonth = dayOfMonth;
		}
		
		/**
		 */
		public CalendarDay() { }
		
		/**
		 * @return A DateTime with the same time information as this CalendarDay.
		 */
		public DateTime toDateTime() {
			// JodaTime months are 1-12.
			return new DateTime(year, month + 1, dayOfMonth, 0, 0);
		}
		
		/**
		 * Fill from another CalendarDay.
		 * @param day
		 */
		public void set(CalendarDay day) {
			this.year = day.year;
			this.month = day.month;
			this.dayOfMonth = day.dayOfMonth;
		}
		
		/**
		 * Set date fields.
		 * @param year
		 * @param month
		 * @param dayOfMonth
		 */
		public void set(int year, int month, int dayOfMonth) {
			this.year = year;
			this.month = month;
			this.dayOfMonth = dayOfMonth;
		}
		
		/**
		 * @param day
		 * @return True if this CalendarDay is the same day as 'day'.
		 */
		public boolean isSameDay(CalendarDay day) {
			return (year == day.year) && (month == day.month) && (dayOfMonth == day.dayOfMonth);
		}
		
		/**
		 * @param calendar
		 * @return True if this CalendarDay is the same day as the set day of the Calendar.
		 */
		public boolean isSameDay(Calendar calendar) {
			if (calendar.get(Calendar.YEAR) != year) {
				return false;
			}
			if (calendar.get(Calendar.MONTH) != month) {
				return false;
			}
			if (calendar.get(Calendar.DAY_OF_MONTH) != dayOfMonth) {
				return false;
			}
			return true;
		}
		
		/**
		 * @param day
		 * @return True if this CalendarDay is before 'day'.
		 */
		public boolean isBeforeDay(CalendarDay day) {
			if (year < day.year) {
				return true;
			}
			if (year > day.year) {
				return false;
			}
			if (month < day.month) {
				return true;
			}
			if (month > day.month) {
				return false;
			}
			return dayOfMonth < day.dayOfMonth;
		}
		
		/**
		 * @param day
		 * @return True if this CalendarDay is after 'day'.
		 */
		public boolean isAfterDay(CalendarDay day) {
			return !isSameDay(day) && !isBeforeDay(day);
		}
		
		/**
		 * @param start
		 * @param end
		 * @return True if this CalendarDay is between 'start' and 'end', inclusive.
		 */
		public boolean isBetween(CalendarDay start, CalendarDay end) {
			if (isBeforeDay(start)) {
				return false;
			}
			if (isAfterDay(end)) {
				return false;
			}
			return true;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if (o == null) {
				return false;
			}
			if (!(o instanceof CalendarDay)) {
				return false;
			}
			CalendarDay other = (CalendarDay) o;
			return other.dayOfMonth == dayOfMonth && other.month == month && other.year == year;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return year * 10000 + month * 100 + dayOfMonth;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return (month + 1) + "/" + dayOfMonth + "/" + year;
		}
	}
}
