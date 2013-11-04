package me.jmhend.CalendarViewer;

import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;

import me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay;
import me.jmhend.CalendarViewer.CalendarViewerDecorator.ApplyLevel;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
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
	
	public static final int MAX_DAYS = 31;
	
	public static final String KEY_MONTH = "month";
	public static final String KEY_YEAR = "year";
	public static final String KEY_SELECTED_DAY = "selected_day";
	
////==================================================================================================
//// Member variables.
////==================================================================================================
	
	// Dimen
	protected int mMaxHeight;
	
	// Time
	protected int mMonth;
	protected int mYear;
	protected int mTodayOfWeek;
	protected int mSelectedDayOfWeek;
	protected int mCurrentYear;
	protected int mCurrentMonth;
	protected int mCurrentDayOfMonth;
	
	// Draw calculations.
	private final int[] mDayXs = new int[MAX_DAYS];
	private final int[] mDayYs = new int[MAX_DAYS];
	private final boolean[] mDayActives = new boolean[MAX_DAYS];
	private boolean mHideSelectedWeek = false;
	private int mSelectedDayY = 0;
	
	private StringBuilder mStringBuilder;
	private Formatter mFormatter;

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
	protected void initResources() {
		super.initResources();
		mStringBuilder = new StringBuilder(50);
		mFormatter = new Formatter(mStringBuilder, Locale.getDefault());
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
		for (int day = 1; day <= mNumCells; day++) {
			final int x = mDayXs[day-1];
			final int y = mDayYs[day-1];
			
			// Hiding this week.
			if (mHideSelectedWeek && y == mSelectedDayY) {
				continue;
			}
			
			// Show the day as selected.
			
			if (mSelectedDayOfWeek == day) {
				canvas.drawCircle(x,  y - mDayTextSize / 3, mSelectedCircleRadius, mSelectedCirclePaint);
			}
			
			int textColor;
			if (mSelectedDayOfWeek == day) {
				textColor = mSelectedDayColor;
			} else if (mTodayOfWeek == day) {
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
		int y = (mRowHeight + mDayTextSize) / 2 - DAY_SEPARATOR_WIDTH;
		int paddingDay = (mWidth - 2 * mPadding) / (2 * mDaysPerWeek);
		int dayOffset = findDayOffset();
		int day = 1;
		
		while (day <= mNumCells) {
			int x = paddingDay * (1 + 2 * dayOffset) + mPadding;
			mDayXs[day-1] = x;
			mDayYs[day-1] = y;
			
			if (mSelectedDayOfWeek == day) {
				mSelectedDayY = y;
			}
			
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

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarView#getXForDay(me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public int getXForDay(CalendarDay day) {
		return mDayXs[day.dayOfMonth-1];
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarView#getYForDay(me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public int getYForDay(CalendarDay day) {
		return mDayYs[day.dayOfMonth-1];
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
		
		int yDay = (int) (y - topOffset) / mRowHeight;
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
		if (params.containsKey(KEY_SELECTED_DAY)) {
			mSelectedDayOfWeek = ((Integer) params.get(KEY_SELECTED_DAY)).intValue();
		}
		
		// Month and Year for the MontView required.
		mMonth = ((Integer) params.get(KEY_MONTH)).intValue();
		mYear = ((Integer) params.get(KEY_YEAR)).intValue();
		
		// Current Month and Year
		mCurrentMonth = ((Integer) params.get(KEY_CURRENT_MONTH)).intValue();
		mCurrentYear = ((Integer) params.get(KEY_CURRENT_YEAR)).intValue();
		mCurrentDayOfMonth = ((Integer) params.get(KEY_CURRENT_DAY_OF_MONTH)).intValue();
		
		// Time calculations.
		boolean isCurrentMonth = (mMonth == mCurrentMonth) && (mYear == mCurrentYear);
		mTodayOfWeek = isCurrentMonth ? mCurrentDayOfMonth : -1;
		mCalendar.set(Calendar.MONTH, mMonth);
		mCalendar.set(Calendar.YEAR, mYear);
		mCalendar.set(Calendar.DAY_OF_MONTH, 1);
		
		// Set up date grid.
		mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);
		if (params.containsKey(KEY_WEEK_START)) {
			mWeekStart = ((Integer) params.get(KEY_WEEK_START)).intValue();
		} else {
			mWeekStart = mCalendar.getFirstDayOfWeek();
		}
		mNumCells = Utils.getDaysInMonth(mMonth, mYear);
		mNumRows = calculateNumRows();
		
		mMaxHeight = mRowHeight * mNumRows + mBottomPadding;
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
