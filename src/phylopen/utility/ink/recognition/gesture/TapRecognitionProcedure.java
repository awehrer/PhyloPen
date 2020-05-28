/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package phylopen.utility.ink.recognition.gesture;

import java.util.Collection;
import javafx.geometry.BoundingBox;
import phylopen.utility.ink.InkUtility;
import phylopen.utility.ink.recognition.AbstractInkGestureRecognitionProcedure;
import phylopen.utility.ink.recognition.AugmentedInkStroke;
import phylopen.utility.ink.recognition.InkGestureRecognizer;

/**
 *
 * @author awehrer
 */
public class TapRecognitionProcedure extends AbstractInkGestureRecognitionProcedure
{
    @Override
    protected InkGestureRecognizer.InkGestureRecognitionProcedureResult performRecognize(Collection<AugmentedInkStroke> inkStrokes, double scale, Object...additionalArgs)
    {
        if (inkStrokes.size() == 1)
        {
            AugmentedInkStroke stroke = inkStrokes.iterator().next();
            BoundingBox bounds = InkUtility.getBoundingBox(stroke);
            
            System.out.println("Duration: " + stroke.getDuration());
            return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(bounds.getWidth() < 30 && bounds.getHeight() < 30 && stroke.getDuration() < 100);
        }
        
        return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
    }

    @Override
    public String getGestureIdentifier()
    {
        return "Tap";
    }
}
