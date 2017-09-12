package eli.blueeye.v1.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import eli.blueeye.v1.R;
import eli.blueeye.v1.util.Util;

public class WifiStatusDialog extends Dialog implements View.OnClickListener{

    private Context context;

    private TextView eCancelButton;
    private TextView eConnectButton;

    public WifiStatusDialog(Context context) {
        super(context, R.style.dialog_wifi_waring);
        this.context = context;
        initView();
    }

    private void initView() {
        setContentView(R.layout.dialog_wifi_waring);
        getWindow().setWindowAnimations(R.style.animation_dialog_wifi);
        setCanceledOnTouchOutside(true);

        eCancelButton = (TextView) findViewById(R.id.dialog_wifi_cancel);
        eCancelButton.setOnClickListener(this);
        eConnectButton = (TextView) findViewById(R.id.dialog_wifi_connect);
        eConnectButton.setOnClickListener(this);
    }

    @Override
    public void show() {
        super.show();
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = Util.dip2px(context, 240);
        layoutParams.height = Util.dip2px(context, 120);
        layoutParams.gravity = Gravity.CENTER;
        window.setAttributes(layoutParams);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_wifi_cancel:
                break;

            case R.id.dialog_wifi_connect:
                if (context != null)
                    context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                break;
        }
        dismiss();
    }
}
