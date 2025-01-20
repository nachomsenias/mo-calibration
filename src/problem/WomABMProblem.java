package problem;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.uma.jmetal.solution.DoubleSolution;

import com.zioabm.beans.CalibrationConfig;

import calibration.CalibrationParameter;
import calibration.fitness.history.ScoreBean.ScoreWrapper;
import model.ModelRunner;
import util.exception.calibration.CalibrationException;
import util.statistics.MonteCarloStatistics;

public class WomABMProblem extends SimpleABMProblem {

	/**
	 * Generated id.
	 */
	private static final long serialVersionUID = 5517228340457271672L;

	public WomABMProblem(CalibrationConfig cc) throws CalibrationException {
		super(cc);
		setName("WomABMProblem");
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
		
		//Store simulation values
		ScoreWrapper wrapper = manager.computeTrainingScore(mcStats);
		solution.setObjective(0, wrapper.awarenessScore.getScore());
		solution.setObjective(1, wrapper.womVolumeScore.getScore());
		//Print agregated score
//		System.out.println(":: " + (wrapper.finalScore)+ " ;; "+simulationTime);
	}
}
