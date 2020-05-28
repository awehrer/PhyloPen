/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import java.util.ArrayList;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import phylopen.PhyloPenCanvas.AnnotationPolygon;
import phylopen.utility.RemScaler;

/**
 *
 * @author Work
 */
public class AnnotationList extends ListView<Annotation>
{
    private PhyloPenCanvas canvas;
    private ListChangeListener<Annotation> annotationChangeListener;
    private ListChangeListener<AnnotationPolygon> annotationPlaceholderChangeListener;
    
    public final double CELL_HEIGHT;
    
    public AnnotationList()
    {
        CELL_HEIGHT = new RemScaler().scale(70.0);
        
        annotationChangeListener = new ListChangeListener<Annotation>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Annotation> c)
            {
                while (c.next())
                {
                    if (c.wasAdded() && !c.wasRemoved())
                    {
                        AnnotationList.this.getItems().addAll(c.getFrom(), new ArrayList<>(c.getAddedSubList()));
                    }
                    else if (!c.wasAdded() && c.wasRemoved())
                    {
                        AnnotationList.this.getItems().removeAll(c.getRemoved());
                    }
                }
            }
        };
        
        annotationPlaceholderChangeListener = new ListChangeListener<AnnotationPolygon>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends AnnotationPolygon> c)
            {
                while (c.next())
                {
                    if (c.wasAdded() && !c.wasRemoved())
                    {
                        for (AnnotationPolygon annotationMarker : new ArrayList<>(c.getAddedSubList()))
                            addScrollToHandler(annotationMarker);
                    }
                }
            }
        };
        
        this.setCellFactory(new Callback<ListView<Annotation>, ListCell<Annotation>>()
        {
            @Override
            public ListCell<Annotation> call(ListView<Annotation> list)
            {
                return new AnnotationListCell();
            }
        });
    }
    
    public void setCanvas(PhyloPenCanvas canvas)
    {
        if (this.canvas != null)
        {
            this.canvas.getAnnotations().removeListener(annotationChangeListener);
            this.canvas.getAnnotationPlaceholders().removeListener(annotationPlaceholderChangeListener);
            this.getItems().clear();
        }

        this.canvas = canvas;

        if (this.canvas != null)
        {
            this.canvas.getAnnotations().addListener(annotationChangeListener);
            this.canvas.getAnnotationPlaceholders().addListener(annotationPlaceholderChangeListener);
            
            this.getItems().addAll(this.canvas.getAnnotations());
            
            for (AnnotationPolygon annotationMarker : canvas.getAnnotationPlaceholders())
                addScrollToHandler(annotationMarker);
        }
    }
    
    private void addScrollToHandler(final AnnotationPolygon annotationMarker)
    {
        annotationMarker.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                AnnotationList.this.getSelectionModel().select(annotationMarker.getAnnotation());
                AnnotationList.this.scrollTo(AnnotationList.this.getSelectionModel().getSelectedIndex());
            }
        });
    }
    
    private class AnnotationListCell extends ListCell<Annotation>
    {
        public AnnotationListCell()
        {
            addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>()
            {
                @Override
                public void handle(MouseEvent event)
                {
                    final int index = AnnotationListCell.this.getIndex();

                    if (index >= 0 && index < AnnotationList.this.getItems().size() && AnnotationList.this.getSelectionModel().isSelected(index))
                    {
                        AnnotationList.this.getSelectionModel().clearSelection(index);
                        event.consume();
                    }
                }
            });
        }

        @Override
        protected void updateItem(Annotation item, boolean empty)
        {
            super.updateItem(item, empty);

            if (empty || item == null)
            {
                setText(null);
                setGraphic(null);
            }
            else
            {
                if (item.getText() == null)
                    setText("null");
                else
                    setText(item.toString());
            }

            setStyle("-fx-cell-size: " + CELL_HEIGHT + "; -fx-alignment: CENTER-LEFT; -fx-font-size: 1.2em;");
        }
    }
}
