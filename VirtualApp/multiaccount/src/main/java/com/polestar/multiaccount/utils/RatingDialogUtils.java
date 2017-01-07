package com.polestar.multiaccount.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.polestar.multiaccount.R;


/**
 * Created by yxx on 2016/7/25.
 */
public class RatingDialogUtils {

    public final static int NEGATIVE_BUTTON = 1;
    public final static int POSITIVE_BUTTON = 2;

    public static Dialog showCustomDialog(Context context, String title, String content, String negBtnName,
                String posBtnName, int tagResId, int layoutResId, final DialogInterface.OnClickListener onClickListener) {
        final Dialog dialog = new Dialog(context, R.style.CustomDialog);
        View dialogView = LayoutInflater.from(context).inflate(layoutResId, null);
        DialogViewHolder holder = new DialogViewHolder();
        holder.title = (TextView) dialogView.findViewById(R.id.dialog_title);
        holder.content = (TextView) dialogView.findViewById(R.id.dialog_content);
        holder.negBtn = (TextView) dialogView.findViewById(R.id.button_negative);
        holder.posBtn = (TextView) dialogView.findViewById(R.id.button_positive);
        holder.tagImage = (ImageView) dialogView.findViewById(R.id.tag_img);

        if (negBtnName == null || TextUtils.isEmpty(negBtnName)) {
            holder.negBtn.setVisibility(View.GONE);
        } else {
            holder.negBtn.setText(posBtnName);
        }

        if (title == null || TextUtils.isEmpty(title)) {
            holder.title.setVisibility(View.GONE);
        } else {
            holder.title.setText(title);
        }

        holder.content.setText(content);
        holder.posBtn.setText(negBtnName);
        if(tagResId != -1) {
            holder.tagImage.setImageResource(tagResId);
        } else {
            holder.tagImage.setVisibility(View.GONE);
        }

        holder.negBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onClick(dialog, NEGATIVE_BUTTON);
                }
            }
        });
        holder.posBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onClick(dialog, POSITIVE_BUTTON);
                }
            }
        });

        dialog.setContentView(dialogView);
        int dialogwidth = DisplayUtils.getScreenWidth(context) * 5 / 6;
        // 设置Dialog的大小
        dialog.getWindow().setLayout(dialogwidth, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        AnimatorHelper.elasticScale(dialogView);
        return dialog;
    }

    static class DialogViewHolder {
        TextView title;
        TextView content;
        TextView negBtn;
        TextView posBtn;
        ImageView tagImage;
    }
}
