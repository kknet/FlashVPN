package in.dualspace.cloner.components.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Toast;

import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.remote.InstalledAppInfo;
import in.dualspace.cloner.BuildConfig;
import in.dualspace.cloner.R;
import in.dualspace.cloner.utils.MLogs;
import in.dualspace.cloner.utils.PreferencesUtils;

public class FeedbackActivity extends BaseActivity {

    private Context mContext;
    private EditText mEtFeedback;
    private Button mBtSubmit;
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
        rating = getIntent().getIntExtra(EXTRA_RATING, 0);
        mContext = this;
        initView();
    }

    private void initView() {
        setTitle(getString(R.string.feedback));

        mEtFeedback = (EditText) findViewById(R.id.et_feedback);
        mBtSubmit = (Button) findViewById(R.id.bt_submit);
        mBtSubmit.setEnabled(false);

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
                    Toast.makeText(mContext, getResources().getString(R.string.feedback_no_description),Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent data=new Intent(Intent.ACTION_SENDTO);
                data.setData(Uri.parse("mailto:ironhead.media@gmail.com"));
                data.putExtra(Intent.EXTRA_SUBJECT, "Feedback About Power Parallel");
                String fullContent = content + "\n\n\n\n"  + "Additional Info for Debug: \n" + "Rating: "+ rating + "\n"+ "App version: " + BuildConfig.VERSION_NAME
                        + "\n" + "Model info: " + Build.FINGERPRINT + "\nOS Version: " + Build.VERSION.SDK_INT + "\nLite Mode: " + PreferencesUtils.isLiteMode() + "\n\n\n";
                for (InstalledAppInfo appInfo: VirtualCore.get().getInstalledApps(0)) {
                    String pkgInfo = "\n Clone: " + appInfo.packageName + " source: " + appInfo.apkPath;
                    fullContent += pkgInfo;
                }

                data.putExtra(Intent.EXTRA_TEXT, fullContent);
                try {
                    startActivity(data);
                }catch (Exception e) {
                    MLogs.e("Start email activity fail!");
                    Toast.makeText(FeedbackActivity.this, getResources().getString(R.string.submit_success), Toast.LENGTH_SHORT).show();
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
