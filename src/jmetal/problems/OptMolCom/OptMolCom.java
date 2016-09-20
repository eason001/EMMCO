
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


package jmetal.problems.OptMolCom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.problems.OptMolCom.MolComSim.MolComSim;
import jmetal.util.JMException;
import jmetal.util.wrapper.XReal;

/**
 * Class representing problem Cloud Optimization
 */
public class OptMolCom extends Problem {
	
	//MolCom parameters
	
	int mediumDimensionX = 150;
	int mediumDimensionY = 150;
	int mediumDimensionZ = 150;
	
	int stepLengthX = 1;
	int stepLengthY = 1;
	int stepLengthZ = 1;
	
	int maxSimulationStep = 25000000;
	
	String transmitter = "(-15, 0, 0) 3 (-15, 0, 0)";
	String receiver = "(15, 0, 0) 3 (15, 0, 0)";
	String intermediateNodeA = "(-10, 0, 0) 3 (-10, 0, 0) (-10, 0, 0) 50 INFO ACTIVE 0 50 ACK ACTIVE 0";
	String intermediateNodeB = "(-5, 0, 0) 3 (-5, 0, 0) (-5, 0, 0) 50 INFO ACTIVE 0 50 ACK ACTIVE 0";
	String intermediateNodeC = "(0, 0, 0) 3 (0, 0, 0) (0, 0, 0) 50 INFO ACTIVE 0 50 ACK ACTIVE 0";
	String intermediateNodeD = "(5, 0, 0) 3 (5, 0, 0) (5, 0, 0) 50 INFO ACTIVE 0 50 ACK ACTIVE 0";
	String intermediateNodeE = "(10, 0, 0) 3 (10, 0, 0) (10, 0, 0) 50 INFO ACTIVE 0 50 ACK ACTIVE 0";
	String microtubuleParams = "(-15, 0, 0) (15, 0, 0)";
	
	int velRail = 1;
	double probDRail = 0.25;
	int useCollisions = 1;
	int useAcknowledgements = 1;
	int decomposing = 0;
	int numMessages = 1;	
	int numRetransmissions = 5; // DV
	int retransmitWaitTime = 1000; // DV
	
	String moleculeParamsINFOaux = " INFO ACTIVE 0"; // DV ACTIVE PASSIVE
	String moleculeParamsACKaux = " ACK ACTIVE 0"; // DV
	String moleculeParamsNOISE = "100000 NOISE"; // 0 100K 338K
	String[] INFOlist = {" INFO ACTIVE 0"," INFO ACTIVE 0"," INFO ACTIVE 0"," INFO ACTIVE 0"," INFO ACTIVE 0"};
	String[] ACKlist = {" ACK ACTIVE 0"," ACK ACTIVE 0"," ACK ACTIVE 0"," ACK ACTIVE 0"," ACK ACTIVE 0"};
	
	String outputFile = "output.txt";
	
	public int n_objectives = 2;
	public int n_constraints = 0;	
	public int n_variables	= 5;
	public int sim_run = 10; // 10
	
		//--------------------------------------------------------------------------------//

	
 /** 
  * Constructor.
  * Creates a default instance of problem CloudOptimization (30 decision variables)
  * @param solutionType The solution type must "Real", "BinaryReal, and "ArrayReal". 
	* ArrayReal, or ArrayRealC".
  */
	
