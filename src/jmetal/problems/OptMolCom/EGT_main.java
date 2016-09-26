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

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPVector;
import org.rosuda.REngine.Rserve.*;


public class EGT_main {
  public static Logger      logger_ ;      // Logger object
  public static FileHandler fileHandler_ ; // FileHandler object
  public static int n_runs = 3; // # sim runs 3 
  public static int n_gen = 50; // # generations 20 50
  public static int n_pop = 7; // # nodes 7
  public static int n_obj = 2;
  public static int op = 10; //4:PD 5:HV 6:PD-HV 7:HVC 8:MM 9:PD-MM 10:HV-NoCons
  public static int pop_size = 50; // 50
  public static double mutate_prob = 0.2; //(1/v)
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
                                  ClassNotFoundException,
  								  Exception{
    Problem   problem   ; // The problem to solve
    Algorithm algorithm ; // The algorithm to use
    Operator  crossover ; // Crossover operator
    Operator  Polymutation  ; // Mutation operator
    Operator  Tabumutation  ; // Mutation operator
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
      //problem = new Kursawe("BinaryReal", 3);
      //problem = new Water("Real");
      //problem = new ZDT1("ArrayReal", 100);
      //problem = new ConstrEx("Real");
      //problem = new DTLZ1("Real");
      //problem = new OKA2("Real") ;
    } // else
    indicators = new QualityIndicator(problem,"cs_pareto_front.pf");
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
    parameters.put("distributionIndex", 20.0) ;//35 40 45 50 55 (60) 65
    Polymutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);                    

    parameters = new HashMap() ;
    parameters.put("probability", 1.0/problem.getNumberOfVariables()) ;
    parameters.put("distributionIndex", 20.0) ;//35 40 45 50 55 (60) 65
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
    
    // Execute the Algorithm
    for (int j = 0; j < n_runs; j++) {
    	System.out.println("============== RUNS: " + j + " =================");
     //   Rtest();
    	algorithm.setInputParameter("runs",j);
	    long initTime = System.currentTimeMillis();
	    SolutionSet population = algorithm.execute();
	    long estimatedTime = System.currentTimeMillis() - initTime;
	    logger_.info("Total execution time: "+estimatedTime + "ms");
	    
	    
	    // Result messages 
	    logger_.info("Total execution time: "+estimatedTime + "ms");
	    logger_.info("Variables values have been writen to file VAR");
	    population.printVariablesToFile("VAR_" + j);    
	    logger_.info("Objectives values have been writen to file FUN");
	    population.printObjectivesToFile("FUN_" + j);
	  
	    if (indicators != null) {
	      logger_.info("Quality indicators") ;
	      logger_.info("Hypervolume: " + indicators.getHypervolume(population)) ;
	      logger_.info("GD         : " + indicators.getGD(population)) ;
	      logger_.info("IGD        : " + indicators.getIGD(population)) ;
	      logger_.info("Spread     : " + indicators.getSpread(population)) ;
	      logger_.info("Epsilon    : " + indicators.getEpsilon(population)) ;  
	     
	      int evaluations = ((Integer)algorithm.getOutputParameter("evaluations")).intValue();
	      logger_.info("Speed      : " + evaluations + " evaluations") ;      
	    } // if
	   
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
	  	  
	  	writeResultToFile(st1+" "+st2+" "+st3,"OBJS_EGT.txt"); 
    	writeResultToFile((double)finalHV[i]+"","HV_EGT.txt");
    	 	
    }
    
  } //main
  
  
  
  public static void writeResultToFile(String st, String filename) {
		try { // Create file
			FileWriter fstream = new FileWriter("out\\EGT\\" + filename, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(st);
			out.newLine();
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}
  public static void Rtest(){
	  double[] myvalues = {1.0, 1.5, 2.2, 0.5, 0.9, 1.12, 4.6, 2.3, 1.2, 4.5, 7.8, 9.0};
	 // double[][] test = { { 1.0, 2.0, 2.4, 5.1, 3.5 }, { 3.0, 4.0, 12.3, 2.5, 4.6 } };
		RConnection c = null;
		try{
			c = new RConnection();
			c.eval("library(ddalpha)");
			/*c.assign("res", test[0]);
			for (int i = 1; i < 2; i++) {
			  c.assign("tmp", test[i]);
			  c.eval("res<-rbind(res,tmp)");
			}	*/
			c.assign("res", myvalues);
			c.eval("res<-matrix(res,6)");
			c.eval("depths<-depth.halfspace(res, res)");
			double[] x = c.eval("depths").asDoubles();
			System.out.println("depth: " + x[0]);
			//x = c.eval("sd(myvalues)");
			//System.out.println("sd: " + x.asDouble());
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if ( c != null ){
				try{
					c.close();
				}finally{}
			}
		}
  }
} // EGT_main
