package com.example.qr_test;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ScanActivity extends SerialPortActivity {
    private static final String TAG = "Scan";
    private static final String ACTION_DEVICE_PERMISSION = "com.tool.UART_PERMISSION";
    public static final int HANDLER_SCAN_STATUS = 1;
    public static final int HANDLER_SCAN_ONE_DEC = 2;
    public static final int HANDLER_SCAN_TWO_DEC = 3;
    public static final int HANDLER_SCAN_TEXT = 4;
    public static final int HANDLER_SCAN_CLEAR = 5;
    public LinkedList<byte[]> SendArray = new LinkedList();
    public LinkedList<byte[]> RecvArray = new LinkedList();
    public LinkedList<byte[]> DecodeArray = new LinkedList();
    private static final int TYPE_PROCESS_CODE = 1;
    private static final int TYPE_SOURCE = 2;
    private static final int TYPE_STATUS = 3;
    private static final int HID_ACK_TYPE = 0;
    private static final int HID_DECODE_TYPE = 1;
    private static final byte PROCODE_CMD_ACK = -48;
    private static final byte PROCODE_CMD_NAK = -47;
    private static final byte PROCODE_ONE_DECODE = -13;
    private static final byte PROCODE_TWO_DECODE = -12;
    private static final byte PROCODE_START = -28;
    private static final byte PROCODE_STOP = -27;
    private static final byte SOURCE_DEVICE = 0;
    private static final byte SOURCE_HOST = 4;
    private static final byte STATUS_FIRST_TEMP = 0;
    private static final byte STATUS_FIRST_FOREVER = 8;
    private static final byte STATUS_REPEAT_TEMP = 1;
    private static final byte STATUS_REPEAT_FOREVER = 9;
    private static final int CTS_DECODE_DATA_LEN = 6144;
    private Context myContext;
    private Handler myHandler;
    private boolean DecodeRun = false;
    protected OutputStream mScanOutputStream;
    protected InputStream mScanInputStream;

    public ScanActivity(Context context, Handler handler, OutputStream out, InputStream in) {
        this.myContext = context;
        this.myHandler = handler;
        this.mScanOutputStream = out;
        this.mScanInputStream = in;
        this.DecodeRun = true;
        new Thread(new ScanRunnable()).start();
        new Thread(new DecodeRunnable()).start();
    }

    private void Sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException var4) {
            var4.printStackTrace();
        }

    }

    public void memset(byte[] str, byte fill, int len) {
        for(int i = 0; i < len; ++i) {
            str[i] = fill;
        }

    }

    public static int readInputStreamWithTimeout(InputStream is, byte[] b, int timeoutMillis) throws IOException {
        int bufferOffset = 0;

        int readResult;
        for(long maxTimeMillis = System.currentTimeMillis() + (long)timeoutMillis; System.currentTimeMillis() < maxTimeMillis && bufferOffset < b.length; bufferOffset += readResult) {
            int readLength = Math.min(is.available(), b.length - bufferOffset);
            readResult = is.read(b, bufferOffset, readLength);
            if (readResult == -1) {
                break;
            }
        }

        return bufferOffset;
    }

    private void ScanMsg(int mode, int type, int status, Object value) {
        Message msg = new Message();
        msg.what = mode;
        msg.arg1 = type;
        msg.arg2 = status;
        msg.obj = value;
        this.myHandler.sendMessage(msg);
    }

    private int Packet(byte code, byte source, byte status, byte[] data, int len, byte[] dest) {
        byte[] crc = new byte[2];
        dest[0] = (byte)(len + 4);
        dest[1] = code;
        dest[2] = source;
        dest[3] = status;
        if (data != null && len != 0) {
            System.arraycopy(data, 0, dest, 4, len);
        }

        this.CheckSum(dest, len + 4, crc);
        System.arraycopy(crc, 0, dest, len + 4, 2);
        return len + 6;
    }

    private int HidPacket(byte[] data, int datalen, byte[] hid, int max) {
        int offset = 0;
        int hidcnt = 0;

        while(datalen > offset) {
            Arrays.fill(hid, (byte)0);
            hid[0] = 1;
            hid[1] = (byte)(datalen - offset > 56 ? 56 : datalen - offset);
            System.arraycopy(data, offset, hid[hidcnt], 2, hid[1]);
            hid[58] = 0;
            hid[59] = 0;
            hid[60] = 0;
            hid[61] = 0;
            hid[62] = 0;
            if (datalen - offset > 56) {
                hid[63] = 1;
            } else {
                hid[63] = 0;
            }

            offset += 56;
            ++hidcnt;
            if (hidcnt > max) {
                break;
            }
        }

        return hidcnt;
    }

    private byte ParseParam(int type, byte[] data, int len) {
        byte[] crc = new byte[2];
        if (len < 6) {
            return -1;
        } else {
            this.CheckSum(data, len - 2, crc);
            if (crc[0] == data[len - 2] && crc[1] == data[len - 1]) {
                if (data[0] == -1) {
                    int packet_len = data[2] & 255;
                    packet_len <<= 8;
                    packet_len += data[3] & 255;
                    if (len >= 9 && packet_len == len - 2) {
                        if (type == 1) {
                            return data[1];
                        } else if (type == 2) {
                            return data[5];
                        } else {
                            return type == 3 ? data[6] : -1;
                        }
                    } else {
                        return -1;
                    }
                } else if ((data[0] & 255) != len - 2) {
                    return -1;
                } else if (type == 1) {
                    return data[1];
                } else if (type == 2) {
                    return data[2];
                } else {
                    return type == 3 ? data[3] : -1;
                }
            } else {
                return -1;
            }
        }
    }

    private int ParseData(byte[] data, int len, byte[] result) {
        byte[] crc = new byte[2];
        if (len <= 6) {
            return 0;
        } else {
            this.CheckSum(data, len - 2, crc);
            if (crc[0] == data[len - 2] && crc[1] == data[len - 1]) {
                if (data[0] == -1) {
                    int packet_len = data[2] & 255;
                    packet_len <<= 8;
                    packet_len += data[3] & 255;
                    if (len >= 9 && packet_len == len - 2) {
                        System.arraycopy(data, 7, result, 0, len - 9);
                        return len - 9;
                    } else {
                        return 0;
                    }
                } else if (data[0] != len - 2) {
                    return 0;
                } else {
                    System.arraycopy(data, 4, result, 0, len - 6);
                    return len - 6;
                }
            } else {
                return 0;
            }
        }
    }

    private boolean CheckSum(byte[] pData, int usLen, byte[] crc) {

        int ucSum = 0;
        if (pData != null && usLen != 0) {
            while(usLen-- != 0) {
                ucSum += pData[usLen] & 255;
            }
            ucSum = ~ucSum + 1;
            crc[0] = (byte)(ucSum >> 8 & 255);
            crc[1] = (byte)(ucSum & 255);
            return true;
        } else {
            return false;
        }
    }



    private void LogSetMsgHex(String log, byte[] data, int len) {
        String str = log + ": ";

        for(int i = 0; i < len; ++i) {
            str = str + Integer.toHexString(data[i] & 255) + " ";
        }

        Log.w("Scan", str);
    }

    private int UartSend(byte[] send, int send_len) {
        byte[] uartsend = new byte[send_len];
        Arrays.fill(uartsend, (byte)0);

        for(int i = 0; i < send_len; ++i) {
            uartsend[i] = send[i];
        }

        try {
            this.mScanOutputStream.write(uartsend);
        } catch (IOException var5) {
            var5.printStackTrace();
        }

        return 0;
    }

    private int UartRecv(int type, byte[] recv, int timeout) {
        int recv_len = 0;
        long startTime = System.currentTimeMillis();
        long PackStartTime = System.currentTimeMillis();
// 데이터 조립을 위한 버퍼
        List<Byte> buffer = new ArrayList<>();

        while(true) {
            byte[] CurRecv = null;
            if (type == 0) {
                CurRecv = (byte[])this.RecvArray.poll();
            } else {
                if (type != 1) {
                    return recv_len;
                }
                CurRecv = (byte[])this.DecodeArray.poll();

            }

            if (CurRecv == null) {
                long curTime = System.currentTimeMillis();
                if (recv_len > 0) {
                    if (curTime - PackStartTime > 500L) {
                        return recv_len;
                    }

                    this.Sleep(5L);
                } else {
                    if (curTime - startTime > (long)timeout) {
                        return recv_len;
                    }

                    this.Sleep(5L);
                }
            } else {
                for (byte b : CurRecv) {
                    buffer.add(b);
                }
                recv_len += CurRecv.length;
                PackStartTime = System.currentTimeMillis();
                this.Sleep(5L);
            }
            // 버퍼에서 최종 데이터를 배열로 변환하여 recv에 복사
            for (int i = 0; i < buffer.size(); i++) {
                recv[i] = buffer.get(i);
            }

        }
    }

    private boolean TimeCheck() {
        return true;
    }

    public boolean StartDecode(int node) {
        byte[] send = new byte[1024];
        byte[] recv = new byte[1024];

        Log.w("Scan", "StartDecode");
        if (!this.TimeCheck()) {
            return false;
        } else {
            int send_len = this.Packet((byte)-28, (byte)4, (byte)0, (byte[])null, 0, send);
            this.UartSend(send, send_len);
            int recv_len = this.UartRecv(0, recv, 1000);
            byte code = this.ParseParam(1, recv, recv_len);
            this.ParseParam(2, recv, recv_len);
            this.ParseParam(3, recv, recv_len);
            if (code == -48) {
                this.ScanMsg(4, 0, 0, "start 성공");
                return true;
            } else if (code == -47) {
                this.ScanMsg(4, 0, 0, "start 실패");
                return true;
            } else {
                Log.w("Scan", "PACKET EXCEPTION");
                this.ScanMsg(4, 0, 0, "报文异常-发送：" + send_len + "接收:" + recv_len);
                return false;
            }
        }
    }

    public boolean StopDecode(int node) {
        byte[] send = new byte[1024];
        byte[] recv = new byte[1024];

        if (!this.TimeCheck()) {
            return false;
        } else {
            int send_len = this.Packet((byte)-27, (byte)4, (byte)0, (byte[])null, 0, send);
            this.UartSend(send, send_len);
            int recv_len = this.UartRecv(0, recv, 1000);
            byte code = this.ParseParam(1, recv, recv_len);
            this.ParseParam(2, recv, recv_len);
            this.ParseParam(3, recv, recv_len);
            if (code == -48) {
                this.ScanMsg(4, 0, 0, "stop 성공");
                return true;
            } else if (code == -47) {
                this.ScanMsg(4, 0, 0, "stop 실패");
                return true;
            } else {
                Log.w("Scan", "PACKET EXCEPTION");
                this.ScanMsg(4, 0, 0, "报文异常-发送：" + send_len + "接收:" + recv_len);
                return false;
            }
        }
    }

    public void exit() {
        this.DecodeRun = false;
    }

    private class DecodeRunnable implements Runnable {
        private DecodeRunnable() {
        }

        public void run() {
            byte[] send = new byte[1024];
            byte[] recv = new byte[1024];
            boolean RetryFlag = false;
            byte[] data = new byte[1024];
            byte[] decode = new byte[1024];
            byte[] Resend = new byte[]{1, 0};

            while(ScanActivity.this.DecodeRun) {
                int recv_lenx = ScanActivity.this.UartRecv(1, recv, 500);

                if (recv_lenx > 0) {
                    byte code = ScanActivity.this.ParseParam(1, recv, recv_lenx);
                    ScanActivity.this.ParseParam(2, recv, recv_lenx);
                    ScanActivity.this.ParseParam(3, recv, recv_lenx);

                    int data_lenx = ScanActivity.this.ParseData(recv, recv_lenx, data);
                    int send_lenx;
                    if (data_lenx == 0) {
                        send_lenx = ScanActivity.this.Packet((byte)-47, (byte)4, (byte)0, Resend, 1, send);
                        ScanActivity.this.UartSend(send, send_lenx);
                    }
                    if (data_lenx > 0) {
                        ScanActivity.this.memset(decode, (byte)0, decode.length);
                        System.arraycopy(data, 1, decode, 0, data_lenx - 1);
                        int decode_lenx = data_lenx - 1;
                        send_lenx = ScanActivity.this.Packet((byte)-48, (byte)4, (byte)0, (byte[])null, 0, send);
                        ScanActivity.this.UartSend(send, send_lenx);
                        if (code == -13) {
                            ScanActivity.this.ScanMsg(2, 0, 0, new String(decode));
                        } else {
                            ScanActivity.this.ScanMsg(3, 0, 0, new String(decode));
                        }
                    }
                }
            }

        }
    }



    private class ScanRunnable implements Runnable {
        private ScanRunnable() {
        }

        public void run() {
            byte[] RecvBuf = new byte[1024];
            int size = 0;

            while(ScanActivity.this.DecodeRun) {
                try {
                    size = ScanActivity.readInputStreamWithTimeout(ScanActivity.this.mScanInputStream, RecvBuf, 100);
                } catch (IOException var8) {
                    var8.printStackTrace();
                }

                if (size > 0) {
                    byte[] uartRecvBuf = new byte[size];

                    for(int i = 0; i < size; ++i) {
                        uartRecvBuf[i] = RecvBuf[i];
                    }

                    if (uartRecvBuf[0] == 4 && uartRecvBuf[1] == -48 && uartRecvBuf[2] == 0 || uartRecvBuf[0] == 5 && uartRecvBuf[1] == -47 && uartRecvBuf[2] == 0) {
                        ScanActivity.this.RecvArray.add((byte[])uartRecvBuf.clone());
                    } else {
                        ScanActivity.this.DecodeArray.add((byte[])uartRecvBuf.clone());
                    }
                }
            }

        }
    }
}
