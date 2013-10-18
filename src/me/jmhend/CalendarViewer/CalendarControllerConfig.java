package me.jmhend.CalendarViewer;

import java.util.Calendar;

import me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay;
import me.jmhend.CalendarViewer.CalendarViewer.Mode;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Holds configuration fields for a CalendarViewer.
 * 
 * @author jmhend
 */
public class CalendarControllerConfig implements Parcelable {
	
	private static final String TAG = CalendarControllerConfig.class.getSimpleName();
	
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
	public static CalendarControllerConfig.Builder startBuilding() {
		return new CalendarControllerConfig.Builder();
	}
	
	/**
	 * @return The default configuration for a CalendarViewer.
	 */
	public static CalendarControllerConfig getDefault() {
		CalendarDay startDay = CalendarDay.currentDay();
		Calendar cal = startDay.toCalendar();
		cal.add(Calendar.YEAR, 1);
		CalendarDay endDay = CalendarDay.fromCalendar(cal);
		return new CalendarControllerConfig.Builder().starts(startDay).ends(endDay).build();
	}

	/**
	 */
	public CalendarControllerConfig(CalendarDay startDay, CalendarDay endDay, CalendarDay selectedDay, int firstDayOfWeek, Mode mode) {
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
			mMode = Mode.WEEK;
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
		
		public CalendarControllerConfig build() {
			if (mStartDay == null || mEndDay == null) {
				throw new IllegalStateException("Start Day and End Day must be set!");
			}
			return new CalendarControllerConfig(mStartDay, mEndDay, mSelectedDay, mFirstDayOfWeek, mMode);
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
	public CalendarControllerConfig(Parcel in) {
		mFirstDayOfWeek = in.readInt();
		mStartDay = in.readParcelable(CalendarDay.class.getClassLoader());
		mEndDay = in.readParcelable(CalendarDay.class.getClassLoader());
		mSelectedDay = in.readParcelable(CalendarDay.class.getClassLoader());
		mMode = Mode.ofValue(in.readInt());
	}
	
	public static final Parcelable.Creator<CalendarControllerConfig> CREATOR = new Parcelable.Creator<CalendarControllerConfig>() {
		@Override
		public CalendarControllerConfig createFromParcel(Parcel source) {
			return new CalendarControllerConfig(source);
		}
		@Override
		public CalendarControllerConfig[] newArray(int size) {
			return new CalendarControllerConfig[size];
		}		
	};
}
