package script.sa;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.solution.impl.DefaultDoubleSolution;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.zioabm.beans.CalibrationConfig;

import graphs.Item;
import problem.WomABMProblem;
import util.exception.calibration.CalibrationException;
import util.io.CSVFileUtils;

public class PerformSensitivityAnalysis {
	
	private static class Modification{
		public int index;
		public double inc;
		public Modification(int index, double inc) {
			this.index = index;
			this.inc = inc;
		}
	}
	
	public static void main(String[] args) throws IOException, CalibrationException {
		//Problem file
		String jsonFile = args[0];
		//Selected configurations
		String selectedFile = args[1];
		//Output folder
		String folder = args[2];
		
		String config = CSVFileUtils.readFile(jsonFile);
		
		CSVReader reader = new CSVReader(new BufferedReader(new FileReader(selectedFile)), ';');
		
		List<String[]> selected = reader.readAll();
		
		reader.close();
		
		//Get ModelDefinition
		Gson gson = new Gson();
		
		CalibrationConfig calibrationConfig = gson.fromJson(config, 
				CalibrationConfig.class);
		
		//Compute Hold-out error
		calibrationConfig.setHoldOut(0.25);
		
		WomABMProblem problem = new WomABMProblem(calibrationConfig);
		
		//Selected
		for (int i=0; i<selected.size(); i++) {
			
			//Load parameters
			String[] values = selected.get(i)[1].split(" ");
			double[] params = new double[values.length];
			
			for (int v=0; v<values.length; v++) {
				params[v] = Double.parseDouble(values[v]);
			}
			
			DefaultDoubleSolution solution = createSolution(problem, params);
			
			List<Modification> mods = getMods(solution.getNumberOfVariables());
			
			for (Modification mod : mods) {
				double[] saValues = params.clone();
				List<DefaultDoubleSolution> solutions = 
						new ArrayList<DefaultDoubleSolution>();	
				
				do {
					saValues[mod.index]+=mod.inc;
					solution = createSolution(problem, saValues);
					problem.evaluate(solution);
					solutions.add(solution);
				}
				while((saValues[mod.index]<=1.0-mod.inc)
						&& (saValues[mod.index]>=0.0+mod.inc));
				
				List<Item> items = SimpleSensitivityAnalysis.createItems(solutions);
				String output = folder+"individuals-"+i+"-"+mod.index+"-"+mod.inc+".tsv"; 
				Item.writeItems(items, output);
			}
			
		}

		
	}
	
	private static DefaultDoubleSolution createSolution(
			WomABMProblem problem, double[] params) {
		DefaultDoubleSolution solution = new DefaultDoubleSolution(problem);
		
		for (int v = 0; v<solution.getNumberOfVariables(); v++) {
			solution.setVariableValue(v, params[v]);
		}
		return solution;
	}
	
	private static List<Modification> getMods(int params) {
		List<Modification> mods = new ArrayList<Modification>();
		
		for (int p=0; p<params; p++) {
			double inc = 0.001;
			mods.add(new Modification(p, inc));
			mods.add(new Modification(p, -inc));
		}
		
		return mods;
	}
	
	
}
