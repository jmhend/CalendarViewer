package me.jmhend.CalendarViewer;

import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;

import junit.framework.Assert;

import me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay;
import me.jmhend.CalendarViewer.CalendarViewerDecorator.ApplyLevel;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;

/**
 * View that displays a month of days.
 * 
 * @author jmhend
 */
public class MonthView extends CalendarView {
	
	private static final String TAG = MonthView.class.getSimpleName();
	
////==================================================================================================
//// Static constants.
////==================================================================================================
	
	public static final int MAX_DAYS = 6 * 7;
	
	public static final String KEY_MONTH = "month";
	public static final String KEY_YEAR = "year";
	public static final String KEY_SELECTED_DAY = "selected_day";
	public static final String KEY_FOCUSED_DAY = "focused_day";
	
////==================================================================================================
//// Member variables.
////==================================================================================================
	
	// Dimen
	protected int mMaxHeight;
	
	// Time
	protected int mMonth;
	protected int mYear;
	protected int mPreviousMonth;
	protected int mPreviousMonthYear;
	protected int mPreviousMonthLastDayOfMonth;
	protected int mNextMonth;
	protected int mNextMonthYear;
	
	protected int mTodayDayOfMonth;
	protected int mSelectedDayOfMonth;
	protected int mFocusedDayOfMonth;
	protected int mCurrentYear;
	protected int mCurrentMonth;
	protected int mCurrentDayOfMonth;
	
	protected int mMonthStartIndex;
	protected int mMonthEndIndex;
	
	// Draw calculations.
	private final int[] mDayXs = new int[MAX_DAYS];
	private final int[] mDayYs = new int[MAX_DAYS];
	private final int[] mDayOfMonths = new int[MAX_DAYS];
	private final boolean[] mDayHasEvents = new boolean[MAX_DAYS];
	private boolean mHideSelectedWeek = false;
	private int mInvisibleWeekY = 0;
	
	private StringBuilder mStringBuilder;
	private Formatter mFormatter;
	
