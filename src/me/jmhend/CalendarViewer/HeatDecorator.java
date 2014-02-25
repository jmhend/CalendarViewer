package me.jmhend.CalendarViewer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;

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
	
	private final Map<CalendarDay, Integer> mColorMap = Collections.synchronizedMap(new HashMap<CalendarDay, Integer>());
	
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
	public HeatDecorator(int year, int month) {
		mRadius = 48;
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.FILL);
		
		for (int i = 0; i < 31; i++) {
			Random r = new Random();
			int c = r.nextInt(10000);
			int color = colors[c % colors.length];
			
			CalendarDay day = new CalendarDay(year, month, i + 1);
			addHeat(day, color);
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
	public void addHeat(CalendarDay day, int color) {
		mColorMap.put(day, Integer.valueOf(color));
	}

////=================================================================================
//// CalendarView Decorator.
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
	public void apply(CalendarView view, Canvas canvas) {
		for (Entry<CalendarDay, Integer> entry : mColorMap.entrySet()) {
			CalendarDay day = entry.getKey();
			int color = entry.getValue().intValue();
			if (color == 0) {
				continue;
			}
			
			int x = view.getXForDay(day);
			int y = view.getYForDay(day);
			y += mRadius;
			if (y <= 0 || x <= 0) {
				return;
			}
			
			mPaint.setColor(0xFFFFFFFF);
			canvas.drawCircle(x,  y - mRadius / 3, mRadius / 3, mPaint);
		}
	}
}
