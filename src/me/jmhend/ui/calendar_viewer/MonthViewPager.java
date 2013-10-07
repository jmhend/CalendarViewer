package me.jmhend.ui.calendar_viewer;

import java.util.HashMap;
import java.util.Map;

import me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * ViewPager that displays a collection of MonthViews.
 * @author jmhend
 */
public class MonthViewPager extends ViewPager implements OnDayClickListener {

	private static final String TAG = MonthViewPager.class.getSimpleName();
	
////=============================================================================
//// Static constants.
////=============================================================================
	
	private static final float WIDTH_THRESHOLD = 0.33f;
	private static final float MIN_MONTH_OPACITY = 0.2f;
	
////=============================================================================
//// Member variables
////=============================================================================
	
	private MonthPagerAdapter mAdapter;
	private OnPageChangeListener mPageChangeListener;
	private OnDayClickListener mDayClickListener;
	
////=============================================================================
//// Constructor.
////=============================================================================
	
	/**
	 * @param context
	 * @param attrs
	 */
	public MonthViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	/**
	 * @param context
	 */
	public MonthViewPager(Context context) {
		super(context);
		init();
	}
	
////=============================================================================
//// Init.
////=============================================================================
	
	/**
	 * Init.
	 */
	private void init() {
		mPageChangeListener = new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				makeFancy(position, positionOffset, positionOffsetPixels);
			}

			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				
			}
			
		};
		setOnPageChangeListener(mPageChangeListener);
	}
	
////=============================================================================
//// Getters/Setters
////=============================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.view.ViewPager#setAdapter(android.support.v4.view.PagerAdapter)
	 */
	@Override
	public void setAdapter(PagerAdapter adapter) {
		if (!(adapter instanceof MonthPagerAdapter)) {
			throw new IllegalArgumentException("PagerAdapter must be of type " 
						+ MonthPagerAdapter.class.getCanonicalName() 
						+ ", but is instead " 
						+ adapter.getClass().getCanonicalName());
		}
		mAdapter = (MonthPagerAdapter) adapter;
		super.setAdapter(adapter);
	}
	
	/**
	 * @param listener
	 */
	public void setOnDayClickListener(OnDayClickListener listener) {
		mDayClickListener = listener;
	}
	
////=============================================================================
//// Views
////=============================================================================
	
	/**
	 * Searches the children Views for the child whose adapter position is 'position'
	 * @param position
	 * @return The View for 'position', or null if it's been recycled/outOfRange
	 */
	public View getViewAtPosition(int position) {
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			int childPosition = getPositionFromTag(child);
			if (childPosition == position) {
				return child;
			}
		}
		return null;
	}
	
	/**
	 * If the view has a tag containing the position keyword, returns that position.
	 * @param view
	 * @return The position if found, else -1.
	 */
	@SuppressWarnings("unchecked")
	public int getPositionFromTag(View view) {
		if (view.getTag() == null) {
			return -1;
		}
		if (!(view.getTag() instanceof Map<?, ?>)) {
			return -1;
		}
		Map<String, Integer> params = (HashMap<String, Integer>) view.getTag();
		if (!params.containsKey(MonthPagerAdapter.KEY_POSITION)) {
			return -1;
		}
		return params.get(MonthPagerAdapter.KEY_POSITION).intValue();
	}
	
	/**
	 * Searches the children Views from the child that is currently displaying 'day'.
	 * @param day
	 * @return The View for 'day', or null if it's been recycled/outOfRange
	 */
	public View getViewForDay(CalendarDay day) {
		int position = mAdapter.getPositionForDay(day);
		if (position == -1) {
			return null;
		}
		return getViewAtPosition(position);
	}
	
////=============================================================================
//// Update
////=============================================================================
	
	/**
	 * Tell the underlying adapter to update the View at position.
	 * @param position
	 */
	public void updatePage(int position) {
		View child = getViewAtPosition(position);
		if (child == null) {
			return;
		}
		mAdapter.updatePage(position, (MonthView) child);
	}
	
	/**
	 * Tells the underlying adapter to update all visible Views.
	 */
	public void updateVisiblePages() {
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			MonthView child = (MonthView) getChildAt(i);
			int childPosition = getPositionFromTag(child);
			if (childPosition != -1) {
				mAdapter.updatePage(childPosition, child);
			}
		}
	}
	
////=============================================================================
//// Fanciness.
////=============================================================================
	
	/**
	 * Adjusts the children elements based on paging distance.
	 * @param position
	 * @param positionOffset
	 * @param positionOffsetPixels
	 */
	private void makeFancy(int position, float positionOffset, int positionOffsetPixels) {
		float primaryAlpha;
		float secondaryAlpha;
		
		if (positionOffset < WIDTH_THRESHOLD) {
			primaryAlpha = 1f;
			secondaryAlpha = MIN_MONTH_OPACITY;
		} else {
			primaryAlpha = 1f - (positionOffset - WIDTH_THRESHOLD) / ((1 - MIN_MONTH_OPACITY) - WIDTH_THRESHOLD);
			secondaryAlpha = Math.min(1f, (positionOffset - WIDTH_THRESHOLD) / (1 - WIDTH_THRESHOLD) + MIN_MONTH_OPACITY);
		}
		
		if (primaryAlpha < MIN_MONTH_OPACITY) {
			primaryAlpha = MIN_MONTH_OPACITY;
		}
		for (int i = 0; i < getChildCount(); i++) {
			final View child = getChildAt(i);
			final int childPos = getPositionFromTag(child);
			if (childPos == position) {
				child.setAlpha(primaryAlpha);
			} else {
				child.setAlpha(secondaryAlpha);
			}
		}
	}
	
	
////====================================================================================
//// OnDayClickListener
////====================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.MonthView.OnDayClickListener#onDayClick(me.jmhend.ui.calendar_viewer.MonthView, me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay)
	 */
	@Override
	public void onDayClick(View calendarView, CalendarDay day) {
		if (day != null) {
			if (!Utils.isDayCurrentOrFuture(day)) {
				return;
			}
			mAdapter.setSelectedDay(day);
			updateVisiblePages();
			if (mDayClickListener != null) {
				mDayClickListener.onDayClick(calendarView, day);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.MonthView.OnDayClickListener#onDayLongClick(me.jmhend.ui.calendar_viewer.MonthView, me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay)
	 */
	@Override
	public void onDayLongClick(View calendarView, CalendarDay day) {
		if (day != null) {
			if (!Utils.isDayCurrentOrFuture(day)) {
				return;
			}
			if (mDayClickListener != null) {
				mDayClickListener.onDayLongClick(calendarView, day);
			}
		}
	}
}
