//  NNEGT.java
//
//  Author:
//       Yi Ren <yiren001@cs.umb.edu>

package jmetal.problems.OptMolCom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.comparators.SolutionComparator;

public class NNEGT extends Algorithm {
	private static int op;
	private Solution[] players;
	private Solution winner;
	private Solution winnerAux;
	private Solution mutated;
	private static int p1Pos;
	private static int p2Pos;
	private static double BWCon;
	private QualityIndicator indicator;
	private SolutionSet populationAUXP1;
	private SolutionSet populationAUXP2;
	public static int Pop_Index;
	private static int winPlayer = 0;
	private static int LSindex = 20; // # interation for local search

	public NNEGT(Problem problem) {
		super (problem) ;
	} // EGT
	
	public static int getPI(){
		return Pop_Index;
	}
	
	public static void setp1Pos(int i){
		p1Pos = i;
	}
	public static void setp2Pos(int i){
		p2Pos = i;
	}
	
	public SolutionSet execute() throws JMException, ClassNotFoundException {
		int populationSize;
		int maxEvaluations;
		int maxPopulations;
		int evaluations;
		int runs;
		double HVlist[];
		double HV;
		QualityIndicator indicators; 
		int requiredEvaluations; 

		
		SolutionSet populationList[];
		SolutionSet AuxPopulationList[];
		SolutionSet popaux = new SolutionSet(1);
		
		NNManager nnm = NNManager.getInstance();
		int NNGen = NNEGT_main.n_gen; // /2

		Operator mutationOperator;
		Operator selectionOperator;

		//Read the parameters
		populationSize = ((Integer) getInputParameter("populationSize")).intValue();
		maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
		maxPopulations = ((Integer) getInputParameter("maxPopulations")).intValue();
		runs = ((Integer) getInputParameter("runs")).intValue();
		op = ((Integer) getInputParameter("combObjOp")).intValue();
		indicators = (QualityIndicator) getInputParameter("indicators");

		//Initialize the variables
		populationList = new SolutionSet[maxPopulations];
		AuxPopulationList = new SolutionSet[maxPopulations];
		SolutionSet populationFinal= new SolutionSet(maxPopulations);
		
		evaluations = 0;
		requiredEvaluations = 0;

		//Read the operators
		mutationOperator = operators_.get("Polymutation");
		selectionOperator = operators_.get("selection");

		// Create the initial solutionSet
		Solution newSolution;
		for (int j = 0; j < maxPopulations; j++) {
			populationList[j]= new SolutionSet(populationSize);
			Pop_Index = j;
			for (int i = 0; i < populationSize; i++) {
				newSolution = new Solution(problem_);
				problem_.evaluate(newSolution);
				//problem_.evaluateConstraints(newSolution);
				nnm.AddToDataset(newSolution);
				populationList[j].add(newSolution);

			} //for   
	   }
		
	  // nnm.TrainNetworks();
	   
		// Generations 
		while (evaluations < maxEvaluations) {
		//	System.out.println(">>>>> GEN: " + evaluations + " <<<<<");
			BWCon = 0;
			HVlist = new double[maxPopulations];
			HV = 0;
			populationFinal = new SolutionSet(maxPopulations);
			
			for (int j = 0; j < maxPopulations; j++) {			
				
			Pop_Index = j;

			players = new Solution[2];
			winner = new Solution();
			winnerAux = new Solution();
		
			if (evaluations == NNGen){
				nnm.TrainNetworks();
			}
			
			AuxPopulationList[j] =new SolutionSet(populationList[j]);
			
			for (int i = 0; i < (populationSize / 2); i++) {
				
					//obtain players
					players = (Solution[]) selectionOperator.execute(AuxPopulationList[j]);				
					populationAUXP1 = new SolutionSet(populationList[j]);
					populationAUXP2 = new SolutionSet(populationList[j]);
		
					winner = playGame(players);
					mutated = new Solution(winner);
					if (PseudoRandom.randDouble() <= NNEGT_main.mutate_prob){
						mutationOperator.execute(mutated);
						
						if(evaluations > NNGen){
							double predictRTT = nnm.PredictRTT(mutated);
							mutated.setObjective(0, predictRTT);
							mutated.setObjective(1, predictRTT);
						}else{
							problem_.evaluate(mutated);
							//problem_.evaluateConstraints(mutated);
							nnm.AddToDataset(mutated);					
						}
					}
					

					double auxObj = 0;
					if(winPlayer==1){
						auxObj = players[1].getObjective(0);
					}else{
						auxObj = players[0].getObjective(0);
					}
	
					for(int i1=0;i1<populationList[j].size();i1++){
						if(populationList[j].get(i1).getObjective(0)==auxObj){
							populationList[j].replace(i1,mutated);
							break;
						}
					}

					
					if (p1Pos>p2Pos){AuxPopulationList[j].remove(p1Pos);AuxPopulationList[j].remove(p2Pos);}
					if (p1Pos<p2Pos){AuxPopulationList[j].remove(p2Pos);AuxPopulationList[j].remove(p1Pos);}			
					if (p1Pos==p2Pos){AuxPopulationList[j].remove(p1Pos);}
		                
			} // for				
					
					Solution solutionaux = new Solution();
				//	System.out.println("before===================================");
				//	printPop(populationList[j]);
					populationList[j].sort(new CustomComparator());
				//	System.out.println("after===================================");
				//	printPop(populationList[j]);
					solutionaux = getDS(populationList[j]);

					popaux = new SolutionSet(1);		
					popaux.add(solutionaux);	
					populationFinal.add(solutionaux);					
					
					HVlist[j]=indicators.getHypervolume(popaux);
					HV+=HVlist[j];
					
			} //end for all population 	  
				  
		    	  HV = HV/maxPopulations;
		    	  NNEGT_main.hv[runs][evaluations]=HV;
		    	  System.out.println(evaluations + ": " + (double)HV);
		    	  
		    	  double [] avgs=new double[problem_.getNumberOfObjectives()];
		    	  for(int i=0;i<problem_.getNumberOfObjectives();i++){
		    		  double sum=0.0;
			    	  for(int k=0;k<populationFinal.size();k++){
		    			  double val=populationFinal.get(k).getObjective(i);
		    			  sum=sum+val;
		    			  
		    		  }
		    		  avgs[i]=sum/populationFinal.size();
		    		  NNEGT_main.finalResults[runs][evaluations][i]=avgs[i];
		    		 
		    	  }

			evaluations++;   
		} // while END GEN

		// Return as output parameter the required evaluations
		setOutputParameter("evaluations", requiredEvaluations);

		return populationFinal;
	} // execute
	
	
	private Solution getDS(SolutionSet pop) {
		
		pop.sort(new CustomComparator());
		int pop_size = pop.size();
		int max = 1;
		int count = 1;
		/*Solution res = pop.get(0);
		Solution aux = pop.get(0);
		for(int i=1; i<pop_size; i++){
			if(pop.get(i).getObjective(0)==aux.getObjective(0)){
				count++;
			}else{
				count=1;
				aux = pop.get(i);
			}
			if(count>max){
				max=count;
				res=pop.get(i);
			}
		}
		System.out.println("BEST: " + res.getObjective(0) + " TIMES: " + max);*/
		SolutionSet res = new SolutionSet(1);
		SolutionSet aux = new SolutionSet(1);
		SolutionSet temp = new SolutionSet(1);
		res.add(pop.get(0));
		aux.add(pop.get(0));
		temp.add(pop.get(0));
		indicator = (QualityIndicator) getInputParameter("indicators");
		for(int i=1; i<pop_size; i++){
			temp.replace(0, pop.get(i));
			if(indicator.getHypervolume(temp)==indicator.getHypervolume(aux)){
				count++;
			}else{
				count=1;
				aux.replace(0, pop.get(i));
			}
			if(count>max){
				max=count;
				res.replace(0, pop.get(i));
			}
		}
		System.out.println("BEST: " + indicator.getHypervolume(res) + " TIMES: " + max);
		
		return res.get(0);
	}


