//  NSGAII.java
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

package jmetal.problems.OptMolCom;

import java.io.BufferedWriter;
import java.io.FileWriter;

import jmetal.core.*;
import jmetal.util.comparators.CrowdingComparator;
import jmetal.util.comparators.SolutionComparator;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.*;

/** 
 *  Implementation of NSGA-II.
 *  This implementation of NSGA-II makes use of a QualityIndicator object
 *  to obtained the convergence speed of the algorithm. This version is used
 *  in the paper:
 *     A.J. Nebro, J.J. Durillo, C.A. Coello Coello, F. Luna, E. Alba 
 *     "A Study of Convergence Speed in Multi-Objective Metaheuristics." 
 *     To be presented in: PPSN'08. Dortmund. September 2008.
 */

public class NSGAII extends Algorithm {
	public static int Pop_Index;
	/**
	 * Constructor
	 * @param problem Problem to solve
	 */
	public NSGAII(Problem problem) {
		super (problem) ;
	} // NSGAII

	/**   
	 * Runs the NSGA-II algorithm.
	 * @return a <code>SolutionSet</code> that is a set of non dominated solutions
	 * as a result of the algorithm execution
	 * @throws JMException 
	 */
	public SolutionSet execute() throws JMException, ClassNotFoundException {
		int populationSize;
		int maxEvaluations;
		int maxPopulations;
		int evaluations;
		int runs;
		SolutionSet populationList[];
		double HVlist[];
		double HV;
		QualityIndicator indicators; // QualityIndicator object
		int requiredEvaluations; // Use in the example of use of the
		// indicators object (see below)

		SolutionSet population;
		SolutionSet offspringPopulation;
		SolutionSet union;
		SolutionSet populationFinal = null;

		Operator mutationOperator;
		Operator crossoverOperator;
		Operator selectionOperator;

		Distance distance = new Distance();

		//Read the parameters
		populationSize = ((Integer) getInputParameter("populationSize")).intValue();
		maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
		maxPopulations = ((Integer) getInputParameter("maxPopulations")).intValue();
		runs = ((Integer) getInputParameter("runs")).intValue();
		indicators = (QualityIndicator) getInputParameter("indicators");

		//Initialize the variables
		population = new SolutionSet(populationSize);
		evaluations = 0;
		populationList = new SolutionSet[maxPopulations];
		requiredEvaluations = 0;
	
		//Read the operators
		mutationOperator = operators_.get("mutation");
		crossoverOperator = operators_.get("crossover");
		selectionOperator = operators_.get("selection");

		// Create the initial solutionSet
		Solution newSolution;
		for (int j = 0; j < maxPopulations; j++) {
			populationList[j]= new SolutionSet(populationSize);
			Pop_Index = j;
			for (int i = 0; i < populationSize; i++) {
				//System.out.println("sol: " + i);
				newSolution = new Solution(problem_);
				problem_.evaluate(newSolution);
				problem_.evaluateConstraints(newSolution);
				//evaluations++;
				//population.add(newSolution);
				populationList[j].add(newSolution);
				//AuxPopulation[j].add(newSolution);
			} //for   
	   }       

		// Generations 
		while (evaluations < maxEvaluations) {
			HVlist = new double[maxPopulations];
			HV = 0;
			populationFinal = new SolutionSet(populationSize);
			for (int j = 0; j < maxPopulations; j++) {
			Pop_Index = j;
			// Create the offSpring solutionSet      
			offspringPopulation = new SolutionSet(populationSize);
			Solution[] parents = new Solution[2];
			for (int i = 0; i < (populationSize / 2); i++) {
				//if (evaluations < maxEvaluations) {
					//obtain parents
					parents[0] = (Solution) selectionOperator.execute(populationList[j]);
					parents[1] = (Solution) selectionOperator.execute(populationList[j]);
					Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
					mutationOperator.execute(offSpring[0]);
					mutationOperator.execute(offSpring[1]);
					
					problem_.evaluate(offSpring[0]);
					problem_.evaluateConstraints(offSpring[0]);
					
					problem_.evaluate(offSpring[1]);
					problem_.evaluateConstraints(offSpring[1]);
					offspringPopulation.add(offSpring[0]);
					offspringPopulation.add(offSpring[1]);
				//	evaluations += 2;
				//}                         
			} // for
			
			// Create the solutionSet union of solutionSet and offSpring
			union = ((SolutionSet)populationList[j]).union(offspringPopulation);
			
			// Ranking the union
			Ranking ranking = new Ranking(union);

			int remain = populationSize;
			int index = 0;
			SolutionSet front = null;
			populationList[j].clear();

			// Obtain the next front
			front = ranking.getSubfront(index);

			while ((remain > 0) && (remain >= front.size())) {
				//Assign crowding distance to individuals
				distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
				//Add the individuals of this front
				for (int k = 0; k < front.size(); k++) {
					populationList[j].add(front.get(k));
				} // for

				//Decrement remain
				remain = remain - front.size();

				//Obtain the next front
				index++;
				if (remain > 0) {
					front = ranking.getSubfront(index);
				} // if        
			} // while

			// Remain is less than front(index).size, insert only the best one
			if (remain > 0) {  // front contains individuals to insert                        
				distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
				front.sort(new CrowdingComparator());
				for (int k = 0; k < remain; k++) {
					populationList[j].add(front.get(k));
				} // for

				remain = 0;
			} // if                               

				HVlist[j]=indicators.getHypervolume(populationList[j]);
				/*
				for (int k = 0; k < populationList[j].size(); k++) {
					Solution ss = populationList[j].get(k);
					System.out.println("solution " + k + ": " + ss.toString());
				}
				SolutionSet sss = new SolutionSet(populationList[j]);
				System.out.println("HV: " + indicators.getHypervolume(sss));
				*/
				HV+=HVlist[j];
				populationFinal = populationList[j];
			} //for each population
						
			
				  HV = HV/maxPopulations;
		    	  NSGAII_main.hv[runs][evaluations]=HV;
		    	  System.out.println(evaluations + ": " + (double)HV);
		    	  
		    	  //Get average objective value, best objective value
		    	  
		    	  double [] avgs=new double[problem_.getNumberOfObjectives()];
		    	  for(int i=0;i<problem_.getNumberOfObjectives();i++){
		    		  double sum=0.0;
			    	  for(int k=0;k<populationFinal.size();k++){
		    			  double val=populationFinal.get(k).getObjective(i);
		    			 // System.out.println("val: " + val);
		    			  sum+=val;	  
		    		  }			    	  
		    		  avgs[i]=sum/populationFinal.size();
		    		  NSGAII_main.finalResults[runs][evaluations][i]=avgs[i];
		    		//  System.out.println(evaluations + ": " + "obj" + i + "=>" + avgs[i] + " ");
		    		  
		    	  }
		    	//  System.out.println("HV: " + indicators.getHypervolume(populationFinal));
		    
	    	  evaluations ++;
					
		} // while
		
		// Return as output parameter the required evaluations
		setOutputParameter("evaluations", requiredEvaluations);
		
		// Return the first non-dominated front
		Ranking ranking = new Ranking(populationFinal);
		//return ranking.getSubfront(0);
		return populationFinal;
	} // execute	
	
} // NSGA-II
