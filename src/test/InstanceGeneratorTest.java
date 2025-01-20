package test;

import java.io.IOException;

import org.junit.Test;
import org.uma.jmetal.solution.impl.DefaultIntegerDoubleSolution;

import com.google.gson.Gson;
import com.zioabm.beans.CalibrationConfig;

import problem.MWomABMProblem;
import util.exception.calibration.CalibrationException;
import util.io.CSVFileUtils;

public class InstanceGeneratorTest {

	private String config = "test/test-instances/50TP/MEDIUM/test-instance.json";
	private String targetConfig = "test/test-instances/50TP/MEDIUM/target-values.out";
	private MWomABMProblem problem;
	
	String[] spplited;
	
	public InstanceGeneratorTest() {
		try {
			String content = CSVFileUtils.readFile(config);
			
			Gson gson = new Gson();
			
			//Read configuration from JSON
			CalibrationConfig calibrationConfig = gson.fromJson(content, CalibrationConfig.class);

			problem = new MWomABMProblem(calibrationConfig);
			
			String values = CSVFileUtils.readFile(targetConfig);
			
			spplited = values.split(", ");
			
		} catch (CalibrationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void evalTest() {		
		DefaultIntegerDoubleSolution solution = new DefaultIntegerDoubleSolution(problem);
		setValues(solution, spplited);
		problem.evaluate(solution);

		System.out.println("Fitness: "+String.valueOf(solution.getObjective(0))+
				" "+String.valueOf(solution.getObjective(1)));
		
	}
	
	private void setValues(DefaultIntegerDoubleSolution solution, String[] params) {
		solution.setVariableValue(0, Integer.parseInt(params[0]));
		for (int s=1; s<params.length-1; s++) {
			solution.setVariableValue(s, Double.parseDouble(params[s]));
		}
	}
}
