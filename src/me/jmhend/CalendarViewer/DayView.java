package me.jmhend.CalendarViewer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
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
	
////============================================================================
//// Member variables.
////============================================================================
	
	private List<EventDrawable> mEvents;
	private Map<EventDrawable, Rect> mRectMap = new HashMap<EventDrawable, Rect>();
	
	private int mWidth;
	private int mHourWidth;
	private int mHourHeight;
	private int mLineHeight;
	private int mPaddingTop;
	private int mEventTitleHeight;
	private int mEventLocationHeight;
	private int mEventXPadding;
	
	private int mHourTextSize;
	private int mTitleTextSize;
	private int mLocationTextSize;
	
	private Paint mLinePaint;
	private Paint mHourPaint;
	private Paint mEventPaint;
	private Paint mEventTitlePaint;
	private Paint mEventLocationPaint;
	
	private Calendar mCalendar;
	
	
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
	
	private void init() {
		mPaddingTop = 80;
		mHourWidth = 140;
		mHourHeight = 200;
		mLineHeight = 3;
		mEventTitleHeight = 60;
		mEventLocationHeight = 45;
		mEventXPadding = 6;
		
		mHourTextSize = 40;
		mTitleTextSize = 40;
		mLocationTextSize = 30;
		
		mLinePaint = new Paint();
		mLinePaint.setAntiAlias(true);
		mLinePaint.setColor(0xFFCCCCCC);
		mLinePaint.setStyle(Style.FILL);
		
		mHourPaint = new Paint();
		mHourPaint.setAntiAlias(true);
		mHourPaint.setColor(0xFF666666);
		mHourPaint.setTextSize(mHourTextSize);
		mHourPaint.setTypeface(Typeface.DEFAULT);
		mHourPaint.setTextAlign(Paint.Align.RIGHT);
		
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
		
		mCalendar = Calendar.getInstance();
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
		final int height = mHourHeight * 24 + mPaddingTop;
		setMeasuredDimension(width, height);
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
	}
	
	/**
	 * Draws the Hours and hour lines behind the Events.
	 * @param canvas
	 */
	private void drawHoursAndLines(Canvas canvas) {
		for (int i = 0; i < 24; i++) {
			int y = i * mHourHeight + mPaddingTop;
			canvas.drawLine(mHourWidth, y, mWidth, y + mLineHeight, mLinePaint);
			canvas.drawText(getHourAtLinePosition(i), mHourWidth - 20, y + mHourTextSize / 3, mHourPaint);
		}
	}
	
	/**
	 * Draws each event Rect to the canvas.
	 */
	private void drawEventRects(Canvas canvas) {
		if (mEvents == null) {
			return;
		}
		for (EventDrawable event : mEvents) {
			Rect rect = mRectMap.get(event);
			if (rect != null) {
				
				// Draw transluscent background.
				canvas.drawRect(rect, mEventPaint);
				
				// Draw border
				Rect border = new Rect(rect);
				border.right = border.left + 10;
				canvas.drawRect(border, mEventPaint);
				canvas.drawRect(border, mEventPaint);
				
				// Draw Title
				canvas.drawText(event.getDrawableTitle(), rect.left + 20, rect.top + (mTitleTextSize) + 10, mEventTitlePaint);
				// Draw Location.
			} else {
				Log.e(TAG, "Drawing null Rect");
			}
		}
	}
	
	/**
	 * @param position
	 * @return The hour label at the line position;
	 */
	private String getHourAtLinePosition(int position) {
		if (position == 0) {
			return "12";
		}
		if (position < 12) {
			return position + "";
		}
		if (position == 12) {
			return "Noon";
		}
		return (position - 12) + "";
	}
	
////============================================================================
//// Event positioning.
////============================================================================
	
	/**
	 * @return The EventDrawables to draw on this DayView.
	 */
	private List<EventDrawable> getEvents() {
		if (mEvents == null) {
			mEvents = getTestEvents();
		}
		return mEvents;
	}
	
	/**
	 * Calculates the positiong of EventDrawables within this DayView.
	 */
	protected void calculateEventRects() {
		mRectMap.clear();
		List<EventDrawable> events = getEvents();
		int i = 0;
		int size = events.size();
		int minY = -1;
		
		List<Rect> groupedRects = new ArrayList<Rect>();
		
		while (i < size) {
			EventDrawable event = events.get(i);
			if (!willDrawEvent(event)) {
				continue;
			}
			
			int topY = getYForTime(event.getDrawingStartTime());
			int bottomY = getYForTime(event.getDrawingEndTime());
			int textBottom = topY + getTextHeight(event);
			
			// minY hasn't been set yet.
			if (minY == -1) {
				minY = topY;
			}
			
			// New collision group; layout previous group.
			if (topY > minY) {
				layoutGroupRects(groupedRects);
			}
			
			Rect r = new Rect();
			r.top = topY;
			r.bottom = bottomY;
			groupedRects.add(r);
			mRectMap.put(event, r);
			
			// End of events.
			if (i == (size - 1)) {
				layoutGroupRects(groupedRects);
			}
			
			minY = Math.max(minY, textBottom);
			i++;
		}
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
			int endX = startX + width - mEventXPadding;
			Rect er = groupedRects.get(j);
			er.left = startX;
			er.right = endX;
		}
		groupedRects.clear();
	}
	
	/**
	 * @return The y position in this DayView that correlates to the 'time'.
	 */
	private int getYForTime(long time) {
		mCalendar.setTimeInMillis(time);
		int hourY = mCalendar.get(Calendar.HOUR_OF_DAY) * mHourHeight + mPaddingTop;
		float offsetPercent = ((float) mCalendar.get(Calendar.MINUTE) / (float) 60f);
		int minOffset = (int) (offsetPercent * mHourHeight);
		return hourY + minOffset;
	}
	
	/**
 	 * @return The height needed to display the EventDrawable's text.
	 */
	private int getTextHeight(EventDrawable e) {
		if (e.getTextLinesCount() == 2) {
			return mEventTitleHeight + mEventLocationHeight;
		}
		return mEventTitleHeight;
	}
	
	/**
	 * @return True if the EventDrawable will be drawn in this DayView, false otherwise.
	 */
	private boolean willDrawEvent(EventDrawable e) {
		return true;
	}
	
////============================================================================
//// Test
////============================================================================

	private List<EventDrawable> getTestEvents() {
		List<EventDrawable> events = new ArrayList<EventDrawable>();
		events.add(new TestEvent(1383832800000L, 1383865200000L));
		events.add(new TestEvent(1383840000000L, 1383865200000L));
		events.add(new TestEvent(1383847200000L, 1383850800000L));
		events.add(new TestEvent(1383847200000L, 1383854300000L));
		events.add(new TestEvent(1383848100000L, 1383854300000L));
		events.add(new TestEvent(1383854400000L, 1383863400000L));
		return events;
	}
	
	private static class TestEvent implements EventDrawable {
		
		private long start;
		private long end;
		
		public TestEvent(long start, long end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public long getDrawingStartTime() {
			return start;
		}
		@Override
		public long getDrawingEndTime() {
			return end;
		}
		@Override
		public boolean isDrawingAllDay() {
			return false;
		}
		@Override
		public int getDrawingColor() {
			return 0;
		}
		@Override
		public int getTextLinesCount() {
			return 1;
		}

		@Override
		public String getDrawableTitle() {
			return "Howdy y'all";
		}

		@Override
		public String getDrawingLocation() {
			return "Copacabana";
		}
		
	}
}
