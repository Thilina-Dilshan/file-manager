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

    public Button btnCopy;
    public Button btnDelete;
    public Button btnMove;
    public Button btnSourceOpen;
    public Button btnTargetOpen;
    public Label lblProgress;
    public ProgressBar prg;
    public TextField txtSource;
    public TextField txtTarget;

    private File targetFolder;
    private File sourceFile;
    private File copyingToDirectory;
    private JFileChooser chooser;

    private ArrayList<File> sourceFilesArrayList = new ArrayList<>();
    private ArrayList<File> targetFilesArrayList = new ArrayList<>();
    private ArrayList<File> deletingFilesArrayList = new ArrayList<>();
    private ArrayList<String> replacingDirNames = new ArrayList<>();

    private FileInputStream fis = null;
    private FileOutputStream fos = null;
    private boolean isCopying;
    private boolean isMoving;

    public void initialize() {
        btnSourceOpen.requestFocus();
    }

     
    public void btnSourceOpenOnAction(ActionEvent event) throws Exception {
        txtSource.clear();
        chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.showOpenDialog(null);

        if (chooser.getSelectedFile()==null) return;
        sourceFile = chooser.getSelectedFile();
        txtSource.setText(chooser.getSelectedFile().getAbsolutePath());
    }

    public void btnTargetOnAction(ActionEvent actionEvent) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        File targetDirectory = dirChooser.showDialog(null);
        if (targetDirectory==null ) return;
        targetFolder = new File(targetDirectory.getAbsolutePath());
        txtTarget.setText(targetFolder.getAbsolutePath());
    }

     
    public void btnCopyOnAction(ActionEvent event) throws Exception {
        if (!(checkingInitialSteps("Copying"))) return;

        Task<Void> task =new Task<Void>() {
            @Override
            protected Void call() {
                System.out.println("Task started..");
                double progress = 0.0;
                double totalLength = 0.0;

                for (int i = 0; i < sourceFilesArrayList.size(); i++) {
                    String sourceAbsPath = sourceFilesArrayList.get(i).getAbsolutePath();
                    String targetAbsPath = targetFilesArrayList.get(i).getAbsolutePath();

                    try {
                        if (!(sourceAbsPath.equals(targetAbsPath))) {
                            fis = new FileInputStream(sourceFilesArrayList.get(i));
                            fos = new FileOutputStream(targetFilesArrayList.get(i));
                            totalLength += sourceFilesArrayList.get(i).length();

                            while (true) {
                                byte[] buffer = new byte[20];
                                int read = fis.read(buffer);
                                if (read == -1) break;

                                fos.write(buffer, 0, read);

                                progress += read;
                                double status = progress / totalLength * 100.0;

                                updateProgress(progress, totalLength);
                                updateMessage(String.format("%.2f", status).concat("% complete"));
                            }
                            fis.close();
                            fos.close();
                        } else {
                            updateProgress(i, sourceFilesArrayList.size() - 1);
                            updateMessage(String.format("%.2f", (double) i / (sourceFilesArrayList.size() - 1) * 100).concat("% complete"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                updateProgress(1, 1);
                updateMessage("100% complete");
                System.out.println("End of the task..");
                return null;
            }
        };
        System.out.println("After the task..");
        task.progressProperty().addListener((observableValue, number, t1) -> {
            prg.setProgress(t1.doubleValue());
        });
        lblProgress.textProperty().bind(task.messageProperty());

        new Thread(task).start();

        System.out.println("End of the btnCopyOnAction() ");

        task.setOnSucceeded(e->{
            isCopying = false;
            showCompletedAlert("Copying");  //**
            clearAndReadyForNext();
        });
    }

    private boolean checkReplacingReqOfDirectory() {
        copyingToDirectory = new File(targetFolder, sourceFile.getName());
        if (copyingToDirectory.exists()) {
            Optional<ButtonType> optButton = new Alert(Alert.AlertType.CONFIRMATION,
                    "Directory/file name is already exists. Do you want to copy as a numbered file?",
                    ButtonType.NO, ButtonType.CLOSE, ButtonType.YES).showAndWait();
            if (optButton.isEmpty() || optButton.get() == ButtonType.CLOSE) {
                return false;
            } else if (optButton.get() == ButtonType.NO) {
                if (sourceFile.isDirectory()) {
                    Optional<ButtonType> optBtn = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to merge the existing one?",
                            ButtonType.YES, ButtonType.NO).showAndWait();
                    if (optBtn.isEmpty() ) return false;
                    if(optBtn.get()== ButtonType.NO){
                        Optional<ButtonType> optionalBtn = new Alert(Alert.AlertType.WARNING,
                                "Old files will be replaced", ButtonType.OK,ButtonType.NO).showAndWait();
                        if (optionalBtn.isEmpty() || optionalBtn.get()== ButtonType.NO) return false;
                        deleteForReplacing(copyingToDirectory);
                        new Alert(Alert.AlertType.INFORMATION,
                                "Deleting the old files, after the replacing will start!").showAndWait();
                    }
                    if (optBtn.get()==ButtonType.YES) {
                    }
                }

            } else if (optButton.get() == ButtonType.YES) {
                String newDirName = generateDirectoryName(copyingToDirectory.getName());
                if (newDirName == null) return false;
                copyingToDirectory = new File(targetFolder, newDirName);
            }
        }
        return true;
    }

    private void deleteForReplacing(File selectedFile) {
        if (selectedFile.isDirectory()) {
            findFilesForDelete(selectedFile);
            for (File file : deletingFilesArrayList) {
                boolean result = file.delete();
            }
        }
        selectedFile.delete();

    }

    private void findFilesForDelete(File targetFolder) {
        File[] files = targetFolder.listFiles();

        for (File file : files) {
            if ((file.isDirectory())) {
                findFilesForDelete(file);
                //continue;
            }
            deletingFilesArrayList.add(file);
        }
    }

    private String generateDirectoryName(String dirName) {
        String newDirName=null;
        int i = 1;
        while ((replacingDirNames.contains(dirName.concat(String.format(" (%d)", i))))) i++;

        newDirName = dirName.concat(String.format(" (%d)",i));

        if ( new File(targetFolder, newDirName).exists()) {
            Optional<ButtonType> optBtn = new Alert(Alert.AlertType.WARNING,
                    newDirName + " also exists, do you want to merge it?.",
                    ButtonType.YES, ButtonType.NO).showAndWait();
            if (optBtn.isEmpty() ) return null;
            if(optBtn.get()== ButtonType.NO){
                Optional<ButtonType> optionalBtn = new Alert(Alert.AlertType.WARNING,
                        "Old files will be replaced", ButtonType.OK,ButtonType.NO).showAndWait();
                if (optionalBtn.isEmpty() || optionalBtn.get()== ButtonType.NO) return null;
                deleteForReplacing(new File(targetFolder,newDirName));
                new Alert(Alert.AlertType.INFORMATION,
                        "Deleting the old files, after the replacing will start!").showAndWait();
            }
        }
        replacingDirNames.add(newDirName);
        return newDirName;
    }


     private void findFiles(File selectedFile) {
         File[] files = selectedFile.listFiles();
         int sub = sourceFile.getAbsolutePath().length();

         for (File file : files) {
             File temp =new File(copyingToDirectory.getAbsolutePath(), file.getAbsolutePath().substring(sub));
             if (file.isDirectory()) {
                 temp.mkdirs();
                 findFiles(file);
                 if (isCopying){
                     continue;
                 } else if (isMoving) {
                     temp.mkdirs();
                     //if any further moving logic available
                 }
             }
             sourceFilesArrayList.add(file);
             targetFilesArrayList.add(temp);
         }
    }

    public void btnMoveOnAction(ActionEvent event) {
        if (targetFolder == null || sourceFile ==null) return;
        if (!(checkReplacingReqOfDirectory())) return;
        if (sourceFile.isDirectory()) {
            copyingToDirectory.mkdir();
            isMoving=true;
            findFiles(sourceFile);
        } else {
            sourceFilesArrayList.add(sourceFile);
            targetFilesArrayList.add(copyingToDirectory);
        }

        for (int i = 0; i < sourceFilesArrayList.size(); i++) {
            if ((!sourceFilesArrayList.get(i).getAbsolutePath().equals(targetFilesArrayList.get(i).getAbsolutePath()))) {
                File oldFile = sourceFilesArrayList.get(i);
                File newFile = targetFilesArrayList.get(i);
                oldFile.renameTo(newFile);
                if (oldFile.isDirectory()) {
                    System.out.println(oldFile.delete());
                }
            }
        }
        if ((!(sourceFile.getAbsolutePath().equals(copyingToDirectory.getAbsolutePath())))) {
            sourceFile.delete();
        }
        lblProgress.setText("100% complete");
        prg.setProgress(1);
        showCompletedAlert("Moving");
        isMoving = false;
        clearAndReadyForNext();
    }

    public void btnDeleteOnAction(ActionEvent actionEvent) {
        if (sourceFile == null) return;
        Optional<ButtonType> optBtn = new Alert(Alert.AlertType.CONFIRMATION,
                "Files will be lost permanently. Are you sure to delete? ",
                ButtonType.YES, ButtonType.NO).showAndWait();
        if (optBtn.isEmpty() || optBtn.get()== ButtonType.NO) return;

        if (sourceFile.isDirectory()) {
            findFiles(sourceFile);
            for (File file : sourceFilesArrayList) {
                file.delete();
            }
        }
        sourceFile.delete();

        lblProgress.setText("100% complete");
        prg.setProgress(1);
        showCompletedAlert("Deleting");
        clearAndReadyForNext();

    }

    private boolean checkingInitialSteps(String task) {
        if (targetFolder == null || sourceFile ==null) return false;
        if (!(checkReplacingReqOfDirectory())) return false;
        if (sourceFile.isDirectory()) {
            copyingToDirectory.mkdir();
            switch (task) {
                case "Copying":
                    isCopying = true; break;
                default:
                    isMoving = true;
            }
            findFiles(sourceFile);
        } else {
            sourceFilesArrayList.add(sourceFile);
            targetFilesArrayList.add(copyingToDirectory);
        }
        return true;
    }

     private void clearAndReadyForNext() {
         lblProgress.textProperty().unbind();
         prg.progressProperty().unbind();
         lblProgress.setText("0% complete");
         prg.setProgress(0);
         sourceFilesArrayList.clear();
         targetFilesArrayList.clear();
         txtSource.clear();
         txtTarget.clear();
         sourceFile = null;
         targetFolder = null;
    }


     private void showCompletedAlert(String info) {
        new Alert(Alert.AlertType.INFORMATION, String.format("%s is finished!",info)).showAndWait();
    }

}
