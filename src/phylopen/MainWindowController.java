/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package phylopen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;
import javafx.util.Pair;
import javax.imageio.ImageIO;
import phylopen.PhyloPenCanvas.PhyloPenInkGestureRecognizer;
import static phylopen.PhyloPenIO.readFile;
import phylopen.utility.RemScaler;
import phylopen.utility.SingleDirectoryFileChooserDialog;
import phylopen.utility.TooltipUtility;
import phylopen.utility.ink.InkStroke;
import phylopen.utility.ink.StylusEvent;
import phylopen.utility.ink.StylusPoint;
import phylopen.utility.ink.recognition.InkGesture;
import phylopen.utility.ink.recognition.InkGestureListener;

/**
 *
 * @author awehrer
 */
public class MainWindowController implements Initializable
{
    private final double CANVAS_BORDER_WIDTH;
    private final double widthHeightChangeDelta;
    private ArborUserInfo userInfo;
    private PhyloPenOptions optionsEntry;
    private File imageSaveDirectory;
    private Animation recordingStatusAnimation;
    private Color defaultCanvasBorderColor;
    private Color currentCanvasBorderColor;
    private String user;
    
    public MainWindowController()
    {
        sidebarAtRight = true;
        widthHeightChangeDelta = 100.0;
        defaultCanvasBorderColor = Color.WHITESMOKE;
        CANVAS_BORDER_WIDTH = 4.0;
        currentCanvasBorderColor = defaultCanvasBorderColor;
    }
    
    public PhyloPenOptions getOptions()
    {
        return optionsEntry;
    }
    
    public void prepareToClose()
    {
        if (getPhyloPenCanvas() != null)
            getPhyloPenCanvas().prepareToClose();
    }
    
    @FXML
    private void handleExitAction(ActionEvent event)
    {
        System.exit(0);
    }
    
    @FXML
    private void handleOptionsAction(ActionEvent event)
    {
        new PhyloPenOptionsDialog(optionsEntry).showAndWait();
    }
    
