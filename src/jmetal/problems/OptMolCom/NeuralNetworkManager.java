package jmetal.problems.OptMolCom;

import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.util.JMException;

import org.encog.Encog;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

public class NeuralNetworkManager
{
	 private static NeuralNetworkManager instance = null;
	
	BasicNetwork minRequestNet;
	BasicNetwork maxBandwidthNet;
	BasicNetwork minDataYieldNet;
	BasicNetwork maxEnergyNet;
	
	double inputs[][];
	double targets[][];
	
	int candidateIndex;
	int numberHiddenNeurons;
	int maxEpochs;
	
	public static NeuralNetworkManager getInstance()
	{
		if (instance == null)
			instance = new NeuralNetworkManager();
	    
		return instance;
	}
	
	public static void DestroyInstance()
	{
		instance = null;
	}
	
	protected NeuralNetworkManager()
	{
		int generations = 600;
		
		// 4 candidates for each of the 600 generations
		inputs = new double[4 * generations][37];
		targets = new double[4 * generations][4];
		
		candidateIndex = 0;
		
		numberHiddenNeurons = 9;
		maxEpochs = 500;
		
		// Building the Neural Networks
		minRequestNet = BuildNetwork (numberHiddenNeurons);
		maxBandwidthNet = BuildNetwork (numberHiddenNeurons);
		minDataYieldNet = BuildNetwork (numberHiddenNeurons);
		maxEnergyNet = BuildNetwork (numberHiddenNeurons);
	}
	
	public BasicNetwork BuildNetwork(int hiddenNeurons)
	{
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null, true, 4));
		network.addLayer(new BasicLayer(new ActivationSigmoid(), true, hiddenNeurons));
		network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 1));
		network.getStructure().finalizeStructure();
		network.reset();
		
		return network;
	}
	
	public void AddToDataset (Solution s) throws JMException, ClassNotFoundException
	{
		double value;
		
		Variable decisionVariables[] = s.getDecisionVariables();
		
		// Adding the Outputs
		for (int index = 0; index < 4; index++)
		{
			targets[candidateIndex][index] = s.getObjective(index);
		}
		
		// Adding the Inputs
		for (int index = 0; index < decisionVariables.length; index++)
		{
			value = decisionVariables[index].getValue();
			value = value / decisionVariables[index].getUpperBound();
			
			inputs[candidateIndex][index] = value;
		}
		
		candidateIndex++;
	}
	
	public void TrainNetworks()
	{
		// Taking the subarray that is already filled with data
		double ins[][] = GetSubmatrix(inputs, candidateIndex, 4);
		double outs[][] = GetSubmatrix(inputs, candidateIndex, 4);
		
		// Creating the Dataset					
		MLDataSet trainingSet = new BasicMLDataSet(ins, outs);
		
		Train (minDataYieldNet, trainingSet);
		Train (minDataYieldNet, trainingSet);
		Train (minDataYieldNet, trainingSet);
		Train (minDataYieldNet, trainingSet);
	}
	
	public void Train (BasicNetwork network, MLDataSet trainingSet)
	{
		// Train the Neural Network
		final ResilientPropagation train = new ResilientPropagation(network, trainingSet);

		int epoch = 1;

		do
		{
			train.iteration();
			//System.out.println("Epoch #" + epoch + " Error:" + train.getError());
			epoch++;
		} while(train.getError() > 0.01 || epoch == maxEpochs);
		
		train.finishTraining();
	}
	
	public double[][] GetSubmatrix (double[][] matrix, int maxLine, int maxCol)
	{
		if (matrix.length < maxLine) return null;
		if (matrix[0].length < maxCol) return null;
		
		double[][] submatrix = new double[maxLine + 1][maxCol];
		
		for (int i = 0; i <= maxLine; i++)
		{
			for (int j = 0; j < maxCol; j++)
			{
				submatrix[i][j] = matrix[i][j];
			}
		}
		
		return submatrix;
	}
	
	public double PredictMinRequestFulfillment (Solution s) throws JMException, ClassNotFoundException
	{
		Variable decisionVariables[] = s.getDecisionVariables();
		double value;		
		
		double[][] input = new double[1][37];
		double[][] output = new double[1][1];
		
		// Taking the Output
		output[0][0] = s.getObjective(0); // Index of the Objective Function
		
		// Taking the Inputs
		for (int index = 0; index < decisionVariables.length; index++)
		{
			value = decisionVariables[index].getValue();
			value = value / decisionVariables[index].getUpperBound();
			
			input[0][index] = value;
		}
		
		MLDataSet trainingSet = new BasicMLDataSet(input, output);
		MLData data = null;
		
		// It has only 1 element
		for (MLDataPair pair : trainingSet)
		{
			data = minRequestNet.compute(pair.getInput());
		}
		
		return data.getData(0);
	}
	
	public double PredictMaxBandwidth (Solution s) throws JMException, ClassNotFoundException
	{
		Variable decisionVariables[] = s.getDecisionVariables();
		double value;		
		
		double[][] input = new double[1][37];
		double[][] output = new double[1][1];
		
		// Taking the Output
		output[0][0] = s.getObjective(1); // Index of the Objective Function
		
		// Taking the Inputs
		for (int index = 0; index < decisionVariables.length; index++)
		{
			value = decisionVariables[index].getValue();
			value = value / decisionVariables[index].getUpperBound();
			
			input[0][index] = value;
		}
		
		MLDataSet trainingSet = new BasicMLDataSet(input, output);
		MLData data = null;
		
		// It has only 1 element
		for (MLDataPair pair : trainingSet)
		{
			data = maxBandwidthNet.compute(pair.getInput());
		}
		
		return data.getData(0);
	}
	
	public double PredictMinDataYield (Solution s) throws JMException, ClassNotFoundException
	{
		Variable decisionVariables[] = s.getDecisionVariables();
		double value;		
		
		double[][] input = new double[1][37];
		double[][] output = new double[1][1];
		
		// Taking the Output
		output[0][0] = s.getObjective(2); // Index of the Objective Function
		
		// Taking the Inputs
		for (int index = 0; index < decisionVariables.length; index++)
		{
			value = decisionVariables[index].getValue();
			value = value / decisionVariables[index].getUpperBound();
			
			input[0][index] = value;
		}
		
		MLDataSet trainingSet = new BasicMLDataSet(input, output);
		MLData data = null;
		
		// It has only 1 element
		for (MLDataPair pair : trainingSet)
		{
			data = minDataYieldNet.compute(pair.getInput());
		}
		
		return data.getData(0);
	}
	
	public double PredictMaxEnergy (Solution s) throws JMException, ClassNotFoundException
	{
		Variable decisionVariables[] = s.getDecisionVariables();
		double value;		
		
		double[][] input = new double[1][37];
		double[][] output = new double[1][1];
		
		// Taking the Output
		output[0][0] = s.getObjective(3); // Index of the Objective Function
		
		// Taking the Inputs
		for (int index = 0; index < decisionVariables.length; index++)
		{
			value = decisionVariables[index].getValue();
			value = value / decisionVariables[index].getUpperBound();
			
			input[0][index] = value;
		}
		
		MLDataSet trainingSet = new BasicMLDataSet(input, output);
		MLData data = null;
		
		// It has only 1 element
		for (MLDataPair pair : trainingSet)
		{
			data = maxEnergyNet.compute(pair.getInput());
		}
		
		return data.getData(0);
	}
}