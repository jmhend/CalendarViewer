package me.jmhend.CalendarViewer;

import java.util.List;


/**
 * Model to query against for Calendar and Event data.
 * @author jmhend
 */
public interface CalendarModel {
	
	public List<? extends Event> getEvents();
	
	public List<? extends Event> getEventsOnDay(long dayStart);
	
	public int getHeat(long dayStart);
	
	public void registerObserver(CalendarModelObserver observer);
	
	public void unregisterObserver(CalendarModelObserver observer);
	
	/**
	 * Observes for changes in the CalendarModel.
	 * @author jmhend
	 */
	public static interface CalendarModelObserver {
		
		/**
		 * Called when the CalendarModel changes.
		 */
		public void onCalendarChanged();
	}

}
