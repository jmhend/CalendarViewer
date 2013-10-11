package me.jmhend.ui.calendar_viewer;

import me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay;
import me.jmhend.ui.calendar_viewer.CalendarView.OnDayClickListener;
import me.jmhend.ui.calendar_viewer.CalendarViewPager.OnPageSelectedListener;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

/**
 * 
 * @author jmhend
 */
public class CalendarViewer extends Fragment implements OnPageSelectedListener, OnDayClickListener {
	
	private static final String TAG = CalendarViewer.class.getSimpleName();
	
////====================================================================================
//// CalendarViewerCallbacks
////====================================================================================
	
	/**
	 * Callback methods the CalendarViewer will invoke.
	 * @author jmhend
	 */
	public static interface CalendarViewerCallbacks {
		
		public void onVisibleDaysChanged(CalendarViewer viewer);
		
		public void onDaySelected(CalendarView view, CalendarDay day);
		
		public void onDayLongPressed(CalendarView view, CalendarDay day);
		
		public void onModeChanged(CalendarViewer viewer, Mode newMode);
		
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
	
	private static final String EXTRA_CONFIG = "config";
	
////====================================================================================
//// Member variables.
////====================================================================================
	
	private CalendarViewerCallbacks mCallback;
	private DayOfWeekLabelView mDayOfWeekView;
	private CalendarViewPager mCurrentPager;
	private CalendarViewPager mWeekPager;
	private CalendarViewPager mMonthPager;
	private CalendarAdapter mMonthAdapter;
	private CalendarAdapter mWeekAdapter;
	private Mode mMode;
	
	private CalendarController mController;
	
	private int mHeight;
	private int mMaxHeight;
	private int mWeekBottom;
	
////====================================================================================
//// Constructor/Instantiation
////====================================================================================
	
	/**
	 * @return A newly instantiated CalendarViewer.
	 */
	public static CalendarViewer newInstance(CalendarControllerConfig config) {
		Bundle args = new Bundle();
		if (config == null) {
			config = CalendarControllerConfig.getDefault();
		}
		args.putParcelable(EXTRA_CONFIG, config);
		
		CalendarViewer f = new CalendarViewer();
		f.setArguments(args);
		return f;
	}
	
	/**
	 * Empty constructor.
	 */
	public CalendarViewer() { }
	
////====================================================================================
//// Fragment lifecycle.
////====================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() == null) {
			throw new IllegalStateException("CalendarViewer must be intialized with a CalendarViewerConfig.");
		}
		
