package me.jmhend.CalendarViewer;

import java.util.Calendar;

import junit.framework.Assert;
import me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay;
import me.jmhend.CalendarViewer.CalendarView.OnDayClickListener;
import me.jmhend.CalendarViewer.CalendarViewPager.OnPageSelectedListener;
import me.jmhend.CalendarViewer.DayPagerAdapter.DayTitleViewProvider;
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
import android.widget.TextView;

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
		TRANSITION(3);
		
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
			case 3: return TRANSITION;
			default: return CLOSED;
			}
		}
	}
	
////====================================================================================
//// Static constants.
////====================================================================================
	
	public static final long TRANSITION_DURATION = 300L;
	public static final long DAY_VIEW_ANIMATE_DURATION = 160L;
	
////====================================================================================
//// Member variables.
////====================================================================================
	
	private Context mContext;
	
	private RelativeLayout mLayout;
	private RelativeLayout mMutableView;
	private TextView mTitle;
	private CalendarViewPager mWeekPager;
	private CalendarViewPager mMonthPager;
	private DayViewPager mDayPager;
	private MonthPagerAdapter mMonthAdapter;
	private WeekPagerAdapter mWeekAdapter;
	private DayPagerAdapter mDayAdapter;
	private DayOfWeekLabelView mDayOfWeekLabelView;
	
	private CalendarController mController;
	private CalendarModel mModel;
	private CalendarViewerCallbacks mCallback;
	
	private Mode mMode;
	private boolean mIsDayVisible;
	
	int mMinHeight;
	int mMaxHeight;
	private int mMonthHeight;
	private int mDayHeight;
	private int mWeekHeight;
	private int mDayBottomPadding;
	
	private Calendar mScratchCalendar = Calendar.getInstance();
	
	private boolean mWasMonthGone = false;
	private boolean mWasWeekGone = false;
	
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
//// Getters/Setters
////====================================================================================
	
	public float getWeekHeight() {
		return mWeekHeight;
	}
	
	public float getMonthHeight() {
		return mMonthHeight;
	}
	
