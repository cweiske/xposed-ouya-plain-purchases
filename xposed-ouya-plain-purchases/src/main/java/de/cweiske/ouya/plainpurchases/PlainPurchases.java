package de.cweiske.ouya.plainpurchases;

import java.lang.reflect.Method;

import javax.crypto.Cipher;

import de.robv.android.xposed.IXposedHookCmdInit;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hook into Java's main encryption method and simply return the input.
 * This disables encryption completely, and allows our OUYA store to retrieve
 * plain text requests and send plain text responses that the OUYA understands
 *
 * @link https://github.com/rovo89/XposedBridge/wiki/Development-tutorial
 * @link https://api.xposed.info/
 *
 * @author Christian Weiske <cweiske+ouya@cweiske.de>
 */
public class PlainPurchases implements IXposedHookLoadPackage, IXposedHookCmdInit
{
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam)
    {
        //we cannot filter on lpparam.packagename because that breaks
        // in-game receipt decryption

        //XposedBridge.log("Loaded app: " + lpparam.packageName);

        XposedHelpers.findAndHookMethod(Cipher.class, "doFinal", byte[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                byte[] input = (byte[]) param.args[0];

                //XposedBridge.log("input: " + new String(input));
                //XposedBridge.log("returning unencrypted input");
                //XposedBridge.log(new Exception("doFinal stack trace"));
                param.setResult(input);
            }
        });
    }

    @Override
    public void initCmdApp(StartupParam startupParam) throws Throwable {
        if (!startupParam.startClassName.equals("com.android.commands.pm.Pm")) {
            return;
        }
        //XposedBridge.log("startup:" + startupParam.startClassName);

        /*
         Final Fantasy 3 has a native library lib__57d5__.so that tries to clear the data
         for all apk files in /data/app/. We do not know why.

         It fails to do this for root-owned packages like this xposed module.
         See
         https://forum.xda-developers.com/xposed/framework-xposed-rom-modding-modifying-t1574401/page170#1700

         We simply prevent it from doing that.

         https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-4.1.2_r1/core/java/android/app/ActivityManagerNative.java#2880

         public boolean clearApplicationUserData(
             String packageName, IPackageDataObserver observer, final int userId
         )
         */
        XposedHelpers.findAndHookMethod(
            "android.app.ActivityManagerProxy",
            null,
            "clearApplicationUserData",
            String.class,
            "android.content.pm.IPackageDataObserver",
            int.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String packageName = (String) param.args[0];
                    XposedBridge.log("clearApplicationUserData: " + packageName);
                    if (!packageName.equals("de.cweiske.ouya.plainpurchases")) {
                        return;
                    }

                    param.setResult(true);

                    //we do not have direct access to IPackageDataObserver, so we have to dance
                    Object observer = param.args[1];
                    if(observer != null) {
                        Class<?> iPackageDataObserverClass = Class.forName("android.content.pm.IPackageDataObserver");
                        Class<?>[] paramTypes = {String.class, boolean.class};
                        Method onRemoveCompletedMethod = iPackageDataObserverClass.getMethod("onRemoveCompleted", paramTypes);
                        Object[] params = {packageName, true};
                        try {
                            onRemoveCompletedMethod.invoke(observer, params);
                            //observer.onRemoveCompleted(packageName, true);
                        } catch (Exception e) {
                            XposedBridge.log("Observer no longer exists.");
                        }
                    }
                    //end dance
                }
            }
        );

    }
}
