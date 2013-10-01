package me.jmhend.ui.calendar_viewer;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay;
import me.jmhend.ui.calendar_viewer.MonthViewDecorator.ApplyLevel;
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
	protected static final int DEFAULT_HEIGHT = 32;
	protected static final int MIN_HEIGHT = 10;
	
	public static final String KEY_MONTH = "month";
	public static final String KEY_YEAR = "year";
	public static final String KEY_HEIGHT = "height";
	public static final String KEY_SELECTED_DAY = "selected_day";
	public static final String KEY_WEEK_START = "week_start";
	
	private static final String FORMAT_DAY = "%d";
	
////==================================================================================================
//// OnDayClickListener
////==================================================================================================
	
	/**
	 * Listens for TouchEvents on MonthView days.
	 * 
	 * @author jmhend
	 *
	 */
	public static interface OnDayClickListener {
		
		/**
		 * Called when a day is clicked on monthView.
		 * @param monthView
		 * @param day
		 */
		public void onDayClick(MonthView monthView, CalendarDay day);
		
		/**
		 * Called when a day is long-clicked on monthView.
		 * @param monthView
		 * @param day
		 */
		public void onDayLongClick(MonthView monthView, CalendarDay day);
	}
	
////==================================================================================================
//// Member variables.
////==================================================================================================
	
	protected int DAY_SELECTED_CIRCLE_SIZE;
	protected int MINI_DAY_NUMBER_TEXT_SIZE;
	protected int MONTH_DAY_LABEL_TEXT_SIZE;
	protected int MONTH_HEADER_SIZE;
	protected int MONTH_LABEL_TEXT_SIZE;
	protected int BOTTOM_PADDING;
	
	private Calendar mCalendar;
	private Calendar mDayLabelCalendar;
	private int mDayOfWeekStart = 0;
	private String mDayOfWeekTypeface;
	protected int mDayTextColor;
	protected boolean mHasToday = false;
	protected int mMonth;
	protected Paint mMonthDayLabelPaint;
	protected Paint mMonthNumPaint;
	protected int mMonthTitleColor;
	protected Paint mMonthTitlePaint;
	private String mMonthTitleTypeface;
	protected int mNumCells = this.mNumDays;
	protected int mNumDays = 7;
	private int mNumRows = 6;
	protected int mPadding = 0;
	protected int mRowHeight = DEFAULT_HEIGHT;
	protected Paint mSelectedCirclePaint;
	protected int mSelectedDay = -1;
	private StringBuilder mStringBuilder;
	protected int mToday = -1;
	protected int mTodayNumberColor;
	protected int mWeekStart = 1;
	protected int mWidth;
	protected int mYear;
	private DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols();
	
	private int mSelectedCircleColor;
	
	private OnDayClickListener mOnDayClickListener;
	private List<MonthViewDecorator> mDecorators = new ArrayList<MonthViewDecorator>();
	
	private float mLastTouchX;
	private float mLastTouchY;

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
		Resources resources = getContext().getResources();
			
		mCalendar = Calendar.getInstance();
		mDayLabelCalendar = Calendar.getInstance();
		
		mDayOfWeekTypeface = "";
		mMonthTitleTypeface = "";
		
		mDayTextColor = 0xAAFFFFFF;
		mTodayNumberColor = 0xFFFFFF66;
		mMonthTitleColor = 0xDDFFFFFF;
		mSelectedCircleColor = 0x3CFFFFFF;
		
		mStringBuilder = new StringBuilder(50);
		
		MINI_DAY_NUMBER_TEXT_SIZE = resources.getDimensionPixelSize(R.dimen.day_number_size);
		MONTH_LABEL_TEXT_SIZE = resources.getDimensionPixelSize(R.dimen.month_label_size);
		MONTH_DAY_LABEL_TEXT_SIZE = resources.getDimensionPixelSize(R.dimen.month_day_label_text_size);
		MONTH_HEADER_SIZE = resources.getDimensionPixelOffset(R.dimen.month_list_item_header_height);
		DAY_SELECTED_CIRCLE_SIZE = resources.getDimensionPixelSize(R.dimen.day_number_select_circle_radius);
		BOTTOM_PADDING = resources.getDimensionPixelSize(R.dimen.month_bottom_padding);
		
		mRowHeight = ((resources.getDimensionPixelOffset(R.dimen.date_picker_view_animator_height) - MONTH_HEADER_SIZE) / 6);

		this.setOnClickListener(new OnClickListener() {
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
		
		this.setOnLongClickListener(new OnLongClickListener() {
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

	/**
	 * Initialize View components.
	 */
	protected void initView() {
		this.mMonthTitlePaint = new Paint();
		this.mMonthTitlePaint.setFakeBoldText(true);
		this.mMonthTitlePaint.setAntiAlias(true);
		this.mMonthTitlePaint.setTextSize(MONTH_LABEL_TEXT_SIZE);
		this.mMonthTitlePaint.setTypeface(Typeface.create(this.mMonthTitleTypeface, 1));
		this.mMonthTitlePaint.setColor(mMonthTitleColor);
		this.mMonthTitlePaint.setTextAlign(Paint.Align.CENTER);
		this.mMonthTitlePaint.setStyle(Paint.Style.FILL);
		this.mSelectedCirclePaint = new Paint();
		this.mSelectedCirclePaint.setFakeBoldText(true);
		this.mSelectedCirclePaint.setAntiAlias(true);
		this.mSelectedCirclePaint.setColor(mSelectedCircleColor);
		this.mSelectedCirclePaint.setTextAlign(Paint.Align.CENTER);
		this.mSelectedCirclePaint.setStyle(Paint.Style.FILL);
		this.mMonthDayLabelPaint = new Paint();
		this.mMonthDayLabelPaint.setAntiAlias(true);
		this.mMonthDayLabelPaint.setTextSize(MONTH_DAY_LABEL_TEXT_SIZE);
		this.mMonthDayLabelPaint.setColor(this.mMonthTitleColor);
		this.mMonthDayLabelPaint.setTypeface(Typeface.create(this.mDayOfWeekTypeface, 0));
		this.mMonthDayLabelPaint.setStyle(Paint.Style.FILL);
		this.mMonthDayLabelPaint.setTextAlign(Paint.Align.CENTER);
		this.mMonthDayLabelPaint.setFakeBoldText(true);
		this.mMonthNumPaint = new Paint();
		this.mMonthNumPaint.setAntiAlias(true);
		this.mMonthNumPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
		this.mMonthNumPaint.setStyle(Paint.Style.FILL);
		this.mMonthNumPaint.setTextAlign(Paint.Align.CENTER);
		this.mMonthNumPaint.setFakeBoldText(false);
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
		final int height = mRowHeight * mNumRows + MONTH_HEADER_SIZE + BOTTOM_PADDING;
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
		final int y = (MONTH_HEADER_SIZE - MONTH_DAY_LABEL_TEXT_SIZE) / 2 + MONTH_LABEL_TEXT_SIZE / 3;
		canvas.drawText(getMonthTitleString(), x, y, mMonthTitlePaint);
	}
	
	/**
	 * Draws the day of the week labels.
	 * @param canvas
	 */
	protected void drawDayOfWeekLabels(Canvas canvas) {
		final int y = MONTH_HEADER_SIZE - MONTH_DAY_LABEL_TEXT_SIZE / 2;
		final int spacing = (mWidth - 2 * mPadding) / (2 * mNumDays);
		for (int day = 0; day < mNumDays; day++) {
			int dayOfWeek = (day + mWeekStart) % mNumDays;
			int x = spacing * (1 + 2 * day) + mPadding;
			mDayLabelCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
			String label = mDateFormatSymbols.getShortWeekdays()[mDayLabelCalendar.get(Calendar.DAY_OF_WEEK)].toUpperCase(Locale.getDefault());
			canvas.drawText(label, x, y, mMonthDayLabelPaint);
		}
	}
	
	/**
	 * Draws each date in the month.
	 * @param canvas
	 */
	protected void drawDates(Canvas canvas) {
		int y = (mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2 - DAY_SEPARATOR_WIDTH + MONTH_HEADER_SIZE;
		int paddingDay = (mWidth - 2 * mPadding) / (2 * mNumDays);
		int dayOffset = findDayOffset();
		int day = 1;
		
		while (day <= mNumCells) {
			int x = paddingDay * (1 + 2 * dayOffset) + mPadding;
			if (mSelectedDay == day) {
				canvas.drawCircle(x,  y - MINI_DAY_NUMBER_TEXT_SIZE / 3, DAY_SELECTED_CIRCLE_SIZE, mSelectedCirclePaint);
			}
			mMonthNumPaint.setColor((mHasToday && mToday == day) ? mTodayNumberColor : mDayTextColor);
			canvas.drawText(String.format(FORMAT_DAY, day), x, y, mMonthNumPaint);
			
			// Reached the end of the week, start drawing on the next line.
			dayOffset++;
			if (dayOffset == mNumDays) {
				dayOffset = 0;
				y+= mRowHeight;
			}
			day++;
		}
	}
	
////==================================================================================================
//// Logic
////==================================================================================================
	
	/**
	 * True if monthDay and time represent the same day.
	 * 
	 * @param monthDay
	 * @param time
	 * @return
	 */
	protected boolean isSameDay(int monthDay, Time time) {
		return (mYear == time.year) && (mMonth == time.month) && (monthDay == time.monthDay);
	}
	
	/**
	 * @return The day's offset.
	 */
	private int findDayOffset() {
		int offset = (mDayOfWeekStart < mWeekStart) ? mDayOfWeekStart + mNumDays : mDayOfWeekStart;
		return offset - mWeekStart;
	}
	
	/**
	 * @return How many rows are needed to draw all dates in this month.
	 */
	private int calculateNumRows() {
		int dayOffset = findDayOffset();
		int numRows = (dayOffset + mNumCells) / mNumDays;
		int plusOne = ((dayOffset + mNumCells) % mNumDays > 0) ? 1 : 0;
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
		
		if (y < MONTH_HEADER_SIZE) {
			Log.e(TAG, "y-pos " + y + " out of bounds");
			return null;
		}
		
		int yDay = (int) (y - MONTH_HEADER_SIZE) / mRowHeight;
		int day = 1 + ((int) ((x - padding) * this.mNumDays / (this.mWidth - padding - this.mPadding)) - findDayOffset()) + yDay * this.mNumDays;
		
		// Check day bounds.
		if (day < 1 || day > Utils.getDaysInMonth(mMonth, mYear)) {
			Log.e(TAG, "day " + day + " is out of bounds");
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
			mSelectedDay = ((Integer) monthParams.get(KEY_SELECTED_DAY)).intValue();
		}
		
		// Month && Year required.
		mMonth = ((Integer) monthParams.get(KEY_MONTH)).intValue();
		mYear = ((Integer) monthParams.get(KEY_YEAR)).intValue();
		
		// Reset fields.
		Time time = new Time(Time.getCurrentTimezone());
		time.setToNow();
		mHasToday = false;
		mToday = -1;
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
		
		// Look for "today"
		for (int day = 0; day < mNumCells; day++) {
			int monthDay = day + 1;
			if (isSameDay(monthDay, time)) {
				mHasToday = true;
				mToday = monthDay;
			}
		}
	}
	
////==================================================================================================
//// Decorators
////==================================================================================================
	
	/**
	 * Adds a MonthViewDecorator to the MonthView.
	 * Decorators will be applied in the order that they're added.
	 * @param decorator
	 */
	public void addDecorator(MonthViewDecorator decorator) {
		mDecorators.add(decorator);
	}
	
	/**
	 * Removes the MonthViewDecorator.
	 * @param decorator
	 */
	public void removeDecorator(MonthViewDecorator decorator) {
		mDecorators.remove(decorator);
	}
	
	/**
	 * Applies all deocorates, in added order, whose AppyLevel == level.
	 * @param level
	 */
	private void applyDecorators(Canvas canvas, ApplyLevel level) {
		for (MonthViewDecorator decorator : mDecorators) {
			if (decorator.getApplyLevel() == level) {
				decorator.apply(this, canvas);
			}
		}
	}
	
}
