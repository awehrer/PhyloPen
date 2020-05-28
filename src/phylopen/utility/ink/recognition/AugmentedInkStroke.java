/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package phylopen.utility.ink.recognition;

import java.util.Collection;
import java.util.List;
import javafx.geometry.BoundingBox;
import javafx.util.Pair;
import phylopen.utility.ink.InkStroke;
import phylopen.utility.ink.InkStrokeDrawingAttributes;
import phylopen.utility.ink.InkUtility;
import phylopen.utility.ink.StylusPoint;

/**
 *
 * @author User
 */
public class AugmentedInkStroke extends InkStroke
{
    private List<Integer> cornerIndices;
    private List<StylusPoint> resampledPoints;
    private List<Pair<Integer, Integer>> selfIntersectionIndexPairs;
    private BoundingBox boundingBox;
    
    public AugmentedInkStroke(InkStrokeDrawingAttributes attributes)
    {
        this(null, attributes, 0);
    }
    
    public AugmentedInkStroke(Collection<StylusPoint> points)
    {
        this(points, new InkStrokeDrawingAttributes(), 0);
    }
    
    public AugmentedInkStroke(Collection<StylusPoint> points, InkStrokeDrawingAttributes attributes)
    {
        this(points, attributes, 0);
    }
    
    public AugmentedInkStroke(InkStroke originalStrokeObj)
    {
        this(originalStrokeObj.getStylusPoints(), originalStrokeObj.getDrawingAttributes(), originalStrokeObj.getDuration());
    }
    
    public AugmentedInkStroke(Collection<StylusPoint> points, InkStrokeDrawingAttributes attributes, int duration)
    {
        super(points, attributes, duration);
        
        //dehooking
        InkUtility.dehook(getStylusPoints(), 15, 20, 5);
        
        Pair<List<Integer>, List<StylusPoint>> results = new CornerFinder().getCorners(this);
        this.cornerIndices = results.getKey();
        this.resampledPoints = results.getValue();
        
        this.selfIntersectionIndexPairs = InkUtility.getSelfIntersections(this);
        
        this.boundingBox = InkUtility.getBoundingBox(resampledPoints);
    }
    
    public List<Integer> getCornerIndices()
    {
        return cornerIndices;
    }
    
    public int getNumCorners()
    {
        return getCornerIndices().size();
    }
    
    public int getNumSelfIntersections()
    {
        return selfIntersectionIndexPairs.size();
    }
    
    public List<StylusPoint> getResampledPoints()
    {
        return resampledPoints;
    }
    
    public List<Pair<Integer, Integer>> getSelfIntersectionIndexPairs()
    {
        return selfIntersectionIndexPairs;
    }
    
    public BoundingBox getBoundingBox()
    {
        return boundingBox;
    }
}