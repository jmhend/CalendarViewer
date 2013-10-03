package me.jmhend.ui.calendar_viewer;

import android.graphics.Canvas;
import android.view.View;

/**
 * 
 * @author jmhend
 *
 */
public interface CalendarViewerDecorator {
	
	/**
	 * Indicates on what z-level the Decorator should be applied in the CalendarViewer.
	 *
	 * @author jmhend
	 */
	public static enum ApplyLevel {
		
		/**
		 * Decorator is applied below the CalendarViewer dates.
		 */
		BELOW,
		
		/**
		 * Decorator is applied in the same pass as the CalendarViewer dates.
		 */
		INLINE,
		
		/**
		 * Decorator is applied on top of the CalendarViewer dates.
		 */
		TOP
	}
	
	/**
	 * @return The ApplyLevel of when this Decorator will be applied.
	 * One of BELOW, WITHIN, TOP
	 */
	public ApplyLevel getApplyLevel();
	
	/**
	 * Applies the Decorator to the calendarView.
	 * 
	 * @param calendarView
	 * @param canvas calendarView's canvas.
	 */
	public void apply(View calendarView, Canvas canvas);
}
