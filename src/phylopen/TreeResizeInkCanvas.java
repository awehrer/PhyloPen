/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package phylopen;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import phylopen.utility.ink.InkCanvas;
import phylopen.utility.ink.InkStroke;
import phylopen.utility.ink.InkStrokeDrawingAttributes;

/**
 *
 * @author User
 */
public class TreeResizeInkCanvas extends InkCanvas
{
    private PhyloTreeInkCanvas treeCanvas;
    
    public TreeResizeInkCanvas()
    {
        getInkStrokes().addListener(new ListChangeListener<InkStroke>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends InkStroke> change)
            {
                if (!getTreeCanvas().isInkDisabled())
                {
                    while (change.next())
                    {
                        if (change.wasAdded() && !change.wasRemoved())
                        {
                            for (InkStroke stroke : change.getAddedSubList())
                            {
                                if (stroke.getStylusPoints().size() > 2)
                                {
                                    stroke.getStylusPoints().remove(1, stroke.getStylusPoints().size() - 1);
                                }

                                if (getTreeCanvas() != null && getTreeCanvas().getTreeModel() != null && stroke.getStylusPoints().size() == 2)
                                {
                                    final double widthChange = (stroke.getStylusPoints().get(1).getX() - stroke.getStylusPoints().get(0).getX());
                                    final double heightChange = (stroke.getStylusPoints().get(1).getY() - stroke.getStylusPoints().get(0).getY());
                                    getTreeCanvas().changeTreeWidthAndHeight(widthChange / getTreeCanvas().getModelRenderScale(), heightChange / getTreeCanvas().getModelRenderScale());
                                }

                                final Timeline timeline = new Timeline();
                                timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100.0), new InkStrokeGhostingHandler(stroke, timeline)));
                                timeline.setCycleCount(Timeline.INDEFINITE);
                                timeline.play();
                            }
                        }
                    }
                }
            }
        });
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
                TreeResizeInkCanvas.this.getInkStrokes().remove(stroke);
                timeline.stop();
            }
        }
    }
    
    public PhyloTreeInkCanvas getTreeCanvas()
    {
        return treeCanvas;
    }
    
    public void setTreeCanvas(PhyloTreeInkCanvas treeCanvas)
    {
        this.treeCanvas = treeCanvas;
    }
}
