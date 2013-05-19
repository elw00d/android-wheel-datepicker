package su.elwood;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import junit.framework.Assert;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * @author igor.kostromin
 * 16.05.13 9:03
 *
 * Custom datepicker based on wheel control.
 */
public class WheelDatePicker extends LinearLayout {

    public static final int DEFAULT_VISIBLE_ITEMS = 3;

    public static final int DEFAULT_MIN_YEAR = 1900;
    public static final int DEFAULT_MAX_YEAR = 2050;

    public static final int DEFAULT_YEAR = 2000;
    public static final int DEFAULT_DAY = 15;
    public static final int DEFAULT_MONTH = 7;

    // state
    private int minYear;
    private int maxYear;

    // last selected manually day, remembered for correct restore selected day while user scrolls months list
    private int lastSelectedDay;

    // flag to disable selected day change listener
    private boolean dayChangeListenerDisabled;

    // wheel controls to select birth date
    private WheelView wheelYear;
    private WheelView wheelMonth;
    private WheelView wheelDay;


    public static interface IDateChangedListener {
        void onChanged(WheelDatePicker sender, int oldDay, int oldMonth, int oldYear,
                       int day, int month, int year);
    }

    private List<IDateChangedListener> dateChangedListeners = new ArrayList<IDateChangedListener>(  );

    public void addDateChangedListener( IDateChangedListener listener) {
        if (null == listener) throw new IllegalArgumentException( "listener is null" );
        Assert.assertTrue( !dateChangedListeners.contains( listener ));
        dateChangedListeners.add( listener );
    }

    public void removeDateChangedListener( IDateChangedListener listener ) {
        if (null == listener ) throw new IllegalArgumentException( "listener is null" );
        Assert.assertTrue( dateChangedListeners.contains( listener ) );
        dateChangedListeners.remove( listener );
    }

    private void raiseDateChangedEvent( int oldDay, int oldMonth, int oldYear, int day, int month, int year) {
        if (!dateChangedListeners.isEmpty()) {
            List<IDateChangedListener > copy = new ArrayList<IDateChangedListener>( dateChangedListeners );
            for ( IDateChangedListener listener : copy ) {
                listener.onChanged( this, oldDay, oldMonth, oldYear, day, month, year );
            }

        }
    }

    public WheelDatePicker( Context context ) {
        super( context );
        init(context );
    }

    public WheelDatePicker( Context context, AttributeSet attrs ) {
        super( context, attrs );
        init(context );
    }

    public WheelDatePicker( Context context, AttributeSet attrs, int defStyle ) {
        super( context, attrs, defStyle );
        init(context );
    }

    public int getMinYear() {
        return minYear;
    }

    /**
     * Use this method to set years interval that will be awailable.
     * @param minYear
     * @param maxYear
     */
    public void setMinMaxYears( int minYear, int maxYear ) {
        if (minYear < 0) throw new IllegalArgumentException( "minYear" );
        if (maxYear < 0) throw new IllegalArgumentException( "maxYear" );
        if (minYear > maxYear) throw new IllegalArgumentException( "minYear should be <= maxYear" );
        //
        if (this.minYear != minYear || this.maxYear != maxYear) {
            this.minYear = minYear;
            this.maxYear = maxYear;

            // reinit wheelYear
            int year = getYear();
            wheelYear.setViewAdapter( new NumericWheelAdapter( this.getContext(), minYear, maxYear ) );
            if (year >= minYear && year <= maxYear) {
                setYear( year );
            } else {
                setYear(minYear);
            }
        }
    }

    public int getMaxYear() {
        return maxYear;
    }

    /**
     * Returns selected day.
     * @return
     */
    public int getDay() {
        return wheelDay.getCurrentItem() + 1;
    }

    /**
     * Returns selected month.
     * @return
     */
    public int getMonth() {
        return wheelMonth.getCurrentItem() + 1;
    }

    /**
     * Returns selected year.
     * @return
     */
    public int getYear() {
        return wheelYear.getCurrentItem() + minYear;
    }

    public void setDay(int day) {
        if ( day < 1 || day > 31) throw new IllegalArgumentException( "day should be between 1 and 31" );
        int month = getMonth();
        int year = getYear();
        int daysInMonth = getDaysCountInMonth( year, month );
        int dayToSet = Math.min( day, daysInMonth );
        wheelDay.setCurrentItem( dayToSet - 1 );
    }

    public void setMonth( int month) {
        if (month < 1 || month > 12) throw new IllegalArgumentException( "month should be between 1 and 12" );
        wheelMonth.setCurrentItem( month - 1 );
    }

    public void setYear( int year ) {
        if (year < minYear || year > maxYear) throw new IllegalArgumentException( "year should be between minYear and maxYear" );
        wheelYear.setCurrentItem( year - minYear );
    }

