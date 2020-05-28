/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import java.io.File;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import phylopen.utility.RemScaler;

/**
 *
 * @author awehrer
 */
public class CommandHelpDialog extends Dialog<Void>
{
    private final double CELL_HEIGHT;
    
    public CommandHelpDialog()
    {
        this.setTitle("Command Help");
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        CELL_HEIGHT = new RemScaler().scale(50.0);
        
        TabPane tabPane = new TabPane();
        tabPane.setMinWidth(775);
        tabPane.setMaxHeight(700);
        tabPane.setPrefHeight(700);
        
        Tab overviewTab = new Tab();
        overviewTab.setText("Features Overview");
        overviewTab.setClosable(false);
        
        Image overviewImage = new Image("file:resources/images/gesturesAndFeaturesOverview.png");
        ImageView overviewImageView = new ImageView(overviewImage);
        /*Label overviewLabel = new Label();
        overviewLabel.setGraphic(overviewImageView);
        overviewLabel.setAlignment(Pos.CENTER);*/
        StackPane imageHolder = new StackPane(overviewImageView);
        
        ScrollPane overviewScrollPane = new ScrollPane(imageHolder);
        overviewScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        overviewScrollPane.setFitToWidth(true);
        
        overviewTab.setContent(overviewScrollPane);
        tabPane.getTabs().add(overviewTab);
        
        Tab demonstrationTab = new Tab();
        demonstrationTab.setText("Demonstrations");
        demonstrationTab.setClosable(false);
        
        BorderPane demonstrationPane = new BorderPane();
        final ListView<String> commandList = new ListView<>();
        
        commandList.setCellFactory((ListView<String> list) -> new FormattedCell());
        
        commandList.getItems().add("Cut and reattach a clade");
        commandList.getItems().add("Cut and delete a clade");
        commandList.getItems().add("Rotate clades (swap/reverse child order)");
        
        final MediaView videoView = new MediaView();
        
        commandList.getItems().addListener(new ListChangeListener<String>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends String> c)
            {
                MediaPlayer player;
                
                switch (commandList.getSelectionModel().getSelectedItem())
                {
                    case "Cut and reattach a clade":
                        player = new MediaPlayer(new Media(new File("resources/videos/sample.mp4").toURI().toString()));
                        break;
                    default:
                        player = null;
                        break;
                }
                
                videoView.setMediaPlayer(player);
                if (player != null)
                {
                    player.setMute(true);
                    player.setCycleCount(MediaPlayer.INDEFINITE);
                    player.play();
                }
            }
        });
        
        ScrollPane commandListScrollPane = new ScrollPane(commandList);
        commandListScrollPane.setFitToWidth(true);
        commandListScrollPane.setPrefHeight(200);
        
        demonstrationPane.setTop(commandListScrollPane);
        
        demonstrationPane.setBottom(new StackPane(videoView));
        
        demonstrationTab.setContent(demonstrationPane);
        tabPane.getTabs().add(demonstrationTab);
        
        getDialogPane().setContent(tabPane);
    }
    
    private class FormattedCell extends ListCell<String>
    {
        public FormattedCell()
        {
            
        }

        @Override
        protected void updateItem(String item, boolean empty)
        {
            super.updateItem(item, empty);

            if (empty || item == null)
            {
                setText(null);
                setGraphic(null);
            }
            else
            {
                setText(item);
            }

            setStyle("-fx-cell-size: " + CELL_HEIGHT + "; -fx-alignment: CENTER-LEFT; -fx-font-size: 1.2em;");
        }
    }
}
