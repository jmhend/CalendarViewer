package me.jmhend.ui.calendar_viewer;

import java.util.Calendar;

import me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay;

/**
 * Holds configuration fields for a CalendarViewer.
 * 
 * @author jmhend
 */
public class CalendarViewerConfig {
	
	private static final String TAG = CalendarViewerConfig.class.getSimpleName();
	
////===========================================================================================
//// Static constants.
////===========================================================================================
	
	public static final int DEFAULT_FIRST_WEEKDAY = Calendar.SUNDAY;
	
////===========================================================================================
//// Member variables.
////===========================================================================================
	
	private final int mFirstDayOfWeek;
	private final CalendarDay mStartDay;
	private final CalendarDay mEndDay;
	private final CalendarDay mSelectedDay;
	
////===========================================================================================
//// Constructor/Initializer
////===========================================================================================
	
	/**
	 * @return A Builder to build a CalendarViewerConfig.
	 */
	public static CalendarViewerConfig.Builder startBuilding() {
		return new CalendarViewerConfig.Builder();
	}

	/**
	 */
	public CalendarViewerConfig(CalendarDay startDay, CalendarDay endDay, CalendarDay selectedDay, int firstDayOfWeek) {
		mFirstDayOfWeek = firstDayOfWeek;
		mStartDay = startDay;
		mEndDay = endDay;
		mSelectedDay = selectedDay;
	}
	
////===========================================================================================
//// Getters/Setters
////===========================================================================================
	
	/**
	 * The first day of the week.
	 * Matches the indexing of Calendar.DAY_OF_WEEK
	 * @return
	 */
	public int getFirstDayOfWeek() {
		return mFirstDayOfWeek;
	}

	/**
	 * @return The day to start showing the date for.
	 */
	public CalendarDay getStartDay() {
		return mStartDay;
	}

	/**
	 * @return The last day to show the date for.
	 */
	public CalendarDay getEndDay() {
		return mEndDay;
	}

	/**
	 * @return The currently selected day.
	 */
	public CalendarDay getSelectedDay() {
		return mSelectedDay;
	}
	
////===========================================================================================
//// Builder
////===========================================================================================
	
	public static class Builder {
		private int mFirstDayOfWeek;
		private CalendarDay mStartDay;
		private CalendarDay mEndDay;
		private CalendarDay mSelectedDay;

		public Builder() {
			mFirstDayOfWeek = DEFAULT_FIRST_WEEKDAY;
			mSelectedDay = CalendarDay.currentDay();
		}
		
		public Builder firstDayOfWeek(int firstDayOfWeek) {
			mFirstDayOfWeek = firstDayOfWeek;
			return this;
		}
		
		public Builder starts(CalendarDay startDay) {
			mStartDay = startDay;
			return this;
		}
		
		public Builder ends(CalendarDay endDay) {
			mEndDay = endDay;
			return this;
		}
		
		public Builder withSelectedDay(CalendarDay selectedDay) {
			mSelectedDay = selectedDay;
			return this;
		}
		
		public CalendarViewerConfig build() {
			if (mStartDay == null || mEndDay == null) {
				throw new IllegalStateException("Start Day and End Day must be set!");
			}
			return new CalendarViewerConfig(mStartDay, mEndDay, mSelectedDay, mFirstDayOfWeek);
		}
	}
}
