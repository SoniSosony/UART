package sample.communication;

import sample.Controller;
import javax.comm.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class COMConnect implements Runnable, SerialPortEventListener {

    InputStream inputStream;
    OutputStream outputStream;
    SerialPort serialPort;
    Thread readThread;
    private CommPortIdentifier portId;
    private int symbolCount;
    private LocalDateTime oldTime;
    private HashMap<String, Integer> collectedData;
    private byte[] readBuffer;
    private Controller controller;

    public void setCollectedData(HashMap<String, Integer> collectedData) {
        this.collectedData = collectedData;
    }

    public HashMap<String, Integer> getCollectedData() {
        return collectedData;
    }

    public int getSymbolCount() {
        return symbolCount;
    }

    public void setSymbolCount(int symbolCount) {
        this.symbolCount = symbolCount;
    }

    public byte[] getReadBuffer() {
        return readBuffer;
    }

    public Thread getReadThread() {
        return readThread;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public COMConnect(CommPortIdentifier portId, Map<String, Integer> serialPortParameters,
                      Controller controller) throws IOException {
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
            outputStream = serialPort.getOutputStream();
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

        symbolCount = 0;
        oldTime = LocalDateTime.now();
        collectedData = new HashMap<String, Integer>();
        this.controller = controller;
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
                    parity);
            serialPort.setFlowControlMode(flowControl);
        } catch (UnsupportedCommOperationException e) {
        }
    }

    public void sendData(String data) throws IOException {
        try {
            outputStream.write(data.getBytes());
        } catch (IOException e) {
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
                readBuffer = new byte[20];

                try {
                    while (inputStream.available() > 0) {
                        int numBytes = inputStream.read(readBuffer);
                        System.out.println(numBytes);
                        symbolCount++;
                        LocalDateTime now = LocalDateTime.now();
                        collectData(now);
                    }

                    controller.updateReceiveTextArea();
                    System.out.println(new String(readBuffer));
                } catch (IOException e) {
                }
                break;
        }
    }

    public void collectData(LocalDateTime time)
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        if (dtf.format(time).compareTo(dtf.format(oldTime)) != 0) {
            symbolCount = 1;
        }
        collectedData.put(dtf.format(time), symbolCount);
        oldTime = time;
    }

    public SortedSet<String> getSortedKeys() {
        SortedSet<String> keys = new TreeSet<>(comparator);
        keys.addAll(collectedData.keySet());
        return keys;
    }

    Comparator<String> comparator = (String o1, String o2) -> {
        try {
            Date date1=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(o1);
            Date date2=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(o2);
            return date1.compareTo(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    };
}
