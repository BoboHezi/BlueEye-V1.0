package eli.blueeye.v1.server;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 发送控制命令的线程
 *
 * @author eli chang
 */
public class SendCommandThread extends Thread {

    private static final String TAG = "TestCommand";
    private final String host = "10.42.0.1";
    private final int port = 15231;

    private int commandData;

    private Socket eClientSocket;
    private DataInputStream eInputStream;
    private DataOutputStream eOutputStream;
    private int valueLength = 32;

    public SendCommandThread(int commandData) {
        this.commandData = commandData;
    }

    @Override
    public void run() {
        try {
            eClientSocket = new Socket(host, port);

            eInputStream = new DataInputStream(eClientSocket.getInputStream());
            eOutputStream = new DataOutputStream(eClientSocket.getOutputStream());

            byte buffers[];

            //接收验证数据
            buffers = readBytes();
            String value = new String(buffers, 0, valueLength);
            //转为long
            long verifyData = Long.parseLong(value);

            //乘以3
            verifyData = verifyData * 3;
            //发送验证数据
            eOutputStream.write((verifyData + "").getBytes());

            //接收验证结果
            buffers = readBytes();

            //发送控制命令
            eOutputStream.writeInt(commandData);

            //结束
            if (eInputStream != null)
                eInputStream.close();
            if (eOutputStream != null)
                eOutputStream.close();
            if (eClientSocket != null)
                eClientSocket.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 读取字节数据
     *
     * @return
     */
    private byte[] readBytes() {
        byte buffer[] = new byte[10240];
        byte data[] = null;
        try {
            int length = eInputStream.read(buffer);
            data = new byte[length];
            System.arraycopy(buffer, 0, data, 0, length);
            for (int i = 0; i < data.length; i++) {
                if (data[i] <= 0) {
                    valueLength = i;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return data;
        }
    }
}