package me.jmhend.CalendarViewer;

import me.jmhend.CalendarViewer.CalendarViewer.Mode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;

public class VerticalSwiper implements OnTouchListener {
	
	private static final String TAG = VerticalSwiper.class.getSimpleName();
	
////========================================================================================
//// Static constants.
////========================================================================================
	
////========================================================================================
//// Member variables.
////========================================================================================
	
	private VelocityTracker mVelocityTracker = VelocityTracker.obtain();
	
	private float mDownY;
	private float mLastY;
	private boolean mAllowsSwipes = true;
	private boolean mAbleToSwipe = true;
	private boolean mSwiping = false;
	
	private int mSwipeSlop = -1;
	
	private final View mView;
	private final CalendarViewer mCalendarViewer;
	
////========================================================================================
//// Constructor.
////========================================================================================
	
	/**
	 * @param context
	 */
	public VerticalSwiper(CalendarViewer viewer, View mutableView) {
		mSwipeSlop = ViewConfiguration.get(mutableView.getContext()).getScaledTouchSlop();
		mView = mutableView;
		mCalendarViewer = viewer;
	}
	
////========================================================================================
//// onTouch
////========================================================================================

	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View v, MotionEvent ev) {
		boolean handled = handleTouch(v, ev);
		printMotionEvent(ev, handled);
		return handled;
	}
	
	/**
	 * @return Handles the onTouch MotionEvents.
	 */
	private boolean handleTouch(final View v, final MotionEvent ev) {
		if (!mAllowsSwipes) {
			return false;
		}
		
		switch (ev.getActionMasked()) {
		
			case MotionEvent.ACTION_DOWN: {
				mVelocityTracker.addMovement(ev);
				mAbleToSwipe = true;
				mDownY = ev.getY();
				mLastY = mDownY;
				mCalendarViewer.mMode = Mode.TRANSITION;
				return false;
			}
			
			case MotionEvent.ACTION_MOVE: {
				if (!mAbleToSwipe) {
					return false;
				}
				
				float y = ev.getY();
				float absDeltaY = Math.abs(mDownY - y);
				
				if (!mSwiping) {
					if (absDeltaY > mSwipeSlop ) {
						mSwiping = true;
					} else {
						return false;
					}
				}
				
				float offsetY = y - mLastY;
				
				int newHeight = (int) (mView.getHeight() + offsetY);
				mCalendarViewer.setHeight(mView, newHeight);
				
				mLastY = y;
				
				return true;
			}
			
			case MotionEvent.ACTION_UP: {
				if (mSwiping) {
					mVelocityTracker.addMovement(ev);
					
					animate(Mode.MONTH, mView.getHeight(), null);
					reset();
					return true;
				} else {
					return false;
				}
			}
			
			case MotionEvent.ACTION_CANCEL: {
				reset();
				return false;
			}
			default: 
				return false;
		}
	}
	
	/**
	 * Animates the CalendarView closed.
	 * @param endMode
	 * @param startHeight
	 * @param endAction
	 */
	private void animate(final Mode endMode, final float startHeight, final Runnable endAction) {
		final float ratio = 1f - ((startHeight - mCalendarViewer.getWeekHeight()) / (mCalendarViewer.getMonthHeight() - mCalendarViewer.getWeekHeight()));
		final long duration = (long) (ratio * CalendarViewer.TRANSITION_DURATION);
		final int targetHeight = mCalendarViewer.getHeightForMode(endMode);
		
		Log.i(TAG, ratio + ", " + duration);
		
		mCalendarViewer.animate(mView, Mode.MONTH, duration, (int) startHeight, targetHeight);
	}

////========================================================================================
//// State.
////========================================================================================
	
	/**
	 * Resets the swiping state to being non-interacted.
	 */
	private void reset() {
		mVelocityTracker.clear();
		mSwiping = false;
		mAbleToSwipe = true;
		mCalendarViewer.mMode = Mode.MONTH;
	}
	
////========================================================================================
//// DEBUG
////========================================================================================
	
	/**
	 * Prints the MotionEvent and whether or not it was handled.
	 * @param ev
	 * @param handled
	 */
	private void printMotionEvent(final MotionEvent ev, final boolean handled) {
//		String message;
//		switch (ev.getActionMasked()) {
//		case MotionEvent.ACTION_DOWN:
//			message = "DOWN";
//			break;
//		case MotionEvent.ACTION_MOVE:
//			message = "MOVE";
//			break;
//		case MotionEvent.ACTION_UP:
//			message = "UP";
//			break;
//		case MotionEvent.ACTION_CANCEL:
//			message = "CANCEL";
//			break;
//		default:
//			return;
//		}
//		Log.i(TAG, message + "\t" + handled);
	}

}