    /**
     * Gets count of visible items
     * @return
     */
    public int getVisibleItems() {
        return wheelDay.getVisibleItems();
    }

    /**
     * Sets the desired count of visible items.
     * @param count
     */
    public void setVisibleItems( int count ) {
        wheelDay.setVisibleItems( count );
        wheelMonth.setVisibleItems( count );
        wheelYear.setVisibleItems( count );

        // trigger remeasuring
        this.requestLayout();
    }

    /**
     * Returns the current locale used.
     * @return
     */
    public Locale getLocale() {
        return ((MonthsAdapter) wheelMonth.getViewAdapter()).locale;
    }

    /**
     * Set locale (needed for months wheel).
     * Supported locales: "en-US", "ru-RU".
     * @param locale for example new Locale("ru", "RU");
     */
    public void setLocale(Locale locale) {
        if (null == locale) throw new IllegalArgumentException( "locale is null" );
        if (!MonthsAdapter.isLocaleSupported( locale ))
            throw new IllegalArgumentException( "Specified locale is not supported yet." );
        //
        ((MonthsAdapter) wheelMonth.getViewAdapter()).setLocale( locale);
        wheelMonth.invalidate();
    }

    private void init( final Context ctx) {
        LayoutInflater inflater = ( LayoutInflater ) ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        inflater.inflate( R.layout.wheel_datepicker, this, true );

        minYear = DEFAULT_MIN_YEAR;
        maxYear = DEFAULT_MAX_YEAR;

        dayChangeListenerDisabled = false;
        lastSelectedDay = DEFAULT_DAY;

        wheelYear = ( WheelView ) findViewById( R.id.wheelYear );
        wheelYear.setTag( "wheelYear" ); // to debug inside wheel control code, can be safely removed
        wheelYear.setViewAdapter( new NumericWheelAdapter( ctx, minYear, maxYear ) );
        wheelYear.setCurrentItem( DEFAULT_YEAR - minYear );
        wheelYear.setVisibleItems( DEFAULT_VISIBLE_ITEMS );
        wheelYear.addChangingListener( new OnWheelChangedListener() {
            public void onChanged( WheelView wheel, int oldValue, int newValue ) {
                int oldDay = getDay();
                // special logic for February
                int oldYear = oldValue + minYear;
                int newYear = newValue + minYear;
                GregorianCalendar gregorianCalendar = new GregorianCalendar();
                boolean leapOldYear = gregorianCalendar.isLeapYear( oldYear );
                boolean leapNewYear = gregorianCalendar.isLeapYear( newYear );
                if ( leapNewYear != leapOldYear ) {
                    int month = wheelMonth.getCurrentItem() + 1;
                    if ( 2 == month ) {
                        int daysCount = getDaysCountInMonth( newYear, 2 );
                        int daysCountPrev = getDaysCountInMonth( oldYear, 2 );
                        int currentDay = wheelDay.getCurrentItem() + 1;
                        wheelDay.setViewAdapter( new NumericWheelAdapter( ctx, 1, daysCount ) );
                        updateWheelDay( daysCount, daysCountPrev, currentDay, lastSelectedDay );
                    }
                }
                int newDay = getDay();
                int month  = getMonth();
                raiseDateChangedEvent( oldDay, month, oldYear, newDay, month, newYear );
            }
        } );

        wheelMonth = ( WheelView ) findViewById( R.id.wheelMonth );
        wheelMonth.setTag( "wheelMonth" );
        wheelMonth.setViewAdapter( new MonthsAdapter( ctx ) );
        wheelMonth.setCurrentItem( DEFAULT_MONTH - 1 );
        wheelMonth.setVisibleItems( DEFAULT_VISIBLE_ITEMS );
        wheelMonth.addChangingListener( new OnWheelChangedListener() {
            public void onChanged( WheelView wheel, int oldValue, int newValue ) {
                int oldDay = getDay();
                //
                int year = wheelYear.getCurrentItem() + minYear;
                int oldMonth = oldValue + 1;
                int newMonth = newValue + 1;
                int daysCount = getDaysCountInMonth( year, newMonth );
                int daysCountPrev = getDaysCountInMonth( year, oldMonth );
                if ( daysCountPrev != daysCount ) {
                    int currentDay = wheelDay.getCurrentItem() + 1;
                    wheelDay.setViewAdapter( new NumericWheelAdapter( ctx, 1, daysCount ) );
                    updateWheelDay( daysCount, daysCountPrev, currentDay, lastSelectedDay );
                }
                //
                int newDay = getDay();
                raiseDateChangedEvent( oldDay, oldMonth, year, newDay, newMonth, year);
            }
        } );

        wheelDay = ( WheelView ) findViewById( R.id.wheelDay );
        wheelDay.setTag( "wheelDay" );
        wheelDay.setViewAdapter( new NumericWheelAdapter( ctx, 1, getDaysCountInMonth( DEFAULT_YEAR, DEFAULT_MONTH ) ) );
        wheelDay.setCurrentItem( DEFAULT_DAY - 1 );
        wheelDay.setVisibleItems( DEFAULT_VISIBLE_ITEMS );
        wheelDay.addChangingListener( new OnWheelChangedListener() {
            public void onChanged( WheelView wheel, int oldValue, int newValue ) {
                if (!dayChangeListenerDisabled) {
                    lastSelectedDay = newValue + 1;
                    //
                    int oldDay = oldValue + 1;
                    int newDay = newValue + 1;
                    int year = getYear();
                    int month = getMonth();
                    raiseDateChangedEvent( oldDay, month, year, newDay, month, year );
                }
            }
        } );
    }

