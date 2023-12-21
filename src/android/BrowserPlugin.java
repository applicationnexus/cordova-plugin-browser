package by.chemerisuk.cordova.browser;

import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import androidx.browser.customtabs.CustomTabsCallback;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;
import android.util.Log;

import by.chemerisuk.cordova.support.CordovaMethod;
import by.chemerisuk.cordova.support.ReflectiveCordovaPlugin;

import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaArgs;
import org.json.JSONException;

public class BrowserPlugin extends ReflectiveCordovaPlugin {
    private static final String TAG = "BrowserPlugin";

    private CustomTabsClient customTabsClient;
    private CustomTabsCallback customTabsCallback;

    private CallbackContext loadCallback;
    private CallbackContext closeCallback;

    @CordovaMethod
    protected void ready(final CallbackContext callbackContext) {
        Context context = this.cordova.getActivity();
        String packageName = CustomTabsClient.getPackageName(context, null);
        if (packageName == null) {
            callbackContext.success();
        } else {
            CustomTabsClient.bindCustomTabsService(context, packageName, new CustomTabsServiceConnection() {
                @Override
                public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
                    customTabsClient = client;
                    customTabsCallback = new CustomTabsCallback() {
                        @Override
                        public void onNavigationEvent(int navigationEvent, Bundle extras) {
                            if (navigationEvent == CustomTabsCallback.NAVIGATION_FINISHED) {
                                if (loadCallback != null) {
                                    loadCallback.success();
                                    loadCallback = null;
                                }
                            } else if (navigationEvent == CustomTabsCallback.TAB_HIDDEN) {
                                if (closeCallback != null) {
                                    closeCallback.success();
                                    closeCallback = null;
                                }
                            }
                        }
                    };
                    callbackContext.success();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d(TAG, "onServiceDisconnected");
                }
            });
        }
    }

    @CordovaMethod
    protected void open(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        String url = args.getString(0);
			
			Context context=this.cordova.getActivity().getApplicationContext();
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(browserIntent);

        callbackContext.success();
    }

    @CordovaMethod
    protected void onLoad(CallbackContext callbackContext) {
        loadCallback = callbackContext;
    }

    @CordovaMethod
    protected void onClose(CallbackContext callbackContext) {
        closeCallback = callbackContext;
    }

    @Override
    public void onPause(boolean multitasking) {
        if (customTabsClient == null && loadCallback != null) {
            loadCallback.success();
            loadCallback = null;
        }
    }

    @Override
    public void onResume(boolean multitasking) {
        if (customTabsClient == null && closeCallback != null) {
            closeCallback.success();
            closeCallback = null;
        }
    }

    private int getAnimResId(String name) {
        Context context = cordova.getActivity();
        return context.getResources()
            .getIdentifier(name, "anim", context.getPackageName());
    }
}
