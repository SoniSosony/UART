package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import sample.communication.COMConnect;
import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.Integer.parseInt;

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
    private Button sendButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button counterButton;

    @FXML
    private Button refreshChartButton;

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
    TextArea transmitTextArea;

    @FXML
    TextArea receiveTextArea;

    @FXML
    TextField counterTime;

    @FXML
    TextField counterASCIITextArea;

    @FXML
    LineChart<String, Number> chartSymbolCount;

    @FXML
    public void initialize() {
        sendButton.setDisable(true);
        counterButton.setDisable(true);

        counterTime.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    counterTime.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
    }

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
                        reader = new COMConnect(portId, map, this);
                        serialPort = reader.getSerialPort();
                        readThread = reader.getReadThread();
                        System.out.println("New reader created");
                        isConnect =true;
                        connectButton.setText("Disconnect");
                    }
                }
            }
        }

        if (e.getSource().equals(sendButton)) {
            String text = transmitTextArea.getText();
            if (reader != null) {
                reader.sendData(text);
            }
        }

        if (e.getSource().equals(counterButton)) {
            if (reader != null) {
                Timer timer = new Timer(true);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        System.out.println("Starting timer task");

                        HashMap<String, Integer> collectedData = reader.getCollectedData();

                        collectedData.clear();
                        reader.setSymbolCount(1);

                        int acquisitionTime = parseInt(counterTime.getText()) * 1000;

                        LocalDateTime time = LocalDateTime.now();
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

                        for (int i = 0; i < acquisitionTime/1000; i++)
                        {
                            time = time.plusSeconds(1);
                            collectedData.put(dtf.format(time), 0);
                        }

                        try {
                            Thread.sleep(acquisitionTime);
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                        timer.cancel();

                        int symbolCountSum = 0;
                        for (Map.Entry<String, Integer> e : collectedData.entrySet()) {
                            symbolCountSum += e.getValue();
                        }
                        counterASCIITextArea.setText(Integer.toString(symbolCountSum));
                        System.out.println("Timer canceled");
                    }
                }, 0);
            }
        }

        if (e.getSource().equals(refreshChartButton)) {
            drawChart();
        }

        if (e.getSource().equals(clearButton)) {
            receiveTextArea.clear();
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

    @FXML
    public void handleKeyReleased() {
        String text = transmitTextArea.getText();
        boolean disableButton = text.isEmpty() || text.trim().isEmpty();
        sendButton.setDisable(disableButton);

        text = counterTime.getText();
        disableButton = text.isEmpty() || text.trim().isEmpty();
        counterButton.setDisable(disableButton);

    }

    private Map<String, Integer> getAllRadioButtonData() {
        Map<String, Integer> map = new HashMap<>();
        RadioButton selectedRadioButton = (RadioButton) baudRateToggleGroup.getSelectedToggle();
        map.put("baudRate", parseInt(selectedRadioButton.getText()));

        selectedRadioButton = (RadioButton) dataBitsToggleGroup.getSelectedToggle();
        map.put("dataBits", parseInt(selectedRadioButton.getText()));

        selectedRadioButton = (RadioButton) parityToggleGroup.getSelectedToggle();
        map.put("parity", parseInt(selectedRadioButton.getUserData().toString()));

        selectedRadioButton = (RadioButton) stopBitsToggleGroup.getSelectedToggle();
        map.put("stopBits", parseInt(selectedRadioButton.getUserData().toString()));

        selectedRadioButton = (RadioButton) flowControlToggleGroup.getSelectedToggle();
        map.put("flowControl", parseInt(selectedRadioButton.getUserData().toString()));

        return map;
    }

    private void drawChart() {

        chartSymbolCount.getData().clear();

        if (reader != null) {
            XYChart.Series<String, Number> series1 = new XYChart.Series<>();
            series1.setName("Series 1");

            HashMap<String, Integer> collectedData = reader.getCollectedData();
            SortedSet<String> sortedKeys = reader.getSortedKeys();

            for (String e: sortedKeys) {
                series1.getData().add(new XYChart.Data<>(e, collectedData.get(e)));
            }
            chartSymbolCount.getData().add(series1);
        }
    }

    public void updateReceiveTextArea() {
        if (reader != null) {
            String text = new String(reader.getReadBuffer());
            receiveTextArea.appendText(text);
        }
    }
}
