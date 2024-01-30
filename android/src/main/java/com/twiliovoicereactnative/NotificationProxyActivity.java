package com.twiliovoicereactnative;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class NotificationProxyActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    handleIntent(getIntent());
    finish();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    handleIntent(intent);
    finish();
  }

  private void handleIntent(Intent intent) {
    String action = intent.getAction();
    if (action != null) {
      switch (action) {
        case Constants.ACTION_PUSH_APP_TO_FOREGROUND:
          launchMainActivity();
          removeMissedCalls();
          break;
        case Constants.ACTION_PUSH_APP_TO_FOREGROUND_AND_MINIMIZE_NOTIFICATION:
        case Constants.ACTION_ACCEPT:
          launchService(intent);
          launchMainActivity();
          removeMissedCalls();
          break;
        case Constants.ACTION_CLEAR_MISSED_CALLS_COUNT:
          removeMissedCalls();
          NotificationUtility.destroyMissedCallsNotificationChannels(getApplicationContext());
          break;
        default:
          launchService(intent);
          break;
      }
    }
  }

  private void removeMissedCalls() {
    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
            Constants.PREFERENCE_KEY, Context.MODE_PRIVATE);
    SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();
    sharedPrefEditor.putInt(Constants.MISSED_CALLS_GROUP, 0);
    sharedPrefEditor.commit();
  }

  private void launchMainActivity() {
    try{
      Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
      launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(launchIntent);
    }catch (Exception e){
      e.printStackTrace();
    }
  }
  private void launchService(Intent intent) {
    Intent launchIntent = new Intent(intent);
    launchIntent.setClass(this, IncomingCallNotificationService.class);
    startService(launchIntent);
  }
}
