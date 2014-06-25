package me.jmhend.CalendarViewer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay;
import me.jmhend.CalendarViewer.CalendarViewerDecorator.ApplyLevel;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
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
	
	public static final String KEY_HEIGHT = "height";
	public static final String KEY_WEEK_START = "week_start";
	public static final String KEY_CURRENT_MONTH = "current_month";
	public static final String KEY_CURRENT_YEAR = "current_year";
	public static final String KEY_CURRENT_DAY_OF_MONTH = "current_day_of_month";
	
	public static final int DAY_SEPARATOR_WIDTH = 1;
	protected static final int MIN_HEIGHT = 10;
	protected static final int DEFAULT_FIRST_WEEKDAY = Calendar.SUNDAY;
	protected static final int DEFAULT_DAYS_PER_WEEK = 7;
	protected static final int DEFAULT_NUM_ROWS = 6;
	protected static final int DAYS_IN_MONTH = 31;
	
	protected static final String[] WEEKDAYS = {
		"SAT",	// Calendar.DAY_OF_WEEK is 1 based. For % 7 operations, 7 --> 0
		"SUN",
		"MON",
		"TUE",
		"WED",
		"THU",
		"FRI",
	};
	
	
	
	protected static final String[] DAYS = new String[DAYS_IN_MONTH+1];
	
////==================================================================================================
//// Static
////==================================================================================================
	
	static {
		// Initialize the DAYS array.
		for (int i = 1; i < DAYS.length; i++) {
			DAYS[i] = String.valueOf(i);
		}
	}
	
////===================================================================================
//// OnDayClickListener
////===================================================================================
	/**
	 * Listens for TouchEvents on CalendarView days.
	 * 
	 * @author jmhend
	 */
	public interface OnDayClickListener {
		
		/**
		 * Called when a day is clicked on a calendar View.
		 * @param calendarView
		 * @param day
		 */
		public void onDayClick(View calendarView, CalendarDay day);
		
		/**
		 * Called when a day is long-clicked on a calendar View.
		 * @param calendarView
		 * @param day
		 */
		public void onDayLongClick(View calendarView, CalendarDay day);
	}
	
