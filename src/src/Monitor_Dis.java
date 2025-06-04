package src;

public class Monitor_Dis {
	
	private double distance;
	private double desiredDist = 20.0; // cm
	private double tolerance = 5.0; // cm
	private boolean OK = false;

	public Monitor_Dis() {}
	
	//Get the real separation distance
	public synchronized double getDistance() {
		return distance;
	}
	
	//Set OK when the communication with python code is established
	public synchronized void setOK() {
		OK = true;
	}
	
	//Get the status of the connection
	public synchronized boolean getOK() {
		return OK;
	}
	
	public synchronized void setDistance(double d) {
		distance = d;
	}
	public synchronized double getDesiredDist() {
		return desiredDist;
	}
	public synchronized void setDesiredDist(double newDesiredDist){
		desiredDist = newDesiredDist;
	}

	// This class also contains a method to calculate the approriate signal to be sent
	// to the MinSeg, depending on the MinSegs' relative positions to each other
	
	public synchronized double calculateOutPut() {
		double ref;
		//Reference calculation
		ref = (distance - desiredDist)/2;
		return ref; 
	}

}
