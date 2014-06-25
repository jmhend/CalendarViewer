package me.jmhend.CalendarViewer;

import java.util.Calendar;

import me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

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
				
				DayView dayView = (DayView) DayViewPager.this.getViewAtPosition(position);
				if (dayView == null) {
					return;
				}
				mAdapter.setSelectedDay(CalendarDay.fromCalendar(mRecycle));
				
				if (mPageSelectedListener != null) {
					mPageSelectedListener.onPageSelected(DayViewPager.this, position);
				}
				
				int earliestY = dayView.getYForEarliestEvent();
				if (earliestY != -1) {
					scrollToEventAtY(dayView, earliestY);
				}
			}
		};
		setOnPageChangeListener(mPageChangeListener);
	}
	
	/**
	 * Scrolls the ViewPager's current DayView to show the Event at its y-position nicely.
	 */
	public void scrollCurrentViewToEventAtY() {
		DayView dayView = (DayView) this.getCurrentView();
		if (dayView != null) {
			int earliestY = dayView.getYForEarliestEvent();
			if (earliestY != -1) {
				scrollToEventAtY(dayView, earliestY);
			}
		}
	}
	
	/**
	 * Scrolls the DayView to show the Event at the y-position nicely.
	 * @param dayView
	 * @param y
	 */
	public void scrollToEventAtY(DayView dayView, int y) {
		final ScrollView scrollView = (ScrollView) dayView.getParent();
		int height = scrollView.getHeight();
		int offset = height / 4;
		int scrollToY = y - offset;
		if (scrollToY < 0) {
			scrollToY = 0;
		}
		scrollView.smoothScrollTo(0, scrollToY);
	}
	
	/**
	 * Signal all the CalendarViewer callbacks to fire.
	 */
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
