//  EGT_main.java
//
//  Author:
//       Yi Ren <yiren001@cs.umb.edu>


package jmetal.problems.OptMolCom;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.ProblemFactory;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Configuration;
import jmetal.util.JMException;


public class NNEGT_main {
  public static Logger      logger_ ;      // Logger object
  public static FileHandler fileHandler_ ; // FileHandler object
  public static int n_runs = 3; // # sim runs
  public static int n_gen = 100; // # generations 100
  public static int n_pop = 1; // # players
  public static int n_obj = 2;
  public static int op = 0; //0:HV-NoCons
  public static int pop_size = 50; // 100
  public static double mutate_prob = 0.3;
  public static double[][][] finalResults = new double[n_runs][n_gen][n_obj];
  public static double[][] max = new double[n_gen][n_obj];
  public static double[][] min = new double[n_gen][n_obj];
  public static double[][] avg = new double[n_gen][n_obj];
  public static double[][] hv = new double[n_runs][n_gen];
  public static double finalHV[] = new double[n_gen];
  
  public static void main(String [] args) throws 
                                  JMException, 
                                  SecurityException, 
                                  IOException, 
                                  ClassNotFoundException {
	  
    Problem   problem   ; // The problem to solve
    Algorithm algorithm ; // The algorithm to use
    Operator  Polymutation  ; // Mutation operator
    Operator  selection ; // Selection operator   
    HashMap  parameters ; // Operator parameters
    
    QualityIndicator indicators ; // Object to get quality indicators

    // Logger object and file to store log messages
    logger_      = Configuration.logger_ ;
    fileHandler_ = new FileHandler("EGT_main.log"); 
    logger_.addHandler(fileHandler_) ;
        
    indicators = null ;
    if (args.length == 1) {
      Object [] params = {"Real"};
      problem = (new ProblemFactory()).getProblem(args[0],params);
    } // if
    else if (args.length == 2) {
      Object [] params = {"Real"};
      problem = (new ProblemFactory()).getProblem(args[0],params);
      indicators = new QualityIndicator(problem, args[1]) ;
    } // if
    else { // Default problem
      problem = new OptMolCom(); 

    } // else
    indicators = new QualityIndicator(problem,"cs_pareto_front.pf");
    algorithm = new NNEGT(problem);

    // Algorithm parameters
    algorithm.setInputParameter("populationSize",pop_size); //100
    algorithm.setInputParameter("maxEvaluations",n_gen); 
    algorithm.setInputParameter("maxPopulations",n_pop); //4
    algorithm.setInputParameter("combObjOp",op); //4
           

    parameters = new HashMap() ;
    parameters.put("probability", 1.0/problem.getNumberOfVariables()) ;
    parameters.put("distributionIndex", 20.0) ;//35 40 45 50 55 (60) 65
    Polymutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);                    

    // Selection Operator 
    parameters = null ;
    selection = SelectionFactory.getSelectionOperator("EGT_RandomSelection", parameters) ;                           

    // Add the operators to the algorithm
    algorithm.addOperator("Polymutation",Polymutation);
    algorithm.addOperator("selection",selection);

    // Add the indicator object to the algorithm
    algorithm.setInputParameter("indicators", indicators) ;
    
    // Execute the Algorithm
    for (int j = 0; j < n_runs; j++) {
    	System.out.println("============== RUNS: " + j + " =================");
    
    	algorithm.setInputParameter("runs",j);
	    long initTime = System.currentTimeMillis();
	    SolutionSet population = algorithm.execute();
	    long estimatedTime = System.currentTimeMillis() - initTime;
	    logger_.info("Total execution time: "+estimatedTime + "ms");
	    

     }//for
    
    //Write MAX MIN AVG and HV
    for (int i = 0; i < n_gen; i++){
    	
    	for (int j = 0; j < n_runs; j++) {
    	
    	// HV
    	finalHV[i]+=hv[j][i];
    	
    	// Max Min Avg
    		for (int k=0;k<n_obj;k++){
    			if (j==0){
    				max[i][k]=finalResults[j][i][k];
    				min[i][k]=finalResults[j][i][k];
    				
    			}else{				
    				if (finalResults[j][i][k]>max[i][k]){max[i][k]=finalResults[j][i][k];}
    				if (finalResults[j][i][k]<min[i][k]){min[i][k]=finalResults[j][i][k];}				
    			}
    			avg[i][k]+=finalResults[j][i][k];
    		}
    		
    	}
    	
    	for (int k=0;k<n_obj;k++){
    		avg[i][k]=avg[i][k]/n_runs;
    	}
    	finalHV[i]=finalHV[i]/n_runs;
    	
    	
    	  String st1="AVG: ";
	  	  String st2="MAX: ";
	  	  String st3="MIN: ";
	  	  for(int x=0;x<n_obj;x++)
	  	  {
	  		  st1=st1+avg[i][x]+" ";
	  		  st2=st2+max[i][x]+" ";
	  		  st3=st3+min[i][x]+" ";
	  	  }
	  	  
	  	writeResultToFile(st1+" "+st2+" "+st3,"OBJS_NNEGT.txt"); 
    	writeResultToFile((double)finalHV[i]+"","HV_NNEGT.txt");
    	 	
    }
    
  } //main
  
  
  
  public static void writeResultToFile(String st, String filename) {
		try { // Create file
			FileWriter fstream = new FileWriter("out\\NNEGT\\" + filename, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(st);
			out.newLine();
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}
} // EGT_main
