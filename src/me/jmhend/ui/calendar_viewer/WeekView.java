package me.jmhend.ui.calendar_viewer;

import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;

import me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay;
import me.jmhend.ui.calendar_viewer.CalendarViewerDecorator.ApplyLevel;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.util.AttributeSet;

/**
 * View displaying a Week of days.
 * 
 * @author jmhend
 */
public class WeekView extends CalendarView {
	
	private static final String TAG = WeekView.class.getSimpleName();
	
////======================================================================================
//// Static constants.
////======================================================================================

	public static final int MAX_DAYS = 7;
	
	public static String KEY_START_YEAR = "start_year";
	public static String KEY_START_MONTH = "start_month";
	public static String KEY_START_DAY_OF_MONTH = "start_dayOfMonth";
	public static String KEY_END_YEAR = "end_year";
	public static String KEY_END_MONTH = "end_month";
	public static String KEY_END_DAY_OF_MONTH = "end_dayOfMonth";
	public static String KEY_SELECTED_YEAR = "selected_year";
	public static String KEY_SELECTED_MONTH = "selected_month";
	public static String KEY_SELECTED_DAY_OF_MONTH = "selected_dayOfMonth";
	
////======================================================================================
//// WeekRange
////======================================================================================
	
	/**
	 * Defines the day range for a week.
	 * 
	 * @author jmhend
	 */
	public static class WeekRange {
		public CalendarDay weekStart;
		public CalendarDay weekEnd;
		
		public WeekRange(CalendarDay start, CalendarDay end) {
			this.weekStart = start;
			this.weekEnd = end;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return weekStart.toString() + "-" + weekEnd.toString();
		}
	}
	
////======================================================================================
//// Member variables.
////======================================================================================
	
	// Draw calculations.
	private int mDayY;
	private final int[] mDayXs = new int[MAX_DAYS];
	private final boolean[] mDayActives = new boolean[MAX_DAYS];
	private final int[] mDayOfMonths = new int[MAX_DAYS];
	private final int[] mMonths = new int[MAX_DAYS];
	private final int[] mYears = new int[MAX_DAYS];
	
	private CalendarDay mStartDay;
	private CalendarDay mEndDay;
	private CalendarDay mCurrentDay;
	private CalendarDay mSelectedDay;
	
	private int mCurrentDayPosition = -1;
	private int mSelectedDayPosition = -1;
	
	private StringBuilder mStringBuilder;
	private Formatter mFormatter;

////======================================================================================
//// Constructor.
////======================================================================================
	
	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public WeekView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public WeekView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	/**
	 * @param context
	 */
	public WeekView(Context context) {
		super(context);
		init();
	}
	
////======================================================================================
//// Init.
////======================================================================================

	/**
	 * Initialize.
	 */
	protected void init() {
		super.init();
		mStartDay = new CalendarDay();
		mEndDay = new CalendarDay();
		mCurrentDay = new CalendarDay();
		mSelectedDay = new CalendarDay();
		mNumCells = 7;
		mNumRows = 1;
		mStringBuilder = new StringBuilder(50);
		mFormatter = new Formatter(mStringBuilder, Locale.getDefault());
	}
	
////======================================================================================
//// View.
////======================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		calculateDayPoints();
		applyDecorators(canvas, ApplyLevel.BELOW);
//		drawTitle(canvas);
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
		final int height = mRowHeight + mBottomPadding;
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
	
	
////======================================================================================
//// Draw
////======================================================================================
	
	/**
	 * Draws the week's title as a range.
	 * @param canvas
	 */
	protected void drawTitle(Canvas canvas) {
		final int x = (mWidth + 2 * mPadding) / 2;
		final int y = (mMonthHeaderHeight - mDayOfWeekTextSize) / 2 + mMonthTitleTextSize / 3;
		String title = getTitle();
		canvas.drawText(title, x, y, mMonthTitlePaint);
	}
	
