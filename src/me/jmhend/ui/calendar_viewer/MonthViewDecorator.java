package me.jmhend.ui.calendar_viewer;

import android.graphics.Canvas;

public interface MonthViewDecorator {
	
	/**
	 * Indicates on what z-level the Decorator should be applied in the MonthView.
	 *
	 * @author jmhend
	 */
	public static enum ApplyLevel {
		
		/**
		 * Decorator is applied below the MonthView dates.
		 */
		BELOW,
		
		/**
		 * Decorator is applied in the same pass as the MonthView dates.
		 */
		INLINE,
		
		/**
		 * Decorator is applied on top of the MonthView dates.
		 */
		TOP
	}
	
	/**
	 * @return The ApplyLevel of when this Decorator will be applied.
	 * One of BELOW, WITHIN, TOP
	 */
	public ApplyLevel getApplyLevel();
	
	/**
	 * Applies the Decorator to the monthView.
	 * 
	 * @param monthView
	 * @param canvas monthView's canvas.
	 */
	public void apply(MonthView monthView, Canvas canvas);
}
