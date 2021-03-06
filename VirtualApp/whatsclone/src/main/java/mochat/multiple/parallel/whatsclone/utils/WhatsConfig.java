package mochat.multiple.parallel.whatsclone.utils;

import android.text.TextUtils;

import java.util.HashSet;

import mochat.multiple.parallel.whatsclone.MApp;

/**
 * Created by guojia on 2019/3/27.
 */

public class WhatsConfig {

    public static final String KEY_INTERCEPT_CLASS = "conf_intercept_class";
    public static final String KEY_ADS_LAUNCH_CTRL = "ads_launch_ctrl";

    private static final int NO_OP = -1;
    public static final int ADS_BLOCK = 0;
    public static final int ADS_TO_COVER = 1;
    public static final int ADS_FORCE_REPLACE = 2;

    private static WhatsConfig sConfig;

    private HashSet<String> mInterstitialActivitySet;

    private int adsCtrl = NO_OP;

    private WhatsConfig(){
        mInterstitialActivitySet = new HashSet<>();
        initData();
    }

    public void initData() {
        mInterstitialActivitySet.clear();
        String conf = PreferencesUtils.getString(MApp.getApp(), KEY_INTERCEPT_CLASS, null);
        if (conf != null) {
            String[] arr = conf.split(";");
            if (arr != null) {
                for (String s:arr) {
                    if (!TextUtils.isEmpty(s)) {
                        mInterstitialActivitySet.add(s);
                    }
                }
            }
        }
        adsCtrl = PreferencesUtils.isAdFree() ? NO_OP : PreferencesUtils.getInt(MApp.getApp(), KEY_ADS_LAUNCH_CTRL, NO_OP);

    }

    public static synchronized WhatsConfig get() {
        if (sConfig == null) {
            sConfig = new WhatsConfig();
        }
        return sConfig;
    }

    public boolean isHandleInterstitial(String clz) {
        return mInterstitialActivitySet.contains(clz) && adsCtrl != NO_OP;
    }

    public boolean isPolicyInterstitialBlock() {
        return adsCtrl == ADS_BLOCK || adsCtrl == ADS_FORCE_REPLACE;
    }

    public int getInterstitialAdsCtl() {
        return adsCtrl;
    }
}
