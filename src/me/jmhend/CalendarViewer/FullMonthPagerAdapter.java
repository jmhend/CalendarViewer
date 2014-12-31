package me.jmhend.CalendarViewer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jmhend on 12/30/14.
 */
public class FullMonthPagerAdapter extends MonthPagerAdapter {

    private static final String TAG = FullMonthPagerAdapter.class.getSimpleName();

////====================================================================================
//// Constructor.
////====================================================================================

    /**
     * Constructor.
     *
     * @param context
     * @param model
     * @param controller
     */
    public FullMonthPagerAdapter(Context context, CalendarModel model, CalendarController controller) {
        super(context, model, controller);
    }

////====================================================================================
//// Adapter
////====================================================================================

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        FullMonthView monthView;
        if (convertView == null) {
            monthView = new FullMonthView(mContext);
            monthView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            monthView.setClickable(true);

            if (container instanceof CalendarView.OnDayClickListener) {
                monthView.setOnDayClickListener((CalendarView.OnDayClickListener) container);
            }
        } else {
            monthView = (FullMonthView) convertView;
        }
        updateView(position, monthView);
        return monthView;
    }


    @Override
    public void updateView(int position, View view) {
        FullMonthView monthView = (FullMonthView) view;
        monthView.setModel(mModel);
        Map<String, Integer> params = (Map<String, Integer>) monthView.getTag();
        if (params == null) {
            params = new HashMap<String, Integer>();
        }

        // Generate MonthView data.
        final int month = getMonthForPosition(position);
        final int year = getYearForPosition(position);
        final int selectedDay = isSelectedDayInMonth(year, month)? mController.getSelectedDay().dayOfMonth : -1;
        final int focusedDay = isFocusedDayInMonth(year, month)? mController.getFocusedDay().dayOfMonth : -1;
        params.put(CalendarAdapter.KEY_POSITION, Integer.valueOf(position));
        params.put(MonthView.KEY_MONTH, Integer.valueOf(month));
        params.put(MonthView.KEY_YEAR, Integer.valueOf(year));
        params.put(MonthView.KEY_SELECTED_DAY, Integer.valueOf(selectedDay));
        params.put(MonthView.KEY_FOCUSED_DAY, Integer.valueOf(focusedDay));
        params.put(MonthView.KEY_WEEK_START, Integer.valueOf(mController.getFirstDayOfWeek()));
        params.put(MonthView.KEY_CURRENT_YEAR, Integer.valueOf(mController.getCurrentDay().year));
        params.put(MonthView.KEY_CURRENT_MONTH, Integer.valueOf(mController.getCurrentDay().month));
        params.put(MonthView.KEY_CURRENT_DAY_OF_MONTH, Integer.valueOf(mController.getCurrentDay().dayOfMonth));

//		monthView.reset();
        monthView.setParams(params);
        monthView.setHideFocusedWeek(false);
        monthView.invalidate();
    }

}
