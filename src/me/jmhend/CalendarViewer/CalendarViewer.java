package me.jmhend.CalendarViewer;

import java.util.Calendar;

import me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay;
import me.jmhend.CalendarViewer.CalendarView.OnDayClickListener;
import me.jmhend.CalendarViewer.CalendarViewPager.OnPageSelectedListener;
import me.jmhend.CalendarViewer.DayView.OnEventClickListener;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

/**
 * 
 * @author jmhend
 */
public class CalendarViewer implements OnPageSelectedListener, OnDayClickListener, OnEventClickListener {
	
	private static final String TAG = CalendarViewer.class.getSimpleName();
	
////====================================================================================
//// CalendarViewerCallbacks
////====================================================================================
	
	/**
	 * Callback methods the CalendarViewer will invoke.
	 * @author jmhend
	 */
	public static interface CalendarViewerCallbacks {
		
		/**
		 * Called when the collection of displayed days changes.
		 * @param viewer
		 */
		public void onVisibleDaysChanged(CalendarViewer viewer);
		
		/**
		 * Called when a CalendarDay is selected.
		 * @param view
		 * @param day
		 */
		public void onDaySelected(View view, CalendarDay day);
		
		/**
		 * Called when a CalendarDay is long-pressed.
		 * @param view
		 * @param day
		 */
		public void onDayLongPressed(View view, CalendarDay day);
		
		/**
		 * Called when the CalendarViewer changes its mode.
		 * i.e. CLOSED <--> WEEK <--> MONTH.
		 * @param viewer
		 * @param newMode
		 */
		public void onModeChanged(CalendarViewer viewer, Mode newMode);
		
		/**
		 * Called when an Event is clicked
		 * @param view
		 * @param event
		 */
		public void onEventClick(View view, Event event);
		
		/**
		 * Called when the CalendarViewers View size changes.
		 * @param viewer
		 * @param top
		 * @param width
		 * @param height
		 */
		public void onResized(CalendarViewer viewer, int top, int width, int height);
	}
	
////====================================================================================
//// Mode
////====================================================================================
	
	/**
	 * Describes which mode the CalendarViewer is in.
	 * @author jmhend
	 */
	public static enum Mode {
		CLOSED(0),
		WEEK(1),
		MONTH(2),
		DAY(3),
		TRANSITION(4);
		
		private int mNum;
		
		private Mode(int num) {
			mNum = num;
		}
		
		public int intValue() {
			return mNum;
		}
		
		public static Mode ofValue(int num) {
			switch (num) {
			case 0: return CLOSED;
			case 1: return WEEK;
			case 2: return MONTH;
			case 3: return DAY;
			case 4: return TRANSITION;
			default: return CLOSED;
			}
		}
	}
	
////====================================================================================
//// Static constants.
////====================================================================================
	
	private static final long TRANSITION_DURATION = 200L;
	
////====================================================================================
//// Member variables.
////====================================================================================
	
	private Context mContext;
	private RelativeLayout mView;
	private CalendarViewerCallbacks mCallback;
	private CalendarViewPager mCurrentPager;
	private CalendarViewPager mWeekPager;
	private CalendarViewPager mMonthPager;
	private DayViewPager mDayPager;
	private CalendarAdapter mMonthAdapter;
	private CalendarAdapter mWeekAdapter;
	private DayPagerAdapter mDayAdapter;
	private Mode mMode;
	
	private CalendarController mController;
	private CalendarModel mModel;
	
	private int mHeight;
	private int mMonthHeight;
	private int mParentHeight;
	private int mWeekBottom;
	
	private Calendar mScratchCalendar = Calendar.getInstance();
	
////====================================================================================
//// Constructor/Instantiation
////====================================================================================

	/**
	 * Default constructor.
	 */
	public CalendarViewer(Context context, ViewGroup parent, CalendarModel model, CalendarControllerConfig config) {
		mContext = context;
		mController = new CalendarController(config);
		mModel = model;
		initView(parent, model, config);
	}
	
////====================================================================================
//// Init.
////====================================================================================
	
	/**
	 * Initialize Views.
	 * @param config
	 */
	public void initView(final ViewGroup parent, CalendarModel model, CalendarControllerConfig config) {
		mView = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.calendar_viewer, parent, false);
		