////===================================================================================
//// Member variables.
////===================================================================================
	
	// Dimens
	protected int mSelectedCircleRadius;
	protected int mSelectedCircleStrokeWidth;
	protected int mDayTextSize;
	protected int mDayOfWeekTextSize;
	protected int mMonthHeaderHeight;
	protected int mMonthTitleTextSize;
	protected int mBottomPadding;
	protected int mPadding = 0;
	protected int mWidth;
	protected int mRowHeight;
	protected int mDayMarkerRadius;
	
	// Time
	protected Calendar mCalendar;
	protected int mDayOfWeekStart = 0;
	protected int mWeekStart = DEFAULT_FIRST_WEEKDAY;
	
	// Colour
	protected int mActiveDayTextColor;
	protected int mInactiveDayTextColor;
	protected int mDayMarkerColor;
	protected int mDayMarkerFaintColor;
	protected int mMonthTitleColor;
	protected int mTodayNumberColor;
	protected int mSelectedDayColor;
	protected int mSelectedCircleColor;
	
	// Typeface
	protected Typeface mTypeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
	protected Typeface mTypefaceBold = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD);
	
	// Paint
	protected Paint mMonthDayLabelPaint;
	protected Paint mMonthNumPaint;
	protected Paint mMonthTitlePaint;
	protected Paint mSelectedCirclePaint;
	protected Paint mDayMarkerPaint;

	// Geometry
	protected int mDaysPerWeek = DEFAULT_DAYS_PER_WEEK;
	protected int mNumRows = DEFAULT_NUM_ROWS;
	protected int mNumCells;
	protected boolean mDrawDayOfWeekLabels;
	
	// TouchEvents
	protected float mLastTouchX;
	protected float mLastTouchY;
	
	// Actions
	protected List<CalendarViewerDecorator> mDecorators = new ArrayList<CalendarViewerDecorator>();
	
	private OnDayClickListener mOnDayClickListener;
	
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
	 * Initialize yo' junk.
	 */
	protected void init() {
		initResources();
		initView();
	}
	
	/**
	 * Initialize/load resources.
	 */
	protected void initResources() {
		Resources r = getContext().getResources();
		mCalendar = Calendar.getInstance();
		mCalendar.set(Calendar.HOUR_OF_DAY, 0);
		mCalendar.set(Calendar.MINUTE, 0);
		mCalendar.set(Calendar.SECOND, 0);
		mCalendar.set(Calendar.MILLISECOND, 0);
		mActiveDayTextColor = r.getColor(R.color.day_text_active);
		mInactiveDayTextColor = r.getColor(R.color.day_text_inactive);
		mDayMarkerColor = r.getColor(R.color.day_marker_color);
		mDayMarkerFaintColor = r.getColor(R.color.day_marker_color_faint);
		mTodayNumberColor = r.getColor(R.color.day_text_today);
		mSelectedDayColor = r.getColor(R.color.day_text_selected);
		mMonthTitleColor = r.getColor(R.color.calendar_text_title);
		mSelectedCircleColor = r.getColor(R.color.selected_highlight);
		mDayTextSize = r.getDimensionPixelSize(R.dimen.day_text_size);
		mMonthTitleTextSize = r.getDimensionPixelSize(R.dimen.month_title_text_size);
		mDayOfWeekTextSize = r.getDimensionPixelSize(R.dimen.day_of_week_text_size);
		mMonthHeaderHeight = r.getDimensionPixelOffset(R.dimen.month_list_item_header_height);
		mSelectedCircleRadius = r.getDimensionPixelSize(R.dimen.selected_circle_radius);
		mSelectedCircleStrokeWidth = r.getDimensionPixelSize(R.dimen.selected_circle_stroke_width);
		mBottomPadding = r.getDimensionPixelSize(R.dimen.month_bottom_padding);
		mRowHeight = (r.getDimensionPixelOffset(R.dimen.monthview_height) - mMonthHeaderHeight) / 6;
		mDrawDayOfWeekLabels = r.getBoolean(R.bool.draw_day_labels);
		mDayMarkerRadius = r.getDimensionPixelSize(R.dimen.day_marker_radius);
	}

	/**
	 * Initialize View components.
	 */
	protected void initView() {
		mMonthTitlePaint = new Paint();
		mMonthTitlePaint.setAntiAlias(true);
		mMonthTitlePaint.setTextSize(mMonthTitleTextSize);
		mMonthTitlePaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
		mMonthTitlePaint.setColor(mMonthTitleColor);
		mMonthTitlePaint.setTextAlign(Paint.Align.CENTER);
		mMonthTitlePaint.setStyle(Paint.Style.FILL);
		mSelectedCirclePaint = new Paint();
		mSelectedCirclePaint.setAntiAlias(true);
		mSelectedCirclePaint.setColor(mSelectedCircleColor);
		mSelectedCirclePaint.setStyle(Paint.Style.FILL);
		mMonthDayLabelPaint = new Paint();
		mMonthDayLabelPaint.setAntiAlias(true);
		mMonthDayLabelPaint.setTextSize(mDayOfWeekTextSize);
		mMonthDayLabelPaint.setColor(mMonthTitleColor);
		mMonthDayLabelPaint.setStyle(Paint.Style.FILL);
		mMonthDayLabelPaint.setTextAlign(Paint.Align.CENTER);
		mMonthNumPaint = new Paint();
		mMonthNumPaint.setAntiAlias(true);
		mMonthNumPaint.setTextSize(mDayTextSize);
		mMonthNumPaint.setStyle(Paint.Style.FILL);
		mMonthNumPaint.setTextAlign(Paint.Align.CENTER);
		mDayMarkerPaint = new Paint();
		mDayMarkerPaint.setAntiAlias(true);
		mDayMarkerPaint.setColor(mDayMarkerColor);
		mDayMarkerPaint.setStyle(Paint.Style.FILL);
		mDayMarkerPaint.setStrokeWidth(mSelectedCircleStrokeWidth);
		
		setOnClickListener(new OnClickListener() {
			/*
			 * (non-Javadoc)
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {
				CalendarDay day = getDayFromLocation(mLastTouchX, mLastTouchY);
				if (day != null) {
					onDayClick(day);
				}
			}
		});
		setOnLongClickListener(new OnLongClickListener() {
			/*
			 * (non-Javadoc)
			 * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
			 */
			@Override
			public boolean onLongClick(View v) {
				CalendarDay day = getDayFromLocation(mLastTouchX, mLastTouchY);
				if (day != null) {
					onDayLongClick(day);
					return true;
				}
				return false;
			}
		});
	}
	
