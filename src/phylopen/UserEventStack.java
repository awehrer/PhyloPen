/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import com.google.gson.JsonObject;
import java.util.LinkedList;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 *
 * @author awehrer
 */
public class UserEventStack
{
    private final LinkedList<JsonObject> eventStack;
    private int stackSizeLimit;
    private final IntegerProperty sizeProperty;
    
    public UserEventStack()
    {
        eventStack = new LinkedList<>();
        stackSizeLimit = Integer.MAX_VALUE;
        sizeProperty = new SimpleIntegerProperty(0);
    }
    
    public IntegerProperty sizeProperty()
    {
        return sizeProperty;
    }
    
    public int getSize()
    {
        return sizeProperty.get();
    }
    
    private void incrementSize()
    {
        setSize(getSize() + 1);
    }
    
    private void decrementSize()
    {
        if (getSize() > 0)
        {
            setSize(getSize() - 1);
        }
    }
    
    private void setSize(int value)
    {
        sizeProperty.set(value);
    }
    
    public void setStackSizeLimit(int limit)
    {
        stackSizeLimit = limit;
    }
    
    public int getStackSizeLimit()
    {
        return stackSizeLimit;
    }
    
    public void push(JsonObject event)
    {
        eventStack.push(event);
        
        if (eventStack.size() > stackSizeLimit)
            eventStack.removeLast();
        else
            incrementSize();
    }
    
    public JsonObject pop()
    {
        JsonObject obj = eventStack.pop();
        decrementSize();
        return obj;
    }
    
    public int size()
    {
        return eventStack.size();
    }
    
    public boolean isEmpty()
    {
        return eventStack.isEmpty();
    }
    
    public JsonObject peek()
    {
        return eventStack.peek();
    }
    
    public void clear()
    {
        eventStack.clear();
        setSize(0);
    }
    
    public boolean contains(JsonObject event)
    {
        return eventStack.contains(event);
    }
}