	private CalendarModel mModel;

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
	 * Initialize/load resources.
	 */
	@Override
	protected void initResources() {
		super.initResources();
		mStringBuilder = new StringBuilder(50);
		mFormatter = new Formatter(mStringBuilder, Locale.getDefault());
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.CalendarView#initView()
	 */
	@Override
	protected void initView() {
		super.initView();
		mNumCells = MAX_DAYS;
		mNumRows = 7;
		mMaxHeight = mRowHeight * mNumRows + mBottomPadding;
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
		final int height = mRowHeight * mNumRows + mBottomPadding;
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
//// Draw
////==================================================================================================
	
	/**
	 * Draws each date in the month.
	 * @param canvas
	 */
	protected void drawDates(Canvas canvas) {
		
		for (int i = 0; i < MAX_DAYS; i++) {
			final int day = mDayOfMonths[i];
			final int x = mDayXs[i];
			final int y = mDayYs[i];
			
			boolean isSelectedDay = isIndexSelectedDay(i);
			boolean isToday = isIndexCurrentDay(i);
			boolean isThisMonth = isIndexInThisMonth(i);
			
			// Don't draw days on this week.
			if (mHideSelectedWeek && y == mInvisibleWeekY) {
				continue;
			}
			
			// Selected day.
			if (isSelectedDay) {
				canvas.drawCircle(x,  y - mDayTextSize / 3, mSelectedCircleRadius, mSelectedCirclePaint);
				
			// Today
			} else if (isToday) {
				mDayMarkerPaint.setColor(mDayMarkerColor);
				mDayMarkerPaint.setStyle(Style.STROKE);
				canvas.drawCircle(x,  y - mDayTextSize / 3, mSelectedCircleRadius - mSelectedCircleStrokeWidth, mDayMarkerPaint);
				
			// Draw Day Marker if there are Events this day and the selected circle isn't drawn;
			} else 	if (mDayHasEvents[i]) {
				int color = isThisMonth ? mDayMarkerColor : mDayMarkerFaintColor;
				mDayMarkerPaint.setColor(color);
				mDayMarkerPaint.setStyle(Style.FILL);
				canvas.drawCircle(x, y + mDayTextSize / 2, mDayMarkerRadius, mDayMarkerPaint);
			}
			
			int textColor;
			if (isSelectedDay) {
				textColor = mSelectedDayColor;
			} else if (isToday) {
				textColor = mTodayNumberColor;
			} else if (!isThisMonth) {
				textColor = mInactiveDayTextColor;
			} else {
				textColor = mActiveDayTextColor;
			}
			Typeface tf = (isToday) ? mTypefaceBold : mTypeface;
			mMonthNumPaint.setTypeface(tf);
			mMonthNumPaint.setColor(textColor);
			
			canvas.drawText(DAYS[day], x, y, mMonthNumPaint);
		}
	}
	
////==================================================================================================
//// Logic
////==================================================================================================

	/**
	 * @return
	 */
	public boolean willHideSelectedWeek() {
		return mHideSelectedWeek;
	}
	
	/**
	 * @param hide
	 */
	public void setHideSelectedWeek(boolean hide) {
		if (mHideSelectedWeek != hide) {
			mHideSelectedWeek = hide;
			invalidate();
		}
	}

	/**
	 * Calculates the (x,y) coordinates of each day in the month.
	 */
	protected void calculateDayPoints() {
		clearDayArrays();
		final int paddingDay = (mWidth - 2 * mPadding) / (2 * mDaysPerWeek);

		for (int i = 0; i < MAX_DAYS; i++) {
			mDayXs[i] = paddingDay * (1 + 2 * (i % 7)) + mPadding;
			mDayYs[i] = (mRowHeight + mDayTextSize) / 2 - DAY_SEPARATOR_WIDTH + ( (i / 7) * mRowHeight);
			
			int day;
			
			// Previous Month
			if (isIndexInPreviousMonth(i)) {
				day = mPreviousMonthLastDayOfMonth - (mMonthStartIndex - i) + 1;
				
			// Next Month
			} else if (isIndexInNextMonth(i)) {
				day = i - mMonthEndIndex;
				
			// This Month
			} else {
				day = i - mMonthStartIndex + 1;
				
				if (mFocusedDayOfMonth == day) {
					mInvisibleWeekY = mDayYs[i];
				}
			}
			mDayOfMonths[i] = day;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarView#getXForDay(me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public int getXForDay(CalendarDay day) {
		int index = getIndexForDay(day);
		if (index == -1) {
			return 0;
		}
		return mDayXs[index];
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarView#getYForDay(me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public int getYForDay(CalendarDay day) {
		int index = getIndexForDay(day);
		if (index == -1) {
			return 0;
		}
		return mDayYs[index];
	}
	
	/**
	 * Gets the top y-position of the CalendarDay.
	 * @param day
	 * @return
	 */
	public int getTopYForDay(CalendarDay day) {
		int yMid = getYForDay(day);
		int yTop = yMid - ((mRowHeight + mDayTextSize) / 2 - DAY_SEPARATOR_WIDTH);
		return yTop;
	}

	/**
	 * @return True if the day at 'index' is in this MonthView's month.
	 */
	protected boolean isIndexInThisMonth(int index) {
		return index >= mMonthStartIndex && index <= mMonthEndIndex;
	}
	
	/**
	 * @return True if the day at 'index' is in the previous Month.
	 */
	protected boolean isIndexInPreviousMonth(int index) {
		return index < mMonthStartIndex;
	}
	
	/**
	 * @return True if the day at 'index' is in the next Month.
	 */
	protected boolean isIndexInNextMonth(int index) {
		return index > mMonthEndIndex;
	}
	
	/**
	 * @return True if the day at 'index' is in this MonthView's month
	 * and is the current Day of the year.
	 */
	protected boolean isIndexCurrentDay(int index) {
		if (!isIndexInThisMonth(index)) {
			return false;
		}
		int dayOfMonth = index - mMonthStartIndex + 1;
		return isCurrentDay(dayOfMonth);
	}
	
	/**
	 * @return The index for the CalendarDay, only if it's within this MonthView's month.
	 * 
	 * Will return -1 for a CalendarDay in previous month or next month;
	 */
	protected int getIndexForDay(CalendarDay day) {
		final int year = day.year;
		final int month = day.month;
		final int dayOfMonth = day.dayOfMonth;
		
		if (year != mYear || month != mMonth) {
			return -1;
		}
		
		int index = mMonthStartIndex + dayOfMonth -1;
		return index;
	}
	
	/**
	 * @return True if the day at 'index' is in this MonthView's month
	 * and is the CalendarViewer's selected day.
	 */
	protected boolean isIndexSelectedDay(int index) {
		if (!isIndexInThisMonth(index)) {
			return false;
		}
		int dayOfMonth = index - mMonthStartIndex + 1;
		return dayOfMonth == mSelectedDayOfMonth;
	}
	
	/**
	 * @param dayOfMonth
	 * @return True if 'dayOfMonth' for this month and year is the current day.
	 */
	protected boolean isCurrentDay(int dayOfMonth) {
		return (mYear == mCurrentYear) && (mMonth == mCurrentMonth) && (dayOfMonth == mCurrentDayOfMonth);
	}
	
	
	/**
	 * @return The day's offset.
	 */
	private int calculateDayOffset() {
		int offset = (mDayOfWeekStart < mWeekStart) ? mDayOfWeekStart + mDaysPerWeek : mDayOfWeekStart;
		return offset - mWeekStart;
	}

	/**
	 * Draws the Month name.
	 * @param canvas
	 */
	protected void drawMonthTitle(Canvas canvas) {
		final int x = (mWidth + 2 * mPadding) / 2;
		final int y = (mMonthHeaderHeight - mDayOfWeekTextSize) / 2 + mMonthTitleTextSize / 3;
		canvas.drawText(getTitle(), x, y, mMonthTitlePaint);
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarView#getTitle()
	 */
	@Override
	public String getTitle() {
		long time = mCalendar.getTimeInMillis();
		mStringBuilder.delete(0, mStringBuilder.length());
		return DateUtils.formatDateRange(getContext(), mFormatter, time, time, 52).toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarView#getDayFromLocation(float, float)
	 */
	@Override
	public CalendarDay getDayFromLocation(float x, float y) {
		int padding = this.mPadding;
		if ((x < padding) || (x > mWidth - mPadding)) {
			return null;
		}
		
		int topOffset = mDrawDayOfWeekLabels ? mMonthHeaderHeight : 0;
		if (y < topOffset) {
			return null;
		}
		
		// Get the index into the 6 * 7 day grid.
		int yDay = (int) (y - topOffset) / mRowHeight;
		int xDay = (int) ((x - padding) * this.mDaysPerWeek / (this.mWidth - padding - this.mPadding));
		
		int index = yDay * 7 + xDay;
		if (index >= MAX_DAYS) {
			return null;
		}
		
		CalendarDay day = new CalendarDay();
		
		if (isIndexInPreviousMonth(index)) {
			day.year = mPreviousMonthYear;
			day.month = mPreviousMonth;
		} else if (isIndexInNextMonth(index)) {
			day.year = mNextMonthYear;
			day.month = mNextMonth;
		} else {
			day.year = mYear;
			day.month = mMonth;
		}
		day.dayOfMonth = mDayOfMonths[index];
		return day;
	}
	
	/**
	 * Marks the MonthView to be reused.
	 */
	public void reset() {
		mNumRows = 6;
		requestLayout();
	}
	
	/**
	 * Sets the CalendarModel.
	 * @param model
	 */
	public void setModel(CalendarModel model) {
		mModel = model;
	}
	
	/**
	 * Sets the month parameters that customize the MonthView's data.
	 * @param params
	 */
	public void setParams(Map<String, Integer> params) {
		setTag(params);
		
		// Check for optional params.
		if (params.containsKey(KEY_HEIGHT)) {
			mRowHeight = params.get(KEY_HEIGHT).intValue();
			if (mRowHeight < MIN_HEIGHT) {
				mRowHeight = MIN_HEIGHT;
			}
		}
		if (params.containsKey(KEY_SELECTED_DAY)) {
			mSelectedDayOfMonth = params.get(KEY_SELECTED_DAY).intValue();
		}
		if (params.containsKey(KEY_FOCUSED_DAY)) {
			mFocusedDayOfMonth = params.get(KEY_FOCUSED_DAY).intValue();
		}
		
		// Month and Year for the MontView required.
		mMonth = params.get(KEY_MONTH).intValue();
		mYear = params.get(KEY_YEAR).intValue();
		
		// Previous Month
		mPreviousMonth = mMonth - 1;
		mPreviousMonthYear = mYear;
		if (mPreviousMonth == -1) {
			mPreviousMonth = Calendar.DECEMBER;
			mPreviousMonthYear--;
		}
		mPreviousMonthLastDayOfMonth = Utils.getDaysInMonth(mPreviousMonth, mPreviousMonthYear);
		
		// Next Month
		mNextMonth = mMonth + 1;
		mNextMonthYear = mYear;
		if (mNextMonth == Calendar.UNDECIMBER) {
			mNextMonth = Calendar.JANUARY;
			mNextMonthYear++;
		}
		
		// Current Month and Year
		mCurrentMonth = params.get(KEY_CURRENT_MONTH).intValue();
		mCurrentYear = params.get(KEY_CURRENT_YEAR).intValue();
		mCurrentDayOfMonth = params.get(KEY_CURRENT_DAY_OF_MONTH).intValue();
		
		// Time calculations.
		boolean isCurrentMonth = (mMonth == mCurrentMonth) && (mYear == mCurrentYear);
		mTodayDayOfMonth = isCurrentMonth ? mCurrentDayOfMonth : -1;
		mCalendar.set(Calendar.MONTH, mMonth);
		mCalendar.set(Calendar.YEAR, mYear);
		mCalendar.set(Calendar.DAY_OF_MONTH, 1);
		
		// Set up date grid.
		mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);
		if (params.containsKey(KEY_WEEK_START)) {
			mWeekStart = params.get(KEY_WEEK_START).intValue();
		} else {
			mWeekStart = mCalendar.getFirstDayOfWeek();
		}
		
		mMonthStartIndex = calculateDayOffset();
		mMonthEndIndex = mMonthStartIndex + Utils.getDaysInMonth(mMonth, mYear) - 1;
		
		// Mark which days have Events.
		clearHasEventsArray();
		int year;
		int month;
		int dayOfMonth;
		CalendarDay day = new CalendarDay();
		for (int i = 0; i < MAX_DAYS; i++) {
			if (isIndexInPreviousMonth(i)) {
				year = mPreviousMonthYear;
				month = mPreviousMonth;
				dayOfMonth = mPreviousMonthLastDayOfMonth - (mMonthStartIndex - i) + 1;
			} else if (isIndexInNextMonth(i)) {
				year = mNextMonthYear;
				month = mNextMonth;
				dayOfMonth = i - mMonthEndIndex;
			} else {
				year = mYear;
				month = mMonth;
				dayOfMonth = i - mMonthStartIndex + 1;
			}
			day.year = year;
			day.month = month;
			day.dayOfMonth = dayOfMonth;
			
			boolean hasEvents = mModel.hasEventsOnDay(day);
			mDayHasEvents[i] = hasEvents;
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
		}
		mInvisibleWeekY = 0;
	}	
	
	/**
	 * Zero-out hasEvents array.
	 */
	private void clearHasEventsArray() {
		for (int i = 0; i < MAX_DAYS; i++) {
			mDayHasEvents[i] = false;
		}
	}
}
