package me.jmhend.CalendarViewer;

import java.util.Calendar;

import org.joda.time.DateTime;
import org.joda.time.Days;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * PagerAdapter for displaying DayViews.
 * @author jmhend
 *
 */
public class DayPagerAdapter extends CalendarAdapter {

	private static final String TAG = DayPagerAdapter.class.getSimpleName();
	
////==================================================================================
//// Static constants.
////==================================================================================
	
////==================================================================================
//// Member variables.
////==================================================================================
	
	private Context mContext;
	private LayoutInflater mInflater;
	private CalendarController mController;
	private final CalendarModel mModel;
	private final Calendar mCalendar;
	private int mCount;
	
	
	
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
//// CalendarAdapter
////==================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.CalendarAdapter#updateView(int, me.jmhend.CalendarViewer.CalendarView)
	 */
	@Override
	public void updateView(int position, View view) {
		DayView dayView = (DayView) view;
		dayView.setAdapter(this);
		dayView.setModel(mModel);
		
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
	private long getDayStartForPosition(int position) {
		resetCalendar();
		mCalendar.add(Calendar.DAY_OF_YEAR, position);
		return mCalendar.getTimeInMillis();
	}
	
	/**
	 * @param position
	 * @return The day end time at position;
	 */
	private long getDayEndForPosition(int position) {
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

}
