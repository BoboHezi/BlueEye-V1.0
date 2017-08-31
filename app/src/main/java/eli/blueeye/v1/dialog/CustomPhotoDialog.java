package eli.blueeye.v1.dialog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;

import eli.blueeye.v1.R;
import eli.blueeye.v1.entity.LoadListView;
import eli.blueeye.v1.inter.OnPressActionListener;
import eli.blueeye.v1.view.CustomImageView;

/**
 * 显示图片的Dialog
 *
 * @author eli chang
 */
public class CustomPhotoDialog extends BaseDialog implements OnPressActionListener, View.OnClickListener {

    private CustomImageView eImageView;
    private ImageButton eButtonMore;
    private Bitmap bitmap;

    public CustomPhotoDialog(Context context, Activity activity, File[] files, LoadListView.RefreshHandler refreshHandler) {
        super(context, activity, files, refreshHandler, R.style.style_image_dialog);
        initView();
    }

    /**
     * 初始化窗口和视图
     */
    @Override
    public void initView() {
        setContentView(R.layout.dialog_picture);
        setWindowAnimation();

        eImageView = (CustomImageView) findViewById(R.id.dialog_picture_view_image);
        eImageView.setOnPressAction(this);
        eButtonMore = (ImageButton) findViewById(R.id.dialog_picture_button_more);
        eButtonMore.setOnClickListener(this);
        eTimeTextView = (TextView) findViewById(R.id.dialog_picture_text_time);
        showTime();
        showPicture();
    }

    /**
     * 设置窗体动画
     */
    @Override
    public void setWindowAnimation() {
        getWindow().setWindowAnimations(R.style.animation_image_dialog);
    }

    /**
     * 定义窗口和动画，通过文件显示图片
     */
    public void showPicture() {
        try {
            bitmap = BitmapFactory.decodeFile(files[0].getPath());
            eImageView.setImageBitmap(bitmap);
        } catch (Exception e) {
        }
        this.show();
    }

    /**
     * 单击事件
     */
    @Override
    public void singleTap() {
        dismiss();
    }

    /**
     * 长按事件
     */
    @Override
    public void longPress() {
        showActionDialog();
    }

    /**
     * 按钮点击事件
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        showActionDialog();
    }
}