import datamodel.*;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.util.List;

public class Controller {
    @FXML
    private BorderPane mainPanel;

    @FXML
    private TextField startRange;

    @FXML
    private TextField endRange;

    @FXML
    private ChoiceBox<String> networkChoice;

    @FXML
    private TableView<PingParrallel.PingResult> ipTable;

    @FXML
    private TableView<PingParrallel.PingResult> ipListView;

    @FXML
    private TextField ipTextField;

    @FXML
    private RadioButton singleMode;

    @FXML
    private RadioButton batch;

    @FXML
    private TextArea enumTextArea;

    private NetworkDiscovery networkDiscovery;
    private SqlDbConnection db_conn;
    private WmiScripts scripts;

    // creates a NetworkDiscovery instance that gets cidr information
    public void initialize() {
        networkDiscovery = new NetworkDiscovery();
        setIPRange();
        ipListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);  // allows to select multiple ips
        // create db conn instance
        db_conn = new SqlDbConnection("192.168.4.113","asoa","dotdotelectricshot","659_project");
        scripts = new WmiScripts();  // call singleton class to create scripts that correspond to button names
    }

    // sets the startRange and endRange values
    @FXML
    public void onNetworkSelected() {
        List<NetworkDiscovery.NetworkInfo> nwList = networkDiscovery.getNwInfo();
        String item = networkChoice.getSelectionModel().getSelectedItem();
        NetworkDiscovery.NetworkInfo nwInfo = null;
        for (NetworkDiscovery.NetworkInfo nw : nwList) {
            try {
                if (nw.getNetworkAndCidr().equals(item)) {
                    startRange.setText(nw.getNwIPRange().get(0));
                    endRange.setText(nw.getNwIPRange().get(1));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // calls pingHosts() when scan button is selected
    @FXML
    public void onScanSelected() {
        if(ipTable.getItems().size() > 1) {
            ipTable.getItems().clear();  // clears ipTable table of current items
            networkDiscovery.pingHosts();
            ipTable.setItems(PingParrallel.getAliveHosts());
            TableColumn<PingParrallel.PingResult, String> ipAddress = new TableColumn<PingParrallel.PingResult, String>("Reachable Hosts");
            ipAddress.setCellValueFactory(new PropertyValueFactory("ipAddress"));
            TableColumn<PingParrallel.PingResult, String> hostname = new TableColumn<PingParrallel.PingResult, String>("Hostname");
            hostname.setCellValueFactory(new PropertyValueFactory("hostname"));
            ipTable.getColumns().setAll(ipAddress,hostname);
            ipListView.setItems(PingParrallel.getAliveHosts()); // updates the Enumeration tab ip list
//            db_conn.dbSelect();
            db_conn.dbInsert();
        } else {
            networkDiscovery.pingHosts();
            ipTable.setItems(PingParrallel.getAliveHosts());
            TableColumn<PingParrallel.PingResult, String> ipAddress = new TableColumn<PingParrallel.PingResult, String>("Reachable Hosts");
            ipAddress.setCellValueFactory(new PropertyValueFactory("ipAddress"));
            TableColumn<PingParrallel.PingResult, String> hostname = new TableColumn<PingParrallel.PingResult, String>("Hostname");
            hostname.setCellValueFactory(new PropertyValueFactory("hostname"));
            ipTable.getColumns().setAll(ipAddress,hostname);
            ipListView.setItems(PingParrallel.getAliveHosts()); // updates the Enumeration tab ip list
//            db_conn.dbSelect();
            db_conn.dbInsert();
        }
    }

    @FXML
    public void toggleTextField(ActionEvent e) {
        if(e.getSource().equals(singleMode)) {
            ipTextField.setDisable(false);
        } else {
            ipTextField.setDisable(true);
        }
    }

    @FXML
    public void handleButtonClick(ActionEvent e) {
        enumTextArea.setText("");
        System.out.println("button pressed: " + e.toString());
        ObservableList<PingParrallel.PingResult> results = ipListView.getSelectionModel().getSelectedItems();  // get PingResult objects from ListView
//        for(PingParrallel.PingResult result: results) {
//            System.out.println(result.getIpAddress());
//        }
        String buttonName = ((Button)e.getSource()).getText();  // get button name
        String script = scripts.getScript(buttonName);
        WmiParrallel wmi = new WmiParrallel(buttonName, script, results);  // call wmi constructor
        // don't think this line is needed because the wmi class loops over the results to build callables
        while(wmi.getThreadsDone().equals(false)) {}  // loop while threads are still running
        enumTextArea.setText(wmi.getFutureResults()); // writes the callable toString() to the text area
        // write to db from here?
        wmi.setThreadsDone(false);
    }

    @FXML
    public void testFunc() {
        System.out.println("label clicked");
    }

    private void setIPRange() {
        for (NetworkDiscovery.NetworkInfo nw : networkDiscovery.getNwInfo()) {
            networkChoice.getItems().add(nw.getNetworkAndCidr());
        }
        networkChoice.getSelectionModel().selectFirst();
    }
}
