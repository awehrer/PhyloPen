/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package phylopen.utility.ink;

import com.google.gson.JsonObject;
import java.awt.Toolkit;
import phylopen.utility.ink.recognition.InkGestureRecognizer;
import phylopen.utility.ink.recognition.InkGesture;
import phylopen.utility.ink.recognition.InkGestureListener;
import phylopen.utility.ink.recognition.AugmentedInkStroke;
import phylopen.utility.ink.recognition.ScratchOutRecognizer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import jpen.PButton;
import jpen.PButtonEvent;
import jpen.PKind;
import jpen.PLevel;
import jpen.PLevelEvent;
import jpen.PenManager;
import jpen.event.PenAdapter;
import phylopen.UserEventStack;
import phylopen.utility.NodePenOwner;

/**
 *
 * @author awehrer
 */
public class InkCanvas extends Pane
{
    private final ObservableList<InkStroke> inkStrokes;
    private final NodePenOwner penOwner;
    private final PenManager penManager;
    private boolean stylusInRange;
    private boolean stylusInAir;
    private InkStroke currentStroke;
    private long currentStrokeStartTimestamp;
    private final ObjectProperty<InkStrokeDrawingAttributes> drawingAttributes;
    
    private InkGestureRecognizer processInkGestureRecognizer;
    private InkGestureRecognizer gestureRecognizer;
    private final ArrayList<InkGestureListener> gestureListeners;
    private ScratchOutRecognizer scratchOutRecognizer;
    private boolean inkDisabled;
    private boolean stylusDown;
    
    /** convenience properties for stylus event handlers */
    private final ObjectProperty<EventHandler<? super StylusEvent>> onStylusDown;
    private final ObjectProperty<EventHandler<? super StylusEvent>> onStylusUp;
    private final ObjectProperty<EventHandler<? super StylusEvent>> onStylusMoved;
    private final ObjectProperty<EventHandler<? super StylusEvent>> onStylusInRange; // not quite finished.
    private final ObjectProperty<EventHandler<? super StylusEvent>> onStylusOutOfRange; // not quite finished.
    
    private UserEventStack undoableEvents;
    private boolean undoing;
    
