/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package phylopen;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.transform.Affine;
import static phylopen.PhyloPenIO.TREE_RESIZE;
import phylopen.utility.LayeredList;
import phylopen.utility.ink.InkCanvas;
import phylopen.utility.ink.StylusEvent;

/**
 * 
 * @author awehrer
 */
public abstract class PhyloTreeInkCanvas extends InkCanvas
{
    private final ObjectProperty<BoundingBox> defaultTreeBounds;
    private final ObjectProperty<BoundingBox> treeBounds;
    
    private final ObjectProperty<PhylogeneticTree> treeModel;
    private final ObservableList<Node> nodeMarkers;
    private final ObservableList<Line> branchLines;
    private final ObservableList<Node> labels;
    private final Pane modelRenderPane;
    private final LayeredList<Node> layeredModelRenderChildren;
    private final ObservableList<Annotation> annotations;
    
    private JsonObject lastTouchChangeEvent;
    private double startModelRenderScaleOnTouch;
    private double startModelRenderXOnTouch;
    private double startModelRenderYOnTouch;
    
    private final DoubleProperty modelRenderTranslationX;
    private final DoubleProperty modelRenderTranslationY;
    private final DoubleProperty modelRenderScale;
    
    protected int nodeMarkerLayerIndex;
    protected int branchLineLayerIndex;
    protected int labelLayerIndex;
    
    private final Affine manipulationTransform;
    
    private boolean touched;
    private boolean touchDisabled;
    private boolean ignoreInertia;
    
