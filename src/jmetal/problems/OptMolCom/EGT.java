//  EGT_main.java
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

public class EGT extends Algorithm {
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
	/**
	 * Constructor
	 * @param problem Problem to solve
	 */
	
	public static double getBWC(){
		return BWCon;
	}
	
	public static int getPI(){
		return Pop_Index;
	}
	
	public static void setp1Pos(int i){
		p1Pos = i;
	}
	public static void setp2Pos(int i){
		p2Pos = i;
	}
	
	public EGT(Problem problem) {
		super (problem) ;
	} // EGT

	/**   
	 * Runs the EGT algorithm.
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
		double HVlist[];
		double HV;
		QualityIndicator indicators; // QualityIndicator object
		int requiredEvaluations; // Use in the example of use of the
		// indicators object (see below)

		SolutionSet population;
		SolutionSet AuxPopulation;
		
		SolutionSet populationList[];
		SolutionSet AuxPopulationList[];
		SolutionSet popaux = new SolutionSet(1);
		

		Operator mutationOperator;
		Operator TabumutationOperator;
		Operator selectionOperator;

		//Read the parameters
		populationSize = ((Integer) getInputParameter("populationSize")).intValue();
		maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
		maxPopulations = ((Integer) getInputParameter("maxPopulations")).intValue();
		runs = ((Integer) getInputParameter("runs")).intValue();
		op = ((Integer) getInputParameter("combObjOp")).intValue();
		indicators = (QualityIndicator) getInputParameter("indicators");

		//Initialize the variables
		//population = new SolutionSet(populationSize);
		//AuxPopulation = new SolutionSet(populationSize);
		populationList = new SolutionSet[maxPopulations];
		AuxPopulationList = new SolutionSet[maxPopulations];
		SolutionSet populationFinal= new SolutionSet(maxPopulations);
		
		evaluations = 0;
		requiredEvaluations = 0;

		//Read the operators
		mutationOperator = operators_.get("Polymutation");
		TabumutationOperator = operators_.get("Tabumutation");
		selectionOperator = operators_.get("selection");

		// Create the initial solutionSet
		Solution newSolution;
		for (int j = 0; j < maxPopulations; j++) {
			populationList[j]= new SolutionSet(populationSize);
			Pop_Index = j;
			for (int i = 0; i < populationSize; i++) {
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
		//	System.out.println(">>>>> GEN: " + evaluations + " <<<<<");
			BWCon = 0;
			HVlist = new double[maxPopulations];
			HV = 0;
			populationFinal = new SolutionSet(maxPopulations);
			
			for (int j = 0; j < maxPopulations; j++) {			
				
			Pop_Index = j;
		//	VMDeployment.checkHOST();
			players = new Solution[2];
			winner = new Solution();
			winnerAux = new Solution();
			//AuxPopulation.copy(population);
			
			AuxPopulationList[j] =new SolutionSet(populationList[j]);
			
		//	System.out.println(populationSize);
			for (int i = 0; i < (populationSize / 2); i++) {
				
					//obtain players
					players = (Solution[]) selectionOperator.execute(AuxPopulationList[j]);				
					populationAUXP1 = new SolutionSet(populationList[j]);
					populationAUXP2 = new SolutionSet(populationList[j]);
					//System.out.println("p1pos: " + p1Pos);
					//System.out.println("p2pos: " + p2Pos);

					winner = playGame(players);
					mutated = new Solution(winner);
					if (PseudoRandom.randDouble() <= EGT_main.mutate_prob){
						mutationOperator.execute(mutated);
						problem_.evaluate(mutated);
						problem_.evaluateConstraints(mutated);
					}
					
					//System.out.println("Winner: " + winner.getObjective(0));
					//System.out.println("Mutated: " + mutated.getObjective(0));
					double auxObj = 0;
					if(winPlayer==1){
						auxObj = players[1].getObjective(0);
					}else{
						auxObj = players[0].getObjective(0);
					}
					//problem_.evaluate(winner);
					//problem_.evaluateConstraints(winner);
					/*
					//elitism
					
					if (winPlayer == 1){
						players[0]=AuxPopulationList[j].get(p1Pos);
						players[1]=mutated;
					}else{
						players[0]=AuxPopulationList[j].get(p2Pos);
						players[1]=mutated;
					}
					
					*/
					//populationAUXP1 = new SolutionSet(populationList[j]);
					//populationAUXP2 = new SolutionSet(populationList[j]);
					//winnerAux = playGame(players);
					//problem_.evaluate(winnerAux);
					//problem_.evaluateConstraints(winnerAux);
					/*System.out.println("BEFORE===================");
					for(int i1=0;i1<populationList[j].size();i1++){
						System.out.println(i1+": " + populationList[j].get(i1).getObjective(0));
					}*/
					for(int i1=0;i1<populationList[j].size();i1++){
						if(populationList[j].get(i1).getObjective(0)==auxObj){
							populationList[j].replace(i1,mutated);
							break;
						}
					}
					//populationList[j].replace(p1Pos,winner);
					//populationList[j].replace(p2Pos,mutated);
					//populationList[j].replace(p2Pos,winnerAux);
					/*System.out.println("===================AFTER");
					for(int i1=0;i1<populationList[j].size();i1++){
						System.out.println(i1+": " + populationList[j].get(i1).getObjective(0));
					}*/
					
