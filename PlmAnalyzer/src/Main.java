import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Menu;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;

/*
 * GUI for PLM Analyzer system
 * @version 2016_05_04
 * @author Jennifer Hunter
 */
@SuppressWarnings("restriction")
public class Main extends Application {
	// Data point text above the graphs
	private Text dataPoint; 
	// Average PLM events per hour 
	private DoubleProperty eph = new SimpleDoubleProperty();
	// Menus
	private Menu file, paramSetup,
		reports;

	// Threshold value - default to 1.0
	private double thresholdValue = 1.0;
	// Data retrieved from csv
	private ObservableList <LineChart.Data<Date, Number>> data;
	// Movement analysis retrieved from csv
	private ObservableList <Movement> mov;
	// How many data points can be seen at a screen at once - default = 5.0
	private int screenCapacity = 5;
	// Format for dates on x-axis
	private SimpleDateFormat dateFormat;
	// Reference to the line chart and its series
	private LineChartWithMarkers chart;
	// Reference to the Axes
	private DateAxis xAxis;
	private NumberAxis yAxis;
	// Reference to the slider GUI object
	private Slider slider;
	// Vertical Box for holding content
	private VBox content;
	// Reference to the borderpane
	private BorderPane borderpane;
	// Night count for report
	private int night;
	// Stage reference variable
	private Stage primary;
	// Dyncamic values for report dialog width and height
	private int h;
	
	//private XYChart.Series<Date, Number> threshold;
	    
	/**
	* Method to start the GUI
	*
	* @param stage Stage for displaying
	*/
	@SuppressWarnings({ "unchecked", "rawtypes"})
	public void start(Stage stage) {
		// Set the stage
		primary = stage;
		// Store the raw data points in an observable array list
		data = FXCollections.observableArrayList();
		// Store the analyzed movement data in an observable array list
		mov = FXCollections.observableArrayList();
		
		// Create a vertical box to show the chartsc3
	    content = new VBox();
	    
		// Date Format Year-Month-Day Hour(24):Minute:Second.Milliseconds
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		// Create data point text
		dataPoint = new Text ("Force: ; Time: ");
		dataPoint.setTextAlignment(TextAlignment.CENTER);
		dataPoint.setTextOrigin(VPos.CENTER);   
	    
	    // Define the axes
	    xAxis = new DateAxis();
	    yAxis = new NumberAxis(0, 7, 1);
	    
	    // Name the axes
	    xAxis.setLabel("Time");
	    yAxis.setLabel("Force (g)");
	    
	    // Format the axes
	    xAxis.setAutoRanging(true);
	    yAxis.setAutoRanging(true);
	
	    //Create the line chart
	    chart = new LineChartWithMarkers(xAxis, yAxis);
	    // Set chart title
	    chart.setTitle("Leg Movement Analysis");
	    // Hide datepoint symbols
	    chart.setCreateSymbols(false);
	    // Hide the chart legend
	    chart.setLegendVisible(false); 
	    // Make the horizontal grid lines visible for easier reference
	    chart.setHorizontalGridLinesVisible(true);
	    // Turn off animation
	    chart.setAnimated(false);
	    // Change the cursor to a crosshair when on the chart
	    chart.setCursor(Cursor.CROSSHAIR);
	   
	    
	    // Set initial value of average to zero
	    // Note: This was added as a framework for displaying a dynamic events per hour text
	    // This is not displayed on the main GUI. Something simliar is seen in the Nightly Report section.
	    eph.set(0.0);
	    Text ephText = new Text();
	    eph.addListener(new ChangeListener(){
	        @Override public void changed(ObservableValue o,Object oldVal, 
	                 Object newVal){
	             ephText.setText("\tPLM events per hour: " + eph.get());
	             // Change the text color depending on if the criteria is met
	             try {
	            	 double val = (double) newVal;
	            	 if (val < 5.0) {
	            		 ephText.setFill(Color.BLACK);
	            	 } else {
	            		 ephText.setFill(Color.RED);
	            	 }
	             } catch (Exception e){
	            	 System.out.println(e.toString());
	             }
	        }
	      });
	    
	    // Setup time slider
	    slider = new Slider();
	    slider.setMin(0);
	    slider.setMax(0);
	    slider.setValue(0);
	    slider.setShowTickMarks(true);

	    // Create a borderpane to hold all GUI objects
	    borderpane = new BorderPane();
	    
	    // Setup the top menu toolbar
	    MenuBar menuBar = new MenuBar();
	    setupMenu(stage);
	    menuBar.getMenus().addAll(file, paramSetup, reports);
	    
	    // Add spacing between the children
	    content.setSpacing(15.0);
	    // Add the dataPoint text, the charts, the slider, the table, and the analyze button
	    content.getChildren().addAll(dataPoint, chart);
	    VBox.setVgrow(chart, Priority.ALWAYS);
	    
	    // Add the menu to the top of the GUI
	    borderpane.setTop(menuBar);
	    // Anchor the slider to the bottom
	    borderpane.setBottom(slider);
	    // Anchor the content to the left
	    borderpane.setCenter(content);
	    // Add extra space around the content
	    BorderPane.setMargin(content, new Insets(0,50,0,15));
	    
	    ///DEBUG - change eph value
	    eph.set(26.0);
	
	    // Add the borderpane containing the menu and content to the GUI scene
	    Scene scene = new Scene(borderpane);
	    stage.setScene(scene);
	    // Change the width of the stage
	    stage.setWidth(800);
	
	    // Set the Title of the GUI (shows up on top bar)
	    stage.setTitle("PLM Analyzer");
	    // Set the GUI to be visible
	    stage.show();
	}
	
