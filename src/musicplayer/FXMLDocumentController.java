
package musicplayer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class FXMLDocumentController implements Initializable {
    
private int index = 0;
private XYChart.Series<String, Number> series ;
private XYChart.Data<String, Number> seriesData[]; 

@FXML private ImageView play;
@FXML private Button stopBtn;
@FXML private Button nextBtn;
@FXML private Button previousBtn;
@FXML private Label statusLabel;
@FXML private Slider seekBar;
@FXML private ListView<String> list;
@FXML private Slider volumeSlider;
@FXML private Pane mediaViewPane;
@FXML private Button audioMode;
@FXML private Button videoMode;
@FXML private BarChart <String, Number> spectrumBar;
@FXML private Label duration;

//to open the dialog to choose the song
FileChooser fileChooser;

//stores the path of the song to play
Media media, media1;

//controlles the media player
MediaPlayer mediaPlayer;

// hold the song file
List<File> songFiles;

//check the status of media player
MediaPlayer.Status status = MediaPlayer.Status.UNKNOWN;

//track the song list
@FXML public void chooseFile()
{
    try {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Select Songs");
        fileChooser.getExtensionFilters().addAll(                    
            new FileChooser.ExtensionFilter("MP3", "*.mp3")
        );
        List<File> getFiles = null;
        String home = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(home+"\\Music"));
        try
        {
            getFiles = fileChooser.showOpenMultipleDialog(MusicPlayer.window);
        }
        catch(MediaException | IllegalArgumentException e)
        {
        }
        if(getFiles==null)
            return;
        if(getFiles!=null)
        {
            if(songFiles==null)
            {
                songFiles = new ArrayList(getFiles);
                songFiles.forEach((file) -> {
                    list.getItems().add(file.getName());
                });  
            }
            else
            {
                for(File file : getFiles)
                {
                    if(!songFiles.contains(file))
                    {
                        songFiles.add(file);
                        list.getItems().add(file.getName());
                    }
                }
            }        
        }
        if(!songFiles.isEmpty()&&mediaPlayer==null)
        {
            media  =new Media(songFiles.get(index).toURI().toURL().toString());
            mediaPlayer = new MediaPlayer(media);
            status = mediaPlayer.getStatus();
            statusLabel.setText("Status:"+status.toString());
            nextBtn.setDisable(false);
            previousBtn.setDisable(false);
        } 
    }   
    catch (MalformedURLException ex) {
        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
    }

    MusicPlayer.scene1.setOnKeyPressed(value->{
        if(value.getCode().equals(KeyCode.P))
        {
            try {
                playButtonClicked();
            }
            catch (MalformedURLException ex) {
              Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else if(value.getCode().equals(KeyCode.RIGHT))
        {
            double currentTime = mediaPlayer.getCurrentTime().toSeconds();
            mediaPlayer.seek(Duration.seconds(Math.min(currentTime+10.0, mediaPlayer.getCycleDuration().toSeconds())));
        }
        else if(value.getCode().equals(KeyCode.LEFT))
        {
            double currentTime = mediaPlayer.getCurrentTime().toSeconds();
            mediaPlayer.seek(Duration.seconds(Math.max(currentTime-10.0, 0)));
        }
    });
}
    
@FXML public void playButtonClicked() throws MalformedURLException
{
    if(media==null)
    {
        return;
    }
    else if(status.equals(MediaPlayer.Status.STOPPED)||status.equals(MediaPlayer.Status.READY)||status.equals(MediaPlayer.Status.UNKNOWN))
    {
        mediaPlayer.setAutoPlay(true);
        stopBtn.setDisable(false);
        list.getSelectionModel().select(index);
        status = MediaPlayer.Status.PLAYING;
        statusLabel.setText("Status :"+status.toString());
        play.setImage(new Image(getClass().getResourceAsStream("images/pause.jpg")));
    }
    else if(status.equals(MediaPlayer.Status.PLAYING))
    {
        mediaPlayer.pause();
        status = MediaPlayer.Status.PAUSED;
        statusLabel.setText("Status :"+status.toString());
        play.setImage(new Image(getClass().getResourceAsStream("images/play.jpg")));
    }
    else if(status.equals(MediaPlayer.Status.PAUSED))
    {
        status = MediaPlayer.Status.PLAYING;
        mediaPlayer.play();
        statusLabel.setText("Status :"+status.toString());
        play.setImage(new Image(getClass().getResourceAsStream("images/pause.jpg")));   
    }
    
    mediaPlayer.setOnReady(() -> {
        double time = mediaPlayer.getTotalDuration().toSeconds();
        seekBar.setMin(0.0);
        seekBar.setValue(0.0);
        seekBar.setMax(time);
        volumeSlider.setValue(mediaPlayer.getVolume()*100); 
    });   
    
    mediaPlayer.setOnEndOfMedia(() -> {
        try {
            nextButtonClicked();
        } 
        catch (MalformedURLException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    });
    
    mediaPlayer.currentTimeProperty().addListener((v,oldValue,newValue)->{
        seekBar.setValue(newValue.toSeconds());
    });
    
    mediaPlayer.setAudioSpectrumListener((double d, double d1, float[] magnitudes,float[] phases)->{
        for(int i = 0;i<magnitudes.length;i++)
        {
            seriesData[i].setYValue(magnitudes[i]+60);
        }
    });
}

@FXML public void stopButtonClicked()
{
    mediaPlayer.stop();
    stopBtn.setDisable(true);
    status = MediaPlayer.Status.STOPPED;
    statusLabel.setText("Status :"+status.toString());
    play.setImage(new Image(getClass().getResourceAsStream("images/play.jpg")));
    mediaPlayer = new MediaPlayer(media);
     for (int i=0; i<seriesData.length; i++) {
            seriesData[i].setYValue(0);  
        }
}

@FXML public void nextButtonClicked() throws MalformedURLException
{
    index = (index+1)%songFiles.size();
    stopButtonClicked();
    media  =new Media(songFiles.get(index).toURI().toURL().toString());
    mediaPlayer = new MediaPlayer(media);
    status = MediaPlayer.Status.READY;
    statusLabel.setText("Status :"+status.toString());
    playButtonClicked();
}

@FXML public void previousButtonClicked() throws MalformedURLException
{
    index--;
    if(index==-1)
        index=songFiles.size()-1;
    stopButtonClicked();
    media  =new Media(songFiles.get(index).toURI().toURL().toString());
    mediaPlayer = new MediaPlayer(media);
    status = MediaPlayer.Status.READY;
    statusLabel.setText("Status :"+status.toString());
    playButtonClicked();
}
    
@Override
public void initialize(URL url, ResourceBundle rb) {
        
    statusLabel.setText("Status :"+status.toString());
    list.getSelectionModel().selectedItemProperty().addListener((v,oldValue,newValue)->{
        try {
            index = list.getSelectionModel().getSelectedIndex();
            stopButtonClicked();
            media = new Media(songFiles.get(index).toURI().toURL().toString());
            mediaPlayer =new MediaPlayer(media);
            playButtonClicked();

        } catch (MalformedURLException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    });

    list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    volumeSlider.valueProperty().addListener((listener)->{
        mediaPlayer.setVolume(volumeSlider.getValue()/100);
    });

    duration.setText("--:--");

    seekBar.valueProperty().addListener((v,oldValue, newValue)->{
       double time = Math.ceil((double)newValue);
       long minutes = (long) (time/60);
       long seconds = (long) (time%60);
       String format = String.format("%d:%d ", minutes,seconds);
       duration.setText(format);
    });


    seekBar.setOnMouseClicked(event->{
        if(mediaPlayer!=null)
        {
            mediaPlayer.seek(Duration.seconds(seekBar.getValue()));
        }
    });

    audioMode.setOnAction(e->MusicPlayer.window.setScene(MusicPlayer.scene1));
    videoMode.setOnAction(e->{
        MusicPlayer.window.setScene(MusicPlayer.scene2);
        if(mediaPlayer!=null)
        {
            stopButtonClicked();
            for(int i =0;i<seriesData.length;i++)
            {
               seriesData[i].setYValue(0);
            }
        }
    });
    series = new XYChart.Series<>();       
    seriesData = new XYChart.Data[128];

    for (int i=0; i<seriesData.length; i++) {
        seriesData[i] = new XYChart.Data(Integer.toString(i+1),0);
        series.getData().add(seriesData[i]);
    }
   spectrumBar.setMinSize(mediaViewPane.getWidth(), mediaViewPane.getHeight());
   spectrumBar.getData().add(series);     
   
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
}