/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package phylopen;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * WIM = World In Miniature
 * @author awehrer
 */
public class PhyloTreeInkCanvasModelWIM extends Pane
{
    private PhyloTreeInkCanvas world;
    private final ChangeListener<PhylogeneticTree> treeModelPropertyChangeListener;
    private final ChangeListener<BoundingBox> treeBoundsPropertyChangeListener;
    private final ChangeListener<Bounds> viewportBoundsPropertyChangeListener;
    private final ChangeListener<Number> modelRenderTranslationXChangeListener;
    private final ChangeListener<Number> modelRenderTranslationYChangeListener;
    private final ChangeListener<Number> modelRenderScaleChangeListener;
    private final ListChangeListener<Line> worldBranchListChangeListener;
    private final ObservableList<Line> branchLines;
    private final Group branchGroup;
    private final Group elementGroup;
    private final Rectangle viewRectangle;
    private double xScale;
    private double yScale;
    private final double defaultBranchWidth;
    private ScrollPane worldViewport;
    
    public PhyloTreeInkCanvasModelWIM()
    {
        defaultBranchWidth = 0.4;
        
        branchLines = FXCollections.observableList(new ArrayList<Line>());
        branchLines.addListener(new BranchLineListChangeListener());
        elementGroup = new Group();
        branchGroup = new Group();
        
        viewRectangle = new Rectangle();
        viewRectangle.setStroke(Color.BLACK);
        viewRectangle.setStrokeWidth(2.5);
        viewRectangle.setFill(Color.TRANSPARENT);
        
        elementGroup.getChildren().add(branchGroup);
        
        getChildren().add(elementGroup);
        elementGroup.relocate(0, 0);
        branchGroup.relocate(0, 0);
        viewRectangle.relocate(0, 0);
        
        treeModelPropertyChangeListener = new ChangeListener<PhylogeneticTree>()
        {
            @Override
            public void changed(ObservableValue<? extends PhylogeneticTree> observable, PhylogeneticTree oldValue, PhylogeneticTree newValue)
            {
                PhyloTreeInkCanvasModelWIM.this.clear();
                PhyloTreeInkCanvasModelWIM.this.render();
            }
        };
        
        treeBoundsPropertyChangeListener = new ChangeListener<BoundingBox>()
        {
            @Override
            public void changed(ObservableValue<? extends BoundingBox> observable, BoundingBox oldValue, BoundingBox newValue)
            {
                double oldXScale = getXScale();
                double oldYScale = getYScale();
                updateScale();
                double xScaleChangeFactor = getXScale() / oldXScale;
                double yScaleChangeFactor = getYScale() / oldYScale;
                
                for (Line branchLine : branchLines)
                {
                    branchLine.setStartX(branchLine.getStartX() * xScaleChangeFactor);
                    branchLine.setStartY(branchLine.getStartY() * yScaleChangeFactor);
                    branchLine.setEndX(branchLine.getEndX() * xScaleChangeFactor);
                    branchLine.setEndY(branchLine.getEndY() * yScaleChangeFactor);
                }
                
                viewRectangle.setWidth(viewRectangle.getWidth() * xScaleChangeFactor);
                viewRectangle.setHeight(viewRectangle.getHeight() * yScaleChangeFactor);
            }
        };
        
        viewportBoundsPropertyChangeListener = new ChangeListener<Bounds>()
        {
            @Override
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue)
            {
                viewRectangle.setWidth(newValue.getWidth() * getXScale() / getWorld().getModelRenderScale());
                viewRectangle.setHeight(newValue.getHeight() * getYScale()  / getWorld().getModelRenderScale());
            }
        };
        
