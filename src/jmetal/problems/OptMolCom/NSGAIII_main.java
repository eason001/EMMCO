package jmetal.problems.OptMolCom;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.thetadea.ThetaDEA;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.ProblemFactory;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Configuration;
import jmetal.util.JMException;

public class NSGAIII_main {
	 public static Logger      logger_ ;      // Logger object
	  public static FileHandler fileHandler_ ; // FileHandler object
	public static int n_runs = 3; // 3, 20 sim runs
	  public static int n_gen = 100; // 100 generations
	  public static int n_pop = 1; // 10 patients
	  public static int n_obj = 2;
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
	public static void main(String args[]) throws JMException, ClassNotFoundException, SecurityException, IOException{
		Problem problem; // The problem to solve
		Algorithm algorithm; // The algorithm to use
		Operator crossover; // Crossover operator
		Operator mutation; // Mutation operator
		Operator selection; //Selection operator
		
		HashMap parameters; // Operator parameters
		QualityIndicator indicators ; 
		// Logger object and file to store log messages
	    logger_      = Configuration.logger_ ;
	    fileHandler_ = new FileHandler("NSGAII_main.log"); 
	    logger_.addHandler(fileHandler_) ;
	    indicators= null;
		
		Object[] params = { "Real", 9, 5};
		problem = new OptMolCom();
		
		algorithm = new NSGAIII(problem);
		

		algorithm.setInputParameter("normalize", true);
		
		
		
		algorithm.setInputParameter("div1", 6);
		algorithm.setInputParameter("div2", 3);
		
		
		algorithm.setInputParameter("maxGenerations", n_gen);
		algorithm.setInputParameter("maxPopulations",n_pop); //1
		algorithm.setInputParameter("populationSize",pop_size); //50
		
		
		indicators = new QualityIndicator(problem,"cs_pareto_front.pf");
		// Mutation and Crossover for Real codification
		parameters = new HashMap();
		parameters.put("probability", 1.0);
		parameters.put("distributionIndex", 30.0);
		crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover",
				parameters);

		parameters = new HashMap();
		parameters.put("probability", 1.0 / problem.getNumberOfVariables());
		parameters.put("distributionIndex", 20.0);
		mutation = MutationFactory.getMutationOperator("PolynomialMutation",
				parameters);

		parameters = null;
		selection = SelectionFactory.getSelectionOperator("RandomSelection",
				parameters);

		// Add the operators to the algorithm
		algorithm.addOperator("crossover", crossover);
		algorithm.addOperator("mutation", mutation);
		algorithm.addOperator("selection", selection);
		
		
		// Add the indicator object to the algorithm
	    algorithm.setInputParameter("indicators", indicators) ;
		
		
		// Execute the Algorithm
	    for (int j = 0; j < n_runs; j++) {
	    	System.out.println("------------RUNS: " + j + " -------------");
	    	
	    	algorithm.setInputParameter("runs",j);
		    long initTime = System.currentTimeMillis();
		    population = algorithm.execute();
		    long estimatedTime = System.currentTimeMillis() - initTime;
		    
		    // Result messages 
		    logger_.info("Total execution time: "+estimatedTime + "ms");
		    logger_.info("Variables values have been writen to file VAR");
		    population.printVariablesToFile("./out/NSGAIII/VAR_NSGAIII_" + j);    
		    logger_.info("Objectives values have been writen to file FUN");
		    population.printObjectivesToFile("./out/NSGAIII/FUN_NSGAIII_" + j);
		  
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
		    
	    }
	    /*
	    logger_.info("Variables values have been writen to file VAR");
	    population.printVariablesToFile("VAR");    
	    logger_.info("Objectives values have been writen to file FUN");
	    population.printObjectivesToFile("FUN");*/
	  //  System.out.println("final pop size: " + population.size());
	    for(int i=0;i<pop_size;i++){
			popaux = new SolutionSet(1);
			popaux.add(population.get(i));	
			sumup+=indicators.getHypervolume(popaux);
			writeResultToFile(indicators.getHypervolume(popaux)+"","LastHV_NSGAIII.txt");
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
		  	  
		  	writeResultToFile(st1+" "+st2+" "+st3,"OBJS_NSGAIII.txt"); 
	    	writeResultToFile((double)finalHV[i]+"","HV_NSGAIII.txt");
	    	 	
	    }

	    System.out.println("============END===========");
	  } //main
	  
	  public static void writeResultToFile(String st, String filename) {
			try { // Create file
				FileWriter fstream = new FileWriter("out\\NSGAIII\\" +filename, true);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(st);
				out.newLine();
				out.close();
			} catch (Exception e) {// Catch exception if any
				System.err.println("Error: " + e.getMessage());
			}
		}
		/*
		SolutionSet population = algorithm.execute();
		
		population.printObjectivesToFile("FUN");*/
	
}
