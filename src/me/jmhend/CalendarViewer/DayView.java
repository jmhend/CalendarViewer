package me.jmhend.CalendarViewer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;

/**
 * A View displaying the Events of a day.
 * 
 * @author jmhend
 *
 */
public class DayView extends View {
	
	private static final String TAG = DayView.class.getSimpleName();
	
////============================================================================
//// Static constants.
////============================================================================
	
	public static final int CLICK_DISPLAY_DURATION = 50;
	public static final int DEFAULT_COLOR = 0xFF2691C8;
	
////============================================================================
//// Member variables.
////============================================================================
	
	private Map<Event, Rect> mRectMap = new HashMap<Event, Rect>();
	private List<Event> mDrawableEvents = new ArrayList<Event>();
	private List<Event> mAllDayEvents = new ArrayList<Event>();
	private Event mPressedEvent;
	
	private CalendarModel mModel;
	
	private int mWidth;
	private int mHourWidth;
	private int mHourHeight;
	private int mLineHeight;
	private int mPaddingTop;
	private int mMinEventHeight;
	private int mEventTitleHeight;
	private int mEventLocationHeight;
	private int mEventPadding;
	private int mEventMargin;
	private int mEventBorderWidth;
	
	private int mTapDelay;
	private boolean mCanSelect = true;
	private long mTouchDown;
	
	private int mHourTextSize;
	private int mAmPmTextSize;
	private int mTitleTextSize;
	private int mOwnerLocationTextSize;
	
	private Paint mLinePaint;
	private Paint mHourPaint;
	private Paint mAmPmPaint;
	private Paint mEventPaint;
	private Paint mEventTitlePaint;
	private Paint mEventOwnerLocationPaint;
	private Paint mCurrentTimePaint;
	
	private long mDayStart;
	private long mDayEnd;
	private Calendar mCalendar;
	
	private int mNonAllDayCount = 0;
	private int mAllDayCount = 0;
	
	private int mFirstEventY = -1;
	
	private OnEventClickListener mEventClickListener;
	
////============================================================================
//// OnEventClick
////============================================================================
	
	/**
	 * Listens for when Events are clicked in a DayView.
	 * @author jmhend
	 *
	 */
	public static interface OnEventClickListener {
		/**
		 * Called when a DayView Event is clicked.
		 * @param view
		 * @param event
		 */
		public void onEventClick(DayView view, Event event);
	}
	
////============================================================================
//// Runnables
////============================================================================
	
	/**
	 * Handles a delayed click event.
	 * @author jmhend
	 */
    private class ClickRunnable implements Runnable {

    	public ClickRunnable(Event e) {
    		mEvent = e;
    	}
    	
    	private Event mEvent;
    	/*
    	 * (non-Javadoc)
    	 * @see java.lang.Runnable#run()
    	 */
		@Override
		public void run() {
			if (mCanSelect) {
				mPressedEvent = mEvent;
				invalidate();
			}
		}
    	
    }
    
    /**
     * Called when an item has clicked and needs to be cleared.
     * @author jmhend
     *
     */
    private class ClearRunnable implements Runnable {
    	
    	public ClearRunnable(Event e) {
    		mEvent = e;
    	}
    	
    	private Event mEvent;

    	/*
    	 * (non-Javadoc)
    	 * @see java.lang.Runnable#run()
    	 */
		@Override
		public void run() {
			if (mPressedEvent == mEvent) {
				onEventClick(mEvent);
			}
			mPressedEvent = null;
			invalidate();
		}
    	
    }
	
	
////============================================================================
//// Constructor.
////============================================================================

	
	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public DayView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public DayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	/**
	 * @param context
	 */
	public DayView(Context context) {
		super(context);
		init();
	}
	
////============================================================================
//// Initialization
////============================================================================
	