    public InkCanvas()
    {
        setMaxHeight(Double.MAX_VALUE);
        setMaxWidth(Double.MAX_VALUE);
        
        inkStrokes = FXCollections.observableList(new LinkedList<InkStroke>());
        penOwner = new NodePenOwner(this);
        penManager = new PenManager(penOwner);
        stylusInRange = false;
        stylusInAir = true;
        inkDisabled = false;
        stylusDown = false;
        
        gestureListeners = new ArrayList<>();
        
        drawingAttributes = new SimpleObjectProperty<>();
        onStylusDown = new SimpleObjectProperty<>();
        onStylusUp = new SimpleObjectProperty<>();
        onStylusMoved = new SimpleObjectProperty<>();
        onStylusInRange = new SimpleObjectProperty<>();
        onStylusOutOfRange = new SimpleObjectProperty<>();
        
        undoableEvents = new UserEventStack();
        undoing = false;
        
        drawingAttributes.set(new InkStrokeDrawingAttributes());
        
        //setStyle(getStyle() + "-fx-background-color: rgb(210,210,210);");
        
        penManager.pen.addListener(new PenAdapter()
        {
            @Override
            public void penLevelEvent(PLevelEvent event)
            {
                //System.out.println(event.pen.getKind().getType().equals(PKind.Type.STYLUS));
                if (event.pen.getKind().getType().equals(PKind.Type.STYLUS))
                {
                    if (!stylusInRange)
                    {
                        stylusInRange = true;
                        InkCanvas.this.fireEvent(new StylusEvent(StylusEvent.STYLUS_IN_RANGE, null, stylusInAir, event.getDeviceTime()));
                    }
                    
                    double trueHorizontalLines = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
                    double scaledHorizontalLines = Screen.getPrimary().getBounds().getHeight();
                    double dpiScaleFactor = trueHorizontalLines / scaledHorizontalLines;

                    double x = Double.NaN;
                    double y = Double.NaN;
                    float pressure = Float.NaN, tiltX = Float.NaN,
                            tiltY = Float.NaN, sidePressure = Float.NaN,
                            rotation = Float.NaN;
                    //System.out.println(x);
                    long timestamp = event.getDeviceTime();
                            
                    // Level types: X, Y, PRESSURE, TILT_X, TILT_Y, SIDE_PRESSURE, ROTATION, CUSTOM
                    for (PLevel level : event.levels)
                    {
                        switch (level.getType())
                        {
                            case X:
                                x = (double) level.value / dpiScaleFactor;
                                break;
                            case Y:
                                y = (double) level.value / dpiScaleFactor;
                                break;
                            case PRESSURE:
                                pressure = level.value;
                                break;
                            case TILT_X:
                                tiltX = level.value;
                                break;
                            case TILT_Y:
                                tiltY = level.value;
                                break;
                            case SIDE_PRESSURE:
                                sidePressure = level.value;
                                break;
                            case ROTATION:
                                rotation = level.value;
                                break;
                        }
                    }
                    
                    //System.out.println(x);

                    if (!Double.isNaN(x) && !Double.isNaN(y))
                    {
                        final StylusPoint newStylusPoint = new StylusPoint(x, y, pressure, tiltX, tiltY, sidePressure, rotation, timestamp);
                        final InkStroke stroke = currentStroke;
                        
                        if (!stylusInAir && currentStroke != null && !isInkDisabled())
                        {
                            Platform.runLater(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    stroke.getStylusPoints().add(newStylusPoint);
                                }
                            });
                            
                            /*if (stroke.getStylusPoints().size() == 0)
                                System.out.println("First: " + newStylusPoint.getTimestamp());*/
                        }
                        
                        final StylusEvent stylusEvent = new StylusEvent(StylusEvent.STYLUS_MOVED, newStylusPoint, stylusInAir, event.getDeviceTime());
                        Platform.runLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                InkCanvas.this.fireEvent(stylusEvent);
                            }
                        });
                    }
                }
            }
            
            @Override
            public void penButtonEvent(PButtonEvent event)
            {
                if (event.pen.getKind().getType().equals(PKind.Type.STYLUS))
                {
                    boolean penDown = false;
                    boolean penUp = false;

                    if (event.button.getType().equals(PButton.Type.ON_PRESSURE))
                    {
                        stylusInAir = !event.button.value;
                        penDown = event.button.value;
                        penUp = !event.button.value;
                    }

                    if (!stylusInRange)
                    {
                        stylusInRange = true;
                        InkCanvas.this.fireEvent(new StylusEvent(StylusEvent.STYLUS_IN_RANGE, null, stylusInAir, event.getDeviceTime()));
                    }

                    if (penDown && !isInkDisabled())
                    {
                        currentStroke = new InkStroke(getDrawingAttributes());
                        currentStrokeStartTimestamp = event.getDeviceTime();
                        final InkStroke strokeToAdd = currentStroke;
                        
                        stylusDown = true;
                        final StylusEvent stylusEvent = new StylusEvent(StylusEvent.STYLUS_DOWN, null, stylusInAir, event.getDeviceTime());
                        Platform.runLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                InkCanvas.this.getChildren().add(strokeToAdd);
                                InkCanvas.this.fireEvent(stylusEvent);
                            }
                        });
                        
                        //System.out.println("DOWN: " + event.getDeviceTime());
                    }
                    else if (penUp && currentStroke != null && !isInkDisabled())
                    {
                        /*if (currentStroke.getStylusPoints().size() > 1)
                        {
                            System.out.println("Last: " + currentStroke.getStylusPoints().get(currentStroke.getStylusPoints().size() - 1).getTimestamp());
                        }*/
                        //System.out.println("UP: " + event.getDeviceTime());
                        
                        final InkStroke strokeToAdd = currentStroke;
                        final int duration = (int)(event.getDeviceTime() - currentStrokeStartTimestamp);
                        //System.out.println(duration);
                        stylusDown = false;
                        final StylusEvent stylusEvent = new StylusEvent(StylusEvent.STYLUS_UP, null, stylusInAir, event.getDeviceTime());
                        
                        Platform.runLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                InkCanvas.this.fireEvent(stylusEvent);
                                InkCanvas.this.getChildren().remove(strokeToAdd);
                                InkUtility.removeDuplicatePoints(strokeToAdd.getStylusPoints());
                                
                                if ((isScratchOutErasingEnabled() && !scratchOutRecognizer.performIfScratchOut(strokeToAdd, getInkStrokes())) || !isScratchOutErasingEnabled())
                                {
                                    if (getGestureRecognizer() == null)
                                    {
                                        inkStrokes.add(new InkStroke(strokeToAdd.getStylusPoints(), getDrawingAttributes(), duration));
                                    }
                                    else
                                    {
                                        AugmentedInkStroke augStroke = new AugmentedInkStroke(strokeToAdd.getStylusPoints(), getDrawingAttributes(), duration);
                                        
                                        if (getProcessInkGestureRecognizer() == null)
                                        {
                                            inkStrokes.add(augStroke);
                                            InkCanvas.this.processInkForGestureRecognition();
                                        }
                                        else
                                        {
                                            if (isProcessInkSignal(augStroke))
                                                InkCanvas.this.processInkForGestureRecognition();
                                            else
                                                inkStrokes.add(augStroke);
                                        }
                                    }
                                }
                            }
                        });
                        
                        currentStroke = null;
                    }
                }
            }
        });
        
        
        inkStrokes.addListener(new ListChangeListener<InkStroke>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends InkStroke> change)
            {
                while (change.next())
                {
                    if (change.wasAdded() && !change.wasRemoved())
                    {
                        InkCanvas.this.getChildren().addAll(change.getAddedSubList());
                        
                        /*if (isScratchOutErasingEnabled())
                        {
                            final ArrayList<InkStroke> addedSublist = new ArrayList<>(change.getAddedSubList());
                            Platform.runLater(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    for (InkStroke stroke : addedSublist)
                                    {
                                        System.out.println(getInkStrokes().size());
                                        if (!scratchOutRecognizer.performIfScratchOut(stroke, getInkStrokes()))
                                            processInkIfStrokeIsSignal(stroke);
                                        else
                                            InkCanvas.this.getChildren().addAll(addedSublist);
                                    }
                                }
                            });
                        }
                        else
                        {
                            InkCanvas.this.getChildren().addAll(change.getAddedSubList());
                        }*/
                    }
                    else if (!change.wasAdded() && change.wasRemoved())
                    {
                        InkCanvas.this.getChildren().removeAll(new ArrayList<>(change.getRemoved()));
                        //System.out.println("Strokes removed. Remaining: " + InkCanvas.this.getChildren().size());
                    }
                    else
                        System.out.println("Change not handled.");
                }
            }
        });
    }
    
    public void setScratchOutErasingEnabled(boolean value)
    {
        if (value)
            scratchOutRecognizer = new ScratchOutRecognizer();
        else
            scratchOutRecognizer = null;
    }
    
    public boolean isScratchOutErasingEnabled()
    {
        return scratchOutRecognizer != null;
    }
    
    protected boolean isProcessInkSignal(InkStroke stroke)
    {
        InkGesture inkGesture = null;
        
        if (getProcessInkGestureRecognizer() != null)
        {
            ArrayList<AugmentedInkStroke> strokeList = new ArrayList<>(1);
            strokeList.add((AugmentedInkStroke)stroke);
            inkGesture = getProcessInkGestureRecognizer().analyzeForGesture(strokeList, 1.0);
        }
        
        return inkGesture != null;
    }
    
    public void processInkForGestureRecognition()
    {
        if (getGestureRecognizer() != null)
        {
            InkGesture inkGesture = getGestureRecognizer().analyzeForGesture((ObservableList)getInkStrokes(), 1.0);
            
            for (InkGestureListener listener : gestureListeners)
            {
                listener.gestureRecognized(inkGesture);
            }
        }
    }
    
    public void setProcessInkGestureRecognizer(InkGestureRecognizer gestureRecognizer)
    {
        this.processInkGestureRecognizer = gestureRecognizer;
    }
    
    public InkGestureRecognizer getProcessInkGestureRecognizer()
    {
        return processInkGestureRecognizer;
    }
    
    public void setGestureRecognizer(InkGestureRecognizer gestureRecognizer)
    {
        if (this.gestureRecognizer == null && gestureRecognizer != null)
        {
            ListIterator<InkStroke> iterator = inkStrokes.listIterator();
            InkStroke stroke;
            
            while (iterator.hasNext())
            {
                stroke = iterator.next();
                
                if (!(stroke instanceof AugmentedInkStroke))
                {
                    iterator.remove();
                    iterator.add(new AugmentedInkStroke(stroke));
                }
            }
        }
        
        this.gestureRecognizer = gestureRecognizer;
    }
    
    public InkGestureRecognizer getGestureRecognizer()
    {
        return gestureRecognizer;
    }
    
    public void addListener(InkGestureListener listener)
    {
        gestureListeners.add(listener);
    }
    
    public void removeListener(InkGestureListener listener)
    {
        gestureListeners.remove(listener);
    }
    
    public final ObservableList<InkStroke> getInkStrokes()
    {
        return inkStrokes;
    }
    
    public final void setDrawingAttributes(InkStrokeDrawingAttributes drawingAttributes)
    {
        this.drawingAttributes.set(drawingAttributes);
    }
    
    public final InkStrokeDrawingAttributes getDrawingAttributes()
    {
        return drawingAttributes.get();
    }
    
    public final ObjectProperty<InkStrokeDrawingAttributes> drawingAttributesProperty()
    {
        return drawingAttributes;
    }
    
    public final void setOnStylusDown(EventHandler<? super StylusEvent> handler)
    {
         if (handler != onStylusDown.get())
        {
            if (onStylusDown.get() != null)
                removeEventHandler(StylusEvent.STYLUS_DOWN, onStylusDown.get());
            
            onStylusDown.set(handler);
            
            if (handler != null)
                addEventHandler(StylusEvent.STYLUS_DOWN, handler);
        }
    }
    
    public final EventHandler<? super StylusEvent> getOnStylusDown()
    {
        return onStylusDown.get();
    }
    
    public final ObjectProperty<EventHandler<? super StylusEvent>> onStylusDownProperty()
    {
        return onStylusDown;
    }
    
    public final void setOnStylusUp(EventHandler<? super StylusEvent> handler)
    {
        if (handler != onStylusUp.get())
        {
            if (onStylusUp.get() != null)
                removeEventHandler(StylusEvent.STYLUS_UP, onStylusUp.get());
            
            onStylusUp.set(handler);
            
            if (handler != null)
                addEventHandler(StylusEvent.STYLUS_UP, handler);
        }
    }
    
    public final EventHandler<? super StylusEvent> getOnStylusUp()
    {
        return onStylusUp.get();
    }
    
    public final ObjectProperty<EventHandler<? super StylusEvent>> onStylusUpProperty()
    {
        return onStylusUp;
    }
    
    public final void setOnStylusMoved(EventHandler<? super StylusEvent> handler)
    {
        if (handler != onStylusMoved.get())
        {
            if (onStylusMoved.get() != null)
                removeEventHandler(StylusEvent.STYLUS_MOVED, onStylusMoved.get());
            
            onStylusMoved.set(handler);
            
            if (handler != null)
                addEventHandler(StylusEvent.STYLUS_MOVED, handler);
        }
    }
    
    public final EventHandler<? super StylusEvent> getOnStylusMoved()
    {
        return onStylusMoved.get();
    }
    
    public final ObjectProperty<EventHandler<? super StylusEvent>> onStylusMovedProperty()
    {
        return onStylusMoved;
    }
    
    public final void setOnStylusInRange(EventHandler<? super StylusEvent> handler)
    {
        if (handler != onStylusInRange.get())
        {
            if (onStylusInRange.get() != null)
                removeEventHandler(StylusEvent.STYLUS_IN_RANGE, onStylusInRange.get());
            
            onStylusInRange.set(handler);
            
            if (handler != null)
                addEventHandler(StylusEvent.STYLUS_IN_RANGE, handler);
        }
    }
    
    public final EventHandler<? super StylusEvent> getOnStylusInRange()
    {
        return onStylusInRange.get();
    }
    
    public final ObjectProperty<EventHandler<? super StylusEvent>> onStylusInRangeProperty()
    {
        return onStylusInRange;
    }
    
    public final void setOnStylusOutOfRange(EventHandler<? super StylusEvent> handler)
    {
        if (handler != onStylusOutOfRange.get())
        {
            if (onStylusOutOfRange.get() != null)
                removeEventHandler(StylusEvent.STYLUS_OUT_OF_RANGE, onStylusOutOfRange.get());
            
            onStylusOutOfRange.set(handler);
            
            if (handler != null)
                addEventHandler(StylusEvent.STYLUS_OUT_OF_RANGE, handler);
        }
    }
    
    public final EventHandler<? super StylusEvent> getOnStylusOutOfRange()
    {
        return onStylusOutOfRange.get();
    }
    
    public final ObjectProperty<EventHandler<? super StylusEvent>> onStylusOutOfRangeProperty()
    {
        return onStylusOutOfRange;
    }
    
    public void setInkDisabled(boolean disabled)
    {
        this.inkDisabled = disabled;
    }
    
    public boolean isInkDisabled()
    {
        return inkDisabled;
    }
    
    public boolean isStylusDown()
    {
        return stylusDown;
    }
    
    public void addUndoableEvent(JsonObject event)
    {
        if (!isUndoing())
            undoableEvents.push(event);
    }
    
    public void clearUndoableEvents()
    {
        undoableEvents.clear();
    }
    
    protected void setUndoing(boolean value)
    {
        undoing = value;
    }
    
    public boolean isUndoing()
    {
        return undoing;
    }
    
    // returns true if event handled
    protected boolean performUndo(JsonObject event)
    {
        return false;
    }
    
    public final void undo()
    {
        if (!undoableEvents.isEmpty())
        {
            setUndoing(true);
            performUndo(undoableEvents.pop());
            setUndoing(false);
        }
    }
    
    public UserEventStack getUndoableEvents()
    {
        return undoableEvents;
    }
}
