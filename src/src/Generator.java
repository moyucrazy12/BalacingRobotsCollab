package src;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.UnsupportedEncodingException;

import se.lth.control.*;
import se.lth.control.plot.*;
import java.util.*;
import se.lth.control.realtime.*;
import com.fazecast.jSerialComm.*;

public class Generator extends Thread {

	private Opcom opcom;
	private Monitor_Dis mon;
	private int startTime = 0;
	private double h = 1;
	private double currentSecond = startTime;
	private boolean doIt = true;
	private boolean connected = false;

	public Generator(Opcom opc, Monitor_Dis mon) {
		this.opcom = opc;
		this.mon = mon;
	}
	
	//Plot desired separation distance and real separation distance
	public void setDGraph(double dist, double Dref) {
		opcom.putDataDistance(currentSecond, dist, Dref);
	}
	//BT connection
	public SerialPort BT_connection(int port) {
		SerialPort [] AvailablePorts = SerialPort.getCommPorts();
	       
        //Open the first available port
        SerialPort MySerialPort = AvailablePorts[port];		//check ports
        
        System.out.println(MySerialPort.getSystemPortName());
        System.out.println(MySerialPort.getDescriptivePortName());
        
        
        int BaudRate = 9600;
        int DataBits = 8;
        int StopBits = SerialPort.ONE_STOP_BIT;
        int Parity   = SerialPort.NO_PARITY;

        //Sets all serial port parameters at one time
        MySerialPort.setComPortParameters(BaudRate,
                                          DataBits,
                                          StopBits,
                                            Parity);

        //Set Read Time outs
        MySerialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 
                                         1000, 
                                            0); 

        MySerialPort.openPort(); //open the port
		
		
		return MySerialPort;
	}
	
	public boolean connected() {
		return connected;
	}

	public void run() {
		double v1 = 0;
		double v2 = 0;
		double v3 = 0;
		double v4 = 0;
		
		double u1 = 0;
		double u2 = 0;
		double u3 = 0;
		double u4 = 0;
		
		//Open both ports
		SerialPort MySerialPort = BT_connection(2);
		SerialPort MySerialPort2 = BT_connection(3);
		
		while (doIt) {
			System.out.println("Done");
			connected = true;
			
			//Plot last received data
			opcom.putDataPoint(currentSecond, v1, u1, v2, u2, v3, u3, v4, u4);
			
			//Update the desired distance using the slider
			mon.setDesiredDist((double)opcom.getSliderValue());
			
			//Plot desired separation distance
			setDGraph(mon.getDistance(), (double)opcom.getSliderValue());
		    			
			byte[] readBuffer = new byte[24]; //10
            byte[] readBuffer2 = new byte[24]; //10
            
            //Convert bytes to String for both MinSeg
            String S = null;
			try {
				S = new String(readBuffer, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
            String S2 = null;
			try {
				S2 = new String(readBuffer2, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 

            char test = S.charAt(0);
            char test2 = S2.charAt(23);
            
            //
            while(test != '+' && test2 != '\n') {
           	 System.out.print("Fixing1");
           	 MySerialPort.flushIOBuffers();
//           	 MySerialPort.openPort();
//           	 System.out.println("Received -> "+ S);
           	 MySerialPort.readBytes(readBuffer, readBuffer.length);
           	 try {
				S = new String(readBuffer, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           	 test = S.charAt(0);
            }
            
            while(test2 != '+') {
           	 System.out.print("Fixing2");
           	 MySerialPort2.flushIOBuffers();
           	 MySerialPort2.readBytes(readBuffer2, readBuffer2.length);
           	 try {
				S2 = new String(readBuffer2, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           	 test2 = S2.charAt(0);

            }
            
            // Split data for the MinSeg1
            String[] parts = S.split("/");
            String x1 = parts[0];	   
            x1 = x1.substring(1);
            String x2 = parts[1];
            String x4 = parts[2];
            String u = parts[3];
            
            // Split data for the MinSeg2
            String[] parts2 = S2.split("/");
            String x1_2 = parts2[0];	   
            x1_2 = x1_2.substring(1);
            String x2_2 = parts2[1];
            String x4_2 = parts2[2];
            String u_2 = parts2[3];
            
            System.out.println("X1.1 = " + x1 + " X2.1 = " + x2 + " X4.1 = " + x4 + " U.1 = " + u);
            System.out.println("X1.2 = " + x1_2 + " X2.2 = " + x2_2 + " X4.2 = " + x4_2 + " U.2 = " + u_2);
		
			//MinSeg 1 data
            v1 = Double.parseDouble(x1);
            v2 = Double.parseDouble(x2);
            v3 = Double.parseDouble(x4);
            v4 = Double.parseDouble(u)/51;
            
           //MinSeg 2 data
            u1 = Double.parseDouble(x1_2);
            u2 = Double.parseDouble(x2_2);
            u3 = Double.parseDouble(x4_2);
            u4 = Double.parseDouble(u_2)/51;
            

			try {
				//Delay
				Thread.sleep(30);
			} catch (InterruptedException e) {
			}
			
			
			double Sout;
			
			//Send real separation distance to Arduino by BT
			try 
	      	{
	      			if(mon.getOK()) {
					Sout = mon.calculateOutPut();
	      			String So = String.format("%.3f",Sout);
	      			
	      			byte[] WriteByte = So.getBytes();
	      			
	      			MySerialPort2.writeBytes(WriteByte,WriteByte.length);
	      			
	      			System.out.println(Sout);
	      			System.out.println(So);

	      			System.out.println(" Word Transmitted -> " + Arrays.toString(WriteByte) );
	      			}
	      			   			
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			currentSecond+=h;

		}
	}

}
