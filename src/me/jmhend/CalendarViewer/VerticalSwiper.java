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
	
	// Animation speed to dismiss the animate the View to it's final state.
	public static final int ANIMATE_DURATION = 240;
	
	// Percentage that the View needs to be swiped
	// to be considered a complete swipe action. (0 - 1)f;
	private static final float DISTANCE_PERCENT_THRESH = 0.35f;
	
	// Velocity (pixels/millisecond) threshold for registering a swipe,
	// even if the distance threshold isn't met.
	private static final float ANIMATE_VELOCITY_THRESH = 0.5f;
	
////========================================================================================
//// Member variables.
////========================================================================================
	
	private VelocityTracker mVelocityTracker = VelocityTracker.obtain();
	
	private float mDownY;
	private float mLastY;
	private boolean mSwipingDown;
	private boolean mAllowsSwipes = true;
	private boolean mAbleToSwipe = false;
	private boolean mSwiping = false;
	
	private int mSwipeSlop = -1;
	
	private final View mView;
	private Mode mInitialMode;
	private final CalendarViewer mCalendarViewer;
	
	private int mMinHeight;
	private int mMaxHeight;
	private int mWeekToMonthThreshHeight;
	private int mMonthToWeekThreshHeight;
	
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
		mMinHeight = viewer.mMinHeight;
		mMaxHeight = viewer.mMaxHeight;
		
		mWeekToMonthThreshHeight = (int) (DISTANCE_PERCENT_THRESH * ((float) (mMaxHeight - mMinHeight))) + mMinHeight;
		mMonthToWeekThreshHeight = (int) ((1f - DISTANCE_PERCENT_THRESH) * ((float) (mMaxHeight - mMinHeight))) + mMinHeight;
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
				float y = ev.getY();
				float viewHeight = mView.getHeight();
				if (ev.getY() >= mView.getHeight() || mCalendarViewer.getMode() == Mode.TRANSITION) {
					return false;
				}
				mVelocityTracker.addMovement(ev);
				mAbleToSwipe = true;
				mDownY = ev.getY();
				mLastY = mDownY;
				return false;
			}
			
			case MotionEvent.ACTION_MOVE: {
				if (!mAbleToSwipe) {
					return false;
				}
				
				float y = ev.getY();
				float absDeltaY = Math.abs(mDownY - y);
				
				if (!mSwiping) {
					if (absDeltaY > mSwipeSlop && mCalendarViewer.getMode() != Mode.TRANSITION) {
						mSwiping = true;
						mInitialMode = mCalendarViewer.getMode();
						mCalendarViewer.beginTransition();
					} else {
						return false;
					}
				}
				
				float offsetY = y - mLastY;
				mSwipingDown = y > mLastY;
				
				int newHeight = (int) (mView.getHeight() + offsetY);
				mCalendarViewer.setHeightFully(newHeight);
				mCalendarViewer.adjustViewsInTransition();
				
				mLastY = y;
				mVelocityTracker.addMovement(ev);
				return true;
			}
			
			case MotionEvent.ACTION_UP: {
				if (mSwiping) {
					mVelocityTracker.addMovement(ev);
					
					determineAnimation();
					reset();
					return true;
					
					
				} else {
					reset();
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
	
	private void determineAnimation() {
		mVelocityTracker.computeCurrentVelocity(1);
		
		float velocityY = Math.abs(mVelocityTracker.getYVelocity());
		boolean swipingDown = mSwipingDown;
		
		Mode endMode = null;
		
		// Fast flick.
		if (velocityY > ANIMATE_VELOCITY_THRESH) {
			endMode = swipingDown? Mode.MONTH : Mode.WEEK;
			
		} else {
			if (mInitialMode == Mode.WEEK) {
				endMode = (mView.getHeight() > mWeekToMonthThreshHeight)? Mode.MONTH : Mode.WEEK;
			} else if (mInitialMode == Mode.MONTH) {
				endMode = (mView.getHeight() > mMonthToWeekThreshHeight)? Mode.MONTH : Mode.WEEK;
			}
		}
		if (endMode == null) {
			throw new IllegalStateException("Mode is NULL.");
		}
		
		animate(endMode, mView.getHeight(), null);
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
		mCalendarViewer.animate(mView, endMode, duration, (int) startHeight, targetHeight);
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
		mAbleToSwipe = false;
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
