# Android wheel datepicker #

This is control based on [https://code.google.com/p/android-wheel/](https://code.google.com/p/android-wheel/) project.

![](http://elwood.su/wp-content/uploads/datepicker.png)

# Features #

* min-max year setting
* no manual validation needs (it is impossible to select invalid date)
* russian localization
* several optimizations in original wheel control code (measuring)

# Using the control #

1) Copy source files of classes kankan.wheel.widget.* into your project source directory

2) Copy WheelDatePicker.java too

3) Copy wheel_datepicker.xml to your res/layout directory

4) Add next ids to your ids.xml file:

   
```xml
<item name="wheelDay" type="id"/>
<item name="wheelMonth" type="id"/>
<item name="wheelYear" type="id"/>
```

5) Add custom view to your layout and configure it in activity code like this:

        
```java

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
```
