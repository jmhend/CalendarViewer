package me.jmhend.ui.calendar_viewer;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ListView;

public class SidewaysListView extends ListView {

	public SidewaysListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setRotation(-90);
	}
	
	public SidewaysListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setRotation(-90);
	}
	
	public SidewaysListView(Context context) {
		super(context);
		this.setRotation(-90);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.AbsListView#draw(android.graphics.Canvas)
	 */
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
	}
	
}