	/**
	 * Initttt.
	 */
	private void init() {
		Resources r = getContext().getResources();
		
		mPaddingTop = r.getDimensionPixelSize(R.dimen.dayview_padding_top);
		mHourWidth = r.getDimensionPixelSize(R.dimen.dayview_hour_label_width);
		mHourHeight = r.getDimensionPixelSize(R.dimen.dayview_hour_height);
		mLineHeight = r.getDimensionPixelSize(R.dimen.dayview_line_height);
		mEventTitleHeight = r.getDimensionPixelSize(R.dimen.dayview_event_title_height);
		mEventLocationHeight = r.getDimensionPixelSize(R.dimen.dayview_event_location_height);
		mEventPadding = r.getDimensionPixelSize(R.dimen.dayview_event_padding);
		mEventMargin = r.getDimensionPixelSize(R.dimen.dayview_event_margin);
		mEventBorderWidth = r.getDimensionPixelSize(R.dimen.dayview_event_border_width);
		mHourTextSize = r.getDimensionPixelSize(R.dimen.dayview_hour_label_size);
		mTitleTextSize = r.getDimensionPixelSize(R.dimen.dayview_event_title_size);
		mOwnerLocationTextSize = r.getDimensionPixelSize(R.dimen.dayview_event_location_size);
		mAmPmTextSize = (int) (mHourTextSize * 0.8f);
		int hourColor = r.getColor(R.color.hour_color);
		int amPmColor = r.getColor(R.color.am_pm_color);
		
		mLinePaint = new Paint();
		mLinePaint.setAntiAlias(true);
		mLinePaint.setColor(0xFFCCCCCC);
		mLinePaint.setStyle(Style.FILL);
		
		mCurrentTimePaint = new Paint();
		mCurrentTimePaint.setAntiAlias(true);
		mCurrentTimePaint.setColor(0xFFFF0000);
		mCurrentTimePaint.setStyle(Style.FILL);
		
		mHourPaint = new Paint();
		mHourPaint.setAntiAlias(true);
		mHourPaint.setTextSize(mHourTextSize);
		mHourPaint.setTypeface(Typeface.DEFAULT);
		mHourPaint.setTextAlign(Paint.Align.RIGHT);
		mHourPaint.setColor(hourColor);
		
		mAmPmPaint = new Paint();
		mAmPmPaint.setAntiAlias(true);
		mAmPmPaint.setTextSize(mAmPmTextSize);
		mAmPmPaint.setTypeface(Typeface.DEFAULT);
		mAmPmPaint.setTextAlign(Paint.Align.RIGHT);
		mAmPmPaint.setColor(amPmColor);
		
		mEventPaint = new Paint();
		mEventPaint.setAntiAlias(true);
		mEventPaint.setColor(0x660088CC);
		mEventPaint.setStyle(Style.FILL);
		
		mEventTitlePaint = new Paint();
		mEventTitlePaint.setAntiAlias(true);
		mEventTitlePaint.setColor(0xCCFFFFFF);
		mEventTitlePaint.setTextAlign(Paint.Align.LEFT);
		mEventTitlePaint.setStyle(Style.FILL);
		mEventTitlePaint.setTextSize(mTitleTextSize);
		mEventTitlePaint.setTypeface(Typeface.DEFAULT_BOLD);
		
		mEventOwnerLocationPaint = new Paint();
		mEventOwnerLocationPaint.setAntiAlias(true);
		mEventOwnerLocationPaint.setColor(0xAAFFFFFF);
		mEventOwnerLocationPaint.setTextAlign(Paint.Align.LEFT);
		mEventOwnerLocationPaint.setStyle(Style.FILL);
		mEventOwnerLocationPaint.setTextSize(mOwnerLocationTextSize);
		mEventOwnerLocationPaint.setTypeface(Typeface.DEFAULT);
		
		mCalendar = Calendar.getInstance();
		
		// Min Height is the height for a half hour.
		int y = this.getYForTime(1383912000000L);
		int y2 = this.getYForTime(1383913800000L);
		mMinEventHeight = y2 - y;
		
		mTapDelay = ViewConfiguration.getTapTimeout();
	}
	
////============================================================================
//// Getters/Setters
////============================================================================

