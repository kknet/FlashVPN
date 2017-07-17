package com.polestar.domultiple.components.ui;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.R;
import com.polestar.domultiple.clone.CloneManager;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.domultiple.utils.AnimatorHelper;
import com.polestar.domultiple.utils.CommonUtils;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.widget.DropableLinearLayout;
import com.polestar.domultiple.widget.ExplosionField;
import com.polestar.domultiple.widget.HomeGridAdapter;
import com.polestar.domultiple.widget.UpDownDialog;
import com.polestar.domultiple.widget.dragdrop.DragController;
import com.polestar.domultiple.widget.dragdrop.DragImageView;
import com.polestar.domultiple.widget.dragdrop.DragLayer;
import com.polestar.domultiple.widget.dragdrop.DragSource;

import java.util.List;

/**
 * Created by guojia on 2017/7/15.
 */

public class HomeActivity extends BaseActivity implements CloneManager.OnClonedAppChangListener, DragController.DragListener{
    private List<CloneModel> mClonedList;
    private CloneManager cm;
    private GridView cloneGridView;
    private HomeGridAdapter gridAdapter;
    boolean showLucky;
    private DragLayer mDragLayer;
    private DragController mDragController;
    private FrameLayout mTitleBar;
    private LinearLayout mActionBar;
    private DropableLinearLayout createShortcutArea;
    private DropableLinearLayout deleteArea;
    private LinearLayout createDropButton;
    private LinearLayout deleteDropButton;
    private TextView deleteDropTxt;
    private TextView createDropTxt;
    private ExplosionField mExplosionField;

    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    private void initData() {
        cm = CloneManager.getInstance(this);
        cm.loadClonedApps(this, this);
        showLucky = PreferencesUtils.hasCloned() && !PreferencesUtils.isAdFree();
        gridAdapter.setShowLucky(showLucky );
    }


