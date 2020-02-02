package de.cweiske.ouya.plainpurchases;

import javax.crypto.Cipher;

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
 * @author Christian Weiske <cweiske+ouya@cweiske.de>
 */
public class PlainPurchases implements IXposedHookLoadPackage
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
                //XposedBridge.log(new Exception("doFinal stack trace"));

                boolean isJson = input.length > 0
                    && input[0] == 123// "{"
                    && input[input.length - 1] == 125;// "}"
                boolean isDummyKey = input.length == 16
                    && (new String(input)).equals("0123456789abcdef");

                //only prevent some data from being encrypted/decrypted
                if (isJson || isDummyKey) {
                    //XposedBridge.log("returning unencrypted input");
                    param.setResult(input);
                }
            }
        });
    }
}