	public class CustomComparator implements Comparator<Solution> {
	  //  @Override
	    QualityIndicator indicators = (QualityIndicator) getInputParameter("indicators");
	    public int compare(Solution o1, Solution o2) {
	    	SolutionSet So1 = new SolutionSet(1);
	    	SolutionSet So2 = new SolutionSet(1);
	    	So1.add(o1);
	    	So2.add(o2);
	        return Integer.compare((int)(indicators.getHypervolume(So1)*100000),(int)(indicators.getHypervolume(So2)*100000));
	        //return Integer.compare((int)o1.getObjective(0),(int)o2.getObjective(0));
	    }
	}
	
	

	
	
	public Solution playGame(Solution[] players) throws JMException{

		Solution winner = startBattle(players[0],players[1]);

		return winner;
		
	}
		
	private Solution startBattle(Solution p1, Solution p2){
		Solution winner = p1;
		if( op == 0 ) winner = compareTwoObj_HV_NoCons(p1, p2);

		return winner;
	}
	
	
	public Solution compareTwoObj_HV_NoCons(Solution p1,Solution p2){

		SolutionSet pop1 = new SolutionSet(1);
		SolutionSet pop2 = new SolutionSet(1);
		pop1.add(p1);
		pop2.add(p2);
		indicator = (QualityIndicator) getInputParameter("indicators");
		//System.out.println("p1: " + indicator.getHypervolume(pop1));
		//System.out.println("p2: " + indicator.getHypervolume(pop2));
		if (indicator.getHypervolume(pop1)>=indicator.getHypervolume(pop2)){
			winPlayer = 1;
			return p1;
		}else{
			winPlayer = 2;
			return p2;
		}

//		System.out.println("p1pos: " + p1Pos + " value: " + p1.getObjective(0));
//		System.out.println("p2pos: " + p2Pos + " value: " + p2.getObjective(0));
	/*	if (p1.getObjective(0)>=p2.getObjective(0)){
			winPlayer = 2;
			return p2;
		}else{
			winPlayer = 1;
			return p1;
		}*/
	}
	
	private void printPop(SolutionSet pop){
		indicator = (QualityIndicator) getInputParameter("indicators");
		for(int i=0;i<pop.size();i++){
			SolutionSet pop1 = new SolutionSet(1);
			pop1.add(pop.get(i));
			System.out.println(i+": " + indicator.getHypervolume(pop1));
		}
	}
	
} // EGT
