package me.jmhend.CalendarViewer;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay;
import me.jmhend.CalendarViewer.CalendarController.OnCalendarControllerChangeListener;
import me.jmhend.CalendarViewer.CalendarView.OnDayClickListener;

import org.joda.time.DateTime;
import org.joda.time.Weeks;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;

/**
 * PagerAdapter for supplying a ViewPager with WeekViews.
 * @author jmhend
 */
public class WeekPagerAdapter extends CalendarAdapter implements OnCalendarControllerChangeListener {
	
	private static final String TAG = WeekPagerAdapter.class.getSimpleName();
	
////======================================================================================
//// Static constants.
////======================================================================================
	
////======================================================================================
//// Member variables.
////======================================================================================
	
	private final Context mContext;
	private final Calendar mCalendar;
	private final CalendarController mController;
	private final CalendarModel mModel;
	private CalendarDay mFirstVisibleDay;
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
	public WeekPagerAdapter(Context context, CalendarModel model, CalendarController controller) {
		mContext = context.getApplicationContext();
		mController = controller;
		mController.registerListener(this);
		mModel = model;
		mCalendar = Calendar.getInstance();
		mCalendar.set(Calendar.MILLISECOND, 0);
		mCalendar.set(Calendar.SECOND, 0);
		mCalendar.set(Calendar.MINUTE, 0);
		mCalendar.set(Calendar.HOUR, 0);
		updateFromController(controller);
		calculateCount();
	}
	
////======================================================================================
//// Init.
////======================================================================================

	/**
	 * Updates the cached time fields from the ConfigProvider.
	 * @param cp
	 */
	private void updateFromController(CalendarController controller) {
		mCalendar.setFirstDayOfWeek(controller.getFirstDayOfWeek());
		mFirstVisibleDay = Utils.getWeekRangeForDay(mCalendar, controller.getStartDay()).weekStart;
	}
	
////====================================================================================
//// CalendarAdapter
////====================================================================================

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarAdapter#updateView(int, me.jmhend.ui.calendar_viewer.CalendarView)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void updateView(int position, View view) {
		WeekView weekView = (WeekView) view;
		weekView.setModel(mModel);
		Map<String, Integer> params = (Map<String, Integer>) weekView.getTag();
		if (params == null) {
			params = new HashMap<String, Integer>();
		}
		
		// Generate WeekView data.
		CalendarDay startDay = getWeekStartForPosition(position);
		CalendarDay endDay = getWeekEndForPosition(position);
		params.put(CalendarAdapter.KEY_POSITION, Integer.valueOf(position));
		params.put(WeekView.KEY_WEEK_START, Integer.valueOf(mController.getFirstDayOfWeek()));
		params.put(WeekView.KEY_START_YEAR, Integer.valueOf(startDay.year));
		params.put(WeekView.KEY_START_MONTH, Integer.valueOf(startDay.month));
		params.put(WeekView.KEY_START_DAY_OF_MONTH, Integer.valueOf(startDay.dayOfMonth));
		params.put(WeekView.KEY_END_YEAR, Integer.valueOf(endDay.year));
		params.put(WeekView.KEY_END_MONTH, Integer.valueOf(endDay.month));
		params.put(WeekView.KEY_END_DAY_OF_MONTH, Integer.valueOf(endDay.dayOfMonth));
		params.put(WeekView.KEY_SELECTED_YEAR, Integer.valueOf(mController.getSelectedDay().year));
		params.put(WeekView.KEY_SELECTED_MONTH, Integer.valueOf(mController.getSelectedDay().month));
		params.put(WeekView.KEY_SELECTED_DAY_OF_MONTH, Integer.valueOf(mController.getSelectedDay().dayOfMonth));
		params.put(WeekView.KEY_CURRENT_YEAR, Integer.valueOf(mController.getCurrentDay().year));
		params.put(WeekView.KEY_CURRENT_MONTH, Integer.valueOf(mController.getCurrentDay().month));
		params.put(WeekView.KEY_CURRENT_DAY_OF_MONTH, Integer.valueOf(mController.getCurrentDay().dayOfMonth));

		weekView.reset();
		weekView.setParams(params);
		weekView.invalidate();
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
		DateTime dtStart = mFirstVisibleDay.toDateTime();
		DateTime dtDay = day.toDateTime();
		return Weeks.weeksBetween(dtStart, dtDay).getWeeks();
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
	
////====================================================================================
//// Positioning
////====================================================================================
	
	/**
	 * Calculates how many months the list will contain.
	 */
	private void calculateCount() {
		DateTime dtStart = mController.getStartDay().toDateTime();
		DateTime dtEnd = mController.getEndDay().toDateTime();
		int numWeeks = Weeks.weeksBetween(dtStart, dtEnd).getWeeks();
		mCount = numWeeks + 1;
	}

	/**
	 * Calculate the CalendarDay of the start of the week, based upon position.
	 * @param position
	 */
	private CalendarDay getWeekStartForPosition(int position) {
		mCalendar.set(Calendar.YEAR, mFirstVisibleDay.year);
		mCalendar.set(Calendar.MONTH, mFirstVisibleDay.month);
		mCalendar.set(Calendar.DAY_OF_MONTH, mFirstVisibleDay.dayOfMonth);
		mCalendar.add(Calendar.DAY_OF_YEAR, 7 * position);
		return CalendarDay.fromCalendar(mCalendar);
	}
	
	/**
	 * Calculate the CalendarDay of the end of the week, based upon position.
	 * @param position
	 */
	private CalendarDay getWeekEndForPosition(int position) {
		mCalendar.set(Calendar.YEAR, mFirstVisibleDay.year);
		mCalendar.set(Calendar.MONTH, mFirstVisibleDay.month);
		mCalendar.set(Calendar.DAY_OF_MONTH, mFirstVisibleDay.dayOfMonth);
		mCalendar.add(Calendar.DAY_OF_YEAR, 7 * position + 6);
		return CalendarDay.fromCalendar(mCalendar);
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
		WeekView weekView;
		if (convertView == null) {
			weekView = new WeekView(mContext);
			weekView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			weekView.setClickable(true);
			
			if (container instanceof OnDayClickListener) {
				weekView.setOnDayClickListener((OnDayClickListener) container);
			}
		} else {
			weekView = (WeekView) convertView;
		}
		updateView(position, weekView);
		return weekView;
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return mCount;
	}
	
////======================================================================================
//// OnCalendarControllerChangeListener
////======================================================================================

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarController.OnCalendarControllerChangeListener#onChange(me.jmhend.ui.calendar_viewer.CalendarController, java.lang.Object, java.lang.String)
	 */
	@Override
	public void onChange(CalendarController controller, Object obj, String tag) {
		if (CalendarController.FIRST_DAY_OF_WEEK.equals(tag)
				|| CalendarController.START_DAY.equals(tag)
				|| CalendarController.END_DAY.equals(tag)) {
			calculateCount();
			updateFromController(controller);
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
