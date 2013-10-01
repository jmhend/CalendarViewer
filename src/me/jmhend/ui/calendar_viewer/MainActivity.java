package me.jmhend.ui.calendar_viewer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
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
public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();
	
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
		
//		Calendar twoYears = Calendar.getInstance();
//		twoYears.add(Calendar.YEAR, 2);
//		Date now = new Date();
//		
//		CalendarPickerView calendar = (CalendarPickerView) findViewById(R.id.calendar);
//		calendar.init(now, twoYears.getTime()).withSelectedDate(now);
//
//		calendar.setOnDateSelectedListener(new OnDateSelectedListener() {
//			/*
//			 * (non-Javadoc)
//			 * @see com.squareup.timessquare.CalendarPickerView.OnDateSelectedListener#onDateSelected(java.util.Date)
//			 */
//			@Override
//			public void onDateSelected(Date date) {
//				Toast.makeText(MainActivity.this, date.toLocaleString(), Toast.LENGTH_SHORT).show();
//			}
//			
//		});
		
		List<String> strings = new ArrayList<String>();
		for (int i = 0; i < 100; i++) {
			strings.add(i + "");
		}
		
		ListView listView = (ListView) findViewById(R.id.back_list);
		listView.setClipToPadding(false);
		listView.setPadding(0, 900, 0, 0);
		listView.setAdapter(new MyAdapter(this, strings));
		listView.setOnItemClickListener(new OnItemClickListener() {
			/*
			 * (non-Javadoc)
			 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
			 */
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String text = ((TextView) view.findViewById(R.id.text)).getText().toString() + ", " + position + "";
				Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
			}
		});

		MonthListAdapter adapter = new MonthListAdapter(this, new CalendarController() {

			@Override
			public int getFirstDayOfWeek() {
				return 2;
			}

			@Override
			public CalendarDay getStartDay() {
				return new CalendarDay(2013, Calendar.OCTOBER, 14);
			}

			@Override
			public CalendarDay getEndDay() {
				return new CalendarDay(2015, Calendar.OCTOBER, 14);
			}

			@Override
			public CalendarDay getSelectedDay() {
				Calendar now = Calendar.getInstance();
				return new CalendarDay(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
			}

			@Override
			public void onDaySelected(int year, int month, int dayOfMonth) {
				Toast.makeText(MainActivity.this, (month + 1) + "/" + dayOfMonth+ "/" + year, Toast.LENGTH_SHORT).show();
			}
			
		});
		
		float f = 0.075f;
		
		ListView list = (ListView) findViewById(R.id.list);
		list.setAdapter(adapter);
		list.setFriction(f);
		list.setVerticalScrollBarEnabled(false);
	}

	private static class MyAdapter extends ArrayAdapter<String> {

		private List<String> mStrings;
		
		public MyAdapter(Context context, List<String> objects) {
			super(context, 0, objects);
			mStrings = objects;
		}
		
		public int getCount() {
			return mStrings.size();
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem, parent, false);
			}
			
			((TextView) convertView.findViewById(R.id.text)).setText(mStrings.get(position));
			return convertView;
		}
		
	}
	
	
}