	/**
	* Initialize Menus for the Menu Toolbar
	*/
	public void setupMenu(Stage stage) {
	
	    // Toolbar Menu creation
	    file = new Menu("File");
	    setupFile(stage);
	
	    paramSetup = new Menu("Parameter Setup");
	    setupParamSetup();
	
	    reports = new Menu("Reports");
	    setupReports();
	}
	
	/**
	 * Initialize Menu Items for the File menu
	 */
	public void setupFile(Stage stage) {
		// Open
		 MenuItem open = new MenuItem("Open");
		 open.setOnAction(new EventHandler<ActionEvent>() {
			   @SuppressWarnings({ "unchecked"})
			public void handle(ActionEvent t) {
				   // Open a file choose dialog to decide which file to open
				   FileChooser fileChooser = new FileChooser();
				   fileChooser.setTitle("Open Data File");
				   fileChooser.getExtensionFilters().addAll(
						   // Only look for text files and csv files
						   new FileChooser.ExtensionFilter("CSV", "*.csv"),
			                new FileChooser.ExtensionFilter("TXT", "*.txt*"));
				   File openFile =  fileChooser.showOpenDialog(stage);
				   if (openFile != null && openFile.exists()) { 
					   // Remove the old chart and slider from the GUI
					   content.getChildren().remove(chart);
					   borderpane.getChildren().remove(slider);
	                	// Extract the data from the csv
						extractData(openFile);
						// Define the axes
					    xAxis = new DateAxis();
					    yAxis = new NumberAxis(0, 7, 1);
					    
					    // Name the axes
					    xAxis.setLabel("Time");
					    yAxis.setLabel("Force (g)");
					    
					    // Format the axes
					    xAxis.setAutoRanging(true);
					    yAxis.setAutoRanging(true);
					    
						//Create the line chart
					    chart = new LineChartWithMarkers(xAxis, yAxis);
					    // Set chart title
					    chart.setTitle("Leg Movement Analysis");
					    // Set node ID
					    chart.setId("chart");
					    // Hide datepoint symbols
					    chart.setCreateSymbols(false);
					    // Hide the chart legend
					    chart.setLegendVisible(false); 
					    // Make the horizontal grid lines visible for easier reference
					    chart.setHorizontalGridLinesVisible(true);
					    // Turn off animation
					    chart.setAnimated(false);
					    
					    // Change the data point text when the mouse is moved over the chart
						chart.setOnMouseMoved(new EventHandler<MouseEvent>() {
						      @Override public void handle(MouseEvent mouseEvent) {
						    	  // Create a 2D point for where the mouse is located in the scene
						    	  Point2D pointInScene = new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY());
						    	  // Calculate what that 2D x-value corresponds to in reference to the x-axis
						    	  double xPosInAxis = xAxis.sceneToLocal(new Point2D(pointInScene.getX(), 0)).getX();
						    	  // Calculate what that 2D x-y-value corresponds to in reference to the y-axis
						    	  double yPosInAxis = yAxis.sceneToLocal(new Point2D(0, pointInScene.getY())).getY();
						    	  // Get the chart values corresponding to the values at the axes
						    	  Date x = xAxis.getValueForDisplay(xPosInAxis);
						    	  double y = yAxis.getValueForDisplay(yPosInAxis).doubleValue();
						    	    
						    	  //Format the number to a prettier string
						    	  NumberFormat numFormat = new DecimalFormat("#0.00");     
							      // Update the string label on the top of the chart
						    	  dataPoint.setText("Force: " + numFormat.format(y)
							          	+ "; Time: " + dateFormat.format(x));
						      	}
						});

					    // Add a horizontal marker as a threshold
					    XYChart.Data<Date, Number> horizontalMarker = new XYChart.Data<Date, Number>(new Date(), thresholdValue);
				        chart.addHorizontalValueMarker(horizontalMarker);
				        
				        // Add a series based off the data
				        chart.getData().add(new LineChart.Series<Date, Number>("RawData", data));

					    // Change the cursor to a crosshair when on the chart
					    chart.setCursor(Cursor.CROSSHAIR);	
					    
					    // Setup time slider
					    slider = new Slider();
					    slider.setMin(0);
					    slider.setValue(0);
					    slider.setShowTickMarks(true);
					    slider.setMajorTickUnit(10);
					    slider.setMinorTickCount(5);
					    slider.setBlockIncrement(5);
					    
	            		if (chart != null) {
		            		LineChart.Series<Date, Number> s = (LineChart.Series<Date, Number>) chart.getData().get(0);
		            		ObservableList<LineChart.Data <Date,Number>> chartData = s.getData();
		            		// Set the max slider value of the slider
		            		int max = chartData.size() == 0? 0: chartData.size() - 1;
		            		slider.setMax(max);
	            		}
	            		
					    // Change the chart view when the slider is moved
					    slider.valueProperty().addListener((
				            ObservableValue<? extends Number> ov, 
				            Number oldVal, Number newVal) -> {
				            	if (data.size() > 0) {
				            		// Get an updated reference of the chart on the graph
				            		LineChart<Date,Number> c = (LineChart<Date,Number>) content.lookup("#chart");
				            		if (c != null) {
					            		LineChart.Series<Date, Number> s = (LineChart.Series<Date, Number>) c.getData().get(0);
					            		ObservableList<LineChart.Data <Date,Number>> chartData = s.getData();
					            		// Set the max slider value of the slider
					            		int max = chartData.size() == 0? 0: chartData.size() - 1;
					            		slider.setMax(max);
					            		slider.setMin(0);
					            		
					            		slider.setFocusTraversable(true);
					            		
							            // the upper bound is the value of the slider
							            int up = (int) Math.round((double)newVal);
							            // the lower bound is the slider value minus the screen capacity
							            int low = (int) Math.round((double)newVal) - screenCapacity;
							            // If the lower bound is less than 0, set it to 0
							            low = (low < 0)? 0 : low;
							            // If the lower bound is greater than max - screen capacity,
							            // set it equal to the max slider value - screen capacity
							            low = (low > max - screenCapacity)? 
							            		max - screenCapacity : low;
							            // If the upper bound, is less than the screen capacity,
							            // set it to the screen capacity
							            up = (up < screenCapacity)? screenCapacity : up;
							
							            // Get the date values for the bounds
							            Date upper = chartData.get(up).getXValue();
							            Date lower = chartData.get(low).getXValue();
							
							            // Set the upper and lower bounds of the chart
							            ((DateAxis)c.getXAxis()).setUpperBound(upper);
							            ((DateAxis)c.getXAxis()).setLowerBound(lower);
							            
							            
				            		} else {
				            			System.out.println("Chart is null.");
				            			
				            		}
				            	}
				        });
					    
					    // Add the new chart and slider to the GUI
						content.getChildren().add(chart);
						VBox.setVgrow(chart, Priority.ALWAYS);
						borderpane.setBottom(slider);
				   	}
			   }}
		);
		 
		 
		 // Add the open menu item under the file menu
		 file.getItems().add(open);
		 //Setup Ctrl+O to activate open
		 open.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
		 
