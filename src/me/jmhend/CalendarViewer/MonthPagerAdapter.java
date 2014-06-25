package me.jmhend.CalendarViewer;

import java.util.HashMap;
import java.util.Map;

import me.jmhend.CalendarViewer.CalendarController.OnCalendarControllerChangeListener;
import me.jmhend.CalendarViewer.CalendarView.OnDayClickListener;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;


public class MonthPagerAdapter extends CalendarAdapter implements OnCalendarControllerChangeListener {
	
	private static final String TAG = MonthPagerAdapter.class.getSimpleName();
	
////====================================================================================
//// Member variables.
////====================================================================================
	
	private final Context mContext;
	private final CalendarController mController;
	private final CalendarModel mModel;
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
	public MonthPagerAdapter(Context context, CalendarModel model, CalendarController controller) {
		mContext = context.getApplicationContext();
		mController = controller;
		mController.registerListener(this);
		mModel = model;
		calculateCount();
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
		updateView(position, monthView);
		return monthView;
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
	 * Updates the content of 'view' at position.
	 * @param position
	 * @param view
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void updateView(int position, View view) {
		MonthView monthView = (MonthView) view;
		monthView.setModel(mModel);
		Map<String, Integer> params = (Map<String, Integer>) monthView.getTag();
		if (params == null) {
			params = new HashMap<String, Integer>();
		}
		
		// Generate MonthView data.
		final int month = getMonthForPosition(position);
		final int year = getYearForPosition(position);
		final int selectedDay = isSelectedDayInMonth(year, month)? mController.getSelectedDay().dayOfMonth : -1;
		final int focusedDay = isFocusedDayInMonth(year, month)? mController.getFocusedDay().dayOfMonth : -1;
		params.put(CalendarAdapter.KEY_POSITION, Integer.valueOf(position));
		params.put(MonthView.KEY_MONTH, Integer.valueOf(month));
		params.put(MonthView.KEY_YEAR, Integer.valueOf(year));
		params.put(MonthView.KEY_SELECTED_DAY, Integer.valueOf(selectedDay));
		params.put(MonthView.KEY_FOCUSED_DAY, Integer.valueOf(focusedDay));
		params.put(MonthView.KEY_WEEK_START, Integer.valueOf(mController.getFirstDayOfWeek()));
		params.put(MonthView.KEY_CURRENT_YEAR, Integer.valueOf(mController.getCurrentDay().year));
		params.put(MonthView.KEY_CURRENT_MONTH, Integer.valueOf(mController.getCurrentDay().month));
		params.put(MonthView.KEY_CURRENT_DAY_OF_MONTH, Integer.valueOf(mController.getCurrentDay().dayOfMonth));

//		monthView.reset();
		monthView.setParams(params);
		monthView.setHideFocusedWeek(false);
		monthView.invalidate();
	}

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarAdapter#getPositionForDay(me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public int getPositionForDay(CalendarDay day) {
		if (day.isBeforeDay(mController.getStartDay()) || day.isAfterDay(mController.getEndDay())) {
			return -1;
		}
		int monthDiff = day.month - mController.getStartDay().month;
		int yearDiff = day.year - mController.getStartDay().year;
		int position = yearDiff * 12 + monthDiff;
		return position;
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarAdapter#setSelectedDay(me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public void setSelectedDay(CalendarDay day) {
		mController.setSelectedDay(day);
		updateViewPager();
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.CalendarAdapter#getFocusedDay(int)
	 */
	@Override
	public CalendarDay getFocusedDay(int position) {
		final int month = getMonthForPosition(position);
		final int year = getYearForPosition(position);
		if (isSelectedDayInMonth(year, month)) {
			return mController.getSelectedDay();
		}
		return new CalendarDay(year, month, 1);
	}
	
////====================================================================================
//// Positioning
////====================================================================================
	
	/**
	 * Calculates how many months the list will contain.
	 */
	private void calculateCount() {
		int startMonths = mController.getStartDay().year * 12 + mController.getStartDay().month;
		int endMonths = mController.getEndDay().year * 12 + mController.getEndDay().month;
		int months = endMonths - startMonths + 1;
		mCount = months;
	}
	
	/**
	 * Gets which month to display for 'position'
	 * @param position
	 * @return
	 */
	private int getMonthForPosition(int position) {
		int month = (position + mController.getStartDay().month) % 12;
		return month;
	}
	
	/**
	 * Gets which year to display for 'position'
	 * @param position
	 * @return
	 */
	private int getYearForPosition(int position) {
		int year = (position + mController.getStartDay().month) / 12 + mController.getStartDay().year;
		return year;
	}
	
	/**
	 * @param year
	 * @param month
	 * @return True if the currently selected day is in the month.
	 */
	private boolean isSelectedDayInMonth(int year, int month) {
		return mController.getSelectedDay().year == year && mController.getSelectedDay().month == month;
	}
	
	/**
	 * @param year
	 * @param month
	 * @return True if the currently focused day is in the month.
	 */
	private boolean isFocusedDayInMonth(int year, int month) {
		return mController.getFocusedDay().year == year && mController.getFocusedDay().month == month;
	}
	
////====================================================================================
//// OnCalendarControllerChangeListener
////====================================================================================

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.CalendarController.OnCalendarControllerChangeListener
	 * #onChange(me.jmhend.CalendarViewer.CalendarController, java.lang.Object, java.lang.String)
	 */
	@Override
	public void onChange(CalendarController controller, Object obj, String tag) {
		if (CalendarController.FIRST_DAY_OF_WEEK.equals(tag)
				|| CalendarController.START_DAY.equals(tag)
				|| CalendarController.END_DAY.equals(tag)) {
			calculateCount();
		}
		if (CalendarController.SELECTED_DAY.equals(tag)) {
			CalendarDay selectedDay = (CalendarDay) obj;
			if (selectedDay != null) {
				getViewPager().setCurrentDay(selectedDay);
			}
		}
		updateViewPager();
	}
}