		parent.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			/*
			 * (non-Javadoc)
			 * @see android.view.ViewTreeObserver.OnGlobalLayoutListener#onGlobalLayout()
			 */
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				    parent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				} else {
					parent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}
				mParentHeight = parent.getHeight();
			}
			
		});

		mWeekAdapter = new WeekPagerAdapter(mContext, mModel, mController);
		mMonthAdapter = new MonthPagerAdapter(mContext, mModel, mController);
		mDayAdapter = new DayPagerAdapter(mContext, mModel, mController);
		
		mWeekPager = (CalendarViewPager) mView.findViewById(R.id.week_pager);
		mWeekPager.setAdapter(mWeekAdapter);
		mWeekPager.setOnPageSelectedListener(this);
		mWeekPager.setOnDayClickListener(this);
		mWeekPager.setCurrentDay(mController.getCurrentDay());
		
		mMonthPager = (CalendarViewPager) mView.findViewById(R.id.month_pager);
		mMonthPager.setAdapter(mMonthAdapter);
		mMonthPager.setOnPageSelectedListener(this);
		mMonthPager.setOnDayClickListener(this);
		mMonthPager.setCurrentDay(mController.getCurrentDay());
		
		mDayPager = (DayViewPager) mView.findViewById(R.id.day_pager);
		mDayPager.setAdapter(mDayAdapter);
		mDayPager.setOnPageSelectedListener(this);
		mDayPager.setOnDayClickListener(this);
		mDayPager.setCurrentDay(mController.getCurrentDay());
		mDayAdapter.setOnEventClickListener(this);
		
		initDimens();
		
		setMode(mView, config.getMode());
		attachToWindow(parent);
	}
	
	/**
	 * @param parent The parent ViewGroup to add this CalendarViewer to.
	 */
	public void attachToWindow(ViewGroup parent) {
		parent.addView(mView);
	}
	
	/**
	 * Initialize CalendarViewer dimensions.
	 */
	private void initDimens() {
		Resources r = mContext.getResources();
		int monthMaxHeight = r.getDimensionPixelOffset(R.dimen.monthview_height);
		int bottomPadding = r.getDimensionPixelSize(R.dimen.month_bottom_padding);
		int dayLabelsHeight = r.getDimensionPixelOffset(R.dimen.month_list_item_header_height);
		
		mMonthHeight = monthMaxHeight + bottomPadding;
		mWeekBottom = ((monthMaxHeight - dayLabelsHeight) / 6) + bottomPadding + dayLabelsHeight;
		mHeight = mMonthHeight;
	}
	
