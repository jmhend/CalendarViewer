package me.jmhend.ui.calendar_viewer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay;
import me.jmhend.ui.calendar_viewer.CalendarView.OnDayClickListener;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Activity demonstrating usage of the CalendarViewer
 * @author jmhend
 *
 */
public class MainActivity extends Activity implements OnDayClickListener {

	private static final String TAG = MainActivity.class.getSimpleName();
	
////==========================================================================================
//// Member variables.
////==========================================================================================
	
	private MonthListView mListView;
	private CalendarViewPager mPager;
	
	private CalendarViewPager mWeekPager;
	
////==========================================================================================
//// Activity lifecycle.
////==========================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initBehindCalendarList();
		
		// MonthListAdapter setup.
		CalendarDay start = new CalendarDay(2013, Calendar.SEPTEMBER, 10);
		CalendarDay end = new CalendarDay(2014, Calendar.SEPTEMBER, 15);
		CalendarViewerConfig.Builder builder = CalendarViewerConfig.startBuilding()
				.starts(start)
				.ends(end);
		
		CalendarDay today = CalendarDay.currentDay();
		
		MonthPagerAdapter pagerAdapter = new MonthPagerAdapter(this, builder.build());
		mPager = (CalendarViewPager) findViewById(R.id.pager);
		mPager.setAdapter(pagerAdapter);
		mPager.setOnDayClickListener(this);
		mPager.setCurrentItem(pagerAdapter.getPositionForDay(today));
		
		WeekPagerAdapter weekAdapter = new WeekPagerAdapter(this, builder.build());
		mWeekPager = (CalendarViewPager) findViewById(R.id.week_pager);
		mWeekPager.setAdapter(weekAdapter);
		mWeekPager.setOnDayClickListener(this);
		mWeekPager.setCurrentDay(today);
		
//		
//		// ListView setup.
//		MonthListAdapter monthAdapter = new MonthListAdapter(this, builder.build(), this);
//		mListView = (MonthListView) findViewById(R.id.month_list);
//		mListView.setAdapter(monthAdapter);
//		mListView.postSetSelection(monthAdapter.getPositionForDay(today));

	}
	
////==========================================================================================
//// OnDayClickListener
////==========================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.OnDayClickListener#onDayClick(android.view.View, me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay)
	 */
	@Override
	public void onDayClick(CalendarView calendarView, CalendarDay day) {
//		Toast.makeText(this, "Click: " + day.toString(), Toast.LENGTH_SHORT).show();
		int x = calendarView.getXForDay(day);
		int y = calendarView.getYForDay(day);
		Log.i(TAG, day.toString() + ": " + x + ", " + y);
		mWeekPager.setCurrentDay(CalendarDay.currentDay());
	}

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.OnDayClickListener#onDayLongClick(android.view.View, me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay)
	 */
	@Override
	public void onDayLongClick(CalendarView calendarView, CalendarDay day) {
		testShit(day);
		Toast.makeText(this, "Long-click: " + day.toString(), Toast.LENGTH_SHORT).show();
	}
	
	private void testShit(CalendarDay day) {
	}
	
	
////==========================================================================================
//// BehindCalendarListAdapter
////==========================================================================================

	/**
	 * Sets up the ListView that sits behind the CalendarViewer.
	 * Used for testing the alpha, etc. of the CalendarViewer.
	 */
	private void initBehindCalendarList() {
		List<String> strings = new ArrayList<String>();
		for (int i = 0; i < 100; i++) {
			strings.add("Marketing Team Meeting");
		}
		
		ListView listView = (ListView) findViewById(R.id.behind_list);
		listView.setClipToPadding(false);
		listView.setPadding(0, 92 * 3, 0, 0);
		listView.setAdapter(new BehindCalendarListAdapter(this, strings));
		listView.setOnItemClickListener(new OnItemClickListener() {
			/*
			 * (non-Javadoc)
			 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
			 */
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			}
		});
	}
	/**
	 * Adapter for ListView that sits behind the CalendarViewer.
	 * @author jmhend
	 *
	 */
	private static class BehindCalendarListAdapter extends ArrayAdapter<String> {

		private List<String> mStrings;
		
		/**
		 * @param context
		 * @param objects
		 */
		public BehindCalendarListAdapter(Context context, List<String> objects) {
			super(context, 0, objects);
			mStrings = objects;
		}
		
		/*
		 * (non-Javadoc)
		 * @see android.widget.ArrayAdapter#getCount()
		 */
		public int getCount() {
			return mStrings.size();
		}
		
		/*
		 * (non-Javadoc)
		 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem, parent, false);
			}
			
			((TextView) convertView.findViewById(R.id.text)).setText(mStrings.get(position));
			return convertView;
		}
	}
}
