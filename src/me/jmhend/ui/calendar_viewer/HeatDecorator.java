package me.jmhend.ui.calendar_viewer;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.View;

/**
 * CalendarViewDecorator that adds heat colors to the CalendarViewer.
 * 
 * @author jmhend
 */
public class HeatDecorator implements CalendarViewerDecorator {
	
	private static final String TAG = HeatDecorator.class.getSimpleName();
	
////=================================================================================
//// Static constants.
////=================================================================================
	
////=================================================================================
//// Member variables.
////=================================================================================
	
	private int mRadius;
	private Paint mPaint;
	private final int[] mDayColors = new int[MonthView.MAX_DAYS];
	
	int colors[] = {
		0x00000000,
		0x00000000,
		0xBB7cd929,
		0xBB7cd929,
		0xBB7cd929,
		0xBBf43838,
		0xBBff9000,
		0xBBfec517,
		0x00000000,
		0x00000000,
		0x00000000,
	};
	
////=================================================================================
//// Constructor.
////=================================================================================
	
	/**
	 * Construct.
	 */
	public HeatDecorator() {
		mRadius = 48;
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.FILL);
		
		for (int i = 0; i < mDayColors.length; i++) {
			Random r = new Random();
			int c = r.nextInt(10000);
			int color = colors[c % colors.length];
			addHeat(i + 1, color);
		}
	}
	
////=================================================================================
//// Getters/Setters
////=================================================================================
	
	/**
	 * Sets the radius of the heat circle.
	 * @param radius
	 */
	public void setRadius(int radius) {
		mRadius = radius;
	}
	
	/**
	 * Adds a heat indicator color for 'day'.
	 * @param day
	 * @param color
	 */
	public void addHeat(int day, int color) {
		mDayColors[day-1] = color;
	}

////=================================================================================
//// MonthView Decorator.
////=================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarViewerDecorator#getApplyLevel()
	 */
	@Override
	public ApplyLevel getApplyLevel() {
		return ApplyLevel.BELOW;
	}

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarViewerDecorator#apply(android.view.View, android.graphics.Canvas)
	 */
	@Override
	public void apply(View view, Canvas canvas) {
		if (!(view instanceof MonthView)) {
			return;
		}
		MonthView monthView = (MonthView) view;
		for (int day = 1; day <= mDayColors.length; day++) {
			if (mDayColors[day-1] == 0x00000000) {
				continue;
			}
			if (!monthView.isDayActive(day)) {
				continue;
			}
			int x = monthView.getXPointForDay(day);
			int y = monthView.getYPointForDay(day);
			if (x == 0 && y == 0) {
				continue;
			}
			mPaint.setColor(mDayColors[day-1]);
			canvas.drawCircle(x,  y - mRadius / 3, mRadius, mPaint);
		}
	}

}
