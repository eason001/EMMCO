
//CloudOptimization.java
//  Authors: Dung Phan
//  <phdung@cs.ubm.edu>
//  Copyright (c) 2013 DUNG PHAN
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.


package jmetal.problems.cloudsensor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import jmetal.core.*;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.solutionType.BinaryRealSolutionType;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.util.JMException;
import jmetal.util.wrapper.XReal;

/**
 * Class representing problem Cloud Optimization
 */
public class CloudOptimization extends Problem {
	
/*Parameter Settings
 * Will be exported to files later on
 */
	/*public static  final double AR_=0.1; //pull request data size, from Edge to Cloud
	public static final  double AR_P_=0.05;// pull request data size, from Sensor to Sensor
	public static final double T_EC_=0.01;//unit secs, time needed to transmit a unit data size from Edge to Cloud
	public static final double T_SS_=0.005;//unit secs, time needed to transmit a unit data size from Edge to Cloud*/
	public static final double E_T_=0.001;//Energy cost to transmit one unit of data from a sensor to a sensor
	
	public static final double TH_YIELD_=20000000.0;
	public static final double TH_ENERGY_=500000.0;
	
//	public static final double TH_YIELD_=20000.0;
//	public static final double TH_ENERGY_=100000.0;
	
	
	//Three groups of sensors
	//private double [] requestTimeWindowMean_ ={300,600,1800};//mean secs
	//private double [] requestTimeWindowSTD_ ={60,120,360};//STD in secs 1/5 of means
	
	private double [] sensorDataSize_ ={15,100,128};//bytes
	
	private double TCP_HEADER_=40;//40 bytes
	private double TINYOS_HEADER_=28;//28 bytes
	private double PULL_SIZE_=4;//28 bytes
	
	
	
	
	final public int nSENSOR_TYPES_=3;//Three types
	final public int nSENSORS_=50;//id start from 0..49
	final public int nREQUESTS_=1000;//Number of requests
	final public int L_=86400;//Length of simulation window, default: 1 day
	private int [] sensorTypes_=null;
	private int [] hopCount_=null;
	
	public int nSensors_=50;

		
	private ArrayList<SensorRequest> requests_=new ArrayList<SensorRequest>();
	
	//private ArrayList<ArrayList<Double>> dataTSEdge_=null;
	//private ArrayList<ArrayList<Double>> dataTSCloud_=null;
	
 /** 
  * Constructor.
  * Creates a default instance of problem CloudOptimization (30 decision variables)
  * @param solutionType The solution type must "Real", "BinaryReal, and "ArrayReal". 
	* ArrayReal, or ArrayRealC".
  */
	
