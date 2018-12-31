
package musicplayer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class FXMLController implements Initializable {
    
private boolean fullScreen = false;
private File videoFile = null;
private TranslateTransition slideIn;
private TranslateTransition slideOut;

@FXML private Slider seekBar;
@FXML private Slider volumeSlider;
@FXML private StackPane mediaViewPane;
@FXML private Button audioMode;
@FXML private Button videoMode;
@FXML private MediaView mediaView;
@FXML private Pane topBox;
@FXML private Pane window;
@FXML private Pane controlPane;
@FXML private Label duration;

//to open the dialog to choose the song
FileChooser fileChooser;

//stores the path of the song to play
Media media;

//controlles the media player
MediaPlayer mediaPlayer;

//check the status of media player
MediaPlayer.Status status = MediaPlayer.Status.UNKNOWN;
    
//track the song list
@FXML public void chooseFile() throws MalformedURLException
{
    fileChooser = new FileChooser();
    fileChooser.setTitle("Select Songs");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("MP4", "*.mp4")
    );

    String home = System.getProperty("user.home");
    fileChooser.setInitialDirectory(new File(home+"\\Videos"));
    try
    {
        videoFile = fileChooser.showOpenDialog(MusicPlayer.window);
    }
    catch(MediaException | IllegalArgumentException e)
    {
    }
    if(videoFile==null)
        return;
    if(mediaPlayer!=null)
        mediaPlayer.stop();

    media  =new Media(videoFile.toURI().toURL().toString());
    mediaPlayer = new MediaPlayer(media);
    status = mediaPlayer.getStatus();

    //media ready
    mediaPlayer.setOnReady(() -> {
        double time = mediaPlayer.getTotalDuration().toSeconds();
        seekBar.setMin(0.0);
        seekBar.setValue(0.0);
        seekBar.setMax(time);
        volumeSlider.setValue(mediaPlayer.getVolume()*100);
        mediaView.setFitHeight(MusicPlayer.window.getHeight());
        mediaView.setFitWidth(MusicPlayer.window.getWidth());
        mediaView.setMediaPlayer(mediaPlayer);    
    }); 

    //key pressed on screen
    MusicPlayer.scene2.setOnKeyPressed(value->{
        
        if(value.getCode().equals(KeyCode.ESCAPE)&&fullScreen)
        {
            MusicPlayer.window.setFullScreen(false);
            fullScreen = false;
        }
        else if(value.getCode().equals(KeyCode.F))
        {
            if(fullScreen)
            {
                MusicPlayer.window.setFullScreen(false);
                fullScreen = false;
            }
            else
            {   

                MusicPlayer.window.setFullScreen(true);   
                fullScreen = true;
            }
            changeScreenMode();
        }
        if(value.getCode().equals(KeyCode.P)||value.getCode().equals((KeyCode.SPACE)))
        {
            try
            {
                playButtonClicked();
            } catch (MalformedURLException ex) 
            {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(value.getCode().equals(KeyCode.RIGHT))
        {
            double currentTime = mediaPlayer.getCurrentTime().toSeconds();
            mediaPlayer.seek(Duration.seconds(Math.min(currentTime+10.0, mediaPlayer.getCycleDuration().toSeconds())));
        }
        if(value.getCode().equals(KeyCode.LEFT))
        {
            double currentTime = mediaPlayer.getCurrentTime().toSeconds();
            mediaPlayer.seek(Duration.seconds(Math.max(currentTime-10.0, 0)));
        }
    });
}
    
public void playButtonClicked() throws MalformedURLException
{
    if(media==null)
    {
        return;
    }

    if(status.equals(MediaPlayer.Status.STOPPED)||status.equals(MediaPlayer.Status.READY)||status.equals(MediaPlayer.Status.UNKNOWN))
    {
        mediaPlayer.setAutoPlay(true);
        status = MediaPlayer.Status.PLAYING;     
    }
    else if(status.equals(MediaPlayer.Status.PLAYING))
    {
        mediaPlayer.pause();
        status = MediaPlayer.Status.PAUSED;

    }
    else if(status.equals(MediaPlayer.Status.PAUSED))
    {
        status = MediaPlayer.Status.PLAYING;
        mediaPlayer.play();
    }


    mediaPlayer.setOnEndOfMedia(() -> {
        try 
        {
            nextButtonClicked();
        } catch (MalformedURLException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    });

    mediaPlayer.currentTimeProperty().addListener((v,oldValue,newValue)->{
            seekBar.setValue(newValue.toSeconds());
        });

    seekBar.setOnMouseClicked(event->{         
               mediaPlayer.seek(Duration.seconds(seekBar.getValue()));
       });   
}

public void stopButtonClicked()
{
    mediaPlayer.stop();
    status = MediaPlayer.Status.STOPPED;
    mediaPlayer = new MediaPlayer(new Media(videoFile.toURI().toString()));
    mediaView.setMediaPlayer(mediaPlayer);
}

public void nextButtonClicked() throws MalformedURLException
{
    stopButtonClicked();
    media  =new Media(videoFile.toURI().toURL().toString());
    mediaPlayer = new MediaPlayer(media);
    status = MediaPlayer.Status.READY;
    playButtonClicked();
}

public void previousButtonClicked() throws MalformedURLException
{   
    stopButtonClicked();
    media  =new Media(videoFile.toURI().toURL().toString());
    mediaPlayer = new MediaPlayer(media);
    status = MediaPlayer.Status.READY;
    playButtonClicked();
}
    
@Override
public void initialize(URL url, ResourceBundle rb) 
{
    volumeSlider.valueProperty().addListener((listener)->
    {
        mediaPlayer.setVolume(volumeSlider.getValue()/100);
    });

    duration.setText("--:--:--");

    seekBar.valueProperty().addListener((v,oldValue, newValue)->
    {
        double time = Math.ceil((double)newValue);
        long minutes = (long) (time/60);
        long hours = minutes/60;
        minutes = minutes%60;
        long seconds = (long) (time%60);
        String format = String.format("%d:%d:%d ", hours,minutes,seconds);
        duration.setText(format);
    });

    audioMode.setOnAction(e->{
    MusicPlayer.window.setScene(MusicPlayer.scene1);
    if(mediaPlayer!=null)
        stopButtonClicked();
    });
    videoMode.setOnAction(e->MusicPlayer.window.setScene(MusicPlayer.scene2));

    //full screen mode
    mediaView.setOnMouseClicked(event->{
        if(event.getClickCount()==2)
        {
            if(fullScreen)
            {
                MusicPlayer.window.setFullScreen(false);
                fullScreen = false;
            }
            else
            {   
                MusicPlayer.window.setFullScreen(true);   
                fullScreen = true;
            }
            changeScreenMode();
        }
        try 
        {
            playButtonClicked();
        } catch (MalformedURLException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    });

    slideIn = new TranslateTransition();
    slideIn.setNode(controlPane);
    slideIn.setDuration(Duration.seconds(1));
    slideIn.setFromY(controlPane.getLayoutY());
    slideIn.setToY(window.getHeight()-controlPane.getHeight());

    slideOut = new TranslateTransition();
    slideOut.setNode(controlPane);
    slideOut.setDuration(Duration.seconds(1));
    slideOut.setFromY(window.getHeight()-controlPane.getHeight());
    slideOut.setToY(controlPane.getLayoutY());

    window.setOnMouseEntered(e->
    {
         slideIn.play();
    });

    window.setOnMouseExited(e->
    {
         slideOut.play();
    });
    
    seekBar.setOnKeyPressed(event->{
        if(event.getCode().equals(KeyCode.RIGHT))
        {
            double currentTime = mediaPlayer.getCurrentTime().toSeconds();
            mediaPlayer.seek(Duration.seconds(Math.min(currentTime+10.0, mediaPlayer.getCycleDuration().toSeconds())));
        }
        else if(event.getCode().equals(KeyCode.LEFT))
        {
            double currentTime = mediaPlayer.getCurrentTime().toSeconds();
            mediaPlayer.seek(Duration.seconds(Math.max(currentTime-10.0, 0)));
        }
    });
}

private void changeScreenMode()
{
    mediaViewPane.setMinHeight(MusicPlayer.window.getHeight());
    mediaViewPane.setMinWidth(MusicPlayer.window.getWidth());
    mediaView.setFitHeight(MusicPlayer.window.getHeight());
    mediaView.setFitWidth(MusicPlayer.window.getWidth());
    seekBar.setMinWidth(MusicPlayer.window.getWidth());
    controlPane.setMinWidth(MusicPlayer.window.getWidth());
    topBox.setMinWidth(MusicPlayer.window.getWidth());
    controlPane.setLayoutY(window.getHeight()-controlPane.getHeight());
    volumeSlider.setLayoutX(window.getWidth()-volumeSlider.getWidth());
    duration.setLayoutX(window.getWidth()-duration.getWidth()-30);
}
}