    private void initView() {
        setContentView(R.layout.home_activity_layout);
        cloneGridView = (GridView) findViewById(R.id.clone_grid_view);
        gridAdapter = new HomeGridAdapter(this);
        cloneGridView.setAdapter(gridAdapter);
        cloneGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int luckyIdx = mClonedList.size();
                int addIdx = showLucky? luckyIdx + 1 : luckyIdx;
                if (i < mClonedList.size()) {
                    CloneModel model = (CloneModel)gridAdapter.getItem(i);
                    CloneManager.launchApp(model.getPackageName());
                } else if (showLucky && i == luckyIdx) {
                    MLogs.d("lucky clicked");
                } else if (i == addIdx) {
                    MLogs.d("to add more clone");
                    startActivity(new Intent(HomeActivity.this, AddCloneActivity.class));
                }
            }
        });
        cloneGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i >= mClonedList.size()) {
                    return false;
                }
                DragImageView iv = (DragImageView) view.findViewById(R.id.app_icon);
                mDragController.startDrag(iv, iv, gridAdapter.getItem(i), DragController.DRAG_ACTION_COPY);
                return true;
            }
        });

        mActionBar = (LinearLayout) findViewById(R.id.action_bar) ;
        mTitleBar = (FrameLayout) findViewById(R.id.title_bar);

        mDragLayer = (DragLayer)findViewById(R.id.drag_layer);
        mDragController = new DragController(this);
        mDragController.setWindowToken(mDragLayer.getWindowToken());
        mDragController.setDragListener(this);
        mDragLayer.setDragController(mDragController);

        createShortcutArea = (DropableLinearLayout) findViewById(R.id.create_shortcut_area);
        createDropButton = (LinearLayout) findViewById(R.id.shortcut_drop_button);
        deleteDropButton = (LinearLayout)findViewById(R.id.delete_drop_button);
        deleteDropTxt = (TextView)findViewById(R.id.delete_drop_text);
        createDropTxt = (TextView)findViewById(R.id.shortcut_drop_text);
        createShortcutArea.setOnEnterListener(new DropableLinearLayout.IDragListener() {
            @Override
            public void onEnter() {
                createDropButton.setBackgroundResource(R.drawable.shape_create_shortcut);
                createDropTxt.setTextColor(getResources().getColor(R.color.shortcut_text_color));
            }

            @Override
            public void onExit() {
                createDropButton.setBackgroundColor(0);
                createDropTxt.setTextColor(getResources().getColor(R.color.white));
            }
        });
        deleteArea = (DropableLinearLayout) findViewById(R.id.delete_app_area);
        deleteArea.setOnEnterListener(new DropableLinearLayout.IDragListener() {
            @Override
            public void onEnter() {
                deleteDropButton.setBackgroundResource(R.drawable.shape_delete);
                deleteDropTxt.setTextColor(getResources().getColor(R.color.delete_text_color));
            }

            @Override
            public void onExit() {
                deleteDropButton.setBackgroundColor(0);
                deleteDropTxt.setTextColor(getResources().getColor(R.color.white));
            }
        });

        mExplosionField = ExplosionField.attachToWindow(this);
    }

    @Override
    public void onInstalled(CloneModel clonedApp, boolean result) {
        mClonedList = cm.getClonedApps();
        if (result && PreferencesUtils.getBoolean(this, AppConstants.KEY_AUTO_CREATE_SHORTCUT, false)) {
            CommonUtils.createShortCut(this, clonedApp);
        }
        gridAdapter.notifyDataSetChanged(mClonedList);
    }

    @Override
    public void onUnstalled(CloneModel clonedApp, boolean result) {
        mClonedList = cm.getClonedApps();
        if (result) {
            CommonUtils.removeShortCut(this, clonedApp);
        }
        gridAdapter.notifyDataSetChanged(mClonedList);
    }

    @Override
    public void onLoaded(List<CloneModel> clonedApp) {
        mClonedList = cm.getClonedApps();
        gridAdapter.notifyDataSetChanged(mClonedList);
    }

    public void onMenuClick(View view) {

    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
        mTitleBar.setVisibility(View.INVISIBLE);
        mActionBar.setVisibility(View.VISIBLE);
        AnimatorHelper.verticalShowFromBottom(mActionBar);
        mDragController.addDropTarget(createShortcutArea);
        createShortcutArea.clearState();
        createDropButton.setBackgroundColor(0);
        createDropTxt.setTextColor(getResources().getColor(R.color.white));
        deleteDropButton.setBackgroundColor(0);
        deleteDropTxt.setTextColor(getResources().getColor(R.color.white));
        mDragController.addDropTarget(deleteArea);
        deleteArea.clearState();
    }

    @Override
    public void onDragEnd(DragSource source, Object info, int dragAction) {
        mTitleBar.setVisibility(View.VISIBLE);
        mActionBar.setVisibility(View.INVISIBLE);

        if (createShortcutArea.isSelected()) {
            CommonUtils.createShortCut(this,((CloneModel) info));
            mActionBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                        Toast.makeText(HomeActivity.this, R.string.toast_shortcut_added, Toast.LENGTH_SHORT).show();
                    //CustomToastUtils.showImageWithMsg(mActivity, mActivity.getResources().getString(R.string.toast_shortcut_added), R.mipmap.icon_add_success);
                }
            },500);
        } else if (deleteArea.isSelected()) {
            mActionBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MLogs.d("Delete clone model!");
                    showDeleteDialog((CloneModel) info);
                }
            },500);
        }
        AnimatorHelper.hideToBottom(mActionBar);
        mActionBar.setVisibility(View.GONE);
        AnimatorHelper.verticalShowFromBottom(mTitleBar);
        mTitleBar.setVisibility(View.VISIBLE);
        mDragController.removeDropTarget(createShortcutArea);
        mDragController.removeDropTarget(deleteArea);
    }

    private int getPosForModel(final CloneModel model) {
        int i = 0;
        for (CloneModel c : mClonedList) {
            if (c.getPackageName().equals(model.getPackageName())) {
                return i;
            }
            i ++;
        }
        return  -1;
    }

    private void showDeleteDialog(CloneModel info) {
        UpDownDialog.show(HomeActivity.this, getString(R.string.delete_dialog_title), getString(R.string.delete_dialog_content),
                getString(R.string.no_thanks), getString(R.string.yes), -1, R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case UpDownDialog.NEGATIVE_BUTTON:
                                break;
                            case UpDownDialog.POSITIVE_BUTTON:
                                int pos = getPosForModel(info);
                                if (pos == -1) {
                                    MLogs.logBug("Unkown package");
                                }
                                View view = cloneGridView.getChildAt(pos);
                                if(view != null) {
                                    mExplosionField.explode(view, new ExplosionField.OnExplodeFinishListener() {
                                        @Override
                                        public void onExplodeFinish(View v) {
                                        }
                                    });
                                }
                                view.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        CloneManager.getInstance(HomeActivity.this).deleteClone(HomeActivity.this, info.getPackageName());
                                    }
                                }, 1000);
                                break;
                        }
                    }
                });
    }
}
