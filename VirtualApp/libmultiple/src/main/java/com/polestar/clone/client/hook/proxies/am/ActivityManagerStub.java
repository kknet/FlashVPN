package com.polestar.clone.client.hook.proxies.am;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IInterface;

import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.hook.base.BinderInvocationStub;
import com.polestar.clone.client.hook.base.Inject;
import com.polestar.clone.client.hook.base.MethodInvocationProxy;
import com.polestar.clone.client.hook.base.MethodInvocationStub;
import com.polestar.clone.client.hook.base.MethodProxy;
import com.polestar.clone.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.polestar.clone.client.hook.base.ReplaceLastPkgMethodProxy;
import com.polestar.clone.client.hook.base.ReplaceLastUidMethodProxy;
import com.polestar.clone.client.hook.base.ResultStaticMethodProxy;
import com.polestar.clone.client.hook.base.StaticMethodProxy;
import com.polestar.clone.client.ipc.VActivityManager;
import com.polestar.clone.helper.compat.BuildCompat;
import com.polestar.clone.helper.compat.ParceledListSliceCompat;
import com.polestar.clone.os.VUserInfo;
import com.polestar.clone.os.VUserManager;
import com.polestar.clone.remote.AppTaskInfo;

import java.lang.reflect.Method;
import java.util.List;

import mirror.android.app.ActivityManagerNative;
import mirror.android.app.ActivityManagerOreo;
import mirror.android.app.IActivityManager;
import mirror.android.content.pm.ParceledListSlice;
import mirror.android.os.ServiceManager;
import mirror.android.util.Singleton;

/**
 * @author Lody
 * @see IActivityManager
 * @see android.app.ActivityManager
 */

@Inject(MethodProxies.class)
public class ActivityManagerStub extends MethodInvocationProxy<MethodInvocationStub<IInterface>> {

    public ActivityManagerStub() {
        super(new MethodInvocationStub<>(ActivityManagerNative.getDefault.call()));
    }

    @Override
    public void inject() throws Throwable {
        if (BuildCompat.isOreo()) {
            //Android Oreo(8.X)
            Object singleton = ActivityManagerOreo.IActivityManagerSingleton.get();
            Singleton.mInstance.set(singleton, getInvocationStub().getProxyInterface());
        } else {
            if (ActivityManagerNative.gDefault.type() == IActivityManager.TYPE) {
                ActivityManagerNative.gDefault.set(getInvocationStub().getProxyInterface());
            } else if (ActivityManagerNative.gDefault.type() == Singleton.TYPE) {
                Object gDefault = ActivityManagerNative.gDefault.get();
                Singleton.mInstance.set(gDefault, getInvocationStub().getProxyInterface());
            }
        }
        BinderInvocationStub hookAMBinder = new BinderInvocationStub(getInvocationStub().getBaseInterface());
        hookAMBinder.copyMethodProxies(getInvocationStub());
        ServiceManager.sCache.get().put(Context.ACTIVITY_SERVICE, hookAMBinder);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        if (VirtualCore.get().isVAppProcess()) {
            addMethodProxy(new StaticMethodProxy("navigateUpTo") {
                @Override
                public Object call(Object who, Method method, Object... args) throws Throwable {
                    try{
                       return super.call(who, method, args);
                    }catch (Throwable ex) {
                        ex.printStackTrace();
                        return false;
                    }
                }
            });

            //public static final int IMPORTANCE_GONE = 1000;
            addMethodProxy(new ResultStaticMethodProxy("getPackageProcessState", 1000));
            addMethodProxy(new ResultStaticMethodProxy("registerUidObserver", null));
//            addMethodProxy(new ReplaceLastUidMethodProxy("checkPermissionWithToken"));
            addMethodProxy(new isUserRunning());
            addMethodProxy(new ResultStaticMethodProxy("updateConfiguration", 0));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("setAppLockedVerifying"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("reportJunkFromApp"));
            addMethodProxy(new ReplaceLastPkgMethodProxy("getAppStartMode"));

            addMethodProxy(new StaticMethodProxy("setRequestedOrientation") {
                @Override
                public Object call(Object who, Method method, Object... args) throws Throwable {
                    try {
                        return super.call(who, method, args);
                    }catch (Exception ex){
                        return 0;
                    }
                }
            });

//            addMethodProxy(new StaticMethodProxy("activityResumed") {
//                @Override
//                public Object call(Object who, Method method, Object... args) throws Throwable {
//                    try{
//                        VActivityManager.get().onActivityResumed((Activity) args[0]);
//                    }catch (Exception ex){
//                    }
//                    return super.call(who, method, args);
//
//                }
//            });
//
//            addMethodProxy(new StaticMethodProxy("activityDestroyed") {
//                @Override
//                public Object call(Object who, Method method, Object... args) throws Throwable {
//                    try{
//                        VActivityManager.get().onActivityDestroy(((Activity) args[0]);
//                    }catch (Exception ex){
//                        ex.printStackTrace();
//                    }
//                    return super.call(who, method, args);
//
//                }
//            });

            addMethodProxy(new StaticMethodProxy("checkUriPermission") {
                @Override
                public Object call(Object who, Method method, Object[] args) throws Throwable {
                    return PackageManager.PERMISSION_GRANTED;
                }
            });
            addMethodProxy(new StaticMethodProxy("getRecentTasks") {
                @Override
                public Object call(Object who, Method method, Object... args) throws Throwable {
                    Object _infos = method.invoke(who, args);
                    //noinspection unchecked
                    List<ActivityManager.RecentTaskInfo> infos =
                            ParceledListSliceCompat.isReturnParceledListSlice(method)
                                    ? ParceledListSlice.getList.call(_infos)
                                    : (List) _infos;
                    for (ActivityManager.RecentTaskInfo info : infos) {
                        AppTaskInfo taskInfo = VActivityManager.get().getTaskInfo(info.id);
                        if (taskInfo == null) {
                            continue;
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                info.topActivity = taskInfo.topActivity;
                                info.baseActivity = taskInfo.baseActivity;
                            } catch (Throwable e) {
                                // ignore
                            }
                        }
                        try {
                            info.origActivity = taskInfo.baseActivity;
                            info.baseIntent = taskInfo.baseIntent;
                        } catch (Throwable e) {
                            // ignore
                        }
                    }
                    return _infos;
                }
            });
        }
    }

    @Override
    public boolean isEnvBad() {
        return ActivityManagerNative.getDefault.call() != getInvocationStub().getProxyInterface();
    }

    private class isUserRunning extends MethodProxy {
        @Override
        public String getMethodName() {
            return "isUserRunning";
        }

        @Override
        public Object call(Object who, Method method, Object... args) {
            int userId = (int) args[0];

            for(VUserInfo info: VUserManager.get().getUsers()){
                if (info.id == userId) {
                    return true;
                }
            }
            return false;
        }
    }
}
