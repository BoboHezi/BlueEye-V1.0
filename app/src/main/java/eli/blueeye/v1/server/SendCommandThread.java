package eli.blueeye.v1.server;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SendCommandThread extends Thread {

    private static final String TAG = "TestCommand";
    private final String host = "10.42.0.1";
    private final int port = 15231;

    private int commandData;

    private Socket clientSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private int valueLength = 32;

    public SendCommandThread(int commandData) {
        this.commandData = commandData;
    }

    @Override
    public void run() {
        try {
            clientSocket = new Socket(host, port);

            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new DataOutputStream(clientSocket.getOutputStream());

            byte buffers[];

            //接收验证数据
            buffers = readBytes();
            String value = new String(buffers, 0, valueLength);
            //转为long
            long verifyData = Long.parseLong(value);

            //乘以3
            verifyData = verifyData * 3;
            //发送验证数据
            outputStream.write((verifyData + "").getBytes());

            //接收验证结果
            buffers = readBytes();

            //发送控制命令
            outputStream.writeInt(commandData);

            //结束
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
            if (clientSocket != null)
                clientSocket.close();
            Log.i(TAG, "Closed...");
        } catch (Exception e) {
            Log.e(TAG, "", e);
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
            int length = inputStream.read(buffer);
            data = new byte[length];
            System.arraycopy(buffer, 0, data, 0, length);
            for (int i = 0; i < data.length; i++) {
                Log.i(TAG, "readBytes: " + data[i]);
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