					if (p1Pos>p2Pos){AuxPopulationList[j].remove(p1Pos);AuxPopulationList[j].remove(p2Pos);}
					if (p1Pos<p2Pos){AuxPopulationList[j].remove(p2Pos);AuxPopulationList[j].remove(p1Pos);}			
					if (p1Pos==p2Pos){AuxPopulationList[j].remove(p1Pos);}
		                
			} // for
					/*
					Iterator itr = populationList[j].iterator();
					int count =0;
					while(itr.hasNext()) {
						count++;
				         Object element = itr.next();
				         System.out.println(count + ") " + element + " ");
				     }
					*/
					populationList[j].sort(new CustomComparator());
				//	populationList[j].sort(new SolutionComparator());
					/*System.out.println("============FINAL=============");
					for(int i=0;i<populationList[j].size();i++){
						System.out.println(i+": " + populationList[j].get(i).getObjective(0));
					}*/
					
					Solution solutionaux = new Solution();
					//solutionaux = populationList[j].best(new SolutionComparator());
					solutionaux = getDS(populationList[j]);
				//	solutionaux = populationList[j].get(0);
				//	SolutionSet popaux1 = new SolutionSet(1);		
					//popaux1.add(solutionaux);	
					//System.out.println("Before: " + indicators.getHypervolume(popaux1));
				//LOCAL SEARCH TO IMPROVE DOMINANT STRATEGY
					
					//solutionaux = localSearch_mutation(solutionaux);
					
					//solutionaux = localSearch_tabu(solutionaux);
					
					//solutionaux = localSearch_tabuR(solutionaux);
					//System.out.println("BEST: " + solutionaux.getObjective(0));
					popaux = new SolutionSet(1);		
					popaux.add(solutionaux);	
					populationFinal.add(solutionaux);
					
				//	System.out.println("After: " + indicators.getHypervolume(popaux));
					
				//	VMDeployment.updateHOST(solutionaux); //UPDATE HOST
					
					HVlist[j]=indicators.getHypervolume(popaux);
					HV+=HVlist[j];
					
			} //end for all population 	  
				  
		    	  HV = HV/maxPopulations;
		    	  EGT_main.hv[runs][evaluations]=HV;
		    	  System.out.println(evaluations + ": " + (double)HV);
		    	  
		    	  double [] avgs=new double[problem_.getNumberOfObjectives()];
		    	  for(int i=0;i<problem_.getNumberOfObjectives();i++){
		    		  double sum=0.0;
			    	  for(int k=0;k<populationFinal.size();k++){
		    			  double val=populationFinal.get(k).getObjective(i);
		    			  sum=sum+val;
		    			  
		    		  }
		    		  avgs[i]=sum/populationFinal.size();
		    		  EGT_main.finalResults[runs][evaluations][i]=avgs[i];
		    		 // System.out.println("Gen " + evaluations + " objs " + i + " : "+ avgs[i]);
		    	  }
		    	  //Energy Consumption Host computed separately from objective value !!
		  //  	  double HostEN = VMDeployment.getHostEN();
		  //  	  EGT_main.finalResults[runs][evaluations][2] = HostEN;
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
		Solution res = pop.get(0);
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
		System.out.println("BEST: " + res.getObjective(0) + " TIMES: " + max);
		return res;
	}


	public class CustomComparator implements Comparator<Solution> {
	  //  @Override
	    QualityIndicator indicators = (QualityIndicator) getInputParameter("indicators");
	    public int compare(Solution o1, Solution o2) {
	    	SolutionSet So1 = new SolutionSet(1);
	    	SolutionSet So2 = new SolutionSet(1);
	    	So1.add(o1);
	    	So2.add(o2);
	        //return Integer.compare((int)indicators.getHypervolume(So1),(int)indicators.getHypervolume(So2));
	        return Integer.compare((int)o1.getObjective(0),(int)o2.getObjective(0));
	    }
	}
	
	
	public Solution localSearch_mutation(Solution solutionaux) throws JMException{
		QualityIndicator indicators = (QualityIndicator) getInputParameter("indicators");
		
		Solution aux = new Solution(solutionaux);
		
		Solution winner = new Solution();		
		Operator mutationOperator = operators_.get("Polymutation");
		for (int i = 0 ; i< LSindex ; i++){
			mutationOperator.execute(aux);
			problem_.evaluate(aux);
			problem_.evaluateConstraints(aux);
			problem_.evaluate(solutionaux);
			problem_.evaluateConstraints(solutionaux);
			SolutionSet pop1 = new SolutionSet(1);
			SolutionSet pop2 = new SolutionSet(1);
			pop1.add(aux);
			pop2.add(solutionaux);
			//System.out.println("pop1: " + indicators.getHypervolume(pop1));
			//System.out.println("pop2: " + indicators.getHypervolume(pop2));
			if (indicators.getHypervolume(pop1)<=indicators.getHypervolume(pop2)){
				winner = solutionaux;
				aux = new Solution(solutionaux);
				//System.out.println("pop2 wins! ");
			}else{
				winner = aux;
				solutionaux = new Solution(aux);
				//System.out.println("pop1 wins! ");
			}
			//winner = compareFourObj_HV(aux, solutionaux, 0, 1, 2, 3 );
			//solutionaux = winner;
		}
		
		return winner;
	}
	
	
	public Solution localSearch_tabu(Solution solutionaux) throws JMException{
		QualityIndicator indicators = (QualityIndicator) getInputParameter("indicators");
		Solution winner = new Solution();		
		Operator TabuOperator = operators_.get("Tabumutation");
		
		Solution aux = new Solution(solutionaux);
		Solution aux2 = new Solution(solutionaux);
		TabuOperator.execute(aux);
		
		
		for (int i = 0 ; i< LSindex ; i++){
			aux2 = new Solution(solutionaux);
			TabuOperator.execute(aux2);
			problem_.evaluate(aux);
			problem_.evaluateConstraints(aux);
			problem_.evaluate(aux2);
			problem_.evaluateConstraints(aux2);
			SolutionSet pop1 = new SolutionSet(1);
			SolutionSet pop2 = new SolutionSet(1);
			pop1.add(aux);
			pop2.add(aux2);
			//System.out.println("pop1: " + indicators.getHypervolume(pop1));
			//System.out.println("pop2: " + indicators.getHypervolume(pop2));
			if (indicators.getHypervolume(pop1)<=indicators.getHypervolume(pop2)){
				aux = new Solution(aux2);
			}
			//winner = compareFourObj_HV(aux, solutionaux, 0, 1, 2, 3 );
			//solutionaux = winner;
		}
		problem_.evaluate(solutionaux);
		problem_.evaluateConstraints(solutionaux);
		SolutionSet pop1 = new SolutionSet(1);
		SolutionSet pop2 = new SolutionSet(1);
		pop1.add(aux);
		pop2.add(solutionaux);
		if (indicators.getHypervolume(pop1)<=indicators.getHypervolume(pop2)){
			winner = new Solution(solutionaux);
		}else{
			winner = new Solution(aux);
		}
		return winner;
	}
	
	
	public Solution localSearch_tabuR(Solution solutionaux) throws JMException{
		QualityIndicator indicators = (QualityIndicator) getInputParameter("indicators");
		Solution winner = new Solution();		
		Operator TabuOperator = operators_.get("Tabumutation");
		
		Solution aux = new Solution(solutionaux);
		Solution aux2 = new Solution(solutionaux);
		TabuOperator.execute(aux);
		
		
		for (int i = 0 ; i< LSindex ; i++){
			aux2 = new Solution(aux);
			TabuOperator.execute(aux2);
			problem_.evaluate(aux);
			problem_.evaluateConstraints(aux);
			problem_.evaluate(aux2);
			problem_.evaluateConstraints(aux2);
			SolutionSet pop1 = new SolutionSet(1);
			SolutionSet pop2 = new SolutionSet(1);
			pop1.add(aux);
			pop2.add(aux2);
			//System.out.println("pop1: " + indicators.getHypervolume(pop1));
			//System.out.println("pop2: " + indicators.getHypervolume(pop2));
			if (indicators.getHypervolume(pop1)<=indicators.getHypervolume(pop2)){
				aux = new Solution(aux2);
			}
			//winner = compareFourObj_HV(aux, solutionaux, 0, 1, 2, 3 );
			//solutionaux = winner;
		}
		problem_.evaluate(solutionaux);
		problem_.evaluateConstraints(solutionaux);
		SolutionSet pop1 = new SolutionSet(1);
		SolutionSet pop2 = new SolutionSet(1);
		pop1.add(aux);
		pop2.add(solutionaux);
		if (indicators.getHypervolume(pop1)<=indicators.getHypervolume(pop2)){
			winner = new Solution(solutionaux);
		}else{
			winner = new Solution(aux);
		}
		return winner;
	}
	
	
	public Solution playGame(Solution[] players) throws JMException{
		/*problem_.evaluate(players[0]);
		problem_.evaluateConstraints(players[0]);
		problem_.evaluate(players[1]);
		problem_.evaluateConstraints(players[1]);*/
		Solution winner = startBattle(players[0],players[1]);
		/*
		System.out.println("P1: " + players[0].getObjective(op));
		System.out.println("P2: " + players[1].getObjective(op));
		System.out.println("Winner: " + winner.getObjective(op));*/
		return winner;
		
	}
		
	private Solution startBattle(Solution p1, Solution p2){
		Solution winner = p1;
		if( op == 0 ) winner = compareOneObj(p1, p2, 0);
		if( op == 1 ) winner = compareOneObj(p1, p2, 1);
		if( op == 2 ) winner = compareOneObj(p1, p2, 2);
		if( op == 3 ) winner = compareOneObj(p1, p2, 3);
	
		if( op == 4) winner = compareFourObj_Pareto(p1, p2, 0, 1, 2, 3 );
		if( op == 5) winner = compareFourObj_HV(p1, p2, 0, 1, 2, 3 );
		if( op == 6) winner = compareFourObj_HVPareto(p1, p2, 0, 1, 2, 3 );
		if( op == 7) winner = compareFourObj_HVC(p1, p2, 0, 1, 2, 3 );
		if( op == 8) winner = compareFourObj_MAXMIN(p1, p2, 0, 1, 2, 3 );
		if( op == 9) winner = compareFourObj_MAXMINPareto(p1, p2, 0, 1, 2, 3 );
		
		if( op == 10 ) winner = compareTwoObj_HV_NoCons(p1, p2);
		/*if( op == 6 ) winner = compareTwoObj(p1, p2, 1, 2 );
		if( op == 7) winner = compareTwoObj(p1, p2, 1, 3 );
		if( op == 8) winner = compareTwoObj(p1, p2, 2, 3 );

		if( op == 9) winner = compareThreeObj(p1, p2, 0, 1, 2 );
		if( op == 10) winner = compareThreeObj(p1, p2, 0, 1, 3 );
		if( op == 11) winner = compareThreeObj(p1, p2, 0, 2, 3 );
		if( op == 12) winner = compareThreeObj(p1, p2, 1, 2, 3 );

		if( op == 13) winner = compareFourObj(p1, p2, 0, 1, 2, 3 );
		*/
		//System.out.println("WinnerFFFFFFFF: " + winner.getObjective()[op]);
		return winner;
	}
	
	public static Solution compareOneObj(Solution p1,Solution p2,int x0){
		if (p1.getConstraint(x0)==0&&p2.getConstraint(x0)==0){
			if (p1.getObjective(x0)<=p2.getObjective(x0)){
			//	System.out.println("lvl1 player1 is winner: " + p1.getObjective(x0));
				return p1;
			}else{
			//	System.out.println("lvl1 player2 is winner: " + p2.getObjective(x0));
				return p2;	
			}
			}else if (p1.getConstraint(x0)==0&&p2.getConstraint(x0)!=0){
			//	System.out.println("p1 violation: " + p1.getConstraint(x0));
			//	System.out.println("p2 violation: " + p2.getConstraint(x0));
			//	System.out.println("lvl2 player1 is winner");
				return p1;
			}else if (p1.getConstraint(x0)!=0&&p2.getConstraint(x0)==0){
			//	System.out.println("p1 violation: " + p1.getConstraint(x0));
			//	System.out.println("p2 violation: " + p2.getConstraint(x0));
			//	System.out.println("lvl2 player2 is winner");
				return p2;
			}else if (p1.getConstraint(x0)!=0&&p2.getConstraint(x0)!=0){
			//	System.out.println("p1 violation: " + p1.getConstraint(x0));
			//	System.out.println("p2 violation: " + p2.getConstraint(x0));
				if (p1.getConstraint(x0)<=p2.getConstraint(x0)){
					
			//		System.out.println("lvl3 player1 is winner");
					return p1;
				}else{
			//		System.out.println("lvl3 player2 is winner");
					return p2;	
				}
			}else{
			//	System.out.println("lvl?? player1 is winner");
				return p1;
				}
	}
	
	public static Solution compareTwoObj(Solution p1,Solution p2,int x0,int x1){
		int k1=0;
		int k2=0;

		if (p1.getConstraint(x0)==0&&p1.getConstraint(x1)==0&&p2.getConstraint(x0)==0&&p2.getConstraint(x1)==0){
			
			if (p1.getObjective(x0)==p2.getObjective(x0)&&p1.getObjective(x1)==p2.getObjective(x1)){
				winPlayer = 1;
				return p1;
			}else{
				if (p1.getObjective(x0)<p2.getObjective(x0)){
					k1++;
				}
				if (p1.getObjective(x1)<p2.getObjective(x1)){
					k1++;
				}
				
				if (p1.getObjective(x0)>p2.getObjective(x0)){
					k2++;
				}
				if (p1.getObjective(x1)>p2.getObjective(x1)){
					k2++;
				}
			}
	
			if (k2!=0&&k1==0){winPlayer = 2;return p2;}else{winPlayer = 1;return p1;}
		
		}else if ((p1.getConstraint(x0)==0&&p1.getConstraint(x1)==0)&&(p2.getConstraint(x0)!=0||p2.getConstraint(x1)!=0)){
			//System.out.println("lvl2 player1 is winner");
			winPlayer = 1;
			return p1;
		}else if ((p1.getConstraint(x0)!=0||p1.getConstraint(x1)!=0)&&(p2.getConstraint(x0)==0&&p2.getConstraint(x1)==0)){
			//System.out.println("lvl2 player2 is winner");
			winPlayer = 2;
			return p2;
		}else if ((p1.getConstraint(x0)!=0||p1.getConstraint(x1)!=0)&&(p2.getConstraint(x0)!=0||p2.getConstraint(x1)!=0)){
			if (p1.getConstraint(x0)+ p1.getConstraint(x1)<=p2.getConstraint(x0)+p2.getConstraint(x1)){
				//System.out.println("lvl3 player1 is winner");
				winPlayer = 1;
				return p1;
			}else{
				//System.out.println("lvl3 player2 is winner");
				winPlayer = 2;
				return p2;	
			}
		}else{
			//System.out.println("lvl?? player1 is winner");
			winPlayer = 1;
			return p1;
			}
	}
	
	
	public Solution compareTwoObj_HV_NoCons(Solution p1,Solution p2){
/*
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
*/
//		System.out.println("p1pos: " + p1Pos + " value: " + p1.getObjective(0));
//		System.out.println("p2pos: " + p2Pos + " value: " + p2.getObjective(0));
		if (p1.getObjective(0)>=p2.getObjective(0)){
			winPlayer = 2;
			return p2;
		}else{
			winPlayer = 1;
			return p1;
		}
	}
	
	public static Solution compareThreeObj(Solution p1,Solution p2,int x0,int x1,int x2){
		int k1=0;
		int k2=0;
		
		if (p1.getConstraint(x0)==0&&p1.getConstraint(x1)==0&&p1.getConstraint(x2)==0&&p2.getConstraint(x0)==0&&p2.getConstraint(x1)==0&&p2.getConstraint(x2)==0){
			
			if (p1.getObjective(x0)==p2.getObjective(x0)&&p1.getObjective(x1)==p2.getObjective(x1)&&p1.getObjective(x2)==p2.getObjective(x2)){
				winPlayer = 1;
				return p1;
			}else{
				if (p1.getObjective(x0)<p2.getObjective(x0)){
					k1++;
				}
				if (p1.getObjective(x1)<p2.getObjective(x1)){
					k1++;
				}
				if (p1.getObjective(x2)<p2.getObjective(x2)){
					k1++;
				}
				
				if (p1.getObjective(x0)>p2.getObjective(x0)){
					k2++;
				}
				if (p1.getObjective(x1)>p2.getObjective(x1)){
					k2++;
				}
				if (p1.getObjective(x2)>p2.getObjective(x2)){
					k2++;
				}
			}
			
			if (k2!=0&&k1==0){winPlayer = 2;return p2;}else{winPlayer = 1;return p1;}
		
		}else if ((p1.getConstraint(x0)!=0||p1.getConstraint(x1)!=0||p1.getConstraint(x2)!=0)&&(p2.getConstraint(x0)==0&&p2.getConstraint(x1)==0&&p2.getConstraint(x2)==0)){
			//System.out.println("lvl2 player1 is winner");
			winPlayer = 2;
			return p2;
		}else if ((p1.getConstraint(x0)==0&&p1.getConstraint(x1)==0&&p1.getConstraint(x2)==0)&&(p2.getConstraint(x0)!=0||p2.getConstraint(x1)!=0||p2.getConstraint(x2)!=0)){
			//System.out.println("lvl2 player2 is winner");
			winPlayer = 1;
			return p1;
		}else if ((p1.getConstraint(x0)!=0||p1.getConstraint(x1)!=0||p1.getConstraint(x2)!=0)&&(p2.getConstraint(x0)!=0||p2.getConstraint(x1)!=0||p2.getConstraint(x2)==0)){
			if (p1.getConstraint(x0)+ p1.getConstraint(x1)+ p1.getConstraint(x2)<=p2.getConstraint(x0)+p2.getConstraint(x1)+ p2.getConstraint(x2)){
				//System.out.println("lvl3 player1 is winner");
				winPlayer = 1;
				return p1;
			}else{
				//System.out.println("lvl3 player2 is winner");
				winPlayer = 2;
				return p2;	
			}
		}else{
			//System.out.println("lvl?? player1 is winner");
			winPlayer = 1;
			return p1;
			}
	}
	
	public static Solution compareFourObj_Pareto(Solution p1,Solution p2,int x0,int x1,int x2,int x3){
		int k1=0;
		int k2=0;
		if (p1.getConstraint(x0)==0&&p1.getConstraint(x1)==0&&p1.getConstraint(x2)==0&&p1.getConstraint(x3)==0&&p2.getConstraint(x0)==0&&p2.getConstraint(x1)==0&&p2.getConstraint(x2)==0&&p2.getConstraint(x3)==0){
			if (p1.getObjective(x0)==p2.getObjective(x0)&&p1.getObjective(x1)==p2.getObjective(x1)&&p1.getObjective(x2)==p2.getObjective(x2)&&p1.getObjective(x3)==p2.getObjective(x3)){
				winPlayer = 1;
				return p1;
			}else{
				if (p1.getObjective(x0)<p2.getObjective(x0)){
					k1++;
				}
				if (p1.getObjective(x1)<p2.getObjective(x1)){
					k1++;
				}
				if (p1.getObjective(x2)<p2.getObjective(x2)){
					k1++;
				}
				if (p1.getObjective(x3)<p2.getObjective(x3)){
					k1++;
				}
				
				if (p1.getObjective(x0)>p2.getObjective(x0)){
					k2++;
				}
				if (p1.getObjective(x1)>p2.getObjective(x1)){
					k2++;
				}
				if (p1.getObjective(x2)>p2.getObjective(x2)){
					k2++;
				}
				if (p1.getObjective(x3)>p2.getObjective(x3)){
					k2++;
				}
			}
	
			if (k2!=0&&k1==0){
				//System.out.println("lvl1 player2 is winner"); 
				winPlayer = 2;
				return p2;
				}else{
					//System.out.println("lvl1 player1 is winner"); 
					winPlayer = 1;
					return p1;
					}
			
		}else if ((p1.getConstraint(x0)==0&&p1.getConstraint(x1)==0&&p1.getConstraint(x2)==0&&p1.getConstraint(x3)==0)&&(p2.getConstraint(x0)!=0||p2.getConstraint(x1)!=0||p2.getConstraint(x2)!=0||p2.getConstraint(x3)!=0)){
			//System.out.println("lvl2 player1 is winner");
			winPlayer = 1;
			return p1;
		}else if ((p1.getConstraint(x0)!=0||p1.getConstraint(x1)!=0||p1.getConstraint(x2)!=0||p1.getConstraint(x3)!=0)&&(p2.getConstraint(x0)==0&&p2.getConstraint(x1)==0&&p2.getConstraint(x2)==0&&p2.getConstraint(x3)==0)){
			//System.out.println("lvl2 player2 is winner");
			winPlayer = 2;
			return p2;
		}else if ((p1.getConstraint(x0)!=0||p1.getConstraint(x1)!=0||p1.getConstraint(x2)!=0||p1.getConstraint(x3)!=0)&&(p2.getConstraint(x0)!=0||p2.getConstraint(x1)!=0||p2.getConstraint(x2)!=0||p2.getConstraint(x3)!=0)){
			
			/*for (int q=0;q<n_objectives;q++){
				System.out.println("player1 violation " + q + ": " + p1.getViolation()[q]);
				System.out.println("player2 violation " + q + ": " + p2.getViolation()[q]);
			}*/
			if (p1.getConstraint(x0)+ p1.getConstraint(x1)+ p1.getConstraint(x2)+ p1.getConstraint(x3)<=p2.getConstraint(x0)+p2.getConstraint(x1)+ p2.getConstraint(x2)+ p1.getConstraint(x3)){
				//System.out.println("lvl3 player1 is winner");
				winPlayer = 1;
				return p1;		
			}else{
				//System.out.println("lvl3 player2 is winner");
				winPlayer = 2;
				return p2;	
			}
		}else{
			//System.out.println("lvl?? player1 is winner");
			winPlayer = 1;
			return p1;
			}

	}
	
	public Solution compareFourObj_HV(Solution p1,Solution p2,int x0,int x1,int x2,int x3){

		SolutionSet pop1 = new SolutionSet(1);
		SolutionSet pop2 = new SolutionSet(1);
		pop1.add(p1);
		pop2.add(p2);
		indicator = (QualityIndicator) getInputParameter("indicators");
		
		if (p1.getConstraint(x0)==0&&p1.getConstraint(x1)==0&&p1.getConstraint(x2)==0&&p1.getConstraint(x3)==0&&p2.getConstraint(x0)==0&&p2.getConstraint(x1)==0&&p2.getConstraint(x2)==0&&p2.getConstraint(x3)==0){
			if (indicator.getHypervolume(pop1)==indicator.getHypervolume(pop2)){
				winPlayer = 1;
				return p1;
			}else{
				if (indicator.getHypervolume(pop1)>=indicator.getHypervolume(pop2)){
					winPlayer = 1;
					return p1;
				}else{
					winPlayer = 2;
					return p2;
				}
			}
	
		}else if ((p1.getConstraint(x0)==0&&p1.getConstraint(x1)==0&&p1.getConstraint(x2)==0&&p1.getConstraint(x3)==0)&&(p2.getConstraint(x0)!=0||p2.getConstraint(x1)!=0||p2.getConstraint(x2)!=0||p2.getConstraint(x3)!=0)){
			//System.out.println("lvl2 player1 is winner");
			winPlayer = 1;
			return p1;
		}else if ((p1.getConstraint(x0)!=0||p1.getConstraint(x1)!=0||p1.getConstraint(x2)!=0||p1.getConstraint(x3)!=0)&&(p2.getConstraint(x0)==0&&p2.getConstraint(x1)==0&&p2.getConstraint(x2)==0&&p2.getConstraint(x3)==0)){
			//System.out.println("lvl2 player2 is winner");
			winPlayer = 2;
			return p2;
		}else if ((p1.getConstraint(x0)!=0||p1.getConstraint(x1)!=0||p1.getConstraint(x2)!=0||p1.getConstraint(x3)!=0)&&(p2.getConstraint(x0)!=0||p2.getConstraint(x1)!=0||p2.getConstraint(x2)!=0||p2.getConstraint(x3)!=0)){
			
			/*for (int q=0;q<n_objectives;q++){
				System.out.println("player1 violation " + q + ": " + p1.getViolation()[q]);
				System.out.println("player2 violation " + q + ": " + p2.getViolation()[q]);
			}*/
			if (p1.getConstraint(x0)+ p1.getConstraint(x1)+ p1.getConstraint(x2)+ p1.getConstraint(x3)<=p2.getConstraint(x0)+p2.getConstraint(x1)+ p2.getConstraint(x2)+ p1.getConstraint(x3)){
				//System.out.println("lvl3 player1 is winner");
				winPlayer = 1;
				return p1;
			}else{
				//System.out.println("lvl3 player2 is winner");
				winPlayer = 2;
				return p2;	
			}
		}else{
			//System.out.println("lvl?? player1 is winner");
			winPlayer = 1;
			return p1;
			}

	}
	
	public Solution compareFourObj_HVPareto(Solution p1,Solution p2,int x0,int x1,int x2,int x3){
		int k1=0;
		int k2=0;
		SolutionSet pop1 = new SolutionSet(1);
		SolutionSet pop2 = new SolutionSet(1);
		pop1.add(p1);
		pop2.add(p2);
		indicator = (QualityIndicator) getInputParameter("indicators");
		if (p1.getConstraint(x0)==0&&p1.getConstraint(x1)==0&&p1.getConstraint(x2)==0&&p1.getConstraint(x3)==0&&p2.getConstraint(x0)==0&&p2.getConstraint(x1)==0&&p2.getConstraint(x2)==0&&p2.getConstraint(x3)==0){
			if (p1.getObjective(x0)==p2.getObjective(x0)&&p1.getObjective(x1)==p2.getObjective(x1)&&p1.getObjective(x2)==p2.getObjective(x2)&&p1.getObjective(x3)==p2.getObjective(x3)){
				if (indicator.getHypervolume(pop1)==indicator.getHypervolume(pop2)){
					winPlayer = 1;
					return p1;
				}else{
					if (indicator.getHypervolume(pop1)>=indicator.getHypervolume(pop2)){
						winPlayer = 1;
						return p1;
					}else{
						winPlayer = 2;
						return p2;
					}
				}
			}else{
				if (p1.getObjective(x0)<p2.getObjective(x0)){
					k1++;
				}
				if (p1.getObjective(x1)<p2.getObjective(x1)){
					k1++;
				}
				if (p1.getObjective(x2)<p2.getObjective(x2)){
					k1++;
				}
				if (p1.getObjective(x3)<p2.getObjective(x3)){
					k1++;
				}
				
				if (p1.getObjective(x0)>p2.getObjective(x0)){
					k2++;
				}
				if (p1.getObjective(x1)>p2.getObjective(x1)){
					k2++;
				}
				if (p1.getObjective(x2)>p2.getObjective(x2)){
					k2++;
				}
				if (p1.getObjective(x3)>p2.getObjective(x3)){
					k2++;
				}
			}
	
			if (k2!=0&&k1==0){
				//System.out.println("lvl1 player2 is winner"); 
				winPlayer = 2;
				return p2;
				}else{
					//System.out.println("lvl1 player1 is winner"); 
					winPlayer = 1;
					return p1;
					}
			
		}else if ((p1.getConstraint(x0)==0&&p1.getConstraint(x1)==0&&p1.getConstraint(x2)==0&&p1.getConstraint(x3)==0)&&(p2.getConstraint(x0)!=0||p2.getConstraint(x1)!=0||p2.getConstraint(x2)!=0||p2.getConstraint(x3)!=0)){
			//System.out.println("lvl2 player1 is winner");
			winPlayer = 1;
			return p1;
		}else if ((p1.getConstraint(x0)!=0||p1.getConstraint(x1)!=0||p1.getConstraint(x2)!=0||p1.getConstraint(x3)!=0)&&(p2.getConstraint(x0)==0&&p2.getConstraint(x1)==0&&p2.getConstraint(x2)==0&&p2.getConstraint(x3)==0)){
			//System.out.println("lvl2 player2 is winner");
			winPlayer = 2;
			return p2;
		}else if ((p1.getConstraint(x0)!=0||p1.getConstraint(x1)!=0||p1.getConstraint(x2)!=0||p1.getConstraint(x3)!=0)&&(p2.getConstraint(x0)!=0||p2.getConstraint(x1)!=0||p2.getConstraint(x2)!=0||p2.getConstraint(x3)!=0)){
			
			/*for (int q=0;q<n_objectives;q++){
				System.out.println("player1 violation " + q + ": " + p1.getViolation()[q]);
				System.out.println("player2 violation " + q + ": " + p2.getViolation()[q]);
			}*/
			if (p1.getConstraint(x0)+ p1.getConstraint(x1)+ p1.getConstraint(x2)+ p1.getConstraint(x3)<=p2.getConstraint(x0)+p2.getConstraint(x1)+ p2.getConstraint(x2)+ p1.getConstraint(x3)){
				//System.out.println("lvl3 player1 is winner");
				winPlayer = 1;
				return p1;
			}else{
				//System.out.println("lvl3 player2 is winner");
				winPlayer = 2;
				return p2;	
			}
		}else{
			//System.out.println("lvl?? player1 is winner");
			winPlayer = 1;
			return p1;
			}

	}
	
	public Solution compareFourObj_HVC(Solution p1,Solution p2,int x0,int x1,int x2,int x3){

		SolutionSet pop1 = new SolutionSet(1);
		SolutionSet pop2 = new SolutionSet(1);
		pop1.add(p1);
		pop2.add(p2);
		indicator = (QualityIndicator) getInputParameter("indicators");
		double totalHV = indicator.getHypervolume(populationAUXP1);
		populationAUXP1.remove(p1Pos);
		populationAUXP2.remove(p2Pos);
		double HVC1 = totalHV - indicator.getHypervolume(populationAUXP1);
		double HVC2 = totalHV - indicator.getHypervolume(populationAUXP2);
		
		if (p1.getConstraint(x0)==0&&p1.getConstraint(x1)==0&&p1.getConstraint(x2)==0&&p1.getConstraint(x3)==0&&p2.getConstraint(x0)==0&&p2.getConstraint(x1)==0&&p2.getConstraint(x2)==0&&p2.getConstraint(x3)==0){
			
				if (HVC1==HVC2){
					winPlayer = 1;
					return p1;
				}else{
					if (HVC1>=HVC2){
						winPlayer = 1;
						return p1;
					}else{
						winPlayer = 2;
						return p2;
					}
				}
	
		}else if ((p1.getConstraint(x0)==0&&p1.getConstraint(x1)==0&&p1.getConstraint(x2)==0&&p1.getConstraint(x3)==0)&&(p2.getConstraint(x0)!=0||p2.getConstraint(x1)!=0||p2.getConstraint(x2)!=0||p2.getConstraint(x3)!=0)){
			//System.out.println("lvl2 player1 is winner");
			winPlayer = 1;
			return p1;
		}else if ((p1.getConstraint(x0)!=0||p1.getConstraint(x1)!=0||p1.getConstraint(x2)!=0||p1.getConstraint(x3)!=0)&&(p2.getConstraint(x0)==0&&p2.getConstraint(x1)==0&&p2.getConstraint(x2)==0&&p2.getConstraint(x3)==0)){
			//System.out.println("lvl2 player2 is winner");
			winPlayer = 2;
			return p2;
		}else if ((p1.getConstraint(x0)!=0||p1.getConstraint(x1)!=0||p1.getConstraint(x2)!=0||p1.getConstraint(x3)!=0)&&(p2.getConstraint(x0)!=0||p2.getConstraint(x1)!=0||p2.getConstraint(x2)!=0||p2.getConstraint(x3)!=0)){
			
			/*for (int q=0;q<n_objectives;q++){
				System.out.println("player1 violation " + q + ": " + p1.getViolation()[q]);
				System.out.println("player2 violation " + q + ": " + p2.getViolation()[q]);
			}*/
			if (p1.getConstraint(x0)+ p1.getConstraint(x1)+ p1.getConstraint(x2)+ p1.getConstraint(x3)<=p2.getConstraint(x0)+p2.getConstraint(x1)+ p2.getConstraint(x2)+ p1.getConstraint(x3)){
				//System.out.println("lvl3 player1 is winner");
				winPlayer = 1;
				return p1;
			}else{
				//System.out.println("lvl3 player2 is winner");
				winPlayer = 2;
				return p2;	
			}
		}else{
			//System.out.println("lvl?? player1 is winner");
			winPlayer = 1;
			return p1;
			}

	}
	public Solution compareFourObj_MAXMIN(Solution p1,Solution p2,int x0,int x1,int x2,int x3){
		//System.out.println("pop1 size: " + populationAUXP1.size());
		//System.out.println("pop2 size: " + populationAUXP2.size());
		populationAUXP1.remove(p1Pos);
		populationAUXP2.remove(p2Pos);
		double[] p1r =new double[populationAUXP1.size()];
		double[] p2r =new double[populationAUXP2.size()];
		double normal = 0;
		double min1 = 100;
		double max1 = 0;
		double min2 = 100;
		double max2 = 0;
		double current = 0;	
		
		if (p1.getConstraint(x0)==0&&p1.getConstraint(x1)==0&&p1.getConstraint(x2)==0&&p1.getConstraint(x3)==0&&p2.getConstraint(x0)==0&&p2.getConstraint(x1)==0&&p2.getConstraint(x2)==0&&p2.getConstraint(x3)==0){
			
			//Solution p1
			for (int i=0;i<populationAUXP1.size();i++){
				for (int j=0;j<EGT_main.n_obj;j++){
					switch (j){
					case 0: normal = 100; break;
					case 1: normal = 500000; break;
					case 2: normal = 0.001; break;
					case 3: normal = 10000; break;
					}
					current = p1.getObjective(j)*100/normal - populationAUXP1.get(i).getObjective(j)*100/normal;
					if (current <= min1){min1 = current;}
				}
				p1r[i]=min1;
			}
			for (int i=0;i<p1r.length;i++){
				if (p1r[i]>=max1){
					max1 = p1r[i];
				}
			}
			//Solution p2		
			for (int i=0;i<populationAUXP2.size();i++){
				for (int j=0;j<EGT_main.n_obj;j++){
					switch (j){
					case 0: normal = 100; break;
					case 1: normal = 500000; break;
					case 2: normal = 0.001; break;
					case 3: normal = 10000; break;
					}
					current = p2.getObjective(j)*100/normal - populationAUXP2.get(i).getObjective(j)*100/normal;
					if (current <= min2){min2 = current;}
				}
				p2r[i]=min2;
			}
			for (int i=0;i<p2r.length;i++){
				if (p2r[i]>=max2){
					max2 = p2r[i];
				}
			}
			
			if (max1<=max2){winPlayer = 1;return p1;}else{winPlayer = 2;return p2;}
	
		}else if ((p1.getConstraint(x0)==0&&p1.getConstraint(x1)==0&&p1.getConstraint(x2)==0&&p1.getConstraint(x3)==0)&&(p2.getConstraint(x0)!=0||p2.getConstraint(x1)!=0||p2.getConstraint(x2)!=0||p2.getConstraint(x3)!=0)){
			//System.out.println("lvl2 player1 is winner");
			winPlayer = 1;
			return p1;
		}else if ((p1.getConstraint(x0)!=0||p1.getConstraint(x1)!=0||p1.getConstraint(x2)!=0||p1.getConstraint(x3)!=0)&&(p2.getConstraint(x0)==0&&p2.getConstraint(x1)==0&&p2.getConstraint(x2)==0&&p2.getConstraint(x3)==0)){
			//System.out.println("lvl2 player2 is winner");
			winPlayer = 2;
			return p2;
		}else if ((p1.getConstraint(x0)!=0||p1.getConstraint(x1)!=0||p1.getConstraint(x2)!=0||p1.getConstraint(x3)!=0)&&(p2.getConstraint(x0)!=0||p2.getConstraint(x1)!=0||p2.getConstraint(x2)!=0||p2.getConstraint(x3)!=0)){
			
			/*for (int q=0;q<n_objectives;q++){
				System.out.println("player1 violation " + q + ": " + p1.getViolation()[q]);
				System.out.println("player2 violation " + q + ": " + p2.getViolation()[q]);
			}*/
			if (p1.getConstraint(x0)+ p1.getConstraint(x1)+ p1.getConstraint(x2)+ p1.getConstraint(x3)<=p2.getConstraint(x0)+p2.getConstraint(x1)+ p2.getConstraint(x2)+ p1.getConstraint(x3)){
				//System.out.println("lvl3 player1 is winner");
				winPlayer = 1;
				return p1;
			}else{
				//System.out.println("lvl3 player2 is winner");
				winPlayer = 2;
				return p2;	
			}
		}else{
			//System.out.println("lvl?? player1 is winner");
			winPlayer = 1;
			return p1;
			}

	}
	
	public Solution compareFourObj_MAXMINPareto(Solution p1,Solution p2,int x0,int x1,int x2,int x3){
		int k1=0;
		int k2=0;
		populationAUXP1.remove(p1Pos);
		populationAUXP2.remove(p2Pos);
		double[] p1r =new double[populationAUXP1.size()];
		double[] p2r =new double[populationAUXP2.size()];
		double normal = 0;
		double min1 = 100;
		double max1 = 0;
		double min2 = 100;
		double max2 = 0;
		double current = 0;	
		if (p1.getConstraint(x0)==0&&p1.getConstraint(x1)==0&&p1.getConstraint(x2)==0&&p1.getConstraint(x3)==0&&p2.getConstraint(x0)==0&&p2.getConstraint(x1)==0&&p2.getConstraint(x2)==0&&p2.getConstraint(x3)==0){
			if (p1.getObjective(x0)==p2.getObjective(x0)&&p1.getObjective(x1)==p2.getObjective(x1)&&p1.getObjective(x2)==p2.getObjective(x2)&&p1.getObjective(x3)==p2.getObjective(x3)){
				
				//Solution p1
				for (int i=0;i<populationAUXP1.size();i++){
					for (int j=0;j<EGT_main.n_obj;j++){
						switch (j){
						case 0: normal = 100; break;
						case 1: normal = 1000; break;
						case 2: normal = 3000; break;
						case 3: normal = 5000; break;
						}
						current = p1.getObjective(j)*100/normal - populationAUXP1.get(i).getObjective(j)*100/normal;
						if (current <= min1){min1 = current;}
					}
					p1r[i]=min1;
				}
				for (int i=0;i<p1r.length;i++){
					if (p1r[i]>=max1){
						max1 = p1r[i];
					}
				}
				//Solution p2		
				for (int i=0;i<populationAUXP2.size();i++){
					for (int j=0;j<EGT_main.n_obj;j++){
						switch (j){
						case 0: normal = 100; break;
						case 1: normal = 1000; break;
						case 2: normal = 3000; break;
						case 3: normal = 5000; break;
						}
						current = p2.getObjective(j)*100/normal - populationAUXP2.get(i).getObjective(j)*100/normal;
						if (current <= min2){min2 = current;}
					}
					p2r[i]=min2;
				}
				for (int i=0;i<p2r.length;i++){
					if (p2r[i]>=max2){
						max2 = p2r[i];
					}
				}
				
				if (max1<=max2){return p1;}else{return p2;}
			
			}else{
				if (p1.getObjective(x0)<p2.getObjective(x0)){
					k1++;
				}
				if (p1.getObjective(x1)<p2.getObjective(x1)){
					k1++;
				}
				if (p1.getObjective(x2)<p2.getObjective(x2)){
					k1++;
				}
				if (p1.getObjective(x3)<p2.getObjective(x3)){
					k1++;
				}
				
				if (p1.getObjective(x0)>p2.getObjective(x0)){
					k2++;
				}
				if (p1.getObjective(x1)>p2.getObjective(x1)){
					k2++;
				}
				if (p1.getObjective(x2)>p2.getObjective(x2)){
					k2++;
				}
				if (p1.getObjective(x3)>p2.getObjective(x3)){
					k2++;
				}
			}
	
			if (k2!=0&&k1==0){
				//System.out.println("lvl1 player2 is winner"); 
				winPlayer = 2;
				return p2;
				}else{
					//System.out.println("lvl1 player1 is winner"); 
					winPlayer = 1;
					return p1;
					}
			
		}else if ((p1.getConstraint(x0)==0&&p1.getConstraint(x1)==0&&p1.getConstraint(x2)==0&&p1.getConstraint(x3)==0)&&(p2.getConstraint(x0)!=0||p2.getConstraint(x1)!=0||p2.getConstraint(x2)!=0||p2.getConstraint(x3)!=0)){
			//System.out.println("lvl2 player1 is winner");
			winPlayer = 1;
			return p1;
		}else if ((p1.getConstraint(x0)!=0||p1.getConstraint(x1)!=0||p1.getConstraint(x2)!=0||p1.getConstraint(x3)!=0)&&(p2.getConstraint(x0)==0&&p2.getConstraint(x1)==0&&p2.getConstraint(x2)==0&&p2.getConstraint(x3)==0)){
			//System.out.println("lvl2 player2 is winner");
			winPlayer = 2;
			return p2;
		}else if ((p1.getConstraint(x0)!=0||p1.getConstraint(x1)!=0||p1.getConstraint(x2)!=0||p1.getConstraint(x3)!=0)&&(p2.getConstraint(x0)!=0||p2.getConstraint(x1)!=0||p2.getConstraint(x2)!=0||p2.getConstraint(x3)!=0)){
			
			/*for (int q=0;q<n_objectives;q++){
				System.out.println("player1 violation " + q + ": " + p1.getViolation()[q]);
				System.out.println("player2 violation " + q + ": " + p2.getViolation()[q]);
			}*/
			if (p1.getConstraint(x0)+ p1.getConstraint(x1)+ p1.getConstraint(x2)+ p1.getConstraint(x3)<=p2.getConstraint(x0)+p2.getConstraint(x1)+ p2.getConstraint(x2)+ p1.getConstraint(x3)){
				//System.out.println("lvl3 player1 is winner");
				winPlayer = 1;
				return p1;
			}else{
				//System.out.println("lvl3 player2 is winner");
				winPlayer = 2;
				return p2;	
			}
		}else{
			//System.out.println("lvl?? player1 is winner");
			winPlayer = 1;
			return p1;
			}

	}
	
} // EGT
