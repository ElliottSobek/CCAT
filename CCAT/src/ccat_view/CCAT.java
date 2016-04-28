package ccat_view;

import com.sun.javafx.application.LauncherImpl;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author John, Elliott, Zac
 */
public class CCAT extends Application  {
    
    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        LauncherImpl.launchApplication(CCAT.class, MyFirstPreloader.class, args);
    }

    /**
     * 
     * @param primaryStage
     * @throws Exception 
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        displayLoginScene(primaryStage);
    }
    
    /**
     * 
     * @param stage
     * @throws IOException 
     */
    private void displayLoginScene(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("MainMenu/MainMenu.fxml"));
        Scene scene = new Scene(root);
        stage.getIcons().add(new Image("/medicalIcon.png"));
        stage.setTitle("Critical Care Audit Tool - CONFIDENTIAL");
        stage.setScene(scene);
        stage.show();
    }

}
