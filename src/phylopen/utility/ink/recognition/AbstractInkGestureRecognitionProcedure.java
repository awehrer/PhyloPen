/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen.utility.ink.recognition;

import java.util.Collection;

/**
 *
 * @author Work
 */
public abstract class AbstractInkGestureRecognitionProcedure implements InkGestureRecognizer.InkGestureRecognitionProcedure
{
    private boolean disabled;

    public AbstractInkGestureRecognitionProcedure()
    {
        this.disabled = false;
    }
    
    @Override
    public InkGestureRecognizer.InkGestureRecognitionProcedureResult recognize(Collection<AugmentedInkStroke> inkStrokes, double scale)
    {
        return recognize(inkStrokes, scale, (Object[])null);
    }

    @Override
    public final InkGestureRecognizer.InkGestureRecognitionProcedureResult recognize(Collection<AugmentedInkStroke> inkStrokes, double scale, Object...additionalArgs)
    {
        if (isDisabled())
            return new InkGestureRecognizer.InkGestureRecognitionProcedureResult(false);
        else
            return performRecognize(inkStrokes, scale, additionalArgs);
    }
    
    protected abstract InkGestureRecognizer.InkGestureRecognitionProcedureResult performRecognize(Collection<AugmentedInkStroke> inkStrokes, double scale, Object...additionalArgs);

    public boolean isDisabled()
    {
        return disabled;
    }

    public void setDisabled(boolean value)
    {
        this.disabled = value;
    }
}
