package sample.communication;

import javax.comm.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.TooManyListenersException;

public class COMConnect implements Runnable, SerialPortEventListener {

    static Enumeration portList;
    InputStream inputStream;
    SerialPort serialPort;
    Thread readThread;
    private CommPortIdentifier portId;

    public Thread getReadThread() {
        return readThread;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public COMConnect(CommPortIdentifier portId, Map<String, Integer> serialPortParameters) {
        this.portId = portId;
        try {
            serialPort = (SerialPort) portId.open("COMConnectApp", 2000);
        } catch (PortInUseException e) {
        }
        try {
            inputStream = serialPort.getInputStream();
        } catch (IOException e) {
        }
        try {
            serialPort.addEventListener(this);
        } catch (TooManyListenersException e) {
        }
        serialPort.notifyOnDataAvailable(true);
        try {
            setSerialParams(serialPortParameters.get("baudRate"),serialPortParameters.get("dataBits"),
                    serialPortParameters.get("stopBits"), serialPortParameters.get("parity"),
                    serialPortParameters.get("flowControl"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        readThread = new Thread(this);
        readThread.start();
    }

    public void run() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
        }
    }

    public void setSerialParams(int baudRate, int dataBits, int stopBits, int parity,
                                 int flowControl) throws Exception {
        try {
            serialPort.setSerialPortParams(baudRate, dataBits, stopBits,
                    0);
        } catch (UnsupportedCommOperationException e) {
        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.DATA_AVAILABLE:
                byte[] readBuffer = new byte[20];

                try {
                    while (inputStream.available() > 0) {
                        int numBytes = inputStream.read(readBuffer);

                    }
                    System.out.print(new String(readBuffer));
                } catch (IOException e) {
                }
                break;
        }
    }
}
