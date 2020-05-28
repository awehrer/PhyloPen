/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Pair;
import phylopen.utility.ink.InkCanvas;
import phylopen.utility.ink.InkStroke;

/**
 *
 * @author Work
 */
public class TrainingRecognitionDialog extends Dialog<List<Pair<String, List<List<InkStroke>>>>>
{
    private List<Pair<String, List<List<InkStroke>>>> trainingData;
    private String [] symbols =
    {
        "Triangle", "Vertical slash", "X", "Horizontal line", "L", "Reverse L", "Left square bracket"
    };
    private String [] images = 
    {
        "file:resources/images/triangle.gif", "file:resources/images/vertical-slash.gif", "file:resources/images/X.gif", "file:resources/images/horizontal-line.gif", "file:resources/images/L.gif", "file:resources/images/reverse-L.gif", "file:resources/images/left-square-bracket.gif"
    };
    private String [] descriptions =
    {
        "Draw a triangle around the node as illustrated. This gesture will target nodes to expand and collapse parts of the phylogenetic tree. How you draw the gesture here should be consistent with how you intend to draw it while using the program if you want accurate recognition. Drawing it 6 times gives more examples for the program to work with of how you may end up drawing it while using the tool.",
        "Draw a vertical slash through the branch of the tree as illustrated. Drawing a vertical line through the branch indicates that you want to cut it off from its current location. The line may slant in either direction, but it's recommended to draw it as if it were the first line in making an X. This will be relevant if you want to permanently remove a clade.",
        "Draw an X through the branch as illustrated. After drawing the vertical slash to cut the branch, drawing another line to form an X will delete the clade entirely.",
        "Draw a horizontal line from the node as illustrated. Draw as if you were drawing a branch from the clade to a subclade in the distance. This will be used to indicate the reattachment point of a cut clade. (i.e., After you draw the vertical slash, instead of deleting the clade by completing it as an X, you draw the horizontal line from the parent where you would like to see it reattached.)",
        "Draw a bending L branch from the node as illustrated. This is an alternative to using the horizontal line in reattaching cut clades.",
        "Draw a bending branch in the opposite direction as illustrated. This an another alternative to using the horizontal line in reattaching cut clades.",
        "Draw a left square bracket symbol over the branch as illustrated. This gesture indicates when you wnat to reverse the ordering of the clade's direct decendents."
    };
    private String [] backgrounds =
    {
        "file:resources/images/training_bg.png", "file:resources/images/training_bg2.png", "file:resources/images/training_bg2.png", "file:resources/images/training_bg3.png", "file:resources/images/training_bg3.png", "file:resources/images/training_bg3.png", "file:resources/images/training_bg4.png", 
    };
    
    private int currentSymbolTrainingIndex;
    
    private InkCanvas canvas0;
    private InkCanvas canvas1;
    private InkCanvas canvas2;
    private InkCanvas canvas3;
    private InkCanvas canvas4;
    private InkCanvas canvas5;
    private Label symbolLabel;
    private ImageView demoImage;
    
    public TrainingRecognitionDialog()
    {
        this.setTitle("Ink Recognition Training");
        
        trainingData = new ArrayList<>();
        
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.NEXT);
        
