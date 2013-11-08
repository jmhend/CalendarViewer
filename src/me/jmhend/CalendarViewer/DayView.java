package me.jmhend.CalendarViewer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;

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
	
	private static final int CLICK_DISPLAY_DURATION = 50;
	
////============================================================================
//// Member variables.
////============================================================================
	
	private List<EventDrawable> mEvents;
	private Map<EventDrawable, Rect> mRectMap = new HashMap<EventDrawable, Rect>();
	private EventDrawable mPressedEvent;
	
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
	private int mTitleTextSize;
	private int mLocationTextSize;
	
	private Paint mLinePaint;
	private Paint mHourPaint;
	private Paint mEventPaint;
	private Paint mEventTitlePaint;
	private Paint mEventLocationPaint;
	
	private Calendar mCalendar;
	
	private ScaleGestureDetector mScaleDetector;
	private float mScale = 1f;
	
////============================================================================
//// Runnables
////============================================================================
	
	/**
	 * Handles a delayed click event.
	 * @author jmhend
	 */
    private class ClickRunnable implements Runnable {

    	public ClickRunnable(EventDrawable e) {
    		mEvent = e;
    	}
    	
    	private EventDrawable mEvent;
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
    	
    	public ClearRunnable(EventDrawable e) {
    		mEvent = e;
    	}
    	
    	private EventDrawable mEvent;

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
	
	private void init() {
		mPaddingTop = 80;
		mHourWidth = 140;
		mHourHeight = 200;
		mLineHeight = 3;
		mEventTitleHeight = 60;
		mEventLocationHeight = 45;
		mEventPadding = 10;
		mEventMargin = 6;
		mEventBorderWidth = 10;
		
		mTapDelay = ViewConfiguration.getTapTimeout();
		
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
		
		mEventLocationPaint = new Paint();
		mEventLocationPaint.setAntiAlias(true);
		mEventLocationPaint.setColor(0xAAFFFFFF);
		mEventLocationPaint.setTextAlign(Paint.Align.LEFT);
		mEventLocationPaint.setStyle(Style.FILL);
		mEventLocationPaint.setTextSize(mTitleTextSize * .8f);
		mEventLocationPaint.setTypeface(Typeface.DEFAULT);
		
		mCalendar = Calendar.getInstance();
		
		// Min Height is the height for a half hour.
		int y = this.getYForTime(1383912000000L);
		int y2 = this.getYForTime(1383913800000L);
		mMinEventHeight = y2 - y;
		
		mScaleDetector = new ScaleGestureDetector(getContext(), new OnScaleGestureListener() {

			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				float scale = detector.getScaleFactor();
				Log.e(TAG, "Scale: " + scale);
				mScale = Math.max(1f, scale);
				DayView.this.requestLayout();
				return false;
			}

			@Override
			public boolean onScaleBegin(ScaleGestureDetector detector) {
				Log.w(TAG, "Scale begin");
				return true;
			}

			@Override
			public void onScaleEnd(ScaleGestureDetector detector) {
				Log.w(TAG, "Scale end");
			}
			
		});
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
		final int height = (int) ((mHourHeight * 24 * mScale) + mPaddingTop);
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
			int y = (int) ((i * mHourHeight + mPaddingTop) * mScale);
			canvas.drawLine(mHourWidth, y, mWidth, y + mLineHeight, mLinePaint);
			canvas.drawText(getHourAtLinePosition(i), mHourWidth - mEventPadding, y + mHourTextSize / 3, mHourPaint);
		}
	}
	
	/**
	 * Draws each event Rect to the canvas.
	 */
	private void drawEventRects(Canvas canvas) {
		// TODO: Don't allocate memory in onDraw()!
		
		if (mEvents == null) {
			return;
		}
		for (EventDrawable event : mEvents) {
			Rect rect = mRectMap.get(event);
			if (rect != null) {
				// Draw transluscent background.
				int color = event.getDrawingColor();
				if (event == mPressedEvent) {
					color = lightenBy(color, 0.5f);
				}
				mEventPaint.setColor(color);
				canvas.drawRect(rect, mEventPaint);
				
				// Draw border
				Rect border = new Rect(rect);
				border.right = border.left + mEventBorderWidth;
				canvas.drawRect(border, mEventPaint);
				canvas.drawRect(border, mEventPaint);
				
				// Draw Title
				int width = rect.right - rect.left - 2 * mEventPadding;
				String title = clipText(event.getDrawablingTitle(), mEventTitlePaint, width);
				canvas.drawText(title, border.right + mEventPadding, rect.top + (mTitleTextSize) + mEventPadding, mEventTitlePaint);
				
				// Draw Location.
				if (event.getTextLinesCount() == 2) {
					String location = clipText(event.getDrawingLocation(), mEventLocationPaint, width);
					canvas.drawText(location, border.right + mEventPadding, rect.top + 2 * mTitleTextSize + mEventPadding, mEventLocationPaint);
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
	private String clipText(String text, Paint p, int maxWidth) {
		int breakpoint = p.breakText(text, true, maxWidth, null);
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
//// Touch
////============================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		mScaleDetector.onTouchEvent(e);
		if (mScaleDetector.isInProgress()) {
			return true;
		}
		
		int action = e.getActionMasked();
		if (action == MotionEvent.ACTION_DOWN) {
			EventDrawable event = eventAtPosition((int) e.getX(), (int) e.getY());
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
			EventDrawable event = eventAtPosition((int) e.getX(), (int) e.getY());
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
	
	private void onEventClick(EventDrawable e) {
		Toast.makeText(getContext(), e.getDrawablingTitle(), Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Finds the highest z-position EventDrawable at (x, y)
	 * @param x
	 * @param y
	 * @return
	 */
	public EventDrawable eventAtPosition(int x, int y) {
		List<EventDrawable> events = getEvents();
		int size = events.size();
		
		// Reverse iterate so that events drawn later (higher z-position)
		// are found first.
		for (int i = size - 1; i >= 0; i--) {
			EventDrawable e = events.get(i);
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
			int endX = startX + width - mEventMargin;
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
		return (int) ((hourY + minOffset) * mScale);
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
//// Utils.
////============================================================================
	
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
	
////============================================================================
//// Test
////============================================================================

	private List<EventDrawable> getTestEvents() {
		List<EventDrawable> events = new ArrayList<EventDrawable>();
		events.add(new TestEvent.Builder()
			.title("Check to make sure I'm asleep.")
			.start(1383888600000L)
			.end(1383894000000L)
			.build());
		events.add(new TestEvent.Builder()
			.title("Terrible seminar on synergy or something.")
			.location("Conference Room Soulkiller 2B")
			.lines(2)
			.start(1383832800000L)
			.end(1383865200000L)
			.build());
		events.add(new TestEvent.Builder()
			.title("Grandpa's Ballet Recital")
			.location("Florida")
			.lines(2)
			.start(1383840000000L)
			.end(1383865200000L)
			.build());
		events.add(new TestEvent.Builder()
			.title("Bored Meeting")
			.start(1383847200000L)
			.end(1383850800000L)
			.build());
		events.add(new TestEvent.Builder()
			.title("DVR Spongebob")
			.lines(2)
			.location("Bikini Bottom")
			.start(1383847200000L)
			.end(1383854300000L)
			.build());
		events.add(new TestEvent.Builder()
			.title("Rap Battle")
			.location("Lincoln Memorial")
			.lines(2)
			.start(1383848100000L)
			.end(1383854300000L)
			.build());
		events.add(new TestEvent.Builder()
			.title("M@dison Building Teardown")
			.location("1555 Broadway, Detroit, MI 48226")
			.lines(2)
			.start(1383937200000L)
			.end(1383938100000L)
			.build());
		events.add(new TestEvent.Builder()
			.title("Unscheduled Time")
			.start(1383854400000L)
			.end(1383863400000L)
			.build());
		return events;
	}
	
	private static class TestEvent implements EventDrawable {
		
		public long start;
		public long end;
		public String title;
		public String location;
		public boolean allDay;
		public int color;
		public int lines;
		
		public TestEvent(long start, long end) {
			this.start = start;
			this.end = end;
		}
		
		public static class Builder {
			public long start;
			public long end;
			public String title;
			public String location;
			public boolean allDay;
			public int color;
			public int lines;
			
			public Builder() {
				title = "";
				location = "";
				lines = 1;
				
				color = 0x660088CC;
				Random r = new Random();
				int x = r.nextInt();
				if (x % 2 == 0) {
//					color = 0x6644AA00;
				}
			}
			
			public Builder start(long start) {
				this.start = start;
				return this;
			}
			
			public Builder end(long end) {
				this.end = end;
				return this;
			}
			
			public Builder title(String title) {
				this.title = title;
				return this;
			}
			
			public Builder location(String location) {
				this.location = location;
				return this;
			}
			
			public Builder allDay(boolean allDay) {
				this.allDay = allDay;
				return this;
			}
			
			public Builder color(int color) {
				this.color = color;
				return this;
			}
			
			public Builder lines(int lines) {
				this.lines = lines;
				return this;
			}
			
			public TestEvent build() {
				TestEvent e = new TestEvent(start, end);
				e.title = title;
				e.location = location;
				e.allDay = allDay;
				e.color = color;
				e.lines = lines;
				return e;
			}
			
			
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
			return allDay;
		}
		@Override
		public int getDrawingColor() {
			return color;
		}
		@Override
		public int getTextLinesCount() {
			return lines;
		}

		@Override
		public String getDrawablingTitle() {
			return title;
		}

		@Override
		public String getDrawingLocation() {
			return location;
		}
	}
}
