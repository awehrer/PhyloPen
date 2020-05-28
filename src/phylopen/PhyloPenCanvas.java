/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package phylopen;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.awt.Desktop;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.Pair;
import static phylopen.PhyloPenIO.ANNOTATION;
import static phylopen.PhyloPenIO.ANNOTATION_EDIT;
import static phylopen.PhyloPenIO.ANNOTATION_REMOVED;
import static phylopen.PhyloPenIO.APPEARANCE_RESET;
import static phylopen.PhyloPenIO.APPEND_SELECTION;
import static phylopen.PhyloPenIO.BRANCH_RECOLOR;
import static phylopen.PhyloPenIO.BRANCH_WIDTH_CHANGE;
import static phylopen.PhyloPenIO.CLADE_COLLAPSE;
import static phylopen.PhyloPenIO.CLADE_CUT;
import static phylopen.PhyloPenIO.CLADE_CUT_REATTACH;
import static phylopen.PhyloPenIO.CLADE_EXPAND;
import static phylopen.PhyloPenIO.CLADE_INSERT;
import static phylopen.PhyloPenIO.CLADE_ROTATE;
import static phylopen.PhyloPenIO.COLOR_BY_ATTRIBUTE;
import static phylopen.PhyloPenIO.COLOR_BY_ATTRIBUTE_MAX_COLOR_CHANGE;
import static phylopen.PhyloPenIO.COLOR_BY_ATTRIBUTE_MIN_COLOR_CHANGE;
import static phylopen.PhyloPenIO.COLOR_BY_ATTRIBUTE_UNDEFINED_COLOR_CHANGE;
import static phylopen.PhyloPenIO.LABEL_FINAL_BRANCH_LENGTHS;
import static phylopen.PhyloPenIO.LABEL_FINAL_BRANCH_LENGTHS_W_LEAF_NAMES;
import static phylopen.PhyloPenIO.LABEL_INTERMEDIATE_BRANCH_LENGTHS;
import static phylopen.PhyloPenIO.LABEL_LEAF_NODE_NAMES;
import static phylopen.PhyloPenIO.LEAF_IMAGES_VISIBLE;
import static phylopen.PhyloPenIO.NODE_RADIUS_CHANGE;
import static phylopen.PhyloPenIO.NODE_RECOLOR;
import phylopen.PhyloPenIO.NodeConnection;
import static phylopen.PhyloPenIO.SCALE_BRANCHES;
import static phylopen.PhyloPenIO.SELECTION;
import static phylopen.PhyloPenIO.SHOW_ANNOTATION_PLACEHOLDERS;
import static phylopen.PhyloPenIO.TREE_RESIZE;
import phylopen.utility.IntersectionUtility;
import phylopen.utility.RemScaler;
import phylopen.utility.TextUtility;
import phylopen.utility.ink.InkStroke;
import phylopen.utility.ink.InkStrokeDrawingAttributes;
import phylopen.utility.ink.InkUtility;
import phylopen.utility.ink.StylusPoint;
import phylopen.utility.ink.recognition.AugmentedInkStroke;
import phylopen.utility.ink.recognition.InkGesture;
import phylopen.utility.ink.recognition.InkGestureListener;
import phylopen.utility.ink.recognition.InkGestureRecognizer;
import static phylopen.PhyloPenIO.DESELECTION;
import phylopen.utility.ink.recognition.AbstractInkGestureRecognitionProcedure;
import phylopen.utility.ink.recognition.DollarNInkGestureRecognizer;
import phylopen.utility.ink.recognition.gesture.TapRecognitionProcedure;
import static phylopen.PhyloPenIO.CLADE_ADD;
import static phylopen.PhyloPenIO.LABEL_ANCESTOR_NAMES;
import static phylopen.PhyloPenIO.SHOW_HYPERLINKS;

/**
 *
 * @author awehrer
 */
public class PhyloPenCanvas extends PhyloTreeInkCanvas
{
    private final double textRowHeight;
    private final double branchLabelWidth;
    
    private Color defaultBranchColor;
    private Color defaultOuterNodeFill;
    private Color defaultOuterNodeStroke;
    private Color defaultInnerNodeFill;
    private Color defaultInnerNodeStroke;
    private double defaultNodeRadius;
    private BooleanProperty scalingBranchesByLengthProperty;
    private BooleanProperty labelingLeafNodeNamesProperty;
    private BooleanProperty labelingIntermediateBranchLengthsProperty;
    private BooleanProperty labelingFinalBranchLengthsWithLeafNamesProperty;
    private BooleanProperty labelingFinalBranchLengthsProperty;
    private BooleanProperty labelingAncestorNamesProperty;
    private BooleanProperty leafImagesVisibleProperty;
    private BooleanProperty showingAnnotationPlaceholdersProperty;
    private BooleanProperty showingHyperlinksProperty;
    private BooleanProperty preservingTreeDimensionsOnCladeDeletionProperty;
    private double lengthScaling;
    private double lineLength;
    private double layoutXScale; // layout scale
    private double layoutYScale; // layout scale
    private double layoutXOffset;
    private double layoutYOffset;
    private double layoutRowHeight;
    private double rightEdge;
    
    private PhylogeneticTree lastModelCached;
    private final HashMap<Clade, CladeMetadata> cladeMetadataCache;
    private final HashMap<String, AttributeMetadata> attributeMetadataCache;
    private final ObservableSet<Clade> collapsedClades;
    private double symbolRecognitionConfidenceThreshold;
    private ObservableList<AnnotationPolygon> annotationPlaceholders;
    protected int annotationLayerIndex;
    protected int tooltipLayerIndex;
    
    private ObservableList<Clade> selectedNodes;
    protected int selectionLayerIndex;
    private SelectionPolygon selectionMarker;
    private SelectionRectangle selectionRectangle;
    
    private ScrollPane viewport;
    
    private ObjectProperty<Color> colorByAttributeMinColorProperty;
    private ObjectProperty<Color> colorByAttributeMaxColorProperty;
    private ObjectProperty<Color> colorByAttributeUndefinedColorProperty;
    
    private StringProperty colorByAttribute;
    
    private ObservableList<CladeImageViewer> cladeImages;
    protected int cladeImageLayerIndex;
    private double imageOffsetFromLeaf;
    
    private Clade cutClade;
    private double cutTransparency;
    
    private boolean mouseDisabled;
    
    private boolean initializingTree;
    
    private EventFileWriter eventWriter;
    private boolean eventTranscribing;
    
