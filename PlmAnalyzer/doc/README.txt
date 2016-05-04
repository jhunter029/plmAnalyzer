JAVAFX 8

MAIN.JAVA
- Displays a Graphical User Interface(GUI) for the user to view PLM data both numerically and graphically.

start(Stage stage)
- Creates main GUI elements (chart, slider, menu toolbar, etc.)

setupMenu(Stage stage)
- Intializes the menus (File, Parameter Setup, Reports)

setupFile(Stage stage)
- Creates menu items for the File menu (Open, Save Analysis As, Import Analysis File, Exit)
- Note: For silly Java purposes, the chart and slider have to be removed and readded in order to update the displayed data. I never found a way to fix it. (I even asked a Stack Overflow question. That's how desparate I was. If you figure it out, good for you.)

setupParameterSetup() 
- Generates a dialog box to allow the user to adjust the xmin xmax, ymin, ymax of the chart on the main screen
- Note: This may error if data hasn't been imported.
- Note: The xmin, xmax is designated by how many data points are shown on screen at once.

setupReports()
- Generates a dialog box that allows the user to view nightly subsets of the chart and the events per hour for that night
- TO-DO: Generate the events per hour for the cummulative nights
- TO-DO: Fix the ymin, ymax for the individual views (Set to a little above the max datapoint for that section?)

calculateEPH(Date start, Date end)
- Calculates the average events per hour for the given Date span

extractData(File dataFile)
- Parses the raw data movements from the given data file

extractAnalysis(File analysisFile)
- Parses Movement objects from the given data file

plot()
- Adds hover nodes for given data and Movement objects (mov)

HoverThersholdNode()
- Creates a the hover nodes for the chart

main(String args[])
- Launches the GUI

MOVEMENT.JAVA
- Defines Movement objects with a start time, peak strength, duration, event type (P = PLM, R = REJECTED, I = ISOLATED, U= USER ADDED), interval, if the leg is down, and the rejections reason)

LINECHARTWITHMARKERS.JAVA
- Defines a type of LineChart that allows for horizontal and vertical lines to be added/removed as markers/thersholds

DATEAXIS.JAVA
- Defines a type of Axis that allows for Date objects

DATETIMEPICKER.JAVA
- Redefines a DatePicker to also allow for time so that a Date object can be created from the values
- TO-DO: It would be nice if the time could be chosen from that object rather than having to type it in.

If you absolutely need to, you can email me at j.hunter@gatech.edu
Written by Jennifer Hunter on 05/04/2016