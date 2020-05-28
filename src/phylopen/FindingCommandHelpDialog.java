/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 *
 * @author awehrer
 */
public class FindingCommandHelpDialog extends Dialog<Void>
{
    public FindingCommandHelpDialog()
    {
        this.setTitle("Finding Command Help");
        getDialogPane().getButtonTypes().add(ButtonType.OK);

        Image visualAidImage = new Image("file:resources/images/findingCommandHelp.png");
        ImageView visualAidImageView = new ImageView(visualAidImage);
        StackPane imageHolder = new StackPane(visualAidImageView);
        imageHolder.setStyle("-fx-border-width: 2px; -fx-border-style: solid; -fx-border-color: black");

        Label explanationLabel = new Label("Remember: If you need help in understanding how to use a command feature while using the program, you can return to the command help dialog under the Help menu as illustrated above.");
        explanationLabel.setGraphic(imageHolder);
        explanationLabel.setAlignment(Pos.CENTER);
        explanationLabel.setContentDisplay(ContentDisplay.TOP);
        explanationLabel.setWrapText(true);
        
        explanationLabel.setMinWidth(300);
        explanationLabel.setPrefWidth(400);
        explanationLabel.setMaxWidth(400);
        
        getDialogPane().setContent(explanationLabel);
    }
}
