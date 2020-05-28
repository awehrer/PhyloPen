/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 *
 * @author User
 */
public class PhyloPenIO
{
    private PhyloPenIO() {}
    
    public static final int UNDO = -5;
    public static final int NEW_TRAINING_DATA = -4;
    public static final int FAILED_INK_RECOGNITION = -3;
    public static final int NEW_SESSION = -2;
    public static final int STATE_SNAPSHOT = -1;
    public static final int TRANSLATION_X = 0;
    public static final int TRANSLATION_Y = 1;
    public static final int SCALE = 2;
    public static final int NAVIGATION = 3; // general type
    public static final int SELECTION = 4;
    public static final int DESELECTION = 5;
    public static final int NODE_RECOLOR = 6;
    public static final int BRANCH_RECOLOR = 7;
    public static final int BRANCH_WIDTH_CHANGE = 8;
    public static final int NODE_RADIUS_CHANGE = 9;
    public static final int ANNOTATION = 10;
    public static final int CLADE_COLLAPSE = 11;
    public static final int CLADE_EXPAND = 12;
    public static final int SCALE_BRANCHES = 13;
    public static final int LABEL_LEAF_NODE_NAMES = 14;
    public static final int LABEL_INTERMEDIATE_BRANCH_LENGTHS = 15;
    public static final int LABEL_FINAL_BRANCH_LENGTHS_W_LEAF_NAMES = 16;
    public static final int LABEL_FINAL_BRANCH_LENGTHS = 17;
    public static final int LEAF_IMAGES_VISIBLE = 18;
    public static final int SHOW_ANNOTATION_PLACEHOLDERS = 19;
    public static final int COLOR_BY_ATTRIBUTE_MIN_COLOR_CHANGE = 20;
    public static final int COLOR_BY_ATTRIBUTE_MAX_COLOR_CHANGE = 21;
    public static final int COLOR_BY_ATTRIBUTE_UNDEFINED_COLOR_CHANGE = 22;
    public static final int COLOR_BY_ATTRIBUTE = 23;
    public static final int TREE_RESIZE = 24;
    public static final int CLADE_CUT_REATTACH = 25;
    public static final int CLADE_CUT = 26;
    public static final int CLADE_ADD = 27;
    public static final int ANNOTATION_REMOVED = 28;
    public static final int CLADE_ROTATE = 29;
    public static final int ANNOTATION_EDIT = 30;
    public static final int APPEARANCE_RESET = 31;
    public static final int APPEND_SELECTION = 32;
    public static final int CLADE_INSERT = 33;
    public static final int LABEL_ANCESTOR_NAMES = 34;
    public static final int SHOW_HYPERLINKS = 35;
    
    private static int nextEventId = 0;
    
    public static void setNextEventId(int nextId)
    {
        nextEventId = nextId;
    }
    
    public static int getNextEventId()
    {
        return nextEventId;
    }
    
    public static void resetNextEventId()
    {
        nextEventId = 0;
    }
    
    public static void save(PhyloPenCanvas canvas, File saveFile) throws FileNotFoundException, IOException
    {
        JsonElement jsonData = canvas.toJson();
        Gson gson = new Gson();
        String jsonString = gson.toJson(jsonData);
        FileWriter fileWriter = new FileWriter(saveFile);
        fileWriter.write(jsonString);
        fileWriter.close();
    }
    
