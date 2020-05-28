/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package phylopen.utility.ink.recognition;

import phylopen.utility.ink.recognition.AugmentedInkStroke;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.scene.shape.Shape;

/**
 *
 * @author awehrer
 */
public class InkGesture
{
    private final String identifier;
    private final List<AugmentedInkStroke> augmentedStrokeList;
    private final double scale;
    private final Shape identifiedShape;
    
    public InkGesture(String identifier, Collection<AugmentedInkStroke> inkStrokes, double scale)
    {
        this(identifier, inkStrokes, scale, null);
    }
    
    public InkGesture(String identifier, Collection<AugmentedInkStroke> inkStrokes, double scale, Shape identifiedShape)
    {
        this.augmentedStrokeList = java.util.Collections.unmodifiableList(new ArrayList<>(inkStrokes));
        this.identifier = identifier;
        this.scale = scale;
        this.identifiedShape = identifiedShape;
    }
    
    public String getIdentifier()
    {
        return identifier;
    }
    
    public List<AugmentedInkStroke> getAugmentedStrokeList()
    {
        return augmentedStrokeList;
    }
    
    public double getScale()
    {
        return scale;
    }
    
    public Shape getIdentifiedShape()
    {
        return identifiedShape;
    }
}
