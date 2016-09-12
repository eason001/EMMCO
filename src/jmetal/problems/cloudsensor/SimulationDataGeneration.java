//SensorRequest.java
//  Authors: Dung Phan
//  <phdung@cs.ubm.edu>


package jmetal.problems.cloudsensor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

public class SimulationDataGeneration {

	final public int nSENSOR_TYPES_=3;//Three types
	final public int nSENSORS_=50;//id start from 0..49
	final public int nREQUESTS_=1000;//Number of requests
	final public int L_=86400;//Length of simulation window, default: 1 day
	final public int MAX_HOP_=5;
	
	final public double TH_YIELD=25000000.00;
	final public double TH_ENERGY=500000.0;
	
	//Three groups of sensors
	private double [] requestTimeWindowMean_ ={300,600,1800};//mean secs
	private double [] requestTimeWindowSTD_ ={60,120,360};//STD in secs 1/5 of means
	
	private Random generator_ = new Random();
	private int [] sensorTypes_=new int[nSENSORS_];
	private int [] sensorHop_=new int[nSENSORS_];
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SimulationDataGeneration dataGen=new SimulationDataGeneration();
		String fileName="cs50_100000.txt";
		dataGen.generateSetOfRequests(100000,fileName);
	}
	
	//Constructor, Initialize Sensor type
	
	public SimulationDataGeneration()
	{
		for(int i=0;i<nSENSORS_;i++){
			//50, 25 type 1, 15 type 2, 10 type 3
			if(i<25) sensorTypes_[i]=0;
			else if(i<40) sensorTypes_[i]=1;
			else sensorTypes_[i]=2;
			sensorHop_[i]=generator_.nextInt(MAX_HOP_)+1;
		}
	}
	
	//Generate Requests
	//ID,Sensor ID,tij,dij
	public void generateSetOfRequests(int N, String fileName)
	{
		String st="";
		//Line 1: # of sensors; # of sensor types
		st="N_S:"+nSENSORS_+ " "+"N_T:"+nSENSOR_TYPES_;
		writeResultToFile(st,fileName);
		//Line 2: time windows of each type
		String h="";
		for(int i=0;i<sensorHop_.length;i++)
			h=h+sensorHop_[i]+" ";
		writeResultToFile(h,fileName);
		
		
		for(int i=0;i<N;i++){
			int sensor_id=getUniformDistributionTargetSensor() ;
			st= i  //id
				+" "+sensor_id//sensor id
				+" "+sensorTypes_[sensor_id]//sensor type
				+" "+sensorHop_[sensor_id]//hop counts
				+" "+getUniformDistributionRequestTime()  //Time
				+" "+getNormalDistributionTimeWindow(sensorTypes_[sensor_id]);
			writeResultToFile(st,fileName);
		}
		System.out.println("Generated: "+N+" requests"+" ->"+fileName);
			
	}
	
	
	//Get Uniformly Request time
	public double getUniformDistributionRequestTime()
	{
		double t=generator_.nextDouble()*L_;
		return round1000(t);
	}
	
	//Get Uniformly Random Sensor
	public int getUniformDistributionTargetSensor()
	{
		int s=generator_.nextInt(nSENSORS_);
		return s;
	}
	
	//Get Normal Distribution Time, input: sensor type:0,1,2
	public double getNormalDistributionTimeWindow(int id){
		double d=generator_.nextGaussian()*requestTimeWindowSTD_[id]+requestTimeWindowMean_[id];
		return round1000(d);
	}
	
	//Utility function
	public static double round1000(double value) {
		double result = value * 1000;
		result = Math.round(result);
		result = result / 1000;
		return result;
	}
	//write Result to files
	public void writeResultToFile(String st, String filename) {
  		try { // Create file
  			FileWriter fstream = new FileWriter(filename, true);
  			BufferedWriter out = new BufferedWriter(fstream);
  			out.write(st);
  			out.newLine();
  			out.close();
  		} catch (Exception e) {// Catch exception if any
  			System.err.println("Error: " + e.getMessage());
  		}
  	}

}