		// Save As
		 MenuItem saveAs = new MenuItem("Save Analysis As");
		 saveAs.setOnAction(new EventHandler<ActionEvent>() {
			   public void handle(ActionEvent t) {
				   // Open a file chooser to decide where to save the file
				   FileChooser fileChooser1 = new FileChooser();
				   fileChooser1.setTitle("Save As");
				 //Set extension filter
		              FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
		              fileChooser1.getExtensionFilters().add(extFilter);
				   File saveFile = fileChooser1.showSaveDialog(stage);
				  
			       if (saveFile != null){
			    	   BufferedWriter writer = null;
					   try {
				            // Initialize the writer for the given save file
				            writer = new BufferedWriter(new FileWriter(saveFile));
				            // Write the header
				            writer.write("StartTimestamp,EventType,Str(g),Duration(s),Interval(s),IsLegDown,RejectionReason");
				            // For each movement, print the movement details
				            for (Movement m: mov) {
				            	writer.write("\r\n" + m.toString());
				            }
				        } catch (Exception e) {
				            e.printStackTrace();
				        } finally {
				            try {
				                // Close the writer regardless of what happens
				                writer.close();
				            } catch (Exception e) {
				            }
				        }
				       }
			   }
		 });
		 file.getItems().add(saveAs);
		 // Ctrl + S to save the analysis movement list to a file
		 saveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		 
		 
