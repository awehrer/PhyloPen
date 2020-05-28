/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import javafx.util.Pair;

/**
 *
 * @author awehrer
 */
public class CladeImageViewer extends Label
{
    private final static Image loadingIcon;
    private final static Image failedToLoadIcon;
    private final DoubleProperty maxPreviewImageWidth;
    private final DoubleProperty maxPreviewImageHeight;
    private ImagePreviewLoadOperation currentImageHoverLoadOperation;
    private Clade clade;
    private EventHandler<WorkerStateEvent> onFinishedLoadingHandler;
    
    static
    {
        loadingIcon = new Image("file:resources/images/image_loading_icon.png");
        failedToLoadIcon = new Image("file:resources/images/image_loading_failure_icon.png");
    }
    
    public CladeImageViewer()
    {
        maxPreviewImageWidth = new SimpleDoubleProperty(250.0);
        maxPreviewImageHeight = new SimpleDoubleProperty(250.0);
        setMinWidth(maxPreviewImageWidth.get());
        setMinHeight(100.0);
        
        setTextAlignment(TextAlignment.CENTER);
        setAlignment(Pos.CENTER);
        setTextFill(Color.LIGHTGRAY);
        setText("Image Unavailable");
        setStyle("-fx-border-width: 5px; -fx-border-color: lightgray; -fx-border-style: solid; -fx-font-size: 22px; -fx-font-weight: bold;");
        
        this.clade = null;
        
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                //System.out.println("Clicked");
                if (getClade() != null && getClade().getImageCount() > 0)
                {
                    //System.out.println("Dialog displayed");
                    new CladeImageViewerDialog(getClade()).showAndWait();
                }
            }
        });
    }
    
    public void setOnFinishedLoading(EventHandler<WorkerStateEvent> handler)
    {
        onFinishedLoadingHandler = handler;
    }
    
    public void setMaxPreviewImageWidth(double width)
    {
        maxPreviewImageWidth.set(width);
        setMinWidth(width);
    }
    
    public void setMaxPreviewImageHeight(double height)
    {
        maxPreviewImageHeight.set(height);
    }
    
    public double getMaxPreviewImageWidth()
    {
        return maxPreviewImageWidth.get();
    }
    
    public double getMaxPreviewImageHeight()
    {
        return maxPreviewImageHeight.get();
    }
    
    public Clade getClade()
    {
        return clade;
    }
    
    public void setClade(Clade clade)
    {
        if (this.clade != null)
        {
            CladeImageViewer.this.setGraphic(null);
            CladeImageViewer.this.setText("Image Unavailable");
            CladeImageViewer.this.setTextFill(Color.LIGHTGRAY);
            CladeImageViewer.this.setStyle("-fx-border-width: 5px; -fx-border-color: lightgray; -fx-border-style: solid; -fx-font-size: 22px; -fx-font-weight: bold;");
            CladeImageViewer.this.setMinWidth(getMaxPreviewImageWidth());
            CladeImageViewer.this.setMinHeight(100.0);
            if (currentImageHoverLoadOperation != null)
                currentImageHoverLoadOperation.cancel();
        }
        
        this.clade = clade;

        if (clade != null)
        {
            if (clade.getImageCount() > 0)
            {
                CladeImageViewer.this.setTextFill(Color.DARKGRAY);
                CladeImageViewer.this.setText("Loading...");
                CladeImageViewer.this.setStyle("-fx-border-width: 5px; -fx-border-color: lightgray; -fx-border-style: solid; -fx-font-size: 22px; -fx-font-weight: bold;");
                final Pair<String, String> imageInfo = clade.createImageIterator().next();
                currentImageHoverLoadOperation = new ImagePreviewLoadOperation(imageInfo.getKey(), imageInfo.getValue());
                Thread hoverLoadThread = new Thread(currentImageHoverLoadOperation);
                hoverLoadThread.setDaemon(true);
                hoverLoadThread.start();
            }
        }
    }
    
    public void refreshPreview()
    {
        setClade(getClade());
    }
    
    public class ImagePreviewLoadOperation extends Task<Void>
    {
        private String url;
        private String description;
        
        public ImagePreviewLoadOperation(String url, String description)
        {
            this.url = url;
            this.description = description;
            this.setOnSucceeded(onFinishedLoadingHandler);
            this.setOnFailed(onFinishedLoadingHandler);
        }
        
        @Override
        public Void call()
        {
            if (!isCancelled())
            {
                ArborImageRetriever imageRetriever = new ArborImageRetriever();
                Image image = imageRetriever.getImage(url);
                if (image == null)
                    image = failedToLoadIcon;
                final ImageView imageView = new ImageView(image);

                if (image.getWidth() > CladeImageViewer.this.getMaxPreviewImageWidth() || image.getHeight() > CladeImageViewer.this.getMaxPreviewImageWidth())
                {
                    imageView.setFitWidth(CladeImageViewer.this.getMaxPreviewImageWidth());
                    imageView.setFitHeight(CladeImageViewer.this.getMaxPreviewImageHeight());
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                }
                
                
                Platform.runLater(new Runnable()
                {
                    public void run()
                    {
                        if (!isCancelled())
                        {
                            CladeImageViewer.this.setText(null);
                            CladeImageViewer.this.setGraphic(imageView);
                            CladeImageViewer.this.setMinWidth(Region.USE_PREF_SIZE);
                            CladeImageViewer.this.setMinHeight(Region.USE_PREF_SIZE);
                            if (getClade().getImageCount() > 1)
                                CladeImageViewer.this.setStyle("-fx-border-width: 5px 5px 5px 5px; -fx-border-color: #F8F8FF #FFD700 #FFD700 #F8F8FF; -fx-border-radius: 1px 1px 1px 1px; -fx-border-style: solid; -fx-font-size: 22px; -fx-font-weight: bold;");
                            else
                                CladeImageViewer.this.setStyle("-fx-border-width: 5px; -fx-border-color: #F8F8FF; -fx-border-style: solid; -fx-font-size: 22px; -fx-font-weight: bold;");
                        }
                    }
                });
            }
            
            return null;
        }
    }
    
    public class CladeImageViewerDialog extends Dialog<Void>
    {
        private Clade clade;
        private GroupImageLoadOperation imageLoadOperation;
        private ObjectProperty<Label> selectedProperty;
        private Button deleteButton;
        
        public CladeImageViewerDialog(Clade clade)
        {
            this.setTitle("Images");
            selectedProperty = new SimpleObjectProperty(null);
            
            selectedProperty().addListener((ObservableValue<? extends Label> observable, Label oldValue, Label newValue) ->
            {
                if (oldValue != null)
                    oldValue.setStyle("-fx-font-weight: bold; -fx-border-width: 1; -fx-border-color: black; -fx-border-style: solid;");
                if (newValue != null)
                {
                    newValue.setStyle("-fx-font-weight: bold; -fx-border-width: 3; -fx-border-color: #0078d7; -fx-border-style: solid;");
                    deleteButton.setDisable(false);
                }
                else
                    deleteButton.setDisable(true);
            });
            
            this.setOnCloseRequest(new EventHandler<DialogEvent>()
            {
                @Override
                public void handle(DialogEvent event)
                {
                    imageLoadOperation.cancel();
                }
            });

            this.clade = clade;
            
            double imageGridWidth = 900.0;
            double imageGridHeight = 600.0;
            double imageWidth, imageHeight;
            int numColumns, numVisibleRows;
            
            getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            StackPane mainContentPane = new StackPane();
            mainContentPane.setMinWidth(imageGridWidth);
            mainContentPane.setMinHeight(imageGridHeight);
            
            if (clade.getImageCount() == 1)
            {
                numColumns = 1;
                numVisibleRows = 1;
            }
            else
            {
                numColumns = 3;
                numVisibleRows = 3;
            }
            
            imageWidth = imageGridWidth / numColumns;
            imageHeight = imageGridHeight / numVisibleRows;

            final GridPane imageGridPane = new GridPane();
            ScrollPane gridScrollPane = new ScrollPane(imageGridPane);
            gridScrollPane.setMaxWidth(imageGridWidth + 100.0);
            gridScrollPane.setMaxHeight(imageGridHeight + 100.0);
            imageGridPane.setHgap(5.0);
            imageGridPane.setVgap(5.0);
            imageGridPane.setPadding(new Insets(2.0, 2.0, 2.0, 2.0));

            Label label;
            imageLoadOperation = new GroupImageLoadOperation();

            Iterator<Pair<String, String>> imageInfoIterator = clade.createImageIterator();

            int row = 0, col = 0;

            while (imageInfoIterator.hasNext())
            {
                Pair<String, String> imageInfo = imageInfoIterator.next();

                label = new Label();
                label.setAlignment(Pos.CENTER);
                label.setWrapText(true);
                label.setTextAlignment(TextAlignment.CENTER);
                label.setGraphicTextGap(2.0);
                label.setContentDisplay(ContentDisplay.TOP);
                label.setStyle("-fx-font-weight: bold; -fx-border-width: 1; -fx-border-color: black; -fx-border-style: solid;");
                GridPane.setValignment(label, VPos.CENTER);
                imageGridPane.add(label, col, row);

                col++;

                if (col == numColumns)
                {
                    col = 0;
                    row++;
                }

                label.setPrefWidth(imageWidth);
                label.setMinWidth(Label.USE_PREF_SIZE);
                label.setMaxWidth(Label.USE_PREF_SIZE);

                VBox loadingViewRegion = new VBox();
                loadingViewRegion.setAlignment(Pos.CENTER);
                ImageView loadingView = new ImageView(loadingIcon);
                loadingViewRegion.getChildren().add(loadingView);
                loadingViewRegion.setPrefHeight(imageHeight);
                loadingViewRegion.setMinHeight(GridPane.USE_PREF_SIZE);
                loadingViewRegion.setMaxHeight(GridPane.USE_PREF_SIZE);
                label.setGraphic(loadingViewRegion);

                label.setText(imageInfo.getValue());

                imageLoadOperation.addLoadRequest(imageInfo.getKey(), imageWidth, imageHeight, label);
                
                label.setOnSwipeLeft((SwipeEvent event) ->
                {
                    Label target = (Label) event.getSource();
                    int currentIndex = imageGridPane.getChildren().indexOf(target);
                    if (imageGridPane.getChildren().size() > 1 && currentIndex > 0)
                    {
                        ObservableList<Node> workingCollection = FXCollections.observableArrayList(imageGridPane.getChildren());
                        int targetRow = GridPane.getRowIndex(target);
                        int targetColumn = GridPane.getColumnIndex(target);
                        Label neighbor = (Label) workingCollection.get(currentIndex - 1);
                        
                        GridPane.setRowIndex(target, GridPane.getRowIndex(neighbor));
                        GridPane.setColumnIndex(target, GridPane.getColumnIndex(neighbor));
                        GridPane.setRowIndex(neighbor, targetRow);
                        GridPane.setColumnIndex(neighbor, targetColumn);
                        
                        Collections.swap(workingCollection, currentIndex - 1, currentIndex);
                        imageGridPane.getChildren().setAll(workingCollection);
                        
                        clade.swapImageIndices(currentIndex - 1, currentIndex);
                        
                        if (currentIndex - 1 == 0)
                            CladeImageViewer.this.refreshPreview();
                    }
                });
                
                label.setOnSwipeRight((SwipeEvent event) ->
                {
                    Label target = (Label) event.getSource();
                    int currentIndex = imageGridPane.getChildren().indexOf(target);
                    if (imageGridPane.getChildren().size() > 1 && currentIndex < imageGridPane.getChildren().size() - 1)
                    {
                        ObservableList<Node> workingCollection = FXCollections.observableArrayList(imageGridPane.getChildren());
                        int targetRow = GridPane.getRowIndex(target);
                        int targetColumn = GridPane.getColumnIndex(target);
                        Label neighbor = (Label) workingCollection.get(currentIndex + 1);
                        
                        GridPane.setRowIndex(target, GridPane.getRowIndex(neighbor));
                        GridPane.setColumnIndex(target, GridPane.getColumnIndex(neighbor));
                        GridPane.setRowIndex(neighbor, targetRow);
                        GridPane.setColumnIndex(neighbor, targetColumn);
                        
                        Collections.swap(workingCollection, currentIndex, currentIndex + 1);
                        imageGridPane.getChildren().setAll(workingCollection);
                        
                        clade.swapImageIndices(currentIndex, currentIndex + 1);
                        
                        if (currentIndex == 0)
                            CladeImageViewer.this.refreshPreview();
                    }
                });
                
                label.setOnTouchPressed((TouchEvent event) ->
                {
                    setSelected((Label)event.getSource());
                });
            }

            mainContentPane.getChildren().add(gridScrollPane);
            
            VBox buttonPane = new VBox();
            deleteButton = new Button(null, new ImageView(new Image("file:resources/images/trash_icon_52_x_52.png")));
            deleteButton.setMinHeight(200);
            deleteButton.setMinWidth(72);
            deleteButton.setDisable(true);
            Button addButton = new Button(null, new ImageView(new Image("file:resources/images/add_icon_52_x_52.png")));
            addButton.setMinHeight(200);
            addButton.setMinWidth(72);
            buttonPane.getChildren().add(deleteButton);
            buttonPane.getChildren().add(addButton);
            
            deleteButton.setOnAction((ActionEvent event) ->
            {
                clade.removeImage(imageGridPane.getChildren().indexOf(getSelected()));
                imageGridPane.getChildren().remove(getSelected());
                setSelected(null);
            });
            
            addButton.setOnAction((ActionEvent event) ->
            {
                NewImageRequestDialog dialog = new NewImageRequestDialog();
                Optional<Boolean> result = dialog.showAndWait();
            });
            
            HBox mainContentPaneWButtons = new HBox();
            mainContentPaneWButtons.getChildren().add(mainContentPane);
            mainContentPaneWButtons.getChildren().add(buttonPane);
            
            getDialogPane().setContent(mainContentPaneWButtons);
            
            Thread imageLoadThread = new Thread(imageLoadOperation);
            imageLoadThread.setDaemon(true);
            imageLoadThread.start();

            this.setResultConverter(new Callback<ButtonType, Void>()
            {
                @Override
                public Void call(ButtonType param)
                {
                    return null;
                }
            });
        }
        
        protected ObjectProperty<Label> selectedProperty()
        {
            return selectedProperty;
        }
        
        protected void setSelected(Label image)
        {
            selectedProperty().set(image);
        }
        
        protected Label getSelected()
        {
            return selectedProperty().get();
        }
        
        private class GroupImageLoadOperation extends Task<Void>
        {
            private class ImageLoadRequest
            {
                private String url;
                private double maxImageWidth;
                private double maxImageHeight;
                private Label destinationComponent;

                public ImageLoadRequest(String url, double maxImageWidth, double maxImageHeight, Label component)
                {
                    this.url = url;
                    this.maxImageWidth = maxImageWidth;
                    this.maxImageHeight = maxImageHeight;
                    this.destinationComponent = component;
                }

                public String getUrl()
                {
                    return url;
                }

                public double getMaxImageWidth()
                {
                    return maxImageWidth;
                }

                public double getMaxImageHeight()
                {
                    return maxImageHeight;
                }

                public Label getDestinationComponent()
                {
                    return destinationComponent;
                }
            }

            private List<ImageLoadRequest> loadRequests;

            public GroupImageLoadOperation()
            {
                loadRequests = new ArrayList<>();
            }

            public void addLoadRequest(String imageUrl, double maxImageWidth, double maxImageHeight, Label component)
            {
                loadRequests.add(new ImageLoadRequest(imageUrl, maxImageWidth, maxImageHeight, component));
            }

            @Override
            protected Void call()
            {
                if (!isCancelled())
                {
                    ArborImageRetriever imageRetriever = new ArborImageRetriever();

                    for (ImageLoadRequest loadRequest : loadRequests)
                    {
                        final String url = loadRequest.getUrl();
                        final Label component = loadRequest.getDestinationComponent();

                        final Image image = imageRetriever.getImage(url);
                        final ImageView imageView;

                        if (image == null)
                        {
                            imageView = new ImageView(failedToLoadIcon);
                        }
                        else
                        {
                            imageView = new ImageView(image);
                            if (image.getWidth() > loadRequest.getMaxImageWidth() || image.getHeight() > loadRequest.getMaxImageHeight())
                            {
                                imageView.setFitWidth(loadRequest.getMaxImageWidth());
                                imageView.setFitHeight(loadRequest.getMaxImageHeight());
                                imageView.setPreserveRatio(true);
                                imageView.setSmooth(true);
                            }

                            imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
                            {
                                @Override
                                public void handle(MouseEvent event)
                                {
                                    if (event.getClickCount() >= 2)
                                    {
                                        final StackPane mainContentPane = (StackPane) ((HBox)CladeImageViewerDialog.this.getDialogPane().getContent()).getChildren().get(0);
                                        final VBox largeImagePane = new VBox();
                                        largeImagePane.setStyle("-fx-background-color: white;");
                                        largeImagePane.setAlignment(Pos.CENTER);

                                        // make a copy of the image
                                       /* PixelReader pixelReader = image.getPixelReader();

                                        int width = (int)image.getWidth();
                                        int height = (int)image.getHeight();

                                        // Copy from source to destination pixel by pixel
                                        WritableImage copyImage = new WritableImage(width, height);
                                        PixelWriter pixelWriter = copyImage.getPixelWriter();

                                        for (int y = 0; y < height; y++)
                                        {
                                            for (int x = 0; x < width; x++)
                                            {
                                                Color color = pixelReader.getColor(x, y);
                                                pixelWriter.setColor(x, y, color);
                                            }
                                        }*/

                                        ImageView largeImageView = new ImageView(image);
                                        Label largeImageLabel = new Label();
                                        largeImageLabel.setGraphic(largeImageView);
                                        largeImagePane.getChildren().add(largeImageView);
                                        largeImagePane.setPrefHeight(mainContentPane.getHeight());
                                        largeImagePane.setMinHeight(GridPane.USE_PREF_SIZE);
                                        largeImagePane.setMaxHeight(GridPane.USE_PREF_SIZE);
                                        largeImagePane.setPrefWidth(mainContentPane.getWidth());
                                        largeImagePane.setMinWidth(GridPane.USE_PREF_SIZE);
                                        largeImagePane.setMaxWidth(GridPane.USE_PREF_SIZE);

                                        if (image.getWidth() > mainContentPane.getWidth() || image.getHeight() > mainContentPane.getHeight())
                                        {
                                            largeImageView.setFitWidth(mainContentPane.getWidth());
                                            largeImageView.setFitHeight(mainContentPane.getHeight());
                                            largeImageView.setPreserveRatio(true);
                                            largeImageView.setSmooth(true);
                                        }

                                        mainContentPane.getChildren().add(largeImagePane);

                                        largeImagePane.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
                                        {
                                            @Override
                                            public void handle(MouseEvent event)
                                            {
                                                mainContentPane.getChildren().remove(largeImagePane);
                                            }
                                        });
                                    }
                                }
                            });
                        }

                        final VBox imageViewRegion = new VBox();
                        imageViewRegion.setAlignment(Pos.CENTER);
                        imageViewRegion.getChildren().add(imageView);
                        imageViewRegion.setPrefHeight(loadRequest.getMaxImageHeight());
                        imageViewRegion.setMinHeight(GridPane.USE_PREF_SIZE);
                        imageViewRegion.setMaxHeight(GridPane.USE_PREF_SIZE);

                        Platform.runLater(new Runnable()
                        {
                            public void run()
                            {
                                if (!isCancelled())
                                {
                                    component.setGraphic(imageViewRegion);
                                }
                            }
                        });
                    }
                }
                return null;
            }
        }
    }
    
    private class NewImageRequestDialog extends Dialog<Boolean>
    {
        private final TextField urlTextField;
        private final TextField captionTextField;

        public NewImageRequestDialog()
        {
            this.setTitle("Add Image");

            getDialogPane().getButtonTypes().add(ButtonType.OK);
            getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

            Label urlLabel = new Label("URL:");
            Label captionLabel = new Label("Caption:");

            urlTextField = new TextField();
            urlTextField.setPrefColumnCount(22);
            urlTextField.setPromptText("Enter url");
            
            captionTextField = new TextField();
            captionTextField.setPrefColumnCount(22);
            captionTextField.setPromptText("Enter caption");

            HBox urlTextPane = new HBox(4);
            urlTextPane.getChildren().addAll(urlLabel, urlTextField);
            HBox captionTextPane = new HBox(4);
            captionTextPane.getChildren().addAll(captionLabel, captionTextField);

            VBox mainContentPane = new VBox(10);
            mainContentPane.getChildren().addAll(urlTextPane, captionTextPane);

            getDialogPane().setContent(mainContentPane);

            this.setResultConverter(new Callback<ButtonType, Boolean>()
            {
                @Override
                public Boolean call(ButtonType param)
                {
                    if (!param.equals(ButtonType.CANCEL))
                    {
                        clade.addImage(urlTextField.getText(), captionTextField.getText());
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            });

            final Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event ->
            {
                if (urlTextField.getText().equals(""))
                    event.consume();
            });

            this.setOnShown(new EventHandler<DialogEvent>()
            {
                public void handle(DialogEvent event)
                {
                    Platform.runLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            NewImageRequestDialog.this.urlTextField.requestFocus();
                        }
                    });
                }
            });
        }
    }
}
