package me.jmhend.CalendarViewer;

import org.joda.time.DateTime;
import org.joda.time.Days;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
	private int mCount;
	
////==================================================================================
//// Constructor
////==================================================================================
	
	/**
	 * @param context
	 * @param controller
	 */
	public DayPagerAdapter(Context context, CalendarController controller) {
		mContext = context.getApplicationContext();
		mInflater = LayoutInflater.from(mContext);
		mController = controller;
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
	public void updateView(int position, CalendarView view) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.CalendarAdapter#getPositionForDay(me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public int getPositionForDay(CalendarDay day) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.CalendarAdapter#setSelectedDay(me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public void setSelectedDay(CalendarDay day) {
		// TODO Auto-generated method stub
		
	}

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.RecyclingPagerAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup container) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.day_view, container, false);
		}
//		CalendarView view = (CalendarView) convertView;
//		updateView(position, view);
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
		Log.e(TAG, "Count: " + mCount);
	}

}
