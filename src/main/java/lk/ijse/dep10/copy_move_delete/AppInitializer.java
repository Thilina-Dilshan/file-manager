package lk.ijse.dep10.copy_move_delete;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AppInitializer extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(
                new FXMLLoader(this.getClass().getResource("/view/MainScene.fxml")).load()));
        primaryStage.setTitle("File/ Directory Manager");
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
}