////====================================================================================
//// Transitions.
////====================================================================================
	
	/**
	 * Updates each CalendarViewer collection.
	 */
	public void updateAllCollections() {
		mMonthPager.updateVisiblePages();
		mWeekPager.updateVisiblePages();
		mDayPager.updateVisiblePages();
	}
	
	/**
	 * Transitions the CalendarViewer to the Mode.
	 */
	public void transitionMode(Mode mode) {
		transitionMode(mode, true);
	}
	
	/**
	 * Transitions the CalendarViewer to the Mode, with the possibility
	 * of smoothly doing so.
	 * @param from
	 * @param to
	 * @param smooth
	 */
	public void transitionMode(final Mode mode, boolean smooth) {
		if (mMode == mode) {
			return;
		}
		mWeekPager.setCurrentDay(mController.getSelectedDay());
		mMonthPager.setCurrentDay(mController.getSelectedDay());
		
		// Don't translate between DAY mode, just make it appear/disappear
		if (mode == Mode.DAY || mMode == Mode.DAY) {
			final Mode prevMode = mMode;
			
			// Navigating away from DAY, so updating the other CalendarViewer positions and callbacks.
			if (prevMode == Mode.DAY) {
				int height = this.getHeightForMode(mode);
				
				// Because of View timing, trick the callback into processing the onResize early,
				// so there's no View jump after the Runnable runs.
				if (mCallback != null) {
					mCallback.onResized(this, 0, mView.getWidth(), height);
				}
				
				mView.post(new Runnable() {
					/*
					 * (non-Javadoc)
					 * @see java.lang.Runnable#run()
					 */
					@Override
					public void run() {
						setMode(mode);
						mDayPager.fireCallbacksAtCurrentPosition();
						mDayPager.setVisibility(View.GONE);
					}
				});
				
			// Navigating to DAY.
			} else {
				setMode(mode);
				mDayPager.setVisibility(View.VISIBLE);
				
				mView.post(new Runnable() {
					/*
					 * (non-Javadoc)
					 * @see java.lang.Runnable#run()
					 */
					@Override
					public void run() {
						mDayPager.setCurrentDay(mController.getSelectedDay(), false);
						mDayPager.post(new Runnable() {
							/*
							 * (non-Javadoc)
							 * @see java.lang.Runnable#run()
							 */
							@Override
							public void run() {
								mDayPager.scrollCurrentViewToEventAtY();
							}
						});
					}
				});
			}
			
			return;
		}
		
		if (!smooth) {
			setMode(mode);
			return;
		}
		
		final int startHeight = mHeight;
		final int targetHeight = getHeightForMode(mode);
		
		Animation animation = new Animation() {
			/*
			 * (non-Javadoc)
			 * @see android.view.animation.Animation#applyTransformation(float, android.view.animation.Transformation)
			 */
			@Override
			public void applyTransformation(float interpolatedTime, Transformation t) {
				int height = ((int) (interpolatedTime * (targetHeight - startHeight))) + startHeight;
				setHeight(mView, height);
			}
		};
		animation.setAnimationListener(new AnimationListener() {
			/*
			 * (non-Javadoc)
			 * @see android.view.animation.Animation.AnimationListener#onAnimationStart(android.view.animation.Animation)
			 */
			@Override
			public void onAnimationStart(Animation animation) {
				mView.setEnabled(false);
				setMode(Mode.TRANSITION);
			}

			/*
			 * (non-Javadoc)
			 * @see android.view.animation.Animation.AnimationListener#onAnimationEnd(android.view.animation.Animation)
			 */
			@Override
			public void onAnimationEnd(Animation animation) {
				mView.setEnabled(true);
				setMode(mode);
			}

			/*
			 * (non-Javadoc)
			 * @see android.view.animation.Animation.AnimationListener#onAnimationRepeat(android.view.animation.Animation)
			 */
			@Override
			public void onAnimationRepeat(Animation animation) {
				mView.clearAnimation();
			}
			
		});
		animation.setDuration(TRANSITION_DURATION);
		animation.setInterpolator(new DecelerateInterpolator());
		mView.startAnimation(animation);
	}
	
	/**
	 * Calculates the y-position of the WeekView to offset itself to align with that
	 * same week in the MonthView.
	 * @return
	 */
	private int calculateWeekDestinationY() {
		MonthView view = (MonthView) mMonthPager.getViewForDay(mController.getSelectedDay());
		if (view == null) {
			return 0;
		}
		return view.getTopYForDay(mController.getSelectedDay());
	}
	
	/**
	 * Sets the height of the CalendarView from a percentage of the maximum height.
	 * @param percent
	 */
	public void setHeightPercent(float percent) {
		setHeight(mView, (int) (percent * mMonthHeight));
	}
	
	/**
	 * Sets the height of the CalendarView.
	 * @param height
	 */
	public void setHeight(View view, int height) {
		mHeight = height;
		
		LayoutParams p = view.getLayoutParams();
		p.height = mHeight;
		view.setLayoutParams(p);
		
		onHeightChanged(view, mHeight);
	}
	
	/**
	 * @param mode
	 * @return The height for the Mode.
	 */
	public int getHeightForMode(Mode mode) {
		int targetHeight = mHeight;
		if (mode == Mode.CLOSED) {
			targetHeight = 1;
		} else if (mode == Mode.WEEK) {
			targetHeight = mWeekBottom;
		} else if (mode == Mode.MONTH) {
			targetHeight = mMonthHeight;
		} else if (mode == Mode.DAY) {
			targetHeight = mParentHeight;
		}
		return targetHeight;
	}
	
	/**
	 * The percentage of the content View's max height past the Week height threshold.
	 */
	private float getBelowWeekHeightPercent() {
		return ((float) (mHeight - mWeekBottom)) / ((float) (mMonthHeight - mWeekBottom));
	}
	
	/**
	 * Called when the height of the CalendarViewer is changed.
	 * @param view
	 * @param height
	 */
	private void onHeightChanged(View view, int height) {
		float alphaWeek = 0f;
		float alphaMonth = 0f;
		int transYWeek = 0;
		boolean hideWeekInMonth = false;
		int weekVis = View.VISIBLE;
		int weekYInMonth = this.calculateWeekDestinationY();
		
		switch (mMode) {
		case CLOSED:
			break;
		case WEEK:
			transYWeek = 0;
			alphaWeek = 1f;
			alphaMonth = 0f;
			mCurrentPager = mWeekPager;
			break;
		case MONTH:
			alphaWeek = 0f;
			alphaMonth = 1f;
			hideWeekInMonth = false;
			weekVis = View.GONE;
			mCurrentPager = mMonthPager;
			break;
		case TRANSITION:
			if (height <= mWeekBottom) {
				transYWeek = 0;
				alphaWeek = 1f;
				alphaMonth = 0f;
			} else {
				float hPercent = getBelowWeekHeightPercent();
				transYWeek = (int) (hPercent * weekYInMonth);
				alphaWeek = 1f;
				alphaMonth = hPercent;
				hideWeekInMonth = true;
			}
			break;
		}
		
		// Update Views
		mWeekPager.setVisibility(weekVis);
		mWeekPager.setTranslationY(transYWeek);
		mWeekPager.setAlpha(alphaWeek);
		mMonthPager.setAlpha(alphaMonth);

		MonthView monthView = (MonthView) mMonthPager.getCurrentView();
		if (monthView != null) {
			monthView.setHideSelectedWeek(hideWeekInMonth);
		}
		
		// Notify callbacks.
		if (mCallback != null) {
			if (view != null) {
				mCallback.onResized(this, view.getTop(), view.getWidth(), mHeight);
			}
		}
	}

