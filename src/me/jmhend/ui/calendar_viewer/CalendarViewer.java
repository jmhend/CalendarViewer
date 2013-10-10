package me.jmhend.ui.calendar_viewer;

import me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay;
import me.jmhend.ui.calendar_viewer.CalendarView.OnDayClickListener;
import me.jmhend.ui.calendar_viewer.CalendarViewPager.OnPageSelectedListener;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
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
		MONTH(2);
		
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
		mMode = config.getMode();
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
		
		transitionMode(null, mMode);
		
		return layout;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
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
		transitionMode(mMode, mode);
		mMode = mode;
		
		mWeekPager.setVisibility(View.VISIBLE);
		mMonthPager.setVisibility(View.VISIBLE);
		int offset = calculateWeekDestinationY();
		Log.i(TAG, "Offset: " + offset);
		
		mWeekPager.setTranslationY(offset);
		
		if (mCallback != null) {
			mCallback.onModeChanged(this, mMode);
		}
	}
	
////====================================================================================
//// State
////====================================================================================
	
	/**
	 * Transitions the CalendarViewer from Mode to Mode.
	 * @param from
	 * @param to
	 */
	private void transitionMode(Mode from, Mode to) {
		if (from == to) {
			return;
		}
		switch (to) {
		case CLOSED:
			mWeekPager.setVisibility(View.GONE);
			mMonthPager.setVisibility(View.GONE);
			mCurrentPager = null;
			break;
		case WEEK:
			mMonthPager.setCurrentDay(mController.getSelectedDay());
			mWeekPager.setVisibility(View.VISIBLE);
			mMonthPager.setVisibility(View.GONE);
			mCurrentPager = mWeekPager;
			if (mCallback != null) {
				mCallback.onResized(this, mWeekPager.getTop(), mWeekPager.getWidth(), mWeekPager.getHeight());
			}
			break;
		case MONTH:
			mWeekPager.setCurrentDay(mController.getSelectedDay());
			mWeekPager.setVisibility(View.GONE);
			mMonthPager.setVisibility(View.VISIBLE);
			mCurrentPager = mMonthPager;
			if (mCallback != null) {
				mCallback.onResized(this, mMonthPager.getTop(), mMonthPager.getWidth(), mMonthPager.getHeight());
			}
			break;
		}
	}
	
	public void animateMyView() {
		final int max = mMonthPager.getHeight();
		final int min = mWeekPager.getHeight();
		
		final View view  = getView();
		
		
		Animation anim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				LayoutParams p = view.getLayoutParams();
				int height = (int) (max * (1f - interpolatedTime));
				p.height = height;
				view.setLayoutParams(p);
				if (mCallback != null) {
					mCallback.onResized(CalendarViewer.this, mMonthPager.getTop(), mMonthPager.getWidth(), height);
				}
		    }
		};
		anim.setDuration(20000);
		view.startAnimation(anim);
		
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
	
	int currY = 0;
	public void go() {
		int minY = 0;
		int maxY = calculateWeekDestinationY();
		Log.e(TAG, "maxY: " + maxY);
		
		float per = 0;
		if (maxY == 0) {
			per = 0;
		} else if (currY > maxY) {
			per = 0;
		} else {
			per = ((float) (currY - minY)) / ((float) (maxY - minY));
		}
		
		Log.e(TAG, "currY: " + currY);
		Log.e(TAG, "Percent: " + per);
		adjustAnimatedViews(mWeekPager, mMonthPager, minY, maxY, per);
		
		currY += 1;
	}
	
	private int monthDayY;
	
	public void changeHeight(float percentage) {
		Log.d(TAG, "ChangeHeight: " + percentage);
		final int max = mMonthPager.getTop() + mMonthPager.getHeight();
		
		View view = getView();
		LayoutParams p = view.getLayoutParams();
		int height = (int) (percentage * max);
		p.height = height;
		
		onHeightChanged(view, 0, max, height);
	}
	
	
	private void onHeightChanged(View view, int minHeight, int maxHeight, int height) {
		float percent = ((float) (height - minHeight)) / ((float) maxHeight - minHeight);
		
		int transY = (int) (percent * maxHeight);
		mWeekPager.setTranslationY(transY);
		
		mMonthPager.setAlpha(percent);
	}
	
	int y;
	int startY;
	
	private void adjustAnimatedViews(CalendarViewPager week, CalendarViewPager month, int minY, int maxY, float percentMax) {
		month.setAlpha(percentMax);
//		week.setAlpha(1f - percentMax);
		
		getView().setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
					startY = 0;
					break;
				case MotionEvent.ACTION_UP:
					startY = -1;
					break;
				case MotionEvent.ACTION_MOVE:
					y = (int) event.getY();
					int diff = Math.abs(startY - y);
					float percent = ((float) diff) / ((float) mMonthPager.getTop() + mMonthPager.getHeight() - 0);
					changeHeight(percent);
					break;
				case MotionEvent.ACTION_CANCEL:
					startY = -1;
					break;
				}
				return false;
			}
			
		});
		
		int translate = (int) (percentMax * maxY);
		week.setTranslationY(translate);
	}
	
	private int calculateWeekDestinationY() {
		if (mMode == Mode.WEEK) {
			return 0;
		}
		if (mMode == Mode.MONTH) {
			MonthView view = (MonthView) mMonthPager.getViewForDay(mController.getSelectedDay());
			if (view == null) {
				return 0;
			}
			int y = view.getYForDay(mController.getSelectedDay());
			int yy = y - ((view.getRowHeight() + view.getDayTextSize()) / 2 - MonthView.DAY_SEPARATOR_WIDTH);
			return yy;
		}
		return 0;
	}
}
