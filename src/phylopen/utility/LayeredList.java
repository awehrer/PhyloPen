/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package phylopen.utility;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Anthony Wehrer
 * @param <E> The element type used in the layered list
 */
public class LayeredList<E>
{
    private final ArrayList<ArrayList<E>> layers;
    
    public LayeredList(int numLayers)
    {
        this.layers = new ArrayList<>(numLayers);
        
        int numLayersAdded = 0;
        
        while (numLayersAdded < numLayers)
        {
            this.layers.add(new ArrayList<>());
            numLayersAdded++;
        }
    }
    
    public void insertNewLayer()
    {
        layers.add(new ArrayList<>());
    }
    
    public void insertNewLayer(int layerIndex)
    {
        layers.add(layerIndex, new ArrayList<>());
    }
    
    public void add(E element)
    {
        addToLayer(0, element);
    }
    
    public void add(int index, E element)
    {
        addToLayer(0, index, element);
    }
    
    public void addAll(Collection<E> elements)
    {
        addAllToLayer(0, elements);
    }
    
    public void addAll(int index, Collection<E> elements)
    {
        addAllToLayer(0, index, elements);
    }
    
    public int addToLayer(int layerIndex, E element)
    {
        if (layerIndex < layers.size())
        {
            layers.get(layerIndex).add(element);
            return getGlobalIndex(layerIndex, layers.get(layerIndex).size() - 1);
        }
        
        return -1;
    }
    
    public int addToLayer(int layerIndex, int indexInLayer, E element)
    {
        if (layerIndex < layers.size())
        {
            layers.get(layerIndex).add(indexInLayer, element);
            return getGlobalIndex(layerIndex, indexInLayer);
        }
        
        return -1;
    }
    
    public int addAllToLayer(int layerIndex, Collection<E> elements)
    {
        if (layerIndex < layers.size())
        {
            layers.get(layerIndex).addAll(elements);
            return getGlobalIndex(layerIndex, layers.get(layerIndex).size() - elements.size());
        }
        
        return -1;
    }
    
    public int addAllToLayer(int layerIndex, int indexInLayer, Collection<E> elements)
    {
        if (layerIndex < layers.size())
        {
            layers.get(layerIndex).addAll(indexInLayer, elements);
            return getGlobalIndex(layerIndex, indexInLayer);
        }
        
        return -1;
    }
    
    public boolean removeFromLayer(int layerIndex, E element)
    {
        return layers.get(layerIndex).remove(element);
    }
    
    public boolean removeAllFromLayer(int layerIndex, Collection<E> elements)
    {
        return layers.get(layerIndex).removeAll(elements);
    }
    
    public E remove(int globalIndex)
    {
        int [] layerIndices = getLayerIndices(globalIndex);
        return layers.get(layerIndices[0]).remove(layerIndices[1]);
    }
    
    public boolean remove(E element)
    {
        for (ArrayList<E> layer : layers)
        {
            if (layer.remove(element))
                return true;
        }
        
        return false;
    }
    
    public boolean removeAll(Collection<E> elements)
    {
        boolean changed = false;
        
        for (ArrayList<E> layer : layers)
        {
            if (layer.removeAll(elements))
                changed = true;
        }
        
        return changed;
    }
    
    public int size()
    {
        int size = 0;
        
        for (ArrayList<E> layer : layers)
            size += layer.size();
        
        return size;
    }
    
    public boolean isEmpty()
    {
        return size() == 0;
    }
    
    public boolean contains(E element)
    {
        for (ArrayList<E> layer : layers)
        {
            if (layer.contains(element))
                return true;
        }
        
        return false;
    }
    
    public void clear()
    {
        for (ArrayList<E> layer : layers)
            layer.clear();
    }

    public int getGlobalIndex(int layerIndex, int indexInLayer)
    {
        if (layers.get(layerIndex).size() <= indexInLayer)
            return -1;
        
        int numElementsBeforeLayer = 0;
        
        for (int i = 0; i < layerIndex; i++)
        {
            numElementsBeforeLayer += layers.get(i).size();
        }
        
        return numElementsBeforeLayer + indexInLayer;
    }
    
    public int [] getLayerIndices(int globalIndex)
    {
        if (size() <= globalIndex)
            return new int [] {-1, -1};
        
        int layerIndex = 0;
        int numElementsBeforeLayer = 0;
        
        while (globalIndex < numElementsBeforeLayer + layers.get(layerIndex).size())
        {
            numElementsBeforeLayer += layers.get(layerIndex).size();
            layerIndex++;
        }
        
        return new int [] {layerIndex, globalIndex - numElementsBeforeLayer};
    }
}