    /**
     * Меняет количество дней в барабане и устанавливает текущий день
     * в соответствии с новым количеством дней и с последним установленным пользователем
     * днём lastSelectedDay
     * @param daysCount количество дней в месяце
     * @param daysCountPrev количество дней в предыдущем выбранном месяце
     * @param currentDay текущий выбранный день
     * @param lastSelectedDay последний день, установленный пользователем (может отличаться от currentDay)
     */
    private void updateWheelDay( int daysCount, int daysCountPrev, int currentDay, int lastSelectedDay ) {
        if (daysCountPrev > daysCount) {
            if (currentDay > daysCount) {
                dayChangeListenerDisabled = true;
                wheelDay.setCurrentItem( daysCount - 1 );
                dayChangeListenerDisabled = false;
            }
        } else if (daysCountPrev < daysCount) {
            if (currentDay != lastSelectedDay) {
                dayChangeListenerDisabled = true;
                if (lastSelectedDay <= daysCount)
                    wheelDay.setCurrentItem( lastSelectedDay - 1 );
                else {
                    // if switch from month with 28 days to month with 29 (or 30),
                    // but lastSelectedDay is 31, we have to set current item = 29 (or 30), not 31
                    wheelDay.setCurrentItem( daysCount - 1 );
                }
                dayChangeListenerDisabled = false;
            }
        }
    }

    /**
     *
     * @param year
     * @param month 1 - January, 2 - February, ..
     * @return
     */
    private static int getDaysCountInMonth(int year, int month) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        switch (month) {
            case 4:
            case 6:
            case 9:
            case 11: return 30;
            case 2: return gregorianCalendar.isLeapYear(year) ? 29 : 28;
            default: return 31;
        }
    }

    public static class MonthsAdapter extends NumericWheelAdapter {

        private Locale locale;

        public MonthsAdapter( Context context) {
            this(context, new Locale( "en" , "US" ) );
        }

        public static boolean isLocaleSupported(Locale locale) {
            return  new Locale( "ru", "RU" ).equals( locale ) || new Locale( "en", "US" ).equals( locale );
        }

        public void setLocale(Locale locale) {
            if ( !isLocaleSupported(locale ))
                throw new UnsupportedOperationException( "Specified locale is not supported yet." );
            this.locale = locale;
        }

        /**
         * Supported locales:
         *
         * @param context
         * @param locale
         */
        public MonthsAdapter( Context context, Locale locale) {
            super( context, 1, 12 );
            setLocale( locale );
        }

        @Override
        public CharSequence getItemText( int index ) {
            if ( index >= 0 && index < getItemsCount() ) {
                int value = 1 + index;
                Assert.assertTrue(value >= 1 && value <= 12);
                if (locale.equals( new Locale( "ru", "RU" ) )) {
                    switch ( value ) {
                        case 1: return "январь";
                        case 2: return "февраль";
                        case 3: return "март";
                        case 4:return "апрель";
                        case 5:return "май";
                        case 6:return "июнь";
                        case 7:return "июль";
                        case 8:return "август";
                        case 9:return "сентябрь";
                        case 10:return "октябрь";
                        case 11:return "ноябрь";
                        case 12:return "декабрь";
                    }
                } else if (locale .equals( new Locale( "en", "US" ))) {
                    switch ( value ) {
                        case 1: return "january";
                        case 2: return "february";
                        case 3: return "march";
                        case 4:return "april";
                        case 5:return "may";
                        case 6:return "june";
                        case 7:return "july";
                        case 8:return "august";
                        case 9:return "september";
                        case 10:return "october";
                        case 11:return "november";
                        case 12:return "december";
                    }
                } else
                    Assert.assertTrue( false );
            }
            return null;
        }
    }
}
