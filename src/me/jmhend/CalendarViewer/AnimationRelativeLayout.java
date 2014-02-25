/**
 * File:    AnimationRelativeLayout.java
 * Created: Feb 25, 2014
 * Author:	Jesse Hendrickson
 */
package me.jmhend.CalendarViewer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * AnimationRelativeLayout
 * RelativeLayout with listeners for animation start and end, because Android's shit is messed up.
 */
public class AnimationRelativeLayout extends RelativeLayout {
	
	private static final String TAG = AnimationRelativeLayout.class.getSimpleName();
	
////==============================================================================================
//// AnimationListener
////==============================================================================================
	
	/**
	 * AnimationListener
	 * Listens for Animation callbacks from this View.
	 */
	public static interface AnimationListener {
		/**
		 * Called when an Animation begins.
		 */
		public void onAnimationStart();
		/**
		 * Called when an Animation ends.
		 */
		public void onAnimationEnd();
	}
	
	
////==============================================================================================
//// Member variables.
////==============================================================================================
	
	private AnimationListener mAnimationListener;
	
////==============================================================================================
//// Constructor.
////==============================================================================================

	/**
	 */
	public AnimationRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	/**
	 */
	public AnimationRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**
	 */
	public AnimationRelativeLayout(Context context) {
		super(context);
	}
	
////==============================================================================================
//// Animation
////==============================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.view.ViewGroup#onAnimationStart()
	 */
	@Override
	protected void onAnimationStart() {
		super.onAnimationStart();
		if (mAnimationListener != null) {
			mAnimationListener.onAnimationStart();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.view.ViewGroup#onAnimationEnd()
	 */
	@Override
	protected void onAnimationEnd() {
		super.onAnimationEnd();
		if (mAnimationListener != null) {
			mAnimationListener.onAnimationEnd();
		}
	}
	
////==============================================================================================
//// Getters/Setters
////==============================================================================================
	
	/**
	 * @param l
	 */
	public void setAnimationListener(AnimationListener l) {
		mAnimationListener = l;
	}

}
