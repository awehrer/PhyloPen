/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import java.util.ArrayList;
import java.util.Iterator;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import phylopen.utility.EditableTableCell;
import phylopen.utility.RemScaler;

/**
 *
 * @author awehrer, ayee
 */
public class TreeAttributeTable extends TableView<Clade>
{
    public final double CELL_HEIGHT;
    public final double MAX_COLUMN_PREF_WIDTH;
    
    private PhyloPenCanvas canvas;
    private boolean hasItems;
    //private HashMap<String, TableColumn<Clade, String>> columns;
    private ListChangeListener<String> attributeListListener;
    
    private boolean leafNodesOnly;
    private final StringConverter<Object> converter;
    private final EventHandler<TableColumn.CellEditEvent<Clade,Object>> cellEditHandler;
    
    // https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TableView.html
    // http://stackoverflow.com/questions/25204068/how-do-i-point-a-propertyvaluefactory-to-a-value-of-a-map
    public TreeAttributeTable()
    {
        RemScaler rem = new RemScaler();
        CELL_HEIGHT = rem.scale(70.0);
        MAX_COLUMN_PREF_WIDTH = rem.scale(200.0);
        leafNodesOnly = true;
        converter = new StringConverter<Object>()
        {
            @Override
            public String toString(Object object)
            {
                return object.toString();
            }

            @Override
            public Object fromString(String string)
            {
                if (isNumeric(string))
                {
                    if (string.contains("."))
                        return Double.parseDouble(string);
                    else
                        return Integer.parseInt(string);
                }
                
                return string;
            }
            
            private boolean isNumeric(String strNum)
            {
                try
                {
                    double d = Double.parseDouble(strNum);
                }
                catch (NumberFormatException | NullPointerException nfe)
                {
                    return false;
                }
                return true;
            }
        };
        
        cellEditHandler = new EventHandler<TableColumn.CellEditEvent<Clade,Object>>()
        {
            @Override
            public void handle(TableColumn.CellEditEvent<Clade, Object> event)
            {
                Clade clade = event.getRowValue();
                String columnHeader = event.getTableColumn().getText();
                Object newValue;
                if (event.getNewValue() instanceof String && event.getNewValue() != null && ((String)event.getNewValue()).isEmpty())
                    newValue = null;
                else
                    newValue = event.getNewValue();
                
                clade.setAttribute(columnHeader, newValue);
            }
        };
        
        this.setFixedCellSize(CELL_HEIGHT);
        
        getSelectionModel().setCellSelectionEnabled(true);
        setEditable(true);
        
        //columns = new HashMap<>();
        attributeListListener = new ListChangeListener<String>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends String> change)
            {
                change.next();
                if (hasItems)
                {
                    if (change.wasAdded() && !change.wasRemoved())
                    {
                        for (String newElement : new ArrayList<>(change.getAddedSubList()))
                        {
                            TableColumn<Clade, Object> column = new TableColumn<>(newElement);
                            column.setOnEditCommit(cellEditHandler);
                            column.setCellValueFactory(new PropertyValueFactory(newElement));
                            
                            column.setCellFactory(col -> new EditableTableCell<Clade, Object>(converter)
                            {
                              @Override
                              public void updateItem(Object item, boolean empty)
                               {
                                  super.updateItem(item, empty);
                                  setText("" + item);
                                  setTooltip(new Tooltip("" + item));
                                  getTooltip().setMaxWidth(600);
                                  getTooltip().setWrapText(true);
                                  setEditable(true);
                               }
                            });
                            
                            column.setStyle("-fx-alignment: CENTER-LEFT;");
                            column.setPrefWidth(Math.min(column.getPrefWidth(), MAX_COLUMN_PREF_WIDTH));
                            getColumns().add(column);
                            //columns.put(newElement, column);column.setPrefWidth(Math.min(column.getPrefWidth(), MAX_COLUMN_PREF_WIDTH));
                        }
                    }
                    else if (change.wasRemoved() && !change.wasAdded())
                    {
                        for (String removedElement : new ArrayList<>(change.getRemoved()))
                        {
                            getColumns().remove(indexOfColumn(removedElement));
                            //columns.remove(removedElement);
                        }
                    }
                }
            }
        };
        
        hasItems = false;
        
        // why number not integer? see http://stackoverflow.com/questions/24889638/javafx-properties-in-tableview
        TableColumn<Clade, Number> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(item -> item.getValue().idProperty());
        idColumn.setCellFactory(column -> new TableCell<Clade, Number>()
        {
          @Override
          public void updateItem(Number item, boolean empty)
           {
              super.updateItem(item, empty);
              setText("" + item);
              setTooltip(new Tooltip("" + item));
              getTooltip().setMaxWidth(600);
              getTooltip().setWrapText(true);
           }
        });
        
        idColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        //System.out.println(idColumn.getPrefWidth());
        idColumn.setPrefWidth(Math.min(idColumn.getPrefWidth(), MAX_COLUMN_PREF_WIDTH));
        
        TableColumn<Clade, String> labelColumn = new TableColumn<>("Label");
        labelColumn.setCellValueFactory(item -> item.getValue().labelProperty());
        
        labelColumn.setCellFactory(column -> new EditableTableCell<Clade, String>(EditableTableCell.IDENTITY_CONVERTER)
        {
          @Override
          public void updateItem(String item, boolean empty)
           {
              super.updateItem(item, empty);
              setText(item);
              setTooltip(new Tooltip(item));
              getTooltip().setMaxWidth(600);
              getTooltip().setWrapText(true);
              setEditable(true);
           }
        });
        
        labelColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        //System.out.println(labelColumn.getPrefWidth());
        labelColumn.setPrefWidth(Math.min(labelColumn.getPrefWidth(), MAX_COLUMN_PREF_WIDTH));
        
        getColumns().setAll(idColumn, labelColumn);
    }
    
    private int indexOfColumn(String columnHeader)
    {
        int index = 0;
        
        for (TableColumnBase<Clade, ?> column : getColumns())
        {
            if (column.getText().equals(columnHeader))
                return index;
            
            index++;
        }
        
        return -1;
    }
    
    public void setCanvas(PhyloPenCanvas canvas)
    {
        this.canvas = canvas;
        
        this.setItems(FXCollections.observableList(new ArrayList<>(isLeafNodesOnly() ? canvas.getTreeModel().getLeafNodes() : canvas.getTreeModel().getNodes())));
        hasItems = true;
        
        TableColumn<Clade, Object> column;
        
        Iterator<TableColumn<Clade, ?>> columnIterator = this.getColumns().iterator();
        columnIterator.next();
        columnIterator.next();
        while (columnIterator.hasNext())
        {
            columnIterator.next();
            columnIterator.remove();
        }
        
        //this.setPrefWidth((canvas.getTreeModel().getAttributeList().size() + 2) * MAX_COLUMN_PREF_WIDTH);
        
        for (String attributeName : canvas.getTreeModel().getAttributeList())
        {
            //if (!columns.containsKey(attributeName))
            if (indexOfColumn(attributeName) < 0)
            {
                column = new TableColumn<>(attributeName);
                column.setOnEditCommit(cellEditHandler);
                column.setCellValueFactory(data -> data.getValue().getAttributeProperty(attributeName));
                
                column.setCellFactory(col -> new EditableTableCell<Clade, Object>(converter)
                {
                  @Override
                  public void updateItem(Object item, boolean empty)
                   {
                      if (item == null) item = "";
                      super.updateItem(item, empty);
                      setText("" + item);
                      setTooltip(new Tooltip("" + item));
                      getTooltip().setMaxWidth(600);
                      getTooltip().setWrapText(true);
                      setEditable(true);
                   }
                });
                
                column.setStyle("-fx-alignment: CENTER-LEFT;");
                //System.out.print(column.getPrefWidth());
                double prefWidth = MAX_COLUMN_PREF_WIDTH;
                //column.setMaxWidth(prefWidth);
                //column.setMinWidth(prefWidth);
                column.setPrefWidth(prefWidth);
                //System.out.println(" -> " + column.getPrefWidth());
                getColumns().add(column);
                //columns.put(attributeName, column);
            }
        }
        
        columnIterator = this.getColumns().iterator();
        
        /*while (columnIterator.hasNext())
        {
            column = (TableColumn) columnIterator.next();
            column.prefWidthProperty().bind(TreeAttributeTable.this.widthProperty().divide(getColumns().size()));
        }*/
        
        canvas.getTreeModel().getAttributeList().addListener(attributeListListener);
        
        for (Node nodeMarker : canvas.getNodeMarkers())
        {
            nodeMarker.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>()
            {
                @Override
                public void handle(MouseEvent event)
                {
                    TreeAttributeTable.this.getSelectionModel().select(canvas.getClade(nodeMarker));
                    TreeAttributeTable.this.getSelectionModel().select(TreeAttributeTable.this.getSelectionModel().getSelectedIndex(), TreeAttributeTable.this.getColumns().get(1));
                    TreeAttributeTable.this.scrollTo(TreeAttributeTable.this.getSelectionModel().getSelectedIndex());
                }
            });
        }
    }
    
    public boolean isLeafNodesOnly()
    {
        return leafNodesOnly;
    }
    
    public void setLeafNodesOnly(boolean value)
    {
        boolean oldValue = leafNodesOnly;
        this.leafNodesOnly = value;
        
        if (leafNodesOnly && !oldValue)
            this.setItems(FXCollections.observableList(new ArrayList<>(canvas.getTreeModel().getLeafNodes())));
        else if (!leafNodesOnly && !oldValue)
            this.setItems(FXCollections.observableList(new ArrayList<>(canvas.getTreeModel().getNodes())));
    }
}
