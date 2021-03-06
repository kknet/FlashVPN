package com.polestar.superclone.component.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.remote.InstalledAppInfo;
import com.polestar.superclone.BuildConfig;
import com.polestar.superclone.R;
import com.polestar.superclone.component.BaseActivity;
import com.polestar.superclone.reward.AppUser;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.utils.PreferencesUtils;
import com.polestar.superclone.utils.ToastUtils;

public class FeedbackActivity extends BaseActivity {

    private Context mContext;
    private EditText mEtFeedback;
    private Button mBtSubmit;
    private TextView mGoFAQ;

    private static final String EXTRA_RATING = "extra_rating";
    private int rating;

    public static void start(Context activity, int rating) {
        Intent intent = new Intent(activity, FeedbackActivity.class);
        intent.putExtra(FeedbackActivity.EXTRA_RATING, rating);
        activity.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        mContext = this;
        initView();
    }

    private void initView() {
        setTitle(getString(R.string.feedback));

        mEtFeedback = (EditText) findViewById(R.id.et_feedback);
        mBtSubmit = (Button) findViewById(R.id.bt_submit);
        mGoFAQ = (TextView) findViewById(R.id.tv_go_faq);
        mBtSubmit.setEnabled(false);

        mGoFAQ.setText(getString(R.string.feedback_go_faq));
        mGoFAQ.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG );

        mEtFeedback.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    mBtSubmit.setEnabled(true);
                } else {
                    mBtSubmit.setEnabled(false);
                }
            }
        });

        mBtSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = mEtFeedback.getText().toString();
                if (content == null) {
                    ToastUtils.ToastDefult(mContext, getResources().getString(R.string.feedback_no_description));
                    return;
                }

                Intent data=new Intent(Intent.ACTION_SENDTO);
                data.setData(Uri.parse("mailto:polestar.applab@gmail.com"));
                String title = "Feedback about Super Clone";
                if (PreferencesUtils.isVIP()) {
                    title = "VIP " + title;
                }
                data.putExtra(Intent.EXTRA_SUBJECT, title);
                String fullContent = content + "\n\n\n\n"  + "Additional Info: \n" + "Rating: "+ rating +  "Super Clone version: " + BuildConfig.VERSION_NAME
                        + "\n" + "Model info: " + Build.FINGERPRINT + "\nGMS state: " + PreferencesUtils.isGMSEnable() + "\n";
                String userContent = "Reward enabled: " + AppUser.isRewardEnabled() + "\n Ad Free: " + PreferencesUtils.isAdFree();
                if (AppUser.isRewardEnabled() && AppUser.getInstance().isRewardAvailable()) {
                    userContent = userContent + "\n id: " + AppUser.getInstance().getMyId() + " balance: " + AppUser.getInstance().getMyBalance();
                }
                fullContent = fullContent + userContent;

                for (InstalledAppInfo appInfo: VirtualCore.get().getInstalledApps(0)) {
                    String pkgInfo = "\n Package: " + appInfo.packageName + " path: " + appInfo.apkPath;
                    fullContent += pkgInfo;
                }

                data.putExtra(Intent.EXTRA_TEXT, fullContent);
                try {
                    startActivity(data);
                }catch (Exception e) {
                    MLogs.e("Start email activity fail!");
                    ToastUtils.ToastDefult(FeedbackActivity.this, getResources().getString(R.string.submit_success));
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