	/**
	 * Draws each date in the month.
	 * @param canvas
	 */
	protected void drawDates(Canvas canvas) {
		for (int i = 0; i < mDayOfMonths.length; i++) {
			int x = mDayXs[i];
			int y = mDayY;
			if (i == mSelectedDayPosition) {
				canvas.drawCircle(x,  y - mDayTextSize / 3, mSelectedCircleRadius, mSelectedCirclePaint);
			}
			
			int textColor;
			Typeface tf;
			if (i == mCurrentDayPosition) {
				textColor = mTodayNumberColor;
				tf = mTypefaceBold;
			} else {
				textColor = mActiveDayTextColor;
				tf = mTypeface;
			}
			
			mMonthNumPaint.setTypeface(tf);
			mMonthNumPaint.setColor(textColor);
			canvas.drawText(DAYS[mDayOfMonths[i]], x, y, mMonthNumPaint);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarView#getTitle()
	 */
	@Override
	public String getTitle() {
		long startTime = mCalendar.getTimeInMillis();
		mCalendar.add(Calendar.DAY_OF_MONTH, 6);
		long endTime = mCalendar.getTimeInMillis();
		
		// Reset.
		setCalendarToStartDay();
		
		mStringBuilder.delete(0, mStringBuilder.length());
		return DateUtils.formatDateRange(getContext(), mFormatter, startTime, endTime, 0).toString();
	}
	
	
	/**
	 * Marks the WeekView to be reused.
	 */
	public void reset() {
		requestLayout();
	}
	
////======================================================================================
//// Logic.
////======================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarView#getDayFromLocation(float, float)
	 */
	@Override
	protected CalendarDay getDayFromLocation(float x, float y) {
		int padding = this.mPadding;
		if ((x < padding) || (x > mWidth - mPadding)) {
			return null;
		}
		
//		if (y < mMonthHeaderHeight) {
//			return null;
//		}
		
		int position = ((int) ((x - padding) * mDaysPerWeek / (this.mWidth - padding - this.mPadding)));
		
		if (position > 0) {
			mCalendar.add(Calendar.DAY_OF_MONTH, position);
		}
		
		CalendarDay day = CalendarDay.fromCalendar(mCalendar);
		
		if (position > 0) {
			setCalendarToStartDay();
		}
		
		return day;
	}	
	
	/**
	 * Calculates the (x,y) coordinates of each day in the month.
	 */
	protected void calculateDayPoints() {
		clearDayArrays();
		int paddingDay = (mWidth - 2 * mPadding) / (2 * mDaysPerWeek);
		mDayY = (mRowHeight + mDayTextSize) / 2 - DAY_SEPARATOR_WIDTH;
		
		for (int i = 0; i < mDayXs.length; i++) {
			int x = paddingDay * (1 + 2 * i) + mPadding;
			mDayXs[i] = x;
		}
	}
	
	/**
	 * Set the parameters that define how his WeekView should display itself.
	 * @param params
	 */
	public void setParams(Map<String, Integer> params) {
		setTag(params);
		
		// Check for optional params.
		if (params.containsKey(KEY_HEIGHT)) {
			mRowHeight = ((Integer) params.get(KEY_HEIGHT)).intValue();
			if (mRowHeight < MIN_HEIGHT) {
				mRowHeight = MIN_HEIGHT;
			}
		}

		int startYear = params.get(KEY_START_YEAR).intValue();
		int startMonth = params.get(KEY_START_MONTH).intValue();
		int startDayOfMonth = params.get(KEY_START_DAY_OF_MONTH);
		mStartDay.set(startYear, startMonth, startDayOfMonth);
		
		int endYear = params.get(KEY_END_YEAR).intValue();
		int endMonth = params.get(KEY_END_MONTH).intValue();
		int endDayOfMonth = params.get(KEY_END_DAY_OF_MONTH).intValue();
		mEndDay.set(endYear, endMonth, endDayOfMonth);
		
		int currentYear = params.get(KEY_CURRENT_YEAR).intValue();
		int currentMonth = params.get(KEY_CURRENT_MONTH).intValue();
		int currentDayOfMonth = params.get(KEY_CURRENT_DAY_OF_MONTH).intValue();
		mCurrentDay.set(currentYear, currentMonth, currentDayOfMonth);
		
		int selectedYear = params.get(KEY_SELECTED_YEAR).intValue();
		int selectedMonth = params.get(KEY_SELECTED_MONTH).intValue();
		int selectedDayOfMonth = params.get(KEY_SELECTED_DAY_OF_MONTH).intValue();
		mSelectedDay.set(selectedYear, selectedMonth, selectedDayOfMonth);

		// Time calculations.
		setCalendarToStartDay();

		// Set up date grid.
		mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);
		if (params.containsKey(KEY_WEEK_START)) {
			mWeekStart = ((Integer) params.get(KEY_WEEK_START)).intValue();
		} else {
			mWeekStart = mCalendar.getFirstDayOfWeek();
		}
		
		mCurrentDayPosition = -1;
		mSelectedDayPosition = -1;
		
		// Calculate which date to display in the week.
		for (int i = 0; i < MAX_DAYS; i++) {
			mYears[i] = mCalendar.get(Calendar.YEAR);
			mMonths[i] = mCalendar.get(Calendar.MONTH);
			mDayOfMonths[i] = mCalendar.get(Calendar.DAY_OF_MONTH);
			
			// Check if and where current day is in this week.
			if (mCurrentDayPosition == -1 && mCurrentDay.isSameDay(mCalendar)) {
				mCurrentDayPosition = i;
			}
			
			// Check if and where selected day is in this week.
			if (mSelectedDayPosition == -1 && mSelectedDay.isSameDay(mCalendar)) {
				mSelectedDayPosition = i;
			}
			
			mCalendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		setCalendarToStartDay();
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarView#getXForDay(me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public int getXForDay(CalendarDay day) {
		for (int i = 0; i < MAX_DAYS; i++) {
			if (mDayOfMonths[i] == day.dayOfMonth 
					&& mMonths[i] == day.month
					&& mYears[i] == day.year) {
				return mDayXs[i];
			}
		}
		return -1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarView#getYForDay(me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public int getYForDay(CalendarDay day) {
		return mDayY;
	}
	
////==================================================================================================
//// Utility.
////==================================================================================================
	
	/**
	 * Sets this WeekView's Calendar to the startDay.
	 */
	private void setCalendarToStartDay() {
		mCalendar.set(Calendar.MONTH, mStartDay.month);
		mCalendar.set(Calendar.YEAR, mStartDay.year);
		mCalendar.set(Calendar.DAY_OF_MONTH, mStartDay.dayOfMonth);
	}
	
	/**
	 * Zero-out point arrays, reset valid array.
	 */
	private void clearDayArrays() {
		for (int i = 0; i < MAX_DAYS; i++) {
			mDayXs[i] = 0;
			mDayActives[i] = false;
		}
	}


}
