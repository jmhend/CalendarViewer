package me.jmhend.CalendarViewer;

import java.util.Random;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class InterceptRelativeLayout extends RelativeLayout {
	
	private static final String TAG = InterceptRelativeLayout.class.getSimpleName();
	
	private VerticalSwiper mVerticalSwiper;
	
	public InterceptRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public InterceptRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public InterceptRelativeLayout(Context context) {
		super(context);
		init();
	}
	
	private void init() {
	}
	
	public void setVerticalSwiper(VerticalSwiper swiper) {
		mVerticalSwiper = swiper;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.view.ViewGroup#onInterceptTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getActionIndex()) {
		case MotionEvent.ACTION_DOWN:
			return this.onTouchEvent(ev);
		}
		return true;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mVerticalSwiper != null) {
			return mVerticalSwiper.onTouch(this, ev);
		}
		return false;
	}

}
