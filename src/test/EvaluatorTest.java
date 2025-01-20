package test;

import static org.junit.Assert.fail;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.uma.jmetal.solution.impl.DefaultIntegerDoubleSolution;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.zioabm.beans.CalibrationConfig;

import problem.MWomABMProblem;
import util.exception.calibration.CalibrationException;
import util.io.CSVFileUtils;

public class EvaluatorTest {

	private String config = "instances/0TP/fast/input_aw_wom_fast.json";
	private String resourceFolder = "/home/ignacio/proyectos/mo-calibration/test/eval-test/";
	private MWomABMProblem problem;
	
	private List<String[]> funs;
	private List<String[]> vars;
	
	public EvaluatorTest() {
		try {
			String content = CSVFileUtils.readFile(config);
			
			Gson gson = new Gson();
			
			//Read configuration from JSON
			CalibrationConfig calibrationConfig = gson.fromJson(content, CalibrationConfig.class);

			problem = new MWomABMProblem(calibrationConfig);
			
			CSVReader reader = new CSVReader(new FileReader(resourceFolder+"FUN0.tsv"), ' ');
			funs=reader.readAll();
			reader.close();
			
			reader = new CSVReader(new FileReader(resourceFolder+"VAR0.tsv"), ' ');
			vars=reader.readAll();
			reader.close();
			
		} catch (CalibrationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void evalTest() {
		int solutions = vars.size();
		for (int s=0; s<solutions; s++) {
			DefaultIntegerDoubleSolution solution = new DefaultIntegerDoubleSolution(problem);
			setValues(solution, vars.get(s));
			problem.evaluate(solution);
			if(!checkSolution(solution, funs.get(s))) {
				System.out.println("Fail! Fitness missmatch!!");
				System.out.println("Fitness: "+String.valueOf(solution.getObjective(0))+
						" "+String.valueOf(solution.getObjective(1)));
				System.out.println("Expected: "+funs.get(s)[0]+" "+funs.get(s)[1]);
				fail();
				
			}
		}
	}
	
	private void setValues(DefaultIntegerDoubleSolution solution, String[] params) {
		solution.setVariableValue(0, Integer.parseInt(params[0]));
		for (int s=1; s<params.length-1; s++) {
			solution.setVariableValue(s, Double.parseDouble(params[s]));
		}
	}
	
	private boolean checkSolution(DefaultIntegerDoubleSolution solution, String[] objs) {
		boolean result = Double.parseDouble(objs[0]) == solution.getObjective(0) ;
		
		result &= Double.parseDouble(objs[1]) == solution.getObjective(1) ;
		
		return result;
	}
}
