package com.polestar.multiaccount.utils;import android.content.Context;import android.view.Gravity;import android.widget.Toast;/** * Created by yxx on 2016/7/14. */public class ToastUtils {    private static Toast toast = null;    /**     * 屏幕上方弹窗     *     * @param context     * @param msg     */    public static void ToastCenter(Context context, String msg) {        if (null == toast) {            toast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT);        }        toast.setGravity(Gravity.CENTER, 0, 0);        toast.setText(msg);        toast.show();    }    /**     * 屏幕底端     *     * @param context     * @param msg     */    public static void ToastBottow(Context context, String msg) {        if (null == toast) {            toast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT);        }        toast.setGravity(Gravity.BOTTOM, 0, 0);        toast.setText(msg);        toast.show();    }    /**     * 默认位置     *     * @param context     * @param msg     */    public static void ToastDefult(Context context, String msg) {        if (null == toast) {            toast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT);        }        toast.setText(msg);        toast.show();    }    /**     * 屏幕上方弹窗     *     * @param context     * @param msg     */    public static void ToastCenterLong(Context context, String msg) {        if (null == toast) {            toast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_LONG);        }        toast.setGravity(Gravity.CENTER, 0, 0);        toast.setText(msg);        toast.show();    }    /**     * 屏幕底端     *     * @param context     * @param msg     */    public static void ToastBottowLong(Context context, String msg) {        if (null == toast) {            toast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_LONG);        }        toast.setGravity(Gravity.BOTTOM, 0, 0);        toast.setText(msg);        toast.show();    }    /**     * 默认位置     *     * @param context     * @param msg     */    public static void ToastDefultLong(Context context, String msg) {        if (null == toast) {            toast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_LONG);        }        toast.setText(msg);        toast.show();    }}