    public static void main(String[] args) {
		// TODO Auto-generated method stub
    	try {
			CloudOptimization cl =new CloudOptimization(100);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
		
  public CloudOptimization() throws ClassNotFoundException {
    this(100); // 30 variables by default
  } // ZDT1
  
 /**
  * Creates a new instance of problem ZDT1.
  * @param numberOfVariables Number of variables.
  * @param solutionType The solution type must "Real", "BinaryReal, and "ArrayReal". 
  */
  public CloudOptimization( Integer numberOfVariables) throws ClassNotFoundException {
    numberOfVariables_  = numberOfVariables.intValue();
    numberOfObjectives_ =  3;
    numberOfConstraints_=  2;
    problemName_        = "CloudOptimization";

    nSensors_=numberOfVariables/2;
    
    
    sensorTypes_=new int[nSensors_];
    for(int i=0;i<nSENSORS_;i++){
		//50, 25 type 1, 15 type 2, 10 type 3
		if(i<25) sensorTypes_[i]=0;
		else if(i<40) sensorTypes_[i]=1;
		else sensorTypes_[i]=2;
		//sensorTypes_[i]=generator_.nextInt(nSENSOR_TYPES_);
	}
    
    
    
    
    upperLimit_ = new double[numberOfVariables_];
    lowerLimit_ = new double[numberOfVariables_];

    // Establishes upper and lower limits for the variables
    for (int var = 0; var < numberOfVariables_; var++)
    {
      lowerLimit_[var] = 1.0/L_;
      upperLimit_[var] = 1.0;
    } // for

   	solutionType_ = new RealSolutionType(this) ;
   	
   	//Read request from file
	String fileName="cs50_100000.txt";
   	BufferedReader br;
	try {
		br = new BufferedReader(new FileReader(fileName));
		String line;
		int count=0;
		while ((line = br.readLine()) != null) {
			//Ignore the first line
			if(count==1){
				String [] data=line.split(" ");
				hopCount_=new int[nSensors_];
				for(int i=0;i<nSensors_;i++)
					hopCount_[i]=Integer.parseInt(data[i]);
					
			}
			if(count>1){
				String [] data=line.split(" ");
				SensorRequest request=new SensorRequest(Integer.parseInt(data[0]),
						                                Integer.parseInt(data[1]),
						                                Integer.parseInt(data[2]),
						                                Integer.parseInt(data[3]),
						                                Double.parseDouble(data[4]),
						                                Double.parseDouble(data[5]));
				requests_.add(request);
			//	System.out.println(request.getRequestTimeWindow());
			}
			count++;
		}
		br.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  } // CloudOptimization
  
 
  
  /** 
   * Evaluates a solution.
   * @param solution The solution to evaluate.
   * @throws JMException 
   */
  public void evaluate(Solution solution) throws JMException {
	XReal x = new XReal(solution) ;
 		
    getDataYieldForEachRequest(x);
    
    //Calculate 3 objectives 
    
    double bandwidth=0.0;
    double energy=0.0;
	for(int i=0;i<nSensors_;i++){
		  //push bandwidth
		  double ve=x.getValue(nSensors_+i);
		  bandwidth=bandwidth+ve*(sensorDataSize_[sensorTypes_[i]]+TCP_HEADER_);
		  double vs=x.getValue(i);
		  energy=energy+vs*(sensorDataSize_[sensorTypes_[i]]+TINYOS_HEADER_)*L_*E_T_*hopCount_[i];
	
	  }
	  
	  
	  //Pull bandwidth
	  double temp=0.0;
	  double temp2=0.0;
	  double sum=0.0;
	  for(int j=0;j<requests_.size();j++){
		  SensorRequest request=requests_.get(j);
		  if(request.atLevel_==1||request.atLevel_==2)
			  temp=temp+(request.data_yield_*(sensorDataSize_[request.getSensorType()]+TCP_HEADER_)+PULL_SIZE_+TCP_HEADER_);
		  
		  if(request.atLevel_==2)
			  temp2=temp2+(sensorDataSize_[request.getSensorType()]+TINYOS_HEADER_+PULL_SIZE_+TINYOS_HEADER_)*E_T_*request.getHopCount();
	 
		  sum=sum+request.data_yield_;
	  }
	  
	  temp=temp/L_;
	  bandwidth=bandwidth+temp;
	  energy=energy+temp2;
    
    
    //END
    
 /*   f[0]=calculateBandwidthConsumption(x);
    f[1]=calculateEnergyConsumption(x);
    f[2]=calculateEnergyDataYield();*/
    
    solution.setObjective(0,bandwidth);
    solution.setObjective(1,energy);
    solution.setObjective(2,1.0/sum);
  } // evaluate
   
  
  public void evaluateConstraints(Solution solution) throws JMException {
	  //Energy Consumption, 2nd objective
	  double c1=solution.getObjective(1)-TH_ENERGY_;
	  if(c1<0) c1=0;
	  solution.setConstraint(0, c1);
	  double c2=solution.getObjective(2)-1.0/TH_YIELD_;
	  if(c2<0) c2=0;
	  solution.setConstraint(1, c2);
	  //Data yield, 3rd objective
	  
	  
  }
  /**
   * Returns the value of the ZDT1 function G.
   * @param decisionVariables The decision variables of the solution to 
   * evaluate.
   * @throws JMException 
   */
  /*private double evalG(XReal x) throws JMException {
    double g = 0.0;        
    for (int i = 1; i < x.getNumberOfDecisionVariables();i++)
      g += x.getValue(i);
    double constante = (9.0 / (numberOfVariables_-1));
    g = constante * g;
    g = g + 1.0;
    return g;
  } // evalG
    
  /**
   * Returns the value of the ZDT1 function H.
   * @param f First argument of the function H.
   * @param g Second argument of the function H.
   */
  /*public double evalH(double f, double g) {
    double h = 0.0;
    h = 1.0 - java.lang.Math.sqrt(f/g);
    return h;        
  } // evalH*/
  
  //First Objective: Bandwidth Consumption
  /*
  public double calculateBandwidthConsumption(XReal x) throws JMException{
	 
	  double bandwidth=0.0;
	  for(int i=0;i<nSensors_;i++){
		  //push bandwidth
		  double ve=x.getValue(nSensors_+i);
		  bandwidth=bandwidth+ve*(sensorDataSize_[sensorTypes_[i]]+TCP_HEADER_);
	
	  }
	  //Pull bandwidth
	  double temp=0.0;
	  for(int j=0;j<requests_.size();j++){
		  SensorRequest request=requests_.get(j);
		  if(request.atLevel_==1||request.atLevel_==2)
			  temp=temp+(request.data_yield_*(sensorDataSize_[request.getSensorType()]+TCP_HEADER_)+PULL_SIZE_+TCP_HEADER_);
	  }
	  
	  temp=temp/L_;
	  bandwidth=bandwidth+temp;
	
	  
	  return bandwidth;
  }
  
  public double calculateEnergyConsumption(XReal x) throws JMException{

	  double energy=0.0;
	  for(int i=0;i<nSensors_;i++){
		  //push bandwidth
		  double vs=x.getValue(i);
		  energy=energy+vs*(sensorDataSize_[sensorTypes_[i]]+TINYOS_HEADER_)*L_*E_T_*hopCount_[i];
	
	  }
	  //Pull bandwidth
	  double temp=0.0;
	  for(int j=0;j<requests_.size();j++){
		  SensorRequest request=requests_.get(j);
		  if(request.atLevel_==2)
			  temp=temp+(sensorDataSize_[request.getSensorType()]+TINYOS_HEADER_+PULL_SIZE_+TINYOS_HEADER_)*E_T_*request.getHopCount();
	  }
	  
	  energy=energy+temp;
	  
	  return energy;
	  
  }
  
  public double calculateEnergyDataYield(){
	  int sum=0;
	  for(int j=0;j<requests_.size();j++){
		  SensorRequest request=requests_.get(j);
		  sum=sum+request.data_yield_;
	  }
	  return -(double)sum;
  }
  */
  //This method updates the timestamp of push data based on the push rate
 /* private void updateDataTimeStamp(XReal x) throws JMException{
	  dataTSEdge_=new  ArrayList<ArrayList<Double>>();
	  dataTSCloud_=new  ArrayList<ArrayList<Double>>();
	  for (int i = 0; i < nSensors_;i++){
		 double vs=x.getValue(i);
		 double ve=x.getValue(nSensors_+i);
		 long nPushSensor=Math.round(vs*L_);
		 long nPushEdge=Math.round(ve*L_);
		 
		 //Timestamp sensor go first
		 double interval=1.0/vs;
		 double time=0.0;
		 ArrayList<Double> tsEdge=new ArrayList<Double>();
		 while(time<=L_){
			 tsEdge.add(time);
			 time=time+interval;
		 }
		 dataTSEdge_.add(tsEdge);
		 
		 //Update time from edge
		 
		 interval=1.0/ve;
		 time=0.0;
		 ArrayList<Double> tsCloud=new ArrayList<Double>();
		 while(time<=L_){
			 tsCloud.add(time);
			 time=time+interval;
		 }
		 dataTSCloud_.add(tsCloud);
		 
		  
	  }
	  
	  
  }*/
  
  private void getDataYieldForEachRequest(XReal x) throws JMException
  {
	  for(int i=0;i<requests_.size();i++){
		  SensorRequest request=requests_.get(i);
		  double time=request.getRequestTime();
		  double window=request.getRequestTimeWindow();
		  int sensor_id=request.getSensorID();
		  
		  double vs=x.getValue(sensor_id);
		  double ve=x.getValue(nSensors_+sensor_id);
		  
		  int temp1=(int)Math.floor(time*ve);
		  int count=0;
		  int check=-1;
		  //Check data yield for the request
		  for(int k=temp1;k>=0;k--)
		  {
			  if((double)k/ve>=time-window){
				  int temp2=(int)Math.floor((double)(k*vs)/ve);//closest value
				  if(temp2*vs>=time-window)
					  if(check!=temp2){
						  count++;
						  check=temp2;
					  }else{}
				  else break;
			  }
			  
		  }
		  //
		  if(count!=0){  //Get data at cloud level
			  request.data_yield_=count;
			  request.atLevel_=0;
			//  System.out.println("Level 0:"+count);
			
		  }else{	//Check data at Edge level
			  temp1=(int)Math.floor(time*vs);
			  count=0;
			  for(int k=temp1;k>=0;k--)
			  {
				  if((double)k/vs>=time-window){
					  count++;
				  }else break;
			  }
			  if(count!=0){ //Data can get at edge level
				  request.data_yield_=count;
				  request.atLevel_=1;
				 
			  }else{ //Sensor level
				  request.data_yield_=1;
				  request.atLevel_=2;
				
			  }
				  
		  }
		 
		 
	  }
  }
  
  
}
