package eli.blueeye.v1.entity;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 读取文件和缩略图的实体
 *
 * @author eli chang
 */
public class ReadFiles {

    private static final String photoPath = Environment.getExternalStorageDirectory() + "/blueeye/photos/";
    private static final String videoPath = Environment.getExternalStorageDirectory() + "/blueeye/videos/";

    /**
     * 读取所有的文件
     *
     * @return
     */
    public List readFiles() {
        File photoFolder = new File(photoPath);
        File videoFolder = new File(videoPath);

        if (!photoFolder.exists()) {
            photoFolder.mkdirs();
        }
        if (!videoFolder.exists()) {
            videoFolder.mkdirs();
        }
        List<File> allFiles = new ArrayList<>();

        //读取所有的图片
        File files[] = photoFolder.listFiles();
        if (files.length > 0) {
            for (File file : files) {
                if (file.isFile() && file.getName().startsWith("IMG"))
                    allFiles.add(file);
            }
        }
        //读取所有的视频
        files = videoFolder.listFiles();
        if (files.length > 0) {
            for (File file : files) {
                if (file.isFile() && file.getName().startsWith("VID"))
                    allFiles.add(file);
            }
        }
        //按时间进行逆序排序
        Collections.sort(allFiles, new ComparatorByLastModified());
        return allFiles;
    }

    /**
     * 对文件按事件排序
     */
    class ComparatorByLastModified implements Comparator<File> {

        @Override
        public int compare(File f1, File f2) {

            long diff = f1.lastModified() - f2.lastModified();
            if (diff > 0)
                return -1;
            else if (diff == 0)
                return 0;
            else
                return 1;
        }
    }
}