    @FXML
    private void handleSnapshotAction(ActionEvent event)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Snapshot As");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("PNG", "*.png"),
                new ExtensionFilter("All Files", "*.*"));
        
        // Start in user directory or go to default if cannot access
        File userDirectory;
        
        if (imageSaveDirectory == null)
            userDirectory = new File(System.getProperty("user.home"));
        else
            userDirectory = imageSaveDirectory;
        
        if (!userDirectory.canRead())
            userDirectory = new File("c:/");
        
        fileChooser.setInitialDirectory(userDirectory);
        fileChooser.setInitialFileName(Long.toString(System.currentTimeMillis()));
        
        File saveFile = fileChooser.showSaveDialog(phyloPenCanvas.getScene().getWindow());
        
        if (saveFile != null)
        {
            imageSaveDirectory = saveFile.getParentFile();
            
            SnapshotParameters param = new SnapshotParameters();
            //Bounds bounds = phyloPenCanvas.getModelRenderPane().getBoundsInParent();
            //param.setViewport(new Rectangle2D(phyloPenCanvas.getLayoutXOffset(), phyloPenCanvas.getLayoutYOffset(), bounds.getWidth() - phyloPenCanvas.getLayoutXOffset(), bounds.getHeight() - phyloPenCanvas.getLayoutYOffset()));
            WritableImage image = phyloPenCanvas.getModelRenderPane().snapshot(param, null);
            
            try
            {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", saveFile);
            }
            catch (IOException e)
            {
                new Alert(Alert.AlertType.ERROR, "Writing to the specified file failed", ButtonType.OK).showAndWait();
            }
        }
    }
    
    @FXML
    private void handleCommandHelpAction(ActionEvent event)
    {
        new CommandHelpDialog().showAndWait();
    }
    
    @FXML
    private void handleOpenAction(ActionEvent event)
    {
        Optional<String> result = new ArborOpenDialog(userInfo, optionsEntry.getGirderBaseURL()).showAndWait();
        
        if (result.isPresent() && result.get() != null)
        {
            final String path = result.get();
            
            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        loadTreeModel(path, false, user);
                    }
                    catch (IOException e) {}
                }
            });
        }
    }
    
    @FXML
    private void handleUndoAction(ActionEvent event)
    {
        this.getPhyloPenCanvas().undo();
    }
    
    @FXML
    private void handleSaveAsLocalFileAction(ActionEvent event)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("NESTED-JSON", "*.nested-json"),
                new ExtensionFilter("All Files", "*.*"));
        
        // Start in user directory or go to default if cannot access
        File userDirectory;
        
        if (fileNameLabel.getText().contains("@Arbor")) // Arbor file
        {
            userDirectory = new File(System.getProperty("user.home"));
            fileChooser.setInitialFileName("");
        }
        else // local file
        {
            File file = new File(fileNameLabel.getText().substring(6, fileNameLabel.getText().length()));
            userDirectory = file.getParentFile();
            fileChooser.setInitialFileName(file.getName());
        }
        
        if (!userDirectory.canRead())
            userDirectory = new File("c:/");
        
        fileChooser.setInitialDirectory(userDirectory);
        
        File saveFile = fileChooser.showSaveDialog(phyloPenCanvas.getScene().getWindow());
        
        if (saveFile != null)
        {
            try
            {
                PhyloPenIO.save(phyloPenCanvas, saveFile);
            }
            catch (FileNotFoundException e)
            {
                new Alert(Alert.AlertType.ERROR, "Could not find the specified file", ButtonType.OK).showAndWait();
            }
            catch (IOException e)
            {
                new Alert(Alert.AlertType.ERROR, "Writing to the specified file failed", ButtonType.OK).showAndWait();
            }
        }
    }
    
    @FXML
    private void handleOpenLessonsAction(ActionEvent event)
    {
        loadFromDefaultDirectory();
    }
    
    @FXML
    private void handleOpenLocalFileAction(ActionEvent event)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("NESTED-JSON", "*.nested-json"),
                new ExtensionFilter("All Files", "*.*"));
        
        // Start in user directory or go to default if cannot access
        File userDirectory;
        
        if (fileNameLabel.getText().equals("File: ") || fileNameLabel.getText().contains("@Arbor")) // empty or Arbor file
        {
            userDirectory = new File(System.getProperty("user.home"));
            fileChooser.setInitialFileName("");
        }
        else
        {
            File file = new File(fileNameLabel.getText().substring(6, fileNameLabel.getText().length()));
            userDirectory = file.getParentFile();
            fileChooser.setInitialFileName(file.getName());
        }
        
        if (!userDirectory.canRead())
            userDirectory = new File("c:/");
        
        fileChooser.setInitialDirectory(userDirectory);
        
        final File openFile = fileChooser.showOpenDialog(phyloPenCanvas.getScene().getWindow());
        
        if (openFile != null)
        {
            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        loadTreeModel(openFile.getAbsolutePath(), true, user);
                    }
                    catch (IOException e)
                    {
                        new Alert(Alert.AlertType.ERROR, "Reading the specified file failed", ButtonType.OK).showAndWait();
                        return;
                    }
                }
            });
        }
    }
    
    @FXML private MenuItem loginMenuItem;
    @FXML private MenuItem userMenuItem;
    @FXML private Button loginButton;
    @FXML private SplitMenuButton userButton;
    
    @FXML
    private void handleLoginAction(ActionEvent event)
    {
        if (userInfo == null)
        {
            Optional<ArborUserInfo> result = new ArborLoginDialog(optionsEntry.getGirderBaseURL()).showAndWait();
            userInfo = (result.isPresent() ? result.get() : null);
            
            if (userInfo != null)
            {
                userMenuItem.setText("Logged in as " + userInfo.getUsername());
                userMenuItem.setVisible(true);
                loginMenuItem.setVisible(false);
                
                
                userButton.setText("Logged in as " + userInfo.getUsername() + " ("
                        + userInfo.getFirstName() + " " + userInfo.getLastName() + ")");
                userButton.setVisible(true);
                userButton.setMinWidth(Region.USE_PREF_SIZE);
                userButton.setMaxWidth(Region.USE_PREF_SIZE);
                loginButton.setVisible(false);
                loginButton.setMinWidth(0.0);
                loginButton.setMaxWidth(0.0);
            }
        }
    }
    
    @FXML
    private void handleLogOutAction(ActionEvent event)
    {
        final String girderBaseURL = optionsEntry.getGirderBaseURL();
        
        JsonElement jsonData = new ArborJsonRetriever().getResponseJson(girderBaseURL + "user/authentication?token=" + userInfo.getAuthenticationToken(), "DELETE");

        if (jsonData != null)
        {
            new Alert(Alert.AlertType.CONFIRMATION, jsonData.getAsJsonObject().get("message").getAsString(), ButtonType.OK).showAndWait();
            userInfo = null;
            
            userMenuItem.setText("Logged in as Guest");
            userMenuItem.setVisible(false);
            loginMenuItem.setVisible(true);
            
            userButton.setText("");
            userButton.setVisible(false);
            userButton.setMinWidth(0.0);
            userButton.setMaxWidth(0.0);
            loginButton.setVisible(true);
            loginButton.setMinWidth(Region.USE_PREF_SIZE);
            loginButton.setMaxWidth(Region.USE_PREF_SIZE);
        }
        else
        {
            new Alert(Alert.AlertType.ERROR, "Log out failed.", ButtonType.OK).showAndWait();
        }
    }
    
    @FXML
    private void handleUpdateUserInfoAction(ActionEvent event)
    {
        Optional<Boolean> result = new ArborUpdateUserInfoDialog(userInfo, optionsEntry.getGirderBaseURL()).showAndWait();
        
        // if user information updated, update relevant labels
        if (result.isPresent() && result.get())
        {
            userButton.setText("User: " + userInfo.getUsername() + " ("
                        + userInfo.getFirstName() + " " + userInfo.getLastName() + ")");
        }
    }
    
    @FXML
    private void handleChgPassAction(ActionEvent event)
    {
        new ArborPasswordChangeDialog(userInfo.getAuthenticationToken(), optionsEntry.getGirderBaseURL()).showAndWait();
    }
    
    @FXML private TextField nodeSizeField;
    
    @FXML
    private void handleNodeSizeChange(ObservableValue<? extends Number> value, Number oldValue, final Number newValue)
    {
        nodeSizeField.setText(String.valueOf((((int)(newValue.doubleValue() * 10.0)) / 10.0)));
    }
    
    @FXML
    private void handleFinalizeNodeSizeChange(MouseEvent event)
    {
        if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED))
        {
            double currentValue = ((Slider)event.getSource()).getValue();

            if (phyloPenCanvas.getSelectedNodes().size() > 0)
                phyloPenCanvas.setNodeRadiusOfSelectedNodes(currentValue);
            else
                phyloPenCanvas.setNodeRadius(currentValue);
        }
    }
    
    @FXML private SplitPane canvasSplitPane;
    private boolean sidebarAtRight;
    @FXML private TabPane sideMenuTabbedPane;
    
    @FXML
    private void handleSidebarSwipe(SwipeEvent event)
    {
        if ((sidebarAtRight && event.getEventType().equals(SwipeEvent.SWIPE_LEFT) || (!sidebarAtRight && event.getEventType().equals(SwipeEvent.SWIPE_RIGHT))) && !sideMenuTabbedPane.getSelectionModel().getSelectedItem().getText().equals("Attributes"))
        {
            double[] splitPositions = canvasSplitPane.getDividerPositions();
            splitPositions[0] = 1.0 - splitPositions[0];
            ObservableList<Node> canvasSplitPaneItems = canvasSplitPane.getItems();
            Node temp1 = canvasSplitPaneItems.remove(0);
            Node temp2 = canvasSplitPaneItems.remove(0);
            canvasSplitPaneItems.add(0, temp2);
            canvasSplitPaneItems.add(1, temp1);
            canvasSplitPane.setDividerPosition(0, splitPositions[0]);
            sidebarAtRight = !sidebarAtRight;
            event.consume();
        }
    }
    
    @FXML
    private void handleStylusMoved(StylusEvent event)
    {
        //System.out.println("Stylus MOVED: " + event);
    }
    
    @FXML
    private void handleStylusDown(StylusEvent event)
    {
        statusRect.setFill(Color.WHITE);
    }
    
    @FXML
    private void handleTreeWidthIncreaseAction(ActionEvent event)
    {
        phyloPenCanvas.changeTreeWidth(widthHeightChangeDelta / phyloPenCanvas.getModelRenderScale());
    }
    
    @FXML
    private void handleTreeWidthDecreaseAction(ActionEvent event)
    {
        phyloPenCanvas.changeTreeWidth(-widthHeightChangeDelta / phyloPenCanvas.getModelRenderScale());
    }
    
    @FXML
    private void handleTreeHeightIncreaseAction(ActionEvent event)
    {
        phyloPenCanvas.changeTreeHeight(widthHeightChangeDelta / phyloPenCanvas.getModelRenderScale());
    }
    
    @FXML
    private void handleTreeHeightDecreaseAction(ActionEvent event)
    {
        phyloPenCanvas.changeTreeHeight(-widthHeightChangeDelta / phyloPenCanvas.getModelRenderScale());
    }
    
    @FXML
    private void handleTreeWidthResetAction(ActionEvent event)
    {
        phyloPenCanvas.resetTreeWidth();
    }
    
    @FXML
    private void handleTreeHeightResetAction(ActionEvent event)
    {
        phyloPenCanvas.resetTreeHeight();
    }
    
    @FXML
    private void handleTreeDimensionsResetAction(ActionEvent event)
    {
        phyloPenCanvas.resetTreeDimensions();
    }
    
    private double roundDouble(double value, int numDecimalPlaces)
    {
        double factor = Math.pow(10, numDecimalPlaces);
        return Math.round(value * factor) / factor;
    }
    
    @FXML
    private void handleProcessInkAction(ActionEvent event)
    {
        phyloPenCanvas.processInkForGestureRecognition();
    }
    
    @FXML
    private void handleColorBranchAction(final ActionEvent event)
    {
        if (phyloPenCanvas.getSelectedNodes().size() > 0)
            phyloPenCanvas.colorSelectedBranches(((ColorPicker)event.getSource()).getValue());
        else
            phyloPenCanvas.colorBranches(((ColorPicker)event.getSource()).getValue());
    }
    
    @FXML private TextField branchWidthField;
    
    @FXML
    private void handleBranchWidthChange(ObservableValue<? extends Number> value, Number oldValue, final Number newValue)
    {
        branchWidthField.setText(String.valueOf((((int)(newValue.doubleValue() * 10.0)) / 10.0)));
        
        if (phyloPenCanvas.getSelectedNodes().size() > 0)
            phyloPenCanvas.changeWidthOfSelectedBranches(newValue.doubleValue());
        else
            phyloPenCanvas.changeWidthOfBranches(newValue.doubleValue());
        
    }
    
    @FXML
    private void handleColorNodeOutlineAction(final ActionEvent event)
    {
        Platform.runLater(new Runnable()
        {
            public void run()
            {
                if (phyloPenCanvas.getSelectedNodes().size() > 0)
                    phyloPenCanvas.colorOutlineOfSelectedNodes(((ColorPicker)event.getSource()).getValue());
                else
                    phyloPenCanvas.colorOutlineOfNodes(((ColorPicker)event.getSource()).getValue());
            }
        });
        
    }
    
    @FXML
    private void handleColorNodeFillAction(final ActionEvent event)
    {
        if (phyloPenCanvas.getSelectedNodes().size() > 0)
            phyloPenCanvas.colorFillOfSelectedNodes(((ColorPicker)event.getSource()).getValue());
        else
            phyloPenCanvas.colorFillOfNodes(((ColorPicker)event.getSource()).getValue());
    }
    
    @FXML
    private void handleClearInkAction(ActionEvent event)
    {
        phyloPenCanvas.getInkStrokes().clear();
    }
    
    @FXML
    private void handleClearSelectionAction(ActionEvent event)
    {
        phyloPenCanvas.deselectNodes(phyloPenCanvas.getSelectedNodes());
    }
    
    @FXML private Button annotateButton;
    @FXML private Label modeLabel;
    
    @FXML
    private void handleAnnotateAction(ActionEvent event)
    {
        phyloPenCanvas.annotateSelectedNodes();
    }
    
    @FXML
    public void handleCustomizationResetAction(ActionEvent event)
    {
        JsonObject command = new JsonObject();
        command.addProperty("typeId", PhyloPenIO.APPEARANCE_RESET);
        command.addProperty("inputMethod", "button");
        command.addProperty("timestamp", System.currentTimeMillis());
        
        phyloPenCanvas.resetAppearance();
    }
    
    @FXML
    public void handleFitImages(ActionEvent event)
    {
        double maxHeight = 0.0, height;
        
        for (CladeImageViewer image : phyloPenCanvas.getCladeImages())
        {
            if (image.getHeight() > maxHeight)
                maxHeight = image.getHeight();
        }
        
        height = phyloPenCanvas.getTreeModel().getNumLeaves() * (maxHeight + 10.0);
        
        if (height > phyloPenCanvas.getTreeHeight())
            phyloPenCanvas.setTreeDimensions(phyloPenCanvas.getWidth(), height);
    }
    
    @FXML
    public void handleTrainingRecognition(ActionEvent event)
    {
        TrainingRecognitionDialog dialog = new TrainingRecognitionDialog();
        
        Optional<List<Pair<String, List<List<InkStroke>>>>> result = dialog.showAndWait();
        
        if (result.isPresent() && result.get() != null)
        {
            ((PhyloPenInkGestureRecognizer)phyloPenCanvas.getGestureRecognizer()).clearTrainingSymbols();
            
            List<Pair<String, List<List<InkStroke>>>> trainingData = result.get();
            
            for (Pair<String, List<List<InkStroke>>> dataEntry : trainingData)
            {
                for (List<InkStroke> inkSample : dataEntry.getValue())
                {
                    ((PhyloPenInkGestureRecognizer)phyloPenCanvas.getGestureRecognizer()).addToTrainingSymbols(dataEntry.getKey(), inkSample);
                }
            }
            
            try
            {
                saveTrainingDataToFile(trainingData, new File("resources/temp/localInkTrainingData-" + user + ".dat"));
            }
            catch (IOException e)
            {
                System.out.println("Error: Could not save training data");
            }
        }
    }
    
    @FXML
    private void handleEditAnnotationAction(ActionEvent event)
    {
        if (!annotationList.getSelectionModel().isEmpty())
            getPhyloPenCanvas().editAnnotation(annotationList.getSelectionModel().getSelectedItem());
    }
    
    @FXML
    private void handleDeleteAnnotationAction(ActionEvent event)
    {
        JsonObject command = new JsonObject();
        command.addProperty("typeId", PhyloPenIO.ANNOTATION_REMOVED);
        command.addProperty("inputMethod", "button");
        command.addProperty("timestamp", System.currentTimeMillis());
        command.addProperty("annotationId", annotationList.getSelectionModel().getSelectedItem().getId());
        
        if (!annotationList.getSelectionModel().isEmpty())
        {
            getPhyloPenCanvas().removeAnnotation(annotationList.getSelectionModel().getSelectedItem());
        }
    }
    
    @FXML
    private void handleCutCladeAction(ActionEvent event)
    {
        phyloPenCanvas.cutSelectedClade();
    }
    
    @FXML
    private void handlePasteCladeAction(ActionEvent event)
    {
        JsonObject command = new JsonObject();
        command.addProperty("typeId", PhyloPenIO.CLADE_CUT_REATTACH);
        command.addProperty("inputMethod", "button");
        command.addProperty("timestamp", System.currentTimeMillis());
        command.addProperty("nodeId", phyloPenCanvas.getCutClade().getId());
        
        if (phyloPenCanvas.reattachCutCladeToSelectedClade())
            pasteCladeButton.setDisable(true);
        else
            new Alert(Alert.AlertType.ERROR, "Error: Cannot reattach cut clade to itself", ButtonType.OK).showAndWait();
    }
    
    @FXML
    private void handleDeleteCladeAction(ActionEvent event)
    {
        JsonObject command = new JsonObject();
        command.addProperty("typeId", PhyloPenIO.CLADE_CUT);
        command.addProperty("inputMethod", "button");
        command.addProperty("timestamp", System.currentTimeMillis());
        command.addProperty("nodeId", phyloPenCanvas.getSelectedNodes().get(0).getId());
        
        phyloPenCanvas.removeSelectedClade();
    }
    
    @FXML
    private void handleRotateCladeAction(ActionEvent event)
    {
        JsonObject command = new JsonObject();
        command.addProperty("typeId", PhyloPenIO.CLADE_ROTATE);
        command.addProperty("inputMethod", "button");
        command.addProperty("timestamp", System.currentTimeMillis());
        
        phyloPenCanvas.reverseSelectedClades();
    }
    
    @FXML
    private void handleDeleteAttributeColumnAction(ActionEvent event)
    {
        getPhyloPenCanvas().getTreeModel().deleteAttribute(attributeTable.getSelectionModel().getSelectedCells().get(0).getTableColumn().getText());
    }
    
    private void saveTrainingDataToFile(List<Pair<String, List<List<InkStroke>>>> trainingData, File outputFile) throws FileNotFoundException, IOException
    {
        outputFile.getParentFile().mkdir();
        outputFile.createNewFile();
        
        File debugOutputFile = new File("resources/temp/localInkTrainingData_debug.dat");
        
        debugOutputFile.createNewFile();
        
        FileOutputStream fileOut = new FileOutputStream(outputFile);
        DataOutputStream out = new DataOutputStream(fileOut);
        
        PrintWriter outDebug = new PrintWriter(debugOutputFile);
        
        String gesture;
        List<List<InkStroke>> samplesForGesture;
        
        // output number of gesture for which there is data
        out.writeInt(trainingData.size());
        outDebug.print(trainingData.size());
        outDebug.print(' ');
        
        for (Pair<String, List<List<InkStroke>>> dataPair : trainingData)
        {
            gesture = dataPair.getKey();
            samplesForGesture = dataPair.getValue();
            
            out.writeInt(gesture.length());
            out.writeBytes(gesture);
            
            outDebug.print(gesture.length());
            outDebug.print(' ');
            outDebug.print(gesture);
            outDebug.print(' ');
            
            for (List<InkStroke> sample : samplesForGesture)
            {
                // output number of strokes
                out.writeInt(sample.size());
                outDebug.print(sample.size());
                outDebug.print(' ');
                
                // output number of points and coordinates for each point for each stroke
                for (InkStroke sampleStroke : sample)
                {
                    out.writeInt(sampleStroke.getStylusPoints().size());
                    outDebug.print(sampleStroke.getStylusPoints().size());
                    outDebug.println();
                    
                    for (StylusPoint point : sampleStroke.getStylusPoints())
                    {
                        out.writeDouble(point.getX());
                        out.writeDouble(point.getY());
                        
                        outDebug.print(point.getX());
                        outDebug.print(' ');
                        outDebug.print(point.getY());
                        outDebug.println();
                    }
                }
            }
        }
        
        //out.flush();
        out.close();
        outDebug.close();
    }
    
    // if exists
    private boolean loadLocalTrainingData(String user) throws FileNotFoundException, IOException
    {
        File inputFile = new File("resources/temp/localInkTrainingData-" + user + ".dat");
        
        if (inputFile.exists())
        {
            PhyloPenInkGestureRecognizer recognizer = (PhyloPenInkGestureRecognizer) phyloPenCanvas.getGestureRecognizer();
            String gesture;
            List<InkStroke> sample;
            List<StylusPoint> points;
            
            FileInputStream fileIn = new FileInputStream(inputFile);
            DataInputStream in = new DataInputStream(fileIn);
            
            int numGestures = in.readInt(), numStrokes, numPoints;
            int gestureNum = 0, sampleNum, strokeNum, pointNum;
            double x, y;
            
            while (gestureNum < numGestures)
            {
                int length = in.readInt();
                byte[] array = new byte[length];
                in.read(array);
                gesture = new String(array);
                //System.out.println(gesture + " " + gestureNum);
                sampleNum = 0;
                
                while (sampleNum < 6)
                {
                    numStrokes = in.readInt();
                    sample = new ArrayList<>(numStrokes);
                    strokeNum = 0;

                    while (strokeNum < numStrokes)
                    {
                        numPoints = in.readInt();
                        points = new ArrayList<>(numPoints);
                        pointNum = 0;

                        while (pointNum < numPoints)
                        {
                            x = in.readDouble();
                            y = in.readDouble();

                            points.add(new StylusPoint(x, y));
                            pointNum++;
                        }

                        sample.add(new InkStroke(points));
                        strokeNum++;
                    }

                    recognizer.addToTrainingSymbols(gesture, sample);
                    sampleNum++;
                }
                
                gestureNum++;
            }
            
            System.out.println("INFO: Local ink training data loaded");
            return true;
        }
        
        return false;
    }
    
    private void updateCanvasBorderColor()
    {
        canvasViewport.setBorder(new Border(new BorderStroke(currentCanvasBorderColor, BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY, new BorderWidths(CANVAS_BORDER_WIDTH))));
    }
    
    @FXML private PhyloPenCanvas phyloPenCanvas;
    @FXML private PhyloTreeInkCanvasModelWIM minitree;
    @FXML private ScrollPane canvasViewport;
    @FXML private Label translationLabel;
    @FXML private Label zoomLabel;
    @FXML private TreeResizeInkCanvas treeResizeCanvas;
    @FXML private Rectangle statusRect;
    @FXML private Label fileNameLabel;
    @FXML private ColorPicker branchColorChooser;
    @FXML private ColorPicker nodeOutlineColorChooser;
    @FXML private ColorPicker nodeFillColorChooser;
    @FXML private ColorByList colorByList;
    @FXML private ColorPicker colorByAttributeMinColorPicker;
    @FXML private ColorPicker colorByAttributeMaxColorPicker;
    @FXML private ColorPicker colorByAttributeUndefinedColorPicker;
    @FXML private TreeAttributeTable attributeTable;
    @FXML private CladeImageViewer imageViewer;
    @FXML private CheckBox scaledBranchesCheckbox;
    @FXML private CheckBox showSpeciesNamesCheckbox;
    @FXML private CheckBox showIntermediateBranchLengthsCheckbox;
    @FXML private CheckBox showFinalBranchLengthsOnBranchesCheckbox;
    @FXML private CheckBox showFinalBranchLengthsWithLeafNamesCheckbox;
    @FXML private CheckBox showAncestorLabelCheckbox;
    @FXML private CheckBox showAnnotationPlaceholdersCheckbox;
    @FXML private CheckBox showLeafImagesCheckbox;
    @FXML private CheckBox showHyperlinksCheckbox;
    @FXML private AnnotationList annotationList;
    @FXML private VBox inkOptionsPane;
    @FXML private VBox mouseOptionsPane;
    @FXML private Button rotateCladeButton;
    @FXML private Button cutCladeButton;
    @FXML private Button pasteCladeButton;
    @FXML private ScrollBar vInnerPaneScrollBar;
    @FXML private ScrollBar hInnerPaneScrollBar;
    @FXML private MenuItem undoMenuItem;
    @FXML private Button deleteCladeButton;
    @FXML private BorderPane canvasBorderPane;
    @FXML private Label undoSwipeBar;
    @FXML private Button deleteAttributeColumnButton;
    
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        AppResources.setController(this);
        TooltipUtility.setTooltipTimers(100, 20000, 200);
        
        boolean mouseMode = false;
        
        try
        {
            String fs = System.getProperty("file.separator");

            String jsonString = readFile(new File("resources" + fs + "settings" + fs + "loadConfig.json"));
            JsonElement parsedJson = new JsonParser().parse(jsonString);
            JsonObject settingJson = parsedJson.getAsJsonObject();
            if (settingJson.has("mouseMode"))
                mouseMode = settingJson.get("mouseMode").getAsBoolean();
        }
        catch (IOException e)
        {
            System.out.println("Warning: Default load configuration file not accessed.");
        }
        
        optionsEntry = new PhyloPenOptions(phyloPenCanvas, attributeTable, "http://arbor.arborworkflows.com/girder/api/v1/", 0.85, new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN), mouseMode, false, true);
        
        optionsEntry.setRectangleDisabled(true);
        
        loginButton.setMinWidth(Region.USE_PREF_SIZE);
        loginButton.setMaxWidth(Region.USE_PREF_SIZE);
        
        if (optionsEntry.isMouseMode())
        {
            inkOptionsPane.setStyle("visibility:collapse;");
            inkOptionsPane.setPrefSize(0.0, 0.0);
            inkOptionsPane.setMaxSize(0.0, 0.0);
            inkOptionsPane.setMinSize(0.0, 0.0);
            //canvasViewport.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            //canvasViewport.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            phyloPenCanvas.setTouchDisabled(true);
            phyloPenCanvas.setInkDisabled(true);
            canvasBorderPane.getChildren().remove(undoSwipeBar);
        }
        else
        {
            mouseOptionsPane.setStyle("visibility:collapse;");
            mouseOptionsPane.setPrefSize(0.0, 0.0);
            mouseOptionsPane.setMaxSize(0.0, 0.0);
            mouseOptionsPane.setMinSize(0.0, 0.0);
            phyloPenCanvas.setMouseDisabled(true);
        }
        
        updateCanvasBorderColor();
        
        String defaultFontFamily = Font.getDefault().getFamily();
        double defaultFontSize = Font.getDefault().getSize();
        fileNameLabel.setFont(Font.font(defaultFontFamily, FontWeight.NORMAL, defaultFontSize + 2.0));
        
        phyloPenCanvas.setScratchOutErasingEnabled(true);
        phyloPenCanvas.setViewport(canvasViewport);
        minitree.setWorld(phyloPenCanvas);
        minitree.setWorldViewport(canvasViewport);
        
        colorByAttributeMinColorPicker.setValue(phyloPenCanvas.getColorByAttributeMinColor());
        colorByAttributeMaxColorPicker.setValue(phyloPenCanvas.getColorByAttributeMaxColor());
        colorByAttributeUndefinedColorPicker.setValue(phyloPenCanvas.getColorByAttributeUndefinedColor());
        
        colorByAttributeMinColorPicker.valueProperty().bindBidirectional(phyloPenCanvas.colorByAttributeMinColorProperty());
        colorByAttributeMaxColorPicker.valueProperty().bindBidirectional(phyloPenCanvas.colorByAttributeMaxColorProperty());
        colorByAttributeUndefinedColorPicker.valueProperty().bindBidirectional(phyloPenCanvas.colorByAttributeUndefinedColorProperty());
        
        canvasViewport.addEventHandler(ScrollEvent.ANY, new EventHandler<ScrollEvent>()
        {
            @Override
            public void handle(ScrollEvent event)
            {
                System.out.println("Warning: ScrollPane Moved.");
                event.consume();
            }
        });
        
        phyloPenCanvas.getSelectedNodes().addListener(new ListChangeListener()
        {
            @Override
            public void onChanged(ListChangeListener.Change c)
            {
                rotateCladeButton.setDisable(phyloPenCanvas.getSelectedNodes().isEmpty());
                cutCladeButton.setDisable(phyloPenCanvas.getSelectedNodes().size() != 1 || phyloPenCanvas.getCutClade() != null);
                pasteCladeButton.setDisable(phyloPenCanvas.getCutClade() == null || phyloPenCanvas.getSelectedNodes().size() != 1);
                deleteCladeButton.setDisable(phyloPenCanvas.getSelectedNodes().size() != 1);
            }
        });
        
        phyloPenCanvas.modelRenderTanslationXProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                translationLabel.setText("Translation: (" + roundDouble(newValue.doubleValue(), 1) + ", " + roundDouble(phyloPenCanvas.getModelRenderTranslationY(), 1) + ")");
            }
        });
        
        phyloPenCanvas.modelRenderTanslationYProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                translationLabel.setText("Translation: (" + roundDouble(phyloPenCanvas.getModelRenderTranslationX(), 1) + ", " + roundDouble(newValue.doubleValue(), 1) + ")");
            }
        });
        
        phyloPenCanvas.modelRenderScaleProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                zoomLabel.setText("Zoom: " + roundDouble(newValue.doubleValue() * 100, 1) + "%");
            }
        });
        
        attributeTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Clade>()
        {
            @Override
            public void changed(ObservableValue<? extends Clade> observable, Clade oldValue, Clade newValue)
            {
                imageViewer.setClade(newValue);
            }
        });
        
        phyloPenCanvas.addListener(new InkGestureListener()
        {
            @Override
            public void gestureRecognized(InkGesture gesture)
            {
                if (gesture == null)
                {
                    statusRect.setFill(Color.RED);
                    final Timeline timeline = new Timeline();
                    timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100.0), new BorderColorGhostingHandler(canvasViewport, Color.RED, timeline)));
                    timeline.setCycleCount(Timeline.INDEFINITE);
                    timeline.play();
                }
                else
                    statusRect.setFill(Color.LIME);
            }
        });
        
        branchColorChooser.setValue(phyloPenCanvas.getDefaultBranchColor());
        nodeOutlineColorChooser.setValue(phyloPenCanvas.getDefaultInnerNodeStroke());
        nodeFillColorChooser.setValue(phyloPenCanvas.getDefaultInnerNodeFill());
        
        scaledBranchesCheckbox.selectedProperty().bindBidirectional(phyloPenCanvas.scalingBranchesByLengthProperty());
        showSpeciesNamesCheckbox.selectedProperty().bindBidirectional(phyloPenCanvas.labelingLeafNodeNamesProperty());
        showIntermediateBranchLengthsCheckbox.selectedProperty().bindBidirectional(phyloPenCanvas.labelingIntermediateBranchLengthsProperty());
        showFinalBranchLengthsOnBranchesCheckbox.selectedProperty().bindBidirectional(phyloPenCanvas.labelingFinalBranchLengthsProperty());
        showFinalBranchLengthsWithLeafNamesCheckbox.selectedProperty().bindBidirectional(phyloPenCanvas.labelingFinalBranchLengthsWithLeafNamesProperty());
        showAncestorLabelCheckbox.selectedProperty().bindBidirectional(phyloPenCanvas.labelingAncestorNamesProperty());
        showHyperlinksCheckbox.selectedProperty().bindBidirectional(phyloPenCanvas.showingHyperlinksProperty());
        showAnnotationPlaceholdersCheckbox.selectedProperty().bindBidirectional(phyloPenCanvas.showingAnnotationPlaceholdersProperty());
        showLeafImagesCheckbox.selectedProperty().bindBidirectional(phyloPenCanvas.leafImagesVisibleProperty());
        
        this.getPhyloPenCanvas().getUndoableEvents().sizeProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
        {
            undoMenuItem.setDisable(newValue.intValue() == 0);
        });
        
        // send prompt
        do
        {
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle("Volunteer ID");
            dialog.setHeaderText("Please enter your Volunteer ID before proceeding: ");

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent())
            {
                user = result.get();
            }
        } while (user == null || user.trim().equals(""));
        
        boolean userDataLoaded = false;
        
        try
        {
            userDataLoaded = loadLocalTrainingData(user);
        }
        catch (IOException e)
        {
            System.out.println("INFO: Unable to load local ink training data");
            e.printStackTrace();
        }
        
        if (!userDataLoaded)
        {
            handleTrainingRecognition(null);
        }
        
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                loadFromDefaultDirectory();
            }
        });
        
        if (!userDataLoaded)
        {
            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    new FindingCommandHelpDialog().showAndWait();
                }
            });
            
            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    handleCommandHelpAction(null);
                }
            });
        }
        
        if (mouseMode)
        {
            getPhyloPenCanvas().getModelRenderPane().widthProperty().addListener(new ChangeListener<Number>()
            {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
                {
                    hInnerPaneScrollBar.setMin(-newValue.doubleValue() * getPhyloPenCanvas().getModelRenderScale());
                }
            });

            getPhyloPenCanvas().getModelRenderPane().heightProperty().addListener(new ChangeListener<Number>()
            {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
                {
                    vInnerPaneScrollBar.setMin(-newValue.doubleValue() * getPhyloPenCanvas().getModelRenderScale());
                }
            });

            getPhyloPenCanvas().modelRenderScaleProperty().addListener(new ChangeListener<Number>()
            {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
                {
                    vInnerPaneScrollBar.setMin(-getPhyloPenCanvas().getModelRenderPane().getHeight() * newValue.doubleValue());
                    vInnerPaneScrollBar.setBlockIncrement(canvasViewport.getHeight() / getPhyloPenCanvas().getModelRenderScale());
                    vInnerPaneScrollBar.setVisibleAmount(canvasViewport.getHeight() / getPhyloPenCanvas().getModelRenderScale());

                    hInnerPaneScrollBar.setMin(-getPhyloPenCanvas().getModelRenderPane().getWidth() * newValue.doubleValue());
                    hInnerPaneScrollBar.setBlockIncrement(canvasViewport.getWidth() / newValue.doubleValue());
                    hInnerPaneScrollBar.setVisibleAmount(canvasViewport.getWidth() / newValue.doubleValue());

                    vInnerPaneScrollBar.setUnitIncrement(50.0 * newValue.doubleValue());
                    hInnerPaneScrollBar.setUnitIncrement(50.0 * newValue.doubleValue());
                }
            });

            getPhyloPenCanvas().modelRenderTanslationXProperty().addListener(new ChangeListener<Number>()
            {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
                {
                    double invertedNewValue = getValueInverted(newValue.doubleValue(), hInnerPaneScrollBar.getMin(), hInnerPaneScrollBar.getMax());
                    if (invertedNewValue != hInnerPaneScrollBar.getValue())
                        hInnerPaneScrollBar.setValue(invertedNewValue);
                }
            });

            getPhyloPenCanvas().modelRenderTanslationYProperty().addListener(new ChangeListener<Number>()
            {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
                {
                    double invertedNewValue = getValueInverted(newValue.doubleValue(), vInnerPaneScrollBar.getMin(), vInnerPaneScrollBar.getMax());
                    if (invertedNewValue != vInnerPaneScrollBar.getValue())
                        vInnerPaneScrollBar.setValue(invertedNewValue);
                }
            });

            canvasViewport.widthProperty().addListener(new ChangeListener<Number>()
            {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
                {
                    hInnerPaneScrollBar.setBlockIncrement(newValue.doubleValue() * getPhyloPenCanvas().getModelRenderScale());
                    hInnerPaneScrollBar.setVisibleAmount(newValue.doubleValue() * getPhyloPenCanvas().getModelRenderScale());
                    hInnerPaneScrollBar.setMax(newValue.doubleValue());
                }
            });

            canvasViewport.heightProperty().addListener(new ChangeListener<Number>()
            {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
                {
                    vInnerPaneScrollBar.setBlockIncrement(newValue.doubleValue() * getPhyloPenCanvas().getModelRenderScale());
                    vInnerPaneScrollBar.setVisibleAmount(newValue.doubleValue() * getPhyloPenCanvas().getModelRenderScale());
                    vInnerPaneScrollBar.setMax(newValue.doubleValue());
                }
            });

            vInnerPaneScrollBar.setBlockIncrement(canvasViewport.getHeight());
            hInnerPaneScrollBar.setBlockIncrement(canvasViewport.getWidth());
            vInnerPaneScrollBar.setUnitIncrement(50.0);
            hInnerPaneScrollBar.setUnitIncrement(50.0);
            vInnerPaneScrollBar.setMax(canvasViewport.getHeight());
            hInnerPaneScrollBar.setMax(canvasViewport.getWidth());

            hInnerPaneScrollBar.valueProperty().addListener(new ChangeListener<Number>()
            {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
                {
                    if (newValue.doubleValue() != getPhyloPenCanvas().getModelRenderTranslationX())
                        getPhyloPenCanvas().translateModelRenderTo(getValueInverted(newValue.doubleValue(), hInnerPaneScrollBar.getMin(), hInnerPaneScrollBar.getMax()), getPhyloPenCanvas().getModelRenderTranslationY());
                }
            });

            vInnerPaneScrollBar.valueProperty().addListener(new ChangeListener<Number>()
            {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
                {
                    if (newValue.doubleValue() != getPhyloPenCanvas().getModelRenderTranslationY())
                    {
                        getPhyloPenCanvas().translateModelRenderTo(getPhyloPenCanvas().getModelRenderTranslationX(), getValueInverted(newValue.doubleValue(), vInnerPaneScrollBar.getMin(), vInnerPaneScrollBar.getMax()));
                    }
                }
            });
        }
        else
        {
            canvasBorderPane.getChildren().remove(vInnerPaneScrollBar);
            canvasBorderPane.getChildren().remove(hInnerPaneScrollBar);
        }
    }
    
    private double getValueInverted(double value, double rangeMin, double rangeMax)
    {
        double midPoint = (rangeMax + rangeMin) / 2.0;
        double distanceFromMidpoint, newValue;
        if (midPoint > value)
        {
            distanceFromMidpoint = midPoint - value;
            newValue = midPoint + distanceFromMidpoint;
        }
        else
        {
            distanceFromMidpoint = value - midPoint;
            newValue = midPoint - distanceFromMidpoint;
        }
        
        return newValue;
    }
    
    public PhyloPenCanvas getPhyloPenCanvas()
    {
        return phyloPenCanvas;
    }
    
    public void loadFromDefaultDirectory()
    {
        RemScaler rem = new RemScaler();
        
        try
        {
            //File treeFile = new File("resources/data/catfish_tree_with_images.nested-json");
            File treeFile;
            String fs = System.getProperty("file.separator");
            String [] fileExtensions = {"nested-json", "json"};
            
            SingleDirectoryFileChooserDialog openDialog = new SingleDirectoryFileChooserDialog("resources" + fs + "lessons", fileExtensions);
            openDialog.getFileList().setPrefSize(rem.scale(600), rem.scale(600));
            openDialog.setTitle("Open Lessons");
            Optional<File> result = openDialog.showAndWait();
            
            if (result.isPresent() && result.get() != null)
            {
                treeFile = result.get();
                loadTreeModel(treeFile.getAbsolutePath(), true, user);
            }
        }
        catch (IOException e)
        {
            new Alert(Alert.AlertType.ERROR, "Reading the specified file failed", ButtonType.OK).showAndWait();
        }
    }
    
    private boolean loadTreeModel(String path, boolean localFile, String user) throws IOException
    {
        if (PhyloPenIO.load(path, phyloPenCanvas, localFile, user))
        {
            System.out.println("Loaded: " + path);
            
            if (localFile)
                fileNameLabel.setText("File: " + path);
            else
                fileNameLabel.setText("File: @Arbor\\\\" + path);
            
            fileNameLabel.setTooltip(new Tooltip(path));
            treeResizeCanvas.setTreeCanvas(phyloPenCanvas);
            attributeTable.setCanvas(phyloPenCanvas);
            colorByList.setCanvas(phyloPenCanvas);
            annotationList.setCanvas(phyloPenCanvas);
            String name = path;
            if (name.contains("\\"))
                name = name.replace("\\", "");
            if (name.contains("/"))
                name = name.replace("/", "");
            if (name.contains(":"))
                name = name.replace(":", "");
            return true;
        }
        else
            new Alert(Alert.AlertType.ERROR, "Tree creation failed.", ButtonType.OK).showAndWait();
        
        return false;
    }
    
    private class BorderColorGhostingHandler implements EventHandler<ActionEvent>
    {
        private final Timeline timeline;
        private final Region borderedRegion;
        private long lastUpdateTime;
        private double alpha;
        private Color ghostingColor;
        
        public BorderColorGhostingHandler(Region borderedRegion, Color ghostingColor, Timeline timeline)
        {
            this.borderedRegion = borderedRegion;
            this.timeline = timeline;
            this.lastUpdateTime = System.currentTimeMillis();
            alpha = 1.0;
            this.ghostingColor = ghostingColor;
        }
        
        @Override
        public void handle(ActionEvent event)
        {
            double rate = 0.8; // per second
            long currentMillisFromEpoch = System.currentTimeMillis();
            alpha = Math.max(alpha - (rate * (currentMillisFromEpoch - lastUpdateTime) / 1000.0), 0.0);
            
            BorderStroke borderStroke = borderedRegion.getBorder().getStrokes().get(0);
            borderedRegion.setBorder(new Border(new BorderStroke(((Color)currentCanvasBorderColor).interpolate(ghostingColor, alpha), borderStroke.getTopStyle(), borderStroke.getRadii(), borderStroke.getWidths())));
            
            lastUpdateTime = currentMillisFromEpoch;
            
            if (alpha == 0.0)
            {
                timeline.stop();
            }
        }
    }
    
    private void setDisableCanvasControls(boolean disabled)
    {
        getPhyloPenCanvas().setTouchDisabled(disabled);
        getPhyloPenCanvas().setInkDisabled(disabled);
        getPhyloPenCanvas().setMouseDisabled(disabled);
    }
}
