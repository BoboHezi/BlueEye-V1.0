package eli.blueeye.v1.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import eli.blueeye.v1.dialog.WifiStatusDialog;

/**
 * 接收网络连接状态的receiver
 *
 * @author eli chang
 */
public class NetStatusReceiver extends BroadcastReceiver {

    //WIFI状态的提示对话框
    private WifiStatusDialog eWifiStatusDialog;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isWifiConnected = false;
        //获取网络间接状态
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiStateInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiStateInfo != null) {
            isWifiConnected = wifiStateInfo.isConnected();
        }
        if (!isWifiConnected) {
            eWifiStatusDialog = new WifiStatusDialog(context);
            eWifiStatusDialog.show();
        } else if (eWifiStatusDialog != null) {
            eWifiStatusDialog.dismiss();
            eWifiStatusDialog = null;
        }
    }
}
