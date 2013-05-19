package su.elwood;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.util.Locale;

public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        WheelDatePicker datepicker = (WheelDatePicker) findViewById(R.id.datepicker);
        datepicker.setDay(10);
        datepicker.setMonth(8);
        datepicker.setYear(1989);
        // Locale("ru", "RU") is also available
        datepicker.setLocale(Locale.US);
        datepicker.setVisibleItems(5);
        datepicker.setMinMaxYears(1970, 2000);
        datepicker.addDateChangedListener(new WheelDatePicker.IDateChangedListener() {
            @Override
            public void onChanged(WheelDatePicker sender, int oldDay, int oldMonth, int oldYear, int day, int month, int year) {
                Log.i( "WHEEL_APP", String.format("Selected date changed ! %02d.%02d.%04d -> %02d.%02d.%04d",
                        oldDay, oldMonth, oldYear, day, month, year));
            }
        });
    }
}
