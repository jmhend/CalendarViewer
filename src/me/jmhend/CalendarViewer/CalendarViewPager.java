package me.jmhend.CalendarViewer;

import java.util.HashMap;
import java.util.Map;

import me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay;
import me.jmhend.CalendarViewer.CalendarView.OnDayClickListener;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Base class for a ViewPager of calendar Views.
 * @author jmhend
 *
 */
public class CalendarViewPager extends ViewPager implements OnDayClickListener {
	
	private static final String TAG = CalendarViewPager.class.getSimpleName();
	
////=====================================================================================
//// OnPageSelectedListener
////=====================================================================================
	
	/**
	 * Callback interface for ViewPager page selections.
	 * @author jmhend
	 *
	 */
	public static interface OnPageSelectedListener {
		
		/**
		 * Called when a ViewPager's page is a selected.
		 * @param pager
		 * @param position
		 */
		public void onPageSelected(ViewPager pager, int position);
	}
	
////=====================================================================================
//// Static constants.
////=====================================================================================
	
	protected static final float WIDTH_THRESHOLD = 0.33f;
	protected static final float MIN_PAGE_OPACITY = 0.2f;
	
////=====================================================================================
//// Member variables.
////=====================================================================================
	
	protected CalendarAdapter mAdapter;
	protected OnPageChangeListener mPageChangeListener;
	protected OnPageSelectedListener mPageSelectedListener;
	protected OnDayClickListener mDayClickListener;
	
////=====================================================================================
//// Constructor
////=====================================================================================

	/**
	 * @param context
	 * @param attrs
	 */
	public CalendarViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	/**
	 * @param context
	 */
	public CalendarViewPager(Context context) {
		super(context);
		init();
	}
	
////=============================================================================
////Init.
////=============================================================================
	
	/**
	 * Initialize.
	 */
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
				if (mPageSelectedListener != null) {
					mPageSelectedListener.onPageSelected(CalendarViewPager.this, position);
				}
			}
		};
		setOnPageChangeListener(mPageChangeListener);
	}
	
////=============================================================================
//// Getters/Setters
////=============================================================================
	
	/**
	 * @return The title of the currently displayed CalendarView.
	 */
	public String getCurrentItemTitle() {
		View view = getViewAtPosition(getCurrentItem());
		if (view != null) {
			return ((CalendarView) view).getTitle();
		}
		return "";
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.view.ViewPager#setAdapter(android.support.v4.view.PagerAdapter)
	 */
	@Override
	public void setAdapter(PagerAdapter adapter) {
		if (!(adapter instanceof CalendarAdapter)) {
			throw new IllegalArgumentException("PagerAdapter must be of type " 
						+ CalendarAdapter.class.getCanonicalName() 
						+ ", but is instead " 
						+ adapter.getClass().getCanonicalName());
		}
		mAdapter = (CalendarAdapter) adapter;
		mAdapter.setViewPager(this);
		super.setAdapter(adapter);
	}
	
	/**
	 * @param listener
	 */
	public void setOnDayClickListener(OnDayClickListener listener) {
		mDayClickListener = listener;
	}
	
	/**
	 * @param listener
	 */
	public void setOnPageSelectedListener(OnPageSelectedListener listener) {
		mPageSelectedListener = listener;
	}
	
	/**
	 * Sets the current day of the ViewPager.
	 * @param day
	 */
	public void setCurrentDay(CalendarDay day, final boolean smooth) {
		final int position = mAdapter.getPositionForDay(day);
		if (position != -1) {
			post(new Runnable() {
				/*
				 * (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				@Override
				public void run() {
					setCurrentItem(position, smooth);
				}
			});
		}
	}
	
	/**
	 * Sets the current day of the ViewPager.
	 * @param day
	 */
	public void setCurrentDay(CalendarDay day) {
		setCurrentDay(day, true);
	}
	
////=============================================================================
//// Views
////=============================================================================
	
	/** 
	 * @return This ViewPager's currently displayed child View.
	 */
	public View getCurrentView() {
		int pos = getCurrentItem();
		return getViewAtPosition(pos);
	}
	
	/**
	 * Searches the children Views for the child whose adapter position is 'position'
	 * @param position
	 * @return The View for 'position', or null if it's been recycled/outOfRange
	 */
	public View getViewAtPosition(int position) {
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getCalendarChildAt(i);
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
		if (!params.containsKey(CalendarAdapter.KEY_POSITION)) {
			return -1;
		}
		return params.get(CalendarAdapter.KEY_POSITION).intValue();
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
		mAdapter.updateView(position, (CalendarView) child);
	}
	
	/**
	 * Tells the underlying adapter to update all visible Views.
	 */
	public void updateVisiblePages() {
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getCalendarChildAt(i);
			int childPosition = getPositionFromTag(child);
			if (childPosition != -1) {
				mAdapter.updateView(childPosition, child);
			}
		}
	}
	
	/**
	 * @param position
	 * @return The CalendarView-like View at position.
	 */
	protected View getCalendarChildAt(int position) {
		return getChildAt(position);
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
	private void fadeEdges(int position, float positionOffset, int positionOffsetPixels) {
		float primaryAlpha;
		float secondaryAlpha;
		
		if (positionOffset < WIDTH_THRESHOLD) {
			primaryAlpha = 1f;
			secondaryAlpha = MIN_PAGE_OPACITY;
		} else {
			primaryAlpha = 1f - (positionOffset - WIDTH_THRESHOLD) / ((1 - MIN_PAGE_OPACITY) - WIDTH_THRESHOLD);
			secondaryAlpha = Math.min(1f, (positionOffset - WIDTH_THRESHOLD) / (1 - WIDTH_THRESHOLD) + MIN_PAGE_OPACITY);
		}
		
		if (primaryAlpha < MIN_PAGE_OPACITY) {
			primaryAlpha = MIN_PAGE_OPACITY;
		}
		for (int i = 0; i < getChildCount(); i++) {
			final View child = getCalendarChildAt(i);
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
	 * @see me.jmhend.CalendarViewer.CalendarView.OnDayClickListener#
	 * onDayClick(me.jmhend.CalendarViewer.CalendarView, me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public void onDayClick(View calendarView, CalendarDay day) {
		if (day != null) {
			mAdapter.setSelectedDay(day);
			if (mDayClickListener != null) {
				mDayClickListener.onDayClick(calendarView, day);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.OnDayClickListener#onDayLongClick(android.view.View, me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public void onDayLongClick(View calendarView, CalendarDay day) {
		if (day != null) {
			if (mDayClickListener != null) {
				mDayClickListener.onDayLongClick(calendarView, day);
			}
		}
	}
}