////===================================================================================
//// Getters/Setters
////===================================================================================
	
	/**
	 * @return The display title of this CalendarView.
	 */
	public String getTitle() {
		return "";
	}
	
	/**
	 * Sets the color of the Month title and day labels.
	 * @param color
	 */
	public void setTitleColor(int color) {
		mMonthTitleColor = color;
		mMonthTitlePaint.setColor(mMonthTitleColor);
		mMonthDayLabelPaint.setColor(mMonthTitleColor);
		invalidate(0, 0, mWidth, mMonthHeaderHeight);
	}
	
////===================================================================================
//// Draw
////===================================================================================
	
	/**
	 * Draws the day of the week labels.
	 * @param canvas
	 */
	protected void drawDayOfWeekLabels(Canvas canvas) {
		if (!mDrawDayOfWeekLabels) {
			return;
		}
		final int y = mMonthHeaderHeight / 2 + mDayOfWeekTextSize / 2 ;
		final int spacing = (mWidth - 2 * mPadding) / (2 * mDaysPerWeek);
		for (int day = 0; day < mDaysPerWeek; day++) {
			int dayOfWeek = (day + mWeekStart) % mDaysPerWeek;
			int x = spacing * (1 + 2 * day) + mPadding;
			canvas.drawText(WEEKDAYS[dayOfWeek], x, y, mMonthDayLabelPaint);
		}
	}
	
////===================================================================================
//// TouchEvents
////===================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {
		switch (motionEvent.getActionMasked()) {
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_MOVE:
		case MotionEvent.ACTION_DOWN:
			mLastTouchX = motionEvent.getX();
			mLastTouchY = motionEvent.getY();
			break;
		default:
		}
		return super.onTouchEvent(motionEvent);
	}
	
	/**
	 * Calculates which day was clicked at the (x,y) position.
	 * @param x
	 * @param y
	 */
	protected abstract CalendarDay getDayFromLocation(float x, float y);
	
	/**
	 * Find the x-position, in pixels, of the day in this CalendarView.
	 * @param day
	 */
	public abstract int getXForDay(CalendarDay day);
	
	
	/**
	 * Find the y-position, in pixels, of the day in this CalendarView.
	 * @param day
	 */
	public abstract int getYForDay(CalendarDay day);
	
////===================================================================================
//// Callbacks.
////===================================================================================
	
	/**
	 * @param listener The OnDayClickListener to register.
	 */
	public void setOnDayClickListener(OnDayClickListener listener) {
		mOnDayClickListener = listener;
	}
	
	/**
	 * Called when a day is clicked.
	 * @param day
	 */
	protected void onDayClick(CalendarDay day) {
		if (mOnDayClickListener != null) {
			mOnDayClickListener.onDayClick(this, day);
		}
	}
	
	/**
	 * Called when a day is long-clicked.
	 * @param day
	 */
	protected void onDayLongClick(CalendarDay day) {
		if (mOnDayClickListener != null) {
			mOnDayClickListener.onDayLongClick(this, day);
		}
	}

////==================================================================================================
//// Decorators
////==================================================================================================
	
	/**
	 * Adds a CalendarViewerDecorator to the MonthView.
	 * Decorators will be applied in the order that they're added.
	 * @param decorator
	 */
	public void addDecorator(CalendarViewerDecorator decorator) {
		mDecorators.add(decorator);
	}
	
	/**
	 * Removes the CalendarViewerDecorators.
	 * @param decorator
	 */
	public void removeDecorator(CalendarViewerDecorator decorator) {
		mDecorators.remove(decorator);
	}
	
	/**
	 * Removes all CalendarViewerDecorators.
	 */
	public void clearDecorators() {
		mDecorators.clear();
	}
	
	/**
	 * Applies all deocorates, in added order, whose AppyLevel == level.
	 * @param level
	 */
	protected void applyDecorators(Canvas canvas, ApplyLevel level) {
		for (CalendarViewerDecorator decorator : mDecorators) {
			if (decorator.getApplyLevel() == level) {
				decorator.apply(this, canvas);
			}
		}
	}
	
}
