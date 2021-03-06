package com.polestar.ad.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;

/**
 * Created by guojia on 2016/12/11.
 */

public class IronSourceInterstitialAdapter extends AdAdapter {
    private String key;
    private Handler mainHandler;

    public IronSourceInterstitialAdapter(Context context, String key) {
        this.key = key;
        LOAD_TIMEOUT = 20*1000;
        mainHandler = new Handler(Looper.getMainLooper());
    }

    private void postOnMainHandler(Runnable runnable) {
        mainHandler.post(runnable);
    }

    @Override
    public Object getAdObject() {
        return this;
    }

    @Override
    public String getAdType() {
        return AdConstants.AdType.AD_SOURCE_IRONSOURCE_INTERSTITIAL;
    }

    @Override
    public void registerPrivacyIconView(View view) {

    }

    @Override
    public boolean isInterstitialAd() {
        return true;
    }

    @Override
    public void show() {
        AdLog.d("Prepare to show");
        if (IronSource.isInterstitialReady()) {
            AdLog.d("show ironsource interstitial");
            registerViewForInteraction(null);
            IronSource.showInterstitial(key);
        }
    }

    @Override
    public void loadAd(Context context, int num, IAdLoadListener listener) {
        adListener = listener;
        if (listener == null){
            AdLog.e("listener is null!!");
            return;
        }
        IronSource.setInterstitialListener(new InterstitialListener() {
            @Override
            public void onInterstitialAdReady() {
                postOnMainHandler(new Runnable() {
                    @Override
                    public void run() {
                        mLoadedTime = System.currentTimeMillis();
                        if (adListener != null) {
                            adListener.onAdLoaded(IronSourceInterstitialAdapter.this);
                        }
                        stopMonitor();
                    }
                });
            }

            @Override
            public void onInterstitialAdLoadFailed(final IronSourceError ironSourceError) {
                postOnMainHandler(new Runnable() {
                    @Override
                    public void run() {
                        if (adListener != null) {
                            adListener.onError("ErrorCode: " + ironSourceError);
                        }
                        stopMonitor();
                    }
                });

            }

            @Override
            public void onInterstitialAdOpened() {
                postOnMainHandler(new Runnable() {
                    @Override
                    public void run() {
                        if (adListener != null) {
                            adListener.onAdClicked(IronSourceInterstitialAdapter.this);
                        }
                    }
                });
            }

            @Override
            public void onInterstitialAdClosed() {
                AdLog.d("ad interstitial onAdClosed");
                //Hack ironSource only start another load when current ad is closed
                //so make it invalid after closed
                postOnMainHandler(new Runnable() {
                    @Override
                    public void run() {
                        mLoadedTime = 0;
                        if (adListener != null) {
                            adListener.onAdClosed(IronSourceInterstitialAdapter.this);
                        }
                    }
                });

            }

            @Override
            public void onInterstitialAdShowSucceeded() {

            }

            @Override
            public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {

            }

            @Override
            public void onInterstitialAdClicked() {
                postOnMainHandler(new Runnable() {
                    @Override
                    public void run() {
                        if (adListener != null) {
                            adListener.onAdClicked(IronSourceInterstitialAdapter.this);
                        }
                    }
                });
            }
        });
        if (!IronSource.isInterstitialReady()) {
            IronSource.loadInterstitial();
            startMonitor();
        } else {
            AdLog.d("IR interstitial already ready");
            mLoadedTime = System.currentTimeMillis();
            if (adListener != null) {
                adListener.onAdLoaded(IronSourceInterstitialAdapter.this);
                adListener = null;
            }
        }
    }

    @Override
    protected void onTimeOut() {
        if (adListener != null) {
            adListener.onError("TIME_OUT");
        }
    }

    @Override
    public void onActivityResume(Activity activity) {
        IronSource.onResume(activity);
    }

    @Override
    public void onActivityPause(Activity activity) {
        IronSource.onPause(activity);
    }
}
