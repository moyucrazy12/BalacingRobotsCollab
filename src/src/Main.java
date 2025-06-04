package src;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import se.lth.control.*;
import se.lth.control.plot.*;
import java.util.*;
import se.lth.control.realtime.*;
import src.Opcom;
import src.Generator;

/** Main class used only for initialization */
class Main {

  /** main method called when application starts */
  public static void main(String[] args) {
    
	Monitor_Dis mon = new Monitor_Dis();
    Opcom opcom = new Opcom();
    opcom.initializeGUI();
    Generator gen = new Generator(opcom,mon);
    ServerCamera server = new ServerCamera(mon,gen);
    Thread serverThread = new Thread(server);
    //gen.setPriority(9);
    //serverThread.setPriority(8);
    serverThread.start();
    opcom.start();
    gen.start();
    
  }
}
            
