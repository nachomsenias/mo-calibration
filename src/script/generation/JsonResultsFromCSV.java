package script.generation;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import org.uma.jmetal.solution.IntegerSolution;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.zioabm.beans.CalibrationConfig;

import problem.SingleABMProblem;
import util.exception.calibration.CalibrationException;
import util.io.CSVFileUtils;

public class JsonResultsFromCSV {

	public static void main(String[] args) throws IOException {
		
		String config = CSVFileUtils.readFile(args[0]);
		
		String root = args[1];
		
		String method = args[2];
		
//		String method = "IPOP-CMA-ES-OPT-real";
//		String method = "CMA-ES-OPT-real";
//		String method = "LSHADE-real";
		
		Gson gson = new Gson();
		
		// Read configuration from JSON
		CalibrationConfig calibrationConfig = gson.fromJson(config,
				CalibrationConfig.class);
		
		String folder = root+method;
		
		File directory = new File(folder);
		if(directory.isDirectory()) {
			
			File[] files = directory.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".csv") && !name.startsWith("summary");
				}
			});
			
			for (File result : files) {
				String path = result.getAbsolutePath();
				
				CSVReader reader = new CSVReader(new FileReader(path),',');
				
				List<String[]> items = reader.readAll();
				
				for (String[] item : items) {
					try {
						SingleABMProblem single = new SingleABMProblem(calibrationConfig);
						IntegerSolution solution = single.createSolution();
						for (int v=0; v<single.getNumberOfVariables(); v++) {
							solution.setVariableValue(v, cleanValue(item[v+2]));
						}
						solution.setObjective(0, cleanValueDouble(item[1]));
						
						try {
							String stringResult = single.exportSolution(solution, gson);
							String name =folder+"/resulting_"+method+"_it"+item[0]+".json";
							CSVFileUtils.writeFile(name, stringResult);
						} catch (CalibrationException e) {
							//We skip the result and do nothing.
						}
					} catch (CalibrationException e) {
						e.printStackTrace();
					}
					
				}
				reader.close();
			}
		}
	}

	private static int cleanValue(String value) {
		if(value.startsWith(",")) {
			return Integer.parseInt(value.substring(1));
		} else {
			return Integer.parseInt(value);
		}
	}
	
	private static double cleanValueDouble(String value) {
		if(value.startsWith(",")) {
			return Double.parseDouble(value.substring(1));
		} else {
			return Double.parseDouble(value);
		}
	}
}
