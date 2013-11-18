package me.jmhend.CalendarViewer;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import me.jmhend.CalendarViewer.CalendarController.OnCalendarControllerChangeListener;
import me.jmhend.CalendarViewer.DayView.OnEventClickListener;

import org.joda.time.DateTime;
import org.joda.time.Days;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * PagerAdapter for displaying DayViews.
 * @author jmhend
 *
 */
public class DayPagerAdapter extends CalendarAdapter implements OnCalendarControllerChangeListener {

	private static final String TAG = DayPagerAdapter.class.getSimpleName();
	
////==================================================================================
//// Member variables.
////==================================================================================
	
	private Context mContext;
	private LayoutInflater mInflater;
	private CalendarController mController;
	private final CalendarModel mModel;
	private final Calendar mCalendar;
	private int mCount;
	
	private OnEventClickListener mEventClickListener;
	
////==================================================================================
//// Constructor
////==================================================================================
	
	/**
	 * @param context
	 * @param controller
	 */
	public DayPagerAdapter(Context context, CalendarModel model, CalendarController controller) {
		mContext = context.getApplicationContext();
		mInflater = LayoutInflater.from(mContext);
		mController = controller;
		mCalendar = Calendar.getInstance();
		mModel = model;
		resetCalendar();
		calculateCount();
	}
	
////==================================================================================
//// Getters/Setters
////==================================================================================
	
	/**
	 * @param l
	 */
	public void setOnEventClickListener(OnEventClickListener l) {
		mEventClickListener = l;
	}
	
////==================================================================================
//// CalendarAdapter
////==================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.CalendarAdapter#updateView(int, me.jmhend.CalendarViewer.CalendarView)
	 */
	@Override
	public void updateView(int position, View view) {
		DayView dayView = (DayView) view;
		dayView.setModel(mModel);
		dayView.setOnEventClickListener(mEventClickListener);
		
		// Necessary to identify Views in CalendarViewPager.
		Map<String, Integer> params = (Map<String, Integer>) dayView.getTag();
		if (params == null) {
			params = new HashMap<String, Integer>();
		}
		params.put(CalendarAdapter.KEY_POSITION, Integer.valueOf(position));
		dayView.setTag(params);
		
		long start = getDayStartForPosition(position);
		long end = getDayEndForPosition(position);
		dayView.setDayBounds(start, end);
		dayView.invalidate();
	}

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.CalendarAdapter#getPositionForDay(me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public int getPositionForDay(CalendarDay day) {
		if (day.isBeforeDay(mController.getStartDay()) || day.isAfterDay(mController.getEndDay())) {
			return -1;
		}
		DateTime dtStart = mController.getStartDay().toDateTime();
		DateTime dtDay = day.toDateTime();
		int position = Days.daysBetween(dtStart, dtDay).getDays();
		return position;
	}

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.CalendarAdapter#setSelectedDay(me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public void setSelectedDay(CalendarDay day) {
		mController.setSelectedDay(day);
		updateViewPager();
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.RecyclingPagerAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup container) {
		DayView dayView;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.day_view, container, false);
		} 
		dayView = ((DayView) convertView.findViewById(R.id.day));
		updateView(position, dayView);
		return convertView;
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return mCount;
	}
	
////==================================================================================
//// Position
////==================================================================================
	
	/**
	 * Calculates how many days are supplied by this DayPagerAdapter.
	 */
	private void calculateCount() {
		DateTime dtStart = mController.getStartDay().toDateTime();
		DateTime dtEnd = mController.getEndDay().toDateTime();
		int numDays = Days.daysBetween(dtStart, dtEnd).getDays();
		mCount = numDays + 1;
	}
	
	/**
	 * @param position
	 * @return The day start time at position.
	 */
	public long getDayStartForPosition(int position) {
		resetCalendar();
		mCalendar.add(Calendar.DAY_OF_YEAR, position);
		return mCalendar.getTimeInMillis();
	}
	
	/**
	 * @param position
	 * @return The day end time at position;
	 */
	public long getDayEndForPosition(int position) {
		resetCalendar();
		mCalendar.add(Calendar.DAY_OF_YEAR, position);
		mCalendar.set(Calendar.SECOND, 59);
		mCalendar.set(Calendar.MINUTE, 59);
		mCalendar.set(Calendar.HOUR_OF_DAY, 23);
		return mCalendar.getTimeInMillis();
	}
	
	/**
	 * Resets mCalendar to be the starting day.
	 */
	private void resetCalendar() {
		mController.getStartDay().fillCalendar(mCalendar);
	}
	
	
////====================================================================================
//// OnCalendarControllerChangeListener
////====================================================================================

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
