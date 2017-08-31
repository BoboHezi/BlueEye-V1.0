package eli.blueeye.v1.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eli.blueeye.v1.R;

/**
 * 自定义ListView的适配器
 *
 * @author eli chang
 */
public class CustomListAdapter extends BaseAdapter {

    private static final String TAG = "ListAdapter";

    private List<Map<String, Object>> eListData;
    private LayoutInflater eLayoutInflater;
    private Animation eAnimation;
    private Map<Integer, Boolean> isFirst;
    private List<Integer> eSelectedItem;
    private int size;

    public CustomListAdapter(Context context, List<Map<String, Object>> eListData) {
        this.eListData = eListData;
        this.eLayoutInflater = LayoutInflater.from(context);
        this.eAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_item_enter);
        this.isFirst = new HashMap<>();
        this.eSelectedItem = new ArrayList<>();
    }

    public final class ItemView {
        public RandomCirclePoint typeColor;
        public TextView itemTime;
        public ImageView itemThumbnail;
    }

    @Override
    public int getCount() {
        return size;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return eListData.get(position);
    }

    @Override
    public void notifyDataSetChanged() {
        size = eListData.size();
        super.notifyDataSetChanged();
    }

    /**
     * 删除某条
     *
     * @param index
     */
    public void removeItem(int index) {
        this.eListData.remove(index);
        notifyDataSetChanged();
        notifyDataSetInvalidated();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemView itemView;

        if (convertView == null) {
            itemView = new ItemView();
            convertView = eLayoutInflater.inflate(R.layout.listitem_file, null);
            itemView.typeColor = (RandomCirclePoint) convertView.findViewById(R.id.item_view_circle);
            itemView.itemTime = (TextView) convertView.findViewById(R.id.item_text_create_time);
            itemView.itemThumbnail = (ImageView) convertView.findViewById(R.id.item_view_thumbnail);
            convertView.setTag(itemView);
        } else {
            itemView = (ItemView) convertView.getTag();
        }
        if (isFirst.get(position) == null || isFirst.get(position)) {
            convertView.startAnimation(eAnimation);
            isFirst.put(position, false);
        }
        itemView.itemTime.setText((String) eListData.get(position).get("time"));
        itemView.itemThumbnail.setImageBitmap((Bitmap) eListData.get(position).get("image"));

        if (eSelectedItem != null && eSelectedItem.contains(position)) {
            itemView.typeColor.setFocus();
        } else {
            itemView.typeColor.setDismiss();
        }

        return convertView;
    }

    /**
     * 设置被选中的记录
     *
     * @param eSelectedItem
     */
    public void setSelectedItem(List<Integer> eSelectedItem) {
        this.eSelectedItem = eSelectedItem;
    }
}