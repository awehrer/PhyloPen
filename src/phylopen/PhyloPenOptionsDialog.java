/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import phylopen.utility.TitledBorderPane;

/**
 *
 * @author User
 */
public class PhyloPenOptionsDialog extends Dialog<Void>
{
    private final TextField girderBaseURLField;
    private final Spinner confidenceThresholdSpinner;
    private CheckBox showTipAttributesOnlyCheckbox;
    private final PhyloPenOptions optionsEntry;
    private CheckBox rectangleDisabledCheckbox;
    private CheckBox triangleExpandCollapseDisabledCheckbox;
    private CheckBox cutAndReattachDisabledCheckbox;
    private CheckBox cutDisabledCheckbox;
    private CheckBox cladeRotationDisabledCheckbox;
    private CheckBox selectionDisabledCheckbox;
    
    public PhyloPenOptionsDialog(PhyloPenOptions optionsEntry)
    {
        this.setTitle("Options");
        
        this.optionsEntry = optionsEntry;
        
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        
        Label girderBaseURLFieldLabel = new Label("Girder base URL:");
        girderBaseURLField = new TextField(optionsEntry.getGirderBaseURL());
        girderBaseURLField.setPrefColumnCount(30);
        
        HBox girderBaseURLPane = new HBox(4);
        girderBaseURLPane.getChildren().addAll(girderBaseURLFieldLabel, girderBaseURLField);
        
        Label confidenceThresholdLabel = new Label("Confidence threshold:");
        confidenceThresholdSpinner = new Spinner(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, optionsEntry.getConfidenceLevelThreshold(), 0.05));
        
        HBox confidenceThresholdPane = new HBox(2);
        confidenceThresholdPane.setStyle("-fx-spacing: 3;");
        
        confidenceThresholdPane.getChildren().addAll(confidenceThresholdLabel, confidenceThresholdSpinner);
        
        showTipAttributesOnlyCheckbox = new CheckBox("Show tip attributes only");
        showTipAttributesOnlyCheckbox.setSelected(optionsEntry.isShowingTipAttributesOnly());
        
        TitledBorderPane gestureDisablePane = new TitledBorderPane();
        gestureDisablePane.setTitle("Gestures Disabled");
        gestureDisablePane.setStyle("-fx-border-width: 0px; -fx-border-style: solid; -fx-border-insets: 0.25em 0.666666667em 0.25em 0.66666667em;");
        gestureDisablePane.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(3), null)));
        
        rectangleDisabledCheckbox = new CheckBox("Rectangle");
        rectangleDisabledCheckbox.setSelected(optionsEntry.isRectangleDisabled());
        triangleExpandCollapseDisabledCheckbox = new CheckBox("Triangle Expand/Collapse Clade");
        triangleExpandCollapseDisabledCheckbox.setSelected(optionsEntry.isTriangleExpandCollapseDisabled());
        cutAndReattachDisabledCheckbox = new CheckBox("Cut and Reattach");
        cutAndReattachDisabledCheckbox.setSelected(optionsEntry.isCutAndReattachDisabled());
        cutDisabledCheckbox = new CheckBox("Cut");
        cutDisabledCheckbox.setSelected(optionsEntry.isCutDisabled());
        cladeRotationDisabledCheckbox = new CheckBox("Clade Rotation");
        cladeRotationDisabledCheckbox.setSelected(optionsEntry.isCladeRotationDisabled());
        selectionDisabledCheckbox = new CheckBox("Selection");
        selectionDisabledCheckbox.setSelected(optionsEntry.isSelectionDisabled());
        
        VBox disableCheckboxPane = new VBox(4);
        disableCheckboxPane.getChildren().addAll(rectangleDisabledCheckbox, triangleExpandCollapseDisabledCheckbox, cutAndReattachDisabledCheckbox,
                cutDisabledCheckbox, cladeRotationDisabledCheckbox, selectionDisabledCheckbox);
        //disableCheckboxPane.setStyle("-fx-background-color: white");
        
        gestureDisablePane.setContent(disableCheckboxPane);
        
        VBox mainContentPane = new VBox(2);
        mainContentPane.getChildren().addAll(girderBaseURLPane, confidenceThresholdPane, showTipAttributesOnlyCheckbox, gestureDisablePane);
        getDialogPane().setContent(mainContentPane);
        
        this.setResultConverter(new Callback<ButtonType, Void>()
        {
            @Override
            public Void call(ButtonType param)
            {
                if (param.equals(ButtonType.OK))
                {
                    optionsEntry.setGirderBaseURL(girderBaseURLField.getText());
                    optionsEntry.setConfidenceLevelThreshold((Double)confidenceThresholdSpinner.getValue());
                    optionsEntry.setShowingTipAttributesOnly(showTipAttributesOnlyCheckbox.isSelected());
                    optionsEntry.setRectangleDisabled(rectangleDisabledCheckbox.isSelected());
                    optionsEntry.setTriangleExpandCollapseDisabled(triangleExpandCollapseDisabledCheckbox.isSelected());
                    optionsEntry.setCutAndReattachDisabled(cutAndReattachDisabledCheckbox.isSelected());
                    optionsEntry.setCutDisabled(cutDisabledCheckbox.isSelected());
                    optionsEntry.setCladeRotationDisabled(cladeRotationDisabledCheckbox.isSelected());
                    optionsEntry.setSelectionDisabled(selectionDisabledCheckbox.isSelected());
                }
                
                return null;
            }
        });
    }
}
