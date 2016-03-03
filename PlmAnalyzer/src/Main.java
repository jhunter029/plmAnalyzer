import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Menu;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.StringConverter;
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
		reports, aols, trim, about;
	// Button for leg movement analysis
	private Button analyze;
	// Data retrieved from csv
	private ArrayList<IndexPair> data;
	
	    
	/**
	* Method to start the GUI
	*
	* @param stage Stage for displaying
	*/
	@SuppressWarnings("unchecked")
	public void start(Stage stage) {
		
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
	    final CategoryAxis xAxis = new CategoryAxis();
	    final NumberAxis yAxis = new NumberAxis(0, 7, 1);
	    // Name the axes
	    xAxis.setLabel("Time");
	    yAxis.setLabel("Force (g)");
	    // Format the axes
	    xAxis.setAutoRanging(true);
	    yAxis.setAutoRanging(true);
	    // Format x-axis to handle dates
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	    //Create the line chart
	    LineChart<String, Number> chartPost = new LineChart<>(new CategoryAxis(), new NumberAxis());
	    // Set chart title
	    chartPost.setTitle("Leg Movement Analysis");
	    //chartPost.setCreateSymbols(false); // hide datepoint symbols
	    // Hide the chart legend
	    chartPost.setLegendVisible(false);
	    // Create a series to add to the chart
	    XYChart.Series<String, Number> adjusted = new XYChart.Series<String, Number>();
	
	    // Populate the series with data
	    for (int i = 0 ; i < data.size() ; i++) {
	        adjusted.getData().add(new XYChart.Data<String, Number> (
	        		data.get(i).getX(), data.get(i).getY()));
	    }
	    
	    // Change the data point text when the mouse is moved over the chart
		chartPost.setOnMouseMoved(new EventHandler<MouseEvent>() {
		      @Override public void handle(MouseEvent mouseEvent) {
		    	  String x = xAxis.getValueForDisplay(mouseEvent.getX());
		    	  double y = (double) yAxis.getValueForDisplay(mouseEvent.getY());
		    	  //if (data.contains(new IndexPair(x, y))) {
			          dataPoint.setText("Force: " + y + "; Time: " +x);
			       // }
		      	}
		});
		// Add series to chart
	    chartPost.getData().add(adjusted);
	    
	    
	    /*
	    // Create the table
	    TableView<String> table = new TableView<String>();
	    final ObservableList<String> tableList =
	        FXCollections.observableArrayList(
	            new String("Jacob"),
	            new String("Isabella"),
	            new String("Ethan"),
	            new String("Emma"),
	            new String("Michael")
	        );
	    
	    // Don't allow users to edit values in the table
	    table.setEditable(false);
	    // Limit the columns to what is designated here
	    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	    // Limit the amount of rows
	    table.setPrefHeight(75);
	    
	    TableColumn<String, String> ephCol = new TableColumn<String, String>
	    ("Avg PLMs/Hour");
	 
        TableColumn<String, String> conclusionCol = new TableColumn<String, String>
        (">25 PLMs/Hour?");
	 
	    table.setItems(tableList);
	    table.getColumns().addAll(conclusionCol, ephCol);
	     */
	    
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
	    slider.setMax(100);
	    slider.setValue(40);
	    slider.setShowTickLabels(false);
	    slider.setShowTickMarks(true);
	    slider.setMajorTickUnit(50);
	    slider.setMinorTickCount(5);
	    slider.setBlockIncrement(10);
	    
	    // Create a borderpane to hold all GUI objects
	    BorderPane borderpane = new BorderPane();
	    
	    // Setup the top menu toolbar
	    MenuBar menuBar = new MenuBar();
	    setupMenu();
	    menuBar.getMenus().addAll(file, monitor, display, paramSetup,
	    		reports, aols, trim, about);
	    
	    // Create a vertical box to show the charts
	    VBox content = new VBox();
	    // Add spacing between the children
	    content.setSpacing(15.0);
	    // Add the dataPoint text, the charts, the slider, the table, and the analyze button
	    content.getChildren().addAll(dataPoint, chartPost, slider, ephText, analyze);
	    		//chartPre, table - elements to add
	    // Give the bottom button some extra space
	    VBox.setMargin(analyze, new Insets(0.0, 0.0, 20.0, 20.0));
	    
	    // Add the menu to the top of the GUI
	    borderpane.setTop(menuBar);
	    // Anchor the content to the left
	    borderpane.setCenter(content);
	    
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
	public void setupMenu() {
	
	    // Toolbar Menu creation
	    file = new Menu("File");
	    setupFile();
	
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
	
	    aols = new Menu("Aols");
	    aols.setOnAction(e -> {
	    	}
	    );
	    
	    trim = new Menu("Trim");
	    trim.setOnAction(e -> {
	    	}
	    );
	
	    about = new Menu("About");
	    about.setOnAction(e -> {
	    	Alert alert = new Alert(AlertType.INFORMATION);
	    	alert.setTitle("About");
	    	alert.setHeaderText("About the PLM Analyzer");
	    	alert.setContentText("Michael Alexander Haver, Jennifer Hunter, Robert Lee, Kevin Powell, and Joesph Thompson"
	    			+ "design this PLM Analyzer for the Emory Sleep Lab. /n2016");
	
	    	alert.showAndWait();
	    	}
	    );
	    
	    // start with sort by date in focus
	    analyze.requestFocus();
	}
	
	/**
	 * Initialize Menu Items for the File menu
	 */
	 public void setupFile() {
		// Open
		 MenuItem open = new MenuItem("Open");
		 open.setOnAction(new EventHandler<ActionEvent>() {
			   public void handle(ActionEvent t) {
	  	     }
		 });
		 file.getItems().add(open);
		//Setup Ctrl+O to activate open
		 
		// Save As
		 MenuItem saveAs = new MenuItem("Save As");
		 saveAs.setOnAction(new EventHandler<ActionEvent>() {
			   public void handle(ActionEvent t) {
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
		  data.add(new IndexPair(sample,2));
		  sample.setTime(timeTest + 5000);
		  data.add(new IndexPair(sample,5));
		  sample.setTime(timeTest + 10000);
		  data.add(new IndexPair(sample,3));
		  sample.setTime(timeTest + 15000);
		  data.add(new IndexPair(sample,4));
		  sample.setTime(timeTest + 20000);
		  data.add(new IndexPair(sample,3));
		  sample.setTime(timeTest + 25000);
		  data.add(new IndexPair(sample,1));
	  }
	  
	  private class IndexPair {
		// Object for creating indexes in the chart
		  final Date x;
		  final double y;
		  SimpleDateFormat dateF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		  IndexPair(Date x, double y) {
			  this.x = new Date(x.getTime());
			  this.y=y;
			  }
		  
		  public String getX() {
			  return dateF.format(x);
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