package layout;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import java.io.IOException;

import juntilla.dexter.pdfreaderwidget.R;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link MainAppWidgetConfigureActivity MainAppWidgetConfigureActivity}
 */
public class MainAppWidget extends AppWidgetProvider {

    private final String LEFT_CLICKED = "leftButtonClicked";
    private final String RIGHT_CLICKED = "rightButtonClicked";
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        if (prefs == null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
            editor = prefs.edit();
        }

//        CharSequence widgetText = MainAppWidgetConfigureActivity.loadTitlePref(context, appWidgetId);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main_app_widget);

        int page = prefs.getInt("PdfPage", 1);
        int pages = prefs.getInt("PdfMaxPages", 1);
        String pdfFile = prefs.getString("PdfFile", "");

        views.setTextViewText(R.id.textView, page + " / " + pages);
        views.setTextViewText(R.id.appwidget_text, readPdfPageText(page, pdfFile));
        views.setOnClickPendingIntent(R.id.imageButton, getPendingSelfIntent(context, LEFT_CLICKED));
        views.setOnClickPendingIntent(R.id.imageButton2, getPendingSelfIntent(context, RIGHT_CLICKED));

        appWidgetManager.updateAppWidget(appWidgetId, views);
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
            MainAppWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
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

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        super.onReceive(context, intent);

        if (prefs == null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
            editor = prefs.edit();
        }

        if (LEFT_CLICKED.equals(intent.getAction())) {
            leftClicked(context);
        }
        else if (RIGHT_CLICKED.equals(intent.getAction())) {
            rightClicked(context);
        }
    }

    private  void leftClicked(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        RemoteViews remoteViews;
        ComponentName watchWidget;

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_app_widget);
        watchWidget = new ComponentName(context, MainAppWidget.class);

        String pdfFile = prefs.getString("PdfFile", "");
        int page = prefs.getInt("PdfPage", 1);
        int pages = prefs.getInt("PdfMaxPages", 1);
        editor.putInt("PdfPage", --page);
        editor.apply();

        remoteViews.setTextViewText(R.id.textView, page + " / " + pages);
        remoteViews.setTextViewText(R.id.appwidget_text, readPdfPageText(page, pdfFile));

        appWidgetManager.updateAppWidget(watchWidget, remoteViews);
    }

    private  void rightClicked(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        RemoteViews remoteViews;
        ComponentName watchWidget;

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_app_widget);
        watchWidget = new ComponentName(context, MainAppWidget.class);

        String pdfFile = prefs.getString("PdfFile", "");
        int page = prefs.getInt("PdfPage", 1);
        int pages = prefs.getInt("PdfMaxPages", 1);
        editor.putInt("PdfPage", ++page);
        editor.apply();

        remoteViews.setTextViewText(R.id.textView, page + " / " + pages);
        remoteViews.setTextViewText(R.id.appwidget_text, readPdfPageText(page, pdfFile));

        appWidgetManager.updateAppWidget(watchWidget, remoteViews);
    }

    private PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private String readPdfPageText(int page, String pdf) {
        try {
            PdfReader reader = new PdfReader(pdf);
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            TextExtractionStrategy strategy = parser.processContent(page, new SimpleTextExtractionStrategy());
            String text = strategy.getResultantText();

            reader.close();

            return text;
        }
        catch (IOException e) {
            return "";
        }
    }
}

