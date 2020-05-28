/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package phylopen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author awehrer
 */
public class PhylogeneticTree
{
    private Clade root;
    private ObservableList<String> attributeList;
    
    public PhylogeneticTree(Clade root)
    {
        this.root = root;
        this.attributeList = FXCollections.observableList(new LinkedList<>());
    }
    
    public PhylogeneticTree(JsonElement jsonData)
    {
        this.attributeList = FXCollections.observableList(new LinkedList<>());
        JsonObject jsonTreeData = (jsonData.getAsJsonObject().has("data") ? jsonData.getAsJsonObject().get("data").getAsJsonObject() : jsonData.getAsJsonObject());
        this.root = new Clade(jsonTreeData);
        
        // build attribute list
        updateAttributeList();
    }
    
    public Clade getRoot()
    {
        return root;
    }
    
    public JsonObject toJson()
    {
        JsonObject rootJson = root.toJson();
        
        // Now the rest of the data
        JsonArray edgeFields = new JsonArray(), nodeFields = new JsonArray();
        
        edgeFields.add(new JsonPrimitive("weight"));
        
        for (String attributeName : attributeList)
            nodeFields.add(new JsonPrimitive(attributeName));
        
        rootJson.add("edge_fields", edgeFields);
        rootJson.add("node_fields", nodeFields);
        
        return rootJson;
    }
    
    public void updateAttributeList()
    {
        List<String> notYetFoundList = new LinkedList<>(attributeList);
        updateAttributeListOver(this.getRoot(), notYetFoundList);
        
        // if any remain that were not found, that means they are no longer in the tree, so we remove them from the tree.
        attributeList.removeAll(notYetFoundList);
    }
    
    private void updateAttributeListOver(Clade clade, List<String> notYetFoundList)
    {
        Iterator<Property<Object>> iterator = clade.createAttributePropertiesIterator();
        Property<Object> property;
        int index;
        
        while (iterator.hasNext())
        {
            property = iterator.next();
            index = notYetFoundList.indexOf(property.getName());
            if (index >= 0)
                notYetFoundList.remove(index);
            else if (!attributeList.contains(property.getName()))
                attributeList.add(property.getName());
        }
        
        for (Clade child : clade)
            updateAttributeListOver(child, notYetFoundList);
    }
    
    public void deleteAttribute(String attributeName)
    {
        deleteAttribute(root, attributeName);
        attributeList.remove(attributeName);
    }
    
    private void deleteAttribute(Clade clade, String attributeName)
    {
        clade.setAttribute(attributeName, null);
        
        for (Clade child : clade)
            deleteAttribute(child, attributeName);
    }
    
    public ObservableList<String> getAttributeList()
    {
        return attributeList;
    }
    
    public Collection<Clade> getNodes()
    {
        LinkedList<Clade> nodes = new LinkedList<>();
        nodes.add(root);
        
        if (root.getChildCount() > 0)
            addChildrenToList(root, nodes);
        
        return nodes;
    }
    
    private void addChildrenToList(Clade node, LinkedList<Clade> list)
    {
        for (Clade child : node)
        {
            list.add(child);
            addChildrenToList(child, list);
        }
    }
    
    public Collection<Clade> getLeafNodes()
    {
        LinkedList<Clade> nodes = new LinkedList<>();
        
        if (root.getChildCount() > 0)
            addLeavesToList(root, nodes);
        else
            nodes.add(root);
        
        return nodes;
    }
    
    private void addLeavesToList(Clade node, LinkedList<Clade> list)
    {
        for (Clade child : node)
        {
            if (child.getChildCount() == 0)
                list.add(child);
            else
                addLeavesToList(child, list);
        }
    }
    
    public Clade getClade(int id)
    {
        if (root.getId() == id)
            return root;
        else if (root.getChildCount() > 0)
            return getClade(id, root);
        
        return null;
    }
    
    private Clade getClade(int id, Clade clade)
    {
        for (Clade child : clade)
        {
            if (child.getId() == id)
                return child;
            else if (child.getChildCount() > 0)
            {
                Clade node = getClade(id, child);
                
                if (node != null)
                    return node;
            }
        }
        
        return null;
    }
    
    public int getNumLeaves()
    {
        return getNumLeaves(root);
    }
    
    private int getNumLeaves(Clade clade)
    {
        if (clade.getChildCount() == 0)
            return 1;
        
        int childLeafCount = 0;
        
        for (Clade child : clade)
            childLeafCount += getNumLeaves(child);
        
        return childLeafCount;
    }
    
    public void setRoot(Clade clade)
    {
        if (clade.getParent() != null)
            clade.getParent().removeChild(clade);
        if (root != null && clade.getChildIndex(root) < 0)
            clade.addChild(root);
        this.root = clade;
    }
}
