package me.jmhend.ui.calendar_viewer;

import java.util.Calendar;

import me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay;
import me.jmhend.ui.calendar_viewer.CalendarViewer.Mode;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Holds configuration fields for a CalendarViewer.
 * 
 * @author jmhend
 */
public class CalendarViewerConfig implements Parcelable {
	
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
	private final Mode mMode;
	
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
	 * @return The default configuration for a CalendarViewer.
	 */
	public static CalendarViewerConfig getDefault() {
		CalendarDay startDay = CalendarDay.currentDay();
		Calendar cal = startDay.toCalendar();
		cal.add(Calendar.YEAR, 1);
		CalendarDay endDay = CalendarDay.fromCalendar(cal);
		return new CalendarViewerConfig.Builder().starts(startDay).ends(endDay).build();
	}

	/**
	 */
	public CalendarViewerConfig(CalendarDay startDay, CalendarDay endDay, CalendarDay selectedDay, int firstDayOfWeek, Mode mode) {
		mFirstDayOfWeek = firstDayOfWeek;
		mStartDay = startDay;
		mEndDay = endDay;
		mSelectedDay = selectedDay;
		mMode = mode;
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
	
	/** 
	 * @return The CalendarViewer Mode to which will be set.
	 */
	public Mode getMode() {
		return mMode;
	}
	
////===========================================================================================
//// Builder
////===========================================================================================
	
	public static class Builder {
		private int mFirstDayOfWeek;
		private CalendarDay mStartDay;
		private CalendarDay mEndDay;
		private CalendarDay mSelectedDay;
		private Mode mMode;

		public Builder() {
			mFirstDayOfWeek = DEFAULT_FIRST_WEEKDAY;
			mSelectedDay = CalendarDay.currentDay();
			mMode = Mode.CLOSED;
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
		
		public Builder selectedDay(CalendarDay selectedDay) {
			mSelectedDay = selectedDay;
			return this;
		}
		
		public Builder mode(Mode mode) {
			mMode = mode;
			return this;
		}
		
		public CalendarViewerConfig build() {
			if (mStartDay == null || mEndDay == null) {
				throw new IllegalStateException("Start Day and End Day must be set!");
			}
			return new CalendarViewerConfig(mStartDay, mEndDay, mSelectedDay, mFirstDayOfWeek, mMode);
		}
	}
	
////===========================================================================================
//// Parcel
////===========================================================================================

	/*
	 * (non-Javadoc)
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mFirstDayOfWeek);
		dest.writeParcelable(mStartDay, 0);
		dest.writeParcelable(mEndDay, 0);
		dest.writeParcelable(mSelectedDay, 0);
		dest.writeInt(mMode.intValue());
	}
	
	/**
	 * Parcel constructor.
	 * @param in
	 */
	public CalendarViewerConfig(Parcel in) {
		mFirstDayOfWeek = in.readInt();
		mStartDay = in.readParcelable(CalendarDay.class.getClassLoader());
		mEndDay = in.readParcelable(CalendarDay.class.getClassLoader());
		mSelectedDay = in.readParcelable(CalendarDay.class.getClassLoader());
		mMode = Mode.ofValue(in.readInt());
	}
	
	public static final Parcelable.Creator<CalendarViewerConfig> CREATOR = new Parcelable.Creator<CalendarViewerConfig>() {
		@Override
		public CalendarViewerConfig createFromParcel(Parcel source) {
			return new CalendarViewerConfig(source);
		}
		@Override
		public CalendarViewerConfig[] newArray(int size) {
			return new CalendarViewerConfig[size];
		}		
	};
}
