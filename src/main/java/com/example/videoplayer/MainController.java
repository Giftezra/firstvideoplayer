package com.example.videoplayer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private ObservableList<File> videoList;
    private MediaPlayer mediaPlayer;
    @FXML
    private MediaView mediaView;
    @FXML
    ProgressBar progressBar;

    // Create a Map to store rotation angles for each video
    private Map<MediaView, Double> videoRotationMap = new HashMap<>();

    @FXML
    private Button playPauseButton;

    private DoubleProperty mediaViewWidth = new SimpleDoubleProperty();
    private DoubleProperty mediaViewHeight = new SimpleDoubleProperty();

    @FXML
    ListView<File> listView;

    @FXML
    private VBox mediaContainer, contentContainer;
    private int currentlyPlayingIndex = -1;
    private Duration currentTime;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        videoList = FXCollections.observableArrayList();

        mediaView.fitWidthProperty().bind(mediaContainer.widthProperty()); // fit the width to its parent container
        mediaView.fitHeightProperty().bind(mediaContainer.heightProperty()); // Fit the height to its parent container
        mediaView.setVisible(true); // Set the media view to visible
        // Set the listener for the width and height
        mediaViewWidth.bind(mediaView.fitWidthProperty());
        mediaViewHeight.bind(mediaView.fitHeightProperty());
        /*These sets of code will get the media angle and  rotate it if the need be*/
        double getCurrentAngle = getCurrentLocation(mediaView);

        if (getCurrentAngle <= -90.0){
            mediaView.setRotate(mediaView.getRotate() + 90.0);
        } else if (getCurrentAngle <= 90.0) {
            mediaView.setRotate(mediaView.getRotate() -90);
        }

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                VBox musicBox = new VBox();
                Label artiste = new Label();
                Label video = new Label();
                artiste.setFont(Font.font("Arial", FontWeight.BLACK, 15));
                artiste.setTextFill(Color.BLACK);
                video.setFont(Font.font("Arial", FontWeight.LIGHT, 12));
                video.setTextFill(Color.DARKBLUE);
                musicBox.setAlignment(Pos.CENTER_LEFT);
                musicBox.setSpacing(10);

                if (empty || item == null) {
                    setText(null);
                } else {
                    String music = item.getName();
                    String[] parts = music.split("_");

                    if (parts.length >= 2){
                        artiste.setText(parts[0].trim());
                        video.setText(parts[1].trim());
                    }
                    musicBox.getChildren().addAll(artiste, video);
                    setGraphic(musicBox); // Set the graphics to their parent node
                }
            }
        });


        playPauseButton.setOnAction(this::playButtonClicked);

        // Set up the timeline to update the progress bar
        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.seconds(1), // Update every 1 second
                        event -> updateProgressBar()
                )
        );
        timeline.setCycleCount(Timeline.INDEFINITE); // Repeat indefinitely
        timeline.play();

        progressBar.prefWidthProperty().bind(contentContainer.widthProperty());
    }

    public ObservableList<File> getVideoList() {
        return videoList;
    }

    public void setVideoList(ObservableList<File> videoList) {
        this.videoList = videoList;
    }

    /**This method will play the music in the file.
     * first check if the media player has been instantiated and if not null ir=t stops whatever was playing.
     * then instantiate a media class parsing the file as an arg as a string.
     * set the media view to the media player and start playing
     * @ return void
     * @ param musicFile
     * */
    private void playVideo(File videoFile) {

        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        Media media = new Media(videoFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.play();
        videoRotationMap.put(mediaView, 0.0);
    }

    /*This method is called when the play button is clicked. it checks if the list is empty before checking the index and updating it accordingly
     * if no song is playing int sets the index to 0 and increments the index.
     * The file is used to get the files in the list parsing them to the play music method*/
    @FXML
    private void playButtonClicked(ActionEvent event) {
        if (!videoList.isEmpty()) {
            if (currentlyPlayingIndex == -1) {
                currentlyPlayingIndex = 0;
            } else if (currentlyPlayingIndex >= videoList.size()) {
                currentlyPlayingIndex = 0;
            }

            File videoFile = videoList.get(currentlyPlayingIndex);

            // If there's an existing mediaPlayer, check its status
            if (mediaPlayer != null) {
                MediaPlayer.Status status = mediaPlayer.getStatus();
                if (status == MediaPlayer.Status.PLAYING) {
                    // If currently playing, pause and store the current time
                    mediaPlayer.pause();
                    currentTime = mediaPlayer.getCurrentTime();
                } else if (status == MediaPlayer.Status.PAUSED) {
                    // If paused, resume playing
                    mediaPlayer.play();
                    return; // No need to play a new video, return from the method
                }
            }

            playVideo(videoFile);
        } else {
            // Handle the case where the list is empty
            System.out.println("No videos in the list.");
        }
    }



    /*This method handles the next button when clicked.
     * checks if the list is empty, before checking what music index is playing and increments it.
     * if the index is greater or equals to the list , the index will start again from 0
     * finally plays the song at the current index */
    @FXML
    private void nextVideo(){
        if (!videoList.isEmpty()){
            if (currentlyPlayingIndex == 0){
                currentlyPlayingIndex ++;
            }else {
                currentlyPlayingIndex ++;
                if (currentlyPlayingIndex >= videoList.size()){
                    currentlyPlayingIndex = 0;
                }
            }
            File music = videoList.get(currentlyPlayingIndex); // Use a file type to get the music from the list
            playVideo(music);
        }else {
            System.out.println("No music in the list");
        }
    }


    private void updateProgressBar() {
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            Duration totalDuration = mediaPlayer.getTotalDuration();
            Duration currentDuration = mediaPlayer.getCurrentTime();
            double progress = currentDuration.toSeconds() / totalDuration.toSeconds();
            progressBar.setProgress(progress);
        }
    }

    /*This method handles the previous button when clicked.
     * checks if the list is empty, before checking what music index is playing
     * it decrements the index if index is greater than zero which means that there is a song playing.
     * finally plays the song at the current index */
    @FXML
    private void previousSong(){
        if (!videoList.isEmpty()){
            if (currentlyPlayingIndex > 0){
                currentlyPlayingIndex --;
            }else {
                currentlyPlayingIndex --;
                if (currentlyPlayingIndex <= videoList.size()) currentlyPlayingIndex = 0;
            }
            File video = videoList.get(currentlyPlayingIndex); // Use a file type to get the music from the list
            playVideo(video);
        }else {
            System.out.println("No music in the list");
        }
    }

    private double getCurrentLocation (Node node){
        double rotate = 0.0;
        if (videoRotationMap.containsKey(mediaView)){
            return videoRotationMap.get(mediaView);
        }
        return rotate;
    }
}