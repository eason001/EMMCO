//  NSGAII_main.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
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


import jmetal.core.*;
import jmetal.operators.crossover.*;
import jmetal.operators.mutation.*;
import jmetal.operators.selection.*;
import jmetal.problems.*                  ;
import jmetal.problems.DTLZ.*;
import jmetal.problems.ZDT.*;
import jmetal.problems.WFG.*;
import jmetal.problems.LZ09.* ;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.MersenneTwisterFast;
import jmetal.util.PseudoRandom;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.* ;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import jmetal.qualityIndicator.QualityIndicator;

/** 
 * Class to configure and execute the NSGA-II algorithm.  
 *     
 * Besides the classic NSGA-II, a steady-state version (ssNSGAII) is also
 * included (See: J.J. Durillo, A.J. Nebro, F. Luna and E. Alba 
 *                  "On the Effect of the Steady-State Selection Scheme in 
 *                  Multi-Objective Genetic Algorithms"
 *                  5th International Conference, EMO 2009, pp: 183-197. 
 *                  April 2009)
 */ 

public class NSGAII_main {
  public static Logger      logger_ ;      // Logger object
  public static FileHandler fileHandler_ ; // FileHandler object
  public static int n_runs = 1; // 20 sim runs
  public static int n_gen = 20; // 600 generations
  public static int n_pop = 3; // 10 patients
  public static int n_obj = 4;
  public static int pop_size = 50;
  public static double[][][] finalResults = new double[n_runs][n_gen][n_obj];
  public static double[][] max = new double[n_gen][n_obj];
  public static double[][] min = new double[n_gen][n_obj];
  public static double[][] avg = new double[n_gen][n_obj];
  public static double[][] hv = new double[n_runs][n_gen];
  public static double finalHV[] = new double[n_gen];
  public static SolutionSet population;
  public static SolutionSet popaux;
  public static double sumup;
  /**
   * @param args Command line arguments.
   * @throws JMException 
   * @throws IOException 
   * @throws SecurityException 
   * Usage: three options
   *      - jmetal.metaheuristics.nsgaII.NSGAII_main
   *      - jmetal.metaheuristics.nsgaII.NSGAII_main problemName
   *      - jmetal.metaheuristics.nsgaII.NSGAII_main problemName paretoFrontFile
   */
  public static void main(String [] args) throws 
                                  JMException, 
                                  SecurityException, 
                                  IOException, 
                                  ClassNotFoundException {
    Problem   problem   ; // The problem to solve
    Algorithm algorithm ; // The algorithm to use
    Operator  crossover ; // Crossover operator
    Operator  mutation  ; // Mutation operator
    Operator  selection ; // Selection operator
    
    HashMap  parameters ; // Operator parameters
    
    QualityIndicator indicators ; // Object to get quality indicators

    // Logger object and file to store log messages
    logger_      = Configuration.logger_ ;
    fileHandler_ = new FileHandler("NSGAII_main.log"); 
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
      //problem = new Kursawe("BinaryReal", 3);
      //problem = new Water("Real");
      //problem = new ZDT1("ArrayReal", 100);
      //problem = new ConstrEx("Real");
      //problem = new DTLZ1("Real");
      //problem = new OKA2("Real") ;
    } // else
    
    algorithm = new NSGAII(problem);
    //algorithm = new ssNSGAII(problem);

    // Algorithm parameters
    algorithm.setInputParameter("populationSize",pop_size); //100
    algorithm.setInputParameter("maxEvaluations",n_gen); //600
    algorithm.setInputParameter("maxPopulations",n_pop); //10

    indicators = new QualityIndicator(problem,"cs_pareto_front.pf");
    // Mutation and Crossover for Real codification 
    parameters = new HashMap() ;
    parameters.put("probability", 0.9) ;
    parameters.put("distributionIndex", 20.0) ;
    crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);                   

    parameters = new HashMap() ;
    parameters.put("probability", 1.0/problem.getNumberOfVariables()) ;
    parameters.put("distributionIndex", 60.0) ;//
    mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);                    

    // Selection Operator 
    parameters = null ;
    selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters) ;                           

    // Add the operators to the algorithm
    algorithm.addOperator("crossover",crossover);
    algorithm.addOperator("mutation",mutation);
    algorithm.addOperator("selection",selection);

    // Add the indicator object to the algorithm
    algorithm.setInputParameter("indicators", indicators) ;
    
    // Execute the Algorithm
    for (int j = 0; j < n_runs; j++) {
    	System.out.println("------------RUNS: " + j + " -------------");
    	BodyCloudOptimization.setMax_BW(BodyCloudOptimization.max_BW); //initialize the max BW
    	algorithm.setInputParameter("runs",j);
	    long initTime = System.currentTimeMillis();
	    population = algorithm.execute();
	    long estimatedTime = System.currentTimeMillis() - initTime;
	    logger_.info("Total execution time: "+estimatedTime + "ms");
	    /*
	    // Result messages 
	    logger_.info("Total execution time: "+estimatedTime + "ms");
	    logger_.info("Variables values have been writen to file VAR");
	    population.printVariablesToFile("VAR");    
	    logger_.info("Objectives values have been writen to file FUN");
	    population.printObjectivesToFile("FUN");
	  
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
	    */
    }
    
    logger_.info("Variables values have been writen to file VAR");
    population.printVariablesToFile("VAR");    
    logger_.info("Objectives values have been writen to file FUN");
    population.printObjectivesToFile("FUN");
  //  System.out.println("final pop size: " + population.size());
    for(int i=0;i<pop_size;i++){
		popaux = new SolutionSet(1);
		popaux.add(population.get(i));	
		sumup+=indicators.getHypervolume(popaux);
		writeResultToFile(indicators.getHypervolume(popaux)+"","LastHV_NSGAII.txt");
	}
    sumup=sumup/pop_size;
    System.out.println("AVG HV is: " + sumup);
    
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
	  	  
	  	writeResultToFile(st1+" "+st2+" "+st3,"OBJS_NSGAII.txt"); 
    	writeResultToFile((double)finalHV[i]+"","HV_NSGAII.txt");
    	 	
    }

  } //main
  
  public static void writeResultToFile(String st, String filename) {
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
} // NSGAII_main
