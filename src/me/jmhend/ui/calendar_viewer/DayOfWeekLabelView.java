package me.jmhend.ui.calendar_viewer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

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
		for (int day = 0; day < mDaysPerWeek; day++) {
			int dayOfWeek = (day + mWeekStart) % mDaysPerWeek;
			int x = spacing * (1 + 2 * day) + mPadding;
			canvas.drawText(CalendarView.WEEKDAYS[dayOfWeek], x, y, mPaint);
		}
	}
	
////======================================================================================
//// Measure
////======================================================================================
	
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
