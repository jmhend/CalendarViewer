package me.jmhend.CalendarViewer;

/**
 * Event model that can provides fields on how it should be drawn.
 * @author jmhend
 *
 */
public interface Event {
	
	/**
	 * @return Start time in millis of the event.
	 */
	public long getDrawingStartTime();
	
	/**
	 * @return End time in millis of the event.
	 */
	public long getDrawingEndTime();
	
	/**
	 * @return True if the event is an all day event.
	 */
	public boolean isDrawingAllDay();
	
	/**
	 * @return The color to draw for the event.
	 */
	public int getDrawingColor();
	
	/**
	 * @return How many lines of text this event needs to draw.
	 */
	public int getTextLinesCount();
	
	/**
	 * @return Title.
	 */
	public String getDrawablingTitle();
	
	/**
	 * @return Location
	 */
	public String getDrawingLocation();
	

}
