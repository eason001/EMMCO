package jmetal.problems.OptMolCom;

import jmetal.core.*;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.*;
import jmetal.util.comparators.SolutionComparator;
import jmetal.util.ranking.NondominatedRanking;
import jmetal.util.ranking.Ranking;
import jmetal.util.vector.VectorGenerator;
import jmetal.util.vector.OneLevelWeightVectorGenerator;
import jmetal.util.vector.TwoLevelWeightVectorGenerator;

public class NSGAIII extends Algorithm {

	private int populationSize_;

	private int div1_;
	private int div2_;

	private SolutionSet population_;
	SolutionSet offspringPopulation_;
	SolutionSet union_;

	int generations_;

	Operator crossover_;
	Operator mutation_;
	Operator selection_;

	double[][] lambda_; // reference points
	
	boolean normalize_; // do normalization or not

	//MULTI POPULATION
	private SolutionSet populationList[];
	private int maxPopulations;
	private int populationSize;
	
	
	
	public NSGAIII(Problem problem) {
		super(problem);
	} // NSGAII

	public SolutionSet execute() throws JMException, ClassNotFoundException {
		
		//MULTI POPULATION
		double HVlist[];
		double HV;
		SolutionSet populationFinal = null;
		SolutionSet populationFinal2 = null;
		QualityIndicator indicators;
		int requiredEvaluations; // Use in the example of use of the
		int runs;
				
		int maxGenerations_;
		
		generations_ = 0;
		
		maxGenerations_ = ((Integer) this.getInputParameter("maxGenerations")).intValue();
		maxPopulations = ((Integer) getInputParameter("maxPopulations")).intValue();
		populationSize = ((Integer) getInputParameter("populationSize")).intValue();
		indicators = (QualityIndicator) getInputParameter("indicators");
		runs = ((Integer) getInputParameter("runs")).intValue();
		
		
		div1_ = ((Integer) this.getInputParameter("div1")).intValue();

		div2_ = ((Integer) this.getInputParameter("div2")).intValue();
		
		
		normalize_ = ((Boolean) this.getInputParameter("normalize")).booleanValue();

		VectorGenerator vg = new TwoLevelWeightVectorGenerator(div1_, div2_,
				problem_.getNumberOfObjectives());
		lambda_ = vg.getVectors();
		
		
		
		
	//	System.out.println("pop size: " + vg.getVectors().length);
		populationSize_ = vg.getVectors().length;
		if (populationSize_ % 2 != 0)
			populationSize_ += 1;
		
		populationSize_ = populationSize;

		//Initialize the variables
		populationList = new SolutionSet[maxPopulations];
		populationFinal2 = new SolutionSet(populationSize);
		populationFinal = new SolutionSet(maxPopulations);
		requiredEvaluations = 0;
				
		mutation_ = operators_.get("mutation");
		crossover_ = operators_.get("crossover");
		selection_ = operators_.get("selection");

		initPopulation();

		// Generations 
		while (generations_ < maxGenerations_) {
			HVlist = new double[maxPopulations];
			HV = 0;
			populationFinal2 = new SolutionSet(populationSize);
			populationFinal = new SolutionSet(maxPopulations);
			for (int j = 0; j < maxPopulations; j++) {
			//System.out.println("pop: " + j);
			offspringPopulation_ = new SolutionSet(populationSize_);
			Solution[] parents = new Solution[2];
			for (int i = 0; i < (populationSize_ / 2); i++) {
				if (generations_ < maxGenerations_) {
					// obtain parents

					parents = (Solution[]) selection_.execute(populationList[j]);

					Solution[] offSpring = (Solution[]) crossover_
							.execute(parents);

					mutation_.execute(offSpring[0]);
					mutation_.execute(offSpring[1]);

					problem_.evaluate(offSpring[0]);
					problem_.evaluateConstraints(offSpring[0]);
					problem_.evaluate(offSpring[1]);
					problem_.evaluateConstraints(offSpring[1]);

					offspringPopulation_.add(offSpring[0]);
					offspringPopulation_.add(offSpring[1]);

				} // if
			} // for
			

			union_ = ((SolutionSet) populationList[j]).union(offspringPopulation_);

			// Ranking the union
			Ranking ranking = new NondominatedRanking(union_);
		//	System.out.println("pop size: " + populationSize_);
			int remain = populationSize_;
			int index = 0;
			SolutionSet front = null;
			//population_.clear();
			populationList[j].clear();

			// Obtain the next front
			front = ranking.getSubfront(index);

			while ((remain > 0) && (remain >= front.size())) {

				for (int k = 0; k < front.size(); k++) {
					//population_.add(front.get(k));
					populationList[j].add(front.get(k));
				} // for

				// Decrement remain
				remain = remain - front.size();

				// Obtain the next front
				index++;
				if (remain > 0) {
					front = ranking.getSubfront(index);
				} // if
			}

			if (remain > 0) { // front contains individuals to insert

				new Niching(populationList[j], front, lambda_, remain, normalize_).execute();
			//	System.out.println("remain size: " + remain + " and front size: " + front.size());
				for (int k = 0; k < front.size(); k++) {
					populationList[j].add(front.get(k));
				} // for
				remain = 0;
			}

			HVlist[j]=indicators.getHypervolume(populationList[j]);
			HV+=HVlist[j];
		
			populationFinal2 = populationList[j];
			
			Solution solutionaux = new Solution();
			solutionaux = populationList[j].best(new SolutionComparator());
			populationFinal.add(solutionaux);
			}	//for each population
			
			
			HV = HV/maxPopulations;
	    	  NSGAIII_main.hv[runs][generations_]=HV;
	    	  System.out.println(generations_ + ": " + (double)HV);
	    	  
	    	  //Get average objective value, best objective value
	    	  
	    	  double [] avgs=new double[problem_.getNumberOfObjectives()];
	    	  for(int i=0;i<problem_.getNumberOfObjectives();i++){
	    		  double sum=0.0;
		    	  for(int k=0;k<populationFinal2.size();k++){
	    			  double val=populationFinal2.get(k).getObjective(i);
	    			  sum=sum+val;
	    			  
	    		  }
	    		  avgs[i]=sum/populationFinal2.size();
	    		  NSGAIII_main.finalResults[runs][generations_][i]=avgs[i];
	    	  }
	    	  
	    	  
			generations_++;
					
						
		}

		// Return as output parameter the required evaluations
				setOutputParameter("evaluations", requiredEvaluations);
		
		//Ranking ranking = new NondominatedRanking(population_);
		//return ranking.getSubfront(0);
		Ranking ranking = new NondominatedRanking(populationFinal);

		//return populationFinal; // return the best solution of each population
		return populationFinal2; //return the last population solution set
	}

	public void initPopulation() throws JMException, ClassNotFoundException {
		for (int j = 0; j < maxPopulations; j++) {
		//	population_ = new SolutionSet(populationSize_);
		//	System.out.println("pop size: " + populationSize_);
			populationList[j]= new SolutionSet(populationSize_);
					
			for (int i = 0; i < populationSize_; i++) {
				Solution newSolution = new Solution(problem_);
	
				problem_.evaluate(newSolution);
				problem_.evaluateConstraints(newSolution);
	
				//population_.add(newSolution);
				populationList[j].add(newSolution);
			} // for
		}
	} // initPopulation


} // NSGA-III