		CalendarControllerConfig config = getArguments().getParcelable(EXTRA_CONFIG);
		mController = new CalendarController(config);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.calendar_viewer, container, false);
		
		mDayOfWeekView = (DayOfWeekLabelView) layout.findViewById(R.id.day_labels);
		mWeekAdapter = new WeekPagerAdapter(getActivity(), mController);
		mMonthAdapter = new MonthPagerAdapter(getActivity(), mController);
		mWeekPager = (CalendarViewPager) layout.findViewById(R.id.week_pager);
		mWeekPager.setAdapter(mWeekAdapter);
		mWeekPager.setOnPageSelectedListener(this);
		mWeekPager.setOnDayClickListener(this);
		mWeekPager.setCurrentDay(mController.getCurrentDay());
		mMonthPager = (CalendarViewPager) layout.findViewById(R.id.month_pager);
		mMonthPager.setAdapter(mMonthAdapter);
		mMonthPager.setOnPageSelectedListener(this);
		mMonthPager.setOnDayClickListener(this);
		mMonthPager.setCurrentDay(mController.getCurrentDay());
		
		initDimens();
		
		CalendarControllerConfig config = getArguments().getParcelable(EXTRA_CONFIG);
		setMode(layout, config.getMode());
		
		return layout;
	}
	
	/**
	 * Initialize CalendarViewer dimensions.
	 */
	private void initDimens() {
		Resources r = getActivity().getResources();
		int monthMaxHeight = r.getDimensionPixelOffset(R.dimen.monthview_height);
		int bottomPadding = r.getDimensionPixelSize(R.dimen.month_bottom_padding);
		int dayLabelsHeight = r.getDimensionPixelOffset(R.dimen.month_list_item_header_height);
		
		mMaxHeight = monthMaxHeight + bottomPadding;
		mWeekBottom = ((monthMaxHeight - dayLabelsHeight) / 6) + bottomPadding + dayLabelsHeight;
		mHeight = mMaxHeight;
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
				setHeight(getView(), height);
			}
		};
		animation.setAnimationListener(new AnimationListener() {
			/*
			 * (non-Javadoc)
			 * @see android.view.animation.Animation.AnimationListener#onAnimationStart(android.view.animation.Animation)
			 */
			@Override
			public void onAnimationStart(Animation animation) {
				getView().setEnabled(false);
				setMode(Mode.TRANSITION);
			}
			
			/*
			 * (non-Javadoc)
			 * @see android.view.animation.Animation.AnimationListener#onAnimationEnd(android.view.animation.Animation)
			 */
			@Override
			public void onAnimationEnd(Animation animation) {
				getView().setEnabled(true);
				setMode(mode);
			}

			/*
			 * (non-Javadoc)
			 * @see android.view.animation.Animation.AnimationListener#onAnimationRepeat(android.view.animation.Animation)
			 */
			@Override
			public void onAnimationRepeat(Animation animation) { }
			
		});
		animation.setDuration(200L);
		animation.setInterpolator(new DecelerateInterpolator());
		getView().startAnimation(animation);
	}
	
	/**
	 * Sets the height of the CalendarView from a percentage of the maximum height.
	 * @param percent
	 */
	public void setHeightPercent(float percent) {
		setHeight(getView(), (int) (percent * mMaxHeight));
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
			targetHeight = mMaxHeight;
		}
		return targetHeight;
	}
	
	/**
	 * The percentage of the content View's max height past the Week height threshold.
	 */
	private float getBelowWeekHeightPercent() {
		return ((float) (mHeight - mWeekBottom)) / ((float) (mMaxHeight - mWeekBottom));
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
	
	public void startTransition() {
		setMode(Mode.TRANSITION);
	}
	
	public void endTransition(int loc) {
		if (loc < 2) {
			setMode(Mode.CLOSED);
		} else if (loc > 70) {
			setMode(Mode.MONTH);
		} else {
			setMode(Mode.WEEK);
		}
	}
	
////====================================================================================
//// Getters/Setters
////====================================================================================
	
	/**
	 * @return The title of the currently displayed View.
	 */
	public String getTitle() {
		if (mCurrentPager != null) {
			return mCurrentPager.getCurrentItemTitle();
		}
		return "";
	}
	
	/**
	 * @param callback
	 */
	public void setCallback(CalendarViewerCallbacks callback) {
		mCallback = callback;
	}

	/**
	 * Sets the display Mode of the CalendarViewer.
	 * @param mode
	 */
	public void setMode(Mode mode) {
		setMode(getView(), mode);
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
	
////====================================================================================
//// State
////====================================================================================
	

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
	 * @see me.jmhend.ui.calendar_viewer.CalendarView.OnDayClickListener#
	 * onDayClick(me.jmhend.ui.calendar_viewer.CalendarView, me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public void onDayClick(CalendarView calendarView, CalendarDay day) {
		if (mCallback != null) {
			mWeekPager.setVisibility(View.VISIBLE);
			mCallback.onDaySelected(calendarView, day);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarView.OnDayClickListener#
	 * onDayLongClick(me.jmhend.ui.calendar_viewer.CalendarView, me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public void onDayLongClick(CalendarView calendarView, CalendarDay day) {
		if (mCallback != null) {
			mWeekPager.setVisibility(View.VISIBLE);
			mCallback.onDayLongPressed(calendarView, day);
		}	
	}
	
////====================================================================================
//// Animation
////====================================================================================

	private int calculateWeekDestinationY() {
		MonthView view = (MonthView) mMonthPager.getViewForDay(mController.getSelectedDay());
		if (view == null) {
			return 0;
		}
		int y = view.getYForDay(mController.getSelectedDay());
		int yy = y - ((view.getRowHeight() + view.getDayTextSize()) / 2 - MonthView.DAY_SEPARATOR_WIDTH);
		return yy;
	}
}
