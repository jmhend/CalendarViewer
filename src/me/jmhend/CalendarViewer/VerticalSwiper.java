package me.jmhend.CalendarViewer;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class VerticalSwiper implements OnTouchListener {
	
	private static final String TAG = VerticalSwiper.class.getSimpleName();
	
////========================================================================================
//// Static constants.
////========================================================================================
	
////========================================================================================
//// Member variables.
////========================================================================================
	
	boolean isActive = false;
	
////========================================================================================
//// Constructor.
////========================================================================================
	
////========================================================================================
//// onTouch
////========================================================================================

	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			isActive = true;
			return true;
		case MotionEvent.ACTION_MOVE:
			if (isActive) {
				return true;
			} else {
				return false;
			}
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			isActive = false;
			return true;
		}
		Log.i(TAG, "onTouch");
		return false;
	}

}
