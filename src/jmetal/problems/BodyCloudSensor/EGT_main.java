//  EGT_main.java
//
//  Author:
//       Yi Ren <yiren001@cs.umb.edu>
// IEEE HEalthCom 2015: Dynamic deployment run multiple times the WHOLE EGT PROCEDURES

package jmetal.problems.BodyCloudSensor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
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


public class EGT_main {
  public static Logger      logger_ ;      // Logger object
  public static FileHandler fileHandler_ ; // FileHandler object
  public static int n_runs = 3; // # sim runs 20
  public static int n_gen = 100; // # generations 100
  public static int n_pop = 100; // # patients 100
  public static int n_obj = 4;
  public static int op = 5; //4:PD 5:HV 6:PD-HV 7:HVC 8:MM 9:PD-MM 
  public static int pop_size = 50; // 100 50 20
  public static double[][][] finalResults = new double[n_runs][n_gen][n_obj];
  public static double[][] max = new double[n_gen][n_obj];
  public static double[][] min = new double[n_gen][n_obj];
  public static double[][] avg = new double[n_gen][n_obj];
  public static double[][] hv = new double[n_runs][n_gen];
  public static double finalHV[] = new double[n_gen];
  
  public static Problem   problem   ; // The problem to solve
  public static Algorithm algorithm ; // The algorithm to use
  public static Operator  crossover ; // Crossover operator
  public static Operator  Polymutation  ; // Mutation operator
  public static Operator  Tabumutation  ; // Mutation operator
  public static Operator  selection ; // Selection operator
  
  public static HashMap  parameters ; // Operator parameters
  public static QualityIndicator indicators ; // Object to get quality indicators
 
  //DYNAMIC CONFIG PARAMETERS
  public static int n_times = 10;
  public static int termination=10;
  public static double maxdiff = 0.001; //0.1%
  public static int c_times = 0;
  public static SolutionSet populationFinal;
  public static int LSoption = 0; // 0: polymutation 1: Tabu 2: TabuPoly
  public static int pop_rate = 0;
  public static int request_rate = 0;
  public static int pop_inc = 50;
  public static int request_inc = 50;
  public static String folder = "Ter10G_50_env";
  public static double dist=30;
  
  public static void main(String [] args) throws 
                                  JMException, 
                                  SecurityException, 
                                  IOException, 
                                  ClassNotFoundException {

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
      problem = new BodyCloudOptimization(); 

    } // else
    indicators = new QualityIndicator(problem,"cs_pareto_front.pf");
    
   
    // Execute the Algorithm
    
    for(int i=0; i<n_times; i++){
    	    System.out.println("============== TIMES: " + i + " =================");
        
        	c_times = i;
        	finalResults = new double[n_runs][n_gen][n_obj];
        	finalHV = new double[n_gen];
        	max = new double[n_gen][n_obj];
        	min = new double[n_gen][n_obj];
        	avg = new double[n_gen][n_obj];
        	hv = new double[n_runs][n_gen];
        	pop_rate = new Random().nextInt(n_pop*2*pop_inc/100) - (n_pop*pop_inc/100);	
        	request_rate = new Random().nextInt(BodyCloudOptimization.getNR()*2*request_inc/100) - (BodyCloudOptimization.getNR()*request_inc/100);	
    		n_pop=n_pop*(1+pop_rate/100); 
    		problem = new BodyCloudOptimization(); 
    		configure();
    		
		    for (int j = 0; j < n_runs; j++) {
		    	System.out.println("============== RUNS: " + j + " =================");
		    //	BodyCloudOptimization.iniHost();
		    	BodyCloudOptimization.setMax_BW(BodyCloudOptimization.max_BW); //initialize the max BW
		    	algorithm.setInputParameter("runs",j);
			    long initTime = System.currentTimeMillis();
			    SolutionSet population = algorithm.execute();
			    long estimatedTime = System.currentTimeMillis() - initTime;
			    logger_.info("Total execution time: "+estimatedTime + "ms");
			    
			}//for
		    
		  //Write MAX MIN AVG and HV
		    computeOut();
    }
    	    
	    System.out.println("============END===========");
  } //main
  
  
  public static void computeOut(){
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
		  	  
		//      if (termination==n_times){
			  	writeResultToFile(st1+" "+st2+" "+st3, c_times + "_OBJS_EGT.txt"); 
		    	writeResultToFile((double)finalHV[i]+"", c_times + "_HV_EGT.txt");	    	 	
		//      }
	  }
  }
  
  public static void writeResultToFile(String st, String filename) {
		try { // Create file
			FileWriter fstream = new FileWriter(new File("out\\"+ folder + "\\" + "50_" + filename), true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(st);
			out.newLine();
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}
  
  
  public static void configure() throws JMException{
	    algorithm = new EGT(problem);
	    //algorithm = new ssNSGAII(problem);

	    // Algorithm parameters
	    algorithm.setInputParameter("populationSize",pop_size); //100
	    algorithm.setInputParameter("maxEvaluations",n_gen); 
	    algorithm.setInputParameter("maxPopulations",n_pop); //4
	    algorithm.setInputParameter("combObjOp",op); //4
	   // algorithm.setInputParameter("n_population",10);
	    // Mutation and Crossover for Real codification              

	    parameters = new HashMap() ;
	    parameters.put("probability", 1.0/problem.getNumberOfVariables()) ;
	    parameters.put("distributionIndex", dist) ;//35 40 45 50 55 (60) 65
	    Polymutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);                    

	    parameters = new HashMap() ;
	    parameters.put("probability", 1.0/problem.getNumberOfVariables()) ;
	    parameters.put("distributionIndex", dist) ;//35 40 45 50 55 (60) 65
	    Tabumutation = MutationFactory.getMutationOperator("TabuPolynomialMutation", parameters); 
	    // Selection Operator 
	    parameters = null ;
	    selection = SelectionFactory.getSelectionOperator("EGT_RandomSelection", parameters) ;                           

	    // Add the operators to the algorithm
	    algorithm.addOperator("Polymutation",Polymutation);
	    algorithm.addOperator("Tabumutation",Tabumutation);
	    algorithm.addOperator("selection",selection);

	    // Add the indicator object to the algorithm
	    algorithm.setInputParameter("indicators", indicators) ;
  }
  
	public static int getTimes(){
		return n_times;
	}
	public static int getCtimes(){
		return c_times;
	}
	public static int getTermination(){
		return termination;
	}
	public static SolutionSet getpopulationFinal(){
		return populationFinal;
	}
	public static void setpopulationFinal(SolutionSet pop){
		populationFinal = pop;
	}
	public static int getPopRate(){
		return pop_rate;
	}
	public static int getReqRate(){
		return request_rate;
	}
	public static double getDiff(){
		return maxdiff;
	}
		
	
} // EGT_main