////====================================================================================
//// Getters/Setters
////====================================================================================
	
	/**
	 * @return The title of the currently displayed View.
	 */
	public String getTitle() {
		if (mMode == Mode.CLOSED || mMode == Mode.DAY) {
			CalendarDay day = mController.getSelectedDay();
			day.fillCalendar(mScratchCalendar);
			return Long.toString(mScratchCalendar.getTimeInMillis());
		}
		if (mCurrentPager != null) {
			return mCurrentPager.getCurrentItemTitle();
		}
		return "";
	}
	
	/**
	 * Sets the CalenderViewer's selected day.
	 * @param day
	 */
	public void setSelectedDay(CalendarDay day) {
		mController.setSelectedDay(day);
	}
	
	/**
	 * @param callback
	 */
	public void setCallback(CalendarViewerCallbacks callback) {
		mCallback = callback;
	}
	
	/**
	 * This CalendarViewer's View.
	 */
	public View getView() {
		return mView;
	}
	
	/**
	 * @return Display Mode of the CalendarViewer.
	 */
	public Mode getMode() {
		return mMode;
	}

	/**
	 * Sets the display Mode of the CalendarViewer.
	 * @param mode
	 */
	public void setMode(Mode mode) {
		setMode(mView, mode);
	}
	
	/**
	 * Sets the display Mode of the CalendarViewer.
	 * @param mode
	 */
	public void setMode(View content, Mode mode) { 
		if (mMode == mode) {
			return;
		}
		mMode = mode;
		
		int height = getHeightForMode(mode);
		setHeight(content, height);
		
		if (mCallback != null) {
			mCallback.onModeChanged(this, mMode);
		}
	}
	
	/**
	 * Sets the CalendarControllerConfig of this CalendarViewer.
	 * @param config
	 */
	public void setConfig(CalendarControllerConfig config) {
		mController = new CalendarController(config);
	}

////====================================================================================
//// OnPageSelectedListener
////====================================================================================

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarViewPager.OnPageSelectedListener#onPageSelected(android.support.v4.view.ViewPager, int)
	 */
	@Override
	public void onPageSelected(ViewPager pager, int position) {
		if (mCallback != null) {
			mCallback.onVisibleDaysChanged(this);
		}
	}
	
////====================================================================================
//// OnDayClickListener
////====================================================================================

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.CalendarView.OnDayClickListener#
	 * onDayClick(me.jmhend.CalendarViewer.CalendarView, me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public void onDayClick(View calendarView, CalendarDay day) {
		if (mCallback != null) {
			mCallback.onDaySelected(calendarView, day);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarView.OnDayClickListener#
	 * onDayLongClick(me.jmhend.ui.calendar_viewer.CalendarView, me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public void onDayLongClick(View calendarView, CalendarDay day) {
		if (mCallback != null) {
			mCallback.onDayLongPressed(calendarView, day);
		}	
	}
	
////====================================================================================
//// OnEventClickListener
////====================================================================================

	@Override
	public void onEventClick(DayView view, Event event) {
		if (mCallback != null) {
			mCallback.onEventClick(view, event);
		}
	}
}