    public static boolean load(String path, PhyloPenCanvas canvas, boolean localFile, String user) throws IOException
    {
        if (localFile)
            return load(new File(path), canvas, user);
        else
        {
            ArborJsonRetriever jsonRetriever = new ArborJsonRetriever();
        
            boolean htmlFormattedJson = true;
            
            try
            {
                URL url = new URL(path);
                htmlFormattedJson = !url.getPath().endsWith("/download");
            }
            catch (MalformedURLException e)
            {
                System.out.println("Error: Malformed URL");
            }

            final JsonElement jsonData = jsonRetriever.getResponseJson(path, "GET", htmlFormattedJson);
            
            if (jsonData != null)
            {
                canvas.loadFromJson(jsonData, path, user);
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean load(File openFile, PhyloPenCanvas canvas, String user) throws IOException
    {
        String jsonString = readFile(openFile);
        JsonElement parsedJson = new JsonParser().parse(jsonString);
        canvas.loadFromJson(parsedJson, openFile.getName(), user);
        return true;
    }
    
    public static String readFile(File file) throws IOException 
    {
        byte [] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        return new String(encoded);
    }
    
    public static void attachColorProperty(JsonObject event, Color color)
    {
        attachColorProperty(event, color, "color");
    }
    
    public static void attachColorProperty(JsonObject event, Color color, String propertyName)
    {
        event.add(propertyName, toJson(color));
    }
    
    public static JsonObject toJson(Color color)
    {
        JsonObject colorProperty = new JsonObject();
        colorProperty.addProperty("red", color.getRed());
        colorProperty.addProperty("green", color.getGreen());
        colorProperty.addProperty("blue", color.getBlue());
        colorProperty.addProperty("opacity", color.getOpacity());
        return colorProperty;
    }
    
    public static Color toColor(JsonObject jsonColor)
    {
        return new Color(jsonColor.get("red").getAsDouble(),
                jsonColor.get("green").getAsDouble(),
                jsonColor.get("blue").getAsDouble(),
                jsonColor.get("opacity").getAsDouble());
    }
    
    public static Color getColor(JsonObject event)
    {
        return getColor(event, "color");
    }
    
    public static Color getColor(JsonObject event, String propertyName)
    {
        return toColor(event.get(propertyName).getAsJsonObject());
    }
    
    public static void attachNodeIdsProperty(JsonObject event, Collection<Clade> nodes)
    {
        attachNodeIdsProperty(event, nodes, "nodeIds");
    }

    public static void attachNodeIdsProperty(JsonObject event, Collection<Clade> nodes, String attachedPropertyName)
    {
        if (nodes != null)
        {
            JsonArray selectedNodeIds = new JsonArray();

            for (Clade node : nodes)
                selectedNodeIds.add(new JsonPrimitive(node.getId()));

            event.add(attachedPropertyName, selectedNodeIds);
        }
    }
    
    public static List<Clade> getNodes(PhyloPenCanvas canvas, JsonObject event)
    {
        return getNodes(canvas, event, "nodeIds");
    }
    
    public static List<Clade> getNodes(PhyloPenCanvas canvas, JsonObject event, String propertyName)
    {
        List<Clade> nodes = new ArrayList<>();
        Collection<Clade> allNodes = canvas.getTreeModel().getNodes();
        int id;
        for (JsonElement element : event.get(propertyName).getAsJsonArray())
        {
            id = element.getAsInt();

            for (Clade node : allNodes)
            {
                if (node.getId() == id)
                {
                    nodes.add(node);
                    break;
                }
            }
        }

        return nodes;
    }
    
    public static JsonObject createStateSnapshot(String name, PhyloPenCanvas canvas)
    {
        JsonObject stateSnapshot = createEventRecord(STATE_SNAPSHOT);
        stateSnapshot.add("snapshot", canvas.toJson());
        return stateSnapshot;
    }
    
    public static void restoreToState(JsonObject stateSnapshot, PhyloPenCanvas canvas, String treeFileName, String user)
    {
        canvas.loadFromJson(stateSnapshot.get("snapshot"), treeFileName, user);
    }
    
    public static JsonObject createEventRecord(int typeId)
    {
        return createEventRecord(typeId, System.currentTimeMillis());
    }
    
    public static JsonObject createEventRecord(int typeId, long timestamp)
    {
        JsonObject event = new JsonObject();
        event.addProperty("typeId", typeId);
        event.addProperty("timestamp", timestamp);
        event.addProperty("id", nextEventId++);
        
        return event;
    }
    
    public static class NodeConnection
    {
        private final Clade parent;
        private final Clade child;
        private final int childIndex;
        
        public NodeConnection(Clade parent, Clade child, int childIndex)
        {
            this.parent = parent;
            this.child = child;
            this.childIndex = childIndex;
        }
        
        public Clade getParent()
        {
            return parent;
        }
        
        public Clade getChild()
        {
            return child;
        }
        
        public int getChildIndex()
        {
            return childIndex;
        }
        
        @Override
        public String toString()
        {
            return getParent().getLabel() + "[" + getChildIndex() + "]" + " => " + getChild().getLabel();
        }
    }
    
    public static void attachConnectionsProperty(JsonObject event, Collection<Pair<Clade, Clade>> connections)
    {
        JsonArray connectionsArray = new JsonArray();
        JsonObject connectionJson;

        for (Pair<Clade, Clade> connection : connections)
        {
            connectionJson = new JsonObject();
            connectionJson.addProperty("parent", connection.getKey().getId());
            connectionJson.addProperty("child", connection.getValue().getId());
            connectionsArray.add(connectionJson);
        }

        event.add("connections", connectionsArray);
    }
    
    public static List<NodeConnection> getConnections(JsonObject event, PhyloPenCanvas canvas)
    {
        JsonArray connectionsArray = event.get("connections").getAsJsonArray();
        JsonObject connectionJson;
        List<NodeConnection> connections = new ArrayList<>(connectionsArray.size());
        Clade parent, child;
        
        for (JsonElement element : connectionsArray)
        {
            connectionJson = element.getAsJsonObject();
            parent = canvas.getTreeModel().getClade(connectionJson.get("parent").getAsInt());
            child = canvas.getTreeModel().getClade(connectionJson.get("child").getAsInt());
            connections.add(new NodeConnection(parent, child, parent.getChildIndex(child)));
        }
        
        return connections;
    }
}
