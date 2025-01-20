package problem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.problem.impl.AbstractIntegerProblem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.solutionattribute.impl.Fitness;

import com.google.gson.Gson;
import com.zioabm.beans.CalibrationConfig;
import com.zioabm.beans.CalibrationResponse;
import com.zioabm.beans.SimulationConfig;

import calibration.CalibrationParameter;
import calibration.CalibrationParametersManager;
import calibration.fitness.history.HistoryManager;
import calibration.fitness.history.ScoreBean.ScoreWrapper;
import model.ModelDefinition;
import model.ModelManager;
import model.ModelRunner;
import util.StringBean;
import util.exception.calibration.CalibrationException;
import util.statistics.MonteCarloStatistics;

public class SingleABMProblem extends AbstractIntegerProblem {

	/**
	 * Generated.
	 */
	private static final long serialVersionUID = -3906892684159455282L;
	
	protected int mcIterations;
	protected ModelDefinition md;
	protected StringBean[] parameters;
	
	protected CalibrationParametersManager paramManager;
	protected HistoryManager manager;
	private ModelManager modelManager;
	
	private double[] initialConfig;
	private CalibrationConfig cc;
	
	public SingleABMProblem(CalibrationConfig cc) throws CalibrationException {
		
		this.cc = cc;
		parameters = cc.getCalibrationModelParameters();
		
		int numberOfVariables = parameters.length;
		setNumberOfVariables(numberOfVariables);
	    setNumberOfObjectives(1);
	    setName("SingleABMProblem");
	    
	    //Set problem
	    SimulationConfig simConfig = cc.getSimConfig();
	    mcIterations = simConfig.getnMC();
	    md = simConfig.getModelDefinition();
	    paramManager= new CalibrationParametersManager(parameters, md, false);
	    manager=cc.getHistoryManager();
//	    manager=cc.getAlternateHistoryManager();
	    modelManager = new ModelManager(md, paramManager.getInvolvedDrivers());
	    paramManager.setModelManager(modelManager);
	    	    
	    List<Integer> lowerLimit = new ArrayList<>(numberOfVariables);
	    List<Integer> upperLimit = new ArrayList<>(numberOfVariables);
	    
	    for (int i = 0; i < numberOfVariables; i++) {
	    	
	    	double[] paramDef = paramManager.parseParameterValue(parameters[i].getValue());
	    	
	    	int conversionFactor = (int) (1 / paramDef[2]);
	    	
	    	// Min
	    	int min = (int)(paramDef[0] * conversionFactor);
	    	lowerLimit.add(min);
	    	// Max
	    	int max = (int)(paramDef[1] * conversionFactor);
	    	upperLimit.add(max);
	    }

	    setLowerLimit(lowerLimit);
	    setUpperLimit(upperLimit);
	    
	    storeInitial();
	}
	
	@Override
	public void evaluate(IntegerSolution solution) {
		//Set values into model definition instance.
		List<CalibrationParameter> modelParams = paramManager.getParameters();
		
		int variables = parameters.length;
		for (int v = 0; v<variables; v++) {
			try {
				//XXX Convert from integer
				double value = solution.getVariableValue(v);
				invoke(modelParams.get(v), value);
//				System.out.print(value + " ");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		
		//Simulates the model for getting a estimation of its computation time.
//		long before = System.currentTimeMillis();
		
		MonteCarloStatistics mcStats= ModelRunner.simulateModel(
				md, mcIterations, false,
				manager.getStatsBean());
		
//		long simulationTime = System.currentTimeMillis()-before;
		
		//Store simulation values employing a single objective
		ScoreWrapper wrapper = manager.computeTrainingScore(mcStats);
		solution.setObjective(0, wrapper.finalScore);

		solution.setAttribute(new Fitness<>(), wrapper.finalScore);
		
		//Print agregated score
//		System.out.println(":: " + (wrapper.finalScore)+ " ;; "+simulationTime);
	}
	
	protected void invoke(CalibrationParameter param, double value) throws 
		IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		int[] indexes = param.indexes;
		Method setter = param.setterMethod;
		
		double doubleValueFromIntRep =  value / param.conversionFactor;
		
		if (indexes == null) {
			setter.invoke(paramManager, doubleValueFromIntRep);	
		} else {
			switch(indexes.length) {
				case 0:
					throw new IllegalStateException(
						"Undefined indexes for parameter: " + param.parameterName
					);
				case 1:
					setter.invoke(modelManager, indexes[0], doubleValueFromIntRep);
					break;
				case 2:
					setter.invoke(modelManager, indexes[0], indexes[1], doubleValueFromIntRep);
					break;
				case 3:
					setter.invoke(modelManager, 
						indexes[0], indexes[1], indexes[2], doubleValueFromIntRep
					);
					break;
				case 4:
					setter.invoke(modelManager, 
						indexes[0], indexes[1], indexes[2], indexes[3], doubleValueFromIntRep
					);
					break;
				default:
					throw new UnsupportedOperationException(
						"Cannot invoke setter with " + indexes.length + " indexes"
					);
			}
		}
	}
	
	public String exportSolution(IntegerSolution solution, Gson gson) throws CalibrationException {
		double previousFitness = solution.getObjective(0);
		
		List<CalibrationParameter> modelParams = paramManager.getParameters();
		
		int variables = parameters.length;
		for (int v = 0; v<variables; v++) {
			try {
				//XXX Convert from integer
				double value = solution.getVariableValue(v);
				invoke(modelParams.get(v), value);
//				System.out.print(value + " ");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		
		//Simulates the model for getting a estimation of its computation time.
//		long before = System.currentTimeMillis();
		
		MonteCarloStatistics mcStats= ModelRunner.simulateModel(
				md, mcIterations, false,
				manager.getStatsBean());
		
//		long simulationTime = System.currentTimeMillis()-before;
		
		CalibrationResponse calibrationResponse = new CalibrationResponse();
		calibrationResponse.setCalibratedModel(md, this.cc.getSimConfig());
		
		//Store simulation values employing a single objective
		ScoreWrapper wrapper = manager.computeTrainingScore(mcStats);
		if(wrapper.finalScore != previousFitness) {
			System.out.println("Warning! Previous fitness ("+previousFitness+
					") and computed fitness ("+wrapper.finalScore+") mismatch!");
			throw new CalibrationException("Avoiding mismatching result.");
		}
		
		calibrationResponse.setScoreDetails(wrapper);
		calibrationResponse.done();
		
		return gson.toJson(calibrationResponse,CalibrationResponse.class);
		
	}
	
	/**
	 * Stores the initial configuration for using it as the typical X.
	 */
	private void storeInitial() {
		double [] initialConfig = new double[parameters.length];
		for (int i = 0; i < parameters.length; i++)
			try {
				initialConfig[i] = paramManager.getParameterValue(i);
			} catch (CalibrationException e) {
				System.out.println("Error loading the initial individual.");
				e.printStackTrace();
			}
	}
	
	public double[] getInitial() {
		return initialConfig;
	}
	
	public ModelDefinition getMD() {
		return md;
	}
}
