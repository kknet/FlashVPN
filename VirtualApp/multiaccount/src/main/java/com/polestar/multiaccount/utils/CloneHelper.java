package com.polestar.multiaccount.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.polestar.multiaccount.db.DbManager;
import com.polestar.multiaccount.model.AppModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yxx on 2016/8/29.
 */
public class CloneHelper {
    private static CloneHelper instance;
    private List<AppModel> mClonedApps;
    private OnClonedAppChangListener loadedListener;
    private boolean isPreLoad;
    public interface OnClonedAppChangListener {
        void onInstalled(List<AppModel> clonedApp);
        void onUnstalled(List<AppModel> clonedApp);
        void onLoaded(List<AppModel> clonedApp);
    }

    public static CloneHelper getInstance(Context context){
        if(instance == null){
            instance = new CloneHelper(context);
        }
        return instance;
    }

    public void loadClonedApps(Context context,OnClonedAppChangListener loadedListener){
        this.loadedListener = loadedListener;
        loadClonedApp(context);
//        new LoadClonedAppTask(context).execute();
    }

    public void installApp(Context context,AppModel appModel){
        appModel.setClonedTime(System.currentTimeMillis());
        appModel.setIndex(mClonedApps.size());
        DbManager.insertAppModel(context,appModel);
        DbManager.notifyChanged();
        mClonedApps.add(appModel);
        if(loadedListener != null){
            loadedListener.onInstalled(mClonedApps);
        }
    }

    public void unInstallApp(Context context,String packageName){
        if(packageName != null){
            List<AppModel> models = DbManager.queryAppModelByPackageName(context,packageName);
            AppManager.deleteApp(context,models);
        }
        for (int i = 0;i >= 0 && i < mClonedApps.size();i ++){
            if(mClonedApps.get(i).getPackageName().equals(packageName)){
                mClonedApps.remove(i);
                i --;
            }
        }
        if(loadedListener != null){
            loadedListener.onUnstalled(mClonedApps);
        }
    }

    public void preLoadClonedApp(Context context){
        List<AppModel> tempList = AppManager.getClonedApp(context);
        if(tempList != null){
            mClonedApps.clear();
            mClonedApps.addAll(tempList);
        }
        isPreLoad = true;
    }

    private void loadClonedApp(Context context){
        if(!isPreLoad){
            List<AppModel> tempList = AppManager.getClonedApp(context);
            if(tempList != null){
                mClonedApps.clear();
                mClonedApps.addAll(tempList);
            }
        }
        isPreLoad = false;
        if(loadedListener != null){
            loadedListener.onLoaded(mClonedApps);
        }
    }

    private CloneHelper(Context context){
        mClonedApps = new ArrayList<>();
    }

    public List<AppModel> getClonedApps(){
        return mClonedApps;
    }

    class LoadClonedAppTask extends AsyncTask<Integer,Integer,List<AppModel>>{
        private Context mContext;

        LoadClonedAppTask(Context context){
            this.mContext = context;
        }

        @Override
        protected List<AppModel> doInBackground(Integer... integers) {
            return AppManager.getClonedApp(mContext);
        }

        @Override
        protected void onPostExecute(List<AppModel> appModels) {
            if(appModels != null){
                mClonedApps.clear();
                mClonedApps.addAll(appModels);
            }
            if(loadedListener != null){
                loadedListener.onLoaded(mClonedApps);
            }
        }
    }
}
