/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

/**
 *
 * @author awehrer
 */
public class ColorByList extends AttributeAveragesTable
{
    private final ChangeListener<String> colorByAttributeListener;
    
    public ColorByList()
    {
        // add a filter to the row mouse events so that a row is deselected when clicked a second time.
        setRowFactory(new Callback<TableView<AttributeAveragesTableRowData>, TableRow<AttributeAveragesTableRowData>>()
        {
            @Override
            public TableRow<AttributeAveragesTableRowData> call(TableView<AttributeAveragesTableRowData> tableView)
            {
                final TableRow<AttributeAveragesTableRowData> row = new TableRow<>();
                
                row.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>()
                {
                    @Override
                    public void handle(MouseEvent event)
                    {
                        final int index = row.getIndex();
                        
                        if (index >= 0 && index < tableView.getItems().size() && tableView.getSelectionModel().isSelected(index))
                        {
                            tableView.getSelectionModel().clearSelection();
                            event.consume();
                        }
                    }
                });
                
                return row;
            }
        });
        
        getSelectionModel().getSelectedCells().addListener(new ListChangeListener<TablePosition>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends TablePosition> c)
            {
                if (ColorByList.this.getSelectionModel().isEmpty())
                {
                    ColorByList.this.getCanvas().setColorByAttribute(null);
                }
                else if (ColorByList.this.getSelectionModel().getSelectedItem().getAttributeName() == null ? ColorByList.this.getCanvas().getColorByAttribute() != null : !ColorByList.this.getSelectionModel().getSelectedItem().getAttributeName().equals(ColorByList.this.getCanvas().getColorByAttribute()))
                {
                    ColorByList.this.getCanvas().setColorByAttribute(ColorByList.this.getSelectionModel().getSelectedItem().getAttributeName());
                }
            }
        });
        
        colorByAttributeListener = new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                for (final AttributeAveragesTableRowData row : getItems())
                {
                    if ((row.getAttributeName() == null ? newValue == null : row.getAttributeName().equals(newValue)) && (getSelectionModel().getSelectedItem() != row))
                    {
                        Platform.runLater(new Runnable()
                        {
                            public void run()
                            {
                                getSelectionModel().select(row);
                            }
                        });
                        
                        return;
                    }
                }
            }
        };
    }
    
    @Override
    public void setCanvas(PhyloPenCanvas canvas)
    {
        if (getCanvas() != null)
        {
            getCanvas().colorByAttributeProperty().removeListener(colorByAttributeListener);
        }

        super.setCanvas(canvas);

        if (getCanvas() != null)
        {
            getCanvas().colorByAttributeProperty().addListener(colorByAttributeListener);
        }
    }
}
