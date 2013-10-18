package me.jmhend.CalendarViewer.list;

import me.jmhend.CalendarViewer.MonthView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * ListView that contains MonthView row cells.
 * @author jmhend
 */
public class MonthListView extends ListView implements AbsListView.OnScrollListener {
	
	private static final String TAG = MonthListView.class.getSimpleName();
	
////====================================================================================
//// Static constants.
////====================================================================================
	
	private static final float DEFAULT_FRICTION = 0.05f;
	private static final int SCROLLBACK_DURATION = 80;
	private static final float MIN_MONTH_OPACITY = 0.15f;
	
////====================================================================================
//// Member variables.
////====================================================================================
	
	private MonthListAdapter mMonthAdapter;
	
////====================================================================================
//// Constructor.
////====================================================================================

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public MonthListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public MonthListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	/**
	 * @param context
	 */
	public MonthListView(Context context) {
		super(context);
		init();
	}
	
////====================================================================================
//// Init
////====================================================================================
	
	/**
	 * Initialize.
	 */
	private void init() {
		setFriction(DEFAULT_FRICTION);
		setOnScrollListener(this);
	}

	
////====================================================================================
//// Getters/Setters
////====================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.ListView#setAdapter(android.widget.ListAdapter)
	 */
	@Override
	public void setAdapter(ListAdapter adapter) {
		if (!(adapter instanceof MonthListAdapter)) {
			throw new IllegalArgumentException("Adapter must be of type MonthListAdapter!");
		}
		mMonthAdapter = (MonthListAdapter) adapter;
		super.setAdapter(adapter);
	}
	
////====================================================================================
//// List
////====================================================================================
	
	/**
	 * @return Calculates the position to which the ListView should be scrolled.
	 */
	public int getScrollToPosition() {
		int childCount = getChildCount();
		if (childCount == 0) {
			return -1;
		}
		if (childCount == 1) {
			return 0;
		}
		
		int firstPos = getFirstVisiblePosition();
		View firstChild = getChildAt(0);
		
		// If the first child is scrolled past at least half, 
		// scroll to the next item. Else, scroll back to the first child.
		int top = firstChild.getTop();
		int height = firstChild.getHeight();
		boolean halfScrolled = Math.abs(top) >= (height / 2);
		
		int position = firstPos;
		if (halfScrolled) {
			position++;
		}
		// Sanity check.
		if (position >= mMonthAdapter.getCount()) {
			position = mMonthAdapter.getCount() - 1;
		}
		return position;
	}
	
	/**
	 * Scrolls to position.
	 * @param position
	 */
	public void postSetSelection(final int position) {
		post(new Runnable() {
			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				MonthListView.this.setSelection(position);
			}
		});
	}
	
	/**
	 * Smoothly scrolls to position.
	 * @param position
	 */
	public void postSmoothScrollTo(final int position) {
		post(new Runnable() {
			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				MonthListView.this.actuallySmoothScrollToPosition(position);
			}
		});
	}
	
	/**
	 * Smooth scrolls to the correct position.
	 * @param position
	 */
	public void actuallySmoothScrollToPosition(int position) {
		this.smoothScrollToPositionFromTop(position, 0, SCROLLBACK_DURATION);
	}
	
	
////====================================================================================
//// OnScrollListener
////====================================================================================

	/*
	 * (non-Javadoc)
	 * @see android.widget.AbsListView.OnScrollListener#onScrollStateChanged(android.widget.AbsListView, int)
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			int pos = getScrollToPosition();
			if (pos != -1) {
				postSmoothScrollTo(pos);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.AbsListView.OnScrollListener#onScroll(android.widget.AbsListView, int, int, int)
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (visibleItemCount == 0) {
			return;
		}
		
		// Fade MonthView Title/DayLabels as they're farther from the top of the View.
		for (int i = 0; i < view.getChildCount(); i++) {
			MonthView child = (MonthView) view.getChildAt(i);
			int top = child.getTop();
			int height = child.getHeight();
			int thresh = height / 2;
			
			// Fade out MonthViews that aren't in the most screen-dominate month.
			float percent;
			
			// Has fully faded out above the screen.
			if (top <= -thresh) {
				percent = MIN_MONTH_OPACITY;
				
			// Is fading out above the screen.
			} else if (top < 0 && top > -thresh) {
				percent = 1f - Math.abs(((float) top) / ((float) (-thresh)));
				if (percent < MIN_MONTH_OPACITY) {
					percent = MIN_MONTH_OPACITY;
				}
			// Has fully appeared on screen.
			} else if (top < thresh) {
				percent = 1f;
			// Is appearing on the screen.
			} else {
				percent = 1f - Math.abs(((float) top - thresh) / ((float) (height - thresh)));
				if (percent < MIN_MONTH_OPACITY) {
					percent = MIN_MONTH_OPACITY;
				}
			}
			child.setAlpha(percent);
		}
	}

}
