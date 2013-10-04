package me.jmhend.ui.calendar_viewer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import me.jmhend.ui.calendar_viewer.CalendarViewerDecorator.ApplyLevel;
import me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MonthView extends View {
	
	private static final String TAG = MonthView.class.getSimpleName();
	
////==================================================================================================
//// Static constants.
////==================================================================================================
	
	protected static final int DAY_SEPARATOR_WIDTH = 1;
	protected static final int MIN_HEIGHT = 10;
	protected static final int DEFAULT_DAYS_PER_WEEK = 7;
	protected static final int DEFAULT_NUM_ROWS = 6;
	protected static final int DEFAULT_FIRST_WEEKDAY = Calendar.SUNDAY;
	public static final int MAX_DAYS = 31;
	
	public static final String KEY_MONTH = "month";
	public static final String KEY_YEAR = "year";
	public static final String KEY_HEIGHT = "height";
	public static final String KEY_SELECTED_DAY = "selected_day";
	public static final String KEY_WEEK_START = "week_start";
	public static final String KEY_CURRENT_MONTH = "current_month";
	public static final String KEY_CURRENT_YEAR = "current_year";
	public static final String KEY_CURRENT_DAY_OF_MONTH = "current_day_of_month";
	
	private static final String[] DAYS = new String[MAX_DAYS+1];
	private static final String[] WEEKDAYS = {
		"SAT",	// Calendar.DAY_OF_WEEK is 1 based. For % 7 operations, 7 --> 0
		"SUN",
		"MON",
		"TUE",
		"WED",
		"THU",
		"FRI",
	};
	
////==================================================================================================
//// Member variables.
////==================================================================================================
	
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
	
	// Time
	private Calendar mCalendar;
	protected int mDayOfWeekStart = 0;
	protected int mWeekStart = DEFAULT_FIRST_WEEKDAY;
	protected int mMonth;
	protected int mYear;
	protected int mTodayOfWeek;
	protected int mSelectedDayOfWeek;
	protected int mCurrentYear;
	protected int mCurrentMonth;
	protected int mCurrentDayOfMonth;
	
	// Colour
	protected int mActiveDayTextColor;
	protected int mInactiveDayTextColor;
	protected int mMonthTitleColor;
	protected int mTodayNumberColor;
	protected int mSelectedCircleColor;
	
	// Typeface
	protected Typeface mTypeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
	protected Typeface mTypefaceBold = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD);
	
	// Paint
	protected Paint mMonthDayLabelPaint;
	protected Paint mMonthNumPaint;
	protected Paint mMonthTitlePaint;
	protected Paint mSelectedCirclePaint;
	
	// Geometry
	protected int mDaysPerWeek = DEFAULT_DAYS_PER_WEEK;
	protected int mNumRows = DEFAULT_NUM_ROWS;
	protected int mNumCells;
	
	// Formatting
	private StringBuilder mStringBuilder;
	
	// TouchEvents
	private float mLastTouchX;
	private float mLastTouchY;
	
	// Actions
	private OnDayClickListener mOnDayClickListener;
	private List<CalendarViewerDecorator> mDecorators = new ArrayList<CalendarViewerDecorator>();
	
	// Draw calculations.
	private final int[] mDayXs = new int[MAX_DAYS];
	private final int[] mDayYs = new int[MAX_DAYS];
	private final boolean[] mDayActives = new boolean[MAX_DAYS];
	
////==================================================================================================
//// Static
////==================================================================================================
	
	static {
		// Initialize the DAYS array.
		for (int i = 1; i < DAYS.length; i++) {
			DAYS[i] = String.valueOf(i);
		}
	}

////==================================================================================================
//// Constructor.
////==================================================================================================

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public MonthView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public MonthView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	/**
	 * @param context
	 */
	public MonthView(Context context) {
		super(context);
		init();
	}
	
////==================================================================================================
//// Init
////==================================================================================================
	
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
		mStringBuilder = new StringBuilder(50);
		mCalendar = Calendar.getInstance();
		mActiveDayTextColor = r.getColor(R.color.day_text_active);
		mInactiveDayTextColor = r.getColor(R.color.day_text_inactive);
		mTodayNumberColor = r.getColor(R.color.day_text_today);
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
		mSelectedCirclePaint.setStyle(Paint.Style.STROKE);
		mSelectedCirclePaint.setStrokeWidth(mSelectedCircleStrokeWidth);
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
	
////==================================================================================================
//// Getters/Setters
////==================================================================================================
	
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
	
////==================================================================================================
//// View.
////==================================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		calculateDayPoints();
		applyDecorators(canvas, ApplyLevel.BELOW);
		drawMonthTitle(canvas);
		drawDayOfWeekLabels(canvas);
		drawDates(canvas);
		applyDecorators(canvas, ApplyLevel.TOP);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeaureSpec, int heightMeasureSpec) {
		final int width = MeasureSpec.getSize(widthMeaureSpec);
		final int height = mRowHeight * mNumRows + mMonthHeaderHeight + mBottomPadding;
		setMeasuredDimension(width, height);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onSizeChanged(int, int, int, int)
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mWidth = w;
	}
	
