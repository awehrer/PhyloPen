/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen.utility;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

/**
 *
 * @author Arbor
 */
public class SingleDirectoryFileChooserDialog extends Dialog<File>
{
    public final double CELL_HEIGHT;
    private List<String> extensionFilters;
    private FileListView fileList;
    
    public SingleDirectoryFileChooserDialog(String directory) throws IOException
    {
        this(directory, null);
    }
    
    public SingleDirectoryFileChooserDialog(String directory, String [] fileExtensions) throws IOException
    {
        this(new File(directory), fileExtensions);
    }
    
    public SingleDirectoryFileChooserDialog(File directory) throws IOException
    {
        this(directory, null);
    }
    
    public SingleDirectoryFileChooserDialog(File directory, String [] fileExtensions) throws IOException
    {
        RemScaler rem = new RemScaler();
        
        CELL_HEIGHT = rem.scale(70.0);
        
        if (!directory.isDirectory())
            throw new IOException("Not a directory");
        
        this.setTitle("Open");
        
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        
        extensionFilters = new ArrayList<>(fileExtensions.length);
        
        if (fileExtensions != null)
        {
            for (String ext : fileExtensions)
                extensionFilters.add(ext);
        }
        
        fileList = new FileListView(directory.listFiles(new PhyloFilenameFilter()));
        //System.out.println(directory.listFiles(new PhyloFilenameFilter()).length);
        fileList.setPrefSize(rem.scale(400), rem.scale(400));
        getDialogPane().setContent(fileList);
        this.setResizable(true);
        
        // set result converter
        this.setResultConverter(new Callback<ButtonType, File>()
        {
            @Override
            public File call(ButtonType param)
            {
                if (param.getButtonData().isCancelButton())
                    return null;
                
                return fileList.getSelectionModel().getSelectedItem();
            }
        });
        
        final Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event ->
        {
            if (fileList.getSelectionModel().getSelectedItem() == null)
            {
                event.consume();
                new Alert(Alert.AlertType.ERROR, "No file selected.", ButtonType.OK).showAndWait();
            }
        });
    }
    
    public ListView getFileList()
    {
        return fileList;
    }
    
    private class PhyloFilenameFilter implements FilenameFilter
    {
        @Override
        public boolean accept(File dir, String name)
        {
            if (extensionFilters.isEmpty())
                return true;
            
            for (String filter : extensionFilters)
            {
                if (name.toLowerCase().endsWith("." + filter.toLowerCase()))
                    return true;
            }
            
            return false;
        }
    }
    
    private class FileListView extends ListView<File>
    {
        public FileListView(File [] files)
        {
            getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            ObservableList list = FXCollections.observableArrayList();
            for (File file : files)
                list.add(file);
            setItems(list);

            this.setCellFactory(new Callback<ListView<File>, ListCell<File>>()
            {
                @Override
                public ListCell<File> call(ListView<File> list)
                {
                    return new FileListCell();
                }
            });
        }
        
        private class FileListCell extends ListCell<File>
        {
            public FileListCell()
            {
                addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>()
                {
                    @Override
                    public void handle(MouseEvent event)
                    {
                        final int index = FileListCell.this.getIndex();

                        if (index >= 0 && index < FileListView.this.getItems().size() && FileListView.this.getSelectionModel().isSelected(index))
                        {
                            FileListView.this.getSelectionModel().clearSelection(index);
                            event.consume();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(File item, boolean empty)
            {
                super.updateItem(item, empty);

                if (empty || item == null)
                {
                    setText(null);
                    setGraphic(null);
                }
                else
                {
                    setText(item.getName());
                }

                setStyle("-fx-cell-size: " + CELL_HEIGHT + "; -fx-alignment: CENTER-LEFT; -fx-font-size: 1.2em;");
            }
        }
    }
}
