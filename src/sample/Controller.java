package sample;

import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import sample.communication.COMConnect;
import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class Controller {

    private Enumeration portList;
    private Thread readThread;
    private boolean isConnect = false;
    private SerialPort serialPort;
    private COMConnect reader;

    @FXML
    private Button connectButton;

    @FXML
    private Button scanButton;

    @FXML
    private ComboBox<String> comboBoxPorts;

    @FXML
    private ToggleGroup $baudRateToggleGroup;

    @FXML
    ToggleGroup baudRateToggleGroup;

    @FXML
    ToggleGroup dataBitsToggleGroup;

    @FXML
    ToggleGroup parityToggleGroup;

    @FXML
    ToggleGroup stopBitsToggleGroup;

    @FXML
    ToggleGroup flowControlToggleGroup;

    @FXML
    public void onButtonClicked(ActionEvent e) throws Exception {
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
                System.out.println("Connecting...");
                portList = CommPortIdentifier.getPortIdentifiers();
                CommPortIdentifier portId;
                while (portList.hasMoreElements()) {
                    portId = (CommPortIdentifier) portList.nextElement();
                    if (portId.getName().equals(comboBoxPorts.getValue())) {
                        Map<String, Integer> map = getAllRadioButtonData();
                        reader = new COMConnect(portId, map);
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

    @FXML
    public void onRadioButtonClicked(ActionEvent e) throws Exception {
        Map<String, Integer> map = getAllRadioButtonData();
        if (reader != null) {
            reader.setSerialParams(map.get("baudRate"), map.get("dataBits"), map.get("stopBits"),
                    map.get("parity"), map.get("flowControl"));
        }
    }

    private Map<String, Integer> getAllRadioButtonData() {
        Map<String, Integer> map = new HashMap<>();
        RadioButton selectedRadioButton = (RadioButton) baudRateToggleGroup.getSelectedToggle();
        map.put("baudRate", Integer.parseInt(selectedRadioButton.getText()));

        selectedRadioButton = (RadioButton) dataBitsToggleGroup.getSelectedToggle();
        map.put("dataBits", Integer.parseInt(selectedRadioButton.getText()));

        selectedRadioButton = (RadioButton) parityToggleGroup.getSelectedToggle();
        map.put("parity", Integer.parseInt(selectedRadioButton.getUserData().toString()));

        selectedRadioButton = (RadioButton) stopBitsToggleGroup.getSelectedToggle();
        map.put("stopBits", Integer.parseInt(selectedRadioButton.getUserData().toString()));

        selectedRadioButton = (RadioButton) flowControlToggleGroup.getSelectedToggle();
        map.put("flowControl", Integer.parseInt(selectedRadioButton.getUserData().toString()));

        return map;
    }
}
