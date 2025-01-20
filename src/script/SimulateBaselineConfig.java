package script;

import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.zioabm.beans.CalibrationConfig;
import com.zioabm.beans.SimulationResult;

import model.ModelDefinition;
import model.ModelRunner;
import problem.MWomABMProblem;
import util.exception.calibration.CalibrationException;
import util.functions.MatrixFunctions;
import util.io.CSVFileUtils;
import util.io.StatisticsRecordingBean;
import util.statistics.MonteCarloStatistics;
import util.statistics.Statistics;
import util.statistics.Statistics.TimePeriod;

public class SimulateBaselineConfig {

	public static void main(String[] args) throws IOException, CalibrationException {
		//Problem file
		String jsonFile = args[0];
		//Target folder
		String folder = args[1];
		
		String config = CSVFileUtils.readFile(jsonFile);
		
		//Get ModelDefinition
		Gson gson = new Gson();
		
		CalibrationConfig calibrationConfig = gson.fromJson(config, 
				CalibrationConfig.class);
		
		MWomABMProblem problem = new MWomABMProblem(calibrationConfig);
		
		StatisticsRecordingBean recordingBean = 
				calibrationConfig.getSimConfig().getStatisticRecordingConfiguration();
		
		ModelDefinition md = problem.getMD();
		
		int mc = calibrationConfig.getSimConfig().getnMC();
		MonteCarloStatistics stats = ModelRunner.simulateModel(
				md, mc, false, 
				recordingBean
			);
		
		
		SimulationResult newResult = new SimulationResult();
		
		newResult.loadValuesFromStatistics(
				stats,
					md.getAgentsRatio(),
							recordingBean,
							calibrationConfig.getSimConfig().getStatPeriod());
		
		String output = gson.toJson(newResult, SimulationResult.class);
		
		//Export JSON results to File
		FileWriter fw = new FileWriter(folder+"results.json");
		fw.write(output);
		fw.close();
		
		//Get time series
//		double[][] awarenessHistoryValues = calibrationConfig.getTargetAwareness();
//		double[][] womHistoryValues = calibrationConfig.getTargetWOMVolumen();
	
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
			
//			double[][] simValues = new double[5][];
			double[][] simValues = new double[4][]; 
			
			simValues[0] = GetTimeSeriesFromOutput.getStepArray(steps);
//			simValues[0] = GetTimeSeriesFromOutput.getStepArray(
//					steps/Statistics.calculateWeeksPerPeriod(AWperiod));
			simValues[1] = minAwareness[b][0];
			simValues[2] = maxAwareness[b][0];
			simValues[3] = avgAwareness[b][0];
//			simValues[4] = awarenessHistoryValues[b];
//			simValues[4] = formatValues(awarenessHistoryValues[b], AWperiod);
			
			String name = "awareness_area_b"+(b+1);
			GetTimeSeriesFromOutput.exportValues(folder, name, simValues);
			
		}
		
		TimePeriod WOMperiod = TimePeriod.valueOf(
				calibrationConfig.getTargetWOMVolumenPeriod());
		newResult.loadValuesFromStatistics(
				stats,
					md.getAgentsRatio(),
							recordingBean,
							WOMperiod);
		
		double[][][] minWOM = newResult.womVolumeByBrandBySegByStepMin;
		double[][][] maxWOM = newResult.womVolumeByBrandBySegByStepMax;
		double[][][] avgWOM = newResult.womVolumeByBrandBySegByStepAvg;
		
		for (int b=0; b<brands; b++) {
			
//			double[][] simValues = new double[5][];
			double[][] simValues = new double[4][]; 
			
//			simValues[0] = GetTimeSeriesFromOutput.getStepArray(steps);
			simValues[0] = GetTimeSeriesFromOutput.getStepArray(
					steps/Statistics.calculateWeeksPerPeriod(WOMperiod));
			simValues[1] = minWOM[b][0];
			simValues[2] = maxWOM[b][0];
			simValues[3] = avgWOM[b][0];
//			simValues[4] = womHistoryValues[b];
//			simValues[4] = formatValues(womHistoryValues[b],WOMperiod);
			
			String name = "wom_area_b"+(b+1);
			GetTimeSeriesFromOutput.exportValues(folder, name, simValues);
		}
	}
	
//	private static double[] formatValues(double[] values, TimePeriod period) {
//		if(period==TimePeriod.WEEKLY) {
//			return values;
//		}
//		int increment = Statistics.calculateWeeksPerPeriod(period);
//		if(values.length % increment!= 0) {
//			throw new IllegalArgumentException("Number of steps mod period != 0");
//		}
//		int steps = values.length / Statistics.calculateWeeksPerPeriod(period);
//		
//		double[] newSteps = new double[steps];
//		
//		for (int s=0; s<steps; s++) {
//			double[] subarray = ArrayUtils.subarray(values, s*increment, s*(increment+1));
//			newSteps[s] = StatUtils.sum(subarray);
//		}
//		
//		return newSteps;
//	}

}