        modelRenderTranslationXChangeListener = new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                viewRectangle.setX(-newValue.doubleValue() * getXScale() / getWorld().getModelRenderScale());
            }
        };
        
        modelRenderTranslationYChangeListener = new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                viewRectangle.setY(-newValue.doubleValue() * getYScale() / getWorld().getModelRenderScale());
            }
        };
        
        modelRenderScaleChangeListener = new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                double scaleChangeFactor = oldValue.doubleValue() / newValue.doubleValue();
                double newWidth = viewRectangle.getWidth() * scaleChangeFactor;
                double newHeight = viewRectangle.getHeight() * scaleChangeFactor;
                viewRectangle.setWidth(newWidth);
                viewRectangle.setHeight(newHeight);
            }
        };
        
        worldBranchListChangeListener = new ListChangeListener<Line>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Line> change)
            {
                while (change.next())
                {
                    if (change.wasAdded() && !change.wasRemoved())
                    {
                        for (Line newBranchLine : new ArrayList<>(change.getAddedSubList()))
                            branchLines.add(createMimickingLineClone(newBranchLine));
                    }
                }
            }
        };
        
        EventHandler<MouseEvent> mouseEventHandler = new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                if (!getWorld().isTouchDisabled())
                    moveViewTo(event.getX() - (viewRectangle.getWidth() / 2.0), event.getY() - (viewRectangle.getHeight() / 2.0));
            }
        };
        
        addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEventHandler);
    }
    
    private class BranchLineListChangeListener implements ListChangeListener<Line>
    {
        private class ModelRenderBranchLineAdditionTask implements Runnable
        {
            private final int startIndex;
            private final List<? extends Line> childrenToAdd;
            
            public ModelRenderBranchLineAdditionTask(int startIndex, List<? extends Line> childrenToAdd)
            {
                this.startIndex = startIndex;
                this.childrenToAdd = new ArrayList<>(childrenToAdd);
            }
            
            @Override
            public void run()
            {
                ObservableList<Node> modelRenderPaneChildren = PhyloTreeInkCanvasModelWIM.this.branchGroup.getChildren();
                modelRenderPaneChildren.addAll(startIndex, childrenToAdd);
            }
        }
        
        private class ModelRenderBranchLineRemovalTask implements Runnable
        {
            private final List<? extends Line> childrenToRemove;
            
            public ModelRenderBranchLineRemovalTask(List<? extends Line> childrenToRemove)
            {
                this.childrenToRemove = new ArrayList<>(childrenToRemove);
            }
            
            @Override
            public void run()
            {
                PhyloTreeInkCanvasModelWIM.this.branchGroup.getChildren().removeAll(childrenToRemove);
            }
        }
        
        @Override
        public void onChanged(ListChangeListener.Change<? extends Line> change)
        {
            while (change.next())
            {
                if (change.wasAdded() && !change.wasRemoved())
                    Platform.runLater(new ModelRenderBranchLineAdditionTask(change.getFrom(), change.getAddedSubList()));
                else if (!change.wasAdded() && change.wasRemoved())
                    Platform.runLater(new ModelRenderBranchLineRemovalTask(change.getRemoved()));
                else
                    System.out.println("Change not handled.");
            }
        }
    }
    
    public void moveViewTo(double wimX, double wimY)
    {
        double worldX = -(wimX / getXScale()) * getWorld().getModelRenderScale();
        double worldY = -(wimY / getYScale()) * getWorld().getModelRenderScale();
        
        getWorld().translateModelRenderTo(worldX, worldY);
    }
    
    public void setWorld(PhyloTreeInkCanvas world)
    {
        if (this.world != world)
        {
            if (this.world != null)
            {
                this.world.treeModelProperty().removeListener(treeModelPropertyChangeListener);
                this.world.getBranchLines().removeListener(worldBranchListChangeListener);
                clear();
                this.world.treeBoundsProperty().removeListener(treeBoundsPropertyChangeListener);
                this.world.modelRenderScaleProperty().removeListener(modelRenderScaleChangeListener);
                this.world.modelRenderTanslationXProperty().removeListener(modelRenderTranslationXChangeListener);
                this.world.modelRenderTanslationYProperty().removeListener(modelRenderTranslationYChangeListener);
            }
            
            this.world = world;
            
            if (this.world != null)
            {
                render();
                this.world.getBranchLines().addListener(worldBranchListChangeListener);
                this.world.treeModelProperty().addListener(treeModelPropertyChangeListener);
                this.world.treeBoundsProperty().addListener(treeBoundsPropertyChangeListener);
                this.world.modelRenderScaleProperty().addListener(modelRenderScaleChangeListener);
                this.world.modelRenderTanslationXProperty().addListener(modelRenderTranslationXChangeListener);
                this.world.modelRenderTanslationYProperty().addListener(modelRenderTranslationYChangeListener);
            }
        }
    }
    
    public void setWorldViewport(ScrollPane viewport)
    {
        if (getWorldViewport() == null && viewport != null)
            elementGroup.getChildren().add(viewRectangle);
        else if (getWorldViewport() != null && viewport == null)
            elementGroup.getChildren().remove(viewRectangle);
        
        if (getWorldViewport() != null)
            getWorldViewport().viewportBoundsProperty().removeListener(viewportBoundsPropertyChangeListener);
        
        if (viewport != null)
            viewport.viewportBoundsProperty().addListener(viewportBoundsPropertyChangeListener);
        
        worldViewport = viewport;
    }
    
    public ScrollPane getWorldViewport()
    {
        return worldViewport;
    }
    
    protected void clear()
    {
        branchLines.clear();
    }
    
    protected void render()
    {
        updateScale();
        
        for (Line branchLine : getWorld().getBranchLines())
            branchLines.add(createMimickingLineClone(branchLine));
    }
    
    private Line createMimickingLineClone(Line branchLine)
    {
        final Line branchClone = new Line(branchLine.getStartX() * getXScale(), branchLine.getStartY() * getYScale(), branchLine.getEndX() * getXScale(), branchLine.getEndY() * getYScale());
        branchClone.setStroke(Color.BLACK);
        branchClone.setStrokeWidth(defaultBranchWidth);
        
        branchLine.startXProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                branchClone.setStartX(newValue.doubleValue() * getXScale());
            }
        });
        
        branchLine.startYProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                branchClone.setStartY(newValue.doubleValue() * getYScale());
            }
        });
        
        branchLine.endXProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                branchClone.setEndX(newValue.doubleValue() * getXScale());
            }
        });
        
        branchLine.endYProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                branchClone.setEndY(newValue.doubleValue() * getYScale());
            }
        });
        
        branchClone.visibleProperty().bind(branchLine.visibleProperty());
        
        branchLine.parentProperty().addListener(new ChangeListener<Parent>()
        {
            @Override
            public void changed(ObservableValue<? extends Parent> observable, Parent oldValue, Parent newValue)
            {
                if (newValue == null)
                {
                    branchLines.remove(branchClone);
                    //System.out.println("Branch clone removed");
                }
            }
        });
        
        return branchClone;
    }
    
    public double getXScale()
    {
        return xScale;
    }
    
    public double getYScale()
    {
        return yScale;
    }
    
    private void updateScale()
    {
        BoundingBox treeBounds = getWorld().getTreeBounds();
        
        if (treeBounds == null)
        {
            xScale = 1.0;
            yScale = 1.0;
        }
        else
        {
            xScale = getPrefWidth() / treeBounds.getMaxX();
            yScale = getPrefHeight() / treeBounds.getMaxY();
        }
    }
    
    public PhyloTreeInkCanvas getWorld()
    {
        return world;
    }
}
