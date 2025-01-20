package problem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

import com.zioabm.beans.CalibrationConfig;
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

public class SimpleABMProblem extends AbstractDoubleProblem {
	
	protected int mcIterations;
	protected ModelDefinition md;
	protected StringBean[] parameters;
	
	protected CalibrationParametersManager paramManager;
	protected HistoryManager manager;
	private ModelManager modelManager;
	
	/**
	 * Generated id.
	 */
	private static final long serialVersionUID = 2656241940318033406L;

	public SimpleABMProblem(CalibrationConfig cc) throws CalibrationException {
		
		parameters = cc.getCalibrationModelParameters();
		
		int numberOfVariables = parameters.length;
		setNumberOfVariables(numberOfVariables);
	    setNumberOfObjectives(2);
	    setName("SimpleABMProblem");
	    
	    //Set problem
	    SimulationConfig simConfig = cc.getSimConfig();
	    mcIterations = simConfig.getnMC();
	    md = simConfig.getModelDefinition();
	    paramManager= new CalibrationParametersManager(parameters, md, false);
	    manager=cc.getHistoryManager();
	    modelManager = new ModelManager(md, paramManager.getInvolvedDrivers());
	    	    
	    List<Double> lowerLimit = new ArrayList<>(numberOfVariables);
	    List<Double> upperLimit = new ArrayList<>(numberOfVariables);
	    
	    for (int i = 0; i < numberOfVariables; i++) {
	    	
	    	double[] paramDef = paramManager.parseParameterValue(parameters[i].getValue());
	    	// Min
	    	lowerLimit.add(paramDef[0]);
	    	// Max
	    	upperLimit.add(paramDef[1]);
	    }

	    setLowerLimit(lowerLimit);
	    setUpperLimit(upperLimit);
	}

	@Override
	public void evaluate(DoubleSolution solution) {
		//Set values into model definition instance.
		List<CalibrationParameter> modelParams = paramManager.getParameters();
		
		int variables = parameters.length;
		for (int v = 0; v<variables; v++) {
			try {
				double value = solution.getVariableValue(v);
				invoke(modelParams.get(v), value);
				System.out.print(value + " ");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		
		//Simulates the model for getting a estimation of its computation time.
		long before = System.currentTimeMillis();
		
		MonteCarloStatistics mcStats= ModelRunner.simulateModel(
				md, mcIterations, false,
				manager.getStatsBean());
		
		long simulationTime = System.currentTimeMillis()-before;
		
		//Store simulation values
		ScoreWrapper wrapper = manager.computeTrainingScore(
				mcStats);
		solution.setObjective(0, wrapper.awarenessScore.getScore());
		solution.setObjective(1, wrapper.totalAwarenessScore.getScore());
		//Print agregated score
		System.out.println(":: " + (wrapper.finalScore)+ " ;; "+simulationTime);
	}
	
	protected void invoke(CalibrationParameter param, double value) throws 
		IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		int[] indexes = param.indexes;
		Method setter = param.setterMethod;
		
		if (indexes == null) {
			setter.invoke(paramManager, value);	
		} else {
			switch(indexes.length) {
				case 0:
					throw new IllegalStateException(
						"Undefined indexes for parameter: " + param.parameterName
					);
				case 1:
					setter.invoke(modelManager, indexes[0], value);
					break;
				case 2:
					setter.invoke(modelManager, indexes[0], indexes[1], value);
					break;
				case 3:
					setter.invoke(modelManager, 
						indexes[0], indexes[1], indexes[2], value
					);
					break;
				case 4:
					setter.invoke(modelManager, 
						indexes[0], indexes[1], indexes[2], indexes[3], value
					);
					break;
				default:
					throw new UnsupportedOperationException(
						"Cannot invoke setter with " + indexes.length + " indexes"
					);
			}
		}
	}
	
	public ModelDefinition getMD() {
		return md;
	}

}
