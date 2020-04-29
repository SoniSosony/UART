package sample;

import sample.communication.COMConnect;

import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

import java.util.Enumeration;
import java.util.Set;

public class Controller {

    private Enumeration portList;
    private Thread readThread;
    private boolean isConnect = false;
    private SerialPort serialPort;

    @FXML
    private Button connectButton;

    @FXML
    private Button scanButton;

    @FXML
    private ComboBox<String> comboBoxPorts;

    private Object COMConnect;

    @FXML
    public void onButtonClicked(ActionEvent e) {
        if (e.getSource().equals(scanButton)) {
            portList = CommPortIdentifier.getPortIdentifiers();
            comboBoxPorts.getItems().clear();

            CommPortIdentifier portId;
            while (portList.hasMoreElements()) {
                portId = (CommPortIdentifier) portList.nextElement();
                if (portId.getName().charAt(0) == 'C') {
                    System.out.println(portId.getName());
                    comboBoxPorts.getItems().add(portId.getName());
                }
            }
        }
        if (e.getSource().equals(connectButton)) {
            if (isConnect) {
                serialPort.close();
                isConnect = false;
                connectButton.setText("Connect");
            } else {
//                if (readThread != null) {
//                    Set<Thread> setOfThread = Thread.getAllStackTraces().keySet();
//                    for (Thread thread : setOfThread) {
//                        if (thread.getId() == readThread.getId())
//                            System.out.println("Thread found");
//                        thread.interrupt();
//                    }
//                }

                System.out.println("Connecting...");
                portList = CommPortIdentifier.getPortIdentifiers();
                CommPortIdentifier portId;
                while (portList.hasMoreElements()) {
                    portId = (CommPortIdentifier) portList.nextElement();
                    if (portId.getName().equals(comboBoxPorts.getValue())) {
                        COMConnect reader = new COMConnect(portId);
                        serialPort = reader.getSerialPort();
                        readThread = reader.getReadThread();
                        System.out.println("New reader created");
                        isConnect =true;
                        connectButton.setText("Disconnect");
                    }
                }
            }
        }
    }
}
