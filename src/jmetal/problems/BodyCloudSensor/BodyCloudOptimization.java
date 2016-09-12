
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


package jmetal.problems.BodyCloudSensor;

import java.util.ArrayList;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.util.JMException;
import jmetal.util.wrapper.XReal;

/**
 * Class representing problem Cloud Optimization
 */
public class BodyCloudOptimization extends Problem {
	
	//EGT PARAMETERS
	//	public  int SIM_RUN = 5; // 5,20 Sim Runs
	//	public  int n_iteration = 100; // 100 interations
		public  int population_size = 100; // higher size better results but slow
		public  double E_T =0.01;// 0.001 Energy cost to transmit one unit of data from a sensor to a sensor
	//	public  int op = 5; // options for objective comb (EGT VARIANTS)
	//	public  int MUTATION_RATE = 1;
	//	public static int n_patients = 20; // # population 
		
		//RANGE CONTROL
		public static int n_requests = 400; // 5000 / 1000 number of requests / 1 day
		public static int total_time = 10800; // 43200 / 10800 secs/day =  3hours
		public  int mobile_range = total_time / 48; // [1800 sec] / 48 / 24
		public  int transmission_range = mobile_range / 12; // [300 sec] able to control DATA LOSE / 6
		public  int sensing_range = transmission_range / 4; // [10 sec] / 30 / 2
		
		public static int max_winPressure = 1000; // min max request time windows size
		public static int max_winAccel = 1800; // min max request time windows size
		public static int max_winECG = 600; // min max request time windows size
		//LOW RANGE RELATIONSHIP HIGHER DATA LOST BECAUSE SENSING RATE IS HIGHER! && SENSING_RANGE CANNOT BE 0! 
		
		//DATA SPECIFICATION
		public static int n_shimmer = 3;// ECG + ACCEL, BLOOD PRESSURE, ACCEL
		public static int n_node = 4; // sensors/shimmer WE JUST USE FOUR SENSORS
		public static int total_n_sensors = n_node; // n_shimmer * n_node; // total number of sensors
		public  int n_sensor = 0; // sensors index	
		public  int resolution = 16; // 16bits/sample = 2bytes/sample
		public  int pull_size = 100; // bytes of a pull request 
		public  int empty_size = 250; // bytes of returning an empty request 
		public  int[] data_rate = {125,250,500,1000,2000,4000,8000}; // samples/s
		public  double sd_storage = 2000000; //2 Mbytes
		public  double mobile_storage = 16000000; //16 Mbytes
		public  request[] request_list;
		public  int ExtraID = population_size; 
		private int DATA_YES = 0;
		
		//CONSTRAINTS//
		//public  double max_DL = 10; //max data lose %
		public  double min_RF = 97; // 95 min request fulfilled %
		public static  double max_BW = 200000;  //1Mbps  bytes/s
		public  double min_DY = 19000; // 50000 min data yield # of sensor data
		public  double max_EN = 150; // 32400 Watts
		
		//Bit-C PARAMETERS
		public  int n_objectives = 4;
		public  int n_constraints = 4;
		public  static int n_variables = total_n_sensors * 4; // 16 decision variables // 4 parameters/ sensor type/ patient
		
		
		//--------------------------------------------------------------------------------//

	
 /** 
  * Constructor.
  * Creates a default instance of problem CloudOptimization (30 decision variables)
  * @param solutionType The solution type must "Real", "BinaryReal, and "ArrayReal". 
	* ArrayReal, or ArrayRealC".
  */

	public static int getNV(){
		return n_variables;
	}
	
