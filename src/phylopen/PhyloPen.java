/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package phylopen;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author awehrer
 */
public class PhyloPen extends Application
{
    @Override
    public void start(Stage stage) throws Exception
    {
        Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
        Scene scene = new Scene(root);
        String css = getClass().getResource("MainWindow.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.getIcons().add(new Image("file:resources/images/arborLogo_32x32.png"));
        stage.setTitle("PhyloPen");
        stage.centerOnScreen();
        stage.sizeToScene();
        stage.show();
    }
    
    @Override
    public void stop()
    {
        System.out.println("Stage is closing");
        AppResources.getController().prepareToClose();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }
    
}
