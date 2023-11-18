package com.example.videoplayer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class WelcomeController implements Initializable {
    /**This is the class to handle the method to get data from local storage or to get data from YouTube using the YouTube api.
     * the class implements the initialize interface and calls several methods */

    @FXML
    private Button localStorageButton, youtubeButton;

    private ObservableList<File> videoList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        videoList = FXCollections.observableArrayList();

    }

    /* This method will ask the user to choose music from any folder of their choice using a directory chooser to select
   their music folder. and the musics in the folder will be
   added to the obsMusicList
   @return void
 */
    @FXML
    private void addFromLocal() throws IOException {
        DirectoryChooser dirChooser = new DirectoryChooser(); // Make an instance of a file chooser
        dirChooser.setTitle("Select Music");
        File file = dirChooser.showDialog(localStorageButton.getScene().getWindow()); // Pass the result to the file class and parse the window as the new stage
        if (file == null || !file.isDirectory()) return; // Return if the file is empty or a not a directory
        /*Use Files.walk to recursively list all files in the selected folder and its sub folders.
         * Use a try catch block to catch the error.
         * Use a list to hold the list of music files that ends with .mp3*/
        try (Stream<Path> paths = Files.walk(file.toPath())){ // Use a stream to walk through each file as a path
            List<File> videoFiles = paths // pass the path to the list of files
                    .filter(Files::isRegularFile) // Check if the file is a regular file or a folder and if regular file
                    .filter(path -> path.toString().toLowerCase().endsWith(".mp4")) // Check if it ends with mp3 extension
                    .map(Path::toFile) // Convert the path back to a file
                    .toList(); // convert to list
            // Add music files to the list only if it is not empty and call the toMain method afterward
            if (!videoFiles.isEmpty()){
                videoList.addAll(videoFiles);
                toMain();
            }else {
                System.out.println("File is empty");
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }


    /*This method will take the user to the main page where they will see the list of music and play it
     * */
    private void toMain(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));// Load the fxml
            Parent root = loader.load();
            MainController mainController = loader.getController(); // Get the main controller class
            mainController.setVideoList(videoList);// Set the music list
            mainController.listView.setItems(videoList);
            Scene scene = new Scene(root);
            Stage stage = (Stage) localStorageButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }catch (IOException ioe){
            System.err.println("Error " + ioe.getMessage());
        }
    }
}
