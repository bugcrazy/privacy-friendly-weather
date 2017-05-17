package org.secuso.privacyfriendlyweather.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.secuso.privacyfriendlyweather.R;
import org.secuso.privacyfriendlyweather.activities.SplashActivity;
import org.secuso.privacyfriendlyweather.database.City;
import org.secuso.privacyfriendlyweather.database.CurrentWeatherData;
import org.secuso.privacyfriendlyweather.database.PFASQLiteHelper;
import org.secuso.privacyfriendlyweather.preferences.AppPreferencesManager;
import org.secuso.privacyfriendlyweather.ui.UiResourceProvider;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WeatherWidgetConfigureActivity WeatherWidgetConfigureActivity}
 */
public class WeatherWidget extends AppWidgetProvider {
    private static final String PREFS_NAME = "org.secuso.privacyfriendlyweather.widget.WeatherWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        //CharSequence widgetText = WeatherWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);

        String number = String.format("%03d", (new Random().nextInt(900) + 100));

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Integer cityId = prefs.getInt(PREF_PREFIX_KEY + appWidgetId, -1);
        if (cityId == -1) {
            Toast.makeText(context, "cityId is null?", Toast.LENGTH_LONG);
            return;
        }

        PFASQLiteHelper database = PFASQLiteHelper.getInstance(context);
        City city = database.getCityById(cityId);
        CurrentWeatherData weatherData = database.getCurrentWeatherByCityId(city.getCityId());

        AppPreferencesManager prefManager =
                new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()));
        DecimalFormat decimalFormat = new DecimalFormat("#.0");
        String temperature = String.format(
                "%s%s",
                decimalFormat.format(prefManager.convertTemperatureFromCelsius(weatherData.getTemperatureCurrent())),
                prefManager.getWeatherUnit()
        );

        views.setTextViewText(R.id.widget_city_weather_temperature, temperature);
        views.setTextViewText(R.id.widget_city_weather_humidity, String.format("%s %%", weatherData.getHumidity()));
        views.setTextViewText(R.id.widget_city_name, city.getCityName());

        views.setImageViewResource(R.id.widget_city_weather_image_view, UiResourceProvider.getIconResourceForWeatherCategory(weatherData.getWeatherID()));

        Intent intent = new Intent(context, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_city_weather_image_view, pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

        database.close();
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            WeatherWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

