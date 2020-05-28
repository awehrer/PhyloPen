/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import phylopen.utility.RemScaler;

/**
 *
 * @author awehrer
 */
public class AttributeAveragesTable extends TableView<AttributeAveragesTableRowData>
{
    public final double CELL_HEIGHT;
    public final double MAX_COLUMN_PREF_WIDTH;
    
    private PhyloPenCanvas canvas;
    
    private final ListChangeListener<Clade> nodeSelectionListener;
    //private final ListChangeListener<Clade> nodeListChangeListener;
    private final ListChangeListener<String> attributeListChangeListener;
    
    public AttributeAveragesTable()
    {
        RemScaler rem = new RemScaler();
        CELL_HEIGHT = rem.scale(70.0);
        MAX_COLUMN_PREF_WIDTH = rem.scale(150.0);
        
        this.setFixedCellSize(CELL_HEIGHT);
        this.setItems(FXCollections.observableList(new LinkedList<>()));
        
        // http://stackoverflow.com/questions/26220896/showing-tooltip-in-javafx-at-specific-row-position-in-the-tableview
        setRowFactory(tv -> new TableRow<AttributeAveragesTableRowData>() {
            @Override
            public void updateItem(AttributeAveragesTableRowData data, boolean empty) {
                super.updateItem(data, empty);
                if (data == null || data.valueRangeFullProperty().get() == null || data.valueRangeFullProperty().equals("")) {
                    setTooltip(null);
                } else {
                    setTooltip(new Tooltip(data.valueRangeFullProperty().get()));
                }
            }
        });
        
        // create columns
        // why Number not Double? see http://linux2biz.net/215599/add-progressbar-in-treetableview-returning-double-value-to-observable
        TableColumn<AttributeAveragesTableRowData, String> attributeColumn = new TableColumn<>("Attribute");
        attributeColumn.setCellValueFactory(new PropertyValueFactory("attributeName"));
        attributeColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        attributeColumn.setPrefWidth(MAX_COLUMN_PREF_WIDTH);
        
        TableColumn<AttributeAveragesTableRowData, String> averageColumn = new TableColumn<>("Population Avg");
        averageColumn.setCellValueFactory(new PropertyValueFactory("averageAsStr"));
        averageColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        averageColumn.setPrefWidth(MAX_COLUMN_PREF_WIDTH);
        
        TableColumn<AttributeAveragesTableRowData, String> standardDeviationColumn = new TableColumn<>("Standard Dev.");
        standardDeviationColumn.setCellValueFactory(new PropertyValueFactory("standardDeviationAsStr"));
        standardDeviationColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        standardDeviationColumn.setPrefWidth(MAX_COLUMN_PREF_WIDTH);
        
        this.getColumns().setAll(attributeColumn, averageColumn, standardDeviationColumn);
        
        
        nodeSelectionListener = new ListChangeListener<Clade>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Clade> change)
            {
                update();
            }
        };
        
        /*nodeListChangeListener = new ListChangeListener<Clade>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Clade> change)
            {
                update();
            }
        };*/
        
        attributeListChangeListener = new ListChangeListener<String>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends String> change)
            {
                update();
            }
        };
    }
    
    public void setCanvas(PhyloPenCanvas canvas)
    {
        if (this.canvas != null)
        {
            this.canvas.getSelectedNodes().removeListener(nodeSelectionListener);
            //this.canvas.getTreeModel().getClades().removeListener(nodeListChangeListener);
            this.canvas.getTreeModel().getAttributeList().removeListener(attributeListChangeListener);
        }

        this.canvas = canvas;

        if (this.canvas != null)
        {
            this.canvas.getSelectedNodes().addListener(nodeSelectionListener);
            //this.canvas.getTreeModel().getClades().addListener(nodeListChangeListener);
            //System.out.println(this.canvas.getTreeModel().getAttributeList());
            this.canvas.getTreeModel().getAttributeList().addListener(attributeListChangeListener);
        }

        update();
    }
    
    public PhyloPenCanvas getCanvas()
    {
        return canvas;
    }
    
    private double roundDouble(double value, int numDecimalPlaces)
    {
        double factor = Math.pow(10, numDecimalPlaces);
        return Math.round(value * factor) / factor;
    }
    
    protected void update()
    {
        Collection<Clade> nodes = (getCanvas().getSelectedNodes().size() > 0)
                ? getCanvas().getSelectedNodes() : getCanvas().getTreeModel().getNodes();
        
        // first pass: get all the attribute values and average for each attribute discovered in selected nodes set
        Map<String, ArrayList<Double>> items = new TreeMap<>();
        Map<String, ArrayList<String>> itemsStr = new TreeMap<>();
        
        Map<String,String> strs;
        Map<String, Double> nums;
        
        for (Clade node : nodes)
        {
            strs = new TreeMap<>();
            nums = new TreeMap<>();
            node.splitAttributeMapByType(nums, strs);
            
            for (Map.Entry<String,Double> kv : nums.entrySet())
            {
                ArrayList<Double> item = items.getOrDefault(kv.getKey(), null);
                
                if (item == null)
                {
                    item = new ArrayList<>();
                    items.put(kv.getKey(), item);
                }
                
                item.add(kv.getValue());
            }
            
            /*for (Map.Entry<String,String> kv : strs.entrySet())
            {
                ArrayList<String> item = itemsStr.getOrDefault(kv.getKey(), null);
                
                if (item == null)
                {
                    item = new ArrayList<>();
                    itemsStr.put(kv.getKey(), item);
                }
                
                item.add(kv.getValue());
            }*/
        }

        // second pass: get the average and stdev for each attribute and update list
        AttributeAveragesTableRowData data;
        ListIterator<AttributeAveragesTableRowData> tableRowDataIterator = getItems().listIterator();

        for (Map.Entry<String, ArrayList<Double>> kv : items.entrySet())
        {
            double avg = 0.0;
            
            for (Double d : kv.getValue())
                avg += (d / kv.getValue().size());
            
            avg = roundDouble(avg, 6);

            double stdev = 0.0;
            
            for (Double d : kv.getValue())
                stdev += Math.pow(d - avg, 2);
            
            stdev = roundDouble((kv.getValue().size() > 1 ? Math.sqrt(stdev / (kv.getValue().size() - 1)) : 0.0), 6);
            
            if (tableRowDataIterator.hasNext())
            {
                data = tableRowDataIterator.next();
                data.setAttributeName(kv.getKey());
                data.setAverage(avg);
                data.setStandardDeviation(stdev);
                data.setValueRangeSummary("");
                data.setValueRangeFull("");
            }
            else
            {
                tableRowDataIterator.add(new AttributeAveragesTableRowData(kv.getKey(), avg, stdev, "", ""));
            }
        }
        
        for (Map.Entry<String, ArrayList<String>> kv : itemsStr.entrySet())
        {
            StringBuilder sb = new StringBuilder(), sb2 = new StringBuilder();
            sb.append("(");
            int i = 0;
            for(String ss : kv.getValue())
            {
                sb.append("\"").append(ss).append("\"");
                if (i == 0) sb2.append("(\"").append(ss).append("\"");
                if (++i != kv.getValue().size())
                    sb.append(", ");
                else
                {
                    if (kv.getValue().size() > 2)
                        sb2.append(", ..., \"").append(ss).append("\"");
                    else if (kv.getValue().size() > 1)
                        sb2.append(", \"").append(ss).append("\"");
                }
            }
            sb.append(")");
            sb2.append(")");
            
            if (tableRowDataIterator.hasNext())
            {
                data = tableRowDataIterator.next();
                data.setAttributeName(kv.getKey());
                data.setAverage(-1.0);
                data.setStandardDeviation(-1.0);
                data.setValueRangeFull(sb.toString());
                data.setValueRangeSummary(sb2.toString());
            }
            else
            {
                tableRowDataIterator.add(new AttributeAveragesTableRowData(kv.getKey(), -1.0, -1.0, sb2.toString(), sb.toString()));
            }
        }
        
        if (tableRowDataIterator.nextIndex() < getItems().size())
            getItems().remove(tableRowDataIterator.nextIndex(), getItems().size());
        //int selectedIndex = getSelectionModel().getSelectedIndex();
        
        //if (selectedIndex > 0 && selectedIndex < getItems().size())
        //    getSelectionModel().select(selectedIndex);
    }
}