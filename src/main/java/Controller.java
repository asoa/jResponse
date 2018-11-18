import datamodel.*;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
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

    private Service<ObservableList<PingParrallel.PingResult>> ping_service;

    private NetworkDiscovery networkDiscovery;
    private SqlDbConnection db_conn;
    private WmiScripts scripts;
    private String ip;
    private String user;
    private String password;
    private String dbName;

    // creates a NetworkDiscovery instance that gets cidr information
    public void initialize() throws ClassNotFoundException {
        networkDiscovery = new NetworkDiscovery();
        setIPRange(); // sets the cidr information in the drop down box
        ipListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);  // allows to select multiple ips


        // bind the service returned observable list to the listview
        ping_service = new PingParrallel(networkDiscovery.getHostList());
//        ((PingParrallel) ping_service).getAliveHosts();
        ipTable.itemsProperty().bind(ping_service.valueProperty());
        TableColumn<PingParrallel.PingResult, String> ipAddress = new TableColumn<PingParrallel.PingResult, String>("Reachable Hosts");
        ipAddress.setCellValueFactory(new PropertyValueFactory("ipAddress"));  // set the ipaddress column of the tablecolumn
        TableColumn<PingParrallel.PingResult, String> hostname = new TableColumn<PingParrallel.PingResult, String>("Hostname");
        hostname.setCellValueFactory(new PropertyValueFactory("hostname"));
        ipTable.getColumns().setAll(ipAddress,hostname);
        ipListView.itemsProperty().bind(ping_service.valueProperty()); // updates the Enumeration tab ip list

        // get db info
//        Scanner s = new Scanner(System.in);
//        System.out.println("What is the ip for the db?");
//        this.ip = s.next();
//        System.out.println("What is the username?");
//        this.user = s.next();
//        System.out.println("What is the password");
//        this.password = s.next();
//        System.out.println("What is the database name?");
//        this.dbName = s.next();
        db_conn = new SqlDbConnection("192.168.3.155", "user2", "dotdotelectricshot", "project");  // connect to db
        db_conn.createTables();
        scripts = new WmiScripts();  // call singleton class to create scripts that correspond to button names
    }

    // sets the startRange and endRange values
    // TODO: allow user to manual select range
    @FXML
    public void onNetworkSelected() {
        List<NetworkDiscovery.NetworkInfo> nwList = networkDiscovery.getNwInfo();
        String item = networkChoice.getSelectionModel().getSelectedItem();
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

    // calls PingParrallel when scan button is selected
    @FXML
    public void onScanSelected() {

        if(ping_service.getState() == Service.State.SUCCEEDED) {
            // write output to db?
            try {
                ObservableList<PingParrallel.PingResult> pingResults = ((PingParrallel) ping_service).getAliveHosts();
                List<PingParrallel.PingResult> pingResultsList = new ArrayList<PingParrallel.PingResult>(pingResults);
                System.out.println("Writing to Database...\n");
                db_conn.dbComputerInsert(pingResultsList);
                for(PingParrallel.PingResult item: pingResultsList) {
                    // TODO: call db_service write method
//                    System.out.printf("%s,%s\n", item.getHostname(), item.getIpAddress());
                }

            } catch (Exception e) {
                System.out.println(e);
            }

            ipTable.getItems().clear();
            ping_service.reset();
            ping_service.start();
        } else if(ping_service.getState() == Service.State.READY) {
            ping_service.start();
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
        String buttonName = ((Button)e.getSource()).getText();  // get button name
        String script = scripts.getScript(buttonName);
        WmiParrallel wmi = new WmiParrallel(buttonName, script, results);  // call wmi constructor
        // don't think this line is needed because the wmi class loops over the results to build callables
        while(wmi.getThreadsDone().equals(false)) {}  // loop while threads are still running
        enumTextArea.setText(wmi.getFutureResults()); // writes the callable toString() to the text area
        // write to db from here?
        wmi.setThreadsDone(false);
    }


    // TODO: fix redundant network list
    private void setIPRange() {
        for (NetworkDiscovery.NetworkInfo nw : networkDiscovery.getNwInfo()) {
            networkChoice.getItems().add(nw.getNetworkAndCidr());
        }
        networkChoice.getSelectionModel().selectFirst();

    }
}
