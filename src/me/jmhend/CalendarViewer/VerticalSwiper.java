package me.jmhend.CalendarViewer;

import me.jmhend.CalendarViewer.CalendarViewer.Mode;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

public class VerticalSwiper implements OnTouchListener {
	
	private static final String TAG = VerticalSwiper.class.getSimpleName();
	
////========================================================================================
//// Static constants.
////========================================================================================
	
////========================================================================================
//// Member variables.
////========================================================================================
	
	private VelocityTracker mVelocityTracker = VelocityTracker.obtain();
	
	
	private int mBaseHeight = 0;
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
				mBaseHeight = mView.getHeight();
				mCalendarViewer.mMode = Mode.TRANSITION;
				Log.e(TAG, "DOWN at: " + mDownY);
				return false;
			}
			
			case MotionEvent.ACTION_MOVE: {
				if (!mAbleToSwipe) {
					return false;
				}
				
				float y = ev.getY();
				Log.i(TAG, "MOVE at: " + y);
				float absDeltaY = Math.abs(mDownY - y);
				
				if (!mSwiping) {
					if (absDeltaY > mSwipeSlop ) {
						Log.w(TAG, "Starting swipe.");
						mSwiping = true;
					} else {
						return false;
					}
				}
				
				float offsetY = y - mLastY;
				Log.d(TAG, "offset: " + offsetY);
				
				int newHeight = (int) (mView.getHeight() + offsetY);
				mCalendarViewer.setHeight(mView, newHeight);
				
//				RelativeLayout.LayoutParams lp =  (RelativeLayout.LayoutParams) mView.getLayoutParams();
//				lp.height += offsetY;
//				mView.setLayoutParams(lp);
//				mView.requestLayout();
				
				mLastY = y;
				
				return true;
			}
			
			case MotionEvent.ACTION_UP: {
				if (mSwiping) {
					mVelocityTracker.addMovement(ev);
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
