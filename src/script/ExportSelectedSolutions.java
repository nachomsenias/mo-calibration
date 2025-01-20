package script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.solution.impl.DefaultDoubleSolution;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.zioabm.beans.CalibrationConfig;
import com.zioabm.beans.SimulationResult;

import calibration.fitness.history.HistoryManager;
import calibration.fitness.history.ScoreBean.ScoreWrapper;
import model.ModelDefinition;
import model.ModelRunner;
import problem.WomABMProblem;
import util.exception.calibration.CalibrationException;
import util.functions.MatrixFunctions;
import util.io.CSVFileUtils;
import util.io.StatisticsRecordingBean;
import util.statistics.MonteCarloStatistics;
import util.statistics.Statistics;
import util.statistics.Statistics.TimePeriod;

public class ExportSelectedSolutions {

	public static String[] tags = {"bestAW", "TradeOff", "bestWOM"}; 
	
	public static void main(String[] args) throws IOException, CalibrationException {
		//Problem file
		String jsonFile = args[0];
		//Selected configurations
		String selectedFile = args[1];
		//Target folder
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
		calibrationConfig.setHoldOut(0.75);
		List<String[]> holdoutValues = new ArrayList<String[]>();
		
//		MWomABMProblem problem = new MWomABMProblem(calibrationConfig);
		WomABMProblem problem = new WomABMProblem(calibrationConfig);
		
		StatisticsRecordingBean recordingBean = 
				calibrationConfig.getSimConfig().getStatisticRecordingConfiguration();
		
		DefaultDoubleSolution solution = new DefaultDoubleSolution(problem);
		
		for (int i=0; i<selected.size(); i++) {
			
			//Load parameters
			String[] values = selected.get(i)[1].split(" ");
			solution.setVariableValue(0, Double.parseDouble(values[0]));
			for (int v=1; v<values.length; v++) {
				solution.setVariableValue(v, Double.parseDouble(values[v]));
			}
			
			//Simulate configuration
			problem.evaluate(solution);
			
			ModelDefinition md = problem.getMD();
			
			MonteCarloStatistics stats = ModelRunner.simulateModel(
					md, calibrationConfig.getSimConfig().getnMC(), false, 
					recordingBean
				);
			
			
			SimulationResult newResult = new SimulationResult();
			
			newResult.loadValuesFromStatistics(
					stats,
						md.getAgentsRatio(),
								recordingBean,
								calibrationConfig.getSimConfig().getStatPeriod());
			
			String output = gson.toJson(newResult, SimulationResult.class);
			
			String dir = folder+"/"+tags[i]+"/";
			
			File f = new File(dir);
			f.mkdirs();
			
			//Export JSON results to File
			FileWriter fw = new FileWriter(dir+"results.json");
			fw.write(output);
			fw.close();
			
			//Get time series
			double[][] awarenessHistoryValues = calibrationConfig.getTargetAwareness();
			double[][] womHistoryValues = calibrationConfig.getTargetWOMVolumen();
		
			int brands = calibrationConfig.getSimConfig().getnBrands();
			int steps = calibrationConfig.getSimConfig().getnWeeks();
			
			double[][][] minAwareness = MatrixFunctions.scaleCopyOfDouble3dMatrix(
					newResult.awarenessByBrandBySegByStepMin, 100);
			double[][][] maxAwareness = MatrixFunctions.scaleCopyOfDouble3dMatrix(
					newResult.awarenessByBrandBySegByStepMax,100);
			double[][][] avgAwareness = MatrixFunctions.scaleCopyOfDouble3dMatrix(
					newResult.awarenessByBrandBySegByStepAvg,100);
			
			//Load values
			for (int b=0; b<brands; b++) {
				
				double[][] simValues = new double[5][]; 
				
				simValues[0] = GetTimeSeriesFromOutput.getStepArray(steps);
				simValues[1] = minAwareness[b][0];
				simValues[2] = maxAwareness[b][0];
				simValues[3] = avgAwareness[b][0];
				simValues[4] = awarenessHistoryValues[b];
				
				String name = "awareness_area_b"+(b+1);
				GetTimeSeriesFromOutput.exportValues(dir, name, simValues);
			}
			
			TimePeriod period = TimePeriod.valueOf(
					calibrationConfig.getTargetWOMVolumenPeriod());
			
			newResult.loadValuesFromStatistics(
					stats,
						md.getAgentsRatio(),
								recordingBean, period);
			
			double[][][] minWOM = newResult.womVolumeByBrandBySegByStepMin;
			double[][][] maxWOM = newResult.womVolumeByBrandBySegByStepMax;
			double[][][] avgWOM = newResult.womVolumeByBrandBySegByStepAvg;
			
			//Load values
			for (int b=0; b<brands; b++) {
				double[][] simValues = new double[5][]; 
				
				simValues[0] = GetTimeSeriesFromOutput.getStepArray(steps
						/ Statistics.calculateWeeksPerPeriod(period));
				simValues[1] = minWOM[b][0];
				simValues[2] = maxWOM[b][0];
				simValues[3] = avgWOM[b][0];
				simValues[4] = womHistoryValues[b];
				
//				simValues[4] = formatValues(womHistoryValues[b],period);
				
				String name = "wom_area_b"+(b+1);
				GetTimeSeriesFromOutput.exportValues(dir, name, simValues);
			}
			
			HistoryManager manager = calibrationConfig.getHistoryManager();
			ScoreWrapper wrapper = manager.computeTrainingScore(stats);
			wrapper = manager.computeHoldOutScore(stats);
			String[] hValues = {
					String.valueOf(wrapper.awarenessScore.getScore()),
					String.valueOf(wrapper.womVolumeScore.getScore())
					};
			holdoutValues.add(hValues);
		}
		
		String fileName = folder + "/holdout.csv";
		CSVWriter writer = new CSVWriter(new FileWriter(fileName), ' ');
		writer.writeAll(holdoutValues);
		writer.close();
	}

}
