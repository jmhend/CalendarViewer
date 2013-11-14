package me.jmhend.CalendarViewer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay;

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
	
	private Map<Event, Rect> mRectMap = new HashMap<Event, Rect>();
	private Event mPressedEvent;
	
	private DayPagerAdapter mAdapter;
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
	private int mTitleTextSize;
	private int mLocationTextSize;
	
	private Paint mLinePaint;
	private Paint mHourPaint;
	private Paint mEventPaint;
	private Paint mEventTitlePaint;
	private Paint mEventLocationPaint;
	private Paint mCurrentTimePaint;
	
	private long mDayStart;
	private long mDayEnd;
	private Calendar mCalendar;
	
	private OnEventClickListener mEventClickListener;
	
	private ScaleGestureDetector mScaleDetector;
	private float mScale = 1f;
	
////============================================================================
//// Runnables
////============================================================================
	
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
		mTitleTextSize = 32;
		mLocationTextSize = 32;
		
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
		mEventLocationPaint.setTextSize(mLocationTextSize);
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
//// Getters/Setters
////============================================================================
	
	/**
	 * @param adapter
	 */
	public void setAdapter(DayPagerAdapter adapter) {
		mAdapter = adapter;
	}
	
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
	 * @param listener
	 */
	public void setOnEventClickListener(OnEventClickListener listener) {
		mEventClickListener = listener;
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
		drawCurrentTimeLine(canvas);
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
	 * Draws the line representing the current time.
	 * @param canvas
	 */
	private void drawCurrentTimeLine(Canvas canvas) {
		long now = System.currentTimeMillis();
		if (now < mDayStart || now > mDayEnd) {
			return;
		}
		long y = getYForTime(now);
		canvas.drawRect(mHourWidth, y, mWidth, y + 2 * mLineHeight, mCurrentTimePaint);
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
					color = 0xFFFF6600;
				}
				if (event == mPressedEvent) {
					color = lightenBy(color, 0.5f);
				}
				color = setAlpha(.6f, color);
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
//		mScaleDetector.onTouchEvent(e);
//		if (mScaleDetector.isInProgress()) {
//			return true;
//		}
		
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
	private void onEventClick(Event e) {
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
		if (mModel == null) {
			return new ArrayList<Event>();
		}
		return (List<Event>) mModel.getEventsOnDay(mDayStart);
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
			if (!willDrawEvent(event)) {
				i++;
				continue;
			}
			
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
				bottomY = (int) ((mHourHeight * 24 * mScale) + mPaddingTop);
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
		return true;
//		return e.getDrawingColor() != 0;
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
