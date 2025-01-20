package test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.google.gson.Gson;
import com.zioabm.beans.CalibrationConfig;

import calibration.fitness.AlternateFitnessFunction;
import calibration.fitness.history.ScoreBean;
import problem.SingleABMProblem;
import util.exception.calibration.CalibrationException;
import util.functions.MatrixFunctions;
import util.io.CSVFileUtils;

public class TestAlternateFitnessFunction {

	static String testFile ="instances/0TP/fast/input_aw_wom_fast.json";
	
	CalibrationConfig calibrationConfig;
	
	SingleABMProblem problem = null;
	
	public TestAlternateFitnessFunction() {
		String ccFile = "";
		try {
			ccFile = CSVFileUtils.readFile(testFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Gson gson = new Gson();

		// Read configuration from JSON
		calibrationConfig = gson.fromJson(ccFile,
				CalibrationConfig.class);
		
		try {
			problem = new SingleABMProblem(calibrationConfig);
		} catch (CalibrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Test
	public void testZero() {
		AlternateFitnessFunction function = 
				new AlternateFitnessFunction(AlternateFitnessFunction.DEFAULT_THRESHOLD);
		
		double[][] history = calibrationConfig.getTargetAwareness();
		double[][][] historyCopy = new double[1][][];
		historyCopy[0] = MatrixFunctions.copyMatrix(history);
		
		//Compute training and no holdout
		ScoreBean[] beans = function.computeScoreDetails(history, historyCopy, true, 0);
		
		//This should return 0 because there is no error.
		for (ScoreBean bean : beans) {
			assertTrue(bean.getScore()==0);
		}
	}
	
	@Test
	public void testUpperOutside() {
		AlternateFitnessFunction function = 
				new AlternateFitnessFunction(AlternateFitnessFunction.DEFAULT_THRESHOLD);
		
		double[][] history = calibrationConfig.getTargetAwareness();
		double[][][] historyCopy = new double[1][][];
		historyCopy[0] = MatrixFunctions.copyMatrix(history);
		
		
		//The modified matrix should be outhside the limits, with max error.
		for (int i=0; i<history.length; i++) {
			for (int j=0; j<history[i].length; j++) {
				historyCopy[0][i][j] += AlternateFitnessFunction.DEFAULT_THRESHOLD+1;
			}
		}
		
		//Compute training with no holdout
		ScoreBean[] beans = function.computeScoreDetails(history, historyCopy, true, 0);
		
		//This should return the number of (failed) steps.
		for (ScoreBean bean : beans) {
			assertTrue(bean.getScore()==history[0].length);
		}
	}
	
	@Test
	public void testUnderOutside() {
		AlternateFitnessFunction function = 
				new AlternateFitnessFunction(AlternateFitnessFunction.DEFAULT_THRESHOLD);
		
		double[][] history = calibrationConfig.getTargetAwareness();
		double[][][] historyCopy = new double[1][][];
		historyCopy[0] = MatrixFunctions.copyMatrix(history);
		
		
		//The modified matrix should be outhside the limits, with max error.
		for (int i=0; i<history.length; i++) {
			for (int j=0; j<history[i].length; j++) {
				historyCopy[0][i][j] -= AlternateFitnessFunction.DEFAULT_THRESHOLD+1;
			}
		}
		
		//Compute training with no holdout
		ScoreBean[] beans = function.computeScoreDetails(history, historyCopy, true, 0);
		
		//This should return the number of (failed) steps.
		for (ScoreBean bean : beans) {
			assertTrue(bean.getScore()==history[0].length);
		}
	}

}