	/**
	 * @param model
	 */
	public void setModel(CalendarModel model) {
		mModel = model;
	}
	
	/**
	 * Sets the time bounds of this DayView's day.
	 * @param dayStart
	 * @param dayEnd
	 */
	public void setDayBounds(long dayStart, long dayEnd) {
		mDayStart = dayStart;
		mDayEnd = dayEnd;
		calculateEventRects();
	}
	
	/**
	 * @return The UNIX start time of this DayView's day.
	 */ 
	public long getDayStart() {
		return mDayStart;
	}
	
	/**
	 * @return The UNIX end time of this DayView's day.
	 */
	public long getDayEnd() {
		return mDayEnd;
	}
	
	/**
	 * @param listener
	 */
	public void setOnEventClickListener(OnEventClickListener listener) {
		mEventClickListener = listener;
	}
	
	/**
	 * @return Events to go into the All Day box at the top of the View.
	 */
	public List<Event> getAllDayEvents() {
		return mAllDayEvents;
	}
	
	/**
	 * @param allDayEvents Events to go into the All Day box at the top of the View.
	 */
	public void setAllDayEvents(List<Event> allDayEvents) {
		mAllDayEvents = allDayEvents;
		setAllDayCount(mAllDayEvents.size());
	}
	
	/**
	 * @param allDayCount
	 */
	public void setAllDayCount(int allDayCount) {
		mAllDayCount = allDayCount;
	}
	
////============================================================================
//// Measure
////============================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onSizeChanged(int, int, int, int)
	 */
	@Override
	protected void onSizeChanged(int width, int height, int oldw, int oldh) {
		mWidth = width;
		calculateEventRects();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int height = (int) ((mHourHeight * 24) + mPaddingTop);
		setMeasuredDimension(width, height);
	}
	
