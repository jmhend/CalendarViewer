package me.jmhend.ui.calendar_viewer;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import me.jmhend.ui.calendar_viewer.CalendarView.OnDayClickListener;

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
public class WeekPagerAdapter extends CalendarAdapter {
	
	private static final String TAG = WeekPagerAdapter.class.getSimpleName();
	
////======================================================================================
//// Static constants.
////======================================================================================
	
////======================================================================================
//// Member variables.
////======================================================================================
	
	private final Context mContext;
	private int mFirstDayOfWeek;
	private final Calendar mCalendar;
	private CalendarDay mStartDay;
	private CalendarDay mEndDay;
	private CalendarDay mFirstVisibleDay;
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
		mCalendar = Calendar.getInstance();
		mCalendar.set(Calendar.MILLISECOND, 0);
		mCalendar.set(Calendar.SECOND, 0);
		mCalendar.set(Calendar.MINUTE, 0);
		mCalendar.set(Calendar.HOUR, 0);
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
		mCalendar.setFirstDayOfWeek(mFirstDayOfWeek);
		mFirstVisibleDay = Utils.getWeekRangeForDay(mCalendar, mStartDay).weekStart;
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
	public void updateView(int position, CalendarView view) {
		WeekView weekView = (WeekView) view;
		Map<String, Integer> params = (Map<String, Integer>) weekView.getTag();
		if (params == null) {
			params = new HashMap<String, Integer>();
		}
		
		// Generate WeekView data.
		CalendarDay startDay = getWeekStartForPosition(position);
		CalendarDay endDay = getWeekEndForPosition(position);
		params.put(CalendarAdapter.KEY_POSITION, Integer.valueOf(position));
		params.put(WeekView.KEY_WEEK_START, Integer.valueOf(mFirstDayOfWeek));
		params.put(WeekView.KEY_START_YEAR, Integer.valueOf(startDay.year));
		params.put(WeekView.KEY_START_MONTH, Integer.valueOf(startDay.month));
		params.put(WeekView.KEY_START_DAY_OF_MONTH, Integer.valueOf(startDay.dayOfMonth));
		params.put(WeekView.KEY_END_YEAR, Integer.valueOf(endDay.year));
		params.put(WeekView.KEY_END_MONTH, Integer.valueOf(endDay.month));
		params.put(WeekView.KEY_END_DAY_OF_MONTH, Integer.valueOf(endDay.dayOfMonth));
		params.put(WeekView.KEY_SELECTED_YEAR, Integer.valueOf(mSelectedDay.year));
		params.put(WeekView.KEY_SELECTED_MONTH, Integer.valueOf(mSelectedDay.month));
		params.put(WeekView.KEY_SELECTED_DAY_OF_MONTH, Integer.valueOf(mSelectedDay.dayOfMonth));
		params.put(WeekView.KEY_CURRENT_YEAR, Integer.valueOf(mCurrentDay.year));
		params.put(WeekView.KEY_CURRENT_MONTH, Integer.valueOf(mCurrentDay.month));
		params.put(WeekView.KEY_CURRENT_DAY_OF_MONTH, Integer.valueOf(mCurrentDay.dayOfMonth));

		HeatDecorator dec;
		if (mDecoratorsMap.containsKey(Integer.valueOf(position))) {
			dec = mDecoratorsMap.get(Integer.valueOf(position));
		} else {
			dec = new HeatDecorator(startDay.year, startDay.month);
			mDecoratorsMap.put(Integer.valueOf(position), dec);
		}
		
		weekView.reset();
		weekView.clearDecorators();
		weekView.addDecorator(dec);
		weekView.setParams(params);
		weekView.invalidate();
	}

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarAdapter#getPositionForDay(me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public int getPositionForDay(CalendarDay day) {
		if (day.isBeforeDay(mStartDay) || day.isAfterDay(mEndDay)) {
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
		mSelectedDay = day;
	}
	
////====================================================================================
//// Positioning
////====================================================================================
	
	/**
	 * Calculates how many months the list will contain.
	 */
	private void calculateCount() {
		DateTime dtStart = mStartDay.toDateTime();
		DateTime dtEnd = mEndDay.toDateTime();
		int numWeeks = Weeks.weeksBetween(dtStart, dtEnd).getWeeks();
		mCount = numWeeks + 1;
	}

	/**
	 * Calculate the CalendarDay of the start of the week, based upon position.
	 * TODO: Cache these? Map<Position, CalendarDay>
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
	 * TODO: Cache these? Map<Position, CalendarDay>
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
}
