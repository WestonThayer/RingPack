/*
 * RingPack is a 'Notification ringtone' rotator for Android.
 *
 * Copyright (C) 2010 Weston Thayer
 *
 * This file is part of RingPack.
 *
 * RingPack is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * RingPack is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * RingPack.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cryclops.ringpack;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

/**
 * The RingWidget class enables a widget that is either dead or alive. If dead,
 * all it can do is sit there. If alive, it may play the current tone, and go to
 * the next as well as display the current tone.
 * 
 * It's life is stored in a preference. Might be good to switch to a singleton
 * someday.
 * 
 * @author Weston Thayer
 * @version 2.0.0
 * @version 2.1.0
 * 					Compatibility with Utilities class.
 *
 */
public class RingWidget extends AppWidgetProvider {
	
	public static final String URI_SCHEME = "ringpack_widget";

	@Override
	public void onDisabled(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(RingService.WIDGET_ALIVE, false);
		editor.commit();
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager awm, int[] appWidgetIds) {
		SharedPreferences.Editor editor = Utilities.getPrefs(context).edit();
		editor.putBoolean(RingService.WIDGET_ALIVE, true);
		editor.commit();
		
		if (Utilities.getEnabled(context)) {
			Intent i = new Intent(context, RingService.class);
			i.putExtra(RingService.ACTION, RingService.WIDGET_STATUS);
			i.putExtra(RingService.WIDGET_ALIVE, true);
			context.startService(i);
			
			update(context, "...");
		}
		else
			disableUpdate(context, context.getString(R.string.disabled));
	}
	
	static void update(Context context, String text) {
		AppWidgetManager awm = AppWidgetManager.getInstance(context);
		
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ringwidget);
		
		//play
		Intent playI = new Intent(context, RingService.class);
		playI.putExtra(RingService.ACTION, RingService.PLAY_TONE);
		Uri playData = Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/#"), "play");
		playI.setData(playData);
		
		PendingIntent playP = PendingIntent.getService(context, 0, playI, PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.wPlay, playP);
		
		//next
		Intent nextI = new Intent(context, RingService.class);
		nextI.putExtra(RingService.ACTION, RingService.NEXT_TONE);
		Uri nextData = Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/#"), "next");
		playI.setData(nextData);
		
		PendingIntent nextP = PendingIntent.getService(context, 0, nextI, PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.wNext, nextP);
		
		//title text
		views.setTextViewText(R.id.wTitle, text);
		
		//finish up
		ComponentName thisWidget = new ComponentName(context, RingWidget.class);
		awm.updateAppWidget(thisWidget, views);
	}
	
	static void disableUpdate(Context context, String text) {
		AppWidgetManager awm = AppWidgetManager.getInstance(context);
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ringwidget);
		
		Intent blankI = new Intent();
		
		//disable play and next
		PendingIntent playP = PendingIntent.getService(context, 0, blankI, PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.wPlay, playP);
		
		PendingIntent nextP = PendingIntent.getService(context, 0, blankI, PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.wNext, nextP);
		
		views.setTextViewText(R.id.wTitle, text);
		
		ComponentName thisWidget = new ComponentName(context, RingWidget.class);
		awm.updateAppWidget(thisWidget, views);
	}
}