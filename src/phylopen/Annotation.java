/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import phylopen.utility.ink.InkStroke;

/**
 *
 * @author awehrer
 */
public class Annotation
{
    private List<Clade> nodes;
    private List<InkStroke> ink;
    private StringProperty textProperty;
    private String recognizedText;
    private ObjectProperty<Color> colorProperty;
    private static int nextId;
    private int id;
    
    static
    {
        nextId = 0;
    }
    
    public Annotation(Collection<Clade> nodes, Collection<InkStroke> ink)
    {
        this(nodes, nextId++);
        this.ink = new ArrayList<>(ink);
    }
    
    public Annotation(Collection<Clade> nodes, String text)
    {
        this(nodes, nextId++);
        setText(text);
    }
    
    public Annotation(Collection<Clade> nodes, Collection<InkStroke> ink, int id)
    {
        this(nodes, id);
        this.ink = new ArrayList<>(ink);
    }
    
    public Annotation(Collection<Clade> nodes, String text, int id)
    {
        this(nodes, id);
        setText(text);
    }
    
    private Annotation(Collection<Clade> nodes, int id)
    {
        this.id = id;
        this.nodes = new ArrayList<>(nodes);
        this.textProperty = new SimpleStringProperty(null);
        this.colorProperty = new SimpleObjectProperty<>(Color.YELLOW);
    }
    
    public Iterator<Clade> createNodeIterator()
    {
        return nodes.iterator();
    }
    
    public Iterator<InkStroke> createInkIterator()
    {
        if (ink == null)
            return null;
        
        return ink.iterator();
    }
    
    public List<Clade> getNodes()
    {
        return nodes;
    }
    
    public StringProperty textProperty()
    {
        return textProperty;
    }
    
    public String getText()
    {
        return textProperty.get();
    }
    
    public void setText(String text)
    {
        textProperty.set(text);
    }
    
    public String getRecognizedText()
    {
        return recognizedText;
    }
    
    public boolean isInkAnnotation()
    {
        return ink != null;
    }
    
    public ObjectProperty<Color> colorProperty()
    {
        return colorProperty;
    }
    
    public Color getColor()
    {
        return colorProperty.get();
    }
    
    public void setColor(Color color)
    {
        colorProperty.set(color);
    }
    
    public static Annotation fromJson(JsonObject annotationJson, PhyloPenCanvas canvas)
    {
        Annotation annotation = new Annotation(PhyloPenIO.getNodes(canvas, annotationJson), annotationJson.get("text").getAsString(), annotationJson.get("annotationId").getAsInt());
        annotation.setColor(PhyloPenIO.getColor(annotationJson));
        
        return annotation;
    }
    
    public JsonObject toJson()
    {
        JsonObject annotationJson = new JsonObject();
        annotationJson.addProperty("annotationId", id);
        if (!isInkAnnotation())
            annotationJson.addProperty("text", getText());
        PhyloPenIO.attachNodeIdsProperty(annotationJson, getNodes());
        PhyloPenIO.attachColorProperty(annotationJson, getColor());
        
        return annotationJson;
    }
    
    public int getId()
    {
        return id;
    }
    
    public static void resetNextId()
    {
        nextId = 0;
    }
    
    public static void setNextId(int id)
    {
        nextId = id;
    }
    
    public static int getNextId()
    {
        return nextId;
    }
    
    @Override
    public String toString()
    {
        return getText();
    }
}