	/**
	 * Sets the top padding of the DayView.
	 * @param topPadding
	 */
	public void setTopPadding(int topPadding) {
		mPaddingTop = topPadding;
	}
////============================================================================
//// Draw
////============================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	public void onDraw(Canvas canvas) {
		drawHoursAndLines(canvas);
		drawEventRects(canvas);
		drawCurrentTimeLine(canvas);
	}
	
	/**
	 * Draws the Hours and hour lines behind the Events.
	 * @param canvas
	 */
	private void drawHoursAndLines(Canvas canvas) {
		boolean is24Hour = DateFormat.is24HourFormat(getContext());
		
		float hourXOffset = is24Hour? (1.75f * mEventPadding) : mAmPmPaint.measureText("am");
		
		for (int i = 0; i < 24; i++) {
			float y = i * mHourHeight + mPaddingTop;
			float xMostPos = mHourWidth - (1.75f * mEventPadding);
			
			// Line
			canvas.drawLine(mHourWidth, y, mWidth, y + mLineHeight, mLinePaint);
			
			// "am"/"pm"
			if (!is24Hour) {
				float amPmX = xMostPos;
				canvas.drawText(getAmPmAtLinePosition(i), amPmX, y + mAmPmTextSize / 3, mAmPmPaint);
			}
			
			// Hour
			String hour = getHourAtLinePosition(i, is24Hour);
			canvas.drawText(hour, "Noon".equals(hour)? xMostPos : xMostPos - (1.3f * hourXOffset), y + mHourTextSize / 3, mHourPaint);
		}
	}
	
	/**
	 * Draws the line representing the current time.
	 * @param canvas
	 */
	private void drawCurrentTimeLine(Canvas canvas) {
		if (!isCurrentDay()) {
			return;
		}
		int y = getYForTime(System.currentTimeMillis());
		canvas.drawRect(mHourWidth, y, mWidth, y + mLineHeight, mCurrentTimePaint);
		canvas.drawCircle(mHourWidth + mEventBorderWidth / 2, y + mLineHeight / 2, 8, mCurrentTimePaint);
	}
	
	/**
	 * Draws each event Rect to the canvas.
	 */
	private void drawEventRects(Canvas canvas) {
		List<Event> events = getEvents();

		for (Event event : events) {
			Rect rect = mRectMap.get(event);
			if (rect != null) {
				
				// Draw transluscent background.
				int color = event.getDrawingColor();
				if (color == 0) {
					color = DEFAULT_COLOR;
				}
				if (event == mPressedEvent) {
					color = setAlpha(1.0f, color);
				} else {
					color = setAlpha(0.6f, color);
				}
				mEventPaint.setColor(color);
				canvas.drawRect(rect, mEventPaint);
				
				// Draw border
				final int borderRight = rect.left + mEventBorderWidth;
				color = setAlpha(.9f, color);
				mEventPaint.setColor(color);
				canvas.drawRect(rect.left, rect.top, borderRight, rect.bottom, mEventPaint);
				
				int widthBounds = rect.right - rect.left - 2 * mEventPadding;
				// Draw Title
				String title = clipText(event.getDrawingTitle(), mEventTitlePaint, widthBounds);
				canvas.drawText(title, borderRight + mEventPadding, rect.top + mTitleTextSize + mEventPadding, mEventTitlePaint);
				
				// Draw Owner or Location.
				String owner = event.getDrawingOwner();
				if (!TextUtils.isEmpty(owner)) {
					String ownerClipped = clipText(event.getDrawingOwner(), mEventOwnerLocationPaint, widthBounds);
					canvas.drawText(ownerClipped, borderRight + mEventPadding, rect.top + (2 * mOwnerLocationTextSize) + mEventPadding, mEventOwnerLocationPaint);
				} else if (!TextUtils.isEmpty(event.getDrawingLocation())) {
					String locationClipped = clipText(event.getDrawingLocation(), mEventOwnerLocationPaint, widthBounds);
					canvas.drawText(locationClipped, borderRight + mEventPadding, rect.top + (2 * mTitleTextSize) + mEventPadding, mEventOwnerLocationPaint);
				}
			}
		}
	}
	
	/**
	 * Clips the text and adds an ellipses if it requires more width than maxWidth;
	 * @param text
	 * @param p
	 * @param maxWidth
	 * @return
	 */
	private static String clipText(String text, Paint p, int maxWidth) {
		if (text == null) {
			return "";
		}
		int breakpoint = p.breakText(text, true, maxWidth, null);
		if (text.length() <= breakpoint) {
			return text;
		}
		String clipped;
		if (breakpoint == 3) {
			clipped = "...";
		} else if (breakpoint == 2) {
			clipped = "..";
		} else if (breakpoint == 1) {
			clipped = ".";
		} else if (breakpoint == 0) {
			clipped = "";
		} else if (breakpoint < text.length()) {
			clipped = text.substring(0, breakpoint - 3).concat("...");
		} else {
			clipped = text;
		}
		return clipped;
	}
	
	/**
	 * @param position
	 * @return The hour label at the line position;
	 */
	private String getHourAtLinePosition(int position, boolean is24Hour) {
		if (is24Hour) {
			if (position < 10) {
				return "0" + String.valueOf(position);
			} else {
				return String.valueOf(position);
			}
		}
		
		if (position == 0) {
			return "12";
		}
		if (position < 12) {
			return String.valueOf(position);
		}
		if (position == 12) {
			return "Noon";
		}
		return String.valueOf(position - 12);
	}
	
	/**
	 * @param position
	 * @return The AM or PM label at the line position;
	 */
	private String getAmPmAtLinePosition(int position) {
		if (position == 12) {
			return "";
		}
		return (position < 12)? "am" : "pm";
	}
	
