package com.polestar.ad.adapters;

import android.content.Context;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;
import com.polestar.ad.AdUtils;
import com.polestar.ad.BuildConfig;

public class AdmobRewardVideoAdapter extends AdAdapter {
    private RewardedVideoAd rewardedVideoAd;

    public AdmobRewardVideoAdapter(Context context, String key) {
        mKey = key;
        LOAD_TIMEOUT = 20*1000;
    }
    @Override
    public void registerPrivacyIconView(View view) {

    }

    @Override
    public void loadAd(Context context, int num, IAdLoadListener listener) {
        adListener = listener;
        if (listener == null) {
            AdLog.e("Not set listener!");
            return;
        }
        rewardedVideoAd =   MobileAds.getRewardedVideoAdInstance(context);
        rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                AdLog.d("onRewardedVideoAdLoaded");
                stopMonitor();
                mLoadedTime = System.currentTimeMillis();
                if(adListener != null) {
                    adListener.onAdLoaded(AdmobRewardVideoAdapter.this);
                }
            }

            @Override
            public void onRewardedVideoAdOpened() {
                AdLog.d("onRewardedVideoAdOpened");
            }

            @Override
            public void onRewardedVideoStarted() {
                AdLog.d("onRewardedVideoStarted");
            }

            @Override
            public void onRewardedVideoAdClosed() {
                AdLog.d("onRewardedVideoAdClosed");
                if (adListener != null) {
                    adListener.onAdClosed(AdmobRewardVideoAdapter.this);
                }
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                AdLog.d("onRewarded " + rewardItem.getType());
                if (adListener != null) {
                    adListener.onRewarded(AdmobRewardVideoAdapter.this);
                }
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                AdLog.d("onRewardedVideoAdLeftApplication");
                if (adListener != null) {
                    adListener.onAdClicked(AdmobRewardVideoAdapter.this);
                }
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                if (adListener != null) {
                    adListener.onError("ErrorCode: " + i);
                }
                stopMonitor();
            }

            @Override
            public void onRewardedVideoCompleted() {
                AdLog.d("onRewardedVideoCompleted");
            }
        });

        if (BuildConfig.DEBUG) {
            String android_id = AdUtils.getAndroidID(context);
            String deviceId = AdUtils.MD5(android_id).toUpperCase();
            AdRequest request = new AdRequest.Builder().addTestDevice(deviceId).build();
            rewardedVideoAd.loadAd(mKey,request);
            boolean isTestDevice = request.isTestDevice(context);
            AdLog.d( "is Admob Test Device ? "+deviceId+" "+isTestDevice);
        } else {
            rewardedVideoAd.loadAd(mKey,new AdRequest.Builder().build());
        }
        startMonitor();
    }

    @Override
    public boolean isInterstitialAd() {
        return true;
    }


    @Override
    public Object getAdObject() {
        return rewardedVideoAd;
    }

    @Override
    public String getAdType() {
        return AdConstants.AdType.AD_SOURCE_ADMOB_REWARD;
    }

    @Override
    protected void onTimeOut() {
        if (adListener != null) {
            adListener.onError("TIME_OUT");
        }
    }

    @Override
    public void show() {
        if (rewardedVideoAd != null
                && rewardedVideoAd.isLoaded()) {
            registerViewForInteraction(null);
            rewardedVideoAd.show();
        }
    }
}
