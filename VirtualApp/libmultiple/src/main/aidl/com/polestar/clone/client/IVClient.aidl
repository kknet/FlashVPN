// IVClient.aidl
package com.polestar.clone.client;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;

import com.polestar.clone.remote.PendingResultData;

interface IVClient {
    void scheduleReceiver(in String processName,in ComponentName component, in Intent intent, in PendingResultData resultData);
    void scheduleNewIntent(in String creator, in IBinder token, in Intent intent);
    void finishActivity(in IBinder token);
    IBinder createProxyService(in ComponentName component, in IBinder binder);
    IBinder acquireProviderClient(in ProviderInfo info);
    IBinder getAppThread();
    IBinder getToken();
    String getDebugInfo();
    void bindApplication(in String pkg, in String processName);
}