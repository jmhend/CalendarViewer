package me.jmhend.ui.calendar_viewer;

import me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * View displaying a Week of days.
 * 
 * @author jmhend
 */
public class WeekView extends View {
	
	private static final String TAG = WeekView.class.getSimpleName();
	
////======================================================================================
//// Static constants.
////======================================================================================

	public static final int DAY_SEPARATOR_WIDTH = MonthView.DAY_SEPARATOR_WIDTH;
	
	public static final int MAX_DAYS = 7;
	
////======================================================================================
//// WeekRange
////======================================================================================
	
	/**
	 * Defines the day range for a week.
	 * 
	 * @author jmhend
	 */
	public static class WeekRange {
		public CalendarDay weekStart;
		public CalendarDay weekEnd;
		
		public WeekRange(CalendarDay start, CalendarDay end) {
			this.weekStart = start;
			this.weekEnd = end;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return weekStart.toString() + "-" + weekEnd.toString();
		}
	}
	
////======================================================================================
//// Member variables.
////======================================================================================
	
	private int mRowHeight;
	private int mDayTextSize;
	private int mMonthHeaderHeight;
	private int mWidth;
	private int mPadding;
	
	private int mDaysPerWeek;
	private int mNumCells;
	
	private int mTodayOfWeek;
	
	private final int[] mDayXs = new int[MAX_DAYS];
	private final boolean[] mDayActives = new boolean[MAX_DAYS];

////======================================================================================
//// Constructor.
////======================================================================================
	
	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public WeekView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public WeekView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	/**
	 * @param context
	 */
	public WeekView(Context context) {
		super(context);
		init();
	}
	
////======================================================================================
//// Init.
////======================================================================================

	/**
	 * Initialize.
	 */
	private void init() {
		
	}
	
////======================================================================================
//// Logic.
////======================================================================================
	
	
	/**
	 * Calculates the (x,y) coordinates of each day in the month.
	 */
	protected void calculateDayPoints() {
//		clearDayArrays();
//		int y = (mRowHeight + mDayTextSize) / 2 - DAY_SEPARATOR_WIDTH + mMonthHeaderHeight;
//		int paddingDay = (mWidth - 2 * mPadding) / (2 * mDaysPerWeek);
//		int dayOffset = findDayOffset();
//		int day = 1;
//		
//		while (day <= mNumCells) {
//			int x = paddingDay * (1 + 2 * dayOffset) + mPadding;
//			mDayXs[day-1] = x;
//			mDayYs[day-1] = y;
//			
//			boolean active = (mTodayOfWeek == day) || isCurrentDayOrLater(day);
//			mDayActives[day-1] = active;
//			
//			// Reached the end of the week, start drawing on the next line.
//			dayOffset++;
//			if (dayOffset == mDaysPerWeek) {
//				dayOffset = 0;
//				y+= mRowHeight;
//			}
//			day++;
//		}
	}
	
////==================================================================================================
////Utility.
////==================================================================================================
	
	/**
	 * Zero-out point arrays, reset valid array.
	 */
	private void clearDayArrays() {
		for (int i = 0; i < MAX_DAYS; i++) {
			mDayXs[i] = 0;
			mDayActives[i] = false;
		}
	}	
}
