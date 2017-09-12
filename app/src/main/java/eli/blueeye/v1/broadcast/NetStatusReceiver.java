package eli.blueeye.v1.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import eli.blueeye.v1.dialog.WifiStatusDialog;

public class NetStatusReceiver extends BroadcastReceiver {
    private static final String TAG = "NetStatusReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isWifiConnected = false;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiStateInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiStateInfo != null) {
            isWifiConnected = wifiStateInfo.isConnected();
        }
        if (!isWifiConnected) {
            new WifiStatusDialog(context).show();
        }
        Log.i(TAG, "Wifi is" + (isWifiConnected ? "" : " not") + " connected.");
    }
}
