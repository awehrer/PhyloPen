/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen.utility.ink.recognition.gesture;

import java.util.Collection;
import java.util.List;
import phylopen.utility.ink.InkUtility;
import phylopen.utility.ink.StylusPoint;
import phylopen.utility.ink.recognition.AbstractInkGestureRecognitionProcedure;
import phylopen.utility.ink.recognition.AugmentedInkStroke;
import phylopen.utility.ink.recognition.InkGestureRecognizer;

/**
 *
 * @author awehrer
 */
public class MinusSignRecognitionProcedure extends AbstractInkGestureRecognitionProcedure
{
    @Override
    protected InkGestureRecognizer.InkGestureRecognitionProcedureResult performRecognize(Collection<AugmentedInkStroke> inkStrokes, double scale, Object...additionalArgs)
    {
        if (inkStrokes.size() == 1)
        {
            AugmentedInkStroke stroke = inkStrokes.iterator().next();
            List<StylusPoint> points = stroke.getResampledPoints();
            List<Integer> cornerIndices = stroke.getCornerIndices();
            
            if (cornerIndices.size() == 2 && InkUtility.isLine(points, 0, points.size() - 1))
            {
                double horizontalLineAngleDiffThreshold = 15.0;
                double horizontalAngle;
                horizontalAngle = Math.toDegrees(InkUtility.computeAngleOfLineWithXAxis(points.get(0), points.get(points.size() - 1)));
                System.out.println("horizontalAngle = " + horizontalAngle);
                
                horizontalAngle = Math.abs(horizontalAngle);
                
                if (horizontalAngle < horizontalLineAngleDiffThreshold)
                {
                    return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(true);
                }
            }
        }

        return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
    }

    @Override
    public String getGestureIdentifier()
    {
        return "Minus sign";
    }
}
