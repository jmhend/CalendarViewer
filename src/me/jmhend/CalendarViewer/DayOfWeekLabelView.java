package me.jmhend.CalendarViewer;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class DayOfWeekLabelView extends View {
	
	private static final String TAG = DayOfWeekLabelView.class.getSimpleName();
	
////======================================================================================
//// Member variables.
////======================================================================================
	
	protected int mHeight;
	protected int mDayOfWeekTextSize;
	protected int mWidth;
	protected int mPadding = 0;
	
	protected int mDaysPerWeek = CalendarView.DEFAULT_DAYS_PER_WEEK;
	protected int mWeekStart = CalendarView.DEFAULT_FIRST_WEEKDAY;
	
	protected int mPaintColor;
	protected Paint mPaint;
	
	private String[] mWeekDays;
	
////======================================================================================
//// Constructor.
////======================================================================================

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public DayOfWeekLabelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public DayOfWeekLabelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	/**
	 * @param context
	 */
	public DayOfWeekLabelView(Context context) {
		super(context);
		init();
	}
	
////======================================================================================
//// Init.
////======================================================================================
	
	/**
	 * Initialize.
	 */
	private void init() {
		Resources r = getContext().getResources();
		mDayOfWeekTextSize = r.getDimensionPixelSize(R.dimen.day_of_week_text_size);
		mHeight = r.getDimensionPixelOffset(R.dimen.month_list_item_header_height);
		mPaintColor = r.getColor(R.color.calendar_text_title);
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(mDayOfWeekTextSize);
		mPaint.setColor(mPaintColor);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setTextAlign(Paint.Align.CENTER);
		
		mWeekDays = new DateFormatSymbols().getShortWeekdays();
		
		// Make allCaps.
		for (int i = 1; i < mWeekDays.length; i++) {
			if (!TextUtils.isEmpty(mWeekDays[i])) {
				mWeekDays[i] = mWeekDays[i].toUpperCase();
			}
		}
	}
	
////======================================================================================
//// Getters/Setters
////======================================================================================
	
	/**
	 * Weeks which Calendar.DAY_OF_WEEK is the start of the week.
	 * @param weekStart
	 */
	public void setWeekStart(int weekStart) {
		mWeekStart = weekStart;
		invalidate();
	}
	
////======================================================================================
//// Draw
////======================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		drawDayOfWeekLabels(canvas);
	}

	/**
	 * Draws the day of the week labels.
	 * @param canvas
	 */
	protected void drawDayOfWeekLabels(Canvas canvas) {
		final int y = mHeight / 2 + mDayOfWeekTextSize / 2 ;
		final int spacing = (mWidth - 2 * mPadding) / (2 * mDaysPerWeek);
		for (int i = 0; i < mDaysPerWeek; i++) {
			int dayOfWeek = (((mWeekStart - Calendar.SUNDAY) + i ) % 7) + Calendar.SUNDAY;
			int x = spacing * (1 + 2 * i) + mPadding;
			canvas.drawText(mWeekDays[dayOfWeek], x, y, mPaint);
		}
	}
	
////======================================================================================
//// Measure
////======================================================================================
	
	/**
	 * @return Max height of this View.
	 */
	public int getMaxHeight() {
		return mHeight;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeaureSpec, int heightMeasureSpec) {
		final int width = MeasureSpec.getSize(widthMeaureSpec);
		final int height = mHeight;
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
}