////====================================================================================
//// Init.
////====================================================================================
	
	/**
	 * Initialize Views.
	 * @param config
	 */
	public void initView(final ViewGroup parent, CalendarModel model, CalendarControllerConfig config) {
		mLayout = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.calendar_viewer, parent, false);
		mMutableView = (RelativeLayout) mLayout.findViewById(R.id.week_month_container);
		mTitle = (TextView) mLayout.findViewById(R.id.month_header);
		
		mDayOfWeekLabelView = (DayOfWeekLabelView) mMutableView.findViewById(R.id.day_labels);
		mDayOfWeekLabelView.setWeekStart(config.getFirstDayOfWeek());
		
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
				mDayHeight = parent.getHeight() - mDayBottomPadding;
				if (mDayHeight < 0) {
					mDayHeight = 0;
				}
				
				mWasWeekGone = mWeekPager.getVisibility()== View.GONE;
				mWasMonthGone = mMonthPager.getVisibility()== View.GONE;
			}
			
		});

		mWeekAdapter = new WeekPagerAdapter(mContext, mModel, mController);
		mMonthAdapter = new MonthPagerAdapter(mContext, mModel, mController);
		mDayAdapter = new DayPagerAdapter(mContext, mModel, mController);
		
		mWeekPager = (CalendarViewPager) mMutableView.findViewById(R.id.week_pager);
		mWeekPager.setAdapter(mWeekAdapter);
		mWeekPager.setOnPageSelectedListener(this);
		mWeekPager.setOnDayClickListener(this);
		mWeekPager.setCurrentDay(mController.getCurrentDay());
		
		mMonthPager = (CalendarViewPager) mMutableView.findViewById(R.id.month_pager);
		mMonthPager.setAdapter(mMonthAdapter);
		mMonthPager.setOnPageSelectedListener(this);
		mMonthPager.setOnDayClickListener(this);
		mMonthPager.setCurrentDay(mController.getCurrentDay());
		
		mDayPager = (DayViewPager) mLayout.findViewById(R.id.day_pager);
		mDayPager.setAdapter(mDayAdapter);
		mDayPager.setOnPageSelectedListener(this);
		mDayPager.setOnDayClickListener(this);
		mDayPager.setCurrentDay(mController.getCurrentDay());
		mDayPager.setPageMargin(mContext.getResources().getDimensionPixelSize(R.dimen.day_pager_margin));
		mDayAdapter.setOnEventClickListener(this);
		
		initDimens();
		
		setModeFully(config.getMode());
		attachToWindow(parent);
		
		// Initial this after CalendarViewer setup, because it relies on CalendarViewer fields to be initialized.
		final VerticalSwiper swiper = new VerticalSwiper(this, mMutableView);
		((InterceptRelativeLayout) mLayout).setVerticalSwiper(swiper);
	}
	
	/**
	 * @param parent The parent ViewGroup to add this CalendarViewer to.
	 */
	public void attachToWindow(ViewGroup parent) {
		parent.addView(mLayout);
	}
	
	/**
	 * Initialize CalendarViewer dimensions.
	 */
	private void initDimens() {
		Resources r = mContext.getResources();
		int monthMaxHeight = r.getDimensionPixelOffset(R.dimen.monthview_height);
		int monthHeaderHeight = r.getDimensionPixelOffset(R.dimen.month_header_height);
		int bottomPadding = r.getDimensionPixelSize(R.dimen.month_bottom_padding);
		int dayLabelsHeight = r.getDimensionPixelOffset(R.dimen.month_list_item_header_height);
		int dayBottomPadding = r.getDimensionPixelSize(R.dimen.dayview_bottom_padding);
		
		mMonthHeight = monthMaxHeight + bottomPadding + monthHeaderHeight;
		mWeekHeight = ((monthMaxHeight - dayLabelsHeight) / 6) + bottomPadding + dayLabelsHeight + monthHeaderHeight;
		mDayBottomPadding = dayBottomPadding;
		
		mMinHeight = mWeekHeight;
		mMaxHeight = mMonthHeight;
	}
	
	/**
	 * Update the DayView items.
	 */
	public void updateDayViewPager() {
		if (mDayPager != null) {
			mDayPager.updateVisiblePages();
		}
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
	}
	
	void ensurePagerStates() {
		mWeekPager.updateVisiblePages();
		mMonthPager.updateVisiblePages();
	}
	
	/**
	 * Animates the View with the given paramenters.
	 * @param view
	 * @param mode
	 * @param startHeight
	 * @param targetHeight
	 */
	public void animate(final View view, final Mode mode, final long duration, final int startHeight, final int targetHeight) {
		Animation animation = new Animation() {
			/*
			 * (non-Javadoc)
			 * @see android.view.animation.Animation#applyTransformation(float, android.view.animation.Transformation)
			 */
			@Override
			public void applyTransformation(float interpolatedTime, Transformation t) {
				int height = ((int) (interpolatedTime * (targetHeight - startHeight))) + startHeight;
				
//				if (mMode == Mode.TRANSITION) {
					setHeightFully(height);
					adjustViewsInTransition();
//				}
			}
		};
		animation.setAnimationListener(new AnimationListener() {
			/*
			 * (non-Javadoc)
			 * @see android.view.animation.Animation.AnimationListener#onAnimationStart(android.view.animation.Animation)
			 */
			@Override
			public void onAnimationStart(Animation animation) {
				view.setEnabled(false);
			}

			/*
			 * (non-Javadoc)
			 * @see android.view.animation.Animation.AnimationListener#onAnimationEnd(android.view.animation.Animation)
			 */
			@Override
			public void onAnimationEnd(Animation animation) {
				view.setEnabled(true);
				endTransition(mode);
			}

			/*
			 * (non-Javadoc)
			 * @see android.view.animation.Animation.AnimationListener#onAnimationRepeat(android.view.animation.Animation)
			 */
			@Override
			public void onAnimationRepeat(Animation animation) {
				view.clearAnimation();
			}
			
		});
		animation.setDuration(duration);
		animation.setInterpolator(new DecelerateInterpolator());
		view.startAnimation(animation);
	}
	
	/**
	 * Calculates the y-position of the WeekView to offset itself to align with that
	 * same week in the MonthView.
	 * @return
	 */
	private int calculateWeekDestinationY() {
		MonthView view = (MonthView) mMonthPager.getViewForDay(mController.getFocusedDay());
		if (view == null) {
			return 0;
		}
		return view.getTopYForDay(mController.getFocusedDay());
	}
	
	/**
	 * @param mode
	 * @return The height for the Mode.
	 */
	public int getHeightForMode(Mode mode) {
		int targetHeight;
		if (mode == Mode.CLOSED) {
			targetHeight = 1;
		} else if (mode == Mode.WEEK) {
			targetHeight = mWeekHeight;
		} else if (mode == Mode.MONTH) {
			targetHeight = mMonthHeight;
		} else {
			targetHeight = mMutableView.getHeight();
		}
		return targetHeight;
	}
	
	/**
	 * The percentage of the content View's max height past the Week height threshold.
	 */
	private float getBelowWeekHeightPercent() {
		return ((float) (mMutableView.getHeight() - mWeekHeight)) / ((float) (mMonthHeight - mWeekHeight));
	}
	
	/**
	 * Adjusts the View properties that are functions of a the CalendarViewer transition height.
	 */
	void adjustViewsInTransition() {
		final float heightPercent = getBelowWeekHeightPercent();
		final float monthAlpha = heightPercent;
		final int weekTransY = (int) (heightPercent * calculateWeekDestinationY());
		
		mMonthPager.setAlpha(monthAlpha);
		mWeekPager.setTranslationY(weekTransY);
	}
	
	/**
	 * Begins transition between two steady CalendarViewer states.
	 */
	void beginTransition() {
		Assert.assertTrue(mMode == Mode.WEEK || mMode == Mode.MONTH);
		if (mMode == Mode.WEEK) {
			Assert.assertTrue(mMonthPager.getVisibility() == View.GONE);
		} else if (mMode == Mode.MONTH) {
			Assert.assertTrue(mMonthPager.getVisibility() == View.VISIBLE);
		}
		
		ensurePagerStates();
		setModeFully(Mode.TRANSITION);
	}
	
	/**
	 * Ends the View transition, and settles onto the final target Mode.
	 * @param targetMode
	 */
	private void endTransition(final Mode targetMode) {
		Assert.assertTrue(targetMode == Mode.WEEK || targetMode == Mode.MONTH);
		Assert.assertTrue(mMode == Mode.TRANSITION);
		Assert.assertTrue(mMonthPager.getVisibility() == View.VISIBLE);
		
		setModeFully(targetMode);
	}
	
	/**
	 * Sets the CalendarViewer's Mode, including ensuring all View properties and state.
	 * @param mode
	 */
	public void setModeFully(final Mode mode) {
		Assert.assertTrue(mode != null);
		Assert.assertTrue(mMode != mode);
		if (mMode == mode) {
			return;
		}
		
		final Mode prevMode = mMode;
		mMode = mode;
		
		hideOrShowFocusedWeekInMonth(mode);

		if (mode == Mode.TRANSITION) {
			mWeekPager.setAlpha(1f);
			setViz(mWeekPager, View.VISIBLE);
			setViz(mMonthPager, View.VISIBLE);
			return;
		} else if (mode == Mode.WEEK) {
			setViz(mWeekPager, View.VISIBLE);
			setViz(mMonthPager, View.GONE);
			mWeekPager.setTranslationY(0);
		} else if (mode == Mode.MONTH) {
			setViz(mMonthPager, View.VISIBLE);
			mMonthPager.setAlpha(1f);
			
			if (prevMode == Mode.TRANSITION) {
				mWeekPager.animate().alpha(0f).setDuration(120);
				mWeekPager.postDelayed(new Runnable() {
					/*
					 * (non-Javadoc)
					 * @see java.lang.Runnable#run()
					 */
					@Override
					public void run() {
						mWeekPager.setAlpha(1f);
						setViz(mWeekPager, View.GONE);
					}
					
				}, 140);
			} else {
				setViz(mWeekPager, View.GONE);
			}
		}			
		
		final int height = getHeightForMode(mode);
		setHeightFully(height);
		
		if (mCallback != null) {
			mCallback.onModeChanged(this, mode);
		}
	}
	
	/**
	 * Sets the height of the mutable View.
	 * @param height
	 */
	public void setHeightFully(int height) {
		if (height < mMinHeight) {
			height = mMinHeight;
		} else if (height > mMaxHeight) {
			height = mMaxHeight;
		}
		
		LayoutParams p = mMutableView.getLayoutParams();
		if (p.height != height) {
			p.height = height;
			mMutableView.setLayoutParams(p);
		}

		onHeightSet(height);
	}
	
	/**
	 * Called when the mutable View's height is set.
	 */
	private void onHeightSet(int height) {
		mDayPager.setPadding(mDayPager.getPaddingLeft(), mMutableView.getTop() + height, mDayPager.getPaddingRight(), mDayPager.getPaddingBottom());
		
		// Notify callbacks.
		if (mCallback != null) {
			if (mMutableView != null) {
				mCallback.onResized(this, mMutableView.getTop(), mMutableView.getWidth(), height);
			}
		}
	}
	
	/**
	 * Tell the MonthView to (or not to) draw the Week of the focused Day.
	 * @param mode
	 */
	private void hideOrShowFocusedWeekInMonth(final Mode mode) {
		MonthView monthView = (MonthView) mMonthPager.getCurrentView();
		if (monthView == null) {
			return;
		}
		
		boolean hideFocusedWeekInMonth;
		if (mode == Mode.WEEK) {
			hideFocusedWeekInMonth = false; 
		} else if (mode == Mode.MONTH) {
			hideFocusedWeekInMonth = false; 
		} else if (mode == Mode.TRANSITION) {
			hideFocusedWeekInMonth = true;
		} else {
			throw new IllegalArgumentException("Invalid Mode: " + mode);
		}
		monthView.setHideFocusedWeek(hideFocusedWeekInMonth);
	}
	
	/**
	 * Sets the View.Visibility of the CalendarViewPager.
	 * @param pager
	 * @param viz
	 */
	private void setViz(CalendarViewPager pager, int viz) {
		pager.animate().cancel();
		pager.setVisibility(viz);
		if (viz == View.VISIBLE) {
			onBecomeVisible(pager);
		}
	}
	
	/**
	 * Called when a WeekPager or MonthPager becomes visible.
	 * 
	 * @param pager
	 */
	private void onBecomeVisible(CalendarViewPager pager) {
		if (!mWasMonthGone && !mWasWeekGone) {
			return;
		}
		CalendarAdapter adapter = (CalendarAdapter) pager.getAdapter();
		boolean isMonth = adapter instanceof MonthPagerAdapter;
		
		if (isMonth) {
			if (mWasMonthGone) {
				pager.post(new Runnable() {
					/*
					 * (non-Javadoc)
					 * @see java.lang.Runnable#run()
					 */
					@Override
					public void run() {
						hideOrShowFocusedWeekInMonth(mMode);
					}
				});
			}
			mWasMonthGone = false;
		} else {
			if (mWasWeekGone) {
				 
			}
			mWasWeekGone = false;
		}
	}
	
	
