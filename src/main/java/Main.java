import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
//        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        URL url = new File("src/main/main.fxml").toURI().toURL();
        Parent root = FXMLLoader.load(url);
        primaryStage.setTitle("JResponse");
        primaryStage.setScene(new Scene(root, 1000, 800));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    public static void main(String[] args) throws Exception {
//        PowerShellResponse response = PowerShell.executeSingleCommand("Get-Process");
//        System.out.println("List Processes:" + response.getCommandOutput());
        launch(args);
    }

    // TODO: create host count
    // TODO: handle network tunnels; /32 causes runtime exception
}