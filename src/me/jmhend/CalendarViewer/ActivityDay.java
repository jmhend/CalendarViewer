package me.jmhend.CalendarViewer;

import me.jmhend.CalendarViewer.R;
import android.app.Activity;
import android.os.Bundle;

public class ActivityDay extends Activity {
	
	private static final String TAG = ActivityDay.class.getSimpleName();
	
////==============================================================================
//// Activity lifecycle.
////==============================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_day);
		
		CalendarViewPager pager = (CalendarViewPager) findViewById(R.id.day_pager);
		pager.setFadeViews(false);
		
		CalendarControllerConfig config = CalendarControllerConfig.getDefault();
		CalendarController controller = new CalendarController(config);
		
		DayPagerAdapter a = new DayPagerAdapter(this, controller);
		pager.setAdapter(a);
	}

}
