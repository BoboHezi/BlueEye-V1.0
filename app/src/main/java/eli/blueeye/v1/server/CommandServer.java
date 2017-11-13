package eli.blueeye.v1.server;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 控制数据发送类
 */
public class CommandServer {

    private static final String TAG = "CommandServer";
    private final int port = 15231;
    private String host = "192.168.2.20";
    private Socket clientSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public CommandServer(String host) {
        this.host = host;
    }

    /**
     * 连接到Server
     */
    public void connect() {
        if (isConnected()) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "try to connect server...");
                    clientSocket = new Socket();
                    clientSocket.connect(new InetSocketAddress(host, port), 3000);
                    clientSocket.setSoTimeout(2000);
                    Log.i(TAG, "connect to server...");

                    inputStream = new DataInputStream(clientSocket.getInputStream());
                    outputStream = new DataOutputStream(clientSocket.getOutputStream());

                    verify();
                } catch (Exception e) {
                    Log.i(TAG, e.getMessage());
                }
            }
        }).start();
    }

    /**
     * 是否处于连接状态
     *
     * @return
     */
    public boolean isConnected() {
        if (clientSocket == null)
            return false;
        try {
            clientSocket.sendUrgentData(0xff);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 发送控制数据
     */
    public void sendData(final int value) {
        if (!isConnected())
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //send command data
                    outputStream.write(intToByteArray(value));
                    Log.i(TAG, "send command data: " + value);
                } catch (Exception e) {
                    Log.i(TAG, e.getMessage());
                }
            }
        }).start();
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        try {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
            if (clientSocket != null)
                clientSocket.close();
            Log.i(TAG, "disconnected");
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        } finally {
            clientSocket = null;
        }
    }

    /**
     * 验证设备
     */
    private boolean verify() {
        try {
            byte buffers[];

            //receive verify data
            buffers = readBytes();
            //parse to long type
            int verifyData = bytesToInt(buffers);
            Log.i(TAG, "receive verify data: " + verifyData);

            //3 times the data
            verifyData = verifyData * 3;
            //send verify data
            outputStream.write(intToByteArray(verifyData));
            Log.i(TAG, "send verify data: " + verifyData);

            //receive verify result
            buffers = readBytes();
            Log.i(TAG, new String(buffers));
            return true;
        } catch (Exception e) {
            Log.i(TAG, "verify failed");
            disconnect();
            return false;
        }
    }

    /**
     * 释放资源
     */
    public void destroy() {
        try {
            disconnect();
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
            if (clientSocket != null)
                clientSocket.close();
        } catch (Exception e) {
        } finally {
            inputStream = null;
            outputStream = null;
            clientSocket = null;
        }
    }

    /**
     * 读取字节数组
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
        } catch (IOException e) {
        } finally {
            return data;
        }
    }

    /**
     * 将字节数组转为整形
     *
     * @return
     */
    private int bytesToInt(byte bytes[]) {
        int time = 1;
        int result = 0;
        for (int i = bytes.length - 1; i >= 0; i--) {
            short temp = bytes[i];
            if (temp < 0) {
                temp = (short) (temp + 256);
            }
            result += temp * time;
            time *= 256;
        }
        return result;
    }

    /**
     * int to array
     *
     * @param value
     * @return
     */
    private byte[] intToByteArray(int value) {
        byte[] result = new byte[4];
        //由高位到低位
        result[0] = (byte) ((value >> 24) & 0xFF);
        result[1] = (byte) ((value >> 16) & 0xFF);
        result[2] = (byte) ((value >> 8) & 0xFF);
        result[3] = (byte) (value & 0xFF);
        return result;
    }
}
