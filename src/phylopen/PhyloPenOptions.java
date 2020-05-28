/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import javafx.scene.input.KeyCombination;
import phylopen.PhyloPenCanvas.PhyloPenInkGestureRecognizer;

/**
 *
 * @author User
 */
public class PhyloPenOptions
{
    private String girderBaseURL;
    private double confidenceLevelThreshold;
    private final PhyloPenCanvas canvas;
    private final TreeAttributeTable attributeTable;
    private KeyCombination recordingHotkey;
    private boolean rectangleDisabled;
    private boolean triangleExpandCollapseDisabled;
    private boolean cutAndReattachDisabled;
    private boolean cutDisabled;
    private boolean cladeRotationDisabled;
    private boolean selectionDisabled;
    private boolean mouseMode;
    private boolean scrollInverted;
    private boolean showingTipAttributesOnly;
    
    
    public PhyloPenOptions(PhyloPenCanvas canvas, TreeAttributeTable attributeTable, String girderBaseURL, double confidenceLevelThreshold, KeyCombination recordingHotkey, boolean mouseMode, boolean invertScroll, boolean showTipAttributesOnly)
    {
        this.canvas = canvas;
        this.attributeTable = attributeTable;
        setGirderBaseURL(girderBaseURL);
        setConfidenceLevelThreshold(confidenceLevelThreshold);
        this.recordingHotkey = recordingHotkey;
        this.mouseMode = mouseMode;
        this.scrollInverted = invertScroll;
        this.showingTipAttributesOnly = showTipAttributesOnly;
    }
    
    public boolean isMouseMode()
    {
        return mouseMode;
    }
    
    public void setMouseMode(boolean value)
    {
        mouseMode = value;
    }
    
    public void setGirderBaseURL(String girderBaseURL)
    {
        this.girderBaseURL = girderBaseURL;
    }
    
    public String getGirderBaseURL()
    {
        return girderBaseURL;
    }
    
    public void setConfidenceLevelThreshold(double value)
    {
        this.confidenceLevelThreshold = value;
        canvas.setSymbolRecognitionConfidenceThreshold(value);
    }
    
    public double getConfidenceLevelThreshold()
    {
        return confidenceLevelThreshold;
    }
    
    public KeyCombination getRecordingHotkey()
    {
        return recordingHotkey;
    }
    
    public void setRecordingHotkey(KeyCombination hotkey)
    {
        recordingHotkey = hotkey;
    }
    
    public void setRectangleDisabled(boolean value)
    {
        rectangleDisabled = value;
        ((PhyloPenInkGestureRecognizer)canvas.getGestureRecognizer()).setDisabled("Rectangle", value);
    }
    
    public boolean isRectangleDisabled()
    {
        return rectangleDisabled;
    }
    
    public void setTriangleExpandCollapseDisabled(boolean value)
    {
        triangleExpandCollapseDisabled = value;
        ((PhyloPenInkGestureRecognizer)canvas.getGestureRecognizer()).setDisabled("Triangle", value);
    }
    
    public boolean isTriangleExpandCollapseDisabled()
    {
        return triangleExpandCollapseDisabled;
    }
    
    public void setCutAndReattachDisabled(boolean value)
    {
        cutAndReattachDisabled = value;
        ((PhyloPenInkGestureRecognizer)canvas.getGestureRecognizer()).setDisabled("Cut and reattach", value);
    }
    
    public boolean isCutAndReattachDisabled()
    {
        return cutAndReattachDisabled;
    }
    
    public void setCladeRotationDisabled(boolean value)
    {
        cladeRotationDisabled = value;
        ((PhyloPenInkGestureRecognizer)canvas.getGestureRecognizer()).setDisabled("Lefft square bracket", value);
    }
    
    public boolean isCutDisabled()
    {
        return cutDisabled;
    }
    
    public void setCutDisabled(boolean value)
    {
        cutDisabled = value;
        ((PhyloPenInkGestureRecognizer)canvas.getGestureRecognizer()).setDisabled("X", value);
    }
    
    public boolean isCladeRotationDisabled()
    {
        return cladeRotationDisabled;
    }
    
    public void setSelectionDisabled(boolean value)
    {
        selectionDisabled = value;
        ((PhyloPenInkGestureRecognizer)canvas.getGestureRecognizer()).setDisabled("Lasso", value);
    }
    
    public boolean isSelectionDisabled()
    {
        return selectionDisabled;
    }
    
    public boolean isScrollInverted()
    {
        return scrollInverted;
    }
    
    public void setScrollInverted(boolean value)
    {
        scrollInverted = value;
    }
    
    public boolean isShowingTipAttributesOnly()
    {
        return showingTipAttributesOnly;
    }
    
    public void setShowingTipAttributesOnly(boolean value)
    {
        this.showingTipAttributesOnly = value;
        attributeTable.setLeafNodesOnly(value);
    }
}