    public PhyloPenCanvas()
    {
        RemScaler rem = new RemScaler();
        
        textRowHeight = rem.scale(30.0);
        branchLabelWidth = rem.scale(200.0);
        
        defaultBranchColor = Color.GRAY;
        defaultOuterNodeFill = Color.LIME;
        defaultOuterNodeStroke = Color.LIMEGREEN;
        defaultInnerNodeFill = Color.WHITESMOKE;
        defaultInnerNodeStroke = Color.TAN;
        defaultNodeRadius = 5.0;
        
        scalingBranchesByLengthProperty = new SimpleBooleanProperty(false);
        labelingLeafNodeNamesProperty = new SimpleBooleanProperty(true);
        labelingIntermediateBranchLengthsProperty = new SimpleBooleanProperty(true);
        labelingFinalBranchLengthsWithLeafNamesProperty = new SimpleBooleanProperty(true);
        labelingFinalBranchLengthsProperty = new SimpleBooleanProperty(false);
        labelingAncestorNamesProperty = new SimpleBooleanProperty(true);
        leafImagesVisibleProperty = new SimpleBooleanProperty(true);
        showingAnnotationPlaceholdersProperty = new SimpleBooleanProperty(true);
        showingHyperlinksProperty = new SimpleBooleanProperty(true);
        preservingTreeDimensionsOnCladeDeletionProperty = new SimpleBooleanProperty(true);
        
        colorByAttributeMinColorProperty = new SimpleObjectProperty<>(Color.BLACK);
        colorByAttributeMaxColorProperty = new SimpleObjectProperty<>(Color.WHITE);
        colorByAttributeUndefinedColorProperty = new SimpleObjectProperty<>(Color.ORANGE);
        colorByAttribute = new SimpleStringProperty(this, null);
        
        lengthScaling = rem.scale(1000.0);
        lineLength = rem.scale(100.0);
        layoutXScale = 1.0;
        layoutYScale = 1.0;
        layoutXOffset = rem.scale(36.0);
        layoutYOffset = rem.scale(36.0);
        layoutRowHeight = rem.scale(16.0);
        
        imageOffsetFromLeaf = rem.scale(200.0);
        
        cladeMetadataCache = new HashMap<>();
        attributeMetadataCache = new HashMap<>();
        collapsedClades = FXCollections.observableSet(new TreeSet<Clade>());
        
        nodeMarkerLayerIndex = 5;
        branchLineLayerIndex = 4;
        labelLayerIndex = 6;
        getLayeredModelRenderChildren().insertNewLayer(0);
        getLayeredModelRenderChildren().insertNewLayer(0);
        getLayeredModelRenderChildren().insertNewLayer(0);
        getLayeredModelRenderChildren().insertNewLayer(0);
        tooltipLayerIndex = 0;
        cladeImageLayerIndex = 1;
        annotationLayerIndex = 2;
        selectionLayerIndex = 3;
        
        symbolRecognitionConfidenceThreshold = 0.5;
        
        cladeImages = FXCollections.observableList(new ArrayList<CladeImageViewer>());
        selectedNodes = FXCollections.observableList(new ArrayList<Clade>());
        
        cutClade = null;
        cutTransparency = 0.3;
        
        initializingTree = false;
        eventTranscribing = true;
        
        setGestureRecognizer(new PhyloPenInkGestureRecognizer());
        
        annotationPlaceholders = FXCollections.observableList(new LinkedList<>());
        
        getAnnotations().addListener(new ListChangeListener<Annotation>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Annotation> change)
            {
                while (change.next())
                {
                    if (change.wasAdded() && !change.wasRemoved())
                    {
                        int startIndex = change.getFrom();
                        List<? extends Annotation> newAnnotations = new ArrayList<>(change.getAddedSubList());
                        List<AnnotationPolygon> placeholders = new ArrayList<>();
                        
                        for (Annotation annotation : newAnnotations)
                            placeholders.add(new AnnotationPolygon(annotation));
                        
                        if (!PhyloPenCanvas.this.isShowingAnnotationPlaceholders())
                        {
                            for (AnnotationPolygon placeholder : placeholders)
                                placeholder.setVisible(false);
                        }
                        
                        PhyloPenCanvas.this.annotationPlaceholders.addAll(placeholders);
                        
                        ObservableList<Node> modelRenderPaneChildren = PhyloPenCanvas.this.getModelRenderPane().getChildren();
                        int additionIndex = PhyloPenCanvas.this.getLayeredModelRenderChildren().addAllToLayer(annotationLayerIndex, startIndex, (List)placeholders);
                        modelRenderPaneChildren.addAll(additionIndex, placeholders);
                    }
                    else if (!change.wasAdded() && change.wasRemoved())
                    {
                        List<? extends Annotation> annotationsToRemove = new ArrayList<>(change.getRemoved());
                        List<AnnotationPolygon> placeholdersToRemove = new ArrayList<>();
                        Iterator<AnnotationPolygon> placeholderIterator;
                        AnnotationPolygon placeholder;
                        
                        for (Annotation annotation : annotationsToRemove)
                        {
                            placeholderIterator = PhyloPenCanvas.this.annotationPlaceholders.iterator();
                            
                            while (placeholderIterator.hasNext())
                            {
                                placeholder = placeholderIterator.next();
                                
                                if (placeholder.getAnnotation() == annotation)
                                {
                                    placeholdersToRemove.add(placeholder);
                                    placeholderIterator.remove();
                                    break;
                                }
                            }
                        }
                        
                        ObservableList<Node> modelRenderPaneChildren = PhyloPenCanvas.this.getModelRenderPane().getChildren();
                        modelRenderPaneChildren.removeAll(placeholdersToRemove);
                        PhyloPenCanvas.this.getLayeredModelRenderChildren().removeAllFromLayer(annotationLayerIndex, (List)placeholdersToRemove);
                    }
                    else
                        System.out.println("Change not handled.");
                }
            }
        });
        
        cladeImages.addListener(new ListChangeListener<CladeImageViewer>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends CladeImageViewer> change)
            {
                while (change.next())
                {
                    if (change.wasAdded() && !change.wasRemoved())
                    {
                        int startIndex = change.getFrom();
                        List<? extends CladeImageViewer> childrenToAdd = new ArrayList<>(change.getAddedSubList());
                        ObservableList<Node> modelRenderPaneChildren = PhyloPenCanvas.this.getModelRenderPane().getChildren();
                        int additionIndex = PhyloPenCanvas.this.getLayeredModelRenderChildren().addAllToLayer(cladeImageLayerIndex, startIndex, (List)childrenToAdd);
                        modelRenderPaneChildren.addAll(additionIndex, childrenToAdd);
                    }
                    else if (!change.wasAdded() && change.wasRemoved())
                    {
                        List<? extends Node> childrenToRemove = new ArrayList<>(change.getRemoved());
                        PhyloPenCanvas.this.getModelRenderPane().getChildren().removeAll(childrenToRemove);
                        PhyloPenCanvas.this.getLayeredModelRenderChildren().removeAllFromLayer(cladeImageLayerIndex, (List)childrenToRemove);
                    }
                    else
                        System.out.println("Change not handled.");
                }
            }
        });
        
        this.addListener(new InkGestureListener()
        {
            @Override
            public void gestureRecognized(InkGesture inkGesture)
            {
                PhyloPenCanvas.this.clearSelection();
                
                if (inkGesture != null)
                {
                    String gestureIdentifier = inkGesture.getIdentifier();
                    
                    // "Triangle", "Rectangle", "Horizontal line", "Vertical slash", "X", "Left square bracket", "L", "Reverse L"
                    switch (gestureIdentifier)
                    {
                        case "Tap":
                            processTapGesture(inkGesture.getAugmentedStrokeList());
                            break;
                        case "Rectangle":
                            processRectangleGesture((Rectangle)inkGesture.getIdentifiedShape());
                            break;
                        case "Triangle":
                            processTriangleCollapseExpandGesture((Polygon)inkGesture.getIdentifiedShape(), inkGesture.getAugmentedStrokeList());
                            break;
                        case "Vertical slash":
                            processVerticalLineGesture(inkGesture.getAugmentedStrokeList());
                            break;
                        case "X":
                            processXGesture(inkGesture.getAugmentedStrokeList());
                            break;
                        case "Cut and reattach":
                            processCutAndReattachGesture(inkGesture.getAugmentedStrokeList());
                            break;
                        case "Left square bracket":
                            processSquareBracketChildReverseGesture((Polyline)inkGesture.getIdentifiedShape(), inkGesture.getAugmentedStrokeList());
                            break;
                        case "Horizontal line":
                            processHorizontalLineGesture(inkGesture.getAugmentedStrokeList());
                            break;
                        case "L":
                            processLGesture(inkGesture.getAugmentedStrokeList());
                            break;
                        case "Reverse L":
                            processReverseLGesture(inkGesture.getAugmentedStrokeList());
                            break;
                        case "Lasso":
                            processLassoGesture(inkGesture.getAugmentedStrokeList());
                            break;
                    }
                }
                else
                {
                    final ArrayList<InkStroke> failedInkStrokes = new ArrayList<>(PhyloPenCanvas.this.getInkStrokes());
                    PhyloPenCanvas.this.getInkStrokes().clear();
                    
                    Platform.runLater(new Runnable()
                    {
                        public void run()
                        {
                            PhyloPenCanvas.this.getChildren().addAll(failedInkStrokes);
                            
                            InkStrokeDrawingAttributes d;
                            Timeline timeline;
                            
                            for (InkStroke stroke : failedInkStrokes)
                            {
                                d = stroke.getDrawingAttributes();
                                stroke.setDrawingAttributes(new InkStrokeDrawingAttributes(Color.RED, d.getStrokeWidth(), d.getStylusTip(), d.isHighlighter()));
                                
                                timeline = new Timeline();
                                timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100.0), new InkStrokeGhostingHandler(stroke, timeline)));
                                timeline.setCycleCount(Timeline.INDEFINITE);
                                timeline.play();
                            }
                        }
                    });
                }
            }
        });
        
        scalingBranchesByLengthProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (!oldValue.equals(newValue))
                {
                    JsonObject event = PhyloPenIO.createEventRecord(SCALE_BRANCHES);
                    event.addProperty("value", newValue);
                    addUndoableEvent(event);
                    
                    PhyloPenCanvas.this.updateLayout();
                    
                    if (selectionMarker != null)
                        selectionMarker.recalculateBounds();
                }
            }
        });
        
        labelingLeafNodeNamesProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (!oldValue.equals(newValue))
                {
                    JsonObject event = PhyloPenIO.createEventRecord(LABEL_LEAF_NODE_NAMES);
                    event.addProperty("value", newValue);
                    addUndoableEvent(event);
                    
                    PhyloPenCanvas.this.updateLayout();
                }
            }
        });
        
        labelingIntermediateBranchLengthsProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (!oldValue.equals(newValue))
                {
                    JsonObject event = PhyloPenIO.createEventRecord(LABEL_INTERMEDIATE_BRANCH_LENGTHS);
                    event.addProperty("value", newValue);
                    addUndoableEvent(event);
                    
                    PhyloPenCanvas.this.updateLayout();
                }
            }
        });
        
        labelingFinalBranchLengthsWithLeafNamesProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (!oldValue.equals(newValue))
                {
                    JsonObject event = PhyloPenIO.createEventRecord(LABEL_FINAL_BRANCH_LENGTHS_W_LEAF_NAMES);
                    event.addProperty("value", newValue);
                    addUndoableEvent(event);
                    
                    PhyloPenCanvas.this.updateLayout();
                }
            }
        });
        
        labelingAncestorNamesProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (!oldValue.equals(newValue))
                {
                    JsonObject event = PhyloPenIO.createEventRecord(LABEL_ANCESTOR_NAMES);
                    event.addProperty("value", newValue);
                    addUndoableEvent(event);
                    
                    PhyloPenCanvas.this.updateLayout();
                }
            }
        });
        
        labelingFinalBranchLengthsProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (!oldValue.equals(newValue))
                {
                    JsonObject event = PhyloPenIO.createEventRecord(LABEL_FINAL_BRANCH_LENGTHS);
                    event.addProperty("value", newValue);
                    addUndoableEvent(event);
                    
                    PhyloPenCanvas.this.updateLayout();
                }
            }
        });
        
        leafImagesVisibleProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (!oldValue.equals(newValue))
                {
                    JsonObject event = PhyloPenIO.createEventRecord(LEAF_IMAGES_VISIBLE);
                    event.addProperty("value", newValue);
                    addUndoableEvent(event);
                    
                    PhyloPenCanvas.this.updateLayout();
                }
            }
        });
        
        showingAnnotationPlaceholdersProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (!oldValue.equals(newValue))
                {
                    JsonObject event = PhyloPenIO.createEventRecord(SHOW_ANNOTATION_PLACEHOLDERS);
                    event.addProperty("value", newValue);
                    addUndoableEvent(event);
                    
                    if (!newValue)
                    {
                        for (AnnotationPolygon placeholder : getAnnotationPlaceholders())
                        {
                            placeholder.setFixedDisplay(false);
                            placeholder.setVisible(isShowingAnnotationPlaceholders());
                        }
                    }
                    else
                    {
                        updateLayout();
                    }
                }
            }
        });
        
        showingHyperlinksProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if (!oldValue.equals(newValue))
                {
                    JsonObject event = PhyloPenIO.createEventRecord(SHOW_HYPERLINKS);
                    event.addProperty("value", newValue);
                    addUndoableEvent(event);

                    for (Node label : PhyloPenCanvas.this.getLabels())
                    {
                        if (label instanceof Hyperlink)
                            label.setDisable(!newValue);
                    }
                }
            }
        });
        
        colorByAttributeMinColorProperty().addListener(new ChangeListener<Color>()
        {
            @Override
            public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue)
            {
                if (!oldValue.equals(newValue))
                {
                    recolorClades();
                    
                    JsonObject event = PhyloPenIO.createEventRecord(COLOR_BY_ATTRIBUTE_MIN_COLOR_CHANGE);
                    PhyloPenIO.attachColorProperty(event, newValue);
                    PhyloPenIO.attachColorProperty(event, oldValue, "oldColor");
                    addUndoableEvent(event);
                }
            }
        });
        
        colorByAttributeMaxColorProperty().addListener(new ChangeListener<Color>()
        {
            @Override
            public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue)
            {
                if (!oldValue.equals(newValue))
                {
                    recolorClades();
                    
                    JsonObject event = PhyloPenIO.createEventRecord(COLOR_BY_ATTRIBUTE_MAX_COLOR_CHANGE);
                    PhyloPenIO.attachColorProperty(event, newValue);
                    PhyloPenIO.attachColorProperty(event, oldValue, "oldColor");
                    addUndoableEvent(event);
                }
            }
        });
        
        colorByAttributeUndefinedColorProperty().addListener(new ChangeListener<Color>()
        {
            @Override
            public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue)
            {
                if (!oldValue.equals(newValue))
                {
                    recolorClades();
                    
                    JsonObject event = PhyloPenIO.createEventRecord(COLOR_BY_ATTRIBUTE_UNDEFINED_COLOR_CHANGE);
                    PhyloPenIO.attachColorProperty(event, newValue);
                    PhyloPenIO.attachColorProperty(event, oldValue, "oldColor");
                    addUndoableEvent(event);
                }
            }
        });
        
        colorByAttributeProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                if ((oldValue == null && newValue != null) || (oldValue != null && !oldValue.equals(newValue)))
                {
                    recolorClades();
                    
                    JsonObject event = PhyloPenIO.createEventRecord(COLOR_BY_ATTRIBUTE);
                    event.addProperty("name", newValue);
                    event.addProperty("oldName", oldValue);
                    addUndoableEvent(event);
                }
            }
        });
        
        MouseSelectionHandler mouseSelectionHandler = new MouseSelectionHandler();
        
        this.setEventHandler(MouseEvent.MOUSE_PRESSED, mouseSelectionHandler);
        this.setEventHandler(MouseEvent.MOUSE_CLICKED, mouseSelectionHandler);
        this.setEventHandler(MouseEvent.MOUSE_DRAGGED, mouseSelectionHandler);
        this.setEventHandler(MouseEvent.MOUSE_RELEASED, mouseSelectionHandler);
        
    } // end PhyloPenCanvas constructor
    
    private class MouseSelectionHandler implements EventHandler<MouseEvent>
    {
        private double originX;
        private double originY;
        private boolean lastPressWasTouch;
        private boolean lastPressWasDragged;
        
        public MouseSelectionHandler()
        {
            originX = Double.NaN;
            originY = Double.NaN;
            lastPressWasTouch = false;
            lastPressWasDragged = false;
        }
        
        @Override
        public void handle(MouseEvent event)
        {
            if (AppResources.getOptions().isMouseMode() && !isMouseDisabled())
            {
                if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED))
                {
                    if (!PhyloPenCanvas.this.isTouched())
                    {
                        originX = event.getX();
                        originY = event.getY();
                        lastPressWasTouch = false;
                        lastPressWasDragged = false;
                    }
                    else
                    {
                        lastPressWasTouch = true;
                    }
                }
                else
                {
                    if (!PhyloPenCanvas.this.isTouched())
                    {
                        if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED))
                        {
                            lastPressWasDragged = true;
                            
                            if (!Double.isNaN(originX) && !Double.isNaN(originY))
                            {
                                if (event.getButton().equals(MouseButton.PRIMARY))
                                {
                                    if (isTouchDisabled() && isInkDisabled())
                                    {
                                        translateModelRender(-(originX - event.getX()) / getModelRenderScale(), -(originY - event.getY()) / getModelRenderScale());
                                        originX = event.getX();
                                        originY = event.getY();
                                    }
                                    else
                                    {
                                        if (selectionRectangle == null)
                                        {
                                            selectionRectangle = new SelectionRectangle(originX, originY, 0.0, 0.0);
                                            int additionIndex = getLayeredModelRenderChildren().addToLayer(selectionLayerIndex, selectionRectangle);
                                            getModelRenderPane().getChildren().add(additionIndex, selectionRectangle);
                                        }

                                        Point2D point1 = getModelRenderPane().parentToLocal(originX, originY);
                                        Point2D point2 = getModelRenderPane().parentToLocal(event.getX(), event.getY());
                                        double rectangleOriginX, rectangleOriginY, rectangleWidth, rectangleHeight;

                                        rectangleOriginX = point1.getX() <= point2.getX() ? point1.getX() : point2.getX();
                                        rectangleOriginY = point1.getY() <= point2.getY() ? point1.getY() : point2.getY();
                                        rectangleWidth = Math.abs(point2.getX() - point1.getX());
                                        rectangleHeight = Math.abs(point2.getY() - point1.getY());

                                        selectionRectangle.setX(rectangleOriginX);
                                        selectionRectangle.setY(rectangleOriginY);
                                        selectionRectangle.setWidth(rectangleWidth);
                                        selectionRectangle.setHeight(rectangleHeight);
                                    }
                                }
                            }
                        }
                        else if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED))
                        {
                            if (event.getButton().equals(MouseButton.PRIMARY))
                            {
                                if (selectionRectangle != null)
                                {
                                    selectionRectangle.stopAnimation();

                                    selectNodesInBounds(selectionRectangle.getX(), selectionRectangle.getY(), selectionRectangle.getWidth(), selectionRectangle.getHeight());

                                    getModelRenderPane().getChildren().remove(selectionRectangle);
                                    getLayeredModelRenderChildren().removeFromLayer(selectionLayerIndex, selectionRectangle);
                                    selectionRectangle = null;
                                }
                            }

                            originX = Double.NaN;
                            originY = Double.NaN;
                        }
                        else if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED))
                        {
                            if (event.getButton().equals(MouseButton.PRIMARY) && !lastPressWasTouch && !event.isSynthesized() && !lastPressWasDragged)
                            {
                                Circle nodeCircle;
                                Node target = null;
                                Point2D point;

                                for (Node nodeMarker : PhyloPenCanvas.this.getNodeMarkers())
                                {
                                    if (nodeMarker instanceof Circle && nodeMarker.isVisible())
                                    {
                                        nodeCircle = (Circle) nodeMarker;
                                        point = getModelRenderPane().parentToLocal(event.getX(), event.getY());

                                        if (InkUtility.distance(point.getX(), point.getY(), nodeCircle.getCenterX(), nodeCircle.getCenterY()) <= nodeCircle.getRadius())
                                        {
                                            target = nodeCircle;
                                            break;
                                        }
                                    }
                                }
                                
                                if (event.getClickCount() == 1)
                                {
                                    if (target == null)
                                    {
                                        // Case: Clicking off a selection to clear it
                                        if (!event.isControlDown() && !PhyloPenCanvas.this.getSelectedNodes().isEmpty())
                                        {
                                            PhyloPenCanvas.this.clearSelection();
                                        }
                                        
                                        if (PhyloPenCanvas.this.getCutClade() != null)
                                            PhyloPenCanvas.this.undoCutClade();
                                    }
                                    else
                                    {
                                        List<Clade> selectedNodes = new LinkedList<Clade>();
                                        selectedNodes.add(PhyloPenCanvas.this.getClade(target));

                                        if (PhyloPenCanvas.this.getSelectedNodes().isEmpty())
                                        {
                                            PhyloPenCanvas.this.selectNodes(selectedNodes);
                                        }
                                        else
                                        {
                                            if (PhyloPenCanvas.this.getSelectedNodes().contains(selectedNodes.get(0)))
                                            {
                                                if (event.isControlDown())
                                                {
                                                    PhyloPenCanvas.this.deselectNodes(selectedNodes);
                                                }
                                                else
                                                {
                                                    PhyloPenCanvas.this.selectNodes(selectedNodes);
                                                }
                                            }
                                            else
                                            {
                                                if (event.isControlDown())
                                                {
                                                    PhyloPenCanvas.this.appendSelectNodes(selectedNodes);
                                                }
                                                else
                                                {
                                                    PhyloPenCanvas.this.selectNodes(selectedNodes);
                                                }
                                            }
                                        }
                                    } // end if-else
                                }
                                else if (event.getClickCount() == 2 && target != null)
                                {
                                    Clade targetForExpandCollapse = PhyloPenCanvas.this.getClade(target);
                                    
                                    if (PhyloPenCanvas.this.isCollapsed(targetForExpandCollapse))
                                        PhyloPenCanvas.this.expandClade(targetForExpandCollapse);
                                    else
                                        PhyloPenCanvas.this.collapseClade(targetForExpandCollapse);
                                    
                                    clearSelection();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void loadFromJson(JsonElement parsedJson, String treeFileName, String user)
    {
        if (eventWriter != null)
            eventWriter.stop();
        
        initializingTree = true;
        clearUndoableEvents();
        Clade.resetNextId();
        Annotation.resetNextId();
        PhyloPenIO.resetNextEventId();
        
        // load tree
        setTreeModel(new PhylogeneticTree(parsedJson));
        updateLayout();
        
        JsonObject treeJson = parsedJson.getAsJsonObject();
        RemScaler rem = new RemScaler();
        
        // set additional options
        if (treeJson.has("treeWidth") && treeJson.has("treeHeight"))
            this.setTreeDimensions(rem.scale(treeJson.get("treeWidth").getAsDouble()), rem.scale(treeJson.get("treeHeight").getAsDouble()));
        if (treeJson.has("branchesScaledByLength"))
            this.setScalingBranchesByLength(treeJson.get("branchesScaledByLength").getAsBoolean());
        if (treeJson.has("showSpeciesNames"))
            this.setLabelingLeafNodeNames(treeJson.get("showSpeciesNames").getAsBoolean());
        if (treeJson.has("showIntermediateBranchLengths"))
            this.setLabelingIntermediateBranchLengths(treeJson.get("showIntermediateBranchLengths").getAsBoolean());
        if (treeJson.has("showFinalBranchLengthsOnBranches"))
            this.setLabelingFinalBranchLengths(treeJson.get("showFinalBranchLengthsOnBranches").getAsBoolean());
        if (treeJson.has("showFinalBranchLengthsWithLeafNames"))
            this.setLabelingFinalBranchLengthsWithLeafNames(treeJson.get("showFinalBranchLengthsWithLeafNames").getAsBoolean());
        if (treeJson.has("showAncestorLabels"))
            this.setLabelingAncestorNames(treeJson.get("showAncestorLabels").getAsBoolean());
        if (treeJson.has("showAnnotationPlaceholders"))
            this.setShowingAnnotationPlaceholders(treeJson.get("showAnnotationPlaceholders").getAsBoolean());
        if (treeJson.has("showImageAtLeaf"))
            this.setLeafImagesVisible(treeJson.get("showImageAtLeaf").getAsBoolean());
        if (treeJson.has("showHyperlinks"))
            this.setShowingHyperlinks(treeJson.get("showHyperlinks").getAsBoolean());
        if (treeJson.has("colorByAttributeMaxColor"))
            this.setColorByAttributeMaxColor(PhyloPenIO.getColor(treeJson, "colorByAttributeMaxColor"));
        if (treeJson.has("colorByAttributeMinColor"))
            this.setColorByAttributeMaxColor(PhyloPenIO.getColor(treeJson, "colorByAttributeMinColor"));
        if (treeJson.has("colorByAttributeUndefinedColor"))
            this.setColorByAttributeMaxColor(PhyloPenIO.getColor(treeJson, "colorByAttributeUndefinedColor"));
        if (treeJson.has("colorByAttribute"))
            this.setColorByAttribute(treeJson.get("colorByAttribute").getAsString());
        
        // annotations
        if (treeJson.has("annotations"))
        {
            JsonArray annotationJsonArray = treeJson.get("annotations").getAsJsonArray();

            for (JsonElement annotationJson : annotationJsonArray)
                getAnnotations().add(Annotation.fromJson(annotationJson.getAsJsonObject(), this));
        }
        
        // recorded history
        //JsonElement history = treeJson.getAsJsonObject().get("event_history");
        
        updateLayout();
        
        /*if (history != null)
            eventRecorder.loadFromJson(history);*/
        
        if (treeJson.has("nextAnnotationId"))
            Annotation.setNextId(treeJson.get("nextAnnotationId").getAsInt());
        if (treeJson.has("nextCladeId"))
            Clade.setNextId(treeJson.get("nextCladeId").getAsInt());
        if (treeJson.has("nextEventId"))
            PhyloPenIO.setNextEventId(treeJson.get("nextEventId").getAsInt());
        
        initializingTree = false;
        
        if (eventTranscribing)
        {
            eventWriter = new EventFileWriter(treeFileName);
            eventWriter.start(user);
        }
    }
    
    public JsonObject toJson()
    {
        RemScaler rem = new RemScaler();
        JsonObject treeJson = getTreeModel().toJson();
        
        // add x and y coordinates and overlay color for nodes
        addExtraInfoToJsonClade(treeJson, getTreeModel().getRoot());
        
        treeJson.addProperty("treeWidth", rem.unscale(this.getTreeWidth()));
        treeJson.addProperty("treeHeight", rem.unscale(this.getTreeHeight()));
        treeJson.addProperty("branchesScaledByLength", this.isScalingBranchesByLength());
        treeJson.addProperty("showSpeciesNames", this.isLabelingLeafNodeNames());
        treeJson.addProperty("showIntermediateBranchLengths", this.isLabelingIntermediateBranchLengths());
        treeJson.addProperty("showFinalBranchLengthsOnBranches", this.isLabelingFinalBranchLengths());
        treeJson.addProperty("showFinalBranchLengthsWithLeafNames", this.isLabelingFinalBranchLengthsWithLeafNames());
        treeJson.addProperty("showAncestorLabels", this.isLabelingAncestorNames());
        treeJson.addProperty("showAnnotationPlaceholders", this.isShowingAnnotationPlaceholders());
        treeJson.addProperty("showImageAtLeaf", this.isLeafImagesVisible());
        treeJson.addProperty("showHyperlinks", this.isShowingHyperlinks());
        
        if (this.getColorByAttribute() != null)
        {
            treeJson.addProperty("colorByAttribute", this.getColorByAttribute());
            PhyloPenIO.attachColorProperty(treeJson, this.getColorByAttributeMaxColor(), "colorByAttributeMaxColor");
            PhyloPenIO.attachColorProperty(treeJson, this.getColorByAttributeMinColor(), "colorByAttributeMinColor");
            PhyloPenIO.attachColorProperty(treeJson, this.getColorByAttributeUndefinedColor(), "colorByAttributeUndefinedColor");
        }
        
        if (!getAnnotations().isEmpty())
        {
            JsonArray annotationJsonArray = new JsonArray();
            
            for (Annotation annotation : getAnnotations())
                annotationJsonArray.add(annotation.toJson());
            
            treeJson.add("annotations", annotationJsonArray);
        }
        
        /*if (eventRecorder.getEventBlock() != null && !eventRecorder.getEventBlock().isEmpty())
            treeJson.add("event_history", eventRecorder.toJson());*/
        
        treeJson.addProperty("nextCladeId", Clade.getNextId());
        treeJson.addProperty("nextAnnotationId", Annotation.getNextId());
        treeJson.addProperty("nextEventId", PhyloPenIO.getNextEventId());
        
        return treeJson;
    }
    
    private void addExtraInfoToJsonClade(JsonObject cladeJson, Clade cladeObject)
    {
        if (cladeObject.getChildCount() > 0)
        {
            Iterator<Clade> childIterator = cladeObject.iterator();
            Iterator<JsonElement> childJsonIterator = cladeJson.get("children").getAsJsonArray().iterator();
            Clade child;
            JsonObject childJson;
            
            while (childIterator.hasNext())
            {
                child = childIterator.next();
                childJson = childJsonIterator.next().getAsJsonObject();
                addExtraInfoToJsonClade(childJson, child);
            }
        }
        
        Circle nodeMarker = (Circle) cladeMetadataCache.get(cladeObject).nodeMarker;
        
        cladeJson.addProperty("x", nodeMarker.getCenterX());
        cladeJson.addProperty("y", nodeMarker.getCenterY());
        PhyloPenIO.attachColorProperty(cladeJson, (Color) nodeMarker.getFill(), "overlayFillColor");
        PhyloPenIO.attachColorProperty(cladeJson, (Color) nodeMarker.getStroke(), "overlayStrokeColor");
    }
    
    private class InkStrokeGhostingHandler implements EventHandler<ActionEvent>
    {
        private final Timeline timeline;
        private final InkStroke stroke;
        private long lastUpdateTime;
        
        public InkStrokeGhostingHandler(InkStroke stroke, Timeline timeline)
        {
            this.stroke = stroke;
            this.timeline = timeline;
            this.lastUpdateTime = System.currentTimeMillis();
        }
        
        @Override
        public void handle(ActionEvent event)
        {
            InkStrokeDrawingAttributes drawingAttributes = stroke.getDrawingAttributes();
            Color oldColor = drawingAttributes.getColor();
            double alpha = oldColor.getOpacity();
            double rate = 0.8; // per second
            long currentMillisFromEpoch = System.currentTimeMillis();
            double newAlpha = Math.max(alpha - (rate * (currentMillisFromEpoch - lastUpdateTime) / 1000.0), 0.0);
            stroke.setDrawingAttributes(new InkStrokeDrawingAttributes(new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), newAlpha), drawingAttributes.getStrokeWidth(), drawingAttributes.getStylusTip(), drawingAttributes.isHighlighter()));
            
            lastUpdateTime = currentMillisFromEpoch;
            
            if (newAlpha == 0.0)
            {
                PhyloPenCanvas.this.getChildren().remove(stroke);
                timeline.stop();
            }
        }
    }
    
    public double getSymbolRecognitionConfidenceThreshold()
    {
        return symbolRecognitionConfidenceThreshold;
    }
    
    // value between 0 and 1
    public void setSymbolRecognitionConfidenceThreshold(double value)
    {
        symbolRecognitionConfidenceThreshold = value;
    }
    
    public ObservableSet<Clade> getCollapsedClades()
    {
        return collapsedClades;
    }
    
    public ObjectProperty<Color> colorByAttributeMinColorProperty()
    {
        return colorByAttributeMinColorProperty;
    }
    
    public void setColorByAttributeMinColor(Color color)
    {
        colorByAttributeMinColorProperty.set(color);
    }
    
    public Color getColorByAttributeMinColor()
    {
        return colorByAttributeMinColorProperty.get();
    }
    
    public ObjectProperty<Color> colorByAttributeMaxColorProperty()
    {
        return colorByAttributeMaxColorProperty;
    }
    
    public void setColorByAttributeMaxColor(Color color)
    {
        colorByAttributeMaxColorProperty.set(color);
    }
    
    public Color getColorByAttributeMaxColor()
    {
        return colorByAttributeMaxColorProperty.get();
    }
    
    public ObjectProperty<Color> colorByAttributeUndefinedColorProperty()
    {
        return colorByAttributeUndefinedColorProperty;
    }
    
    public void setColorByAttributeUndefinedColor(Color color)
    {
        colorByAttributeUndefinedColorProperty.set(color);
    }
    
    public Color getColorByAttributeUndefinedColor()
    {
        return colorByAttributeUndefinedColorProperty.get();
    }
    
    public ObservableList<Clade> getSelectedNodes()
    {
        return selectedNodes;
    }
    
    public Color getDefaultBranchColor()
    {
        return defaultBranchColor;
    }
    
    public Color getDefaultOuterNodeFill()
    {
        return defaultOuterNodeFill;
    }
    
    public Color getDefaultOuterNodeStroke()
    {
        return defaultOuterNodeStroke;
    }
    
    public Color getDefaultInnerNodeFill()
    {
        return defaultInnerNodeFill;
    }
    
    public Color getDefaultInnerNodeStroke()
    {
        return defaultInnerNodeStroke;
    }
    
    private void processTextAnnotationGesture(List<AugmentedInkStroke> inkStrokes)
    {
        getInkStrokes().removeAll(inkStrokes);
        
        List<Clade> annotatedNodes = new ArrayList<>();
        Point2D testPoint;
        List<StylusPoint> points = inkStrokes.get(0).getResampledPoints();
        Circle vertexCircle;
        BoundingBox bounds = inkStrokes.get(0).getBoundingBox();

        for (Node nodeMarker : PhyloPenCanvas.this.getNodeMarkers())
        {
            if (nodeMarker instanceof Circle && nodeMarker.isVisible())
            {
                vertexCircle = (Circle) nodeMarker;
                testPoint = new Point2D(innerToOuterX(vertexCircle.getCenterX()), innerToOuterY(vertexCircle.getCenterY()));
                if (InkUtility.isPointInPolygon((List)points, testPoint, bounds))
                    annotatedNodes.add(getClade(vertexCircle));
            }
        }
        
        annotateNodes(annotatedNodes);
    }
    
    public boolean annotateNodes(List<Clade> annotatedNodes)
    {
        Annotation temp = new Annotation(annotatedNodes, (String) null, -1);
        getAnnotations().add(temp);
        
        AnnotationEditDialog dialog = new AnnotationEditDialog(temp, true);
        
        // Traditional way to get the response value.
        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get() != null && result.get())
        {
            Annotation finalAnnotation = new Annotation(annotatedNodes, temp.getText());
            finalAnnotation.setColor(temp.getColor());
            getAnnotations().add(finalAnnotation);
            
            JsonObject event = PhyloPenIO.createEventRecord(ANNOTATION);
            event.add("annotationJson", finalAnnotation.toJson());
            
            addUndoableEvent(event);
        }
        
        getAnnotations().remove(temp);
        return result.isPresent() && result.get() != null && result.get();
    }
    
    public void annotateSelectedNodes()
    {
        if (annotateNodes(getSelectedNodes()))
            clearSelection();
    }
    
    public JsonObject editAnnotation(Annotation annotation)
    {
        AnnotationEditDialog dialog = new AnnotationEditDialog(annotation, false);
        dialog.showAndWait();
        
        return editAnnotation(annotation, dialog.getAnnotationTextField().getText(), dialog.getColorPicker().getValue());
    }
    
    public JsonObject editAnnotation(Annotation annotation, String text, Color color)
    {
        JsonObject event = null;
        
        if (!annotation.getText().equals(text) || !annotation.getColor().equals(color))
        {
            event = PhyloPenIO.createEventRecord(ANNOTATION_EDIT);
            event.addProperty("annotationId", annotation.getId());
            event.addProperty("oldText", annotation.getText());
            event.addProperty("newText", text);
            PhyloPenIO.attachColorProperty(event, annotation.getColor(), "oldColor");
            PhyloPenIO.attachColorProperty(event, color, "newColor");
            
            annotation.setText(text);
            annotation.setColor(color);
            
            addUndoableEvent(event);
        }
        
        return event;
    }
    
    public JsonObject removeAnnotation(Annotation annotation)
    {
        JsonObject event = null;
        
        if (getAnnotations().remove(annotation))
        {
            event = PhyloPenIO.createEventRecord(ANNOTATION_REMOVED);
            event.add("annotationJson", annotation.toJson());

            addUndoableEvent(event);
        }
        
        return event;
    }
    
    private void processLassoGesture(List<AugmentedInkStroke> inkStrokes)
    {
        getInkStrokes().removeAll(inkStrokes);
        
        if (!selectedNodes.isEmpty())
        {
            clearSelection();
        }
        
        List<Clade> newSelectedNodes = new ArrayList<>();
        Point2D testPoint;
        List<StylusPoint> points = inkStrokes.get(0).getResampledPoints();
        Circle vertexCircle;
        BoundingBox bounds = inkStrokes.get(0).getBoundingBox();
        
        for (Node nodeMarker : PhyloPenCanvas.this.getNodeMarkers())
        {
            if (nodeMarker instanceof Circle && nodeMarker.isVisible())
            {
                vertexCircle = (Circle) nodeMarker;
                testPoint = new Point2D(innerToOuterX(vertexCircle.getCenterX()), innerToOuterY(vertexCircle.getCenterY()));
                if (InkUtility.isPointInPolygon((List)points, testPoint, bounds))
                    newSelectedNodes.add(getClade(vertexCircle));
            }
        }
        
        selectNodes(newSelectedNodes);
    }
    
    // cut without re-attach
    private void processVerticalLineGesture(List<AugmentedInkStroke> inkStrokes)
    {
        //getInkStrokes().removeAll(inkStrokes);
        
        AugmentedInkStroke inkStroke1 = inkStrokes.get(0);
        
        double x1 = inkStroke1.getPoints().get(0);
        double y1 = inkStroke1.getPoints().get(1);
        double x2 = inkStroke1.getPoints().get(inkStroke1.getPoints().size() - 2);
        double y2 = inkStroke1.getPoints().get(inkStroke1.getPoints().size() - 1);
        
        Clade cutClade = null, parentOfCutClade = null, reattachmentClade = null;
        int childIndex;
        
        for (Map.Entry<Clade, CladeMetadata> entry : cladeMetadataCache.entrySet())
        {
            childIndex = 0;
            
            for (Line branchLine : entry.getValue().branchHorizontalConnectors)
            {
                branchLine = new Line(innerToOuterX(branchLine.getStartX()), innerToOuterY(branchLine.getStartY()), innerToOuterX(branchLine.getEndX()), innerToOuterY(branchLine.getEndY()));
                
                if (IntersectionUtility.isLineSegmentLineSegmentIntersection(x1, y1, x2, y2, branchLine.getStartX(), branchLine.getStartY(), branchLine.getEndX(), branchLine.getEndY()))
                {
                    parentOfCutClade = entry.getKey();
                    Iterator<Clade> childIterator = parentOfCutClade.iterator();
                    
                    for (int i = 0; i < childIndex; i++)
                        childIterator.next();
                    
                    cutClade = childIterator.next();
                    //branchLine.setStroke(Color.RED);
                    //getChildren().add(branchLine);
                    break;
                }
                
                childIndex++;
            }
        }
        
        /*x1 = inkStroke2.getPoints().get(0);
        y1 = inkStroke2.getPoints().get(1);
        x2 = inkStroke2.getPoints().get(inkStroke2.getPoints().size() - 2);
        y2 = inkStroke2.getPoints().get(inkStroke2.getPoints().size() - 1);*/
        
        if (cutClade == null)
            System.out.println("Couldn't find clade to cut.");
        else
        {
            this.cutClade(cutClade);
        }
    }
    
    private void processHorizontalLineGesture(List<AugmentedInkStroke> inkStrokes)
    {
        getInkStrokes().removeAll(inkStrokes);
        
        AugmentedInkStroke inkStroke = inkStrokes.get(0);
        
        Clade targetClade = null;
        
        double x1 = inkStroke.getPoints().get(0);
        double y1 = inkStroke.getPoints().get(1);
        
        Node closestMarker = getClosestMarkerToInkPoint(x1, y1);
        
        if (closestMarker != null)
        {
            targetClade = getClade(closestMarker);
        }
        else
        {
            System.out.println("No intersecting node marker found for line.");
        }
        
        if (targetClade != null)
        {
            double xn = inkStroke.getPoints().get(inkStroke.getPoints().size() - 2);
            
            if (xn < x1) // if the line is going to the left
                this.insertNewClade(targetClade.getParent(), targetClade);
            else // if the line is going to the right
                addClade(null, targetClade, Math.max(0, targetClade.getChildCount() - 1));
        }
    }
    
    private void processLGesture(List<AugmentedInkStroke> inkStrokes)
    {
        getInkStrokes().removeAll(inkStrokes);
        
        AugmentedInkStroke inkStroke = inkStrokes.get(0);
        
        Clade targetClade = null;
        
        double x1 = inkStroke.getPoints().get(0);
        double y1 = inkStroke.getPoints().get(1);
        
        Node closestMarker = getClosestMarkerToInkPoint(x1, y1);
        
        if (closestMarker != null)
        {
            targetClade = getClade(closestMarker);
        }
        else
        {
            System.out.println("No intersecting node marker found for line.");
        }
        
        if (targetClade != null)
            addClade(null, targetClade, Math.max(0, targetClade.getChildCount() - 1));
    }
    
    private void processReverseLGesture(List<AugmentedInkStroke> inkStrokes)
    {
        processLGesture(inkStrokes);
    }
    
    // delete clade
    private void processXGesture(List<AugmentedInkStroke> inkStrokes)
    {
        getInkStrokes().removeAll(inkStrokes);
        
        Clade cutClade = null;
        
        if (this.cutClade == null)
        {
            AugmentedInkStroke inkStroke1 = inkStrokes.get(0);

            double x1 = inkStroke1.getPoints().get(0);
            double y1 = inkStroke1.getPoints().get(1);
            double x2 = inkStroke1.getPoints().get(inkStroke1.getPoints().size() - 2);
            double y2 = inkStroke1.getPoints().get(inkStroke1.getPoints().size() - 1);

            Clade parentOfCutClade;
            int childIndex;

            for (Map.Entry<Clade, CladeMetadata> entry : cladeMetadataCache.entrySet())
            {
                childIndex = 0;

                for (Line branchLine : entry.getValue().branchHorizontalConnectors)
                {
                    branchLine = new Line(innerToOuterX(branchLine.getStartX()), innerToOuterY(branchLine.getStartY()), innerToOuterX(branchLine.getEndX()), innerToOuterY(branchLine.getEndY()));

                    if (IntersectionUtility.isLineSegmentLineSegmentIntersection(x1, y1, x2, y2, branchLine.getStartX(), branchLine.getStartY(), branchLine.getEndX(), branchLine.getEndY()))
                    {
                        parentOfCutClade = entry.getKey();
                        Iterator<Clade> childIterator = parentOfCutClade.iterator();

                        for (int i = 0; i < childIndex; i++)
                            childIterator.next();
                        
                        cutClade = childIterator.next();
                        //branchLine.setStroke(Color.RED);
                        //getChildren().add(branchLine);
                        break;
                    }

                    childIndex++;
                }
            }
        }
        else
        {
            cutClade = this.cutClade;
            undoCutClade();
        }
        
        if (cutClade == null)
            System.out.println("Couldn't find clade to remove.");
        else
            removeClade(cutClade);
    }
    
    private void processCutAndReattachGesture(List<AugmentedInkStroke> inkStrokes)
    {
        getInkStrokes().removeAll(inkStrokes);
        
        Clade cutClade = null;
        double x1, y1;
        
        if (this.cutClade == null)
        {
            AugmentedInkStroke inkStroke1 = inkStrokes.get(0);

            x1 = inkStroke1.getPoints().get(0);
            y1 = inkStroke1.getPoints().get(1);
            double x2 = inkStroke1.getPoints().get(inkStroke1.getPoints().size() - 2);
            double y2 = inkStroke1.getPoints().get(inkStroke1.getPoints().size() - 1);

            Clade parentOfCutClade;
            int childIndex;

            for (Map.Entry<Clade, CladeMetadata> entry : cladeMetadataCache.entrySet())
            {
                childIndex = 0;

                for (Line branchLine : entry.getValue().branchHorizontalConnectors)
                {
                    branchLine = new Line(innerToOuterX(branchLine.getStartX()), innerToOuterY(branchLine.getStartY()), innerToOuterX(branchLine.getEndX()), innerToOuterY(branchLine.getEndY()));

                    if (IntersectionUtility.isLineSegmentLineSegmentIntersection(x1, y1, x2, y2, branchLine.getStartX(), branchLine.getStartY(), branchLine.getEndX(), branchLine.getEndY()))
                    {
                        parentOfCutClade = entry.getKey();
                        Iterator<Clade> childIterator = parentOfCutClade.iterator();

                        for (int i = 0; i < childIndex; i++)
                            childIterator.next();

                        cutClade = childIterator.next();
                        //branchLine.setStroke(Color.RED);
                        //getChildren().add(branchLine);
                        break;
                    }

                    childIndex++;
                }
            }
        }
        else
        {
            cutClade = this.cutClade;
            undoCutClade();
        }

        AugmentedInkStroke inkStroke2 = inkStrokes.get(1);
        
        Clade reattachmentClade = null;
        
        x1 = inkStroke2.getPoints().get(0);
        y1 = inkStroke2.getPoints().get(1);
        
        Node closestMarker = getClosestMarkerToInkPoint(x1, y1);
        
        if (closestMarker != null)
        {
            reattachmentClade = getClade(closestMarker);
        }
        else
        {
            System.out.println("No intersecting node marker found for reattach line.");
        }
        
        if (reattachmentClade != null)
            cutAndReattachClade(cutClade, reattachmentClade);
    }
    
    private void processTapGesture(List<AugmentedInkStroke> inkStrokes)
    {
        clearSelection();
        getInkStrokes().removeAll(inkStrokes);
        if (this.cutClade != null)
            undoCutClade();
    }
    
    public BooleanProperty preservingTreeDimensionsOnCladeDeletionProperty()
    {
        return preservingTreeDimensionsOnCladeDeletionProperty;
    }
    
    public void setPreservingTreeDimensionsOnCladeDeletion(boolean value)
    {
        preservingTreeDimensionsOnCladeDeletionProperty.set(value);
    }
    
    public boolean isPreservingTreeDimensionsOnCladeDeletion()
    {
        return preservingTreeDimensionsOnCladeDeletionProperty.get();
    }
    
    public JsonObject addClade(JsonObject cladeJson, Clade parent, int childIndex)
    {
        Clade newClade = cladeJson != null ? new Clade(cladeJson) : new Clade();
        parent.addChild(childIndex, newClade);
        getTreeModel().updateAttributeList();
        markDirty(getTreeModel().getRoot());
        
        this.updateLayout();
        
        if (parent.getChildCount() == 1)
        {
            CladeMetadata parentMetadata = cladeMetadataCache.get(parent);

            if (parentMetadata != null)
            {
                if (parentMetadata.image != null)
                {
                    getCladeImages().remove(parentMetadata.image);
                    parentMetadata.image = null;
                }

                if ((parent.getFillColor() == null || parent.getFillColor() == this.getDefaultOuterNodeFill()) && (parent.getStrokeColor() == null || parent.getStrokeColor() == this.getDefaultOuterNodeStroke()))
                {
                    Collection<Clade> singleParent = new LinkedList<>();
                    singleParent.add(parent);
                    colorFillOfNodes(singleParent, getDefaultInnerNodeFill(), null);
                    colorOutlineOfNodes(singleParent, getDefaultInnerNodeStroke(), null);
                }
            }
        }
        
        if (cladeJson == null)
        {
            CladeEditDialog dialog = new CladeEditDialog(newClade, true);

            Optional<Boolean> result = dialog.showAndWait();
            if (result.isPresent() && result.get() != null && result.get())
            {
                System.out.println("Successfully added clade labeled: " + newClade.getLabel());
            }
            
            /*CladeMetadata metadata = cladeMetadataCache.get(newClade);

            if (metadata != null)
            {
                if (metadata.nodeLabel != null)
                {
                    //metadata.nodeLabel.setText(newClade.getLabel());
                    updateLayout();
                }
            }*/
        }
        
        return addUndoableCladeAdditionEvent(newClade, cladeJson, parent, childIndex, System.currentTimeMillis());
    }
    
    public JsonObject insertNewClade(Clade parent, Clade child)
    {
        Clade newClade = new Clade();
        if (parent == null && child == getTreeModel().getRoot())
            getTreeModel().setRoot(newClade);
        else
        {
            
            System.out.println(parent);
            System.out.println(child);
            System.out.println(newClade.getId());
            if (parent != null)
                parent.addChild(newClade);
            if (child != null)
            {
                parent.removeChild(child);
                newClade.addChild(child);
            }
        }
        
        getTreeModel().updateAttributeList();
        markDirty(getTreeModel().getRoot());
        
        this.updateLayout();
        
        CladeEditDialog dialog = new CladeEditDialog(newClade, true);
        
        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get() != null && result.get())
        {
            System.out.println("Successfully inserted clade labeled: " + newClade.getLabel());
        }
        
        return addUndoableCladeInsertionEvent(newClade, parent, child, System.currentTimeMillis());
    }
    
    private void undoCladeInsertion(Clade insertedClade, Clade parent, Clade child)
    {
        if (insertedClade == getTreeModel().getRoot())
        {
            if (child != null)
            {
                insertedClade.removeChild(child);
                getTreeModel().setRoot(child);
            }
        }
        else
        {
            insertedClade.removeChild(child);
            parent.removeChild(insertedClade);
            parent.addChild(child);
        }
        
        getTreeModel().updateAttributeList();
        markDirty(getTreeModel().getRoot());
        
        this.updateLayout();
    }
    
    private JsonObject addUndoableCladeAdditionEvent(Clade newClade, JsonObject cladeJson, Clade parent, int childIndex, long timestamp)
    {
        JsonObject event = PhyloPenIO.createEventRecord(CLADE_ADD, timestamp);
        event.addProperty("nodeId", newClade.getId());
        if (cladeJson != null)
            event.add("cladeJson", cladeJson);
        event.addProperty("parentId", parent.getId());
        event.addProperty("childIndex", childIndex);
        
        addUndoableEvent(event);
        
        return event;
    }
    
    private JsonObject addUndoableCladeInsertionEvent(Clade newClade, Clade parent, Clade child, long timestamp)
    {
        JsonObject event = PhyloPenIO.createEventRecord(CLADE_INSERT, timestamp);
        event.addProperty("nodeId", newClade.getId());
        if (parent != null)
            event.addProperty("parentId", parent.getId());
        if (child != null)
            event.addProperty("childId", child.getId());
        
        addUndoableEvent(event);
        
        return event;
    }
    
    public void removeSelectedClade()
    {
        if (!this.getSelectedNodes().isEmpty())
        {
            removeClade(getSelectedNodes().get(0));
        }
    }
    
    public JsonObject removeClade(Clade cutClade)
    {
        JsonObject event = null;
        
        if (cutClade != getTreeModel().getRoot())
        {
            double width = getTreeWidth();
            double height = getTreeHeight();
            int childIndex = -1;
            JsonObject cladeJson = cutClade.toJson();
            Clade parentOfCutClade = cutClade.getParent();

            if (parentOfCutClade != null)
            {
                childIndex = cutClade.getParent().getChildIndex(cutClade);
                cutClade.getParent().removeChild(cutClade);
                //markDirty(cutClade.getParent());
                CladeMetadata parentMetadata = cladeMetadataCache.get(parentOfCutClade);
                getBranchLines().remove(parentMetadata.branchHorizontalConnectors.remove(childIndex));
                getLabels().remove(parentMetadata.branchLabels.remove(childIndex));
            }
            
            List<Clade> nodesToDeselect = new ArrayList<>();
            List<JsonObject> annotationsRemoved = new ArrayList<>();

            removeSubClade(cutClade, nodesToDeselect, annotationsRemoved);
            
            if (!nodesToDeselect.isEmpty())
                deselectNodes(nodesToDeselect);
            
            markDirty(getTreeModel().getRoot());

            getTreeModel().updateAttributeList();
            this.updateLayout();

            event = addUndoableCladeRemovalEvent(cutClade, cladeJson, parentOfCutClade, childIndex, System.currentTimeMillis(), annotationsRemoved);
            
            if (isPreservingTreeDimensionsOnCladeDeletion())
                this.setTreeDimensions(width, height);
            
            if (parentOfCutClade.getChildCount() == 0 && (parentOfCutClade.getFillColor() == null || parentOfCutClade.getFillColor() == this.getDefaultInnerNodeFill()) && (parentOfCutClade.getStrokeColor() == null || parentOfCutClade.getStrokeColor() == this.getDefaultInnerNodeStroke()))
            {
                Collection<Clade> singleParent = new LinkedList<>();
                singleParent.add(parentOfCutClade);
                colorFillOfNodes(singleParent, getDefaultOuterNodeFill(), null);
                colorOutlineOfNodes(singleParent, getDefaultOuterNodeStroke(), null);
            }
        }
        
        return event;
    }
    
    private JsonObject addUndoableCladeRemovalEvent(Clade cutClade, JsonObject cladeJson, Clade oldParent, int oldChildIndex, long timestamp, List<JsonObject> annotationsRemoved)
    {
        JsonObject event = PhyloPenIO.createEventRecord(CLADE_CUT, timestamp);
        event.addProperty("nodeId", cutClade.getId());
        event.add("cladeJson", cladeJson);
        event.addProperty("oldParentId", oldParent.getId());
        event.addProperty("oldChildIndex", oldChildIndex);
        
        if (annotationsRemoved != null && annotationsRemoved.size() > 0)
        {
            JsonArray jsonArray = new JsonArray();
            for (JsonObject a : annotationsRemoved)
                jsonArray.add(a);
            event.add("annotationsRemoved", jsonArray);
        }
        
        addUndoableEvent(event);
        
        return event;
    }
    
    private void removeSubClade(Clade subClade, List<Clade> nodesToDeselect, List<JsonObject> annotationsRemoved)
    {
        // if it is in an annotation, get rid of the annotation
        Iterator<Annotation> annotationIterator = getAnnotations().iterator();
        Annotation annotation;
        
        while (annotationIterator.hasNext())
        {
            annotation = annotationIterator.next();
            
            if (annotation.getNodes().contains(subClade))
            {
                annotationsRemoved.add(annotation.toJson());
                annotationIterator.remove();
            }
        }
        
        // if it is in a selection, remove it from the selection
        nodesToDeselect.add(subClade);
        
        CladeMetadata metadata = cladeMetadataCache.remove(subClade);
        
        getNodeMarkers().remove(metadata.nodeMarker);
        
        if (subClade.getChildCount() > 0)
        {
            getBranchLines().removeAll(metadata.branchHorizontalConnectors);
            getBranchLines().remove(metadata.branchVerticalConnector);
            getLabels().removeAll(metadata.branchLabels);
            
            for (Clade child : subClade)
                removeSubClade(child, nodesToDeselect, annotationsRemoved);
        }
        else
        {
            getLabels().remove(metadata.nodeLabel);
            
            CladeImageViewer cladeImageViewer;
            Iterator<CladeImageViewer> cladeImageIterator = getCladeImages().iterator();
            
            while (cladeImageIterator.hasNext())
            {
                cladeImageViewer = cladeImageIterator.next();
                
                if (cladeImageViewer.getClade() == subClade)
                {
                    cladeImageIterator.remove();
                    break;
                }
            }
        }
        
        getNodeMarkers().remove(metadata.nodeMarker);
        
        if (metadata.nodeLabel != null)
            getLabels().remove(metadata.nodeLabel);
    }
    
    public JsonObject cutAndReattachClade(Clade cutClade, Clade reattachmentClade)
    {
        return cutAndReattachClade(cutClade, reattachmentClade, reattachmentClade.getChildCount());
    }
    
    public JsonObject cutAndReattachClade(Clade cutClade, Clade reattachmentClade, int newChildIndex)
    {
        JsonObject event = null;
        
        if (cutClade != null && reattachmentClade != null)
        {
            if (cutClade != reattachmentClade && !cutClade.isDescendent(reattachmentClade))
            {
                List<JsonObject> annotationsRemoved = removeAnnotations(cutClade);
                
                Clade parentOfCutClade = cutClade.getParent();
                int oldChildIndex = parentOfCutClade.getChildIndex(cutClade);
                parentOfCutClade.removeChild(cutClade);
                reattachmentClade.addChild(newChildIndex, cutClade);
                markDirty(getTreeModel().getRoot());
                //markDirty(parentOfCutClade);
                //markDirty(reattachmentClade);
                
                if (parentOfCutClade.getChildCount() == 0)
                {
                    if ((parentOfCutClade.getFillColor() == null || parentOfCutClade.getFillColor() == this.getDefaultInnerNodeFill()) && (parentOfCutClade.getStrokeColor() == null || parentOfCutClade.getStrokeColor() == this.getDefaultInnerNodeStroke()))
                    {
                        Collection<Clade> singleParent = new LinkedList<>();
                        singleParent.add(parentOfCutClade);
                        colorFillOfNodes(singleParent, getDefaultOuterNodeFill(), null);
                        colorOutlineOfNodes(singleParent, getDefaultOuterNodeStroke(), null);
                    }
                    
                    CladeMetadata parentMetadata = cladeMetadataCache.get(parentOfCutClade);

                    if (parentMetadata != null)
                    {
                        if (!parentMetadata.branchHorizontalConnectors.isEmpty())
                        {
                            this.getBranchLines().removeAll(parentMetadata.branchHorizontalConnectors);
                            parentMetadata.branchHorizontalConnectors.clear();
                        }
                        /*if (parentMetadata.image != null)
                        {
                            getCladeImages().remove(parentMetadata.image);
                            parentMetadata.image = null;
                        }*/
                    }
                }
                
                if (reattachmentClade.getChildCount() == 1)
                {
                    CladeMetadata parentMetadata = cladeMetadataCache.get(reattachmentClade);

                    if (parentMetadata != null)
                    {
                        if (parentMetadata.image != null)
                        {
                            getCladeImages().remove(parentMetadata.image);
                            parentMetadata.image = null;
                        }

                        if ((reattachmentClade.getFillColor() == null || reattachmentClade.getFillColor() == this.getDefaultOuterNodeFill()) && (reattachmentClade.getStrokeColor() == null || reattachmentClade.getStrokeColor() == this.getDefaultOuterNodeStroke()))
                        {
                            Collection<Clade> singleParent = new LinkedList<>();
                            singleParent.add(reattachmentClade);
                            colorFillOfNodes(singleParent, getDefaultInnerNodeFill(), null);
                            colorOutlineOfNodes(singleParent, getDefaultInnerNodeStroke(), null);
                        }
                    }
                }
                
                event = addUndoableCladeCutAndReattachEvent(cutClade, reattachmentClade, parentOfCutClade, oldChildIndex, newChildIndex, System.currentTimeMillis(), annotationsRemoved);
                
                this.updateLayout();
            }
        }
        
        return event;
    }
    
    private JsonObject addUndoableCladeCutAndReattachEvent(Clade cutClade, Clade newParent, Clade oldParent, int oldChildIndex, int newChildIndex, long timestamp, List<JsonObject> annotationsRemoved)
    {
        JsonObject event = PhyloPenIO.createEventRecord(CLADE_CUT_REATTACH, timestamp);
        event.addProperty("nodeId", cutClade.getId());
        event.addProperty("newParentId", newParent.getId());
        event.addProperty("oldParentId", oldParent.getId());
        event.addProperty("oldChildIndex", oldChildIndex);
        event.addProperty("newChildIndex", newChildIndex);
        
        if (annotationsRemoved != null && annotationsRemoved.size() > 0)
        {
            JsonArray jsonArray = new JsonArray();
            for (JsonObject a : annotationsRemoved)
                jsonArray.add(a);
            event.add("annotationsRemoved", jsonArray);
        }
        
        addUndoableEvent(event);
        
        return event;
    }
    
    private List<JsonObject> removeAnnotations(Clade clade)
    {
        List<JsonObject> annotationsRemoved = new LinkedList<>();
        removeAnnotations(clade, annotationsRemoved);
        return annotationsRemoved;
    }
    
    private void removeAnnotations(Clade clade, List<JsonObject> annotationsRemoved)
    {
        Iterator<Annotation> annotationIterator = getAnnotations().iterator();
        Annotation annotation;

        while (annotationIterator.hasNext())
        {
            annotation = annotationIterator.next();

            if (annotation.getNodes().contains(clade))
            {
                annotationsRemoved.add(annotation.toJson());
                annotationIterator.remove();
            }
        }
        
        for (Clade child : clade)
            removeAnnotations(child, annotationsRemoved);
    }
    
    public void cutSelectedClade()
    {
        if (!getSelectedNodes().isEmpty())
        {
            cutClade(getSelectedNodes().get(0));
            this.clearSelection();
        }
    }
    
    public void cutClade(Clade clade)
    {
        this.cutClade = clade;
        this.updateLayout();
    }
    
    public void undoCutClade()
    {
        this.cutClade = null;
        this.updateLayout();
    }
    
    public boolean reattachCutCladeTo(Clade reattachmentClade)
    {
        JsonObject event = cutAndReattachClade(cutClade, reattachmentClade);
        
        if (event != null)
        {
            this.cutClade = null;
            this.updateLayout();
            return true;
        }
        
        return false;
    }
    
    public boolean reattachCutCladeToSelectedClade()
    {
        if (reattachCutCladeTo(getSelectedNodes().get(0)))
        {
            clearSelection();
            return true;
        }
        
        return false;
    }
    
    public Clade getCutClade()
    {
        return cutClade;
    }
    
    public void markDirty(Clade clade)
    {
        CladeMetadata metadata = cladeMetadataCache.get(clade);
        
        if (metadata != null)
            metadata.dirty = true;
    }
    
    private void processSquareBracketChildReverseGesture(Polyline squareBracket, List<AugmentedInkStroke> inkStrokes)
    {
        getInkStrokes().removeAll(inkStrokes);
        
        Line verticalLine;
        Line horizontalLine = new Line(
                squareBracket.getPoints().get(0),
                squareBracket.getPoints().get(1),
                squareBracket.getPoints().get(2),
                squareBracket.getPoints().get(3));
        
        //getChildren().add(horizontalLine);
        
        Clade targetClade = null;
        
        for (Map.Entry<Clade, CladeMetadata> entry : cladeMetadataCache.entrySet())
        {
            if (entry.getValue().branchVerticalConnector != null)
            {
                verticalLine = entry.getValue().branchVerticalConnector;
                verticalLine = new Line(innerToOuterX(verticalLine.getStartX()), innerToOuterY(verticalLine.getStartY()), innerToOuterX(verticalLine.getEndX()), innerToOuterY(verticalLine.getEndY()));
                
                if (IntersectionUtility.isHorizontalVerticalLineIntersection(horizontalLine, verticalLine))
                {
                    targetClade = entry.getKey();
                    //verticalLine.setStroke(Color.RED);
                    //getChildren().add(verticalLine);
                    break;
                }
            }
        }
        
        if (targetClade != null)
            reverseChildOrdering(targetClade);
    }
    
    public JsonObject reverseChildOrdering(Clade clade)
    {
        List<Clade> clades = new ArrayList<>(1);
        clades.add(clade);
        return reverseChildOrdering(clades);
    }
    
    public JsonObject reverseChildOrdering(List<Clade> clades)
    {
        for (Clade clade : clades)
            clade.reverseChildOrdering();
        
        this.updateLayout();
        this.updateLayout(); // temporary fix.
        
        JsonObject event = PhyloPenIO.createEventRecord(CLADE_ROTATE, System.currentTimeMillis());
        PhyloPenIO.attachNodeIdsProperty(event, clades);
        
        addUndoableEvent(event);
        
        return event;
    }
    
    public JsonObject reverseSelectedClades()
    {
        JsonObject event = reverseChildOrdering(getSelectedNodes());
        clearSelection();
        
        return event;
    }
    
    private Node getClosestMarkerToInkPoint(double x, double y)
    {
        Node closestMarker = null;
        double minDistance = Double.POSITIVE_INFINITY, distanceToMarkerCenter;
        double centerX, centerY;
        Circle nodeCircle;
        
        for (Node nodeMarker : this.getNodeMarkers())
        {
            if (nodeMarker.isVisible() && nodeMarker instanceof Circle)
            {
                nodeCircle = (Circle) nodeMarker;
                centerX = innerToOuterX(((Circle)nodeMarker).getCenterX());
                centerY = innerToOuterY(((Circle)nodeMarker).getCenterY());
                
                distanceToMarkerCenter = InkUtility.distance(x, y, centerX, centerY);
                if (distanceToMarkerCenter < minDistance)
                {
                    minDistance = distanceToMarkerCenter;
                    closestMarker = nodeCircle;
                }
            }
        }
        
        return closestMarker;
    }
    
    private Node getClosestIntersectingMarkerToPlusOrMinusSign(List<AugmentedInkStroke> inkStrokes)
    {
        double r, x1, x2, y1, y2;
        Bounds markerBounds, inkSymbolBounds = InkUtility.getBoundingBox((Collection)inkStrokes);
        double inkSymbolBoundsCenterX = inkSymbolBounds.getMinX() + (inkSymbolBounds.getMaxX() - inkSymbolBounds.getMinX()) * 0.5;
        double inkSymbolBoundsCenterY = inkSymbolBounds.getMinY() + (inkSymbolBounds.getMaxY() - inkSymbolBounds.getMinY()) * 0.5;
        Node closestIntersectingMarker = null;
        double minDistance = Double.POSITIVE_INFINITY;
        double centerX, centerY;
        
        for (Node nodeMarker : this.getNodeMarkers())
        {
            if (nodeMarker.isVisible() && nodeMarker instanceof Circle)
            {
                Circle nodeCircle = (Circle) nodeMarker;
                centerX = innerToOuterX(((Circle)nodeMarker).getCenterX());
                centerY = innerToOuterY(((Circle)nodeMarker).getCenterY());
                r = Math.max(new RemScaler().scale(nodeCircle.getRadius()) * 3.0, 10.0);
                markerBounds = new BoundingBox(centerX - r, centerY - r, r * 2.0, r * 2.0);
                
                // if bounds intersect, do line segment-circle intersection test
                if (inkSymbolBounds.intersects(markerBounds))
                {
                    // test each line for intersection with the extended circle range
                    for (AugmentedInkStroke inkStroke : inkStrokes)
                    {
                        x1 = inkStroke.getResampledPoints().get(0).getX();
                        y1 = inkStroke.getResampledPoints().get(0).getY();
                        x2 = inkStroke.getResampledPoints().get(inkStroke.getResampledPoints().size() - 1).getX();
                        y2 = inkStroke.getResampledPoints().get(inkStroke.getResampledPoints().size() - 1).getY();
                        
                        if (IntersectionUtility.isLineSegmentCircleIntersection(x1, y1, x2, y2, r, centerX, centerY))
                        {
                            // an intersection exists
                            double distanceToCircleCenter = InkUtility.distance(inkSymbolBoundsCenterX, inkSymbolBoundsCenterY, centerX, centerY);
                            if (distanceToCircleCenter < minDistance)
                            {
                                minDistance = distanceToCircleCenter;
                                closestIntersectingMarker = nodeCircle;
                            }
                        }
                    }
                }
            }
        }
        
        return closestIntersectingMarker;
    }
    
    private void processTriangleCollapseExpandGesture(Polygon triangle, List<AugmentedInkStroke> inkStrokes)
    {
        //getChildren().add(triangle);
        getInkStrokes().removeAll(inkStrokes);
        
        Iterator<Double> vertexIterator = triangle.getPoints().iterator();
        List<Point2D> points = new ArrayList<>(3);
        
        while (vertexIterator.hasNext())
            points.add(new Point2D(vertexIterator.next(), vertexIterator.next()));
        
        //boolean clockwiseOrder = InkUtility.isClockwiseOrder(points, true);
        Point2D triangleCentroid = InkUtility.computeCentroid(points);
        List<Node> nodeMarkers = getNodeMarkers();
        
        Node markerInTriangleClosestToCenter = null;
        double minDistanceToCenter = Double.POSITIVE_INFINITY, distanceToCenter;
        Bounds markerModelRenderBounds, markerOuterCoordBounds;
        double minX, maxX, minY, maxY;
        //Point2D min, max;
        
        for (Node nodeMarker : nodeMarkers)
        {
            if (nodeMarker.isVisible())
            {
                markerModelRenderBounds = nodeMarker.getBoundsInLocal();
                minX = innerToOuterX(markerModelRenderBounds.getMinX());
                minY = innerToOuterY(markerModelRenderBounds.getMinY());
                maxX = innerToOuterX(markerModelRenderBounds.getMaxX());
                maxY = innerToOuterY(markerModelRenderBounds.getMaxY());
                markerOuterCoordBounds = new BoundingBox(minX, minY, maxX - minX, maxY - minY);
                
                if (markerOuterCoordBounds.intersects(triangle.getBoundsInLocal()))
                {
                    boolean inTriangle = false;
                    double centerX = 0.0, centerY = 0.0;

                    if (nodeMarker instanceof Circle)
                    {
                        centerX = innerToOuterX(((Circle)nodeMarker).getCenterX());
                        centerY = innerToOuterY(((Circle)nodeMarker).getCenterY());

                        // circle-triangle intersection
                        // http://www.phatcode.net/articles.php?id=459

                        // test whether circle center is within triangle
                        // NOTE: this method seems flawed. Doesn't work 100% of the time.

                        /*double signedValue0 = ((points.get(1).getY() - points.get(0).getY())*(centerX - points.get(0).getX()) - (points.get(1).getX() - points.get(0).getX())*(centerY - points.get(0).getY()));
                        double signedValue1 = ((points.get(2).getY() - points.get(1).getY())*(centerX - points.get(1).getX()) - (points.get(2).getX() - points.get(1).getX())*(centerY - points.get(1).getY()));
                        double signedValue2 = ((points.get(0).getY() - points.get(2).getY())*(centerX - points.get(2).getX()) - (points.get(0).getX() - points.get(2).getX())*(centerX - points.get(2).getX()));

                        if (!clockwiseOrder)
                        {
                            signedValue0 = -signedValue0;
                            signedValue1 = -signedValue1;
                            signedValue2 = -signedValue2;
                        }
                        
                        System.out.println(signedValue0 + ", " + signedValue1 + ", " + signedValue2);

                        if (signedValue0 >= 0 && signedValue1 >= 0 && signedValue2 >= 0)
                        {
                            inTriangle = true;
                            //Circle tempCircle = new Circle(centerX, centerY, ((Circle)nodeMarker).getRadius());
                            //tempCircle.setStroke(Color.TRANSPARENT);
                            //tempCircle.setFill(Color.VIOLET);

                            //getChildren().add(tempCircle);
                        }*/
                        
                        // if center of circle is inside triangle
                        if (IntersectionUtility.isPointInsideTriangle(new Point2D(centerX, centerY), points.get(0), points.get(1), points.get(2)));
                            inTriangle = true;
                    }
                    /*else
                    {
                        // more shapes may be added in later
                        inTriangle = true;
                        centerX = (markerOuterCoordBounds.getMinX() + markerOuterCoordBounds.getMaxX()) / 2.0;
                        centerY = (markerOuterCoordBounds.getMinY() + markerOuterCoordBounds.getMaxY()) / 2.0;
                    }*/

                    if (inTriangle)
                    {
                        //System.out.println("IN TRIANGLE");
                        //System.out.println(((Circle)nodeMarker).getCenterX() + ", " + ((Circle)nodeMarker).getCenterY());

                        distanceToCenter = InkUtility.distance(triangleCentroid.getX(), triangleCentroid.getY(), centerX, centerY);

                        if (distanceToCenter < minDistanceToCenter)
                        {
                            markerInTriangleClosestToCenter = nodeMarker;
                            minDistanceToCenter = distanceToCenter;
                        }
                    }
                }
            }
        }
        
        if (markerInTriangleClosestToCenter != null)
        {
            /*double centerX = innerToOuterX(((Circle)markerInTriangleClosestToCenter).getCenterX());
            double centerY = innerToOuterY(((Circle)markerInTriangleClosestToCenter).getCenterY());
            Circle tempCircle = new Circle(centerX, centerY, ((Circle)markerInTriangleClosestToCenter).getRadius() * PhyloPenCanvas.this.getModelRenderScale());
            tempCircle.setStroke(Color.RED);
            tempCircle.setFill(Color.GOLD);
            PhyloPenCanvas.this.getChildren().add(tempCircle);*/
            
            Clade clade = getClade(markerInTriangleClosestToCenter);
            
            if (clade != null)
            {
                if (isCollapsed(clade))
                {
                    //System.out.println("EXPANDED");
                    expandClade(clade);
                }
                else
                {
                    //System.out.println("COLLAPSED");
                    collapseClade(clade);
                }
            }
            else
            {
                System.out.println("Clade not found.");
            }
        }
        
    }
    
    private void processRectangleGesture(Rectangle zoomRectangle)
    {
        getInkStrokes().clear();
    }
    
    public Color getNodeFillColor(Clade node)
    {
        return (Color) cladeMetadataCache.get(node).fill;
    }
    
    public Color getNodeOutlineColor(Clade node)
    {
        return (Color) cladeMetadataCache.get(node).stroke;
    }
    
    public void colorOutlineOfSelectedNodes(Color color)
    {
        colorOutlineOfNodes(getSelectedNodes(), color);
    }
    
    public void colorOutlineOfNode(Clade node, Color color)
    {
        LinkedList<Clade> nodes = new LinkedList<>();
        nodes.add(node);
        colorOutlineOfNodes(nodes, color);
    }
    
    public void colorOutlineOfNodes(Color color)
    {
        colorOutlineOfNodes(getTreeModel().getNodes(), color);
    }
    
    public JsonObject colorOutlineOfNodes(Collection<Clade> nodes, Color color)
    {
        ArrayList<Color> oldColors = new ArrayList<>();
        
        colorOutlineOfNodes(nodes, color, oldColors);
        
        JsonObject event = PhyloPenIO.createEventRecord(NODE_RECOLOR, System.currentTimeMillis());
        event.addProperty("recolorType", 1);
        PhyloPenIO.attachColorProperty(event, color);
        PhyloPenIO.attachNodeIdsProperty(event, nodes);

        JsonArray oldColorsArray = new JsonArray();

        for (Color oldColor : oldColors)
            oldColorsArray.add(PhyloPenIO.toJson(oldColor));

        event.add("oldColors", oldColorsArray);
        
        addUndoableEvent(event);
        
        return event;
    }
    
    protected void colorOutlineOfNodes(Collection<Clade> nodes, Color color, List<Color> oldColors)
    {
        for (Clade node : nodes)
        {
            if (oldColors != null)
                oldColors.add(node.getStrokeColor());
            
            //System.out.println(node);
            if (cladeMetadataCache.get(node).nodeMarker instanceof Circle)
            {
                cladeMetadataCache.get(node).stroke = color;
                ((Circle)cladeMetadataCache.get(node).nodeMarker).setStroke(color);
            }
            
            node.setStrokeColor(color);
        }
    }
    
    public void colorFillOfSelectedNodes(Color color)
    {
        colorFillOfNodes(getSelectedNodes(), color);
    }
    
    public void colorFillOfNode(Clade node, Color color)
    {
        LinkedList<Clade> nodes = new LinkedList<>();
        nodes.add(node);
        colorFillOfNodes(nodes, color);
    }
    
    public void colorFillOfNodes(Color color)
    {
        colorFillOfNodes(getTreeModel().getNodes(), color);
    }
    
    public JsonObject colorFillOfNodes(Collection<Clade> nodes, Color color)
    {
        ArrayList<Color> oldColors = new ArrayList<>();
        
        colorFillOfNodes(nodes, color, oldColors);
        
        JsonObject event = PhyloPenIO.createEventRecord(NODE_RECOLOR, System.currentTimeMillis());
        event.addProperty("recolorType", 0);
        PhyloPenIO.attachColorProperty(event, color);
        PhyloPenIO.attachNodeIdsProperty(event, nodes);

        JsonArray oldColorsArray = new JsonArray();

        for (Color oldColor : oldColors)
            oldColorsArray.add(PhyloPenIO.toJson(oldColor));

        event.add("oldColors", oldColorsArray);
        
        addUndoableEvent(event);
        
        return event;
    }
    
    protected void colorFillOfNodes(Collection<Clade> nodes, Color color, List<Color> oldColors)
    {
        for (Clade node : nodes)
        {
            if (oldColors != null)
                oldColors.add(node.getFillColor());
            
            if (cladeMetadataCache.get(node).nodeMarker instanceof Circle)
            {
                cladeMetadataCache.get(node).fill = color;
                ((Circle)cladeMetadataCache.get(node).nodeMarker).setFill(color);
            }
            
            node.setFillColor(color);
        }
    }
    
    public JsonObject setNodeRadiusOfSelectedNodes(double radius)
    {
        return setNodeRadius(getSelectedNodes(), radius);
    }
    
    public JsonObject setNodeRadius(Clade node, double radius)
    {
        List<Clade> nodes = new LinkedList<>();
        nodes.add(node);
        return setNodeRadius(nodes, radius);
    }
    
    public JsonObject setNodeRadius(double radius)
    {
        return setNodeRadius(this.getTreeModel().getNodes(), radius);
    }
    
    public JsonObject setNodeRadius(Collection<Clade> nodes, double radius)
    {
        List<Double> oldRadii = new ArrayList<>(nodes.size());
        
        setNodeRadius(nodes, radius, oldRadii);
        
        JsonObject event = PhyloPenIO.createEventRecord(NODE_RADIUS_CHANGE, System.currentTimeMillis());
        event.addProperty("radius", radius);
        PhyloPenIO.attachNodeIdsProperty(event, nodes);

        JsonArray oldRadiiArray = new JsonArray();

        for (Double oldRadius : oldRadii)
            oldRadiiArray.add(new JsonPrimitive(oldRadius));

        event.add("oldRadii", oldRadiiArray);
        
        addUndoableEvent(event);
        
        return event;
    }
    
    protected void setNodeRadius(Collection<Clade> nodes, double radius, List<Double> oldRadii)
    {
        for (Clade node : nodes)
        {
            oldRadii.add(node.getNodeRadius());
            node.setNodeRadius(radius);
        }
        
        updateLayout();
    }
    
    public JsonObject changeWidthOfSelectedBranches(double width)
    {
        return changeWidthOfBranches(getSelectedNodes(), width);
    }
    
    public JsonObject changeWidthOfBranches(double width)
    {
        return changeWidthOfBranches(getTreeModel().getNodes(), width);
    }
    
    public JsonObject changeWidthOfBranches(Collection<Clade> nodes, double width)
    {
        List<Pair<Clade, Clade>> connections = new ArrayList<>();
        List<Double> oldWidths = new ArrayList<>();
        
        changeWidthOfBranches(nodes, width, connections, oldWidths);
        
        JsonObject event = PhyloPenIO.createEventRecord(BRANCH_WIDTH_CHANGE, System.currentTimeMillis());
        event.addProperty("width", width);
        PhyloPenIO.attachNodeIdsProperty(event, nodes);

        JsonArray connectionsArray = new JsonArray();
        JsonObject connectionJson;

        for (Pair<Clade, Clade> connection : connections)
        {
            connectionJson = new JsonObject();
            connectionJson.addProperty("parent", connection.getKey().getId());
            connectionJson.addProperty("child", connection.getValue().getId());
            connectionsArray.add(connectionJson);
        }

        event.add("connections", connectionsArray);

        JsonArray oldWidthsArray = new JsonArray();

        for (Double oldWidth : oldWidths)
            oldWidthsArray.add(new JsonPrimitive(oldWidth));

        event.add("oldWidths", oldWidthsArray);
        
        addUndoableEvent(event);
        
        return event;
    }
    
    protected void changeWidthOfBranches(Collection<Clade> nodes, double width, List<Pair<Clade, Clade>> connections, List<Double> oldWidths)
    {
        List<NodeConnection> infoList = getRelationships(nodes);
        
        for (NodeConnection info : infoList)
        {
            connections.add(new Pair<>(info.getParent(), info.getChild()));
            
            cladeMetadataCache.get(info.getParent()).branchHorizontalConnectors.get(info.getChildIndex()).setStrokeWidth(width);
            if (cladeMetadataCache.get(info.getParent()).branchVerticalConnector != null)
                cladeMetadataCache.get(info.getParent()).branchVerticalConnector.setStrokeWidth(width);
            
            oldWidths.add(info.getChild().getIncidentBranchLineWidth());
            
            info.getChild().setIncidentBranchLineWidth(width);
        }
    }
    
    public JsonObject changeWidthOfBranch(Clade parent, int childIndex, double width)
    {
        Clade child = parent.getChild(childIndex);
        List<Pair<Clade, Clade>> connections = new ArrayList<>(1);
        connections.add(new Pair<>(parent, child));
        List<Double> oldWidths = new ArrayList<>(1);

        cladeMetadataCache.get(parent).branchHorizontalConnectors.get(childIndex).setStrokeWidth(width);
        if (cladeMetadataCache.get(parent).branchVerticalConnector != null)
            cladeMetadataCache.get(parent).branchVerticalConnector.setStrokeWidth(width);

        oldWidths.add(child.getIncidentBranchLineWidth());

        child.setIncidentBranchLineWidth(width);
        
        List<Clade> nodes = new ArrayList<>(2);
        nodes.add(parent);
        nodes.add(child);
        
        JsonObject event = PhyloPenIO.createEventRecord(BRANCH_WIDTH_CHANGE, System.currentTimeMillis());
        event.addProperty("width", width);
        PhyloPenIO.attachNodeIdsProperty(event, nodes);

        JsonArray connectionsArray = new JsonArray();
        JsonObject connectionJson;

        for (Pair<Clade, Clade> connection : connections)
        {
            connectionJson = new JsonObject();
            connectionJson.addProperty("parent", connection.getKey().getId());
            connectionJson.addProperty("child", connection.getValue().getId());
            connectionsArray.add(connectionJson);
        }

        event.add("connections", connectionsArray);

        JsonArray oldWidthsArray = new JsonArray();

        for (Double oldWidth : oldWidths)
            oldWidthsArray.add(new JsonPrimitive(oldWidth));

        event.add("oldWidths", oldWidthsArray);
        
        addUndoableEvent(event);
        
        return event;
    }
    
    public void colorSelectedBranches(Color color)
    {
        colorBranches(getSelectedNodes(), color);
    }
    
    public void colorBranches(Color color)
    {
        colorBranches(getTreeModel().getNodes(), color);
    }
    
    public JsonObject colorBranches(Collection<Clade> nodes, Color color)
    {
        List<Pair<Clade, Clade>> connections = new ArrayList<>();
        List<Color> oldColors = new ArrayList<>();
        
        colorBranches(nodes, color, connections, oldColors);
        
        JsonObject event = PhyloPenIO.createEventRecord(BRANCH_RECOLOR, System.currentTimeMillis());
        PhyloPenIO.attachColorProperty(event, color);
        PhyloPenIO.attachNodeIdsProperty(event, nodes);

        PhyloPenIO.attachConnectionsProperty(event, connections);

        JsonArray oldColorsArray = new JsonArray();

        for (Color oldColor : oldColors)
            oldColorsArray.add(PhyloPenIO.toJson(oldColor));

        event.add("oldColors", oldColorsArray);
        
        addUndoableEvent(event);
        
        return event;
    }
    
    protected void colorBranches(Collection<Clade> nodes, Color color, List<Pair<Clade, Clade>> connections, List<Color> oldColors)
    {
        List<NodeConnection> infoList = getRelationships(nodes);
        List<Stop> stops;
        
        for (NodeConnection info : infoList)
        {
            connections.add(new Pair<>(info.getParent(), info.getChild()));
            cladeMetadataCache.get(info.getParent()).branchHorizontalConnectors.get(info.getChildIndex()).setStroke(color);
            oldColors.add(info.getChild().getIncidentBranchLineColor());
            info.getChild().setIncidentBranchLineColor(color);
            
            if (cladeMetadataCache.get(info.getParent()).branchVerticalConnector != null)
            {
                stops = new ArrayList<>();
                int index = 0;
                double stopCoordinate;

                for (Line childBranch : cladeMetadataCache.get(info.getParent()).branchHorizontalConnectors)
                {
                    stopCoordinate = index / ((double)info.getParent().getChildCount());
                    stops.add(new Stop(stopCoordinate, (Color)childBranch.getStroke()));

                    if (stopCoordinate < 0.5)
                        stops.add(new Stop(Math.min(0.5, (index + 1) / ((double)info.getParent().getChildCount())), (Color)childBranch.getStroke()));
                    else if (stopCoordinate > 0.5)
                        stops.add(new Stop(Math.min(1.0, (index + 1) / ((double)info.getParent().getChildCount())), (Color)childBranch.getStroke()));
                    else
                        stops.add(new Stop(0.5, (Color)childBranch.getStroke()));

                    index++;
                }

                LinearGradient gradient = new LinearGradient(0.0, 0.0, 0.0, 1.0, true, CycleMethod.NO_CYCLE, stops);
                cladeMetadataCache.get(info.getParent()).branchVerticalConnector.setStroke(gradient);
            }
        }
    }
    
    public JsonObject colorBranch(Clade parent, int childIndex, Color color)
    {
        Clade child = parent.getChild(childIndex);
        List<Pair<Clade, Clade>> connections = new ArrayList<>(1);
        List<Color> oldColors = new ArrayList<>(1);
        
        cladeMetadataCache.get(parent).branchHorizontalConnectors.get(childIndex).setStroke(color);
        oldColors.add(child.getIncidentBranchLineColor());
        child.setIncidentBranchLineColor(color);
        
        connections.add(new Pair<>(parent, child));
        
        if (cladeMetadataCache.get(parent).branchVerticalConnector != null)
        {
            List<Stop> stops = new ArrayList<>();
            int index = 0;
            double stopCoordinate;

            for (Line childBranch : cladeMetadataCache.get(parent).branchHorizontalConnectors)
            {
                stopCoordinate = index / ((double)parent.getChildCount());
                stops.add(new Stop(stopCoordinate, (Color)childBranch.getStroke()));

                if (stopCoordinate < 0.5)
                    stops.add(new Stop(Math.min(0.5, (index + 1) / ((double)parent.getChildCount())), (Color)childBranch.getStroke()));
                else if (stopCoordinate > 0.5)
                    stops.add(new Stop(Math.min(1.0, (index + 1) / ((double)parent.getChildCount())), (Color)childBranch.getStroke()));
                else
                    stops.add(new Stop(0.5, (Color)childBranch.getStroke()));

                index++;
            }

            LinearGradient gradient = new LinearGradient(0.0, 0.0, 0.0, 1.0, true, CycleMethod.NO_CYCLE, stops);
            cladeMetadataCache.get(parent).branchVerticalConnector.setStroke(gradient);
        }
        
        List<Clade> nodes = new ArrayList<>(2);
        nodes.add(parent);
        nodes.add(child);
        
        JsonObject event = PhyloPenIO.createEventRecord(BRANCH_RECOLOR, System.currentTimeMillis());
        PhyloPenIO.attachColorProperty(event, color);
        PhyloPenIO.attachNodeIdsProperty(event, nodes);

        PhyloPenIO.attachConnectionsProperty(event, connections);

        JsonArray oldColorsArray = new JsonArray();

        for (Color oldColor : oldColors)
            oldColorsArray.add(PhyloPenIO.toJson(oldColor));

        event.add("oldColors", oldColorsArray);
        
        addUndoableEvent(event);
        
        return event;
    }
    
    public JsonObject resetAppearance()
    {
        List<Clade> nodes;
        List<Color> oldNodeOutlineColors, oldNodeFillColors, oldBranchColors;
        List<Pair<Clade, Clade>> connections;
        List<Double> oldNodeRadii, oldBranchWidths;
        
        nodes = new ArrayList<>(getTreeModel().getNodes());
        connections = new ArrayList<>();
        oldBranchColors = new ArrayList<>();
        oldBranchWidths = new ArrayList<>();
        
        colorBranches(nodes, getDefaultBranchColor(), connections, oldBranchColors);
        changeWidthOfBranches(nodes, 1.0, new ArrayList<>(), oldBranchWidths);
        
        List<Clade> outerNodes = new ArrayList<>();
        List<Clade> innerNodes = new ArrayList<>();
        
        for (Clade clade : getTreeModel().getNodes())
        {
            if (clade.getChildCount() == 0 || clade.getParent() == null)
                outerNodes.add(clade);
            else
                innerNodes.add(clade);
        }
        
        oldNodeFillColors = new ArrayList<>(outerNodes.size() + innerNodes.size());
        
        this.colorFillOfNodes(outerNodes, getDefaultOuterNodeFill(), oldNodeFillColors);
        this.colorFillOfNodes(innerNodes, getDefaultInnerNodeFill(), oldNodeFillColors);
        
        oldNodeOutlineColors = new ArrayList<>(outerNodes.size() + innerNodes.size());
        
        this.colorOutlineOfNodes(outerNodes, getDefaultOuterNodeStroke(), oldNodeOutlineColors);
        this.colorOutlineOfNodes(innerNodes, getDefaultInnerNodeStroke(), oldNodeOutlineColors);
        
        oldNodeRadii = new ArrayList<>(outerNodes.size() + innerNodes.size());
        
        this.setNodeRadius(outerNodes, getDefaultNodeRadius(), oldNodeRadii);
        this.setNodeRadius(innerNodes, getDefaultNodeRadius(), oldNodeRadii);
        
        nodes = new ArrayList<>(outerNodes.size() + innerNodes.size());
        nodes.addAll(outerNodes);
        nodes.addAll(innerNodes);
        
        updateLayout();
        
        JsonObject event = PhyloPenIO.createEventRecord(APPEARANCE_RESET, System.currentTimeMillis());
        PhyloPenIO.attachNodeIdsProperty(event, nodes);

        JsonArray jsonArray = new JsonArray();

        for (Color oldColor : oldNodeOutlineColors)
            jsonArray.add(PhyloPenIO.toJson(oldColor));

        event.add("oldNodeOutlineColors", jsonArray);

        jsonArray = new JsonArray();

        for (Color oldColor : oldNodeFillColors)
            jsonArray.add(PhyloPenIO.toJson(oldColor));

        event.add("oldNodeFillColors", jsonArray);

        jsonArray = new JsonArray();

        for (Double oldRadii : oldNodeRadii)
            jsonArray.add(new JsonPrimitive(oldRadii));

        event.add("oldNodeRadii", jsonArray);

        PhyloPenIO.attachConnectionsProperty(event, connections);

        jsonArray = new JsonArray();

        for (Color oldColor : oldBranchColors)
            jsonArray.add(PhyloPenIO.toJson(oldColor));

        event.add("oldBranchColors", jsonArray);

        jsonArray = new JsonArray();

        for (Double branchWidth : oldBranchWidths)
            jsonArray.add(new JsonPrimitive(branchWidth));

        event.add("oldBranchWidths", jsonArray);
        
        addUndoableEvent(event);
        
        return event;
    }
    
    public double getDefaultNodeRadius()
    {
        return defaultNodeRadius;
    }
    
    public double getBranchWidth(Clade parent, int childIndex)
    {
        return cladeMetadataCache.get(parent).branchHorizontalConnectors.get(childIndex).getStrokeWidth();
    }
    
    public Color getBranchColor(Clade parent, int childIndex)
    {
        return (Color) cladeMetadataCache.get(parent).branchHorizontalConnectors.get(childIndex).getStroke();
    }
    
    private List<NodeConnection> getRelationships(Collection<Clade> nodes)
    {
        Clade [] nodeArray = nodes.toArray(new Clade[nodes.size()]);
        int childIndex;
        List<NodeConnection> relationshipList = new ArrayList<>();
        
        for (int i = 0; i < nodeArray.length; i++)
        {
            for (int j = 0; j < nodeArray.length; j++)
            {
                if (i != j)
                {
                    childIndex = nodeArray[j].getChildIndex(nodeArray[i]);

                    if (childIndex >= 0)
                        relationshipList.add(new NodeConnection(nodeArray[j], nodeArray[i], childIndex));
                }
            }
        }
        
        return relationshipList;
    }
    
    private class RectangleZoomHandler implements EventHandler<ActionEvent>
    {
        private final Timeline timeline;
        private final Rectangle zoomRectangle2;
        private long lastUpdateTime;
        private final double zoomScaleTarget;
        private final double zoomTargetX;
        private final double zoomTargetY;
        
        public RectangleZoomHandler(Rectangle zoomRectangle, Timeline timeline)
        {
            // convert corners to inner canvas extents
            double upperLeftX = outerToInnerX(zoomRectangle.getX()), upperLeftY = outerToInnerY(zoomRectangle.getY());
            double lowerRightX = outerToInnerX(zoomRectangle.getX() + zoomRectangle.getWidth()), lowerRightY = outerToInnerY(zoomRectangle.getY() + zoomRectangle.getHeight());
            Rectangle zoomRectangle2 = new Rectangle(upperLeftX, upperLeftY, lowerRightX - upperLeftX, lowerRightY - upperLeftY);
            zoomRectangle2.setFill(zoomRectangle.getFill());
            zoomRectangle2.setStroke(zoomRectangle.getStroke());
            zoomRectangle2.setStrokeWidth(zoomRectangle.getStrokeWidth());
            getModelRenderPane().getChildren().add(zoomRectangle2);
            double zoomFactor = Math.min(getViewport().getViewportBounds().getWidth() / zoomRectangle.getWidth(), getViewport().getViewportBounds().getHeight() / zoomRectangle.getHeight());
            zoomScaleTarget = getModelRenderScale() * zoomFactor;
            zoomTargetX = zoomRectangle2.getX() + (zoomRectangle2.getWidth() / 2.0);
            zoomTargetY = zoomRectangle2.getY() + (zoomRectangle2.getHeight() / 2.0);
            
            this.zoomRectangle2 = zoomRectangle2;
            this.timeline = timeline;
            this.lastUpdateTime = System.currentTimeMillis();
        }
        
        @Override
        public void handle(ActionEvent event)
        {
            double zoomRate = 2.5; // per second
            long currentTime = System.currentTimeMillis();

            double newScale = Math.min(getModelRenderScale() + zoomRate * ((currentTime - lastUpdateTime) / 1000.0f), zoomScaleTarget);
            setModelRenderScale(newScale);
            translateModelRenderTo(-(zoomTargetX * newScale - getViewport().getViewportBounds().getWidth() * 0.5), -(zoomTargetY * newScale - getViewport().getViewportBounds().getHeight() * 0.5));

            lastUpdateTime = currentTime;
            
            if (newScale == zoomScaleTarget)
            {
                getModelRenderPane().getChildren().remove(zoomRectangle2);
                timeline.stop();
            }
        }
    }
    
    public ScrollPane getViewport()
    {
        return viewport;
    }
    
    public void setViewport(ScrollPane viewport)
    {
        this.viewport = viewport;
    }
    
    private double outerToInnerX(double x) { return (x - getModelRenderTranslationX()) / getModelRenderScale(); }
    private double outerToInnerY(double y) { return (y - getModelRenderTranslationY()) / getModelRenderScale(); }

    private double innerToOuterX(double x) { return x * getModelRenderScale() + getModelRenderTranslationX(); }
    private double innerToOuterY(double y) { return y * getModelRenderScale() + getModelRenderTranslationY(); }
    
    private class CladeMetadata
    {
        public int remainingLevels;
        public double remainingLength;
        public double childMidpoint;
        public int numLeaves;
        public int leafNum;
        public int leafNumRangeMin;
        public int leafNumRangeMax;
        public Clade self;
        public int childNum;
        
        public double branchLengthSum;
        public double branchScaleFactor;
        public double branchScaleFactorOfRemainingLength;

        // if single child or no children (leaf), have a node
        public Node nodeMarker;

        // if leaf, have a node label
        public Labeled nodeLabel;

        // if one child, one horizontal connector; if interior, two or more
        public List<Line> branchHorizontalConnectors;
        
        // non-null only if has more than one child.
        public Line branchVerticalConnector;

        // branch label(s)
        public List<Label> branchLabels;
        
        public CladeImageViewer image;
        
        public boolean dirty;
        
        public Paint fill;
        public Paint stroke;
        
        public boolean inCollapsedRegion; // tells if in collapsed region. Node itself may not be collapsed directly.
        
        public CladeMetadata()
        {
            branchHorizontalConnectors = new ArrayList<>();
            branchLabels = new ArrayList<>();
            dirty = false;
            inCollapsedRegion = false;
        }
    }
    
    private class CachingHelperData
    {
        public CachingHelperData()
        {
            this.nextLeafNum = 0;
            this.maxBranchLengthSum = 0.0;
        }
        
        // copy constructor
        public CachingHelperData(CachingHelperData original)
        {
            this.nextLeafNum = original.nextLeafNum;
            this.maxBranchLengthSum = original.maxBranchLengthSum;
        }
        
        public int nextLeafNum;
        //public Clade lastLeaf;
        public double maxBranchLengthSum;
    }
    
    protected void clearCache()
    {
        lastModelCached = null;
        cladeMetadataCache.clear();
    }
    
    protected void cacheCladeMetadata()
    {
        clearCache();
        lastModelCached = getTreeModel();
        updateCladeMetadataCache();
    }
    
    protected void updateCladeMetadataCache()
    {
        CachingHelperData helper = new CachingHelperData();
        cacheCladeMetadata(getTreeModel().getRoot(), -1, helper);
        
        for (CladeMetadata metadata : cladeMetadataCache.values())
        {
            metadata.branchScaleFactor = metadata.self.getBranchLength() / helper.maxBranchLengthSum;
            metadata.branchScaleFactorOfRemainingLength = metadata.remainingLength / helper.maxBranchLengthSum;
        }
    }
    
    private CladeMetadata cacheCladeMetadata(Clade clade, int cladeChildIndex, CachingHelperData helperData)
    {
        CladeMetadata metadata = cladeMetadataCache.get(clade);
        
        if (metadata == null)
        {
            metadata = new CladeMetadata();
            cladeMetadataCache.put(clade, metadata);
        }
        
        metadata.self = clade;
        metadata.childNum = cladeChildIndex;
        CladeMetadata childMetadata;
        
        if (clade.getParent() == null)
            metadata.branchLengthSum = clade.getBranchLength();
        else
            metadata.branchLengthSum = cladeMetadataCache.get(clade.getParent()).branchLengthSum + clade.getBranchLength();
        
        if (clade.getChildCount() > 0) // if non-leaf node
        {
            metadata.numLeaves = 0;
            metadata.leafNumRangeMin = Integer.MAX_VALUE;
            metadata.leafNumRangeMax = Integer.MIN_VALUE;
            metadata.remainingLevels = 0;
            metadata.remainingLength = Double.MIN_VALUE;
            
            int childIndex = 0;
            
            // iterate through children
            for (Clade child : clade)
            {
                childMetadata = cacheCladeMetadata(child, childIndex, helperData);
                metadata.numLeaves += childMetadata.numLeaves;
                
                if (childMetadata.leafNumRangeMin < metadata.leafNumRangeMin)
                        metadata.leafNumRangeMin = childMetadata.leafNumRangeMin;
                
                if (childMetadata.leafNumRangeMax > metadata.leafNumRangeMax)
                    metadata.leafNumRangeMax = childMetadata.leafNumRangeMax;
                
                if (childMetadata.remainingLevels > metadata.remainingLevels)
                    metadata.remainingLevels = childMetadata.remainingLevels;
                
                if (childMetadata.remainingLength > metadata.remainingLength)
                    metadata.remainingLength = childMetadata.remainingLength;
            }
            
            if (metadata.remainingLength < 0.0)
                metadata.remainingLength = 0.0;
            
            metadata.remainingLength += clade.getBranchLength();
            metadata.remainingLevels++;
            metadata.childMidpoint = (cladeMetadataCache.get(clade.getFirstChild()).childMidpoint + cladeMetadataCache.get(clade.getLastChild()).childMidpoint) / 2.0;
            metadata.leafNum = -1; // not a leaf
        }
        else // leaf node
        {
            metadata.remainingLength = clade.getBranchLength();
            metadata.leafNum = helperData.nextLeafNum++;
            //helperData.lastLeaf = clade;
            metadata.numLeaves = 1;
            metadata.leafNumRangeMin = metadata.leafNum;
            metadata.leafNumRangeMax = metadata.leafNum;
            metadata.childMidpoint = metadata.leafNum;
            metadata.remainingLevels = 0;
            
            //System.out.println(metadata.branchLengthSum);
            
            if (metadata.branchLengthSum > helperData.maxBranchLengthSum)
                helperData.maxBranchLengthSum = metadata.branchLengthSum;
        }
        
        return metadata;
    }
    
    public boolean isCollapsed(Clade clade)
    {
        return clade.isCollapsed();
    }
    
    public JsonObject collapseClade(Clade clade)
    {
        JsonObject event = null;
        
        if (clade.getChildCount() > 0)
        {
            clade.setCollapsed(true);
            collapsedClades.add(clade);
            updateLayout();
            
            event = PhyloPenIO.createEventRecord(CLADE_COLLAPSE);
            event.addProperty("nodeId", clade.getId());
            
            addUndoableEvent(event);
        }
        
        return event;
    }
    
    public JsonObject expandClade(Clade clade)
    {
        JsonObject event = null;
        
        clade.setCollapsed(false);
        if (collapsedClades.remove(clade))
        {
            updateLayout();
            
            event = PhyloPenIO.createEventRecord(CLADE_EXPAND);
            event.addProperty("nodeId", clade.getId());
            
            addUndoableEvent(event);
        }
        
        return event;
    }
    
    protected Clade getClade(Node nodeMarker)
    {
        for (Map.Entry<Clade, CladeMetadata> entry : cladeMetadataCache.entrySet())
        {
            if (entry.getValue().nodeMarker == nodeMarker)
            {
                return entry.getKey();
            }
        }
        
        return null;
    }
    
    public double getLayoutXOffset()
    {
        return layoutXOffset;
    }
    
    public double getLayoutYOffset()
    {
        return layoutYOffset;
    }
    
    protected Node createNodeMarker(double centerX, double centerY, double radius, Clade clade)
    {
        Circle nodeCircle = new Circle(centerX, centerY, new RemScaler().scale(radius));
        nodeCircle.setStrokeWidth(2.0);
        
        if (clade.getFillColor() != null && clade.getStrokeColor() != null)
        {
            nodeCircle.setFill(clade.getFillColor());
            nodeCircle.setStroke(clade.getStrokeColor());
        }
        else if (clade.getParent() == null || clade.getChildCount() == 0)
        {
            clade.setFillColor(defaultOuterNodeFill);
            clade.setStrokeColor(defaultOuterNodeStroke);
            nodeCircle.setFill(defaultOuterNodeFill);
            nodeCircle.setStroke(defaultOuterNodeStroke);
        }
        else
        {
            clade.setFillColor(defaultInnerNodeFill);
            clade.setStrokeColor(defaultInnerNodeStroke);
            nodeCircle.setFill(defaultInnerNodeFill);
            nodeCircle.setStroke(defaultInnerNodeStroke);
        }
        
        nodeCircle.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                if (PhyloPenCanvas.this.isShowingAnnotationPlaceholders())
                {
                    Clade clade = PhyloPenCanvas.this.getClade(nodeCircle);
                    
                    for (AnnotationPolygon placeholder : getAnnotationPlaceholders())
                    {
                        if (placeholder.isVisible() && placeholder.getAnnotation().getNodes().contains(clade))
                        {
                            placeholder.fireEvent(event);
                        }
                    }
                }
            }
        });
        
        return nodeCircle;
    }
    
    protected void updateNodeMarker(Node nodeMarker, double centerX, double centerY, double radius, Clade clade, CladeMetadata metadata)
    {
        Circle nodeCircle = (Circle) nodeMarker;
        nodeCircle.setCenterX(centerX);
        nodeCircle.setCenterY(centerY);
        nodeCircle.setRadius(new RemScaler().scale(radius));
        
        nodeCircle.setFill(metadata.fill);
        nodeCircle.setStroke(metadata.stroke);
    }
    
    private Node getUpdatedNodeMarker(double centerX, double centerY, Clade clade, CladeMetadata metadata)
    {
        Node nodeMarker;
        
        if (metadata.nodeMarker == null)
        {
            nodeMarker = createNodeMarker(centerX, centerY, clade.getNodeRadius(), clade);
            metadata.nodeMarker = nodeMarker;
            getNodeMarkers().add(nodeMarker);
            
            metadata.fill = ((Circle)nodeMarker).getFill();
            metadata.self.setFillColor((Color)((Circle)nodeMarker).getFill());
            metadata.stroke = ((Circle)nodeMarker).getStroke();
            metadata.self.setStrokeColor((Color)((Circle)nodeMarker).getStroke());
        }
        else
        {
            nodeMarker = metadata.nodeMarker;
            updateNodeMarker(nodeMarker, centerX, centerY, clade.getNodeRadius(), clade, metadata);
        }
        
        return nodeMarker;
    }
    
    private Line createHorizontalBranchLine(double x0, double x1, double y, Clade incidentClade)
    {
        Line branchLine = new Line(x0, y, x1, y);
        
        branchLine.setMouseTransparent(true);
        
        if (incidentClade.getIncidentBranchLineColor() == null)
        {
            branchLine.setStroke(defaultBranchColor);
            incidentClade.setIncidentBranchLineColor(defaultBranchColor);
        }
        else
            branchLine.setStroke(incidentClade.getIncidentBranchLineColor());
        
        branchLine.setStrokeWidth(incidentClade.getIncidentBranchLineWidth());
        
        branchLine.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                if (PhyloPenCanvas.this.isShowingAnnotationPlaceholders())
                {
                    Clade clade1 = null, clade2 = null;
                    int index;
                    
                    for (CladeMetadata metadata : cladeMetadataCache.values())
                    {
                        index = metadata.branchHorizontalConnectors.indexOf(branchLine);
                        
                        if (index >= 0)
                        {
                            clade1 = metadata.self;
                            clade2 = clade1.getChild(index);
                            break;
                        }
                    }
                    
                    for (AnnotationPolygon placeholder : getAnnotationPlaceholders())
                    {
                        if (placeholder.isVisible())
                        {
                            if (placeholder.getAnnotation().getNodes().contains(clade1) && placeholder.getAnnotation().getNodes().contains(clade2))
                            {
                                placeholder.fireEvent(event);
                            }
                        }
                    }
                }
            }
        });
        
        return branchLine;
    }
    
    private Line createVerticalBranchLine(double x, double y0, double y1)
    {
        Line branchLine = new Line(x, y0, x, y1);
        branchLine.setMouseTransparent(true);
        branchLine.setStroke(defaultBranchColor);
        
        branchLine.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                if (PhyloPenCanvas.this.isShowingAnnotationPlaceholders())
                {
                    Clade clade = null;
                    
                    for (CladeMetadata metadata : cladeMetadataCache.values())
                    {
                        if (branchLine == metadata.branchVerticalConnector)
                        {
                            clade = metadata.self;
                            break;
                        }
                    }
                    
                    for (AnnotationPolygon placeholder : getAnnotationPlaceholders())
                    {
                        if (placeholder.isVisible())
                        {
                            if (placeholder.getAnnotation().getNodes().contains(clade))
                            {
                                for (Clade clade2 : clade)
                                {
                                    if (placeholder.getAnnotation().getNodes().contains(clade2))
                                    {
                                        placeholder.fireEvent(event);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        
        return branchLine;
    }
    
    private Hyperlink createHyperlinkFor(Clade clade)
    {
        String name = clade.getLabel();
        Hyperlink link = new Hyperlink(name);
        
        final String linkDestination;
        
        name = name != null ? name.trim(): "?";
        if (clade.hasAttribute("url"))
        {
            linkDestination = (String) clade.getAttributeValue("url");
        }
        else
        {
            name = name.replace(" ", "_");
            linkDestination = "https://en.wikipedia.org/wiki/" + name;
        }
        
        link.setDisable(!isShowingHyperlinks());
        
        link.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                try
                {
                    Desktop.getDesktop().browse(new URI(linkDestination));
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
                catch (URISyntaxException e1)
                {
                    e1.printStackTrace();
                }
            }
        });
                
        return link;
    }
    
    private BoundingBox union(BoundingBox boundingBox1, BoundingBox boundingBox2)
    {
        if (boundingBox1 == null)
            return boundingBox2;
        
        double minX = Math.min(boundingBox1.getMinX(), boundingBox2.getMinX());
        double minY = Math.min(boundingBox1.getMinY(), boundingBox2.getMinY());
        double maxX = Math.max(boundingBox1.getMaxX(), boundingBox2.getMaxX());
        double maxY = Math.max(boundingBox1.getMaxY(), boundingBox2.getMaxY());
        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }
    
    private BoundingBox [] layoutClade(Clade clade, boolean inCollapsedRegion, boolean inCutRegion, double startX)
    {
        return layoutClade(clade, inCollapsedRegion, inCutRegion, startX, startX, null, null, new CachingHelperData());
    }
    
    private BoundingBox [] layoutClade(Clade clade, boolean inCollapsedRegion, boolean inCutRegion, double startX, double unscaledStartX, BoundingBox unscaledBoundingBox, BoundingBox scaledBoundingBox, CachingHelperData helperData)
    {
        RemScaler rem = new RemScaler();
        
        CladeMetadata metadata = cladeMetadataCache.get(clade);
        boolean collapsedNode = isCollapsed(clade);
        boolean cutNode = getCutClade() == clade;
        
        if (getDefaultTreeWidth() > 0.0)
            lengthScaling = this.getDefaultTreeWidth();
        
        Map<String, Double> nattrs = clade.getNumericalAttributeMap();
                
        for (Map.Entry<String, Double> kv : nattrs.entrySet())
        {
            AttributeMetadata attrMetadata = attributeMetadataCache.get(kv.getKey());

            if (attrMetadata == null)
            {
                attrMetadata = new AttributeMetadata();
                attributeMetadataCache.put(kv.getKey(), attrMetadata);
            }

            if (kv.getValue() < attrMetadata.minVal) attrMetadata.minVal = kv.getValue();
            if (kv.getValue() > attrMetadata.maxVal) attrMetadata.maxVal = kv.getValue();
        }
        
        if (startX < 0.0)
            startX = layoutXOffset;
        
        if (unscaledStartX < 0.0)
            unscaledStartX = layoutXOffset;
        
        // if leaf node
        if (clade.getChildCount() == 0)
        {
            helperData.nextLeafNum = metadata.leafNum + 1;
            
            if (collapsedNode)
                inCollapsedRegion = true;
            
            if (cutNode)
                inCutRegion = true;
            
            // layout node name labels
            if (isLabelingLeafNodeNames())
            {
                Labeled leafLabel;
                
                // if node label not cached, create a new one
                if (metadata.nodeLabel == null)
                    leafLabel = createHyperlinkFor(clade);
                else
                    leafLabel = metadata.nodeLabel;
                
                leafLabel.setMouseTransparent(false);
                
                if (!inCollapsedRegion)
                {
                    leafLabel.setVisible(true);
                    leafLabel.setText(clade.getLabel() + ((isLabelingFinalBranchLengthsWithLeafNames()) ? ("   (" + roundDouble(clade.getBranchLength(), 6) + ")") : ""));
                    leafLabel.setAlignment(Pos.CENTER_LEFT); // center vertically, left horizontally
                    leafLabel.setPrefHeight(textRowHeight);
                    // set position (layoutX and layoutY of Node instance)
                    leafLabel.relocate(startX + 4.0 * rem.scale(clade.getNodeRadius()), layoutYScale * (layoutYOffset + metadata.leafNum * layoutRowHeight - 0.5 * layoutRowHeight) - textRowHeight * 0.5);

                    if (leafLabel.getLayoutX() + 200.0 > rightEdge)
                        rightEdge = leafLabel.getLayoutX() + 200.0;
                }
                else
                {
                    leafLabel.setVisible(false);
                    
                    if (startX + 4.0 * rem.scale(clade.getNodeRadius()) + 200.0 > rightEdge)
                        rightEdge = startX + 4.0 * rem.scale(clade.getNodeRadius()) + 200.0;
                }
                
                if (metadata.nodeLabel == null)
                {
                    metadata.nodeLabel = leafLabel;
                    getLabels().add(leafLabel);
                }
            }
            else
            {
                if (startX + rem.scale(clade.getNodeRadius()) + 10.0 > rightEdge)
                    rightEdge = startX + rem.scale(clade.getNodeRadius()) + 10.0;
                
                // if a label exists when it should not (meaning one of the options changed), remove it.
                if (metadata.nodeLabel != null)
                {
                    Labeled labelToRemove = (Labeled) metadata.nodeLabel;
                    metadata.nodeLabel = null;
                    getLabels().remove(labelToRemove);
                }
            }
            
            Node leafMarker;
            
            // layout nodes
            leafMarker = getUpdatedNodeMarker((startX + rem.scale(clade.getNodeRadius())), layoutYScale * (layoutYOffset + metadata.leafNum * layoutRowHeight - 0.5 * layoutRowHeight), clade, metadata);
            leafMarker.setVisible(!inCollapsedRegion);
            
            if (leafMarker instanceof Circle)
            {
                Color c = (Color) ((Circle)leafMarker).getStroke();
                Color transNodeStrokeColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), (inCutRegion ? cutTransparency : 1.0));
                c = (Color) ((Circle)leafMarker).getFill();
                Color transNodeFillColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), (inCutRegion ? cutTransparency : 1.0));
                ((Circle)leafMarker).setStroke(transNodeStrokeColor);
                ((Circle)leafMarker).setFill(transNodeFillColor);
            }
            
            // if leaf node has image(s)
            if (clade.getImageCount() > 0)
            {
                if (metadata.image == null)
                {
                    metadata.image = new CladeImageViewer();
                    metadata.image.setMaxPreviewImageWidth(125.0);
                    metadata.image.setMaxPreviewImageHeight(150.0);
                    
                    metadata.image.setOnFinishedLoading(new EventHandler<WorkerStateEvent>()
                    {
                        @Override
                        public void handle(WorkerStateEvent event)
                        {
                            PhyloPenCanvas.this.updateLayout();
                        }
                    });
                    
                    metadata.image.setClade(clade);
                    addCladeImage(metadata.image);
                }
                
                metadata.image.setVisible(!inCollapsedRegion && isLeafImagesVisible());
                metadata.image.relocate((startX + rem.scale(clade.getNodeRadius())) + imageOffsetFromLeaf, layoutYScale * (layoutYOffset + metadata.leafNum * layoutRowHeight - 0.5 * layoutRowHeight) - metadata.image.getHeight() * 0.5);
            }
        }
        else // has children
        {
            CladeMetadata [] childMetadata = new CladeMetadata[clade.getChildCount()];
            int childIndex = 0;
            int maxRemainingLevels = Integer.MIN_VALUE;
            
            boolean recache = metadata.dirty;
            
            if (!recache)
            {
                // make sure the children are still in the original cached order,
                // otherwise re-cache
                for (Clade child : clade)
                {
                    childMetadata[childIndex] = cladeMetadataCache.get(child);

                    if (childMetadata[childIndex].childNum != childIndex)
                    {
                        recache = true;
                        break;
                    }

                    childIndex++;
                }
            }
            else
                metadata.dirty = false;
            
            if (recache) // ??
            {
                //cacheCladeMetadata(clade, (clade.getParent() == null ? 0 : clade.getParent().getChildIndex(clade)), new CachingHelperData(helperData));
                cacheCladeMetadata(getTreeModel().getRoot(), -1, new CachingHelperData());
                
                childIndex = 0;
                
                // reload metadata in correct order
                for (Clade child : clade)
                    childMetadata[childIndex++] = cladeMetadataCache.get(child);
            }
            
            
            
            
            // ^^
            // layout node name labels
            if (isLabelingAncestorNames())
            {
                Labeled ancestorLabel;
                
                // if node label not cached, create a new one
                if (metadata.nodeLabel == null)
                    ancestorLabel = createHyperlinkFor(clade);
                else
                    ancestorLabel = metadata.nodeLabel;
                
                ancestorLabel.setMouseTransparent(false);
                
                if (!inCollapsedRegion)
                {
                    ancestorLabel.setVisible(true);
                    ancestorLabel.setText(clade.getLabel());
                    ancestorLabel.setAlignment(Pos.CENTER_LEFT); // center vertically, left horizontally
                    ancestorLabel.setPrefHeight(textRowHeight);
                    // set position (layoutX and layoutY of Node instance)
                    if (clade.getChildCount() % 2 == 1)
                        ancestorLabel.relocate(startX + rem.scale(clade.getNodeRadius()) * 0.5, layoutYScale * (layoutYOffset + metadata.childMidpoint * layoutRowHeight - 0.5 * layoutRowHeight) - textRowHeight * 0.5 + 9);
                    else
                        ancestorLabel.relocate(startX + rem.scale(clade.getNodeRadius()) + 2, layoutYScale * (layoutYOffset + metadata.childMidpoint * layoutRowHeight - 0.5 * layoutRowHeight) - textRowHeight * 0.5);

                    if (ancestorLabel.getLayoutX() + 200.0 > rightEdge)
                        rightEdge = ancestorLabel.getLayoutX() + 200.0;
                }
                else
                {
                    ancestorLabel.setVisible(false);
                    
                    if (startX + 4.0 * rem.scale(clade.getNodeRadius()) + 200.0 > rightEdge)
                        rightEdge = startX + 4.0 * rem.scale(clade.getNodeRadius()) + 200.0;
                }
                
                if (metadata.nodeLabel == null)
                {
                    metadata.nodeLabel = ancestorLabel;
                    getLabels().add(ancestorLabel);
                }
            }
            else
            {
                if (startX + rem.scale(clade.getNodeRadius()) + 10.0 > rightEdge)
                    rightEdge = startX + rem.scale(clade.getNodeRadius()) + 10.0;
                
                // if a label exists when it should not (meaning one of the options changed), remove it.
                if (metadata.nodeLabel != null)
                {
                    Labeled labelToRemove = (Labeled) metadata.nodeLabel;
                    metadata.nodeLabel = null;
                    getLabels().remove(labelToRemove);
                }
            }
            
            childIndex = 0;
            
            for (Clade child : clade)
            {
                if (maxRemainingLevels < childMetadata[childIndex].remainingLevels)
                    maxRemainingLevels = childMetadata[childIndex].remainingLevels;
                
                childIndex++;
            }
            
            boolean originalInCollapsedRegion = inCollapsedRegion;
            
            if (collapsedNode)
                inCollapsedRegion = true;
            
            if (cutNode)
                inCutRegion = true;
            
            // Add Branch Lines
            
            Line branchLine;
            
            if (clade.getChildCount() > 1)
            {
                double y0 = layoutYScale * (layoutYOffset + childMetadata[0].childMidpoint * layoutRowHeight - 0.5 * layoutRowHeight);
                double y1 = layoutYScale * (layoutYOffset + childMetadata[clade.getChildCount() - 1].childMidpoint * layoutRowHeight - 0.5 * layoutRowHeight);
                
                if (metadata.branchVerticalConnector == null)
                {
                    branchLine = createVerticalBranchLine(startX, y0, y1);
                    metadata.branchVerticalConnector = branchLine;
                    getBranchLines().add(branchLine);
                }
                else
                {
                    branchLine = metadata.branchVerticalConnector;
                    branchLine.setStartX(startX);
                    branchLine.setEndX(startX);
                    branchLine.setStartY(y0);
                    branchLine.setEndY(y1);
                }
                
                branchLine.setVisible(!inCollapsedRegion);
                
                unscaledBoundingBox = union(unscaledBoundingBox, new BoundingBox(unscaledStartX, y0 / layoutYScale, 0.0, (y1 - y0) / layoutYScale));
                scaledBoundingBox = union(scaledBoundingBox, new BoundingBox(startX, y0, 0.0, y1 - y0));
            }
            else if (clade.getChildCount() == 1)
            {
                // if children went down from multiple to only one, we need to remove the vertical connector
                if (metadata.branchVerticalConnector != null)
                {
                    getBranchLines().remove(metadata.branchVerticalConnector);
                    metadata.branchVerticalConnector = null;
                }
            }
            
            // remove excess horizontal connectors
            while (metadata.branchHorizontalConnectors.size() > clade.getChildCount())
            {
                getBranchLines().remove(metadata.branchHorizontalConnectors.get(metadata.branchHorizontalConnectors.size() - 1));
                getLabels().remove(metadata.branchLabels.get(metadata.branchLabels.size() - 1));
                metadata.branchHorizontalConnectors.remove(metadata.branchHorizontalConnectors.size() - 1);
                metadata.branchLabels.remove(metadata.branchLabels.size() - 1);
            }
            
            childIndex = 0;
            double defaultWidth, width, y, unscaledWidth, scaledWidth;
            
            for (Clade child : clade)
            {
                defaultWidth = (lineLength * (1.0 - childMetadata[childIndex].remainingLevels + (double) maxRemainingLevels));
                width = layoutXScale * ((isScalingBranchesByLength()) ? (childMetadata[childIndex].branchScaleFactor * lengthScaling) : defaultWidth);
                y = layoutYScale * (layoutYOffset + childMetadata[childIndex].childMidpoint * layoutRowHeight - 0.5 * layoutRowHeight);
                
                if (metadata.branchHorizontalConnectors.size() <= childIndex)
                {
                    if (inCollapsedRegion)
                    {
                        branchLine = createHorizontalBranchLine(startX, startX + width, y, child);
                        unscaledWidth = defaultWidth;
                        scaledWidth = width;
                        branchLine.setVisible(false);
                    }
                    else
                    {
                        if (isCollapsed(child))
                        {
                            // EDIT
                            double defaultWidth0a = (lineLength * (1.0 + (double) maxRemainingLevels));
                            double width0a = layoutXScale * ((isScalingBranchesByLength()) ? (childMetadata[childIndex].branchScaleFactorOfRemainingLength * lengthScaling) : defaultWidth0a);
                            unscaledWidth = defaultWidth0a;
                            scaledWidth = width0a;
                            branchLine = createHorizontalBranchLine(startX, startX + width0a, y, child);
                        }
                        else
                        {
                            branchLine = createHorizontalBranchLine(startX, startX + width, y, child);
                            unscaledWidth = defaultWidth;
                            scaledWidth = width;
                        }

                        branchLine.setVisible(true);
                    }
                    
                    Color color = (Color) branchLine.getStroke();
                    branchLine.setStroke(new Color(color.getRed(), color.getGreen(), color.getBlue(), (inCutRegion ? cutTransparency : 1.0)));

                    metadata.branchHorizontalConnectors.add(branchLine);
                    getBranchLines().add(branchLine);
                }
                else
                {
                    branchLine = metadata.branchHorizontalConnectors.get(childIndex);

                    if (inCollapsedRegion)
                    {
                        branchLine.setVisible(false);
                        unscaledWidth = defaultWidth;
                        scaledWidth = width;
                    }
                    else
                    {
                        branchLine.setStartX(startX);

                        if (isCollapsed(child))
                        {
                            // EDIT
                            double defaultWidth0a = (lineLength * (1.0 + (double) maxRemainingLevels));
                            double width0a = layoutXScale * ((isScalingBranchesByLength()) ? (childMetadata[childIndex].branchScaleFactorOfRemainingLength * lengthScaling) : defaultWidth0a);
                            unscaledWidth = defaultWidth0a;
                            scaledWidth = width0a;
                            branchLine.setEndX(startX + width0a);
                        }
                        else
                        {
                            branchLine.setEndX(startX + width);
                            unscaledWidth = defaultWidth;
                            scaledWidth = width;
                        }

                        branchLine.setStartY(y);
                        branchLine.setEndY(y);
                        branchLine.setVisible(true);
                    }
                    
                    Color color = (Color) branchLine.getStroke();
                    branchLine.setStroke(new Color(color.getRed(), color.getGreen(), color.getBlue(), (inCutRegion ? cutTransparency : 1.0)));
                }
                
                unscaledBoundingBox = union(unscaledBoundingBox, new BoundingBox(unscaledStartX, y / layoutYScale, unscaledWidth, 0.0));
                scaledBoundingBox = union(scaledBoundingBox, new BoundingBox(startX, y, scaledWidth, 0.0));
                
                if (child.getBranchLength() > 0.0 && ((isLabelingIntermediateBranchLengths() && child.getChildCount() > 0) || (isLabelingFinalBranchLengths() && child.getChildCount() == 0)))
                {
                    Label branchLabel;

                    if (metadata.branchLabels.size() <= childIndex)
                    {
                        branchLabel = new Label();
                        branchLabel.setPrefSize(branchLabelWidth, textRowHeight);
                        branchLabel.setAlignment(Pos.CENTER_RIGHT); // center vertically, right horizontally
                        branchLabel.setMouseTransparent(true);

                        metadata.branchLabels.add(childIndex, branchLabel);
                        getLabels().add(branchLabel);
                        //System.out.println("Branch label created: " + Double.toString(roundDouble((isCollapsed(child)) ? childMetadata[childIndex].remainingLength : child.getBranchLength(), 6)));
                    }
                    else
                    {
                        branchLabel = metadata.branchLabels.get(childIndex);
                        
                        // if branch label does not exist (meaning something changed), create one
                        if (branchLabel == null)
                        {
                            branchLabel = new Label();
                            branchLabel.setPrefSize(branchLabelWidth, textRowHeight);
                            branchLabel.setAlignment(Pos.CENTER_RIGHT); // center vertically, right horizontally
                            branchLabel.setMouseTransparent(true);

                            metadata.branchLabels.set(childIndex, branchLabel);
                            getLabels().add(branchLabel);
                        }
                    }

                    branchLabel.setText(Double.toString(roundDouble((isCollapsed(child)) ? childMetadata[childIndex].remainingLength : child.getBranchLength(), 6)));
                    branchLabel.relocate(startX + width - branchLabelWidth - 15.0, y - textRowHeight + 4.0);
                    branchLabel.setVisible(!inCollapsedRegion);
                }
                else if (metadata.branchLabels.size() <= childIndex)
                {
                    metadata.branchLabels.add(childIndex, null);
                }
                else if (metadata.branchLabels.get(childIndex) != null)
                {
                    Label labelToRemove = metadata.branchLabels.get(childIndex);
                    metadata.branchLabels.set(childIndex, null);
                    getLabels().remove(labelToRemove);
                }

                // lay out child clade
                BoundingBox [] boundingBoxes = layoutClade(child, inCollapsedRegion, inCutRegion, startX + width, unscaledStartX + defaultWidth, unscaledBoundingBox, scaledBoundingBox, helperData);
                unscaledBoundingBox = boundingBoxes[0];
                scaledBoundingBox = boundingBoxes[1];
                childIndex++;
            }
            
            if (clade.getChildCount() > 0)
            {
                // recolor vertical branch according to horizontal branch colors
                List<Stop> stops;
                stops = new ArrayList<>();
                int index = 0;
                double stopCoordinate;
                Color regularColor, transColor;
                
                for (Line childBranch : metadata.branchHorizontalConnectors)
                {
                    regularColor = (Color) childBranch.getStroke();
                    transColor = new Color(regularColor.getRed(), regularColor.getGreen(), regularColor.getBlue(), (inCutRegion ? cutTransparency : 1.0));
                    
                    stopCoordinate = index / ((double)clade.getChildCount());
                    stops.add(new Stop(stopCoordinate, (Color)childBranch.getStroke()));

                    if (stopCoordinate < 0.5)
                        stops.add(new Stop(Math.min(0.5, (index + 1) / ((double)clade.getChildCount())), (Color)childBranch.getStroke()));
                    else if (stopCoordinate > 0.5)
                        stops.add(new Stop(Math.min(1.0, (index + 1) / ((double)clade.getChildCount())), (Color)childBranch.getStroke()));
                    else
                        stops.add(new Stop(0.5, (Color)childBranch.getStroke()));

                    index++;
                }

                LinearGradient gradient = new LinearGradient(0.0, 0.0, 0.0, 1.0, true, CycleMethod.NO_CYCLE, stops);
                if (metadata.branchVerticalConnector != null)
                    metadata.branchVerticalConnector.setStroke(gradient);
            }
            
            inCollapsedRegion = originalInCollapsedRegion;
            double nodeY = layoutYScale * (layoutYOffset + metadata.childMidpoint * layoutRowHeight - 0.5 * layoutRowHeight);
            
            // Add Node Marker
            
            Node nodeMarker;
            
            if (inCollapsedRegion)
            {
                nodeMarker = getUpdatedNodeMarker(startX, nodeY, clade, metadata);
                nodeMarker.setVisible(false);
            }
            else
            {
                if (collapsedNode)
                {
                    // EDIT
                    double width0a = layoutXScale * ((isScalingBranchesByLength()) ? (childMetadata[0].branchScaleFactorOfRemainingLength * lengthScaling) : (lineLength * (1.0 + (double) maxRemainingLevels)));
                    nodeMarker = getUpdatedNodeMarker(startX + width0a + rem.scale(clade.getNodeRadius()), nodeY, clade, metadata);
                }
                else
                {
                    nodeMarker = getUpdatedNodeMarker(startX, nodeY, clade, metadata);
                }
                
                nodeMarker.setVisible(true);
            }
            
            if (nodeMarker instanceof Circle)
            {
                Color c = (Color) ((Circle)nodeMarker).getStroke();
                Color transNodeStrokeColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), (inCutRegion ? cutTransparency : 1.0));
                c = (Color) ((Circle)nodeMarker).getFill();
                Color transNodeFillColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), (inCutRegion ? cutTransparency : 1.0));
                ((Circle)nodeMarker).setStroke(transNodeStrokeColor);
                ((Circle)nodeMarker).setFill(transNodeFillColor);
            }
        }
        
        metadata.inCollapsedRegion = inCollapsedRegion;
        
        return new BoundingBox [] {unscaledBoundingBox, scaledBoundingBox};
    }
    
    private double roundDouble(double value, int numDecimalPlaces)
    {
        double factor = Math.pow(10, numDecimalPlaces);
        return Math.round(value * factor) / factor;
    }
    
    @Override
    protected void layoutTreeModel()
    {
        if (getTreeModel() != null)
        {
            if (getTreeModel() != lastModelCached)
                cacheCladeMetadata();

            rightEdge = -1.0;
            BoundingBox [] boundingBoxes = layoutClade(getTreeModel().getRoot(), false, false, -1.0);
            //System.out.println(defaultDimensions);
            setDefaultTreeBounds(boundingBoxes[0]);
            setTreeBounds(boundingBoxes[1]);
            
            // remove these lines?
            layoutXScale = getTreeWidth() / getDefaultTreeWidth();
            layoutYScale = getTreeHeight() / getDefaultTreeHeight();
            
            for (AnnotationPolygon annotationPlaceholder : annotationPlaceholders)
            {
                annotationPlaceholder.recalculateBounds();
            }
            
            //System.out.println("Tree dimensions: " + getTreeWidth() + ", " + getTreeHeight());
        }
    }
    
    @Override
    public void updateLayout()
    {
        layoutTreeModel();
    }
    
    public void updateLayoutWithAnimatedChanges()
    {
        // UNFINISHED
        
        for (Line branchLine : getBranchLines())
            ;
    }
    
    private class BranchLineEntry
    {
        public double startX;
        public double startY;
        public double endX;
        public double endY;
        public Line branchLineObject;
        
        public BranchLineEntry(double startX, double startY, double endX, double endY, Line branchLineObject)
        {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.branchLineObject = branchLineObject;
        }
    }
    
    private class NodeCircleEntry
    {
        public double centerX;
        public double centerY;
        public Circle nodeCircleObject;
        
        public NodeCircleEntry(double centerX, double centerY, Circle nodeCircleObject)
        {
            this.centerX = centerX;
            this.centerY = centerY;
            this.nodeCircleObject = nodeCircleObject;
        }
    }
    
    private class LabelEntry
    {
        public double x;
        public double y;
        public Label labelObject;
        
        public LabelEntry(double x, double y, Label labelObject)
        {
            this.x = x;
            this.y = y;
            this.labelObject = labelObject;
        }
    }
    
    @Override
    public JsonObject setTreeDimensions(double width, double height)
    {
        //System.out.print("[Tree dimensions change] width: " + getTreeWidth() + ", default width: " + getDefaultTreeWidth() + ", layoutXScale: " + layoutXScale + " --> ");
        JsonObject event = super.setTreeDimensions(width, height);
        
        if (event != null && getDefaultTreeWidth() > 0 && getDefaultTreeHeight() > 0)
        {
            layoutXScale = getTreeWidth() / getDefaultTreeWidth();
            layoutYScale = getTreeHeight() / getDefaultTreeHeight();
            //System.out.println("width: " + getTreeWidth() + ", default width: " + getDefaultTreeWidth() + ", layoutXScale: " + layoutXScale);
            updateLayout();
            
            if (selectionMarker != null)
                selectionMarker.recalculateBounds();
        }
        
        return event;
    }
    
    public BooleanProperty leafImagesVisibleProperty()
    {
        return leafImagesVisibleProperty;
    }
    
    public void setLeafImagesVisible(boolean value)
    {
        leafImagesVisibleProperty().set(value);
    }
    
    public boolean isLeafImagesVisible()
    {
        return leafImagesVisibleProperty().get();
    }
    
    public BooleanProperty labelingIntermediateBranchLengthsProperty()
    {
        return labelingIntermediateBranchLengthsProperty;
    }
    
    public void setLabelingIntermediateBranchLengths(boolean value)
    {
        labelingIntermediateBranchLengthsProperty.set(value);
    }
    
    public boolean isLabelingIntermediateBranchLengths()
    {
        return labelingIntermediateBranchLengthsProperty.get();
    }
    
    public BooleanProperty labelingFinalBranchLengthsProperty()
    {
        return labelingFinalBranchLengthsProperty;
    }
    
    public void setLabelingFinalBranchLengths(boolean value)
    {
        labelingFinalBranchLengthsProperty().set(value);
    }
    
    public boolean isLabelingFinalBranchLengths()
    {
        return labelingFinalBranchLengthsProperty().get();
    }
    
    public BooleanProperty labelingFinalBranchLengthsWithLeafNamesProperty()
    {
        return labelingFinalBranchLengthsWithLeafNamesProperty;
    }
    
    public void setLabelingFinalBranchLengthsWithLeafNames(boolean value)
    {
        labelingFinalBranchLengthsWithLeafNamesProperty().set(value);
    }
    
    public boolean isLabelingFinalBranchLengthsWithLeafNames()
    {
        return labelingFinalBranchLengthsWithLeafNamesProperty().get();
    }
    
    public BooleanProperty labelingLeafNodeNamesProperty()
    {
        return labelingLeafNodeNamesProperty;
    }
    
    public void setLabelingLeafNodeNames(boolean value)
    {
        labelingLeafNodeNamesProperty().set(value);
    }
    
    public boolean isLabelingLeafNodeNames()
    {
        return labelingLeafNodeNamesProperty().get();
    }
    
    public BooleanProperty scalingBranchesByLengthProperty()
    {
        return scalingBranchesByLengthProperty;
    }
    
    public void setScalingBranchesByLength(boolean value)
    {
        scalingBranchesByLengthProperty().set(value);
    }
    
    public boolean isScalingBranchesByLength()
    {
        return scalingBranchesByLengthProperty.get();
    }
    
    public BooleanProperty labelingAncestorNamesProperty()
    {
        return labelingAncestorNamesProperty;
    }
    
    public boolean isLabelingAncestorNames()
    {
        return labelingAncestorNamesProperty.get();
    }
    
    public void setLabelingAncestorNames(boolean value)
    {
        this.labelingAncestorNamesProperty.set(value);
    }
    
    public BooleanProperty showingHyperlinksProperty()
    {
        return showingHyperlinksProperty;
    }
    
    public boolean isShowingHyperlinks()
    {
        return showingHyperlinksProperty.get();
    }
    
    public void setShowingHyperlinks(boolean value)
    {
        showingHyperlinksProperty.set(value);
    }
    
    public boolean isInCollapsedRegion(Clade clade)
    {
        return cladeMetadataCache.get(clade).inCollapsedRegion;
    }
    
    @Override
    public void clearModelRender()
    {
        super.clearModelRender();
        clearCache();
        cladeImages.clear();
    }
    
    public ObservableList<AnnotationPolygon> getAnnotationPlaceholders()
    {
        return annotationPlaceholders;
    }
    
    public BooleanProperty showingAnnotationPlaceholdersProperty()
    {
        return showingAnnotationPlaceholdersProperty;
    }
    
    public void setShowingAnnotationPlaceholders(boolean value)
    {
        showingAnnotationPlaceholdersProperty().set(value);
    }
    
    public boolean isShowingAnnotationPlaceholders()
    {
        return showingAnnotationPlaceholdersProperty.get();
    }
    
    public JsonObject selectNodes(List<Clade> nodesToSelect)
    {
        clearSelection();
        
        JsonObject event = PhyloPenIO.createEventRecord(SELECTION);
        PhyloPenIO.attachNodeIdsProperty(event, new ArrayList<>(nodesToSelect));
        
        selectedNodes.addAll(nodesToSelect);
        selectionMarker = new SelectionPolygon();
        int additionIndex = getLayeredModelRenderChildren().addToLayer(selectionLayerIndex, selectionMarker);
        getModelRenderPane().getChildren().add(additionIndex, selectionMarker);
        
        addUndoableEvent(event);
        
        return event;
    }
    
    public JsonObject appendSelectNodes(List<Clade> nodesToSelect)
    {
        if (!getSelectedNodes().isEmpty())
        {
            JsonObject event = PhyloPenIO.createEventRecord(APPEND_SELECTION);
            PhyloPenIO.attachNodeIdsProperty(event, new ArrayList<>(nodesToSelect));
            
            selectedNodes.addAll(nodesToSelect);
            selectionMarker.recalculateBounds();
            
            addUndoableEvent(event);
            
            return event;
        }
        
        return null;
    }
    
    public JsonObject deselectNodes(List<Clade> nodesToDeselect)
    {
        if (!getSelectedNodes().isEmpty())
        {
            JsonObject event = PhyloPenIO.createEventRecord(DESELECTION);
            PhyloPenIO.attachNodeIdsProperty(event, new ArrayList<>(nodesToDeselect));
            
            selectedNodes.removeAll(nodesToDeselect);
            if (selectedNodes.isEmpty())
                clearSelection();
            else
                selectionMarker.recalculateBounds();
            
            addUndoableEvent(event);
            
            return event;
        }
        
        return null;
    }
    
    public JsonObject selectNodesInBounds(double x, double y, double width, double height)
    {
        List<Clade> nodesToSelect = new LinkedList<Clade>();
        Circle nodeCircle;
        Clade clade;
        BoundingBox bounds = new BoundingBox(x, y, width, height);
        
        for (Node nodeMarker : getNodeMarkers())
        {
            if (nodeMarker instanceof Circle && nodeMarker.isVisible())
            {
                nodeCircle = (Circle) nodeMarker;
                if (bounds.contains(nodeCircle.getCenterX(), nodeCircle.getCenterY()))
                {
                    nodesToSelect.add(getClade(nodeCircle));
                }
            }
        }
        
        if (!nodesToSelect.isEmpty())
        {
            return selectNodes(nodesToSelect);
        }
        
        return null;
    }
    
    protected void clearSelection()
    {
        if (!selectedNodes.isEmpty() || selectionMarker != null)
        {
            getModelRenderPane().getChildren().remove(selectionMarker);
            getLayeredModelRenderChildren().removeFromLayer(selectionLayerIndex, selectionMarker);
            selectionMarker.stopAnimation();
            this.selectionMarker = null;
            selectedNodes.clear();
        }
    }
    
    private class AttributeMetadata
    {
        double minVal = Double.MAX_VALUE;
        double maxVal = Double.MIN_VALUE;
        double range = Double.MIN_VALUE;
    }
    
    public StringProperty colorByAttributeProperty()
    {
        return colorByAttribute;
    }
    
    public void setColorByAttribute(String attributeName)
    {
        colorByAttributeProperty().set(attributeName);
    }
    
    public String getColorByAttribute()
    {
        return colorByAttributeProperty().get();
    }
    
    // used when changing colorbyattribute, either intentionally or when an attribute disappears from all nodes
    public void recolorClades()
    {
        AttributeMetadata attrMetadata = (getColorByAttribute() != null) ? attributeMetadataCache.getOrDefault(getColorByAttribute(), null) : null;
        
        if (attrMetadata != null && attrMetadata.range < 1e-11)
            attrMetadata.range = attrMetadata.maxVal - attrMetadata.minVal;

        for (Map.Entry<Clade, CladeMetadata> kv : cladeMetadataCache.entrySet())
        {
            CladeMetadata cladeMetadata = kv.getValue();
            Clade clade = kv.getKey();
            
            if (getColorByAttribute() != null && ((this.getSelectedNodes().size() > 0 && this.getSelectedNodes().contains(clade)) || this.getSelectedNodes().size() == 0))
            {
                if (attrMetadata != null)
                {
                    Double d = clade.getNumericalAttribute(getColorByAttribute());
                    // alternate approach: http://stackoverflow.com/questions/4414673/android-color-between-two-colors-based-on-percentage
                    if (!Double.isNaN(d))
                    {
                        cladeMetadata.fill = getColorByAttributeMinColor().interpolate(getColorByAttributeMaxColor(), (d - attrMetadata.minVal) / attrMetadata.range);
                    }
                    else
                    {
                        cladeMetadata.fill = getColorByAttributeUndefinedColor();
                    }
                }
                else
                {
                    cladeMetadata.fill = getColorByAttributeUndefinedColor();
                }
            }
            else if ((this.getSelectedNodes().size() > 0 && this.getSelectedNodes().contains(clade)) || this.getSelectedNodes().size() == 0)
            {
                cladeMetadata.fill = cladeMetadata.self.getFillColor();
            }
        }
        
        updateLayout();
    }
    
    public List<CladeImageViewer> getCladeImages()
    {
        return cladeImages;
    }
    
    protected void addCladeImage(CladeImageViewer image)
    {
        cladeImages.add(image);
    }
    
    public class SelectionRectangle extends Rectangle
    {
        public Transition animation;
        
        public SelectionRectangle(double x, double y, double width, double height)
        {
            super(x,y,width,height);
            this.setFill(Color.LIGHTYELLOW);
            this.setStroke(Color.BLACK);
            this.getStrokeDashArray().addAll(20.0, 7.0);
            this.setStrokeWidth(3.0);
            
            FadeTransition fadeAnimation = new FadeTransition(Duration.millis(450), this);
            fadeAnimation.setFromValue(1.0f);
            fadeAnimation.setToValue(0.2f);
            fadeAnimation.setCycleCount(Timeline.INDEFINITE);
            fadeAnimation.setAutoReverse(true);
            animation = fadeAnimation;
            animation.play();
        }
        
        public void stopAnimation()
        {
            animation.stop();
        }
    }
    
    public class SelectionPolygon extends SubtreeEnclosingPolygon
    {
        public Transition animation;
        
        public SelectionPolygon()
        {
            recalculateBounds();
            this.setFill(Color.LIGHTYELLOW);
            this.setStroke(Color.BLACK);
            this.getStrokeDashArray().addAll(20.0, 7.0);
            this.setStrokeWidth(3.0);
            
            FadeTransition fadeAnimation = new FadeTransition(Duration.millis(450), this);
            fadeAnimation.setFromValue(1.0f);
            fadeAnimation.setToValue(0.2f);
            fadeAnimation.setCycleCount(Timeline.INDEFINITE);
            fadeAnimation.setAutoReverse(true);
            animation = fadeAnimation;
            animation.play();
        }
        
        @Override
        public List<Clade> getEnclosedNodes()
        {
            return PhyloPenCanvas.this.selectedNodes;
        }
        
        public void stopAnimation()
        {
            animation.stop();
        }
    }
    
    public class AnnotationPolygon extends SubtreeEnclosingPolygon
    {
        private final Annotation annotation;
        private TextField tooltip;
        private boolean fixedDisplay;
        
        public AnnotationPolygon(Annotation annotation)
        {
            this.annotation = annotation;
            fixedDisplay = false;
            recalculateBounds();
            this.setFill(annotation.getColor());
            this.setStroke(Color.BLACK);
            //this.setStrokeWidth(3.0);
            
            if (annotation.getText() != null)
            {
                tooltip = new TextField();
                tooltip.setEditable(false);
                tooltip.setFocusTraversable(false);
                tooltip.getStyleClass().add("annotation-tooltip");
                tooltip.setOpacity(0.8);
                //Tooltip.install(this, new Tooltip(annotation.getText()));
                tooltip.setMinWidth(Region.USE_PREF_SIZE);
                tooltip.setMaxWidth(Region.USE_PREF_SIZE);
                tooltip.textProperty().bind(annotation.textProperty());
                
                this.parentProperty().addListener(new ChangeListener<Parent>()
                {
                    @Override
                    public void changed(ObservableValue<? extends Parent> observable, Parent oldValue, Parent newValue)
                    {
                        if (newValue == null && oldValue instanceof Pane)
                        {
                            Platform.runLater(new Runnable()
                            {
                                public void run()
                                {
                                    ((Pane)oldValue).getChildren().remove(tooltip);
                                }
                            });
                        }
                    }
                });
                
                tooltip.textProperty().addListener(new ChangeListener<String>()
                {
                    @Override
                    public void changed(ObservableValue<? extends String> ob, String o, String n) {
                        // expand the textfield
                        //tooltip.setPrefColumnCount(n.length());
                        tooltip.setPrefWidth(TextUtility.computeTextWidth(tooltip.getFont(), tooltip.getText(), 0.0) + 25.0
                                + tooltip.getPadding().getLeft() + tooltip.getPadding().getRight() + 2.0);
                    }
                });
                
                annotation.colorProperty().addListener(new ChangeListener<Color>()
                {
                    @Override
                    public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue)
                    {
                        if (!AnnotationPolygon.this.getFill().equals(Color.TRANSPARENT))
                            AnnotationPolygon.this.setFill(newValue);
                    }
                });
            }
            
            this.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>()
            {
                @Override
                public void handle(MouseEvent event)
                {
                    if (!fixedDisplay)
                    {
                        AnnotationPolygon.this.setFill(Color.TRANSPARENT);
                        Platform.runLater(new Runnable()
                        {
                            public void run()
                            {
                                if (!PhyloPenCanvas.this.getModelRenderPane().getChildren().contains(tooltip))
                                {
                                    int additionIndex = getLayeredModelRenderChildren().addToLayer(tooltipLayerIndex, tooltip);
                                    PhyloPenCanvas.this.getModelRenderPane().getChildren().add(additionIndex, tooltip);
                                }
                            }
                        });

                        AnnotationPolygon.this.setStrokeWidth(3.0);
                        tooltip.relocate(event.getX() - tooltip.getWidth() - 25.0, event.getY() - tooltip.getHeight() / 2.0);
                    }
                }
            });
                    
            this.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>()
            {
                @Override
                public void handle(MouseEvent event)
                {
                    if (!fixedDisplay)
                    {
                        AnnotationPolygon.this.setFill(annotation.getColor());
                        PhyloPenCanvas.this.getModelRenderPane().getChildren().remove(tooltip);
                        getLayeredModelRenderChildren().removeFromLayer(tooltipLayerIndex, tooltip);
                        AnnotationPolygon.this.setStrokeWidth(1.0);
                        //System.out.println("MouseExited");
                    }
                }
            });
            
            this.addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>()
            {
                @Override
                public void handle(MouseEvent event)
                {
                    //System.out.println("MouseMoved");
                    if (!fixedDisplay)
                        tooltip.relocate(event.getX() - tooltip.getWidth() - 25.0, event.getY() - tooltip.getHeight() / 2.0);
                }
            });
            
            this.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>()
            {
                @Override
                public void handle(MouseEvent event)
                {
                    if (!fixedDisplay)
                    {
                        setFixedDisplay(true);
                    }
                    else
                    {
                        setFixedDisplay(false);
                    }
                }
            });
            
            if (tooltip != null)
            {
                tooltip.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>()
                {
                    @Override
                    public void handle(MouseEvent event)
                    {
                        setFixedDisplay(false);
                    }
                });
            }
        }
        
        @Override
        public List<Clade> getEnclosedNodes()
        {
            return annotation.getNodes();
        }
        
        public void setFixedDisplay(boolean value)
        {
            fixedDisplay = value;
            
            if (fixedDisplay)
            {
                AnnotationPolygon.this.setFill(Color.TRANSPARENT);
                if (!PhyloPenCanvas.this.getModelRenderPane().getChildren().contains(tooltip))
                {
                    int additionIndex = getLayeredModelRenderChildren().addToLayer(tooltipLayerIndex, tooltip);
                    PhyloPenCanvas.this.getModelRenderPane().getChildren().add(additionIndex, tooltip);
                }
                AnnotationPolygon.this.setStrokeWidth(3.0);
                Bounds bounds = AnnotationPolygon.this.getBoundsInLocal();
                tooltip.relocate(bounds.getMinX() + bounds.getWidth() / 2.0 - tooltip.getWidth() / 2.0, bounds.getMinY() + bounds.getHeight() / 2.0 - tooltip.getHeight() / 2.0);
            }
            else
            {
                AnnotationPolygon.this.setFill(annotation.getColor());
                PhyloPenCanvas.this.getModelRenderPane().getChildren().remove(tooltip);
                getLayeredModelRenderChildren().removeFromLayer(tooltipLayerIndex, tooltip);
                AnnotationPolygon.this.setStrokeWidth(1.0);
            }
        }
        
        public boolean isFixedDisplay()
        {
            return fixedDisplay;
        }
        
        public Annotation getAnnotation()
        {
            return annotation;
        }
        
        @Override
        public void recalculateBounds()
        {
            super.recalculateBounds();
            
            if (tooltip != null && tooltip.getParent() == this.getParent())
            {
                Bounds bounds = AnnotationPolygon.this.getBoundsInLocal();
                tooltip.relocate(bounds.getMinX() + bounds.getWidth() / 2.0 - tooltip.getWidth() / 2.0, bounds.getMinY() + bounds.getHeight() / 2.0 - tooltip.getHeight() / 2.0);
            }
            
            boolean visible;
            
            if (!isShowingAnnotationPlaceholders())
            {
                visible = false;
            }
            else
            {
                visible = true;
                
                // determine whether the placeholder should be visible based on collaped regions
                // if at least one node is in a collapsed region, hide the placeholder
                for (Clade node : getEnclosedNodes())
                {
                    if (isInCollapsedRegion(node))
                    {
                        visible = false;
                        break;
                    }
                }
            }
            
            if (!visible)
                setFixedDisplay(false);
        }
    }
    
    public abstract class SubtreeEnclosingPolygon extends Polygon
    {
        public abstract List<Clade> getEnclosedNodes();
        
        public List<Point2D> generatePoints()
        {
            List<Point2D> points = new ArrayList<>();
            List<Clade> nodes = getEnclosedNodes();
            CladeMetadata metadata;
            Clade node;
            Bounds boundingBox;
            int childIndex;
            double padding = 2.5;
            
            for (int i = 0; i < nodes.size(); i++)
            {
                node = nodes.get(i);
                metadata = cladeMetadataCache.get(node);
                boundingBox = metadata.nodeMarker.getBoundsInLocal();
                
                points.add(new Point2D(boundingBox.getMinX() - padding, boundingBox.getMinY() - padding));
                points.add(new Point2D(boundingBox.getMaxX() + padding, boundingBox.getMinY() - padding));
                points.add(new Point2D(boundingBox.getMaxX() + padding, boundingBox.getMaxY() + padding));
                points.add(new Point2D(boundingBox.getMinX() - padding, boundingBox.getMaxY() + padding));
                
                if (node.getChildCount() != 0)
                {
                    childIndex = 0;
                    
                    for (Clade child : node)
                    {
                        if (nodes.contains(child))
                        {
                            points.add(new Point2D(metadata.branchHorizontalConnectors.get(childIndex).getStartX(),
                                    metadata.branchHorizontalConnectors.get(childIndex).getStartY()));
                        }
                        
                        childIndex++;
                    }
                }
            }
            
            return points;
        }
        
        // uses the quickhull algorithm to generate a convex hull
        public void recalculateBounds()
        {
            if (getEnclosedNodes().size() > 0)
            {
                getPoints().clear();

                if (getEnclosedNodes().size() > 1)
                {
                    // vertices in counterclockwise order
                    List<Point2D> convexHullVertices = generateConvexHull(generatePoints());

                    for (Point2D point : convexHullVertices)
                    {
                        getPoints().add(point.getX());
                        getPoints().add(point.getY());
                    }
                }
                else
                {
                    Clade node = getEnclosedNodes().get(0);
                    CladeMetadata metadata = cladeMetadataCache.get(node);
                    Bounds boundingBox = metadata.nodeMarker.getBoundsInLocal();
                    double padding = 2.5;

                    getPoints().addAll(boundingBox.getMinX() - padding, boundingBox.getMinY() - padding);
                    getPoints().addAll(boundingBox.getMaxX() + padding, boundingBox.getMinY() - padding);
                    getPoints().addAll(boundingBox.getMaxX() + padding, boundingBox.getMaxY() + padding);
                    getPoints().addAll(boundingBox.getMinX() - padding, boundingBox.getMaxY() + padding);
                }

                if (isShowingAnnotationPlaceholders())
                {
                    boolean visible = true;

                    // determine whether the placeholder should be visible based on collaped regions
                    // if at least one node is in a collapsed region, hide the placeholder
                    for (Clade node : getEnclosedNodes())
                    {
                        if (isInCollapsedRegion(node))
                        {
                            visible = false;
                            break;
                        }
                    }

                    this.setVisible(visible);
                }
                else
                {
                    this.setVisible(false);
                }
            }
        }
        
        // quickhull
        public List<Point2D> generateConvexHull(List<Point2D> pointSet)
        {
            LinkedList<Point2D> points = new LinkedList<>(pointSet);
            
            Collections.sort(points, new Comparator<Point2D>()
            {
                @Override
                public int compare(Point2D o1, Point2D o2)
                {
                    if (o1.getX() == o2.getX())
                        return (new Double(o1.getY())).compareTo(o2.getY());
                    else
                        return (new Double(o1.getX())).compareTo(o2.getX());
                }
            });
            
            // the extreme points are vertices of the convex hull
            // the first and last of the sorted points must be extreme points
            Point2D extremePoint1 = points.pollFirst();
            Point2D extremePointN = points.pollLast();
            
            // Split the points into two sets divided by the line between the
            // two known extreme points
            LinkedList<Point2D> pointsToRight = new LinkedList<>();
            LinkedList<Point2D> pointsToLeft = new LinkedList<>();
            
            for (Point2D point : points)
            {
                if (isPointOnRightOfLine(point, extremePoint1, extremePointN))
                    pointsToRight.add(point);
                else
                    pointsToLeft.add(point);
            }
            
            // in counterclockwise order
            List<Point2D> convexHullVertices = new ArrayList<>();
            convexHullVertices.add(extremePoint1);
            convexHullVertices.addAll(quickhull(pointsToRight, extremePoint1, extremePointN));
            convexHullVertices.add(extremePointN);
            convexHullVertices.addAll(quickhull(pointsToLeft, extremePointN, extremePoint1));
            return convexHullVertices;
        }
        
        private boolean isPointOnRightOfLine(Point2D point, Point2D lineEndpoint1, Point2D lineEndpoint2)
        {
            return (lineEndpoint1.getX() * lineEndpoint2.getY()
                        + point.getX() * lineEndpoint1.getY()
                        + lineEndpoint2.getX() * point.getY()
                        - point.getX() * lineEndpoint2.getY()
                        - lineEndpoint2.getX() * lineEndpoint1.getY()
                        - lineEndpoint1.getX() * point.getY() < 0.0);
        }
        
        @SuppressWarnings("empty-statement")
        private List<Point2D> quickhull(List<Point2D> points, Point2D lineEndpoint1, Point2D lineEndpoint2)
        {
            if (points.isEmpty())
            {
                return new LinkedList<>();
            }
            else
            {
                Point2D farthestPoint; // farthest point from the dividing line
                Point2D point;
                double farthestDistanceSquared, distanceSquared;
                
                Iterator<Point2D> pointIterator = points.iterator();
                farthestPoint = pointIterator.next();
                farthestDistanceSquared = distanceSquaredFromLine(farthestPoint, lineEndpoint1, lineEndpoint2);
                
                while (pointIterator.hasNext())
                {
                    point = pointIterator.next();
                    distanceSquared = distanceSquaredFromLine(point, lineEndpoint1, lineEndpoint2);
                    
                    if (distanceSquared > farthestDistanceSquared)
                    {
                        farthestPoint = point;
                        farthestDistanceSquared = distanceSquared;
                    }
                }
                
                pointIterator = points.iterator();
                while (pointIterator.next() != farthestPoint);
                pointIterator.remove();
                
                pointIterator = points.iterator();
                LinkedList<Point2D> pointsToRight = new LinkedList<>();
                LinkedList<Point2D> pointsToLeft = new LinkedList<>();
                
                while (pointIterator.hasNext())
                {
                    point = pointIterator.next();
                    
                    if (isPointOnRightOfLine(point, lineEndpoint1, farthestPoint))
                        pointsToRight.add(point);
                    else if (isPointOnRightOfLine(point, farthestPoint, lineEndpoint2))
                        pointsToLeft.add(point);
                }
                
                // all the points outside the triangle formed by lineEndpoint1,
                // farthestPoint, and lineEndpoint2 are kept
                // those inside the triangle can't be vertices of the convex hull
                
                List<Point2D> convexHullVertices = new ArrayList<>();
                convexHullVertices.addAll(quickhull(pointsToRight, lineEndpoint1, farthestPoint));
                convexHullVertices.add(farthestPoint);
                convexHullVertices.addAll(quickhull(pointsToLeft, farthestPoint, lineEndpoint2));
                return convexHullVertices;
            }
        }
        
        private double distanceSquaredFromLine(Point2D point, Point2D lineEndpoint1, Point2D lineEndpoint2)
        {
            Point2D u = new Point2D(point.getX() - lineEndpoint1.getX(), point.getY() - lineEndpoint1.getY());
            Point2D v = new Point2D(lineEndpoint2.getX() - lineEndpoint1.getX(), lineEndpoint2.getY() - lineEndpoint1.getY());
            double uDotV = u.dotProduct(v);
            return u.dotProduct(u) - ((uDotV * uDotV) / v.dotProduct(v));
        }
    }
    
    /**
     *
     * @author awehrer
     */
    public class LassoRecognitionProcedure extends AbstractInkGestureRecognitionProcedure
    {
        @Override
        public InkGestureRecognizer.InkGestureRecognitionProcedureResult performRecognize(Collection<AugmentedInkStroke> inkStrokes, double scale, Object...additionalArgs)
        {
            if (inkStrokes.size() == 1)
            {
                AugmentedInkStroke stroke = inkStrokes.iterator().next();
                BoundingBox bounds = stroke.getBoundingBox();
                List<StylusPoint> points = stroke.getResampledPoints();

                if (stroke.getNumSelfIntersections() > 1 || points.size() < 2)
                    return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);

                if (stroke.getNumSelfIntersections() == 1)
                {
                    Pair<Integer, Integer> indexPair = stroke.getSelfIntersectionIndexPairs().get(0);
                    System.out.println(indexPair);
                    points = new ArrayList<>(stroke.getResampledPoints().subList(indexPair.getKey(), indexPair.getValue() + 1));
                    bounds = InkUtility.getBoundingBox(points);
                }
                else if (InkUtility.distance(points.get(0), points.get(points.size() - 1)) > 100.0) // no self-intersections; endpoints should be close
                {
                    return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
                }
                
                List<Point2D> selectablePoints = new ArrayList<>();
                Circle vertexCircle;

                for (Node nodeMarker : PhyloPenCanvas.this.getNodeMarkers())
                {
                    if (nodeMarker instanceof Circle)
                    {
                        vertexCircle = (Circle) nodeMarker;
                        if (vertexCircle.isVisible())
                            selectablePoints.add(new Point2D(innerToOuterX(vertexCircle.getCenterX()), innerToOuterY(vertexCircle.getCenterY())));
                    }
                }

                // find one point that is within this polygon
                Point2D testPoint;

                for (Object data : selectablePoints)
                {
                    if (data instanceof Point2D)
                    {
                        testPoint = (Point2D) data;

                        if (InkUtility.isPointInPolygon((List)points, testPoint, bounds))
                        {
                            Polygon shape = new Polygon();

                            for (StylusPoint point : points)
                            {
                                shape.getPoints().add(point.getX());
                                shape.getPoints().add(point.getY());
                            }

                            return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(true, shape);
                        }
                    }
                }
            }

            return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
        }

        @Override
        public String getGestureIdentifier()
        {
            return "Lasso";
        }
    }
    
    private class DollarNRectangleRecognitionProcedure extends AbstractInkGestureRecognitionProcedure
    {
        @Override
        protected InkGestureRecognizer.InkGestureRecognitionProcedureResult performRecognize(Collection<AugmentedInkStroke> inkStrokes, double scale, Object...additionalArgs)
        {
            if (additionalArgs != null)
            {
                String result = (String) additionalArgs[0];
                double confidenceValue = (Double) additionalArgs[1];
                
                if (result.equals("Rectangle") && confidenceValue >= getSymbolRecognitionConfidenceThreshold())
                {
                    AugmentedInkStroke stroke = inkStrokes.iterator().next();
                    List<StylusPoint> p = stroke.getResampledPoints();

                    double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY, minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

                    List<Integer> cornerIndices = new ArrayList<>(stroke.getCornerIndices());

                    for (int i = 0; i < 4; i++)
                    {
                        if (p.get(cornerIndices.get(i)).getX() < minX) minX = p.get(cornerIndices.get(i)).getX();
                        if (p.get(cornerIndices.get(i)).getX() > maxX) maxX = p.get(cornerIndices.get(i)).getX();
                        if (p.get(cornerIndices.get(i)).getY() < minY) minY = p.get(cornerIndices.get(i)).getY();
                        if (p.get(cornerIndices.get(i)).getY() > maxY) maxY = p.get(cornerIndices.get(i)).getY();
                    }

                    Rectangle identifiedShape = new Rectangle(minX, minY, maxX - minX, maxY - minY);
                    identifiedShape.setFill(Color.TRANSPARENT);
                    identifiedShape.setStroke(stroke.getDrawingAttributes().getColor());
                    identifiedShape.setStrokeWidth(stroke.getDrawingAttributes().getStrokeWidth());
                    return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(true, identifiedShape);
                }
            }

            return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
        }
        
        @Override
        public String getGestureIdentifier()
        {
            return "Rectangle";
        }
    }
    
    public class DollarNTriangleRecognitionProcedure extends AbstractInkGestureRecognitionProcedure
    {
        @Override
        public InkGestureRecognizer.InkGestureRecognitionProcedureResult performRecognize(Collection<AugmentedInkStroke> inkStrokes, double scale, Object...additionalArgs)
        {
            if (additionalArgs != null)
            {
                String result = (String) additionalArgs[0];
                double confidenceValue = (Double) additionalArgs[1];
                
                if (result.equals("Triangle") && confidenceValue >= getSymbolRecognitionConfidenceThreshold())
                {
                    AugmentedInkStroke stroke = inkStrokes.iterator().next();
                    List<StylusPoint> p = stroke.getResampledPoints();
                    List<Integer> cornerIndices = new ArrayList<>(stroke.getCornerIndices());

                    // if 5 corners and the first and last corners are close together, merge them
                    if (cornerIndices.size() == 4)
                    {
                        double distanceBetweenEndpoints = InkUtility.distance(p.get(cornerIndices.get(0)), p.get(cornerIndices.get(3)));

                        if (distanceBetweenEndpoints <= 25.0)
                        {
                            cornerIndices.remove(cornerIndices.get(cornerIndices.size() - 1));
                        }
                        else if (stroke.getNumSelfIntersections() > 0)
                        {
                            // if we have self-intersection data, see if the lines of the first and last corners intersect and
                            // overshoot each other by some distance; if less, remove the ends, and merge at the
                            // intersection point

                            for (Pair<Integer, Integer> intersectionIndexPair : stroke.getSelfIntersectionIndexPairs())
                            {
                                if (intersectionIndexPair.getKey() >= 0 && intersectionIndexPair.getKey() <= cornerIndices.get(1) && intersectionIndexPair.getValue() >= cornerIndices.get(2)
                                        && InkUtility.distance(p.get(cornerIndices.get(0)), p.get(intersectionIndexPair.getKey())) / InkUtility.distance(p.get(cornerIndices.get(0)), p.get(cornerIndices.get(1))) < 0.2
                                        && InkUtility.distance(p.get(intersectionIndexPair.getValue()), p.get(cornerIndices.get(3))) / InkUtility.distance(p.get(cornerIndices.get(2)), p.get(cornerIndices.get(3))) < 0.2)
                                {
                                    cornerIndices.set(0, intersectionIndexPair.getKey());
                                    cornerIndices.remove(3);
                                    break;
                                }
                            }
                        }
                    }

                    Polygon identifiedShape = new Polygon();
                    identifiedShape.getPoints().addAll(new Double[]
                    {
                        p.get(cornerIndices.get(0)).getX(), p.get(cornerIndices.get(0)).getY(),
                        p.get(cornerIndices.get(1)).getX(), p.get(cornerIndices.get(1)).getY(),
                        p.get(cornerIndices.get(2)).getX(), p.get(cornerIndices.get(2)).getY(),
                    });

                    identifiedShape.setFill(Color.TRANSPARENT);
                    identifiedShape.setStroke(stroke.getDrawingAttributes().getColor());
                    identifiedShape.setStrokeWidth(stroke.getDrawingAttributes().getStrokeWidth());
                    return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(true, identifiedShape);
                }
            }

            return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
        }
        
        @Override
        public String getGestureIdentifier()
        {
            return "Triangle";
        }
    }
    
    public class DollarNVerticalLineRecognitionProcedure extends AbstractInkGestureRecognitionProcedure
    {
        @Override
        public InkGestureRecognizer.InkGestureRecognitionProcedureResult performRecognize(Collection<AugmentedInkStroke> inkStrokes, double scale, Object...additionalArgs)
        {
            if (additionalArgs != null)
            {
                String result = (String) additionalArgs[0];
                double confidenceValue = (Double) additionalArgs[1];
                
                if (result.equals("Vertical slash") && confidenceValue >= getSymbolRecognitionConfidenceThreshold())
                    return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(true);
            }

            return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
        }
        
        @Override
        public String getGestureIdentifier()
        {
            return "Vertical slash";
        }
    }
    
    public class DollarNHorizontalLineRecognitionProcedure extends AbstractInkGestureRecognitionProcedure
    {
        @Override
        public InkGestureRecognizer.InkGestureRecognitionProcedureResult performRecognize(Collection<AugmentedInkStroke> inkStrokes, double scale, Object...additionalArgs)
        {
            if (additionalArgs != null)
            {
                String result = (String) additionalArgs[0];
                double confidenceValue = (Double) additionalArgs[1];
                
                if (result.equals("Horizontal line") && confidenceValue >= getSymbolRecognitionConfidenceThreshold())
                    return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(true);
            }

            return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
        }
        
        @Override
        public String getGestureIdentifier()
        {
            return "Horizontal line";
        }
    }
    
    public class DollarNLRecognitionProcedure extends AbstractInkGestureRecognitionProcedure
    {
        @Override
        public InkGestureRecognizer.InkGestureRecognitionProcedureResult performRecognize(Collection<AugmentedInkStroke> inkStrokes, double scale, Object...additionalArgs)
        {
            if (additionalArgs != null)
            {
                String result = (String) additionalArgs[0];
                double confidenceValue = (Double) additionalArgs[1];
                
                if (result.equals("L") && confidenceValue >= getSymbolRecognitionConfidenceThreshold())
                    return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(true);
            }

            return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
        }
        
        @Override
        public String getGestureIdentifier()
        {
            return "L";
        }
    }
    
    public class DollarNReverseLRecognitionProcedure extends AbstractInkGestureRecognitionProcedure
    {
        @Override
        public InkGestureRecognizer.InkGestureRecognitionProcedureResult performRecognize(Collection<AugmentedInkStroke> inkStrokes, double scale, Object...additionalArgs)
        {
            if (additionalArgs != null)
            {
                String result = (String) additionalArgs[0];
                double confidenceValue = (Double) additionalArgs[1];
                
                if (result.equals("Reverse L") && confidenceValue >= getSymbolRecognitionConfidenceThreshold())
                    return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(true);
            }

            return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
        }
        
        @Override
        public String getGestureIdentifier()
        {
            return "Reverse L";
        }
    }
    
    public class DollarNLeftSquareBracketRecognitionProcedure extends AbstractInkGestureRecognitionProcedure
    {
        @Override
        public InkGestureRecognizer.InkGestureRecognitionProcedureResult performRecognize(Collection<AugmentedInkStroke> inkStrokes, double scale, Object...additionalArgs)
        {
            if (additionalArgs != null)
            {
                String result = (String) additionalArgs[0];
                double confidenceValue = (Double) additionalArgs[1];
                
                if (result.equals("Left square bracket") && confidenceValue >= getSymbolRecognitionConfidenceThreshold())
                {
                    double minX = Double.POSITIVE_INFINITY;
                    double maxX = Double.NEGATIVE_INFINITY;
                    double minY = Double.POSITIVE_INFINITY;
                    double maxY = Double.NEGATIVE_INFINITY;
                    
                    AugmentedInkStroke stroke = inkStrokes.iterator().next();
                    List<Integer> cornerIndices = new ArrayList<>(stroke.getCornerIndices());
                    List<StylusPoint> p = stroke.getResampledPoints();

                    for (int i = 0; i < 4; i++)
                    {
                        if (p.get(cornerIndices.get(i)).getX() < minX) minX = p.get(cornerIndices.get(i)).getX();
                        if (p.get(cornerIndices.get(i)).getX() > maxX) maxX = p.get(cornerIndices.get(i)).getX();
                        if (p.get(cornerIndices.get(i)).getY() < minY) minY = p.get(cornerIndices.get(i)).getY();
                        if (p.get(cornerIndices.get(i)).getY() > maxY) maxY = p.get(cornerIndices.get(i)).getY();
                    }

                    Polyline identifiedShape = new Polyline(maxX, minY, minX, minY, minX, minY, maxX, maxY);
                    identifiedShape.setFill(Color.TRANSPARENT);
                    identifiedShape.setStroke(stroke.getDrawingAttributes().getColor());
                    identifiedShape.setStrokeWidth(stroke.getDrawingAttributes().getStrokeWidth());
                    return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(true, identifiedShape);
                }
            }

            return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
        }
        
        @Override
        public String getGestureIdentifier()
        {
            return "Left square bracket";
        }
    }
    
    public class DollarNCutAndReattachRecognitionProcedure extends AbstractInkGestureRecognitionProcedure
    {
        @Override
        public InkGestureRecognizer.InkGestureRecognitionProcedureResult performRecognize(Collection<AugmentedInkStroke> inkStrokes, double scale, Object...additionalArgs)
        {
            if (additionalArgs != null && additionalArgs.length > 2)
            {
                String result1 = (String) additionalArgs[2], result2 = (String) additionalArgs[4];
                double confidenceValue1 = (Double) additionalArgs[3], confidenceValue2 = (Double) additionalArgs[5];
                
                if (result1.equals("Vertical slash") && confidenceValue1 >= getSymbolRecognitionConfidenceThreshold()
                        && (result2.equals("L") || result2.equals("Reverse L") || result2.equals("Horizontal line")) && confidenceValue2 >= getSymbolRecognitionConfidenceThreshold())
                    return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(true);
            }

            return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
        }
        
        @Override
        public String getGestureIdentifier()
        {
            return "Cut and reattach";
        }
    }
    
    public class DollarNXRecognitionProcedure extends AbstractInkGestureRecognitionProcedure
    {
        @Override
        public InkGestureRecognizer.InkGestureRecognitionProcedureResult performRecognize(Collection<AugmentedInkStroke> inkStrokes, double scale, Object...additionalArgs)
        {
            if (additionalArgs != null)
            {
                String result = (String) additionalArgs[0];
                double confidenceValue = (Double) additionalArgs[1];
                
                if (result.equals("X") && confidenceValue >= getSymbolRecognitionConfidenceThreshold())
                    return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(true);
            }

            return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
        }
        
        @Override
        public String getGestureIdentifier()
        {
            return "X";
        }
    }
    
    public final class PhyloPenInkGestureRecognizer extends DollarNInkGestureRecognizer
    {
        public PhyloPenInkGestureRecognizer()
        {
            addGestureRecognitionProcedure(new TapRecognitionProcedure());
            addGestureRecognitionProcedure(new DollarNRectangleRecognitionProcedure());
            addGestureRecognitionProcedure(new DollarNTriangleRecognitionProcedure());
            addGestureRecognitionProcedure(new DollarNVerticalLineRecognitionProcedure());
            addGestureRecognitionProcedure(new DollarNHorizontalLineRecognitionProcedure());
            addGestureRecognitionProcedure(new DollarNLRecognitionProcedure());
            addGestureRecognitionProcedure(new DollarNReverseLRecognitionProcedure());
            addGestureRecognitionProcedure(new DollarNXRecognitionProcedure());
            addGestureRecognitionProcedure(new DollarNCutAndReattachRecognitionProcedure());
            addGestureRecognitionProcedure(new DollarNLeftSquareBracketRecognitionProcedure());
            addGestureRecognitionProcedure(new LassoRecognitionProcedure());
        }
    }
    
    public static interface CladeCutListener
    {
        public void cladeCut(Clade cutClade, JsonObject cladeJson, Clade oldParent, int oldChildIndex, long timestamp);
    }
    
    public static interface CladeAdditionListener
    {
        public void cladeAdded(Clade newClade, JsonObject cladeJson, Clade parent, int childIndex, long timestamp);
    }
    
    public static interface CladeRotationListener
    {
        public void cladesRotated(Collection<Clade> clades, long timestamp);
    }
    
    public void setMouseDisabled(boolean disabled)
    {
        this.mouseDisabled = disabled;
    }
    
    public boolean isMouseDisabled()
    {
        return mouseDisabled;
    }
    
    // returns true if event handled
    @Override
    protected boolean performUndo(JsonObject event)
    {
        if (eventTranscribing && eventWriter != null)
        {
            JsonObject undoEvent = PhyloPenIO.createEventRecord(PhyloPenIO.UNDO);
            undoEvent.add("event", event);
            eventWriter.queueEventForFileWrite(undoEvent);
        }
        
        if (this.getCutClade() != null)
            this.undoCutClade();
        
        if (!super.performUndo(event))
        {
            List<Color> oldColors;
            List<Clade> nodes;
            List<NodeConnection> connections;
            Annotation annotation;
            JsonArray array;

            switch (event.get("typeId").getAsInt())
            {
                case SELECTION: // node selection
                    this.deselectNodes(PhyloPenIO.getNodes(this, event));
                    break;
                case DESELECTION: // clear selection
                    this.selectNodes(PhyloPenIO.getNodes(this, event));
                    break;
                case NODE_RECOLOR: // recolor nodes
                    JsonArray nodeColorArray = event.get("oldColors").getAsJsonArray();
                    oldColors = new ArrayList<>();
                    nodes = getNodes(event);

                    for (JsonElement element : nodeColorArray)
                        oldColors.add(PhyloPenIO.toColor(element.getAsJsonObject()));

                    if (event.get("recolorType").getAsInt() == 0)
                    {
                        for (int i = 0; i < nodes.size(); i++)
                            this.colorFillOfNode(nodes.get(i), oldColors.get(i));
                    }
                    else
                    {
                        for (int i = 0; i < nodes.size(); i++)
                            this.colorOutlineOfNode(nodes.get(i), oldColors.get(i));
                    }
                    break;
                case BRANCH_RECOLOR: // recolor branches
                    JsonArray branchColorArray = event.get("oldColors").getAsJsonArray();
                    oldColors = new ArrayList<>();
                    connections = PhyloPenIO.getConnections(event, this);

                    for (JsonElement element : branchColorArray)
                        oldColors.add(PhyloPenIO.toColor(element.getAsJsonObject()));

                    for (int i = 0; i < connections.size(); i++)
                        this.colorBranch(connections.get(i).getParent(), connections.get(i).getChildIndex(), oldColors.get(i));

                    break;
                case BRANCH_WIDTH_CHANGE: // change branch width
                    JsonArray branchWidthArray = event.get("oldWidths").getAsJsonArray();
                    List<Double> oldWidths = new ArrayList<>();
                    connections = PhyloPenIO.getConnections(event, this);

                    for (JsonElement element : branchWidthArray)
                        oldWidths.add(element.getAsJsonPrimitive().getAsDouble());

                    for (int i = 0; i < connections.size(); i++)
                        this.changeWidthOfBranch(connections.get(i).getParent(), connections.get(i).getChildIndex(), oldWidths.get(i));
                    break;
                case NODE_RADIUS_CHANGE: // change node radius
                    JsonArray nodeRadiusArray = event.get("oldRadii").getAsJsonArray();
                    List<Double> oldRadii = new ArrayList<>(nodeRadiusArray.size());
                    nodes = PhyloPenIO.getNodes(this, event);

                    for (JsonElement element : nodeRadiusArray)
                        oldRadii.add(element.getAsJsonPrimitive().getAsDouble());

                    for (int i = 0; i < nodes.size(); i++)
                        this.setNodeRadius(nodes.get(i), oldRadii.get(i));

                    this.updateLayout();
                    break;
                case ANNOTATION:
                    Iterator<Annotation> annotationIterator = getAnnotations().iterator();
                    Annotation a;
                    while (annotationIterator.hasNext())
                    {
                        a = annotationIterator.next();
                        if (a.getId() == event.get("annotationJson").getAsJsonObject().get("annotationId").getAsInt())
                        {
                            for (AnnotationPolygon placeholder : this.getAnnotationPlaceholders())
                            {
                                if (placeholder.getAnnotation() == a)
                                    placeholder.setFixedDisplay(true);
                            }
                            annotationIterator.remove();
                            break;
                        }
                    }
                    break;
                case CLADE_COLLAPSE:
                    this.expandClade(this.getTreeModel().getClade(event.get("nodeId").getAsInt()));
                    break;
                case CLADE_EXPAND: // expand clade
                    this.collapseClade(this.getTreeModel().getClade(event.get("nodeId").getAsInt()));
                    break;
                case SCALE_BRANCHES: // scalingBranchesByLength
                    this.setScalingBranchesByLength(!event.get("value").getAsBoolean());
                    break;
                case LABEL_LEAF_NODE_NAMES: // labelingLeafNodeNames
                    this.setLabelingLeafNodeNames(!event.get("value").getAsBoolean());
                    break;
                case LABEL_INTERMEDIATE_BRANCH_LENGTHS: // labelingIntermediateBranchLengths
                    this.setLabelingIntermediateBranchLengths(!event.get("value").getAsBoolean());
                    break;
                case LABEL_FINAL_BRANCH_LENGTHS_W_LEAF_NAMES: // labelingFinalBranchLengthsWithLeafNames
                    this.setLabelingFinalBranchLengthsWithLeafNames(!event.get("value").getAsBoolean());
                    break;
                case LABEL_FINAL_BRANCH_LENGTHS: // labelingFinalBranchLengths
                    this.setLabelingFinalBranchLengths(!event.get("value").getAsBoolean());
                    break;
                case LEAF_IMAGES_VISIBLE: // leafImagesVisible
                    this.setLeafImagesVisible(!event.get("value").getAsBoolean());
                    break;
                case SHOW_ANNOTATION_PLACEHOLDERS: // showingAnnotationPlaceholders
                    this.setShowingAnnotationPlaceholders(!event.get("value").getAsBoolean());
                    break;
                case COLOR_BY_ATTRIBUTE_MIN_COLOR_CHANGE: // colorByAttributeMinColor
                    this.setColorByAttributeMinColor(PhyloPenIO.getColor(event, "oldColor"));
                    break;
                case COLOR_BY_ATTRIBUTE_MAX_COLOR_CHANGE: // colorByAttributeMaxColor
                    this.setColorByAttributeMaxColor(PhyloPenIO.getColor(event, "oldColor"));
                    break;
                case COLOR_BY_ATTRIBUTE_UNDEFINED_COLOR_CHANGE: // colorByAttributeUndefinedColor
                    this.setColorByAttributeUndefinedColor(PhyloPenIO.getColor(event, "oldColor"));
                    break;
                case COLOR_BY_ATTRIBUTE: // colorByAttribute
                    this.setColorByAttribute(event.get("oldName").getAsString());
                    break;
                case TREE_RESIZE:
                    this.setTreeDimensions(event.get("oldWidth").getAsDouble(), event.get("oldHeight").getAsDouble());
                    break;
                case CLADE_CUT_REATTACH:
                    this.cutAndReattachClade(
                            this.getTreeModel().getClade(event.get("nodeId").getAsInt()),
                            this.getTreeModel().getClade(event.get("oldParentId").getAsInt()),
                            event.get("oldChildIndex").getAsInt());
                    if (event.has("annotationsRemoved"))
                    {
                        array = event.get("annotationsRemoved").getAsJsonArray();
                        for (JsonElement element : array)
                            this.getAnnotations().add(Annotation.fromJson(element.getAsJsonObject(), this));
                    }
                    break;
                case CLADE_CUT:
                    this.addClade(
                            event.get("cladeJson").getAsJsonObject(),
                            this.getTreeModel().getClade(event.get("oldParentId").getAsInt()),
                            event.get("oldChildIndex").getAsInt());
                    if (event.has("annotationsRemoved"))
                    {
                        array = event.get("annotationsRemoved").getAsJsonArray();
                        for (JsonElement element : array)
                            this.getAnnotations().add(Annotation.fromJson(element.getAsJsonObject(), this));
                    }
                    break;
                case CLADE_ADD:
                    this.removeClade(this.getTreeModel().getClade(event.get("nodeId").getAsInt()));
                    break;
                case ANNOTATION_REMOVED:
                    this.getAnnotations().add(Annotation.fromJson(event.get("annotationJson").getAsJsonObject(), this));
                    break;
                case CLADE_ROTATE:
                    this.reverseChildOrdering(PhyloPenIO.getNodes(this, event));
                    break;
                case ANNOTATION_EDIT:
                    annotation = getAnnotationById(event.get("annotationId").getAsInt());
                    annotation.setText(event.get("oldText").getAsString());
                    annotation.setColor(PhyloPenIO.getColor(event, "oldColor"));
                    break;
                case APPEARANCE_RESET:
                    undoResetAppearance(event);
                    break;
                case CLADE_INSERT:
                    undoCladeInsertion(this.getTreeModel().getClade(event.get("nodeId").getAsInt()),
                                    event.has("parentId") ? this.getTreeModel().getClade(event.get("parentId").getAsInt()) : null,
                                    event.has("childId") ? this.getTreeModel().getClade(event.get("childId").getAsInt()) : null);
                    break;
                case LABEL_ANCESTOR_NAMES:
                    this.setLabelingAncestorNames(!event.get("value").getAsBoolean());
                    break;
                case SHOW_HYPERLINKS:
                    this.setShowingHyperlinks(!event.get("value").getAsBoolean());
                    break;
                default:
                    return false;
            }
        }
        
        return true;
    }
    
    public Annotation getAnnotationById(int id)
    {
        for (Annotation annotation : getAnnotations())
        {
            if (annotation.getId() == id)
                return annotation;
        }
        
        return null;
    }
    
    private void undoResetAppearance(JsonObject event)
    {
        List<Clade> nodes = PhyloPenIO.getNodes(this, event);
        List<Color> oldNodeOutlineColors, oldNodeFillColors, oldBranchColors;
        List<NodeConnection> connections = PhyloPenIO.getConnections(event, this);
        List<Double> oldNodeRadii, oldBranchWidths;
        
        JsonArray jsonArray = event.get("oldBranchColors").getAsJsonArray();
        oldBranchColors = new ArrayList<>(jsonArray.size());
        for (JsonElement element : jsonArray)
            oldBranchColors.add(PhyloPenIO.toColor(element.getAsJsonObject()));
        
        jsonArray = event.get("oldBranchWidths").getAsJsonArray();
        oldBranchWidths = new ArrayList<>(jsonArray.size());
        for (JsonElement element : jsonArray)
            oldBranchWidths.add(element.getAsJsonPrimitive().getAsDouble());
        
        for (int i = 0; i < connections.size(); i++)
        {
            this.colorBranch(connections.get(i).getParent(), connections.get(i).getChildIndex(), oldBranchColors.get(i));
            this.changeWidthOfBranch(connections.get(i).getParent(), connections.get(i).getChildIndex(), oldBranchWidths.get(i));
        }
        
        jsonArray = event.get("oldNodeOutlineColors").getAsJsonArray();
        oldNodeOutlineColors = new ArrayList<>();
        for (JsonElement element : jsonArray)
            oldNodeOutlineColors.add(PhyloPenIO.toColor(element.getAsJsonObject()));
        
        jsonArray = event.get("oldNodeFillColors").getAsJsonArray();
        oldNodeFillColors = new ArrayList<>();
        for (JsonElement element : jsonArray)
            oldNodeFillColors.add(PhyloPenIO.toColor(element.getAsJsonObject()));
        
        jsonArray = event.get("oldNodeRadii").getAsJsonArray();
        oldNodeRadii = new ArrayList<>();
        for (JsonElement element : jsonArray)
            oldNodeRadii.add(element.getAsJsonPrimitive().getAsDouble());
        
        for (int i = 0; i < nodes.size(); i++)
        {
            this.colorOutlineOfNode(nodes.get(i), oldNodeOutlineColors.get(i));
            this.colorFillOfNode(nodes.get(i), oldNodeFillColors.get(i));
            this.setNodeRadius(nodes.get(i), oldNodeRadii.get(i));
        }
        
        this.updateLayout();
    }
    
    private List<Clade> getNodes(JsonObject event)
    {
        return PhyloPenIO.getNodes(this, event);
    }
    
    public boolean isInitializingTree()
    {
        return initializingTree;
    }
    
    protected void setInitializingTree(boolean value)
    {
        initializingTree = value;
    }
    
    @Override
    public void addUndoableEvent(JsonObject event)
    {
        if (!isInitializingTree())
        {
            if (event.get("typeId").getAsInt() != PhyloPenIO.NAVIGATION)
                super.addUndoableEvent(event);
            eventWriter.queueEventForFileWrite(event);
        }
    }
    
    public void prepareToClose()
    {
        if (eventTranscribing && eventWriter != null)
            eventWriter.stop();
    }
    
    private class AnnotationEditDialog extends Dialog<Boolean>
    {
        private Annotation annotation;
        private final TextField annotationTextField;
        private final ColorPicker colorPicker;

        public AnnotationEditDialog(Annotation annotation, boolean newAnnotation)
        {
            this.annotation = annotation;
            this.setTitle("Annotation");

            getDialogPane().getButtonTypes().add(ButtonType.OK);
            getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

            Label enterAnnotationLabel = new Label("Text:");
            Label colorAnnotationLabel = new Label("Color:");

            annotationTextField = new TextField();
            annotationTextField.setPrefColumnCount(22);
            annotationTextField.setPromptText("Enter text for annotation");

            if (annotation.getText() != null)
                annotationTextField.setText(annotation.getText());

            colorPicker = new ColorPicker(annotation.getColor());

            HBox annotationTextPane = new HBox(4);
            annotationTextPane.getChildren().addAll(enterAnnotationLabel, annotationTextField);
            HBox annotationColorPane = new HBox(4);
            annotationColorPane.getChildren().addAll(colorAnnotationLabel, colorPicker);

            VBox mainContentPane = new VBox(10);
            mainContentPane.getChildren().addAll(annotationTextPane, annotationColorPane);

            getDialogPane().setContent(mainContentPane);

            this.setResultConverter(new Callback<ButtonType, Boolean>()
            {
                @Override
                public Boolean call(ButtonType param)
                {
                    if (!param.equals(ButtonType.CANCEL))
                    {
                        if (newAnnotation)
                        {
                            annotation.setText(annotationTextField.getText());
                            annotation.setColor(colorPicker.getValue());
                        }
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            });

            final Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event ->
            {
                if (annotationTextField.getText().equals(""))
                    event.consume();
            });

            this.setOnShown(new EventHandler<DialogEvent>()
            {
                public void handle(DialogEvent event)
                {
                    Platform.runLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            AnnotationEditDialog.this.annotationTextField.requestFocus();
                        }
                    });
                }
            });
        }

        public Annotation getAnnotation()
        {
            return annotation;
        }
        
        public TextField getAnnotationTextField()
        {
            return annotationTextField;
        }
        
        public ColorPicker getColorPicker()
        {
            return colorPicker;
        }
    }
    
    private class CladeEditDialog extends Dialog<Boolean>
    {
        private Clade clade;
        private final TextField labelTextField;

        public CladeEditDialog(Clade clade, boolean newClade)
        {
            this.clade = clade;
            this.setTitle("Clade");

            getDialogPane().getButtonTypes().add(ButtonType.OK);
            getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

            Label enterAnnotationLabel = new Label("Label:");

            labelTextField = new TextField();
            labelTextField.setPrefColumnCount(22);
            labelTextField.setPromptText("Enter label name");

            if (!newClade)
                labelTextField.setText(clade.getLabel());

            HBox annotationTextPane = new HBox(4);
            annotationTextPane.getChildren().addAll(enterAnnotationLabel, labelTextField);

            VBox mainContentPane = new VBox(10);
            mainContentPane.getChildren().addAll(annotationTextPane);

            getDialogPane().setContent(mainContentPane);

            this.setResultConverter(new Callback<ButtonType, Boolean>()
            {
                @Override
                public Boolean call(ButtonType param)
                {
                    if (!param.equals(ButtonType.CANCEL))
                    {
                        if (newClade)
                        {
                            clade.setLabel(labelTextField.getText());
                            updateLayout();
                        }
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            });

            final Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event ->
            {
                if (labelTextField.getText().equals(""))
                    event.consume();
            });

            this.setOnShown(new EventHandler<DialogEvent>()
            {
                public void handle(DialogEvent event)
                {
                    Platform.runLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            CladeEditDialog.this.labelTextField.requestFocus();
                        }
                    });
                }
            });
        }

        public Clade getClade()
        {
            return clade;
        }
    }
    
    private class EventFileWriter
    {
        private final List<JsonObject> outputQueue;
        private String saveFile;
        private Thread writeThread;
        private FileWriter fileWriter;
        private boolean running;

        public EventFileWriter(String treeFilename)
        {
            saveFile = "log\\" + treeFilename + "_log.txt";
            outputQueue = new LinkedList<>();
            running = false;
        }
        
        public void queueEventForFileWrite(JsonObject event)
        {
            synchronized (outputQueue)
            {
                outputQueue.add(event);
            }
        }

        public List<JsonObject> getOutputQueue()
        {
            return outputQueue;
        }

        public void start(String user)
        {
            JsonObject sessionEvent = PhyloPenIO.createEventRecord(PhyloPenIO.NEW_SESSION);
            sessionEvent.addProperty("userID", user);

            fileWriter = null;

            try
            {
                fileWriter = new FileWriter(saveFile, true); // appends to file
            }
            catch (FileNotFoundException e)
            {
                System.out.println("Cannot transcribe events");
                return;
            }
            catch (IOException e)
            {
                System.out.println("Cannot transcribe events");
                return;
            }

            writeThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    while (fileWriter != null)
                    {
                        writeOutputQueueToFile();

                        if (running)
                        {
                            try{Thread.sleep(10000);}catch(InterruptedException e){}
                        }
                    }
                    
                    System.out.println("Thread finished");
                }


            });

            running = true;
            writeThread.start();
            
            PhyloPenCanvas.this.addListener(new InkGestureListener()
            {
                @Override
                public void gestureRecognized(InkGesture gesture)
                {
                    if (gesture == null)
                    {
                        // failed gesture recognition
                        queueEventForFileWrite(PhyloPenIO.createEventRecord(PhyloPenIO.FAILED_INK_RECOGNITION));
                    }
                }
            });
        }

        public void stop()
        {
            if (fileWriter != null)
            {
                writeOutputQueueToFile();

                try
                {
                    fileWriter.flush();
                    fileWriter.close();
                    System.out.println("Closed record file: " + saveFile);
                }
                catch (IOException e)
                {
                    System.out.println("Error while closing record file");
                }

                synchronized (outputQueue)
                {
                    running = false;
                    fileWriter = null;
                }
            }

            if (writeThread != null && writeThread.isAlive())
            {
                writeThread.interrupt();
            }
        }

        public void writeOutputQueueToFile()
        {
            Gson gson = new Gson();
            String jsonString;
            Iterator<JsonObject> iterator;
            JsonObject event;

            if (!outputQueue.isEmpty() && fileWriter != null)
            {
                synchronized (outputQueue)
                {
                    iterator = outputQueue.iterator();

                    while (iterator.hasNext())
                    {
                        event = iterator.next();
                        jsonString = gson.toJson(event);

                        try
                        {
                            fileWriter.write(jsonString + "," + System.getProperty("line.separator"));
                        }
                        catch (IOException e)
                        {
                            System.out.println("Cannot write to record file");
                        }

                        iterator.remove();
                    }
                }

                try
                {
                    fileWriter.flush();
                }
                catch (IOException e)
                {
                    System.out.println("Cannot flush to record file");
                }
            }
        }
    }
}
