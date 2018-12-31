
package musicplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MusicPlayer extends Application {
    
    public static Stage window;
    static Scene scene1,scene2;
    Button open;
    @Override
    public void start(Stage stage) throws Exception {
        window = stage;
        window.setResizable(false);
        window.getIcons().add(new Image(getClass().getResourceAsStream("images/icon.png")));
        window.setTitle("Music Player");
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        Parent root2 = FXMLLoader.load(getClass().getResource("FXML.fxml"));
        scene1 = new Scene(root);
        scene2 = new Scene(root2);
        window.setScene(scene1);
        window.show();
        
        
        
    }

  
    public static void main(String[] args) {
        launch(args);
    }
    
    
}
