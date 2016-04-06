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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/*
 * GUI for PLM Analyzer system
 * @version 2016_04_06
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
		reports, about;

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
	    menuBar.getMenus().addAll(file, paramSetup, reports, about);
	    
	    // Add spacing between the children
	    content.setSpacing(15.0);
	    // Add the dataPoint text, the charts, the slider, the table, and the analyze button
	    content.getChildren().addAll(dataPoint, ephText, chart);
	    
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
				   fileChooser.setTitle("Open File");
				   fileChooser.getExtensionFilters().addAll(
						   // Only look for text files and csv files
			                new FileChooser.ExtensionFilter("TXT", "*.txt*"),
			                new FileChooser.ExtensionFilter("CSV", "*.csv"));
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
						borderpane.setBottom(slider);
				   	}
			   }}
		);
		 
		 
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
		 // Ctrl + S to save the analysis movement list to a file
		 saveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		 
		 
		 // Import Analysis
		 MenuItem importAnalysis = new MenuItem("Import Analysis File");
		 importAnalysis.setOnAction(new EventHandler<ActionEvent>() {
			   public void handle(ActionEvent t) {
				// Open a file choose dialog to decide which file to open
				   FileChooser fileChooser = new FileChooser();
				   fileChooser.setTitle("Open File");
				   fileChooser.getExtensionFilters().addAll(
						   // Only look for text files and csv files
			                new FileChooser.ExtensionFilter("TXT", "*.txt*"),
			                new FileChooser.ExtensionFilter("CSV", "*.csv"));
				   File analysisFile =  fileChooser.showOpenDialog(stage);
				   if (analysisFile != null && analysisFile.exists()) { 
	                	// Extract the data from the csv
						extractAnalysis(analysisFile);
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
	  	 // Set PLM Paramters
	  	 MenuItem setPLM = new MenuItem("Set PLM Paramters");
	  	 setPLM.setOnAction(new EventHandler<ActionEvent>() {
	  	     public void handle(ActionEvent t) {
	  	     }
	  	 });
	  	 paramSetup.getItems().add(setPLM);
	  	 //Setup Ctrl+P to activate setPLM
	  	 setPLM.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
	  	 
	  	 // Display Paramters
	  	 MenuItem disp = new MenuItem("Set Display Paramters");
	  	 disp.setOnAction(new EventHandler<ActionEvent>() {
	  	     public void handle(ActionEvent t) {
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
		            while((line = bufferedReader.readLine()) != null) {
		                // YYYY-MM-DD hh:mm:ss.sss,accx,accy,accz,gyrx,gyry,gyrz
		                // 01234567890123456789012345678901234567890123456789012
		                
		                String[] value = line.split("");
		                // Check if the first value is a number = not title 
		            	boolean ret = true;
		                try {
		                    Double.parseDouble(value[0]);
	
		                }catch (NumberFormatException e) {
		                    ret = false;
		                }
		                
		            	if (ret) {
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
		            while((line = bufferedReader.readLine()) != null) {
		                // YYYY-MM-DD hh:mm:ss.sss,E,strG,duraS,intvS,d,
		                // 01234567890123456789012345678901234567890123456789012
		            	String[] value = line.split("");
		            	// Check if the first value is a number = not title/header
		            	boolean ret = true;
		                try {
		                    Double.parseDouble(value[0]);
	
		                }catch (NumberFormatException e) {
		                    ret = false;
		                }
		                
		            	if (ret) {
			                Calendar event = Calendar.getInstance();
			                // Parse the values for the date
			                event.set(Integer.parseInt(value[0] + value[1] + value[2] + value[3]),
			                		Integer.parseInt(value[5] + value[6]), Integer.parseInt(value[8] + value[9]),
			                		Integer.parseInt(value[11] + value[12]), Integer.parseInt(value[14] + value[15]), 
			                		Integer.parseInt(value[17] + value[18]));
			                event.set(Calendar.MILLISECOND, Integer.parseInt(value[20] + value[21] + value[22]));
			                // Parse the event type
			                String eType = value[24];
			                // Parse the strength
			                double str = Double.parseDouble(value[26] + value[27] + value[28] +  value[29]);
			                // Parse the duration
			                double dur = Double.parseDouble(value[31] + value[32] + value[33] +  value[34] + value[35]);
			                // Parse the interval - if text is nan, interval = positive infinity
			                double inv = Double.POSITIVE_INFINITY;
			                try {
			                	inv = Double.parseDouble((value[37] + value[38] + value[39] +  value[40] + value[41]).trim());
			                } catch (NumberFormatException nfe) {}
			                
			                // Parse if the leg is down or up - default true
			                boolean leg = true;
			                if (value[43].equals("f")) {
			                	leg = false;
			                }
			                // Parse the reason for rejection
			                String reason = "";
			                if (value.length > 45) {
				                for (int i = 45; i < value.length; i++) {
				                	reason += value[i];
				                }
			                }
			                // Create a movement with the values and add to the list
			                mov.add(new Movement(event.getTime(), eType, str, dur, inv, leg, reason));
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
	                analysisFile.getName() + "'");                
	      } catch(IOException ex) {
	            System.out.println(
	                "Error reading file '" 
	                + analysisFile.getName() + "'");
	      }
	      for (int i = 0; i < mov.size(); i++) {
	    	  System.out.println(mov.get(i));
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