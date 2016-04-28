package ccat_view;

import static com.sun.javafx.PlatformUtil.isEmbedded;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Preloader;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 *
 * @author Elliott
 */
public class MyFirstPreloader extends Preloader {

    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("SplashScreen.fxml"));
        Scene scene = new Scene(root);
        this.stage.getIcons().add(new Image("/medicalIcon.png"));
        this.stage.initStyle(StageStyle.UNDECORATED);
        this.stage.setScene(scene);
        this.stage.show();
//        DoubleProperty opacity = root.opacityProperty();
//        Timeline ft = new Timeline(
//                new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)),
//                new KeyFrame(new Duration(4000), new KeyValue(opacity, 1.0))
//        );
//        ft.play();
        FadeTransition ft = new FadeTransition(
                Duration.millis(4000), stage.getScene().getRoot());
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
        ft.setOnFinished((ActionEvent) -> {
            stage.hide();
        });
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification e) {
        if (e.getType() == StateChangeNotification.Type.BEFORE_START) {
            if (stage.isShowing()) {
//                DoubleProperty opacity = stage.getScene().getRoot().opacityProperty();
//                Timeline ft = new Timeline(
//                        new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)),
//                        new KeyFrame(new Duration(4000), new KeyValue(opacity, 1.0))
//                );
//                ft.play();
//                FadeTransition ft = new FadeTransition(
//                    Duration.millis(4000), stage.getScene().getRoot());
//                ft.setFromValue(0.0);
//                ft.setToValue(1.0);
//                ft.play();
//                ft.setOnFinished((ActionEvent) -> {
//                    stage.hide();
//                });
            } else {
                stage.hide();
            }
        }
    }

}
