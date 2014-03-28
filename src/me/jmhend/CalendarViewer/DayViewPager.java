package me.jmhend.CalendarViewer;

import java.util.Calendar;

import me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay;
import me.jmhend.CalendarViewer.CalendarViewer.Mode;
import android.content.Context;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * CalendarViewPager for DayViews.
 * @author jmhend
 *
 */
public class DayViewPager extends CalendarViewPager {
	
	private static final String TAG = DayViewPager.class.getSimpleName();
	
////========================================================================================
//// Member variables.
////========================================================================================
	
	private Calendar mRecycle = Calendar.getInstance();
	
////========================================================================================
//// Constructor.
////========================================================================================

	/**
	 * @param context
	 * @param attrs
	 */
	public DayViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**
	 * 
	 * @param context
	 */
	public DayViewPager(Context context) {
		super(context);
	}
	
////========================================================================================
//// Init
////========================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.CalendarViewPager#init()
	 */
	@Override
	protected void init() {
		mPageChangeListener = new OnPageChangeListener() {

			/*
			 * (non-Javadoc)
			 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrollStateChanged(int)
			 */
			@Override
			public void onPageScrollStateChanged(int state) { }

			/*
			 * (non-Javadoc)
			 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrolled(int, float, int)
			 */
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

			/*
			 * (non-Javadoc)
			 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageSelected(int)
			 */
			@Override
			public void onPageSelected(int position) {
				long dayStart = ((DayPagerAdapter) getAdapter()).getDayStartForPosition(position);
				mRecycle.setTimeInMillis(dayStart);
				
				View container = DayViewPager.this.getViewAtPosition(position);
				if (container == null) {
					return;
				}
				mAdapter.setSelectedDay(CalendarDay.fromCalendar(mRecycle));
				
				if (mPageSelectedListener != null) {
					mPageSelectedListener.onPageSelected(DayViewPager.this, position);
				}
			}
		};
		setOnPageChangeListener(mPageChangeListener);
	}
	
	public void fireCallbacksAtCurrentPosition() {
		int position = this.getCurrentItem();
		long dayStart = ((DayPagerAdapter) getAdapter()).getDayStartForPosition(position);
		mRecycle.setTimeInMillis(dayStart);
		
		View container = DayViewPager.this.getViewAtPosition(position);
		if (container == null) {
			return;
		}
		View dayView = container.findViewById(R.id.day);
		DayViewPager.this.onDayClick(dayView, CalendarDay.fromCalendar(mRecycle));
		
		Log.e(TAG, "Scrolling to: " + CalendarDay.fromCalendar(mRecycle));
		
		if (mPageSelectedListener != null) {
			mPageSelectedListener.onPageSelected(DayViewPager.this, position);
		}
	}
	
////========================================================================================
//// View
////========================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.CalendarViewPager#getCalendarChildAt(int)
	 */
	@Override
	protected View getCalendarChildAt(int position) {
		View view = getChildAt(position);
		return view.findViewById(R.id.day);
	}

}
