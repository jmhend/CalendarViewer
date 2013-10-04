package me.jmhend.ui.calendar_viewer;

import java.util.HashMap;
import java.util.Map;

import me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;


public class MonthPagerAdapter extends RecyclingPagerAdapter {
	
	private static final String TAG = MonthPagerAdapter.class.getSimpleName();
	
////====================================================================================
//// Static constants.
////====================================================================================
	
	public static final String KEY_POSITION = "position";
	
////====================================================================================
//// Member variables.
////====================================================================================
	
	private final Context mContext;
	private int mFirstDayOfWeek;
	private CalendarDay mStartDay;
	private CalendarDay mEndDay;
	private CalendarDay mSelectedDay;
	private final CalendarDay mCurrentDay;
	private int mCount;
	
	private Map<Integer, HeatDecorator> mDecoratorsMap = new HashMap<Integer, HeatDecorator>();
	
////====================================================================================
//// Constructor.
////====================================================================================
	
	/**
	 * Constructor.
	 * @param context
	 * @param controller
	 */
	public MonthPagerAdapter(Context context, CalendarViewerConfig config) {
		mContext = context;
		mCurrentDay = CalendarDay.currentDay();
		init(config);
		calculateCount();
	}
	
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
//// RecyclingPagerAdapter
////====================================================================================

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.RecyclingPagerAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup container) {
		MonthView monthView;
		if (convertView == null) {
			monthView = new MonthView(mContext);
			monthView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			monthView.setClickable(true);
			
			if (container instanceof OnDayClickListener) {
				monthView.setOnDayClickListener((OnDayClickListener) container);
			}
		} else {
			monthView = (MonthView) convertView;
		}
		updatePage(position, monthView);
		return monthView;
	}
	
	/**
	 * Updates the content of 'view' at position.
	 * @param position
	 * @param view
	 */
	@SuppressWarnings("unchecked")
	public void updatePage(int position, MonthView view) {
		Map<String, Integer> params = (Map<String, Integer>) view.getTag();
		if (params == null) {
			params = new HashMap<String, Integer>();
		}
		
		// Generate MonthView data.
		final int month = getMonthForPosition(position);
		final int year = getYearForPosition(position);
		final int selectedDay = isSelectedDayInMonth(year, month) ? mSelectedDay.dayOfMonth : -1;
		params.put(KEY_POSITION, Integer.valueOf(position));
		params.put(MonthView.KEY_MONTH, Integer.valueOf(month));
		params.put(MonthView.KEY_YEAR, Integer.valueOf(year));
		params.put(MonthView.KEY_SELECTED_DAY, Integer.valueOf(selectedDay));
		params.put(MonthView.KEY_WEEK_START, Integer.valueOf(mFirstDayOfWeek));
		params.put(MonthView.KEY_CURRENT_YEAR, Integer.valueOf(mCurrentDay.year));
		params.put(MonthView.KEY_CURRENT_MONTH, Integer.valueOf(mCurrentDay.month));
		params.put(MonthView.KEY_CURRENT_DAY_OF_MONTH, Integer.valueOf(mCurrentDay.dayOfMonth));

		HeatDecorator dec;
		if (mDecoratorsMap.containsKey(Integer.valueOf(position))) {
			dec = mDecoratorsMap.get(Integer.valueOf(position));
		} else {
			dec = new HeatDecorator();
			mDecoratorsMap.put(Integer.valueOf(position), dec);
		}
		
		view.reset();
		view.clearDecorators();
		view.addDecorator(dec);
		view.setMonthParams(params);
		view.invalidate();
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return mCount;
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
	
	/**
	 * Sets which day to select.
	 * @param day
	 */
	public void setSelectedDay(CalendarDay day) {
		mSelectedDay = day;
		notifyDataSetChanged();
	}

	/**
	 * 
	 * @param day
	 * @return
	 */
	public int getPositionForDay(CalendarDay day) {
		if (day.isBeforeDay(mStartDay) || day.isAfterDay(mEndDay)) {
			return -1;
		}
		int monthDiff = day.month - mStartDay.month;
		int yearDiff = day.year - mStartDay.year;
		int position = yearDiff * 12 + monthDiff;
		return position;
	}
	
	/**
	 * @param year
	 * @param month
	 * @return True if the currently selected day is the month.
	 */
	private boolean isSelectedDayInMonth(int year, int month) {
		return mSelectedDay.year == year && mSelectedDay.month == month;
	}
	
////====================================================================================
//// OnPageChangeListener
////====================================================================================

}