    public  void main(String[] args) {
		// TODO Auto-generated method stub
    	try {
			OptMolCom cl =new OptMolCom();
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
  public OptMolCom() throws ClassNotFoundException {
    numberOfVariables_  = n_variables;
    numberOfObjectives_ =  n_objectives;
    numberOfConstraints_=  n_constraints;
    problemName_        = "OptMolCom";
         
    upperLimit_ = new double[numberOfVariables_];
    lowerLimit_ = new double[numberOfVariables_];


  //# of INFO molecules 
    lowerLimit_[0] = 1; 
    upperLimit_[0] = 100; 
  //# of RTx
    lowerLimit_[1] = 0;
    upperLimit_[1] = 10;
  //RTx time out 500 1000 1500 2000 
    lowerLimit_[2] = 1; 
    upperLimit_[2] = 4;
    //PROTOCOL PASSIVE ACTIVE
    lowerLimit_[3] = 1;  //ACT ACT
    upperLimit_[3] = 2; // PAS ACT
    upperLimit_[3] = 3; // ACT PAS
    upperLimit_[3] = 4; // PAS PAS
  //# of of ACK molecules 
    lowerLimit_[4] = 1; 
    upperLimit_[4] = 100; 
  
   	solutionType_ = new RealSolutionType(this) ;
   	
  } // CloudOptimization
  

  
  /** 
   * Evaluates a solution.
   * @param solution The solution to evaluate.
   * @throws JMException 
   */
  public void evaluate(Solution solution) throws JMException {
	XReal str = new XReal(solution) ;
 	
	int SuccessRate = 0;
	int RTT = 0;
	int simStep = 0;
	int pop_index = EGT.getPI();
	
	Map<String,String> map = new HashMap<String,String>();
	
	String moleculeParams = "50";
	moleculeParamsINFOaux = moleculeParams + moleculeParamsINFOaux;
	moleculeParamsACKaux = moleculeParams + moleculeParamsACKaux;
	
	numRetransmissions = (int) str.getValue(1);
/* RTO
ACT ACT 30 153;169;166;154
ACT ACT 50 1405;1461;1383;1427
ACT ACT 90 11520;11218;11499;10927

PAS PAS 30 1278;1186;1255;1259 // 20000, 200 OK
PAS PAS 50 6416;6466;6488;6202 // 20000, 200 OK
PAS PAS 90 19871;19683;20113;19833 // 200000, 200

ACT PAS 30 661;668;648;646 // 4000, 200 OK
ACT PAS 50 3742;3887;3954;4058 // 20000, 200 OK
ACT PAS 90 16898;17393;17168;16894 //200000, 200

PAS ACT = ACT PAS
*/
		
	
		switch (pop_index) {		
    	case 0: 
    		switch ((int)str.getValue(3)) {
    		case 1: 
    			moleculeParamsINFOaux = " INFO ACTIVE 0"; 
        		break;
        	case 2: 
        		moleculeParamsINFOaux = " INFO ACTIVE 0"; 
        		break;
        	case 3: 
        		moleculeParamsINFOaux = " INFO PASSIVE 0"; 
        		break;
        	case 4: 
        		moleculeParamsINFOaux = " INFO PASSIVE 0"; 
        		break;
        	default:
        		moleculeParamsINFOaux = " INFO ACTIVE 0"; 
        		break;
    		}
    		moleculeParamsINFOaux = Integer.toString((int)str.getValue(0)) + moleculeParamsINFOaux;
    		break;
    	case 1: 
    		switch ((int)str.getValue(3)) {
    		case 1: 
    			intermediateNodeA = "(-10, 0, 0) 3 (-10, 0, 0) (-10, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO ACTIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK ACTIVE 0";
        		break;
        	case 2: 
        		intermediateNodeA = "(-10, 0, 0) 3 (-10, 0, 0) (-10, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO ACTIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK PASSIVE 0";
        		break;
        	case 3: 
        		intermediateNodeA = "(-10, 0, 0) 3 (-10, 0, 0) (-10, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO PASSIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK ACTIVE 0";
        		break;
        	case 4: 
        		intermediateNodeA = "(-10, 0, 0) 3 (-10, 0, 0) (-10, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO PASSIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK PASSIVE 0";
        		break;
        	default:
        		intermediateNodeA = "(-10, 0, 0) 3 (-10, 0, 0) (-10, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO ACTIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK ACTIVE 0";
        		break;
    		}
    		break;
    	case 2: 
    		switch ((int)str.getValue(3)) {
    		case 1: 
    			intermediateNodeB = "(-5, 0, 0) 3 (-5, 0, 0) (-5, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO ACTIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK ACTIVE 0";
        		break;
        	case 2: 
        		intermediateNodeB = "(-5, 0, 0) 3 (-5, 0, 0) (-5, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO ACTIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK PASSIVE 0";
        		break;
        	case 3: 
        		intermediateNodeB = "(-5, 0, 0) 3 (-5, 0, 0) (-5, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO PASSIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK ACTIVE 0";
        		break;
        	case 4: 
        		intermediateNodeB = "(-5, 0, 0) 3 (-5, 0, 0) (-5, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO PASSIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK PASSIVE 0";
        		break;
        	default:
        		intermediateNodeB = "(-5, 0, 0) 3 (-5, 0, 0) (-5, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO ACTIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK ACTIVE 0";
        		break;
    		}
    		break;
    	case 3: 
    		switch ((int)str.getValue(3)) {
    		case 1: 
    			intermediateNodeC = "(0, 0, 0) 3 (0, 0, 0) (0, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO ACTIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK ACTIVE 0";
        		break;
        	case 2: 
        		intermediateNodeC = "(0, 0, 0) 3 (0, 0, 0) (0, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO ACTIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK PASSIVE 0";
        		break;
        	case 3: 
        		intermediateNodeC = "(0, 0, 0) 3 (0, 0, 0) (0, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO PASSIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK ACTIVE 0";
        		break;
        	case 4: 
        		intermediateNodeC = "(0, 0, 0) 3 (0, 0, 0) (0, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO PASSIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK PASSIVE 0";
        		break;
        	default:
        		intermediateNodeC = "(0, 0, 0) 3 (0, 0, 0) (0, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO ACTIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK ACTIVE 0";
        		break;
    		}
    		break;
    	case 4: 
    		switch ((int)str.getValue(3)) {
    		case 1: 
    			intermediateNodeD = "(5, 0, 0) 3 (5, 0, 0) (5, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO ACTIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK ACTIVE 0";
        		break;
        	case 2: 
        		intermediateNodeD = "(5, 0, 0) 3 (5, 0, 0) (5, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO ACTIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK PASSIVE 0";
        		break;
        	case 3: 
        		intermediateNodeD = "(5, 0, 0) 3 (5, 0, 0) (5, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO PASSIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK ACTIVE 0";
        		break;
        	case 4: 
        		intermediateNodeD = "(5, 0, 0) 3 (5, 0, 0) (5, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO PASSIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK PASSIVE 0";
        		break;
        	default:
        		intermediateNodeD = "(5, 0, 0) 3 (5, 0, 0) (5, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO ACTIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK ACTIVE 0";
        		break;
    		}
    		break;
    	case 5: 
    		switch ((int)str.getValue(3)) {
    		case 1: 
    			intermediateNodeE = "(10, 0, 0) 3 (10, 0, 0) (10, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO ACTIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK ACTIVE 0";
        		break;
        	case 2: 
        		intermediateNodeE = "(10, 0, 0) 3 (10, 0, 0) (10, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO ACTIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK PASSIVE 0";
        		break;
        	case 3: 
        		intermediateNodeE = "(10, 0, 0) 3 (10, 0, 0) (10, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO PASSIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK ACTIVE 0";
        		break;
        	case 4: 
        		intermediateNodeE = "(10, 0, 0) 3 (10, 0, 0) (10, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO PASSIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK PASSIVE 0";
        		break;
        	default:
        		intermediateNodeE = "(10, 0, 0) 3 (10, 0, 0) (10, 0, 0) " + Integer.toString((int)str.getValue(0)) + " INFO ACTIVE 0 "+ Integer.toString((int)str.getValue(4)) +" ACK ACTIVE 0";
        		break;
    		}
    		break;
    	case 6: 
    		switch ((int)str.getValue(3)) {
    		case 1: 
    			moleculeParamsACKaux = " ACK ACTIVE 0";
        		break;
        	case 2: 
    			moleculeParamsACKaux = " ACK PASSIVE 0";
        		break;
        	case 3: 
    			moleculeParamsACKaux = " ACK ACTIVE 0";
        		break;
        	case 4: 
    			moleculeParamsACKaux = " ACK PASSIVE 0";
        		break;
        	default:
    			moleculeParamsACKaux = " ACK ACTIVE 0";
        		break;
    		}
    		moleculeParamsACKaux = Integer.toString((int)str.getValue(4)) + moleculeParamsACKaux;
    		break;   		
    	default:
    		retransmitWaitTime = 100;
    		break;		
		}
	
	switch ((int)str.getValue(3)) {
		case 1: 
			switch ((int)str.getValue(2)) {
	    	case 1: //RTO=2*RTT
	    		retransmitWaitTime = 153;
	    		break;
	    	case 2: //RTO=RTT+0.5*STD
	    		retransmitWaitTime = 169;
	    		break;
	    	case 3: //RTO=RTT+STD
	    		retransmitWaitTime = 166;
	    		break;
	    	case 4: //RTO=RTT+0.33*STD
	    		retransmitWaitTime = 154;
	    		break;
	    	default:
	    		retransmitWaitTime = 100;
	    		break;    		
			}
    		break;
    	case 2: 
    		switch ((int)str.getValue(2)) {
        	case 1: //RTO=2*RTT
        		retransmitWaitTime = 661;
        		break;
        	case 2: //RTO=RTT+0.5*STD
        		retransmitWaitTime = 668;
        		break;
        	case 3: //RTO=RTT+STD
        		retransmitWaitTime = 648;
        		break;
        	case 4: //RTO=RTT+0.33*STD
        		retransmitWaitTime = 646;
        		break;
        	default:
        		retransmitWaitTime = 1000;
        		break;        		
    		}
    		break;
    	case 3: 
    		switch ((int)str.getValue(2)) {
        	case 1: //RTO=2*RTT
        		retransmitWaitTime = 661;
        		break;
        	case 2: //RTO=RTT+0.5*STD
        		retransmitWaitTime = 668;
        		break;
        	case 3: //RTO=RTT+STD
        		retransmitWaitTime = 648;
        		break;
        	case 4: //RTO=RTT+0.33*STD
        		retransmitWaitTime = 646;
        		break;
        	default:
        		retransmitWaitTime = 1000;
        		break;     		
    		}
    		break;
    	case 4: 
    		switch ((int)str.getValue(2)) {
        	case 1: //RTO=2*RTT
        		retransmitWaitTime = 1278;
        		break;
        	case 2: //RTO=RTT+0.5*STD
        		retransmitWaitTime = 1186;
        		break;
        	case 3: //RTO=RTT+STD
        		retransmitWaitTime = 1255;
        		break;
        	case 4: //RTO=RTT+0.33*STD
        		retransmitWaitTime = 1259;
        		break;
        	default:
        		retransmitWaitTime = 1000;
        		break;       		
    		}
    		break;
    	default:
    		switch ((int)str.getValue(2)) {
        	case 1: //RTO=2*RTT
        		retransmitWaitTime = 153;
        		break;
        	case 2: //RTO=RTT+0.5*STD
        		retransmitWaitTime = 169;
        		break;
        	case 3: //RTO=RTT+STD
        		retransmitWaitTime = 166;
        		break;
        	case 4: //RTO=RTT+0.33*STD
        		retransmitWaitTime = 154;
        		break;
        	default:
        		retransmitWaitTime = 100;
        		break;        		
    		}
    		break;
	}	
	
	
	String moleculeParamsINFO = moleculeParamsINFOaux;
	String moleculeParamsACK = moleculeParamsACKaux;	
		
	map.put("mediumDimensionX", Integer.toString(mediumDimensionX));
	map.put("mediumDimensionY", Integer.toString(mediumDimensionY));
	map.put("mediumDimensionZ", Integer.toString(mediumDimensionZ));
	
	map.put("stepLengthX", Integer.toString(stepLengthX));
	map.put("stepLengthY", Integer.toString(stepLengthY));
	map.put("stepLengthZ", Integer.toString(stepLengthZ));
	
	map.put("maxSimulationStep", Integer.toString(maxSimulationStep));
	map.put("velRail", Integer.toString(velRail));
	map.put("probDRail", Double.toString(probDRail));
	map.put("useCollisions", Integer.toString(useCollisions));
	map.put("useAcknowledgements", Integer.toString(useAcknowledgements));
	map.put("decomposing", Integer.toString(decomposing));
	map.put("numMessages", Integer.toString(numMessages));
	map.put("numRetransmissions", Integer.toString(numRetransmissions));
	map.put("retransmitWaitTime", Integer.toString(retransmitWaitTime));
	map.put("decomposing", Integer.toString(decomposing));
	
	map.put("microtubuleParams", microtubuleParams);
	map.put("intermediateNodeA", intermediateNodeA);
	map.put("intermediateNodeB", intermediateNodeB);
	map.put("intermediateNodeC", intermediateNodeC);
	map.put("intermediateNodeD", intermediateNodeD);
	map.put("intermediateNodeE", intermediateNodeE);
	map.put("receiver", receiver);
	map.put("transmitter", transmitter);
	
	map.put("moleculeParamsINFO", moleculeParamsINFO);
	map.put("moleculeParamsACK", moleculeParamsACK);
	map.put("moleculeParamsNOISE", moleculeParamsNOISE);
	map.put("outputFile", outputFile);
	/*
	Set set = map.entrySet();
	Iterator iterator = set.iterator();
	while(iterator.hasNext()) {
        Map.Entry mentry = (Map.Entry)iterator.next();
        System.out.print("key is: "+ mentry.getKey() + " & Value is: ");
        System.out.println(mentry.getValue());
     }
	*/
	System.out.println("start evaluating . . .");
    //Calculate 2 objectives 
	for(int i = 0 ; i < sim_run ; i ++){
	
		MolComSim mcs = new MolComSim();
		try {
			simStep = mcs.runSim(map);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(simStep<=retransmitWaitTime*numRetransmissions){SuccessRate++;}
		System.out.println("   " + i + " : " + simStep + " steps");
		RTT += simStep;	
	}	
	
	RTT = RTT / sim_run;
	SuccessRate = SuccessRate / sim_run * 100;
		
    //END
	
	System.out.println("RTT: " + RTT);
	System.out.println("SuccessRate: " + SuccessRate);
	
    solution.setObjective(0,RTT);
  //  solution.setObjective(1,RTT);//Round Trip Time
    solution.setObjective(1,101-SuccessRate); // Failure Rate    

  } 
  
  
  public void evaluateConstraints(Solution solution) throws JMException {
	  XReal str = new XReal(solution) ;
	  /*
	  //Min Request Fulfillment constraint
	  double c0 = solution.getObjective(0)-(100-min_RF);
	  if(c0<0) c0=0;
	  solution.setConstraint(0, c0);
	*/
	  
  }
 
  
}