        canvas0 = new InkCanvas();
        canvas0.setScratchOutErasingEnabled(true);
        ScrollPane scroll0 = new ScrollPane(canvas0);
        scroll0.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll0.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll0.setMinSize(200.0, 200.0);
        canvas0.setMinSize(200.0, 200.0);
        /*// new Image(url)
        Image image0 = new Image("file:resources/images/training_bg.png");
        // new BackgroundSize(width, height, widthAsPercentage, heightAsPercentage, contain, cover)
        BackgroundSize backgroundSize0 = new BackgroundSize(200, 200, true, true, true, false);
        // new BackgroundImage(image, repeatX, repeatY, position, size)
        BackgroundImage backgroundImage0 = new BackgroundImage(image0, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize0);
        // new Background(images...)
        Background background0 = new Background(backgroundImage0);
        canvas0.setBackground(background0);*/
        canvas1 = new InkCanvas();
        canvas1.setScratchOutErasingEnabled(true);
        ScrollPane scroll1 = new ScrollPane(canvas1);
        scroll1.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll1.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll1.setMinSize(200.0, 200.0);
        canvas1.setMinSize(200.0, 200.0);
        canvas2 = new InkCanvas();
        canvas2.setScratchOutErasingEnabled(true);
        ScrollPane scroll2 = new ScrollPane(canvas2);
        scroll2.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll2.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll2.setMinSize(200.0, 200.0);
        canvas2.setMinSize(200.0, 200.0);
        canvas3 = new InkCanvas();
        canvas3.setScratchOutErasingEnabled(true);
        ScrollPane scroll3 = new ScrollPane(canvas3);
        scroll3.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll3.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll3.setMinSize(200.0, 200.0);
        canvas3.setMinSize(200.0, 200.0);
        canvas4 = new InkCanvas();
        canvas4.setScratchOutErasingEnabled(true);
        ScrollPane scroll4 = new ScrollPane(canvas4);
        scroll4.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll4.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll4.setMinSize(200.0, 200.0);
        canvas4.setMinSize(200.0, 200.0);
        canvas5 = new InkCanvas();
        canvas5.setScratchOutErasingEnabled(true);
        ScrollPane scroll5 = new ScrollPane(canvas5);
        scroll5.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll5.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll5.setMinSize(200.0, 200.0);
        canvas5.setMinSize(200.0, 200.0);
        
        currentSymbolTrainingIndex = 0;
        
        symbolLabel = new Label();
        symbolLabel.setText(symbols[currentSymbolTrainingIndex]);
        symbolLabel.setMinWidth(100.0);
        
        Label descriptionLabel = new Label(descriptions[currentSymbolTrainingIndex]);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(425);
        descriptionLabel.setStyle("-fx-font-size:1.16666667em");
        ImageView demoImage = new ImageView(new Image(images[currentSymbolTrainingIndex]));
        //demoImage.setStyle("-fx-border-width: 1px; -fx-border-style: solid;");
        
        Background bg = new Background(new BackgroundImage(new Image(backgrounds[currentSymbolTrainingIndex]), BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(200, 200, true, true, true, false)));
        canvas0.setBackground(bg);
        canvas1.setBackground(bg);
        canvas2.setBackground(bg);
        canvas3.setBackground(bg);
        canvas4.setBackground(bg);
        canvas5.setBackground(bg);
        
        HBox descriptionRow = new HBox(3);
        descriptionRow.getChildren().addAll(descriptionLabel, demoImage);
        
        Button clearButton0 = new Button("Clear");
        clearButton0.setMinWidth(100);
        clearButton0.setMinHeight(40);
        clearButton0.setStyle("-fx-font-size:1.333333em");
        clearButton0.setOnAction((event) -> { canvas0.getInkStrokes().clear(); });
        Button clearButton1 = new Button("Clear");
        clearButton1.setMinWidth(100);
        clearButton1.setMinHeight(40);
        clearButton1.setStyle("-fx-font-size:1.333333em");
        clearButton1.setOnAction((event) -> { canvas1.getInkStrokes().clear(); });
        Button clearButton2 = new Button("Clear");
        clearButton2.setMinWidth(100);
        clearButton2.setMinHeight(40);
        clearButton2.setStyle("-fx-font-size:1.333333em");
        clearButton2.setOnAction((event) -> { canvas2.getInkStrokes().clear(); });
        Button clearButton3 = new Button("Clear");
        clearButton3.setMinWidth(100);
        clearButton3.setMinHeight(40);
        clearButton3.setStyle("-fx-font-size:1.333333em");
        clearButton3.setOnAction((event) -> { canvas3.getInkStrokes().clear(); });
        Button clearButton4 = new Button("Clear");
        clearButton4.setMinWidth(100);
        clearButton4.setMinHeight(40);
        clearButton4.setStyle("-fx-font-size:1.333333em");
        clearButton4.setOnAction((event) -> { canvas4.getInkStrokes().clear(); });
        Button clearButton5 = new Button("Clear");
        clearButton5.setMinWidth(100);
        clearButton5.setMinHeight(40);
        clearButton5.setStyle("-fx-font-size:16");
        clearButton5.setOnAction((event) -> { canvas5.getInkStrokes().clear(); });
        
