import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

//import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Menu;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/*
 * GUI for PLM Analyzer system
 * @author Jennifer Hunter
 */
@SuppressWarnings("restriction")
public class Main extends Application {
	// Data point text above the graphs
	private Text dataPoint; 
	// Average PLM events per hour 
	private DoubleProperty eph = new SimpleDoubleProperty();
	// Menus
	private Menu file, monitor, display, paramSetup,
		reports, trim, about;
	// Button for leg movement analysis
	private Button analyze;
	// Threshold value - default to 1.0
	private double thresholdValue = 1.0;
	// Data retrieved from csv
	private ObservableList <LineChart.Data<Date, Number>> data;
	// How many data points can be seen at a screen at once
	private int screenCapacity = 10;
	// Format for dates on x-axis
	private SimpleDateFormat dateFormat;
	// Reference to the line chart and its series
	private LineChartWithMarkers chart;
	
	//private XYChart.Series<Date, Number> threshold;
	    
	/**
	* Method to start the GUI
	*
	* @param stage Stage for displaying
	*/
	@SuppressWarnings({ "unchecked", "rawtypes"})
	public void start(Stage stage) {
		// Store the raw data points in an observable array list
		data = FXCollections.observableArrayList();
		// Date Format Year-Month-Day Hour(24):Minute:Second.Milliseconds
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		// Create data point text
		dataPoint = new Text ("Time: Datapoint: Amplitude:");
		dataPoint.setTextAlignment(TextAlignment.CENTER);
		dataPoint.setTextOrigin(VPos.CENTER);
	
		// Analyze Button Creation
	    analyze = new Button("Find and Analyze Leg Movements");
	    analyze.setOnAction(e -> {
	        }
	    );    
	    
	    // Define the axes
	    DateAxis xAxis = new DateAxis();
	    NumberAxis yAxis = new NumberAxis(0, 7, 1);
	    
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
	    
	    // Change the data point text when the mouse is moved over the chart
		chart.setOnMouseMoved(new EventHandler<MouseEvent>() {
		      @Override public void handle(MouseEvent mouseEvent) {
		    	  // The values were off by random amounts.
		    	  // So, I had to calculate the shift in the chart for the scene
		    	  Node chartPlotBackground = chart.lookup(".chart-plot-background");
		    	  final double shiftX = xSceneShift(chartPlotBackground);
		    	  final double shiftY = ySceneShift(chartPlotBackground);
		    	  // Gets the x and y values of the chart under the mouse
		    	  Date x = xAxis.getValueForDisplay(mouseEvent.getX() - shiftX + 55);
		    	  double y = (double) yAxis.getValueForDisplay(mouseEvent.getY() - shiftY + 55);		    	  
		    	  
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
	   
	    
	    // Set initial value of average to zero
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
	    Slider slider = new Slider();
	    slider.setMin(0);
	    int max = data.size() == 0? 0: data.size() - 1;
	    slider.setMax(max);
	    slider.setValue(0);
	    //slider.setShowTickLabels(false);
	    slider.setShowTickMarks(true);
	    slider.setMajorTickUnit(25);
	    slider.setMinorTickCount(5);
	    slider.setBlockIncrement(5);
	    // Change the chart view when the slider is moved
	    slider.valueProperty().addListener((
            ObservableValue<? extends Number> ov, 
            Number oldVal, Number newVal) -> {
            	if (data.size() > 0) {
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
		            Date upper = data.get(up).getXValue();
		            Date lower = data.get(low).getXValue();
		
		            // Set the upper and lower bounds of the chart
		            ((DateAxis)chart.getXAxis()).setUpperBound(upper);
		            ((DateAxis)chart.getXAxis()).setLowerBound(lower);
            	}
        });
	    
	    // Create a borderpane to hold all GUI objects
	    BorderPane borderpane = new BorderPane();
	    
	    // Setup the top menu toolbar
	    MenuBar menuBar = new MenuBar();
	    setupMenu(stage);
	    menuBar.getMenus().addAll(file, monitor, display, paramSetup,
	    		reports, trim, about);
	    
	    // Create a vertical box to show the charts
	    VBox content = new VBox();
	    // Add spacing between the children
	    content.setSpacing(15.0);
	    // Add the dataPoint text, the charts, the slider, the table, and the analyze button
	    content.getChildren().addAll(dataPoint, chart, slider, ephText, analyze);
	    		//chartPre, table - elements to add
	    // Give the bottom button some extra space
	    VBox.setMargin(analyze, new Insets(0.0, 0.0, 20.0, 0.0));
	    
	    // Add the menu to the top of the GUI
	    borderpane.setTop(menuBar);
	    // Anchor the content to the left
	    borderpane.setCenter(content);
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
	
	/*
	 * Calculate the X shift cause by the scene for a given Javafx Node
	 */
	private double xSceneShift(Node node) {
	    return node.getParent() == null ? 0 : node.getBoundsInParent().getMinX() + xSceneShift(node.getParent());
	}

	/*
	 * Calculate the Y shift cause by the scene for a given Javafx Node
	 */
	private double ySceneShift(Node node) {
	    return node.getParent() == null ? 0 : node.getBoundsInParent().getMinY() + ySceneShift(node.getParent());
	}
	
	
	/**
	* Initialize Menus for the Menu Toolbar
	*/
	public void setupMenu(Stage stage) {
	
	    // Toolbar Menu creation
	    file = new Menu("File");
	    setupFile(stage);
	
	    monitor = new Menu("Monitor");
	    setupMonitor();
	
	    display = new Menu("Display");
	    display.setOnAction(e -> {
	        }
	    );
	
	    paramSetup = new Menu("Parameter Setup");
	    setupParamSetup();
	
	    reports = new Menu("Reports");
	    setupReports();
	    
	    trim = new Menu("Trim");
	    trim.setOnAction(e -> {
	    	}
	    );
	
	    about = new Menu("About");
	    about.setOnAction(e -> {
	    	System.out.println("About was clicked.");
	    	// Generate a dialog box with the about info
	    	Alert alert = new Alert(AlertType.INFORMATION);
	    	alert.setTitle("About");
	    	alert.setHeaderText("About the PLM Analyzer");
	    	alert.setContentText("Michael Alexander Haver, Jennifer Hunter, Robert Lee, Kevin Powell, and Joesph Thompson"
	    			+ "designed this PLM Analyzer for the Emory Sleep Lab. /n2016");
	    	alert.initOwner(stage);
	    	alert.showAndWait();
	    	}
	    	
	    );
	    
	    // start with sort by date in focus
	    analyze.requestFocus();
	}
	
	/**
	 * Initialize Menu Items for the File menu
	 */
	public void setupFile(Stage stage) {
		// Open
		 MenuItem open = new MenuItem("Open");
		 open.setOnAction(new EventHandler<ActionEvent>() {
			   public void handle(ActionEvent t) {
				   // Open a file choose dialog to decide which file to open
				   FileChooser fileChooser = new FileChooser();
				   fileChooser.setTitle("Open File");
				   fileChooser.getExtensionFilters().addAll(
						   // Only look for text files and csv files
			                new FileChooser.ExtensionFilter("TXT", "*.txt*"),
			                new FileChooser.ExtensionFilter("CSV", "*.csv"));
				   File openFile =  fileChooser.showOpenDialog(stage);
				   if (openFile != null && openFile.exists()) {
					  // Platform.runLater(new Runnable() {
						//    @Override public void run() {
						  //          Platform.runLater(new Runnable() {
						  //              @Override public void run() {             
						                	// Extract the data from the csv
											extractData(openFile);
						            		// Print out data for DEBUGGING
						 				    System.out.println(chart.getData().get(0).getData());
						 				    // Reset chart view
						 				    chart.applyCss();
						 				    chart.layout();
						 				    chart.getXAxis().layout();
						 				    chart.getYAxis().layout();
						 				    chart.getXAxis().requestAxisLayout();
						 				    chart.getYAxis().requestAxisLayout();
						 				    /*
						 				    for (int i = 0; i < chart.getData().get(0).getData().size(); i++) {
						 				    	System.out.println(chart.getXAxis().getDisplayPosition(data.get(i).getXValue()));	
						 				    	System.out.println(chart.getXAxis().getDisplayPosition(data.get(i).getXValue()));
						 				    }
						 				    */
						 				    
				   }
			   }}
			   );
				   /*
						            });
						    }});
				   }
	  	     }
		 */
		 
		 
		 // Add the open menu item under the file menu
		 file.getItems().add(open);
		 //Setup Ctrl+O to activate open
		 open.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
		 
		// Save As
		 MenuItem saveAs = new MenuItem("Save As");
		 saveAs.setOnAction(new EventHandler<ActionEvent>() {
			   public void handle(ActionEvent t) {
				   // Open a file chooser to decide where to save the file
				   FileChooser fileChooser1 = new FileChooser();
				   fileChooser1.setTitle("Save As");
				   File saveFile = fileChooser1.showSaveDialog(stage);
	  	     }
		 });
		 file.getItems().add(saveAs);
		 saveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		// Save Raw Data
		 MenuItem saveRaw = new MenuItem("Save Raw Data");
		 saveRaw.setOnAction(new EventHandler<ActionEvent>() {
			   public void handle(ActionEvent t) {
	  	     }
		 });
		 file.getItems().add(saveRaw);
		 
		// File Information
		 MenuItem fileInfo = new MenuItem("File Information");
		 fileInfo.setOnAction(new EventHandler<ActionEvent>() {
			   public void handle(ActionEvent t) {
	  	     }
		 });
		 file.getItems().add(fileInfo);
		 
		 // Exit
		 MenuItem exit = new MenuItem("Exit");
		 exit.setOnAction(new EventHandler<ActionEvent>() {
		     public void handle(ActionEvent t) {
		         System.exit(0);
		     }
		 });
		 file.getItems().add(exit);
		 //Setup Ctrl+Q to activate exit
	 }
	 
	 /**
	  * Initialize Menu Items for the Monitor menu
	  */
	public void setupMonitor() {
	 	 // Download
	 	 MenuItem download = new MenuItem("Download");
	 	 download.setOnAction(new EventHandler<ActionEvent>() {
	 		   public void handle(ActionEvent t) {
	   	     }
	 	 });
	 	 monitor.getItems().add(download);
	 	 //Setup Ctrl+D to activate download
	 	 
	 	 // Initialize
	 	 MenuItem initialize = new MenuItem("Initialize");
	 	 initialize.setOnAction(new EventHandler<ActionEvent>() {
	 	     public void handle(ActionEvent t) {
	 	     }
	 	 });
	 	 monitor.getItems().add(initialize);
	 	 //Setup Ctrl+I to activate initialize
	 	 
	 	 // Cable Test
	 	 MenuItem cableTest = new MenuItem("Cable Test");
	 	 cableTest.setOnAction(new EventHandler<ActionEvent>() {
	 		   public void handle(ActionEvent t) {
	   	     }
	 	 });
	 	 monitor.getItems().add(cableTest);
	 	 
	 	 // Comm Settings
	 	 MenuItem commSettings = new MenuItem("Comm Settings");
	 	 commSettings.setOnAction(new EventHandler<ActionEvent>() {
	 		   public void handle(ActionEvent t) {
	   	     }
	 	 });
	 	 monitor.getItems().add(commSettings);
	  }
	  
	  /**
	   * Initialize Menu Items for the paramSetup menu
	   */
	public void setupParamSetup() {
	  	 // Preprocess
	  	 MenuItem preprocess = new MenuItem("Preprocess");
	  	 preprocess.setOnAction(new EventHandler<ActionEvent>() {
	  		   public void handle(ActionEvent t) {
	    	     }
	  	 });
	  	 paramSetup.getItems().add(preprocess);
	  	 //Setup Ctrl+P to activate preprocess
	  	 
	  	 // Set PLM Paramters
	  	 MenuItem setPLM = new MenuItem("Set PLM Paramters");
	  	 setPLM.setOnAction(new EventHandler<ActionEvent>() {
	  	     public void handle(ActionEvent t) {
	  	     }
	  	 });
	  	 paramSetup.getItems().add(setPLM);
	  	 //Setup Ctrl+K to activate setPLM
	  	 
	  	 // PLM Analysis
	  	 MenuItem plmAnalysis = new MenuItem("PLM Analysis");
	  	 plmAnalysis.setOnAction(new EventHandler<ActionEvent>() {
	  	     public void handle(ActionEvent t) {
	  	     }
	  	 });
	  	 paramSetup.getItems().add(plmAnalysis);
	  	 //Setup Ctrl+M to activate plmAnalysis
	  	 
	  	 // Paramter Sets
	  	 MenuItem paramSets = new MenuItem("Parameter Sets");
	  	 paramSets.setOnAction(new EventHandler<ActionEvent>() {
	  		   public void handle(ActionEvent t) {
	    	     }
	  	 });
	  	 paramSetup.getItems().add(paramSets);
	   }
	   
   /**
    * Initialize Menu Items for the reports menu
    */
	public void setupReports() {
	   	 // Events
	   	 MenuItem events = new MenuItem("Events");
	   	 events.setOnAction(new EventHandler<ActionEvent>() {
	   		   public void handle(ActionEvent t) {
	     	     }
	   	 });
	   	 reports.getItems().add(events);
	   	 
	   	 // Summary
	   	 MenuItem summaries = new MenuItem("Summary");
	   	 summaries.setOnAction(new EventHandler<ActionEvent>() {
	   	     public void handle(ActionEvent t) {
	   	     }
	   	 });
	   	 reports.getItems().add(summaries);
	   	 
	   	 // PLM Episodes
	   	 MenuItem plmEpisodes = new MenuItem("PLM Episodes");
	   	 plmEpisodes.setOnAction(new EventHandler<ActionEvent>() {
	   		   public void handle(ActionEvent t) {
	     	     }
	   	 });
	   	 reports.getItems().add(plmEpisodes);
	   	 
	   	 // Verify Events
	   	 MenuItem verify = new MenuItem("Verify Events");
	   	 verify.setOnAction(new EventHandler<ActionEvent>() {
	   		   public void handle(ActionEvent t) {
	     	     }
	   	 });
	   	 reports.getItems().add(verify);
	    }
	  
	  
	 /**
	  * Extract the accelerometer data from the device's csv file
	 * @param dataFile 
	  */
	  public void extractData(File dataFile) {
		  // Extract data with file i/o shiz
		  // For now just put random numbers

	      // This will reference one line at a time
	      String line = null;

	      try {
	            // FileReader reads text files in the default encoding.
	            FileReader fileReader = 
	                new FileReader(dataFile);

	            // Always wrap FileReader in BufferedReader.
	            BufferedReader bufferedReader = 
	                new BufferedReader(fileReader);

	            while((line = bufferedReader.readLine()) != null) {
	                // YYYY-MM-DD hh:mm:ss.sss,accx,accy,accz,gyrx,gyry,gyrz
	                // 01234567890123456789012345678901234567890123456789012
	                
	                String[] value = line.split("");
	                Calendar event = Calendar.getInstance();
	                // Parse the values for the date
	                event.set(Integer.parseInt(value[0] + value[1] + value[2] + value[3]),
	                		Integer.parseInt(value[5] + value[6]), Integer.parseInt(value[8] + value[9]),
	                		Integer.parseInt(value[11] + value[12]), Integer.parseInt(value[14] + value[15]), 
	                		Integer.parseInt(value[17] + value[18]));
	                event.set(Calendar.MILLISECOND, Integer.parseInt(value[20] + value[21] + value[22]));
	                // Parse the accelerometer and gyroscope values
	                double ax = Double.parseDouble(value[24] + value[25] + value[26] + value[27]);
	                double ay = Double.parseDouble(value[29] + value[30] + value[31] + value[32]);
	                double az = Double.parseDouble(value[34] + value[35] + value[36] + value[37]);
	                double gx = Double.parseDouble(value[39] + value[40] + value[41] + value[42]);
	                double gy = Double.parseDouble(value[44] + value[45] + value[46] + value[47]);
	                double gz = Double.parseDouble(value[49] + value[50] + value[51] + value[52]);
	                // Get the magnitudes for the accelerometer and gyroscope
	                double accel = Math.sqrt(ax*ax + ay*ay + az*az);
	                double gyro = Math.sqrt(gx*gx + gy*gy + gz*gz);
	                data.add(new XYChart.Data<Date, Number>(event.getTime(), accel));
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
	  

	  /** @return plotted y values for monotonically increasing integer x values, starting from x=1 */
	  public ObservableList<XYChart.Data<Integer, Integer>> plot(int... y) {
	    final ObservableList<XYChart.Data<Integer, Integer>> dataset = FXCollections.observableArrayList();
	    int i = 0;
	    while (i < y.length) {
	      final XYChart.Data<Integer, Integer> data = new XYChart.Data<>(i + 1, y[i]);
	      data.setNode(
	          new HoveredThresholdNode(
	              (i == 0) ? 0 : y[i-1],
	              y[i]
	          )
	      );

	      dataset.add(data);
	      i++;
	    }

	    return dataset;
	  }

	  /** a node which displays a value on hover, but is otherwise empty */
	  class HoveredThresholdNode extends StackPane {
	    HoveredThresholdNode(int priorValue, int value) {
	      setPrefSize(15, 15);

	      final Label label = createDataThresholdLabel(priorValue, value);

	      setOnMouseEntered(new EventHandler<MouseEvent>() {
	        @Override public void handle(MouseEvent mouseEvent) {
	          getChildren().setAll(label);
	          setCursor(Cursor.NONE);
	          toFront();
	        }
	      });
	      setOnMouseExited(new EventHandler<MouseEvent>() {
	        @Override public void handle(MouseEvent mouseEvent) {
	          getChildren().clear();
	          setCursor(Cursor.CROSSHAIR);
	        }
	      });
	    }

	    private Label createDataThresholdLabel(int priorValue, int value) {
	      final Label label = new Label(value + "");
	      label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
	      label.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

	      if (priorValue == 0) {
	        label.setTextFill(Color.DARKGRAY);
	      } else if (value > priorValue) {
	        label.setTextFill(Color.FORESTGREEN);
	      } else {
	        label.setTextFill(Color.FIREBRICK);
	      }

	      label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
	      return label;
	    }
	  }

	//JavaFX applications use the main method to launch the GUI.
	//It should only ever contain the call to the launch method
    public static void main(String[] args) {
        launch(args);
    }
}