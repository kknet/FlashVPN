package com.polestar.ad.com.polestar.ad.adapters;

/**
 * Created by guojia on 2016/10/31.
 */

public interface INativeAdLoader {
    /**
     *
     * @param num number of Ads; if <= 0 , will use the default value 1s
     * @param listener
     */
    void loadAd(int num, INativeAdLoadListener listener);
}
