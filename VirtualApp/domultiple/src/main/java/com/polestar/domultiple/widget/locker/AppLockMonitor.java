package com.polestar.domultiple.widget.locker;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.lody.virtual.client.core.VirtualCore;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.clone.CloneManager;
import com.polestar.domultiple.components.ui.AppLockActivity;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.domultiple.db.DBManager;
import com.polestar.domultiple.notification.QuickSwitchNotification;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.utils.RemoteConfig;

import java.util.HashMap;
import java.util.List;

/**
 * Created by PolestarApp on 2017/1/4.
 */

public class AppLockMonitor {

    private String mUnlockedForegroudPkg;
    private static AppLockMonitor sInstance = null;
    private HashMap<String , CloneModel> modelHashMap = new HashMap<>();
    private Handler mHandler;
    private static long relockDelay = PreferencesUtils.getLockInterval(); //if paused for 2 minutes, and then resume, it need be locked
    private final static int MSG_DELAY_LOCK_APP = 0;
    public final static int MSG_PACKAGE_UNLOCKED = 1;
    public final static int MSG_PRELOAD_AD = 2;
    public final static int MSG_SHOW_LOCKER = 3;
    public final static int MSG_HIDE_LOCKER = 4;
    public final static String CONFIG_APPLOCK_PRELOAD_INTERVAL = "applock_preload_interval";
    public final static String CONFIG_SLOT_APP_LOCK = "slot_app_lock";
    private final static String TAG = "AppLockMonitor";

    private FuseAdLoader mAdLoader;
    private boolean hasAppLocked;
    private boolean adFree;

    private AppLockMonitor() {
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_DELAY_LOCK_APP:
                        MLogs.d(TAG, "Package change to background, last foreground: " + mUnlockedForegroudPkg);
                        QuickSwitchNotification.getInstance(VirtualCore.get().getContext()).updateLruPackages((String)msg.obj);
                        mUnlockedForegroudPkg = null;
                        break;
                    case MSG_PACKAGE_UNLOCKED:
                        String pkg = (String) msg.obj;
                        MLogs.d(TAG, "Package was unlocked" + pkg);
                        mUnlockedForegroudPkg = pkg;
                        break;
                    case MSG_PRELOAD_AD:
                        if (!adFree && hasLocker()) {
                            if (mAdLoader.hasValidAdSource()) {
                                mAdLoader.loadAd(1, null);
                            }
                            long interval = RemoteConfig.getLong(CONFIG_APPLOCK_PRELOAD_INTERVAL);
                            MLogs.d(TAG, "Applocker schedule next ad at " + interval);
                            if (interval >= 15*60*000) {
                                MLogs.d(TAG, "Go schedule next ad at " + interval);
                                mHandler.sendEmptyMessageDelayed(MSG_PRELOAD_AD, interval);
                            }
                        }
                    case MSG_SHOW_LOCKER:
                        String key = (String) msg.obj;
                        try {
                            String name = CloneManager.getNameFromKey(key);
                            int userId = CloneManager.getUserIdFromKey(key);
                            AppLockActivity.start(VirtualCore.get().getContext(), name, userId);
                        }catch (Exception ex) {

                        }
                        break;
                    case MSG_HIDE_LOCKER:
                        break;
                }
            }
        };
        initSetting();
    }

    private boolean hasLocker() {
        return hasAppLocked && !TextUtils.isEmpty(LockPatternUtils.getTempKey());
    }
    private void preloadAd() {
        mHandler.removeMessages(MSG_PRELOAD_AD);
        mHandler.sendEmptyMessage(MSG_PRELOAD_AD);
    }

    private void initSetting() {
        MLogs.d("initSetting");
        List<CloneModel> list = DBManager.queryAppList(PolestarApp.getApp());
        for (CloneModel model: list) {
            modelHashMap.put(CloneManager.getMapKey(model.getPackageName(), model.getPkgUserId()), model);
            if (model.getLockerState() != AppConstants.AppLockState.DISABLED) {
                hasAppLocked = true;
            }
        }
        adFree = false;
        mAdLoader = FuseAdLoader.get(CONFIG_SLOT_APP_LOCK, PolestarApp.getApp());
        mAdLoader.setBannerAdSize(AppLockActivity.getBannerSize());
        LockPatternUtils.setTempKey(PreferencesUtils.getEncodedPatternPassword(PolestarApp.getApp()));
        preloadAd();
    }

    public FuseAdLoader getAdLoader(){
        return mAdLoader;
    }

    public void reloadSetting(String newKey, boolean adFree, long interval) {
        MLogs.d(TAG, "reloadSetting adfree:" + adFree + " tmpkey: " + newKey);
        modelHashMap.clear();
        DBManager.resetSession();
        List<CloneModel> list = DBManager.queryAppList(PolestarApp.getApp());
        for (CloneModel model: list) {
            modelHashMap.put(model.getPackageName() + model.getPkgUserId(), model);
            if (model.getLockerState() != AppConstants.AppLockState.DISABLED) {
                MLogs.d(TAG, "hasAppLocked " + model.getPackageName());
                hasAppLocked = true;
            }
        }
        preloadAd();
        this.adFree = adFree;
        LockPatternUtils.setTempKey(newKey);
        if ( interval >= 3000) {
            relockDelay = interval;
        }
    }

    public static synchronized AppLockMonitor getInstance(){
        if (sInstance == null) {
            sInstance = new AppLockMonitor();
        }
        return sInstance;
    }

    public void unlocked(String pkg, int userId) {
        mHandler.sendMessage(mHandler.obtainMessage(AppLockMonitor.MSG_PACKAGE_UNLOCKED, CloneManager.getMapKey(pkg, userId)));
    }

    public void onActivityResume(String pkg, int userId) {
        String key = CloneManager.getMapKey(pkg, userId);
        MLogs.d(TAG, "onActivityResume " + key);
        CloneModel model = modelHashMap.get(key);
        if (model == null || pkg == null) {
            MLogs.logBug(TAG, "cannot find cloned model : " + pkg);
            return;
        }
        if (hasLocker() && model.getLockerState() != AppConstants.AppLockState.DISABLED) {
            MLogs.d(TAG, "Need lock app " + pkg);
            if (mUnlockedForegroudPkg == null || (!mUnlockedForegroudPkg.equals(key))) {
                //do lock
                MLogs.d(TAG, "Do lock app " + pkg);
                mHandler.removeMessages(MSG_SHOW_LOCKER, key);
                mHandler.removeMessages(MSG_HIDE_LOCKER, key);
                mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_LOCKER, key));
            }
        }
        //Remove the same object with send
        mHandler.removeMessages(MSG_DELAY_LOCK_APP, key);
        //mUnlockedForegroudPkg = pkg;
    }

    public void onActivityPause(String pkg, int userId) {
        String key = CloneManager.getMapKey(pkg, userId);
        MLogs.d(TAG, "onActivityPause " + pkg + " delay relock: " + relockDelay);
        CloneModel model = modelHashMap.get(key);
        if (model != null) {
            mHandler.removeMessages(MSG_SHOW_LOCKER, key);
            mHandler.removeMessages(MSG_HIDE_LOCKER, key);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_HIDE_LOCKER, key), 500);
        }
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_DELAY_LOCK_APP, key),
                relockDelay);
    }
}