        VBox canvasPane0 = new VBox(3);
        canvasPane0.getChildren().addAll(scroll0, clearButton0);
        canvasPane0.setAlignment(Pos.CENTER);
        VBox canvasPane1 = new VBox(3);
        canvasPane1.getChildren().addAll(scroll1, clearButton1);
        canvasPane1.setAlignment(Pos.CENTER);
        VBox canvasPane2 = new VBox(3);
        canvasPane2.getChildren().addAll(scroll2, clearButton2);
        canvasPane2.setAlignment(Pos.CENTER);
        VBox canvasPane3 = new VBox(3);
        canvasPane3.getChildren().addAll(scroll3, clearButton3);
        canvasPane3.setAlignment(Pos.CENTER);
        VBox canvasPane4 = new VBox(3);
        canvasPane4.getChildren().addAll(scroll4, clearButton4);
        canvasPane4.setAlignment(Pos.CENTER);
        VBox canvasPane5 = new VBox(3);
        canvasPane5.getChildren().addAll(scroll5, clearButton5);
        canvasPane5.setAlignment(Pos.CENTER);
        
        VBox mainContentPane = new VBox(3);
        HBox canvasRow1 = new HBox(3);
        canvasRow1.getChildren().addAll(canvasPane0, canvasPane1, canvasPane2);
        canvasRow1.setPadding(new Insets(6, 0, 6, 0));
        HBox canvasRow2 = new HBox(3);
        canvasRow2.getChildren().addAll(canvasPane3, canvasPane4, canvasPane5);
        FlowPane subcontentPane = new FlowPane();
        
        subcontentPane.getChildren().addAll(descriptionRow, symbolLabel, canvasRow1, canvasRow2);
        mainContentPane.getChildren().addAll(subcontentPane);
        
        getDialogPane().setContent(mainContentPane);
        
        this.setResultConverter(new Callback<ButtonType, List<Pair<String, List<List<InkStroke>>>>>()
        {
            @Override
            public List<Pair<String, List<List<InkStroke>>>> call(ButtonType param)
            {
                if (param.equals(ButtonType.CANCEL))
                    return null;
                else
                    return trainingData;
            }
        });
        
        final Button nextButton = (Button) getDialogPane().lookupButton(ButtonType.NEXT);
        
        nextButton.addEventFilter(ActionEvent.ACTION, event ->
        {
            if (canvas0.getInkStrokes().size() == 0
                    || canvas1.getInkStrokes().size() == 0
                    || canvas2.getInkStrokes().size() == 0
                    || canvas3.getInkStrokes().size() == 0
                    || canvas4.getInkStrokes().size() == 0
                    || canvas5.getInkStrokes().size() == 0)
            {
                event.consume();
                return;
            }
            
            List<List<InkStroke>> inkSamples = new ArrayList<>(6);
            inkSamples.add(new ArrayList<>(canvas0.getInkStrokes()));
            inkSamples.add(new ArrayList<>(canvas1.getInkStrokes()));
            inkSamples.add(new ArrayList<>(canvas2.getInkStrokes()));
            inkSamples.add(new ArrayList<>(canvas3.getInkStrokes()));
            inkSamples.add(new ArrayList<>(canvas4.getInkStrokes()));
            inkSamples.add(new ArrayList<>(canvas5.getInkStrokes()));
            
            trainingData.add(new Pair<>(symbols[currentSymbolTrainingIndex], inkSamples));
            
            currentSymbolTrainingIndex++;
            canvas0.getInkStrokes().clear();
            canvas1.getInkStrokes().clear();
            canvas2.getInkStrokes().clear();
            canvas3.getInkStrokes().clear();
            canvas4.getInkStrokes().clear();
            canvas5.getInkStrokes().clear();
            
            if (currentSymbolTrainingIndex < symbols.length)
            {
                symbolLabel.setText(symbols[currentSymbolTrainingIndex]);
                demoImage.setImage(new Image(images[currentSymbolTrainingIndex]));
                Background background = new Background(new BackgroundImage(new Image(backgrounds[currentSymbolTrainingIndex]), BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(200, 200, true, true, true, false)));
                canvas0.setBackground(background);
                canvas1.setBackground(background);
                canvas2.setBackground(background);
                canvas3.setBackground(background);
                canvas4.setBackground(background);
                canvas5.setBackground(background);
                descriptionLabel.setText(descriptions[currentSymbolTrainingIndex]);
                event.consume();
            }
        });
    }
}
