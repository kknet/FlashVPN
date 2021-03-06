package com.polestar.clone.helper.utils;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;

import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.env.Constants;
import com.polestar.clone.client.env.SpecialComponentList;
import com.polestar.clone.GmsSupport;
import com.polestar.clone.helper.compat.ObjectsCompat;
import com.polestar.clone.remote.BroadcastIntentData;

import static android.content.pm.ActivityInfo.LAUNCH_SINGLE_INSTANCE;

/**
 * @author Lody
 */
public class ComponentUtils {

    public static String getTaskAffinity(ActivityInfo info) {
        if (info.launchMode == LAUNCH_SINGLE_INSTANCE) {
            return "-SingleInstance-" + info.packageName + "/" + info.name;
        } else if (info.taskAffinity == null && info.applicationInfo.taskAffinity == null) {
            return info.packageName;
        } else if (info.taskAffinity != null) {
            return info.taskAffinity;
        }
        return info.applicationInfo.taskAffinity;
    }

    public static boolean isSameIntent(Intent a, Intent b) {
        if (a != null && b != null) {
            if (!ObjectsCompat.equals(a.getAction(), b.getAction())) {
                return false;
            }
            if (!ObjectsCompat.equals(a.getData(), b.getData())) {
                return false;
            }
            if (!ObjectsCompat.equals(a.getType(), b.getType())) {
                return false;
            }
            Object pkgA = a.getPackage();
            if (pkgA == null && a.getComponent() != null) {
                pkgA = a.getComponent().getPackageName();
            }
            String pkgB = b.getPackage();
            if (pkgB == null && b.getComponent() != null) {
                pkgB = b.getComponent().getPackageName();
            }
            if (!ObjectsCompat.equals(pkgA, pkgB)) {
                return false;
            }
            if (!ObjectsCompat.equals(a.getComponent(), b.getComponent())) {
                return false;
            }
            if (!ObjectsCompat.equals(a.getCategories(), b.getCategories())) {
                return false;
            }
        }
        return true;
    }

    public static String getProcessName(ComponentInfo componentInfo) {
        String processName = componentInfo.processName;
        if (processName == null) {
            processName = componentInfo.packageName;
            componentInfo.processName = processName;
        }
        return processName;
    }

    public static boolean isSameComponent(ComponentInfo first, ComponentInfo second) {

        if (first != null && second != null) {
            String pkg1 = first.packageName + "";
            String pkg2 = second.packageName + "";
            String name1 = first.name + "";
            String name2 = second.name + "";
            return pkg1.equals(pkg2) && name1.equals(name2);
        }
        return false;
    }

    public static ComponentName toComponentName(ComponentInfo componentInfo) {
        return new ComponentName(componentInfo.packageName, componentInfo.name);
    }

    public static boolean isSystemApp(ApplicationInfo applicationInfo) {
        if (applicationInfo == null) {
            return false;
        }
        return !GmsSupport.isGmsFamilyPackage(applicationInfo.packageName)
                && ((ApplicationInfo.FLAG_SYSTEM & applicationInfo.flags) != 0
                || SpecialComponentList.isSpecSystemPackage(applicationInfo.packageName));
    }

    public static boolean isStubComponent(Intent intent) {
        return intent != null
                && intent.getComponent() != null
                && VirtualCore.get().getHostPkg().equals(intent.getComponent().getPackageName());
    }

    public static Intent redirectBroadcastIntent(Intent intent, int userId) {
        Intent newIntent = intent.cloneFilter();
        newIntent.setComponent(null);
        newIntent.setPackage(null);
        ComponentName component = intent.getComponent();
        String pkg = intent.getPackage();
//        newIntent.putExtra(Constants.VA_INTENT_KEY_INTENT, new Intent(intent));
        if (component != null) {
//            newIntent.putExtra(Constants.VA_INTENT_KEY_USERID, userId);
            newIntent.setAction(String.format(Constants.VA_INTENT_KEY_COMPONENT_ACTION_FMT, component.getPackageName(), component.getClassName()));
//            newIntent.putExtra(Constants.VA_INTENT_KEY_COMPONENT, component);
            if (pkg == null) {
                pkg = component.getPackageName();
            }
        } else {
//            newIntent.putExtra(Constants.VA_INTENT_KEY_USERID, userId);
            String protectedAction = SpecialComponentList.protectAction(intent.getAction());
            if (protectedAction != null) {
                newIntent.setAction(protectedAction);
            }
        }
        newIntent.putExtra(Constants.VA_INTENT_KEY_BRDATA, new BroadcastIntentData(userId, intent, pkg, component));
        return newIntent;
    }
}
