/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package phylopen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 *
 * @author awehrer
 */
public class Clade implements Comparable<Clade>, Iterable<Clade>
{
    static
    {
        nextId = 0;
    }
    
    private final IntegerProperty id;
    private Clade parent;
    private final List<Clade> children;
    private double branchLength;
    private double nodeWeight;
    private final StringProperty label;
    private final List<Property<Object>> attributeProperties;
    private static int nextId;
    private List<Pair<String, String>> images;
    private boolean collapsed;
    private Color incidentBranchLineColor; // the line connect it to its parent
    private double incidentBranchLineWidth; // the line connect it to its parent
    private Color fillColor;
    private Color strokeColor;
    private double nodeRadius;
    
    public Clade()
    {
        this(nextId++);
    }
    
    public Clade(int id)
    {
        this.parent = null;
        this.children = new ArrayList<>();
        this.attributeProperties = new LinkedList<>();
        this.id = new SimpleIntegerProperty(this, "id", id);
        this.label = new SimpleStringProperty(this, "label");
        this.images = new ArrayList<>();
        this.incidentBranchLineColor = null;
        this.incidentBranchLineWidth = 1.0;
        this.fillColor = null;
        this.strokeColor = null;
        this.nodeRadius = 5.0;
    }
    
    public Clade(JsonObject jsonClade)
    {
        this(jsonClade.has("node_data") ? (jsonClade.has("nodeid") ? jsonClade.get("nodeid").getAsInt() : nextId++) : nextId++);
        
        JsonElement property;
        
        property = jsonClade.get("node_data");
        
        if (property != null)
        {
            JsonObject nodeDataObject = property.getAsJsonObject();
            
            setLabel(nodeDataObject.get("node name").getAsString());
            setNodeWeight(nodeDataObject.get("node weight").getAsDouble());
            
            // additional node properties
            if (nodeDataObject.has("collapsed"))
                setCollapsed(nodeDataObject.get("collapsed").getAsBoolean());
            if (nodeDataObject.has("incidentBranchLineColor"))
                setIncidentBranchLineColor(PhyloPenIO.getColor(nodeDataObject, "incidentBranchLineColor"));
            if (nodeDataObject.has("incidentBranchLineWidth"))
                setIncidentBranchLineWidth(nodeDataObject.get("incidentBranchLineWidth").getAsDouble());
            if (nodeDataObject.has("fillColor"))
                setFillColor(PhyloPenIO.getColor(nodeDataObject, "fillColor"));
            if (nodeDataObject.has("strokeColor"))
                setStrokeColor(PhyloPenIO.getColor(nodeDataObject, "strokeColor"));
            if (nodeDataObject.has("nodeRadius"))
                setNodeRadius(nodeDataObject.get("nodeRadius").getAsDouble());
            
            property = nodeDataObject.get("attributes");
            
            if (property != null)
            {
                JsonArray attributeArray = property.getAsJsonArray();
                JsonObject attributeJsonObject;
                String attributeName;
                JsonElement attributeProperty;
                
                for (JsonElement attributeArrayElement : attributeArray)
                {
                    attributeJsonObject = attributeArrayElement.getAsJsonObject();
                    attributeName = attributeJsonObject.entrySet().iterator().next().getKey();
                    attributeProperty = attributeJsonObject.get(attributeName);

                    // Determine what type the data is
                    String strRep = attributeProperty.toString();

                    if (strRep.startsWith("\""))
                        setAttribute(attributeName, attributeProperty.getAsString());
                    else if (strRep.contains("."))
                        setAttribute(attributeName, attributeProperty.getAsDouble());
                    else
                        setAttribute(attributeName, attributeProperty.getAsInt());
                }
            }
            
            property = nodeDataObject.get("images");

            if (property != null)
            {
                JsonElement imageProperty, textProperty;
                JsonArray imageArray = property.getAsJsonArray();
                JsonObject imageJsonObject;
                
                for (JsonElement imageArrayElement : imageArray)
                {
                    imageJsonObject = imageArrayElement.getAsJsonObject();
                    imageProperty = imageJsonObject.get("image");
                    textProperty = imageJsonObject.get("text");
                    addImage((imageProperty != null ? imageProperty.getAsString() : null), (textProperty != null ? textProperty.getAsString() : null));
                }
            }
        }
        
        property = jsonClade.get("edge_data");
        
        if (property != null)
            setBranchLength(property.getAsJsonObject().get("weight").getAsDouble());
        
        property = jsonClade.get("children");
        
        if (property != null)
        {
            JsonArray children = property.getAsJsonArray();
            for (JsonElement childArrayElement : children)
                addChild(new Clade(childArrayElement.getAsJsonObject()));
        }
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
    
    public StringProperty labelProperty()
    {
        return label;
    }
    
    public void setLabel(String label)
    {
        labelProperty().set(label);
    }
    
    public String getLabel()
    {
        return labelProperty().get();
    }
    
    public IntegerProperty idProperty()
    {
        return id;
    }
    
    public int getId()
    {
        return idProperty().get();
    }
    
    public void setParent(Clade parent)
    {
        this.parent = parent;
    }
    
    public Clade getParent()
    {
        return parent;
    }
    
    public int getChildIndex(Clade child)
    {
        return children.indexOf(child);
    }

    public double getBranchLength()
    {
        return branchLength;
    }

    public void setBranchLength(double branchLength)
    {
        this.branchLength = branchLength;
    }
    
    public double getNodeWeight()
    {
        return nodeWeight;
    }
    
    public void setNodeWeight(double nodeWeight)
    {
        this.nodeWeight = nodeWeight;
    }
    
    public int getChildCount()
    {
        return children.size();
    }

    public void addChild(Clade child)
    {
        children.add(child);
        child.setParent(this);
    }

    public Clade getLastChild()
    {
        if (children.size() > 0)
            return children.get(children.size() - 1);
        else
            return null;
    }

    public void addChild(int index, Clade child)
    {
        children.add(index, child);
        child.setParent(this);
    }

    // removes last child
    public void removeChild()
    {
        if (children.size() > 0)
        {
            Clade removedChild = children.remove(children.size() - 1);
            removedChild.setParent(null);
        }
    }

    public boolean removeChild(Clade child)
    {
        child.setParent(null);
        return children.remove(child);
    }

    public Clade getFirstChild()
    {
        if (children.size() > 0)
            return children.get(0);
        else
            return null;
    }

    public Clade getSecondChild()
    {
        if (children.size() > 1)
            return children.get(1);
        else
            return null;
    }
    
    public Clade getChild(int index)
    {
        return children.get(index);
    }
    
    public void reverseChildOrdering()
    {
        Collections.reverse(children);
    }
    
    public Object getAttributeValue(String attributeName)
    {
        for (Property<Object> attributeProperty : attributeProperties)
        {
            if (attributeProperty.getName().equals(attributeName))
                return attributeProperty.getValue();
        }
        
        return null;
    }
    
    public void setAttribute(String attributeName, Object attributeValue)
    {
        boolean found = false;
        Iterator<Property<Object>> attributePropertyIterator = attributeProperties.iterator();
        Property<Object> attributeProperty;
        
        while (attributePropertyIterator.hasNext())
        {
            attributeProperty = attributePropertyIterator.next();
            
            if (attributeProperty.getName().equals(attributeName))
            {
                found = true;
                
                if (attributeValue == null)
                    attributePropertyIterator.remove();
                else
                    attributeProperty.setValue(attributeValue);
                
                break;
            }
        }
        
        if (!found && attributeValue != null)
        {
            // add new property
            attributeProperties.add(new SimpleObjectProperty(this, attributeName, attributeValue));
        }
    }
    
    public boolean hasAttribute(String name)
    {
        for (Property<Object> attributeProperty : attributeProperties)
        {
            if (attributeProperty.getName().equals(name))
                return true;
        }
        
        return false;
    }
    
    public Double getNumericalAttribute(String name)
    {
        Property<Object> targetProperty = null;
        
        for (Property<Object> attributeProperty : attributeProperties)
        {
            if (attributeProperty.getName().equals(name))
            {
                targetProperty = attributeProperty;
                break;
            }
        }
        
        if (targetProperty == null)
            return Double.NaN;
        
        Object value = targetProperty.getValue();
        
        if (!(value instanceof Number))
            return Double.NaN;
        
        return ((Number)value).doubleValue();
    }
    
    public Map<String, Double> getNumericalAttributeMap()
    {
        Map<String, Double> map = new TreeMap<>();
        splitAttributeMapByType(map, null);
        return map;
    }
    
    public Map<String, String> getStringAttributeMap()
    {
        Map<String, String> map = new TreeMap<>();
        splitAttributeMapByType(null, map);
        return map;
    }
    
    public void splitAttributeMapByType(Map<String, Double> numericalAttributeMap, Map<String, String> stringAttributeMap)
    {
        Iterator<Property<Object>> attributePropertyIterator = attributeProperties.iterator();
        Property<Object> attributeProperty;
        
        while (attributePropertyIterator.hasNext())
        {
            attributeProperty = attributePropertyIterator.next();
            
            if (numericalAttributeMap != null && attributeProperty.getValue() instanceof Number)
            {
                numericalAttributeMap.put(attributeProperty.getName(), ((Number)attributeProperty.getValue()).doubleValue());
            }
            else if (stringAttributeMap != null && attributeProperty.getValue() instanceof String)
            {
                stringAttributeMap.put(attributeProperty.getName(), (String)attributeProperty.getValue());
            }
        }
    }
    
    public Property<Object> getAttributeProperty(String name)
    {
        for (Property<Object> attributeProperty : attributeProperties)
        {
            if (attributeProperty.getName().equals(name))
                return attributeProperty;
        }
        
        return null;
    }
    
    public Iterator<Property<Object>> createAttributePropertiesIterator()
    {
        return attributeProperties.iterator();
    }
    
    public void addImage(String url, String description)
    {
        images.add(new Pair<>(url, description));
    }
    
    public int getImageCount()
    {
        return images.size();
    }
    
    public Iterator<Pair<String, String>> createImageIterator()
    {
        return images.iterator();
    }
    
    public void swapImageIndices(int index1, int index2)
    {
        if (index1 != index2 && index1 >= 0 && index1 < images.size() && index2 >= 0 && index2 < images.size())
            Collections.swap(images, index1, index2);
    }
    
    public void removeImage(int index)
    {
        images.remove(index);
    }
    
    public boolean isCollapsed()
    {
        return collapsed;
    }
    
    public void setCollapsed(boolean value)
    {
        this.collapsed = value;
    }
    
    public void setIncidentBranchLineColor(Color color)
    {
        this.incidentBranchLineColor = color;
    }
    
    public Color getIncidentBranchLineColor()
    {
        return incidentBranchLineColor;
    }
    
    public void setIncidentBranchLineWidth(double width)
    {
        this.incidentBranchLineWidth = width;
    }
    
    public double getIncidentBranchLineWidth()
    {
        return incidentBranchLineWidth;
    }
    
    public void setFillColor(Color color)
    {
        this.fillColor = color;
    }
    
    public void setStrokeColor(Color color)
    {
        this.strokeColor = color;
    }
    
    public Color getFillColor()
    {
        return fillColor;
    }
    
    public Color getStrokeColor()
    {
        return strokeColor;
    }
    
    public void setNodeRadius(double radius)
    {
        this.nodeRadius = radius;
    }
    
    public double getNodeRadius()
    {
        return nodeRadius;
    }
    
    public boolean isDescendent(Clade searchForClade)
    {
        return isDescendent(searchForClade, this);
    }
    
    public static boolean isDescendent(Clade searchForClade, Clade parentClade)
    {
        if (parentClade.getChildCount() > 0)
        {
            for (Clade child : parentClade)
            {
                if (searchForClade == child || isDescendent(searchForClade, child))
                    return true;
            }
        }
        
        return false;
    }
    
    public JsonObject toJson()
    {
        JsonObject cladeJson = new JsonObject();
        
        if (getChildCount() > 0)
        {
            JsonArray childrenJson = new JsonArray();

            // Convert child data to JSON format
            for (Clade child : this)
                childrenJson.add(child.toJson());
            
            cladeJson.add("children", childrenJson);
        }
        
        JsonObject edgeData = new JsonObject();
        edgeData.addProperty("weight", this.getBranchLength());
        
        JsonObject nodeData = new JsonObject();
        nodeData.addProperty("node name", this.getLabel());
        nodeData.addProperty("node weight", this.getNodeWeight());
        nodeData.addProperty("nodeid", this.getId());
        
        // additional node properties
        nodeData.addProperty("collapsed", this.isCollapsed());
        if (this.getIncidentBranchLineColor() != null)
            PhyloPenIO.attachColorProperty(nodeData, this.getIncidentBranchLineColor(), "incidentBranchLineColor");
        if (this.getIncidentBranchLineWidth() != 1.0)
            nodeData.addProperty("incidentBranchLineWidth", this.getIncidentBranchLineWidth());
        if (this.getFillColor() != null)
            PhyloPenIO.attachColorProperty(nodeData, getFillColor(), "fillColor");
        if (this.getStrokeColor() != null)
            PhyloPenIO.attachColorProperty(nodeData, getStrokeColor(), "strokeColor");
        nodeData.addProperty("nodeRadius", this.getNodeRadius());
        
        
        JsonArray attributesJson = new JsonArray();
        
        Iterator<Property<Object>> attributeIterator = this.createAttributePropertiesIterator();
        Property<Object> attributeProperty;
        JsonObject attributeObj;
        
        while (attributeIterator.hasNext())
        {
            attributeProperty = attributeIterator.next();
            attributeObj = new JsonObject();
            
            if (attributeProperty.getValue() instanceof Number)
                attributeObj.addProperty(attributeProperty.getName(), (Number) attributeProperty.getValue());
            else if (attributeProperty.getValue() instanceof String)
                attributeObj.addProperty(attributeProperty.getName(), (String) attributeProperty.getValue());
            else
                System.out.println("Unknown attribute data type. Value not saved.");
            
            attributesJson.add(attributeObj);
        }
        
        JsonArray imageJson = new JsonArray();
        
        Iterator<Pair<String, String>> imageIterator = this.createImageIterator();
        JsonObject imageObj;
        Pair<String, String> stringPair;
        
        while (imageIterator.hasNext())
        {
            stringPair = imageIterator.next();
            imageObj = new JsonObject();
            imageObj.addProperty("image", stringPair.getKey());
            imageObj.addProperty("text", stringPair.getValue());
            
            imageJson.add(imageObj);
        }
        
        nodeData.add("attributes", attributesJson);
        nodeData.add("images", imageJson);
        
        cladeJson.add("edge_data", edgeData);
        cladeJson.add("node_data", nodeData);
        
        return cladeJson;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Clade)
            return getId() == ((Clade)obj).getId();
        
        return false;
    }

    @Override
    public int hashCode()
    {
        return getId();
    }

    @Override
    public int compareTo(Clade other)
    {
        if (other == null) return 1;
        return new Integer(getId()).compareTo(other.getId());
    }
    
    @Override
    public Iterator<Clade> iterator()
    {
        return children.iterator();
    }
}