////============================================================================
//// Touch
////============================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		int action = e.getActionMasked();
		if (action == MotionEvent.ACTION_DOWN) {
			Event event = eventAtPosition((int) e.getX(), (int) e.getY());
			if (event != null) {
				mCanSelect = true;
				mTouchDown = System.currentTimeMillis();
				postDelayed(new ClickRunnable(event), mTapDelay);
				return true;
			}
			return false;
		}
		if (action == MotionEvent.ACTION_CANCEL) {
			mPressedEvent = null;
			mCanSelect = false;
			invalidate();
			return true;
		}
		if (action == MotionEvent.ACTION_UP) {
			Event event = eventAtPosition((int) e.getX(), (int) e.getY());
			if (event != null) {
				long clearDelay = (CLICK_DISPLAY_DURATION + mTapDelay) - (System.currentTimeMillis() - mTouchDown);
				if (clearDelay > 0) {
					postDelayed(new ClearRunnable(event), clearDelay);
				} else {
					post(new ClearRunnable(event));
				}
				return true;
			}	
		}
		return false;
	}
	
	/**
	 * Called when an Event is clicked.
	 * @param e
	 */
	public void onEventClick(Event e) {
		if (mEventClickListener != null) {
			mEventClickListener.onEventClick(this, e);
		}
	}
	
	/**
	 * Finds the highest z-position EventDrawable at (x, y)
	 * @param x
	 * @param y
	 * @return
	 */
	public Event eventAtPosition(int x, int y) {
		List<Event> events = getEvents();
		int size = events.size();
		
		// Reverse iterate so that events drawn later (higher z-position)
		// are found first.
		for (int i = size - 1; i >= 0; i--) {
			Event e = events.get(i);
			Rect r = mRectMap.get(e);
			if (r != null) {
				if (r.contains(x, y)) {
					return e;
				}
			}
		}
		return null;
	}
	
	
