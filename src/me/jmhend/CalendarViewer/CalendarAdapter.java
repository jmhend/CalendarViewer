package me.jmhend.CalendarViewer;

import java.util.Calendar;

import org.joda.time.DateTime;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Abstract class for providing Views for a collection of Calendar days.
 * @author jmhend
 */
public abstract class CalendarAdapter extends RecyclingPagerAdapter {
	
	public static final String KEY_POSITION = "position";
	
////=====================================================================================
//// Member variables.
////=====================================================================================
	
	private CalendarViewPager mViewPager;
	
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
//// ViewPager
////=====================================================================================
	
	/**
	 * @return
	 */
	protected CalendarViewPager getViewPager() {
		return mViewPager;
	}
	
	/**
	 * @param pager
	 */
	public void setViewPager(CalendarViewPager pager) {
		mViewPager = pager;
	}
	
	/**
	 * Updates the content of the ViewPager.
	 */
	protected void updateViewPager() {
		if (mViewPager != null) {
			mViewPager.updateVisiblePages();
		}
	}
	
////=====================================================================================
//// CalendarDay
////=====================================================================================
	
	/**
	 * Represents a day on the Calendar.
	 * 
	 * @author jmhend
	 */
	public static class CalendarDay implements Parcelable {
		public int year;
		public int month;
		public int dayOfMonth;
		
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
		 * @return A Calendar with the same time information as this CalendarDay.
		 */
		public Calendar toCalendar() {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			cal.set(Calendar.MONTH, month);
			cal.set(Calendar.YEAR, year);
			return cal;
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
		
		/**
		 * Parcel constructor.
		 * @param in
		 */
		public CalendarDay(Parcel in) {
			year = in.readInt();
			month = in.readInt();
			dayOfMonth = in.readInt();
		}

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
			dest.writeInt(year);
			dest.writeInt(month);
			dest.writeInt(dayOfMonth);
		}
		
		public static final Parcelable.Creator<CalendarDay> CREATOR = new Parcelable.Creator<CalendarDay>() {
			@Override
			public CalendarDay createFromParcel(Parcel source) {
				return new CalendarDay(source);
			}

			@Override
			public CalendarDay[] newArray(int size) {
				return new CalendarDay[size];
			}		
		};
	}
}