////==================================================================================================
//// TouchEvents
////==================================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {
		switch (motionEvent.getActionMasked()) {
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_MOVE:
			mLastTouchX = motionEvent.getX();
			mLastTouchY = motionEvent.getY();
			break;
		default:
		}
		return super.onTouchEvent(motionEvent);
	}
	
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
	private void onDayClick(CalendarDay day) {
		if (mOnDayClickListener != null) {
			mOnDayClickListener.onDayClick(this, day);
		}
	}
	
	/**
	 * Called when a day is long-clicked.
	 * @param day
	 */
	private void onDayLongClick(CalendarDay day) {
		if (mOnDayClickListener != null) {
			mOnDayClickListener.onDayLongClick(this, day);
		}
	}
	
////==================================================================================================
//// Draw
////==================================================================================================
	
	/**
	 * Draws the Month name.
	 * @param canvas
	 */
	protected void drawMonthTitle(Canvas canvas) {
		final int x = (mWidth + 2 * mPadding) / 2;
		final int y = (mMonthHeaderHeight - mDayOfWeekTextSize) / 2 + mMonthTitleTextSize / 3;
		canvas.drawText(getMonthTitleString(), x, y, mMonthTitlePaint);
	}
	
	/**
	 * Draws the day of the week labels.
	 * @param canvas
	 */
	protected void drawDayOfWeekLabels(Canvas canvas) {
		final int y = mMonthHeaderHeight - mDayOfWeekTextSize / 2;
		final int spacing = (mWidth - 2 * mPadding) / (2 * mDaysPerWeek);
		for (int day = 0; day < mDaysPerWeek; day++) {
			int dayOfWeek = (day + mWeekStart) % mDaysPerWeek;
			int x = spacing * (1 + 2 * day) + mPadding;
			canvas.drawText(WEEKDAYS[dayOfWeek], x, y, mMonthDayLabelPaint);
		}
	}
	
	/**
	 * Draws each date in the month.
	 * @param canvas
	 */
	protected void drawDates(Canvas canvas) {
		for (int day = 1; day <= mNumCells; day++) {
			final int x = mDayXs[day-1];
			final int y = mDayYs[day-1];
			
			if (mSelectedDayOfWeek == day) {
				canvas.drawCircle(x,  y - mDayTextSize / 3, mSelectedCircleRadius, mSelectedCirclePaint);
			}
			int textColor;
			if (mTodayOfWeek == day) {
				textColor = mTodayNumberColor;
			} else if (mDayActives[day-1]) {
				textColor = mActiveDayTextColor;
			} else {
				textColor = mInactiveDayTextColor;
			}
			Typeface tf = (mTodayOfWeek == day) ? mTypefaceBold : mTypeface;
			mMonthNumPaint.setTypeface(tf);
			mMonthNumPaint.setColor(textColor);
			canvas.drawText(DAYS[day], x, y, mMonthNumPaint);
		}
	}
	