    public PhyloTreeInkCanvas()
    {
        modelRenderPane = new Pane();
        //modelRenderPane.setBackground(new Background(new BackgroundFill(Color.ORANGERED, new CornerRadii(0), new Insets(0))));
        getChildren().add(modelRenderPane);
        
        nodeMarkers = FXCollections.observableList(new ArrayList<Node>());
        branchLines = FXCollections.observableList(new ArrayList<Line>());
        labels = FXCollections.observableList(new ArrayList<Node>());
        layeredModelRenderChildren = new LayeredList<>(3);
        annotations = FXCollections.observableList(new LinkedList<Annotation>());
        touchDisabled = false;
        
        touched = false;
        
        lastTouchChangeEvent = null;
        startModelRenderScaleOnTouch = Double.NaN;
        startModelRenderXOnTouch = Double.NaN;
        startModelRenderYOnTouch = Double.NaN;
        
        /*
        * Graphics layer order from bottom to top:
        * 0. branch lines
        * 1. node markers
        * 2. labels
        * 
        * Child classes are able to override this ordering, potentially adding additional layers.
        * The layered list is used to calculate the index at which to place new children in the model
        * render pane.
        */
        
        nodeMarkerLayerIndex = 1;
        branchLineLayerIndex = 0;
        labelLayerIndex = 2;
        
        nodeMarkers.addListener(new ListChangeListener<Node>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Node> change)
            {
                while (change.next())
                {
                    if (change.wasAdded() && !change.wasRemoved())
                    {
                        int startIndex = change.getFrom();
                        List<? extends Node> childrenToAdd = new ArrayList<>(change.getAddedSubList());
                        ObservableList<Node> modelRenderPaneChildren = PhyloTreeInkCanvas.this.modelRenderPane.getChildren();
                        int additionIndex = PhyloTreeInkCanvas.this.layeredModelRenderChildren.addAllToLayer(nodeMarkerLayerIndex, startIndex, (List)childrenToAdd);
                        modelRenderPaneChildren.addAll(additionIndex, childrenToAdd);
                    }
                    else if (!change.wasAdded() && change.wasRemoved())
                    {
                        List<? extends Node> childrenToRemove = new ArrayList<>(change.getRemoved());
                        PhyloTreeInkCanvas.this.modelRenderPane.getChildren().removeAll(childrenToRemove);
                        PhyloTreeInkCanvas.this.layeredModelRenderChildren.removeAllFromLayer(nodeMarkerLayerIndex, (List)childrenToRemove);
                    }
                    else
                        System.out.println("Change not handled.");
                }
            }
        });
        
        branchLines.addListener(new ListChangeListener<Line>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Line> change)
            {
                while (change.next())
                {
                    if (change.wasAdded() && !change.wasRemoved())
                    {
                        int startIndex = change.getFrom();
                        List<? extends Line> childrenToAdd = new ArrayList<>(change.getAddedSubList());
                        ObservableList<Node> modelRenderPaneChildren = PhyloTreeInkCanvas.this.modelRenderPane.getChildren();
                        int additionIndex = PhyloTreeInkCanvas.this.layeredModelRenderChildren.addAllToLayer(branchLineLayerIndex, startIndex, (List)childrenToAdd);
                        modelRenderPaneChildren.addAll(additionIndex, childrenToAdd);
                    }
                    else if (!change.wasAdded() && change.wasRemoved())
                    {
                        List<? extends Line> childrenToRemove = new ArrayList<>(change.getRemoved());
                        PhyloTreeInkCanvas.this.modelRenderPane.getChildren().removeAll(childrenToRemove);
                        PhyloTreeInkCanvas.this.layeredModelRenderChildren.removeAllFromLayer(branchLineLayerIndex, (List)childrenToRemove);
                    }
                    else
                        System.out.println("Change not handled.");
                }
            }
        });
        
        labels.addListener(new ListChangeListener<Node>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Node> change)
            {
                while (change.next())
                {
                    if (change.wasAdded() && !change.wasRemoved())
                    {
                        int startIndex = change.getFrom();
                        List<? extends Node> childrenToAdd = new ArrayList<>(change.getAddedSubList());
                        ObservableList<Node> modelRenderPaneChildren = PhyloTreeInkCanvas.this.modelRenderPane.getChildren();
                        int additionIndex = PhyloTreeInkCanvas.this.layeredModelRenderChildren.addAllToLayer(labelLayerIndex, startIndex, (List)childrenToAdd);
                        modelRenderPaneChildren.addAll(additionIndex, childrenToAdd);
                    }
                    else if (!change.wasAdded() && change.wasRemoved())
                    {
                        List<? extends Node> childrenToRemove = new ArrayList<>(change.getRemoved());
                        PhyloTreeInkCanvas.this.modelRenderPane.getChildren().removeAll(childrenToRemove);
                        PhyloTreeInkCanvas.this.layeredModelRenderChildren.removeAllFromLayer(labelLayerIndex, (List)childrenToRemove);
                    }
                    else
                        System.out.println("Change not handled.");
                }
            }
        });
        
        treeModel = new SimpleObjectProperty<>();
        defaultTreeBounds = new SimpleObjectProperty<>();
        treeBounds = new SimpleObjectProperty<>();
        
        modelRenderTranslationX = new SimpleDoubleProperty(0.0);
        modelRenderTranslationY = new SimpleDoubleProperty(0.0);
        modelRenderScale = new SimpleDoubleProperty(1.0);
        
        manipulationTransform = new Affine();
        getModelRenderPane().getTransforms().add(manipulationTransform);
        modelRenderScale.bindBidirectional(manipulationTransform.mxxProperty());
        modelRenderScale.bindBidirectional(manipulationTransform.myyProperty());
        modelRenderTranslationX.bindBidirectional(manipulationTransform.txProperty());
        modelRenderTranslationY.bindBidirectional(manipulationTransform.tyProperty());
        
        TouchStatusHandler touchStatusHandler = new TouchStatusHandler();
        
        addEventHandler(TouchEvent.TOUCH_PRESSED, touchStatusHandler);
        addEventHandler(TouchEvent.TOUCH_RELEASED, touchStatusHandler);
        addEventHandler(ScrollEvent.ANY, new TranslationHandler());
        addEventHandler(ZoomEvent.ANY, new ZoomHandler());
        
        addEventHandler(StylusEvent.ANY, new EventHandler<StylusEvent>()
        { 
            @Override
            public void handle(StylusEvent event)
            {
                if (event.getEventType().equals(StylusEvent.STYLUS_DOWN))
                {
                    ignoreInertia = true;
                    touched = false;
                    setTouchDisabled(true);
                }
                else if (event.getEventType().equals(StylusEvent.STYLUS_UP))
                {
                    Platform.runLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setTouchDisabled(false);
                        }
                    });
                }
            }
        });
    }
    
    public boolean isTouched()
    {
        return touched;
    }
    
    protected void setTouched(boolean value)
    {
        this.touched = value;
    }
    
    // returns true if event handled
    @Override
    protected boolean performUndo(JsonObject event)
    {
        if (!super.performUndo(event) && event.get("typeId").getAsInt() == PhyloPenIO.NAVIGATION)
        {
            setModelRenderTranslationX(event.get("oldValueX").getAsDouble());
            setModelRenderTranslationY(event.get("oldValueY").getAsDouble());
            setModelRenderScale(event.get("oldValueScale").getAsDouble());
            return true;
        }
        
        return false;
    }
    
    private class TouchStatusHandler implements EventHandler<TouchEvent>
    {
        @Override
        public void handle(TouchEvent event)
        {
            if (event.getEventType().equals(TouchEvent.TOUCH_PRESSED) && !isStylusDown())
            {
                touched = true;
                ignoreInertia = false;
                //System.out.println("TOUCHED");
                startModelRenderScaleOnTouch = getModelRenderScale();
                startModelRenderXOnTouch = getModelRenderTranslationX();
                startModelRenderYOnTouch = getModelRenderTranslationY();
            }
            else if (event.getEventType().equals(TouchEvent.TOUCH_RELEASED))
            {
                if (startModelRenderScaleOnTouch != getModelRenderScale() || startModelRenderXOnTouch != getModelRenderTranslationX() || startModelRenderYOnTouch != getModelRenderTranslationY())
                {
                    lastTouchChangeEvent = PhyloPenIO.createEventRecord(PhyloPenIO.NAVIGATION);
                    lastTouchChangeEvent.addProperty("oldValueX", startModelRenderXOnTouch);
                    lastTouchChangeEvent.addProperty("translationX", getModelRenderTranslationX());
                    lastTouchChangeEvent.addProperty("oldValueY", startModelRenderYOnTouch);
                    lastTouchChangeEvent.addProperty("translationY", getModelRenderTranslationY());
                    lastTouchChangeEvent.addProperty("oldValueScale", startModelRenderScaleOnTouch);
                    lastTouchChangeEvent.addProperty("scale", getModelRenderScale());
                    
                    addUndoableEvent(lastTouchChangeEvent);
                }
                
                touched = false;
            }
        }
    }
    
    private class TranslationHandler implements EventHandler<ScrollEvent>
    {
        private static final double MOUSE_WHEEL_ZOOM_DELTA = 0.1;
        
        @Override
        public void handle(final ScrollEvent event)
        {
            if (!event.getEventType().equals(ScrollEvent.SCROLL_STARTED))
            {
                if (event.getEventType().equals(ScrollEvent.SCROLL) && event.getTotalDeltaX() == 0.0 && event.getTotalDeltaY() == 0.0 && event.getDeltaY() != 0.0 && AppResources.getOptions().isMouseMode()) // mouse scroll wheel event
                {
                    //System.out.println("Mouse scroll wheel event triggered " + event.getDeltaX() + ", " + event.getDeltaY());
                    Platform.runLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            ZoomEvent.fireEvent(event.getTarget(), new ZoomEvent(event.getSource(), event.getTarget(), ZoomEvent.ZOOM, event.getX(), event.getY(), event.getScreenX(), event.getScreenY(), event.isShiftDown(), event.isControlDown(), event.isAltDown(), event.isMetaDown(), event.isDirect(), false, (event.getDeltaY() > 0.0 ? (1.0 + MOUSE_WHEEL_ZOOM_DELTA) : (1.0 - MOUSE_WHEEL_ZOOM_DELTA)), 0.0, event.getPickResult()));
                        }
                    });
                }
                else if (!isTouchDisabled()) // touch event
                {
                    if (!event.isInertia() || (!ignoreInertia && event.isInertia()))
                        translateModelRender(event.getDeltaX() / getModelRenderScale(), event.getDeltaY() / getModelRenderScale());
                }
            }
            
            event.consume();
        }
    }
    
    private class ZoomHandler implements EventHandler<ZoomEvent>
    {
        @Override
        public void handle(ZoomEvent event)
        {
            if (!isTouchDisabled() || !isTouched())
            {
                if (event.getEventType().equals(ZoomEvent.ZOOM))
                {
                    Point2D point = getModelRenderPane().parentToLocal(event.getX(), event.getY());
                    manipulationTransform.appendScale(event.getZoomFactor(), event.getZoomFactor(), point.getX(), point.getY());
                    //System.out.println(point.getX() + ", " + point.getY());

                }
                else
                {
                    //System.out.println(event.getEventType());
                }
            }
            
            event.consume();
        }
    }
    
    protected LayeredList<Node> getLayeredModelRenderChildren()
    {
        return layeredModelRenderChildren;
    }
    
    protected Affine getManipulationTransform()
    {
        return manipulationTransform;
    }
    
    public DoubleProperty modelRenderTanslationXProperty()
    {
        return modelRenderTranslationX;
    }
    
    public DoubleProperty modelRenderTanslationYProperty()
    {
        return modelRenderTranslationY;
    }
    
    public void setModelRenderTranslationX(double x)
    {
        modelRenderTranslationX.set(x);
    }
    
    public void setModelRenderTranslationY(double y)
    {
        modelRenderTranslationY.set(y);
    }
    
    public double getModelRenderTranslationX()
    {
        return modelRenderTranslationX.get();
    }
    
    public double getModelRenderTranslationY()
    {
        return modelRenderTranslationY.get();
    }
    
    public void translateModelRender(double deltaX, double deltaY)
    {
        manipulationTransform.appendTranslation(deltaX, deltaY);
    }
    
    public void translateModelRenderTo(double x, double y)
    {
        manipulationTransform.setTx(x);
        manipulationTransform.setTy(y);
    }
    
    public DoubleProperty modelRenderScaleProperty()
    {
        return modelRenderScale;
    }
    
    public void setModelRenderScale(double scale)
    {
        modelRenderScale.set(scale);
    }
    
    public double getModelRenderScale()
    {
        return modelRenderScale.get();
    }
    
    public void clearModelRender()
    {
        nodeMarkers.clear();
        branchLines.clear();
        labels.clear();
        defaultTreeBounds.set(null);
        treeBounds.set(null);
    }
    
    public void clearTreeModel()
    {
        if (treeModel.get() != null)
        {
            clearModelRender();
            treeModel.set(null);
        }
    }
    
    protected ObservableList<Node> getNodeMarkers()
    {
        return nodeMarkers;
    }
    
    protected ObservableList<Line> getBranchLines()
    {
        return branchLines;
    }
    
    protected ObservableList<Node> getLabels()
    {
        return labels;
    }
    
    protected Pane getModelRenderPane()
    {
        return modelRenderPane;
    }
    
    public double getTreeWidth()
    {
        return (treeBounds.get() == null ? 0.0 : treeBounds.get().getWidth());
    }
    
    public double getTreeHeight()
    {
        return (treeBounds.get() == null ? 0.0 : treeBounds.get().getHeight());
    }
    
    protected void setTreeBounds(BoundingBox treeBounds)
    {
        this.treeBounds.set(treeBounds);
    }
    
    public BoundingBox getTreeBounds()
    {
        return treeBounds.get();
    }
    
    public final ObjectProperty<BoundingBox> treeBoundsProperty()
    {
        return treeBounds;
    }
    
    public double getDefaultTreeWidth()
    {
        return (defaultTreeBounds.get() == null ? 0.0 : defaultTreeBounds.get().getWidth());
    }
    
    public double getDefaultTreeHeight()
    {
        return (defaultTreeBounds.get() == null ? 0.0 : defaultTreeBounds.get().getHeight());
    }
    
    protected void setDefaultTreeBounds(BoundingBox defaultTreeBounds)
    {
        this.defaultTreeBounds.set(defaultTreeBounds);
    }
    
    public BoundingBox getDefaultTreeBounds()
    {
        return defaultTreeBounds.get();
    }
    
    public final ObjectProperty<BoundingBox> defaultTreeBoundsProperty()
    {
        return defaultTreeBounds;
    }
    
    public JsonObject changeTreeWidthAndHeight(double widthDelta, double heightDelta)
    {
        return setTreeDimensions(getTreeWidth() + widthDelta, getTreeHeight() + heightDelta);
    }
    
    public JsonObject changeTreeWidth(double delta)
    {
        return changeTreeWidthAndHeight(delta, 0.0);
    }
    
    public JsonObject changeTreeHeight(double delta)
    {
        return changeTreeWidthAndHeight(0.0, delta);
    }
    
    public JsonObject resetTreeWidth()
    {
        return setTreeDimensions(getDefaultTreeWidth(), getTreeHeight());
    }

    public JsonObject resetTreeHeight()
    {
        return setTreeDimensions(getTreeWidth(), getDefaultTreeHeight());
    }

    public JsonObject resetTreeDimensions()
    {
        return setTreeDimensions(getDefaultTreeWidth(), getDefaultTreeHeight());
    }
    
    public void resetTranslation()
    {
        translateModelRenderTo(0.0, 0.0);
    }
    
    public void resetScale()
    {
        setModelRenderScale(1.0);
    }
    
    public JsonObject setTreeDimensions(double width, double height)
    {
        if (width <= 0)
            width = 1;
        
        if (height <= 0)
            height = 1;
        
        if (width != getTreeWidth() || height != getTreeHeight())
        {
            BoundingBox oldBounds = getTreeBounds();
            setTreeBounds(new BoundingBox(oldBounds.getMinX(), oldBounds.getMinY(), width, height));
            
            JsonObject event = PhyloPenIO.createEventRecord(TREE_RESIZE);
            event.addProperty("oldWidth", oldBounds.getWidth());
            event.addProperty("oldHeight", oldBounds.getHeight());
            event.addProperty("width", getTreeBounds().getWidth());
            event.addProperty("height", getTreeBounds().getHeight());
            
            addUndoableEvent(event);
            
            return event;
        }
        
        return null;
    }
    
    protected abstract void layoutTreeModel();
    
    protected abstract void updateLayout();
    
    public void setTreeModel(PhylogeneticTree tree)
    {
        if (treeModel.get() != tree)
        {
            clearTreeModel();
            treeModel.set(tree);
            layoutTreeModel();
            resetTreeDimensions();
            resetTranslation();
        }
    }
    
    public final PhylogeneticTree getTreeModel()
    {
        return treeModel.get();
    }
    
    public final ObjectProperty<PhylogeneticTree> treeModelProperty()
    {
        return treeModel;
    }
    
    public ObservableList<Annotation> getAnnotations()
    {
        return annotations;
    }
    
    public void setTouchDisabled(boolean disabled)
    {
        this.touchDisabled = disabled;
    }
    
    public boolean isTouchDisabled()
    {
        return touchDisabled;
    }
}
