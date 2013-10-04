package me.jmhend.ui.calendar_viewer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Base class for Views that display Calendar days.
 * 
 * @author jmhend
 *
 */
public abstract class CalendarView extends View {

	private static final String TAG = CalendarView.class.getSimpleName();
	
////===================================================================================
//// Static constants.
////===================================================================================
	
////===================================================================================
//// Member variables.
////===================================================================================
	
////===================================================================================
//// Constructor
////===================================================================================
	
	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public CalendarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public CalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	/**
	 * @param context
	 */
	public CalendarView(Context context) {
		super(context);
		init();
	}
	
////===================================================================================
//// Init.
////===================================================================================
	
	/**
	 * Initialize resources, View state, etc.
	 */
	protected void init() {
		
	}

}
