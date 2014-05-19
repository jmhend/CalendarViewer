package me.jmhend.CalendarViewer;

import java.util.ArrayList;
import java.util.List;

import me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay;
import android.util.Log;

/**
 * When the pimps in the crib ma, drop it like it's hot, drop it like it's hot.
 * @author jmhend
 */
public class CalendarController {

	private static final String TAG = CalendarController.class.getSimpleName();
	
////===============================================================================
//// Static constants.
////===============================================================================
	
	public static final String FIRST_DAY_OF_WEEK = "firstDayOfWeek";
	public static final String START_DAY = "startDay";
	public static final String END_DAY = "endDay";
	public static final String CURRENT_DAY = "currentDay";
	public static final String SELECTED_DAY = "selectedDay";
	public static final String FOCUSED_DAY = "focusedDay";
	
////===============================================================================
//// Member variables.
////===============================================================================
	
	private int mFirstDayOfWeek;
	private CalendarDay mStartDay;
	private CalendarDay mEndDay;
	private CalendarDay mCurrentDay;
	private CalendarDay mSelectedDay;
	private CalendarDay mFocusedDay;
	
	private List<OnCalendarControllerChangeListener> mListeners = new ArrayList<OnCalendarControllerChangeListener>();
	
////===============================================================================
//// Constructor.
////===============================================================================
	
	/**
	 */
	public CalendarController() { 
		mCurrentDay = CalendarDay.currentDay();
	}
	
	/**
	 * Initialize fields from the CalendarControllerConfig.
	 * @param config
	 */
	public CalendarController(CalendarControllerConfig config) {
		this();
		mFirstDayOfWeek = config.getFirstDayOfWeek();
		mStartDay = config.getStartDay();
		mEndDay = config.getEndDay();
		mSelectedDay = config.getSelectedDay();
		mFocusedDay = mSelectedDay;
	}
	
////===============================================================================
//// Getters/Setters
////===============================================================================
	
	public int getFirstDayOfWeek() {
		return mFirstDayOfWeek;
	}
	
	public void setFirstDayOfWeek(int day) {
		mFirstDayOfWeek = day;
		notifyListeners(day, FIRST_DAY_OF_WEEK);
	}
	
	public CalendarDay getStartDay() {
		return mStartDay;
	}
	
	public void setStartDay(CalendarDay startDay) {
		mStartDay = startDay;
		notifyListeners(startDay, START_DAY);
	}
	
	public CalendarDay getEndDay() {
		return mEndDay;
	}
	
	public void setEndDay(CalendarDay endDay) {
		mEndDay = endDay;
		notifyListeners(endDay, END_DAY);
	}
	
	public CalendarDay getCurrentDay() {
		return mCurrentDay;
	}
	
	public void setCurrentDay(CalendarDay currentDay) {
		mCurrentDay = currentDay;
		notifyListeners(currentDay, CURRENT_DAY);
	}
	
	public CalendarDay getSelectedDay() {
		return mSelectedDay;
	}
	
	public void setSelectedDay(CalendarDay selectedDay) {
		mSelectedDay = selectedDay;
		notifyListeners(selectedDay, SELECTED_DAY);
	}
	
	public CalendarDay getFocusedDay() {
		return mFocusedDay;
	}
	
	public void setFocusedDay(CalendarDay focusedDay) {
		Log.i(TAG, "FOCUS: " + focusedDay);
		mFocusedDay = focusedDay;
	}

////===============================================================================
//// OnCalendarControllerChangedListener
////===============================================================================
	
	/**
	 * Registers an OnCalendarControllerChangeListener.
	 * @param listener
	 */
	public void registerListener(OnCalendarControllerChangeListener listener) {
		mListeners.add(listener);
	}
	
	/**
	 * Unregisters an OnCalendarControllerChangeListener.
	 * @param listener
	 */
	public void unregisterListener(OnCalendarControllerChangeListener listener) {
		mListeners.remove(listener);
	}
	
	/**
	 * Notify each OnCalendarControllerChangeListener of a data change.
	 */
	private void notifyListeners(Object obj, String tag) {
		for (OnCalendarControllerChangeListener listener : mListeners) {
			listener.onChange(this, obj, tag);
		}
	}
	
	/**
	 * Listens for CalendarController changes.
	 * @author jmhend
	 *
	 */
	public static interface OnCalendarControllerChangeListener {
		
		/**
		 * Called when a CalendarController field changes.
		 * @param obj The new field Object.
		 * @param tag Tag describing field changed.
		 */
		public void onChange(CalendarController controller, Object obj, String tag);
	}
}
