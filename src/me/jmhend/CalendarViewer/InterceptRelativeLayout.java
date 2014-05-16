package me.jmhend.CalendarViewer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class InterceptRelativeLayout extends RelativeLayout {
	
	private static final String TAG = InterceptRelativeLayout.class.getSimpleName();
	
////=======================================================================================
//// Member variables.
////=======================================================================================
	
	private VerticalSwiper mVerticalSwiper;
	
////=======================================================================================
//// Constructor.
////=======================================================================================
	
	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public InterceptRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public InterceptRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	/**
	 * @param context
	 */
	public InterceptRelativeLayout(Context context) {
		super(context);
		init();
	}
	
////=======================================================================================
//// Init.
////=======================================================================================
	
	/**
	 * Common init.
	 */
	private void init() {
	}
	
	
////=======================================================================================
//// Getters/Setters
////=======================================================================================
	
	/**
	 * @param swiper
	 */
	public void setVerticalSwiper(VerticalSwiper swiper) {
		mVerticalSwiper = swiper;
	}
	
////=======================================================================================
//// Touch
////=======================================================================================
	
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