////==================================================================================================
//// Logic
////==================================================================================================

	/**
	 * Calculates the (x,y) coordinates of each day in the month.
	 */
	protected void calculateDayPoints() {
		clearDayArrays();
		int y = (mRowHeight + mDayTextSize) / 2 - DAY_SEPARATOR_WIDTH + mMonthHeaderHeight;
		int paddingDay = (mWidth - 2 * mPadding) / (2 * mDaysPerWeek);
		int dayOffset = findDayOffset();
		int day = 1;
		
		while (day <= mNumCells) {
			int x = paddingDay * (1 + 2 * dayOffset) + mPadding;
			mDayXs[day-1] = x;
			mDayYs[day-1] = y;
			
			boolean active = (mTodayOfWeek == day) || isCurrentDayOrLater(day);
			mDayActives[day-1] = active;
			
			// Reached the end of the week, start drawing on the next line.
			dayOffset++;
			if (dayOffset == mDaysPerWeek) {
				dayOffset = 0;
				y+= mRowHeight;
			}
			day++;
		}
	}

	/**
	 * @param day
	 * @return The x coordinate where the date of 'day' is drawn.
	 */
	public int getXPointForDay(int day) {
		return mDayXs[day-1];
	}
	
	/**
	 * @param day
	 * @return The y coordinate where the date of 'day' is drawn.
	 */
	public int getYPointForDay(int day) {
		return mDayYs[day-1];
	}
	
	/**
	 * @param day
	 * @return True if 'day' is an active day.
	 */
	public boolean isDayActive(int day) {
		return mDayActives[day-1];
	}
	
	/**
	 * @param dayOfMonth
	 * @return True if 'dayOfMonth' for this month and year is the current day.
	 */
	protected boolean isCurrentDay(int dayOfMonth) {
		return (mYear == mCurrentYear) && (mMonth == mCurrentMonth) && (dayOfMonth == mCurrentDayOfMonth);
	}
	
	/**
	 * @param dayOfMonth
	 * @return True if 'dayOfMonth' for this month and year is today or in the future.
	 */
	protected boolean isCurrentDayOrLater(int dayOfMonth) {
		if (mYear < mCurrentYear) {
			return false;
		}
		if (mYear > mCurrentYear) {
			return true;
		}
		if (mMonth < mCurrentMonth) {
			return false;
		}
		if (mMonth > mCurrentMonth) {
			return true;
		}
		return dayOfMonth >= mCurrentDayOfMonth;
	}
	
	/**
	 * @return The day's offset.
	 */
	private int findDayOffset() {
		int offset = (mDayOfWeekStart < mWeekStart) ? mDayOfWeekStart + mDaysPerWeek : mDayOfWeekStart;
		return offset - mWeekStart;
	}
	
	/**
	 * @return How many rows are needed to draw all dates in this month.
	 */
	private int calculateNumRows() {
		int dayOffset = findDayOffset();
		int numRows = (dayOffset + mNumCells) / mDaysPerWeek;
		int plusOne = ((dayOffset + mNumCells) % mDaysPerWeek > 0) ? 1 : 0;
		return plusOne + numRows;
	}
	
	/**
	 * @return String to title the month.
	 */
	private String getMonthTitleString() {
		mStringBuilder.setLength(0);
		long time = mCalendar.getTimeInMillis();
		return DateUtils.formatDateRange(getContext(), time, time, 52).toString();
	}
	
	/**
	 * Calculates which day was clicked at the (x,y) position.
	 * @param x
	 * @param y
	 */
	public CalendarDay getDayFromLocation(float x, float y) {
		int padding = this.mPadding;
		if ((x < padding) || (x > mWidth - mPadding)) {
			return null;
		}
		
		if (y < mMonthHeaderHeight) {
			return null;
		}
		
		int yDay = (int) (y - mMonthHeaderHeight) / mRowHeight;
		int day = 1 + ((int) ((x - padding) * this.mDaysPerWeek / (this.mWidth - padding - this.mPadding)) - findDayOffset()) + yDay * this.mDaysPerWeek;
		
		// Check day bounds.
		if (day < 1 || day > Utils.getDaysInMonth(mMonth, mYear)) {
			return null;
		}

		 return new CalendarDay(mYear, mMonth, day);
	}
	
	/**
	 * Marks the MonthView to be reused.
	 */
	public void reset() {
		mNumRows = 6;
		requestLayout();
	}
	
	/**
	 * Sets the month parameters that customize the MonthView's data.
	 * @param monthParams
	 */
	public void setMonthParams(Map<String, Integer> monthParams) {
		setTag(monthParams);
		
		// Check for optional params.
		if (monthParams.containsKey(KEY_HEIGHT)) {
			mRowHeight = ((Integer) monthParams.get(KEY_HEIGHT)).intValue();
			if (mRowHeight < MIN_HEIGHT) {
				mRowHeight = MIN_HEIGHT;
			}
		}
		if (monthParams.containsKey(KEY_SELECTED_DAY)) {
			mSelectedDayOfWeek = ((Integer) monthParams.get(KEY_SELECTED_DAY)).intValue();
		}
		
		// Month and Year for the MontView required.
		mMonth = ((Integer) monthParams.get(KEY_MONTH)).intValue();
		mYear = ((Integer) monthParams.get(KEY_YEAR)).intValue();
		
		// Current Month and Year
		mCurrentMonth = ((Integer) monthParams.get(KEY_CURRENT_MONTH)).intValue();
		mCurrentYear = ((Integer) monthParams.get(KEY_CURRENT_YEAR)).intValue();
		mCurrentDayOfMonth = ((Integer) monthParams.get(KEY_CURRENT_DAY_OF_MONTH)).intValue();
		
		// Time calculations.
		boolean isCurrentMonth = (mMonth == mCurrentMonth) && (mYear == mCurrentYear);
		mTodayOfWeek = isCurrentMonth ? mCurrentDayOfMonth : -1;
		mCalendar.set(Calendar.MONTH, mMonth);
		mCalendar.set(Calendar.YEAR, mYear);
		mCalendar.set(Calendar.DAY_OF_MONTH, 1);
		
		// Set up date grid.
		mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);
		if (monthParams.containsKey(KEY_WEEK_START)) {
			mWeekStart = ((Integer) monthParams.get(KEY_WEEK_START)).intValue();
		} else {
			mWeekStart = mCalendar.getFirstDayOfWeek();
		}
		mNumCells = Utils.getDaysInMonth(mMonth, mYear);
		mNumRows = calculateNumRows();
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
	private void applyDecorators(Canvas canvas, ApplyLevel level) {
		for (CalendarViewerDecorator decorator : mDecorators) {
			if (decorator.getApplyLevel() == level) {
				decorator.apply(this, canvas);
			}
		}
	}
	
////==================================================================================================
//// Utility.
////==================================================================================================
	
	/**
	 * Zero-out point arrays, reset valid array.
	 */
	private void clearDayArrays() {
		for (int i = 0; i < MAX_DAYS; i++) {
			mDayXs[i] = 0;
			mDayYs[i] = 0;
			mDayActives[i] = false;
		}
	}	
}
