
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.VPos;

/*
 * GUI for PLM Analyzer system
 * @version 2016_03_02
 * @author Jennifer Hunter
 */
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
	// Data retrieved from csv
	private ArrayList<IndexPair> data;
	// How many data points can be seen at a screen at once
	private int screenCapacity = 10;
	// Format for dates on x-axis
	private SimpleDateFormat dateFormat;
	    
	/**
	* Method to start the GUI
	*
	* @param stage Stage for displaying
	*/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void start(Stage stage) {
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
	    
	    // Extract the data from the csv
	    extractData();
	    
	    // Adjusted Line Chart
	    DateAxis xAxis = new DateAxis();
	    NumberAxis yAxis = new NumberAxis(0, 7, 1);
	    // Name the axes
	    xAxis.setLabel("Time");
	    yAxis.setLabel("Force (g)");
	    // Format the axes
	    xAxis.setAutoRanging(true);
	    yAxis.setAutoRanging(true);
	
	    //Create the line chart
	    LineChart<Date, Number> chartPost = new LineChart<Date, Number>(xAxis, yAxis);
	    // Set chart title
	    chartPost.setTitle("Leg Movement Analysis");
	    // Hide datepoint symbols
	    chartPost.setCreateSymbols(false);
	    // Hide the chart legend
	    chartPost.setLegendVisible(false);
	    // Create a series to add to the chart
	    XYChart.Series<Date, Number> adjusted = new XYChart.Series<Date, Number>();
	
	    // Create a threshold line
	    XYChart.Series<Date, Number> threshold = new XYChart.Series<Date, Number>();
	    // Populate the series with data
	    for (int i = 0 ; i < data.size() ; i++) {
	        adjusted.getData().add(new XYChart.Data<Date, Number> (
	        		data.get(i).getX(), data.get(i).getY()));
	        threshold.getData().add(new XYChart.Data<Date, Number> (
	        		data.get(i).getX(), 1));
	    }
	    
	   
	    
	    // Change the data point text when the mouse is moved over the chart
		chartPost.setOnMouseMoved(new EventHandler<MouseEvent>() {
		      @Override public void handle(MouseEvent mouseEvent) {
		    	  // The values were off by random amounts.
		    	  // So, I hard-coded the adjustment values to make them correct.
		    	  
		    	  // Gets the x and y values of the chart under the mouse
		    	  Date x = xAxis.getValueForDisplay(mouseEvent.getX());
		    	  double y = (double) yAxis.getValueForDisplay(mouseEvent.getY()) + 0.84;
		    	  Calendar cal = Calendar.getInstance();
		    	  cal.setTime(x);
		    	  cal.add(Calendar.SECOND, -20);
		    	  //Format the date to a prettier string
		    	  
		    	  NumberFormat numFormat = new DecimalFormat("#0.00");     
			      // Update the string on the top of the chart
		    	  dataPoint.setText("Force: " + numFormat.format(y)
			          	+ "; Time: " + dateFormat.format(cal.getTime()));
			       // }
		      	}
		});
		// Add series to chart
	    chartPost.getData().addAll(adjusted, threshold);
	    
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
	            	 if (val < 25.0) {
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
	    slider.setMax(data.size() - 1);
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
            // the upper bound is the value of the slider
            int up = (int) Math.round((double)newVal);
            // the lower bound is the slider value minus the screen capacity
            int low = (int) Math.round((double)newVal) - screenCapacity;
            // If the lower bound is less than 0, set it to 0
            low = (low < 0)? 0 : low;
            // If the lower bound is greater than max - screen capacity,
            // set it equal to the max slider value - screen capacity
            low = (low > data.size() - 1 - screenCapacity)? 
            		data.size() - 1 - screenCapacity : low;
            // If the upper bound, is less than the screen capacity,
            // set it to the screen capacity
            up = (up < screenCapacity)? screenCapacity : up;

            // Get the date values for the bounds
            Date upper = data.get(up).getX();
            Date lower = data.get(low).getX();

            // Set the upper and lower bounds of the chart
            ((DateAxis)chartPost.getXAxis()).setUpperBound(upper);
            ((DateAxis)chartPost.getXAxis()).setLowerBound(lower);

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
	    content.getChildren().addAll(dataPoint, chartPost, slider, ephText, analyze);
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
	
	    // Set the Title of the GUI (show up on top bar)
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
				   FileChooser fileChooser = new FileChooser();
				   fileChooser.setTitle("Open File");
				   fileChooser.getExtensionFilters().addAll(
			                new FileChooser.ExtensionFilter("TXT", "*.txt*"),
			                new FileChooser.ExtensionFilter("CSV", "*.csv"));
				   fileChooser.showOpenDialog(stage);
	  	     }
		 });
		 file.getItems().add(open);
		//Setup Ctrl+O to activate open
		 
		// Save As
		 MenuItem saveAs = new MenuItem("Save As");
		 saveAs.setOnAction(new EventHandler<ActionEvent>() {
			   public void handle(ActionEvent t) {
				   FileChooser fileChooser1 = new FileChooser();
				   fileChooser1.setTitle("Save As");
				   File file = fileChooser1.showSaveDialog(stage);
	  	     }
		 });
		 file.getItems().add(saveAs);
		 
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
		 
		// Print
		 MenuItem print = new MenuItem("Print");
		 print.setOnAction(new EventHandler<ActionEvent>() {
			   public void handle(ActionEvent t) {
	  	     }
		 });
		 file.getItems().add(print);
		 
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
	  */
	  public void extractData() {
		  // Extract data with file i/o shiz
		  // For now just put random numbers
		  data = new ArrayList<IndexPair>();
		  Date sample = new Date();
		  long timeTest = sample.getTime();
		  
		  for (int i = 0; i < 50; i++) {
			  // Update sample time
			  sample.setTime(timeTest);
			  // Generate random force
			  Random ran = new Random(); 
			  int rand = ran.nextInt(7);
			  // Add the new data point
			  data.add(new IndexPair(sample, rand));
			  // Increment the time variable
			  timeTest += 5000;
		  }
	  }
	  
	  private class IndexPair {
		// Object for creating indexes in the chart
		  final Date x;
		  final double y;
		  IndexPair(Date x, double y) {
			  this.x = new Date(x.getTime());
			  this.y=y;
		  }
		  
		  public Date getX() {
			  return x;
		  }
		  
		  public double getY() {
			  return y;
		  }
		  
		  @Override
		  public boolean equals(Object obj) {
		      if (obj == null) {
		          return false;
		      }
		      if (!IndexPair.class.isAssignableFrom(obj.getClass())) {
		          return false;
		      }
		      final IndexPair other = (IndexPair) obj;
		      if ((this.x != other.x) || (this.y != other.y)) {
		          return false;
		      }
		      return true;
		  }
		  
		  @Override
		  public int hashCode(){
		      return 11 * Objects.hashCode(x) + 13 * Objects.hashCode(y) + 17;
		  }
	  }
    
	//JavaFX applications use the main method to launch the GUI.
	//It should only ever contain the call to the launch method
    public static void main(String[] args) {
        launch(args);
    }
}
