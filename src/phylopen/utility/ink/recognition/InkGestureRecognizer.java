/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package phylopen.utility.ink.recognition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javafx.scene.shape.Shape;

/**
 *
 * @author awehrer
 */
public class InkGestureRecognizer
{
    public interface InkGestureRecognitionProcedure
    {
        public InkGestureRecognitionProcedureResult recognize(Collection<AugmentedInkStroke> inkStrokes, double scale);
        public InkGestureRecognitionProcedureResult recognize(Collection<AugmentedInkStroke> inkStrokes, double scale, Object...additionalArgs);
        public String getGestureIdentifier();
        public boolean isDisabled();
        public void setDisabled(boolean value);
    }
    
    public static class InkGestureRecognitionProcedureResult
    {
        private final boolean match;
        private final Shape identifiedShape;
        
        public InkGestureRecognitionProcedureResult(boolean match)
        {
            this(match, null);
        }
        
        public InkGestureRecognitionProcedureResult(boolean match, Shape identifiedShape)
        {
            this.match = match;
            this.identifiedShape = identifiedShape;
        }
        
        public boolean isMatch()
        {
            return match;
        }
        
        public Shape getIdentifiedShape()
        {
            return identifiedShape;
        }
    }
    
    private final ArrayList<InkGestureRecognitionProcedure> gestureRecognitionProcedures;
    
    public InkGestureRecognizer()
    {
        this.gestureRecognitionProcedures = new ArrayList<>();
    }
    
    public InkGestureRecognizer(Collection<InkGestureRecognitionProcedure> gestureRecognitionProcedures)
    {
        this.gestureRecognitionProcedures = new ArrayList<>(gestureRecognitionProcedures);
    }
    
    public InkGesture analyzeForGesture(Collection<AugmentedInkStroke> inkStrokes, double scale)
    {
        return analyzeForGesture(inkStrokes, scale, (Object[])null);
    }
    
    protected InkGesture analyzeForGesture(Collection<AugmentedInkStroke> inkStrokes, double scale, Object...additionalArgs)
    {
        InkGestureRecognitionProcedureResult result;
        
        for (InkGestureRecognitionProcedure gestureRecognitionProcedure : gestureRecognitionProcedures)
        {
            result = gestureRecognitionProcedure.recognize(inkStrokes, scale, additionalArgs);
            
            if (result.isMatch())
            {
                System.out.println(gestureRecognitionProcedure.getGestureIdentifier());
                return new InkGesture(gestureRecognitionProcedure.getGestureIdentifier(), inkStrokes, scale, result.getIdentifiedShape());
            }
        }
        
        return null;
    }
    
    public void addGestureRecognitionProcedure(InkGestureRecognitionProcedure gestureRecognitionProcedure)
    {
        gestureRecognitionProcedures.add(gestureRecognitionProcedure);
    }
    
    public void removeGesturesRecognitionProcedure(InkGestureRecognitionProcedure gestureRecognitionProcedure)
    {
        gestureRecognitionProcedures.remove(gestureRecognitionProcedure);
    }
    
    protected Iterator<InkGestureRecognitionProcedure> createGestureRecognitionProcedureIterator()
    {
        return gestureRecognitionProcedures.iterator();
    }
}
