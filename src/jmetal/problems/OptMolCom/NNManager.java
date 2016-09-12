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

public class NNManager
{
	 private static NNManager instance = null;
	
	BasicNetwork RTTNet;

	
	double inputs[][];
	double outputs[][];
	
	int candidateIndex;
	int numberHiddenNeurons;
	int maxEpochs;
	
	public static NNManager getInstance()
	{
		if (instance == null)
			instance = new NNManager();
	    
		return instance;
	}
	
	public static void DestroyInstance()
	{
		instance = null;
	}
	
	protected NNManager()
	{
		int max_samples = 10000;
		
		inputs = new double[max_samples][3];
		outputs = new double[max_samples][1];
		
		candidateIndex = 0;
		
		numberHiddenNeurons = 9;
		maxEpochs = 500;
		
		// Building the Neural Networks
		RTTNet = BuildNetwork (numberHiddenNeurons);

	}
	
	public BasicNetwork BuildNetwork(int hiddenNeurons)
	{
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null, true, 3));
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
		for (int index = 0; index < 1; index++)
		{
			outputs[candidateIndex][index] = s.getObjective(index);
		}
		
		// Adding the Inputs
		for (int index = 0; index < decisionVariables.length; index++)
		{
			value = decisionVariables[index].getValue();
			//value = value / decisionVariables[index].getUpperBound();
			
			inputs[candidateIndex][index] = value;
		}
		
		candidateIndex++;
	}
	
	public void TrainNetworks()
	{
		// Taking the subarray that is already filled with data
		double ins[][] = GetSubmatrix(inputs, candidateIndex, 3);
		double outs[][] = GetSubmatrix(outputs, candidateIndex, 1);
		
		// Creating the Dataset					
		MLDataSet trainingSet = new BasicMLDataSet(ins, outs);
		
		Train (RTTNet, trainingSet);
	
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
		} while(train.getError() > 0.01 && epoch <= maxEpochs);
		
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
	
	public double PredictRTT (Solution s) throws JMException, ClassNotFoundException
	{
		Variable decisionVariables[] = s.getDecisionVariables();
		double value;		
		
		double[][] input = new double[1][3];
		double[][] output = new double[1][1];
		
		// Taking the Output
		output[0][0] = s.getObjective(0); // Index of the Objective Function
		
		// Taking the Inputs
		for (int index = 0; index < decisionVariables.length; index++)
		{
			value = decisionVariables[index].getValue();
			//value = value / decisionVariables[index].getUpperBound();
			
			input[0][index] = value;
		}
		
		MLDataSet trainingSet = new BasicMLDataSet(input, output);
		MLData data = null;
		
		// It has only 1 element
		for (MLDataPair pair : trainingSet)
		{
			data = RTTNet.compute(pair.getInput());
		}
		
		return data.getData(0);
	}
	

}