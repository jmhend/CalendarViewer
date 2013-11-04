package me.jmhend.CalendarViewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;

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
	
////============================================================================
//// Member variables.
////============================================================================
	
	private int mWidth;
	private int mHourWidth;
	private int mHourTextSize;
	private int mHourHeight;
	private int mLineHeight;
	private int mPaddingTop;
	
	private Paint mLinePaint;
	private Paint mHourPaint;
	private Paint mEventPaint;
	private Paint mEventTextPaint;
	
	
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
		mHourTextSize = 40;
		mHourHeight = 200;
		mLineHeight = 3;
		
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
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int height = mHourHeight * 24 + mPaddingTop;
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
		
		mEventPaint.setColor(0x660088CC);
		int top = 2 * mHourHeight + mPaddingTop;
		int bottom = top + mHourHeight * 2;
		canvas.drawRect(mHourWidth, top, mWidth, bottom, mEventPaint);
		
		int top2 = top + mHourHeight;
		int bottom2 = bottom + mHourHeight / 2;
		canvas.drawRect(mHourWidth, top2, mWidth, bottom2, mEventPaint);
		
		
		int top3 = top2 - mHourHeight / 4;
		int bottom3 = bottom2 + mHourHeight;
		int width = mHourWidth + (mWidth - mHourWidth) / 2;
		
		mEventPaint.setColor(0x6622AA00);
		canvas.drawRect(width, top3, mWidth, bottom3, mEventPaint);
	}
	
	/**
	 * Draws the Hours and hour lines behind the Events.
	 * @param canvas
	 */
	private void drawHoursAndLines(Canvas canvas) {
		for (int i = 0; i < 24; i++) {
			int y = i * mHourHeight + mPaddingTop;
			canvas.drawLine(mHourWidth, y, mWidth, y + mLineHeight, mLinePaint);
			canvas.drawText(getHourAtLinePosition(i), mHourWidth - 20, y + mHourTextSize / 3, mHourPaint);
		}
	}
	
	private void drawEvents(Canvas canvas) {
		
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

}