////============================================================================
//// Event positioning.
////============================================================================
	
	/**
	 * @return The EventDrawables to draw on this DayView.
	 */
	@SuppressWarnings("unchecked")
	private List<Event> getEvents() {
		mDrawableEvents.clear();
		if (mModel == null) {
			return mDrawableEvents;
		}
		List<Event> allEvents = (List<Event>) mModel.getEventsOnDay(mDayStart);
		
		// Filter out Events not to be drawn.
		for (Event e : allEvents) {
			if (willDrawEvent(e)) {
				mDrawableEvents.add(e);
			}
		}
		return mDrawableEvents;
	}
	
	/**
	 * Calculates the positiong of EventDrawables within this DayView.
	 */
	protected void calculateEventRects() {
		mRectMap.clear();
		List<Event> events = getEvents();
		int i = 0;
		int size = events.size();
		int minY = -1;
		
		List<Rect> groupedRects = new ArrayList<Rect>();
		
		while (i < size) {
			Event event = events.get(i);
			
			int topY = 0;
			int bottomY = 0;
			long startTime = event.getDrawingStartTime();
			long endTime = event.getDrawingEndTime();
			
			// Event started before this day.
			if (startTime < mDayStart) {
				topY = 0;
			// Event started this day;
			} else {
				topY = getYForTime(startTime) + 1;
			}
			
			// Event ends after this day.
			if (endTime > mDayEnd) {
				bottomY = (int) ((mHourHeight * 24) + mPaddingTop);
			// Event ends this day.
			} else {
				bottomY = getYForTime(endTime) - 1;
			}
			
			int textBottom = topY + getTextHeight(event);
			
			// minY hasn't been set yet.
			if (minY == -1) {
				minY = topY;
			}
			
			// New collision group; layout previous group.
			if (topY > minY) {
				layoutGroupRects(groupedRects);
				groupedRects.clear();
			}
			
			if (bottomY - topY < mMinEventHeight) {
				bottomY = topY + mMinEventHeight;
			}
			
			Rect r = new Rect();
			r.top = topY;
			r.bottom = bottomY;
			groupedRects.add(r);
			mRectMap.put(event, r);
			
			// End of events.
			if (i == (size - 1)) {
				layoutGroupRects(groupedRects);
				groupedRects.clear();
			}
			
			minY = Math.max(minY, textBottom);
			i++;
		}
		
		mFirstEventY = calculateYForEarliestEvent();
		mNonAllDayCount = events.size();
		updateEventCountView();
	}
	
	/**
	 * Lays out a collision group of event Rects.
	 * @param groupedRects
	 */
	private void layoutGroupRects(List<Rect> groupedRects) {
		int collisionCount = groupedRects.size();
		int width = (mWidth - mHourWidth) / collisionCount;
		
		// Calculate positions for the collision group's events.
		for (int j = 0; j < collisionCount; j++) {
			int startX = mHourWidth + j * width;
			int endX = startX + width - mEventMargin;
			Rect er = groupedRects.get(j);
			er.left = startX;
			er.right = endX;
		}
	}
	
	/**
	 * @return The Y position of the earliest occuring Event.
	 */
	private int calculateYForEarliestEvent() {
		if (isCurrentDay()) {
			return getYForTime(System.currentTimeMillis());
		}
		int y = getHeight() + 1;
		for (Entry<Event, Rect>  entry: mRectMap.entrySet()) {
			Rect rect = entry.getValue();
			if (rect.top < y) {
				y = rect.top;
			}
		}
		if (y == getHeight() + 1) {
			y = -1;
		}
		return y;
	}
	
	/**
	 * @return The Y position of the earliest occuring Event.
	 */
	public int getYForEarliestEvent() {
		return mFirstEventY;
	}
	
	/**
	 * @return The y position in this DayView that correlates to the 'time'.
	 */
	private int getYForTime(long time) {
		mCalendar.setTimeInMillis(time);
		int hourY = mCalendar.get(Calendar.HOUR_OF_DAY) * mHourHeight + mPaddingTop;
		float offsetPercent = ((float) mCalendar.get(Calendar.MINUTE) / (float) 60f);
		int minOffset = (int) (offsetPercent * mHourHeight);
		return (int) (hourY + minOffset);
	}
	
	/**
 	 * @return The height needed to display the EventDrawable's text.
	 */
	private int getTextHeight(Event e) {
		if (e.getTextLinesCount() == 2) {
			return mEventTitleHeight + mEventLocationHeight;
		}
		return mEventTitleHeight;
	}
	
	/**
	 * @return True if the EventDrawable will be drawn in this DayView, false otherwise.
	 */
	private boolean willDrawEvent(Event e) {
		return !e.isDrawingAllDay(mDayStart, mDayEnd) && mModel.shouldDrawEvent(e);
	}
	
	/**
	 * @return True if this DayView is of the current Day.
	 */
	private boolean isCurrentDay() {
		long now = System.currentTimeMillis();
		if (now < mDayStart || now > mDayEnd) {
			return false;
		}
		return true;
	}
	
	/**
	 * Updates the count of Events on this Day.
	 * @param count
	 */
	public void updateEventCountView() {
		int count = mNonAllDayCount + mAllDayCount;
		
		TextView countView = (TextView) ((View) getParent().getParent()).findViewById(R.id.events_count);
		if (count == 0) {
			countView.setVisibility(View.GONE);
		} else {
			countView.setVisibility(View.VISIBLE);
			countView.setText(count + " event" + ((count == 1)? "" : "s"));
		}
	}
	
////============================================================================
//// Utils.
////============================================================================
	
	/**
	 * Sets the alpha of color.
	 * @param alpha
	 * @param color
	 * @return
	 */
	public static int setAlpha(float alpha, int color) {
		int alphaInt = (int) (alpha * 255);
		return Color.argb(alphaInt, Color.red(color), Color.green(color), Color.blue(color));
	}
	
	/**
	 * Lightens the color by 'amount'
	 * @param color
	 * @param amount
	 * @return
	 */
	public static int lightenBy(int color, float amount) {
		if (amount > 1.0f || amount < 0f) {
			return color;
		}
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[1] *= (1f - amount);
		hsv[2] *= (1f + amount);
		return Color.HSVToColor(hsv);
	}
}