		 // Import Analysis
		 MenuItem importAnalysis = new MenuItem("Import Analysis File");
		 importAnalysis.setOnAction(new EventHandler<ActionEvent>() {
			   @SuppressWarnings("unchecked")
			public void handle(ActionEvent t) {
				// Open a file choose dialog to decide which file to open
				   FileChooser fileChooser = new FileChooser();
				   fileChooser.setTitle("Import Analysis File");
				   fileChooser.getExtensionFilters().addAll(
						   // Only look for text files and csv files
						   new FileChooser.ExtensionFilter("TXT", "*.txt*"),
						   new FileChooser.ExtensionFilter("CSV", "*.csv"));
				   File analysisFile =  fileChooser.showOpenDialog(stage);
				   if (analysisFile != null && analysisFile.exists() & !data.isEmpty()) { 
	                	// Extract the data from the csv
						extractAnalysis(analysisFile);
						// Reset chart with extra data nodes
						// Remove the old chart and slider from the GUI
					    content.getChildren().remove(chart);
					    borderpane.getChildren().remove(slider);
						// Define the axes
					    xAxis = new DateAxis();
					    yAxis = new NumberAxis(0, 7, 1);
					    
					    // Name the axes
					    xAxis.setLabel("Time");
					    yAxis.setLabel("Force (g)");
					    
					    // Format the axes
					    xAxis.setAutoRanging(true);
					    yAxis.setAutoRanging(true);
					    
						//Create the line chart
					    chart = new LineChartWithMarkers(xAxis, yAxis);
					    // Set chart title
					    chart.setTitle("Leg Movement Analysis");
					    // Set node ID
					    chart.setId("chart");
					    // Show datepoint symbols
					    chart.setCreateSymbols(true);
					    // Hide the chart legend
					    chart.setLegendVisible(false); 
					    // Make the horizontal grid lines visible for easier reference
					    chart.setHorizontalGridLinesVisible(true);
					    // Turn off animation
					    chart.setAnimated(false);
					    
					    // Change the data point text when the mouse is moved over the chart
						chart.setOnMouseMoved(new EventHandler<MouseEvent>() {
						      @Override public void handle(MouseEvent mouseEvent) {
						    	  // Create a 2D point for where the mouse is located in the scene
						    	  Point2D pointInScene = new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY());
						    	  // Calculate what that 2D x-value corresponds to in reference to the x-axis
						    	  double xPosInAxis = xAxis.sceneToLocal(new Point2D(pointInScene.getX(), 0)).getX();
						    	  // Calculate what that 2D x-y-value corresponds to in reference to the y-axis
						    	  double yPosInAxis = yAxis.sceneToLocal(new Point2D(0, pointInScene.getY())).getY();
						    	  // Get the chart values corresponding to the values at the axes
						    	  Date x = xAxis.getValueForDisplay(xPosInAxis);
						    	  double y = yAxis.getValueForDisplay(yPosInAxis).doubleValue();
						    	    
						    	  //Format the number to a prettier string
						    	  NumberFormat numFormat = new DecimalFormat("#0.00");     
							      // Update the string label on the top of the chart
						    	  dataPoint.setText("Force: " + numFormat.format(y)
							          	+ "; Time: " + dateFormat.format(x));
						      	}
						});

					    // Add a horizontal marker as a threshold
					    XYChart.Data<Date, Number> horizontalMarker = new XYChart.Data<Date, Number>(new Date(), thresholdValue);
				        chart.addHorizontalValueMarker(horizontalMarker);
				        
				        // Add a series based off the data
				        chart.getData().add(new LineChart.Series<Date, Number>(plot()));

					    // Change the cursor to a crosshair when on the chart
					    chart.setCursor(Cursor.CROSSHAIR);	
					    // Bring the chart to the front of the scene
					    chart.toFront();
					    
					    // Setup time slider
					    slider = new Slider();
					    slider.setMin(0);
					    slider.setValue(0);
					    slider.setShowTickMarks(true);
					    slider.setMajorTickUnit(10);
					    slider.setMinorTickCount(5);
					    slider.setBlockIncrement(5);
					    
	            		if (chart != null) {
		            		LineChart.Series<Date, Number> s = (LineChart.Series<Date, Number>) chart.getData().get(0);
		            		ObservableList<LineChart.Data <Date,Number>> chartData = s.getData();
		            		// Set the max slider value of the slider
		            		int max = chartData.size() == 0? 0: chartData.size() - 1;
		            		slider.setMax(max);
	            		}
	            		
					    // Change the chart view when the slider is moved
					    slider.valueProperty().addListener((
				            ObservableValue<? extends Number> ov, 
				            Number oldVal, Number newVal) -> {
				            	if (data.size() > 0) {
				            		// Get an updated reference of the chart on the graph
				            		LineChart<Date,Number> c = (LineChart<Date,Number>) content.lookup("#chart");
				            		if (c != null) {
					            		LineChart.Series<Date, Number> s = (LineChart.Series<Date, Number>) c.getData().get(0);
					            		ObservableList<LineChart.Data <Date,Number>> chartData = s.getData();
					            		// Set the max slider value of the slider
					            		int max = chartData.size() == 0? 0: chartData.size() - 1;
					            		slider.setMax(max);
					            		slider.setMin(0);
					            		
					            		slider.setFocusTraversable(true);
					            		
							            // the upper bound is the value of the slider
							            int up = (int) Math.round((double)newVal);
							            // the lower bound is the slider value minus the screen capacity
							            int low = (int) Math.round((double)newVal) - screenCapacity;
							            // If the lower bound is less than 0, set it to 0
							            low = (low < 0)? 0 : low;
							            // If the lower bound is greater than max - screen capacity,
							            // set it equal to the max slider value - screen capacity
							            low = (low > max - screenCapacity)? 
							            		max - screenCapacity : low;
							            // If the upper bound, is less than the screen capacity,
							            // set it to the screen capacity
							            up = (up < screenCapacity)? screenCapacity : up;
							
							            // Get the date values for the bounds
							            Date upper = chartData.get(up).getXValue();
							            Date lower = chartData.get(low).getXValue();
							
							            // Set the upper and lower bounds of the chart
							            ((DateAxis)c.getXAxis()).setUpperBound(upper);
							            ((DateAxis)c.getXAxis()).setLowerBound(lower);
							            
							            
				            		} else {
				            			System.out.println("Chart is null.");
				            			
				            		}
				            	}
				        });
					    
					    // Add the new chart and slider to the GUI
						content.getChildren().add(chart);
						VBox.setVgrow(chart, Priority.ALWAYS);
						borderpane.setBottom(slider);
				   	}
			   }
		 });
		 file.getItems().add(importAnalysis);
		 // Ctrl + I to open the import
		 importAnalysis.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
		 
		 // Exit
		 MenuItem exit = new MenuItem("Exit");
		 exit.setOnAction(new EventHandler<ActionEvent>() {
		     public void handle(ActionEvent t) {
		         System.exit(0);
		     }
		 });
		 file.getItems().add(exit);
		 //Setup Ctrl+Q to activate exit
		 exit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
	 }
	
	  /**
	   * Initialize Menu Items for the paramSetup menu
	   */
	public void setupParamSetup() {  	 
	  	 // Display Parameters
	  	 MenuItem disp = new MenuItem("Set Display Paramters");
	  	 disp.setOnAction(new EventHandler<ActionEvent>() {
	  	     @SuppressWarnings("unchecked")
			public void handle(ActionEvent t) {
	  	    	// Open a dialog to allow the user to set display parameters
	  	    	// Paramters = y-max, y-min, timescale (screen capacity)
	  	    	// Create the custom dialog.
	  	    	Dialog<List<Object>> dialog = new Dialog<>();
	  	    	dialog.setTitle("Display Setup Dialog");
	  	    	dialog.setHeaderText("Adjust the display paramters for the chart.");

	  	    	// Set the button types.
	  	    	dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

	  	    	// Create the slider fields for adjusting the parameters
	  	    	GridPane grid = new GridPane();
	  	    	grid.setHgap(10);
	  	    	grid.setVgap(10);
	  	    	grid.setPadding(new Insets(20, 50, 10, 10));

	  	    	// Create sliders for the ymin and ymax values
	  	    	Slider ymin = new Slider(0, 10, 5);
	  	    	ymin.setShowTickMarks(true);
	  	    	ymin.setShowTickLabels(true);
	  	    	ymin.setMajorTickUnit(1.0f);
	  	    	ymin.setPrefWidth(300);
	  	    	Slider ymax = new Slider(0, 10, 5);
	  	    	ymax.setShowTickMarks(true);
	  	    	ymax.setShowTickLabels(true);
	  	    	ymax.setMajorTickUnit(1.0f);
	  	    	ymax.setPrefWidth(300);
	  	    	
	  	    	//Create and bind text values to the slider values
	  	    	// Ymin
	  	        final Label yminText = new Label();
	  	        yminText.setText(String.format("%.2f", ymin.getValue()));
	  	        ymin.valueProperty().addListener(new ChangeListener<Number>() {
	  	        @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
	  	          if (newValue == null) {
	  	        	yminText.setText("");
	  	            return;
	  	          }
	  	          yminText.setText(String.format("%.2f", newValue));
	  	        }
	  	        });
	  	        yminText.setPrefWidth(50);
	  	        // Ymax
	  	        final Label ymaxText = new Label();
	  	        ymaxText.setText(String.format("%.2f", ymax.getValue()));
	  	        ymax.valueProperty().addListener(new ChangeListener<Number>() {
	  	        @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
	  	          if (newValue == null) {
	  	        	ymaxText.setText("");
	  	            return;
	  	          }
	  	          ymaxText.setText(String.format("%.2f", newValue));
	  	        }
	  	        });
	  	        ymaxText.setPrefWidth(50);
	  	        
	  	        // Create a slider for the screen capacity
	  	        Slider cap = new Slider(1, data.size(), screenCapacity);
	  	    	cap.setShowTickMarks(true);
	  	    	cap.setShowTickLabels(true);
	  	    	cap.setMajorTickUnit(5.0f);
	  	    	cap.setPrefWidth(300);
	  	    	
	  	    	//Create and bind text values to the slider values
	  	    	// Chart capacity
	  	        final Label capText = new Label();
	  	        capText.setText(Math.round(cap.getValue()) + "");
	  	        cap.valueProperty().addListener(new ChangeListener<Number>() {
	  	        @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
	  	          if (newValue == null) {
	  	        	capText.setText("");
	  	            return;
	  	          }
	  	          capText.setText(Math.round(cap.getValue()) + "");
	  	        }
	  	        });
	  	        capText.setPrefWidth(50);
	  
	  	        // Add the components to the grid of the dialog box
	  	    	grid.add(new Label("Min Force Displayed on Chart:"), 0, 0);
	  	    	grid.add(ymin, 1, 0);
	  	    	grid.add(yminText, 2, 0);
	  	    	grid.add(new Label("Max Force Displayed on Chart:"), 0, 1);
	  	    	grid.add(ymax, 1, 1);
	  	    	grid.add(ymaxText, 2, 1);
	  	    	grid.add(new Label("Data Points Shown on Screen:"), 0, 2);
	  	    	grid.add(cap, 1, 2);
	  	    	grid.add(capText, 2, 2);

	  	    	dialog.getDialogPane().setContent(grid);

	  	    	// Convert the result to a username-password-pair when the login button is clicked.
	  	    	dialog.setResultConverter(dialogButton -> {
	  	    	    if (dialogButton == ButtonType.OK) {
	  	    	    	List<Object> res = new ArrayList<Object>();
	  	    	    	res.add(ymin.getValue());
	  	    	    	res.add(ymax.getValue());
	  	    	    	res.add(Math.round(cap.getValue()));
	  	    	        return res;
	  	    	    }
	  	    	    return null;
	  	    	});

	  	    	dialog.showAndWait().ifPresent(res -> {
	  	    	     if (res != null && !res.isEmpty()) {
	  	    	        // Get an updated reference of the chart on the graph
	            		LineChart<Date,Number> c = (LineChart<Date,Number>) content.lookup("#chart");
	  	    	        if (c != null) {
		            		// Set the upper and lower bounds of the chart
	  	    	        	// Y Axis
	  	    	        	NumberAxis y = ((NumberAxis)c.getYAxis());
	  	    	        	y.setAutoRanging(false);
				            y.setUpperBound((double) res.get(1));
				            y.setLowerBound((double) res.get(0));
				            // X Axis
				            //Set the screen capacity
				            screenCapacity = ((Long)res.get(2)).intValue();
				            // Get references to the series on the chart and its data
				            LineChart.Series<Date, Number> s = (LineChart.Series<Date, Number>) c.getData().get(0);
		            		ObservableList<LineChart.Data <Date,Number>> chartData = s.getData();
		            		// Set the max slider value of the slider
		            		int max = chartData.size() == 0? 0: chartData.size() - 1;
		            		double newVal = chartData.size() - 1;
				            // the upper bound is the max point
				            int up = (int) Math.round(newVal);
				            // the lower bound is the max point value minus the screen capacity
				            int low = (int) Math.round(newVal) - screenCapacity;
				            // If the lower bound is less than 0, set it to 0
				            low = (low < 0)? 0 : low;
				            // If the lower bound is greater than max - screen capacity,
				            // set it equal to the max slider value - screen capacity
				            low = (low > max - screenCapacity)? 
				            		max - screenCapacity : low;
				            // If the upper bound, is less than the screen capacity,
				            // set it to the screen capacity
				            up = (up < screenCapacity)? screenCapacity : up;
				
				            // Get the date values for the bounds
				            Date upper = chartData.get(up).getXValue();
				            Date lower = chartData.get(low).getXValue();
				
				            // Set the upper and lower bounds of the chart
				            ((DateAxis)c.getXAxis()).setUpperBound(upper);
				            ((DateAxis)c.getXAxis()).setLowerBound(lower);
				            
	  	    	        }
	  	    	     }
	  	    	 });
	  	     }
	  	 });
	  	 paramSetup.getItems().add(disp);
	  	 //Setup Ctrl+D to activate display parameters
	  	 disp.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
	   }
	   
   /**
    * Initialize Menu Items for the reports menu
    */
	public void setupReports() {   	 
	     // Sleep Report
	  	 MenuItem setSleep = new MenuItem("Nightly Report");
	  	 setSleep.setOnAction(new EventHandler<ActionEvent>() {
	  	     public void handle(ActionEvent t) {
	  	    	 
	  	    // Open a dialog to allow the user to set display parameters
		  	    	// Paramters = y-max, y-min, timescale (screen capacity)
		  	    	// Create the custom dialog.
		  	    	Dialog<List<Object>> dialog = new Dialog<>();
		  	    	dialog.initModality(Modality.NONE);
		  	    	dialog.initOwner(primary);
		  	    	dialog.setTitle("Nightly Report");
		  	    	dialog.setHeaderText("Select the sleep periods you wish to score.");

		  	    	// Set the button types.
		  	    	//ButtonType apply = new ButtonType("Apply", null);
		  	    	dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

		  	    	// Create the slider fields for adjusting the parameters
		  	    	GridPane grid = new GridPane();
		  	    	grid.setHgap(10);
		  	    	grid.setVgap(10);
		  	    	grid.setPadding(new Insets(20, 50, 10, 10));
		  	    	// Make the dialog dynamically grow as things are added
		  	    	h=300;
		  	    	grid.setPrefSize(750,h);
		  	    	
		  	    	// Set incremental variables
		  	    	night = 0;
		  	    	List<Label> nightText = new ArrayList<Label>();
		  	    	List<List<DateTimePicker>> sleepTimes = new ArrayList<List<DateTimePicker>>();
		  	    	List<Label> ephNightText = new ArrayList<Label>();
		  	    	List<Button> buttons = new ArrayList<Button>();
		  	    	
		  	    	// Add night button
		  	    	Button addNight = new Button("Add Sleep Period");
		  	    	addNight.setOnAction(new EventHandler<ActionEvent>() {
						public void handle(ActionEvent event) {
					    // Grow doalog
						h+=100;
						grid.setPrefHeight(h);
			  	    	// Add first night automatically
			  	    	nightText.add(night, new Label("Night " + (night + 1) + " (From - To): "));
			  	    	// Make two text boxes to hold from and to dates - open DateTimePicker onClick
			  	        sleepTimes.add(night,new ArrayList<DateTimePicker>()); 
			  	        sleepTimes.get(night).add(0, new DateTimePicker());
			  	        sleepTimes.get(night).add(1, new DateTimePicker());	
			  	        // Add EPH text
			  	        ephNightText.add(night, new Label("Events Per Hour: "));
			  	        // Button View Chart
			  	        buttons.add(night, new Button ("View Chart"));
				  	    buttons.get(night).setOnAction(new EventHandler<ActionEvent>() {
							public void handle(ActionEvent event) {
								
								// 2016-04-13 13:54:31.000
				  	        	int n = buttons.indexOf(event.getSource());
				  	        	
								// Create the custom dialog.
								Dialog<Void> dia = new Dialog<>();
								dia.setTitle("Night " + (n+1));
								dia.setHeaderText("Data for Night " + (n+1));
								dia.initModality(Modality.NONE);
								dia.initOwner(primary);
								
								VBox vb = new VBox();
								
				  	        	// Get the dates from the DateTimePickers
				  	        	LocalDateTime s = sleepTimes.get(n).get(0).getDateTimeValue();
				  	        	LocalDateTime e = sleepTimes.get(n).get(1).getDateTimeValue();
				  	        	// debug
				  	        	System.out.println(s);
				  	        	System.out.println(e);
				  	        	// Define the axes
							    DateAxis x = new DateAxis();
							    NumberAxis y = new NumberAxis(0, 7, 1);
							    
							    // Name the axes
							    x.setLabel("Time");
							    y.setLabel("Force (g)");
							    
							    // Format the axes
							    x.setAutoRanging(false);
							    y.setAutoRanging(true);
							    
							    System.out.println("CALENDAR");
							    // Update the bounds using the datetimepicker valies
							    Calendar cal = Calendar.getInstance();
							    // Get the "from" date
							    // For some reason Calendar months are indexed from 0
							    cal.set(s.getYear(), s.getMonthValue() - 1, s.getDayOfMonth(), s.getHour(), s.getMinute(), s.getSecond());
							    System.out.println(cal);
							    cal.set(Calendar.MILLISECOND, s.getNano()*1000);
							    Date start = cal.getTime();
							    System.out.println("Start: " + start);
							    // Get the "to" date
							    cal.set(e.getYear(), e.getMonthValue() - 1, e.getDayOfMonth(), e.getHour(), e.getMinute(), e.getSecond());
							    cal.set(Calendar.MILLISECOND, e.getNano()*1000);
								Date end = cal.getTime();
								System.out.println("End: " + end);
								// Set the bounds
								x.setUpperBound(end);
								x.setLowerBound(start);
								
								System.out.println("Lower Bound: " + x.getLowerBound());
								System.out.println("Upper Bound: " + x.getUpperBound());
								
								// Update the eph label
								ephNightText.get(n).setText("Events Per Hour: " + calculateEPH(start, end));
							    
								// Create a new chart
				  	        	LineChartWithMarkers charts = new LineChartWithMarkers(x, y);
				  	        	
							    // Set chart title
							    charts.setTitle("Night " + (n + 1));
							    // Turn off symbols
							    charts.setCreateSymbols(false);
							    // Hide the chart legend
							    charts.setLegendVisible(false); 
							    // Make the horizontal grid lines visible for easier reference
							    charts.setHorizontalGridLinesVisible(true);
							    // Turn off animation
							    charts.setAnimated(false);
	
						        // Add a series based off the data
							    charts.setData(chart.getData());
						   	    // Change the cursor to a crosshair when on the chart
							    charts.setCursor(Cursor.CROSSHAIR);	
							    // Bring the chart to the front of the scene
							    charts.toFront();
							    // Add to the dialog box
							    vb.getChildren().add(charts);
							    
							    System.out.println(charts.getData().get(0).getData());
							    
							    dia.getDialogPane().setContent(vb);
							    dia.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
							    dia.showAndWait();
							    
							    
				  	        }
				  	   });
			  	        
			  	    	// Add the components to the grid of the dialog box
			  	        grid.add(nightText.get(night), 0, night + 1);
			  	        grid.add(sleepTimes.get(night).get(0), 1, night + 1);
			  	    	grid.add(sleepTimes.get(night).get(1), 2, night + 1);
			  	    	grid.add(ephNightText.get(night), 3, night + 1);
			  	    	grid.add(buttons.get(night), 4, night + 1);
			  	    	
			  	    	night++;
						}
					});
		  	    	grid.add(addNight, 0, 0);
		  	    	
		  	    	dialog.getDialogPane().setContent(grid);
		  	    	dialog.showAndWait();
		  	    	
	  	     }
	  	 });
	  	 reports.getItems().add(setSleep);
	  	 //Setup Ctrl+P to activate setPLM
	  	 setSleep.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
	}
	
	/*
	 * Calculate the events per hour for a given time period
	 * @param start The starting time for the time period
	 * @param end The ending time for the time period
	 * @return the average events per hour
	 */
	private double calculateEPH(Date start, Date end) {
		System.out.println("EPH CALCULATION");
		System.out.println(start);
		System.out.println(end);
		// variable to hold the total amount of plms in the time period
		double count = 0;
		// the time difference between the two times in hours - note: getTime() returns ms
		double hours = Math.abs(end.getTime() - start.getTime())/3600000;
		System.out.println("Hours: " + hours);
		
		for (Movement m : mov) {
			if (m!=null && m.getTime().getTime() > start.getTime() && m.getTime().getTime() < end.getTime()
					&& m.getType().equals("P")) {
				count++;
			}
		}
		
		return (hours != 0)? count/hours : 0.0;
	}
	  
	 /**
	  * Extract the accelerometer data from the device's csv file
	 * @param dataFile 
	  */
	  public void extractData(File dataFile) {
	      // This will reference one line at a time
	      String line = null;

	      try {
	            // FileReader reads text files in the default encoding.
	            FileReader fileReader = 
	                new FileReader(dataFile);

	            // Always wrap FileReader in BufferedReader.
	            BufferedReader bufferedReader = 
	                new BufferedReader(fileReader);

	            try {
	            	data.clear();
		            while((line = bufferedReader.readLine()) != null) {
		                // YYYY-MM-DD hh:mm:ss.sss,accx,accy,accz,gyrx,gyry,gyrz
		                // 01234567890123456789012345678901234567890123456789012
		                
		                String[] value = line.split(",");
		                String date = value [0];

		                // Check if the first value is a number = not title 
		            	boolean ret = true;
		                try {
		                    Double.parseDouble(Character.toString(date.charAt(0)));
	
		                }catch (NumberFormatException e) {
		                    ret = false;
		                }
		                
		            	if (ret) {
			                Calendar event = Calendar.getInstance();
			                // Parse the values for the date
			                event.set(Integer.parseInt(Character.toString(date.charAt(0)) + Character.toString(date.charAt(1))
			                	+ Character.toString(date.charAt(2)) + Character.toString(date.charAt(3))),
			                		Integer.parseInt(Character.toString(date.charAt(5)) + Character.toString(date.charAt(6))) - 1,
			                		Integer.parseInt(Character.toString(date.charAt(8)) + Character.toString(date.charAt(9))),
			                		Integer.parseInt(Character.toString(date.charAt(11)) + Character.toString(date.charAt(12))),
			                		Integer.parseInt(Character.toString(date.charAt(14)) + Character.toString(date.charAt(15))), 
			                		Integer.parseInt(Character.toString(date.charAt(17)) + Character.toString(date.charAt(18))));
			                event.set(Calendar.MILLISECOND, Integer.parseInt(Character.toString(date.charAt(20))
			                		+ Character.toString(date.charAt(21)) + Character.toString(date.charAt(22))));
			                // Parse the accelerometer and gyroscope values
			                double accGyro[] = new double[6];
			                for(int i = 1; i < accGyro.length; i++) {
			                	// Parse the data
			                	accGyro[i - 1] = Double.parseDouble(value[i]);
			                }
			                
			                // Get the magnitudes for the accelerometer and gyroscope
			                double accel = Math.sqrt(accGyro[0]*accGyro[0] + accGyro[1]*accGyro[1]
			                		+ accGyro[2]*accGyro[2]);
			                //double gyro = Math.sqrt(accGyro[3]*accGyro[3] + accGyro[4]*accGyro[4]
			                //		+ accGyro[5]*accGyro[5]);
			                data.add(new XYChart.Data<Date, Number>(event.getTime(), accel));
			            }   
		            }
	            } catch (Exception e) {
	            	Alert alert = new Alert(AlertType.ERROR);
	            	alert.setTitle("Error: Open File Error");
	            	alert.setHeaderText("Open File Error");
	            	alert.setContentText("The data file you attempted to open was of the wrong type or was misformatted."
	            			+ "\nPlease check your file and try again.");

	            	alert.showAndWait();
	            }
	            // Always close files.
	            bufferedReader.close();         
	      } catch(FileNotFoundException ex) {
	            System.out.println(
	                "Unable to open file '" + 
	                dataFile.getName() + "'");                
	      } catch(IOException ex) {
	            System.out.println(
	                "Error reading file '" 
	                + dataFile.getName() + "'");
	      }
	  }
	  
	  /**
	  * Extract the accelerometer data from the device's csv file
	 * @param dataFile 
	  */
	  public void extractAnalysis(File analysisFile) {
	      // This will reference one line at a time
	      String line = null;

	      try {
	            // FileReader reads text files in the default encoding.
	            FileReader fileReader = 
	                new FileReader(analysisFile);

	            // Always wrap FileReader in BufferedReader.
	            BufferedReader bufferedReader = 
	                new BufferedReader(fileReader);
	            try {
	            	mov.clear();
		            while((line = bufferedReader.readLine()) != null) {
		                // YYYY-MM-DD hh:mm:ss.sss,E,strG,duraS,intvS,d,
		                // 01234567890123456789012345678901234567890123456789012
		            	 String[] value = line.split(",");
			             String date = value [0];

			             // Check if the first value is a number = not title 
			             boolean ret = true;
			             try {
			                 Double.parseDouble(Character.toString(date.charAt(0)));
		
			             }catch (Exception e) {
			                 ret = false;
			             }
		                
		            	if (ret) {
			                Calendar event = Calendar.getInstance();
			                // Parse the values for the date
			                event.set(Integer.parseInt(Character.toString(date.charAt(0)) + Character.toString(date.charAt(1))
			                	+ Character.toString(date.charAt(2)) + Character.toString(date.charAt(3))),
			                		Integer.parseInt(Character.toString(date.charAt(5)) + Character.toString(date.charAt(6))) - 1,
			                		Integer.parseInt(Character.toString(date.charAt(8)) + Character.toString(date.charAt(9))),
			                		Integer.parseInt(Character.toString(date.charAt(11)) + Character.toString(date.charAt(12))),
			                		Integer.parseInt(Character.toString(date.charAt(14)) + Character.toString(date.charAt(15))), 
			                		Integer.parseInt(Character.toString(date.charAt(17)) + Character.toString(date.charAt(18))));
			                event.set(Calendar.MILLISECOND, Integer.parseInt(Character.toString(date.charAt(20))
			                		+ Character.toString(date.charAt(21)) + Character.toString(date.charAt(22))));
			                // Parse the event type
			                String eType = value[1].trim();
			                // Parse the strength
			                double str = Double.parseDouble(value[2].trim());
			                // Parse the duration
			                double dur = Double.parseDouble(value[3].trim());
			                // Parse the interval - if text is nan, interval = positive infinity
			                double inv = Double.POSITIVE_INFINITY;
			                try {
			                	inv = Double.parseDouble((value[4]).trim());
			                } catch (NumberFormatException nfe) {}
			                
			                // Parse if the leg is down or up - default true
			                boolean leg = true;
			                if (value[5].trim().toLowerCase().equals("f")) {
			                	leg = false;
			                }
			                // Parse the reason for rejection
			                String reason = "";
			                try{
			                     reason = value[6];
			                } catch (Exception e) {}
			              
			                // Create a movement with the values and add to the list
			                Movement m = new Movement(event.getTime(), eType, str, dur, inv, leg, reason);
			                System.out.println(m);
			                mov.add(m);
			            }   
		            }
	            } catch (Exception e) {
	            	Alert alert = new Alert(AlertType.ERROR);
	            	alert.setTitle("Error: Open File Error");
	            	alert.setHeaderText("Open File Error");
	            	alert.setContentText("The data file you attempted to open was of the wrong type or was misformatted."
	            			+ "\nPlease check your file and try again.\nError: " + e.getMessage());

	            	alert.showAndWait();
	            }
	            // Always close files.
	            bufferedReader.close();         
	      } catch(FileNotFoundException ex) {
	            System.out.println(
	                "Unable to open file '" + 
	                analysisFile.getName() + "'");                
	      } catch(IOException ex) {
	            System.out.println(
	                "Error reading file '" 
	                + analysisFile.getName() + "'");
	      }
	  }

	  /** 
	   * @return plotted y values for monotonically increasing integer x values, starting from x=1
	   * */
	  public ObservableList<XYChart.Data<Date, Number>> plot() {
	    final ObservableList<XYChart.Data<Date, Number>> dataset = FXCollections.observableArrayList();
	    // For each movement in the list add a node
	    for (LineChart.Data<Date, Number> val : data) {
	    	final LineChart.Data<Date, Number> point = val;
	    	for (Movement m : mov) {
	    		if (val.getXValue().equals(m.getTime())){
	    	    	String lab = "Start Time: " + dateFormat.format(m.getTime()) + "\nEvent Type: " + m.getType() + "\nStrength: "  + m.getStr() + "\nDuration: " + m.getDur()
	    	    		+ "\nInterval: " + m.getInterval();
	    	    	if (!m.getReason().isEmpty()) {
	    	    		lab += "\nRejection Reason: " + m.getReason();
	    	    	}
	    	    	point.setNode(new HoveredThresholdNode(lab));
	    		}
	    	}
	    	dataset.add(point);
	    }

	    return dataset;
	  }

	  /** a node which displays a value on hover, but is otherwise empty */
	  class HoveredThresholdNode extends StackPane {
	    HoveredThresholdNode(String l) {
	      setPrefSize(15, 15);

	      final Label label = new Label(l);
	      label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
	      label.setStyle("-fx-font-weight: bold;");
	      label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);

	      setOnMouseEntered(new EventHandler<MouseEvent>() {
	        @Override public void handle(MouseEvent mouseEvent) {
	          // When the mouse is hovering, display the label and remove the cursor
	          getChildren().setAll(label);
	          setCursor(Cursor.NONE);
	          // Bring the label in front of the node
	          toFront();
	        }
	      });
	      setOnMouseExited(new EventHandler<MouseEvent>() {
	        @Override public void handle(MouseEvent mouseEvent) {
	          // When the mouse leaves hover, remove the label and reset the cursor to a crosshair
	          getChildren().clear();
	          setCursor(Cursor.CROSSHAIR);
	        }
	      });
	      
	      setOnMouseClicked(new EventHandler<MouseEvent>() {
		        @Override public void handle(MouseEvent mouseEvent) {
		          // When the mouse is clicked, open a dialog box with the label
		        	Alert alert = new Alert(AlertType.INFORMATION);
	            	alert.setTitle("Movement Information");
	            	alert.setHeaderText("Movement Details");
	            	alert.setContentText(label.getText());

	            	alert.showAndWait();
		        }
	      });
	    }
	  }

	//JavaFX applications use the main method to launch the GUI.
	//It should only ever contain the call to the launch method
    public static void main(String[] args) {
        launch(args);
    }
}