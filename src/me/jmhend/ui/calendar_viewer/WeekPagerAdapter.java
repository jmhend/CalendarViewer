package me.jmhend.ui.calendar_viewer;

import java.util.HashMap;
import java.util.Map;

import me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * PagerAdapter for supplying a ViewPager with WeekViews.
 * @author jmhend
 */
public class WeekPagerAdapter extends RecyclingPagerAdapter {
	
	private static final String TAG = WeekPagerAdapter.class.getSimpleName();
	
////======================================================================================
//// Static constants.
////======================================================================================
	
////======================================================================================
//// Member variables.
////======================================================================================
	
	private final Context mContext;
	private int mFirstDayOfWeek;
	private CalendarDay mStartDay;
	private CalendarDay mEndDay;
	private CalendarDay mSelectedDay;
	private final CalendarDay mCurrentDay;
	private int mCount;
	
	private Map<Integer, HeatDecorator> mDecoratorsMap = new HashMap<Integer, HeatDecorator>();
	
////======================================================================================
//// Constructor.
////======================================================================================
	
	/**
	 * Constructor.
	 * @param context
	 * @param controller
	 */
	public WeekPagerAdapter(Context context, CalendarViewerConfig config) {
		mContext = context;
		mCurrentDay = CalendarDay.currentDay();
		init(config);
		calculateCount();
	}
	
////======================================================================================
//// Init.
////======================================================================================
	
	/**
	 * Initialize.
	 */
	private void init(CalendarViewerConfig config) {
		mFirstDayOfWeek = config.getFirstDayOfWeek();
		mStartDay = config.getStartDay();
		mEndDay = config.getEndDay();
		mSelectedDay = config.getSelectedDay();
	}

////====================================================================================
//// Positioning
////====================================================================================
	
	/**
	 * Calculates how many months the list will contain.
	 */
	private void calculateCount() {
		int startMonths = mStartDay.year * 12 + mStartDay.month;
		int endMonths = mEndDay.year * 12 + mEndDay.month;
		int months = endMonths - startMonths + 1;
		mCount = months;
	}
	
	/**
	 * Gets which month to display for 'position'
	 * @param position
	 * @return
	 */
	private int getMonthForPosition(int position) {
		int month = (position + mStartDay.month) % 12;
		return month;
	}
	
	/**
	 * Gets which year to display for 'position'
	 * @param position
	 * @return
	 */
	private int getYearForPosition(int position) {
		int year = (position + mStartDay.month) / 12 + mStartDay.year;
		return year;
	}
	
	// 1. When creating adapter, determine the first week, using the Utils.getWeekRangeForCalendarDay(mStartDay).
	// 2. ''                   , determine the end week, using 				''
	// 3. TODO: getCount()??
	// 3. for getWeekStart(position): mStartDay.asCalendar().add(Calendar.DAY_OF_YEAR,7 * position) ?
	
	private CalendarDay getWeekStartForPosition(int position) {
		return null;
	}
////====================================================================================
//// View
////====================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.RecyclingPagerAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup container) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

}