    public  void main(String[] args) {
		// TODO Auto-generated method stub
    	try {
			BodyCloudOptimization cl =new BodyCloudOptimization();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
		

 /**
  * Creates a new instance of problem ZDT1.
  * @param numberOfVariables Number of variables.
  * @param solutionType The solution type must "Real", "BinaryReal, and "ArrayReal". 
  */
  public BodyCloudOptimization() throws ClassNotFoundException {
    numberOfVariables_  = n_variables;
    numberOfObjectives_ =  n_objectives;
    numberOfConstraints_=  n_constraints;
    problemName_        = "BodyCloudOptimization";
  
    GenerateRequest();
       
    upperLimit_ = new double[numberOfVariables_];
    lowerLimit_ = new double[numberOfVariables_];

    // Establishes upper and lower limits for the variables
   /* Set parameters range depends on sensor type~ Problem... how to set different parameter range to different population.
    switch (EGT.Pop_Index) {
    	case 0:
    		break;
    	case 1:
    		break;
    	case 2:
    		break;
    	case 3:
    		break;
    	default:
    		break;
    		
    }
    */
    for (int var = 0; var < total_n_sensors; var++)
    {
      lowerLimit_[var*4+0] = transmission_range; //mobile tx interval
      upperLimit_[var*4+0] = mobile_range + transmission_range;
      lowerLimit_[var*4+1] = sensing_range; // sensor tx interval
      upperLimit_[var*4+1] = transmission_range + sensing_range;
      lowerLimit_[var*4+2] = 1; // sensor sensing interval
      upperLimit_[var*4+2] = sensing_range + 1;
    } // for
    //blood pressure sensor 
    lowerLimit_[3] = 10; // sensor sensing rate samples/s //10
    upperLimit_[3] = 50; //50
  //Accelerometer sensor 
    lowerLimit_[7] = 250; // sensor sensing rate samples/s
    upperLimit_[7] = 2000;
    lowerLimit_[11] = 250; // sensor sensing rate samples/s
    upperLimit_[11] = 2000;
  //ECG sensor 
    lowerLimit_[15] = 125; // sensor sensing rate samples/s
    upperLimit_[15] = 8000;
 
    
    
   	solutionType_ = new RealSolutionType(this) ;
   	
  } // CloudOptimization
  
  	// GENERATE REQUESTS
  	private  void GenerateRequest(){
  		//if(EGT_main.getTermination()==EGT_main.getTimes()){
  		if(EGT_main.getCtimes()!=0){
  		n_requests = n_requests *(1 +  EGT_main.getReqRate() / n_requests);
  		}
		request_list = new request[n_requests];
		for (int i = 0; i<n_requests ; i++){
			request_list[i] = new request(i);
		}	
	}
  
  /** 
   * Evaluates a solution.
   * @param solution The solution to evaluate.
   * @throws JMException 
   */
  public void evaluate(Solution solution) throws JMException {
	XReal str = new XReal(solution) ;
 		
	int pointer=0;
	boolean flagReq_FF = false;
	int RF_YES = 0;
	int RF_NO = 0;
	double requestFF = 0;
	double pushBW = 0;
	double pullBW = 0;
	double datayield = 0;
	double datalose = 0;
	double totalBW = 0;
	double totalEN = 0;
	double sensortotalyield = 0;
	double sensortotaltransmitted = 0;
	double mobiletotaltransmitted = 0;
	double totaltransmitted = 0;	
	boolean flag=false;
	boolean flagS=false;
	DATA_YES = 0;
	
	/* May used for different patient (to compute energy harvesting)
	switch (EGT.getPI()){
	  case 0:
		  max_SR = 50;
		  min_SR = 10;
		  break;
	  case 1:
		  max_SR = 2000;
		  min_SR = 250;
		  break;
	  case 2:
		  max_SR = 2000;
		  min_SR = 250;
		  break;
	  case 3:
		  max_SR = 8000;
		  min_SR = 125;
		  break;
	  default:
		  max_SR = 8000;
		  min_SR = 125;
		  break;
	}
	*/
	
    //Calculate 3 objectives 

	for(int i=0;i<BodyCloudOptimization.total_n_sensors;i++){
			
			
			 //----------GENERATE VIRTUAL NODE MAP -----------//
			slot[] virtual_map = new slot[BodyCloudOptimization.total_time];
			for(int x=0;x<BodyCloudOptimization.total_time;x++){
				virtual_map[x] = new slot();
			}
			
			while(pointer<BodyCloudOptimization.total_time){			
				virtual_map[pointer].MR=1;
				pointer += str.getValue(i*3+0);
			}
			
			pointer=0;
			while(pointer<BodyCloudOptimization.total_time){							
				virtual_map[pointer].TR=1;
				pointer += str.getValue(i*3+1);
			}
			pointer=0;
			while(pointer<BodyCloudOptimization.total_time){			
				virtual_map[pointer].SR=1;
				pointer += str.getValue(i*3+2);
			}   
			pointer=0;
			
			//-------compute requests-------//
			for (int k=0;k<BodyCloudOptimization.n_requests;k++){
				if (request_list[k].getRS()== i){
					flagS=true;
					for (int x=request_list[k].getIniT();x<request_list[k].getEndT();x++){
						if (virtual_map[x].SR==1){
							for (int y=x;y<request_list[k].getRT();y++){
								if(virtual_map[y].TR==1){							
									flagReq_FF = true; //data is yield from sensors
									datayield += (str.getValue(i*3+3)*16/8);
									DATA_YES++;
									//System.out.println("DATA");
									for (int z=y;z<request_list[k].getRT();z++){
										if(virtual_map[z].MR==1){
											flag = true;	// we have already data in the cloud			
											break;
										}
									}
									if(flag==false){ //data in the edge node, but not in the cloud virtual node
										pullBW += (str.getValue(i*3+3)*16/8); 										
									}else{flag=false;}
								break;
								}
							}
						}	
					}								
				}	
				
				if (flagReq_FF&&flagS){
					RF_YES++; 
					//System.out.println("FLAG");
					flagReq_FF=false;
					flagS=false;
					}else if (flagS&&!flagReq_FF){
						flagS=false;
						RF_NO++;
						mobiletotaltransmitted += pull_size;
						sensortotaltransmitted += empty_size;
						mobiletotaltransmitted += empty_size;
						//pullBW += empty_size;
					}
				
			}
			
		//========END compute request=======//
			
			//----COMPUTE DL-----//
			double sensorCollectData = str.getValue(i*3+1)/str.getValue(i*3+2)*(str.getValue(i*3+3)*16/8);
			sensortotalyield += sensorCollectData * (total_time/str.getValue(i*3+1));
			double sensorTransmissionData = 0;
			double mobileTransmissionData = 0;
			if (sensorCollectData>sd_storage){
					datalose = (total_time/str.getValue(i*3+1))*(sensorCollectData-sd_storage);
					sensorTransmissionData = sd_storage;
					//System.out.println("DATA LOSE");
				}else{
					sensorTransmissionData = sensorCollectData;
				}
			double mobileCollectData = str.getValue(0)/str.getValue(i*3+1) * sensorTransmissionData;
			if (mobileCollectData>mobile_storage){
				datalose += (total_time/str.getValue(0))*(mobileCollectData-mobile_storage);
				mobileTransmissionData = mobile_storage;
				//System.out.println("DATA LOSE");
			}else{
				mobileTransmissionData = mobileCollectData;
			}
			
			sensortotaltransmitted += sensorTransmissionData;
			mobiletotaltransmitted += mobileTransmissionData;
		
	}
	
			//datayield = pullBW; // this makes more sense
			totaltransmitted = sensortotaltransmitted + mobiletotaltransmitted + pullBW; //it should + pullBW !!!!!!
			pushBW = mobiletotaltransmitted; // this makes more sense.
			datalose = datalose * 100 / sensortotalyield; // %
			totalBW = (pushBW + pullBW); // /total_time; // Bytes/s
			//datayield = (datayield/total_time); // %
			datayield = DATA_YES; // %
			totalEN = totaltransmitted * E_T; // Watts // before was / total_time
			requestFF = (RF_NO *100 / n_requests );
			
			//System.out.println("REQUEST YES: " + RF_YES);
			//System.out.println("REQUEST FF: " + requestFF);
			//System.out.println("datayield: " + datayield);
			//System.out.println("totalBW: " + totalBW);
	
    //END
	
	
    solution.setObjective(0,requestFF); //min No FF
    solution.setObjective(1,totalBW);
    solution.setObjective(2,1/datayield);
    solution.setObjective(3,totalEN);
    
    
    //TESTING AREA
    /*
			solution.setObjective(0,totalBW);
			 solution.setObjective(1,totalEN);
			 */
  } // evaluate
   
  
  public void evaluateConstraints(Solution solution) throws JMException {
	  XReal str = new XReal(solution) ;
	  //Min Request Fulfillment constraint
	  double c0 = solution.getObjective(0)-(100-min_RF);
	  if(c0<0) c0=0;
	  solution.setConstraint(0, c0);
	
	  //Max Bandwidth Consumption constraint
	  double c1=solution.getObjective(1)-max_BW;
	  if(c1<0) c1=0;
	  solution.setConstraint(1, c1);
	
	  //Min Data Yield constraint
	  double c2=solution.getObjective(2)-(1/min_DY);
	  if(c2<0) c2=0;
	  solution.setConstraint(2, c2);
	
	  //Max Energy Consumption constraint
	  double c3=solution.getObjective(3)-max_EN;
	  if(c3<0) c3=0;
	  solution.setConstraint(3, c3);
	 

	//TESTING AREA
	  /*
	//Max Bandwidth Consumption constraint
	  double c1=solution.getObjective(0)-max_BW;
	  if(c1<0) c1=0;
	  solution.setConstraint(0, c1);
	  //Max Energy Consumption constraint
	  double c3=solution.getObjective(1)-max_EN;
	  if(c3<0) c3=0;
	  solution.setConstraint(1, c3);
	  */
	  
	  //solution.setConstraint(0, 0);
	  //solution.setConstraint(1, 0);
	  //solution.setConstraint(2, 0);
	  //solution.setConstraint(3, 0);
	 
	  
  }
 
  
  public static void setMax_BW(double bw){
	  max_BW=bw;
  }
  public static double getMax_BW(){
	  return max_BW;
  }
  
  public static int getNR(){
	  return n_requests;
  }
}
