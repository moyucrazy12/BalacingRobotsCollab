package src;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import se.lth.control.*;
import se.lth.control.plot.*;
import java.util.*;



/** Class for creating and maintaining the GUI. Contains a plotter
 * panel with an internal thread taking care of the plotting. */
public class Opcom {
  // Declaration of main frame.
  private JFrame frame;
  // Declaration of panels
  private BoxPanel boxPanel_Graphs;
  private JPanel guiPanel;
  // Declaration of sliders
  private JSlider desiredDist;
  // Declaration of text labels
  private JLabel sliderLabel;
  
  private PlotterPanel plotterx1, plotterx2, plotterx3, plotterx4, plotterD; // Plotter panel


  public Opcom() {
    plotterx1 = new PlotterPanel(2, 4); // Two channels, priority 4
    plotterx2 = new PlotterPanel(2, 4);
    plotterx3 = new PlotterPanel(2, 4);
    plotterx4 = new PlotterPanel(2, 4);
    plotterD = new PlotterPanel(2, 4);
  }

  /** Starts the thread in the plotter panel */
  public void start() {
    plotterx1.start();
    plotterx2.start();
    plotterx3.start();
    plotterx4.start();
    plotterD.start();
  }

  /** Create the GUI. Called from Main. */
  public void initializeGUI() {
    // Create new main window
    frame = new JFrame("MnSegPlot");

    // Create all the panels
    // Create the main panel that will become the content pane of frame
    guiPanel = new JPanel();
    desiredDist = new JSlider(0,20,10);
    sliderLabel = new JLabel();
    desiredDist.setPaintTrack(true);
    desiredDist.setPaintTicks(true);
    desiredDist.setPaintLabels(true);
    sliderLabel.setText("Desired separation distance");

    // set spacing
    desiredDist.setMajorTickSpacing(5);
    desiredDist.setMinorTickSpacing(1);

    boxPanel_Graphs = new BoxPanel();
    guiPanel.setLayout(new BorderLayout());

    // Set the y-axis and x-axis of the plotter panel: range, bottom,
    // number of divisions for tickmarks number of divisions for the
    // grid
    plotterx1.setBorder(BorderFactory.createEtchedBorder());
    plotterx1.setYAxis(160, -80, 2, 2); 
    plotterx1.setXAxis(100, 5, 5); 
    plotterx1.setTitle("Pitch speed (°/s)");
    plotterx1.setColor(1, Color.red);
    
    plotterx2.setBorder(BorderFactory.createEtchedBorder());
    plotterx2.setYAxis(20, -10, 2, 2); 
    plotterx2.setXAxis(100, 5, 5);
    plotterx2.setTitle("Pitch (°)");
    plotterx2.setColor(1, Color.blue);
    
    plotterx3.setBorder(BorderFactory.createEtchedBorder());
    plotterx3.setYAxis(100, -50, 2, 2); 
    plotterx3.setXAxis(100, 5, 5);
    plotterx3.setTitle("Angle (rad)");
    plotterx3.setColor(1, Color.green);
    
    plotterx4.setBorder(BorderFactory.createEtchedBorder());
    plotterx4.setYAxis(12, -6, 2, 2); 
    plotterx4.setXAxis(100, 5, 5);
    plotterx4.setTitle("Voltage (V)");
    plotterx4.setColor(1, Color.orange);
    
    plotterD.setBorder(BorderFactory.createEtchedBorder());
    plotterD.setYAxis(25, 0, 2, 2); 
    plotterD.setXAxis(100, 5, 5);
    plotterD.setTitle("Separation Distance (cm)");
    plotterD.setColor(1, Color.gray);
    
   
    // Add components to boxPanel
    boxPanel_Graphs.add(plotterx1);
    boxPanel_Graphs.add(plotterx2);
    boxPanel_Graphs.add(plotterx3);
    boxPanel_Graphs.add(plotterx4);
    boxPanel_Graphs.add(plotterD);
    boxPanel_Graphs.add(desiredDist);
    boxPanel_Graphs.add(sliderLabel);
    frame.add(boxPanel_Graphs);

    // Add a WindowListener that exists the system if the main
    // window is closed
    frame.addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) {System.exit(0);}
      });
    // Pack the components of the window. The size is calculated.
    frame.getContentPane().add(boxPanel_Graphs, BorderLayout.CENTER);
    frame.pack();

    // Code to position the window at the center of the screen
    Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension fd = frame.getSize();
    frame.setLocation((sd.width - fd.width)/2, (sd.height - fd.height)/2);

    // Make the window visible
    frame.setVisible(true);
  }


  /** Sends a new data point to the plotter panel */
  public void putDataPoint(DoublePoint dp) {
    plotterx1.putData(dp.x, dp.y);
  }
  
  public int getSliderValue () {
	  return desiredDist.getValue();
  }

  public void putDataPoint(double x, double y1, double y1_2, double y2, double y2_2, double y3, double y3_2, double y4, double y4_2) {
	    plotterx1.putData(x,y1, y1_2);
	    plotterx2.putData(x,y2, y2_2);
	    plotterx3.putData(x,y3, y3_2);
	    plotterx4.putData(x,y4, y4_2);
	    
  }
  public void putDataDistance (double x, double D, double DRef) {
	  plotterD.putData(x,D,DRef);
  }

}




