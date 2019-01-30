package com.polestar.superclone.reward;

import android.content.Context;

import com.polestar.superclone.MApp;
import com.polestar.superclone.R;
import com.polestar.task.ADErrorCode;

/**
 * Created by guojia on 2019/1/29.
 */

final public class RewardErrorCode {
    private static final int REWARD_ERROR_CODE_BASE = ADErrorCode.MAX_SERVER_ERR_CODE + 1000;
    public static final int TASK_OK = REWARD_ERROR_CODE_BASE;
    public static final int TASK_EXCEED_DAY_LIMIT = TASK_OK + 1;
    public static final int TASK_AD_NO_FILL = TASK_OK + 2;
    public static final int TASK_UNEXPECTED_ERROR = TASK_OK + 3;
    public static final int TASK_AD_LOADING = TASK_OK + 4;

    public static final int PRODUCT_OK = REWARD_ERROR_CODE_BASE + 1000;
    public static final int PRODUCT_NO_ENOUGH_COIN = PRODUCT_OK + 1;


    public static final String getToastMessage(int code, Object ... args) {
        return getToastMessage(MApp.getApp(), code, args);
    }

    public static final String getToastMessage(Context context, int code, Object ... args) {
        switch (code) {
            case TASK_AD_NO_FILL:
                return context.getString(R.string.error_ad_no_fill);
            case TASK_EXCEED_DAY_LIMIT:
            case ADErrorCode.DAY_LIMITTED:
            case ADErrorCode.TOTAL_LIMITTED:
                return context.getString(R.string.error_day_limit);
            case TASK_OK:
                float payment = (float) args[0];
                return context.getString(R.string.task_ok, payment);
            case PRODUCT_OK:
                return context.getString(R.string.product_ok);
            case ADErrorCode.NOT_ENOUGH_MONEY:
                return context.getString(R.string.no_enough_coin);
            case ADErrorCode.PRODUCT_NOTEXIST:
                return context.getString(R.string.product_not_exist);
            default:
                break;
        }
        return context.getString(R.string.error_unexpected);
    }
}
