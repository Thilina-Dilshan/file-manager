package lk.ijse.dep10.copy_move_delete.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Optional;

public class MainSceneController {

    @FXML
    private Button btnCopy;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnMove;

    @FXML
    private Button btnSourceOpen;

    @FXML
    private Button btnTargetOpen;

    @FXML
    private Label lblProgress;

    @FXML
    private ProgressBar prg;

    @FXML
    private TextField txtSource;

    @FXML
    private TextField txtTarget;


    public void initialize() {
        btnSourceOpen.requestFocus();
    }

    @FXML
    void btnSourceOpenOnAction(ActionEvent event) throws Exception {

    }

    public void btnTargetOnAction(ActionEvent actionEvent) {

    }

    @FXML
    void btnCopyOnAction(ActionEvent event) throws Exception {


    }

    @FXML
    void btnMoveOnAction(ActionEvent event) {

    }

    public void btnDeleteOnAction(ActionEvent actionEvent) {

    }

}