////====================================================================================
//// DayView
////====================================================================================
	
	/**
	 * Shows the DayView.
	 */
	public void showDayView() {
		if (mDayPager != null) {
			syncDayViewDay(false);
			mDayPager.post(new Runnable() {
				/*
				 * (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				@Override
				public void run() {
					mDayPager.setVisibility(View.VISIBLE);
					mDayPager.animate().alpha(1f).setDuration(DAY_VIEW_ANIMATE_DURATION);
					mIsDayVisible = true;
					mDayPager.post(new Runnable() {
						/*
						 * (non-Javadoc)
						 * @see java.lang.Runnable#run()
						 */
						@Override
						public void run() {
							updateDayViewPager();
							mDayPager.post(new Runnable() {
								/*
								 * 
								 */
								@Override
								public void run() {
									mDayPager.scrollCurrentViewToEventAtY();
								}
								
							});
						}
					});
				}
			});
		}
	}
	
	/**
	 * Hides the DayView.
	 */
	public void hideDayView() {
		if (mDayPager != null) {
			mDayPager.post(new Runnable() {
				/*
				 * (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				@Override
				public void run() {
					mDayPager.animate().alpha(0f).setDuration(DAY_VIEW_ANIMATE_DURATION);
				}
			});
			mDayPager.postDelayed(new Runnable() {
				/*
				 * (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				@Override
				public void run() {
					mDayPager.setVisibility(View.GONE);
					mIsDayVisible = false;
				}
			}, (long) (DAY_VIEW_ANIMATE_DURATION * 1.1));
		}
	}
	
	/**
	 * @return True if the DayView visible.
	 */
	public boolean isDayVisible() {
		return mIsDayVisible;
	}
	
	/**
	 * Syncs the DayView so that it displays the CalendarViewer's selected day.
	 */
	public void syncDayViewDay(boolean smooth) {
		mDayPager.setCurrentDay(mController.getSelectedDay(), smooth);
	}
	
	/**
	 * The DayView title View provider.
	 * @param provider
	 */
	public void setDayViewTitleProvider(DayTitleViewProvider provider) {
		if (mDayAdapter != null) {
			mDayAdapter.setDayTitleViewProvider(provider);
		}
	}
	
	/**
	 * Scrolls the DayViewPager's current DayView to its next event.
	 */
	public void scrollCurrentDayToNextEvent() {
		DayView dayView = (DayView) mDayPager.getCurrentView();
		if (dayView == null) {
			return;
		}
		int earliestY = dayView.getYForEarliestEvent();
		if (earliestY != -1) {
			mDayPager.scrollToEventAtY(dayView, earliestY);
		}
	}

////====================================================================================
//// Getters/Setters
////====================================================================================
	
	/**
	 * @return The title of the currently displayed View.
	 */
	public String getTitle() {
		if (mMode == Mode.WEEK) {
			return mWeekPager.getCurrentItemTitle();
		} else if (mMode == Mode.MONTH) {
			return mMonthPager.getCurrentItemTitle();
		}
		return "";
	}
	
	/**
	 * Sets the CalendarViewer's title.
	 * @param title
	 */
	public void setTitle(final String title) {
		mTitle.setText(title);
	}
	
	/**
	 * @return The backing CalendarController.
	 */
	public CalendarController getController() {
		return mController;
	}
	
	/**
	 * Sets the CalenderViewer's selected day.
	 * @param day
	 */
	public void setSelectedDay(CalendarDay day) {
		mController.setSelectedDay(day);
		mController.setFocusedDay(day);
	}
	
	/**
	 * @param callback
	 */
	public void setCallback(CalendarViewerCallbacks callback) {
		mCallback = callback;
	}
	
	/**
	 * This CalendarViewer's layout View.
	 */
	public View getLayout() {
		return mLayout;
	}
	
	/**
	 * @return The adjustable View.
	 */
	public View getMutableView() {
		return mMutableView;
	}
	
	/**
	 * @return Display Mode of the CalendarViewer.
	 */
	public Mode getMode() {
		return mMode;
	}

//	/**
//	 * Sets the display Mode of the CalendarViewer.
//	 * @param mode
//	 */
//	public void setMode(Mode mode) { 
//		if (mMode == mode) {
//			return;
//		}
//		mMode = mode;
//		
//		int height = getHeightForMode(mode);
//		setHeight(mMutableView, height);
//		
//		if (mCallback != null) {
//			mCallback.onModeChanged(this, mMode);
//		}
//	}
	
	/**
	 * Sets the CalendarControllerConfig of this CalendarViewer.
	 * @param config
	 */
	public void setConfig(CalendarControllerConfig config) {
		mController = new CalendarController(config);
	}
	
	/**
	 * Sets the first day of the week.
	 * @param dayOfWeek
	 */
	public void setFirstDayOfWeek(int dayOfWeek) {
		if (mController == null || mLayout == null) {
			return;
		}
		if (mController.getFirstDayOfWeek() == dayOfWeek) {
			return;
		}
		mController.setFirstDayOfWeek(dayOfWeek);
		
		mDayOfWeekLabelView.setWeekStart(dayOfWeek);
		mMonthPager.updateVisiblePages();
		mWeekPager.updateVisiblePages();
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
		if ((mMode == Mode.MONTH && pager == mMonthPager) ||
				(mMode == Mode.WEEK && pager == mWeekPager)) {
			
			final CalendarDay focus = ((CalendarAdapter) pager.getAdapter()).getFocusedDay(position);
			mController.setFocusedDay(focus);
			
			if (mMode == Mode.MONTH) {
				mWeekPager.setCurrentDay(focus, false);
			} else if (mMode == Mode.WEEK) {
				mMonthPager.setCurrentDay(focus, false);
			}
			
			mMonthPager.updateVisiblePages();
			mWeekPager.updateVisiblePages();
		}
		
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
		mController.setFocusedDay(day);
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

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.DayView.OnEventClickListener#onEventClick(me.jmhend.CalendarViewer.DayView, me.jmhend.CalendarViewer.Event)
	 */
	@Override
	public void onEventClick(DayView view, Event event) {
		if (mCallback != null) {
			mCallback.onEventClick(view, event);
		}
	}
}
