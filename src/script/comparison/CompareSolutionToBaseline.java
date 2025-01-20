package script.comparison;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.zioabm.beans.CalibrationConfig;

import calibration.CalibrationParametersManager;
import model.ModelDefinition;
import model.ModelManager;
import util.StringBean;
import util.exception.calibration.CalibrationException;
import util.io.CSVFileUtils;

public class CompareSolutionToBaseline {

	public static void main(String[] args) throws IOException, CalibrationException {
		//Problem file
		String jsonFile = args[0];
		//Target folder
		String selectedFile = args[1];
		//Target folder
		String folder = args[2];
		
		String config = CSVFileUtils.readFile(jsonFile);
		Gson gson = new Gson();
		
		CalibrationConfig calibrationConfig = gson.fromJson(config, 
				CalibrationConfig.class);
		
		CSVReader reader = new CSVReader(
				new BufferedReader(new FileReader(selectedFile)), ';');
		
		List<String[]> selected = reader.readAll();
		reader.close();
		
		List<String[]> parameters = new ArrayList<String[]>();
		parameters.add(getBaselineValues(calibrationConfig));
		
		for (int s = 0; s<selected.size(); s++) {
			String[] paramValues = selected.get(s)[1].split(" ");
			parameters.add(paramValues);
		}
		
		String output = folder + "parameters.csv";
		CSVWriter writer = new CSVWriter(new FileWriter(output), ' ');
		writer.writeAll(parameters);
		writer.close();
	}
	
	private static String[] getBaselineValues(CalibrationConfig cc) 
			throws CalibrationException {
		
		StringBean[] parameters = cc.getCalibrationModelParameters();
		String[] values = new String[parameters.length+1];
		
		ModelDefinition md = cc.getSimConfig().getModelDefinition();
		CalibrationParametersManager paramManager = 
				new CalibrationParametersManager(parameters, md, false);
		
		ModelManager modelManager = 
				new ModelManager(md, paramManager.getInvolvedDrivers());
		paramManager.setModelManager(modelManager);
		paramManager.setRealCoding(true);
		
		//M value
		values[0] = String.valueOf(modelManager.getMValue(0));
		
		for (int i=0; i<parameters.length; i++) {
			values[i+1] = String.valueOf(paramManager.getParameterValue(i));
		}
		
		return values;
	}

}
