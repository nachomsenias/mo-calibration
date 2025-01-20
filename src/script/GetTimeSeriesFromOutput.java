package script;

import java.io.IOException;

import org.ejml.simple.SimpleMatrix;

import com.google.gson.Gson;
import com.zioabm.beans.CalibrationConfig;
import com.zioabm.beans.SimulationResult;

import util.functions.MatrixFunctions;
import util.io.CSVFileUtils;

public class GetTimeSeriesFromOutput {
	
	public static void main(String[] args) throws IOException {
		String inputJsonFile = args[0];
		String outputJsonFile = args[1];
		String folder = args[2];
		
		String config = CSVFileUtils.readFile(inputJsonFile);
		
		//Get ModelDefinition
		Gson gson = new Gson();
		
		CalibrationConfig calibrationConfig = gson.fromJson(config, 
				CalibrationConfig.class);
		
		double[][] awarenessHistoryValues = calibrationConfig.getTargetAwareness();
		double[][] womHistoryValues = calibrationConfig.getTargetWOMVolumen();
	
		int brands = calibrationConfig.getSimConfig().getnBrands();
		int steps = awarenessHistoryValues[0].length;
		
		//Get results output
		String output = CSVFileUtils.readFile(outputJsonFile);
		
		SimulationResult result = gson.fromJson(output, SimulationResult.class);
		
		double[][][] minAwareness = MatrixFunctions.scaleCopyOfDouble3dMatrix(
				result.awarenessByBrandBySegByStepMin, 100);
		double[][][] maxAwareness = MatrixFunctions.scaleCopyOfDouble3dMatrix(
				result.awarenessByBrandBySegByStepMax,100);
		double[][][] avgAwareness = MatrixFunctions.scaleCopyOfDouble3dMatrix(
				result.awarenessByBrandBySegByStepAvg,100);
		
		double[][][] minWOM = result.womVolumeByBrandBySegByStepMin;
		double[][][] maxWOM = result.womVolumeByBrandBySegByStepMax;
		double[][][] avgWOM = result.womVolumeByBrandBySegByStepAvg;
		
		//Load values
		for (int b=0; b<brands; b++) {
			
			double[][] values = new double[5][]; 
			
			values[0] = getStepArray(steps);
			values[1] = minAwareness[b][0];
			values[2] = maxAwareness[b][0];
			values[3] = avgAwareness[b][0];
			values[4] = awarenessHistoryValues[b];
			
			String name = "awareness_area_b"+(b+1);
			exportValues(folder, name, values);
			
			values[0] = getStepArray(steps);
			values[1] = minWOM[b][0];
			values[2] = maxWOM[b][0];
			values[3] = avgWOM[b][0];
			values[4] = womHistoryValues[b];
			
			name = "wom_area_b"+(b+1);
			exportValues(folder, name, values);
		}
	}
	
	public static double [] getStepArray(int numSteps) {
		double[] steps = new double [numSteps];
		
		for (int i=0; i<numSteps; i++) {
			steps[i]=i+1;
		}
		
		return steps;
	}
	
	public static void exportValues(String folder, String name, double[][] values) {
		SimpleMatrix m = new SimpleMatrix(values);
		double[] baseArray = m.transpose().getMatrix().data;
		
		double[][] matrix = new double[values[0].length][values.length];
		int count =0;
		for (int i=0; i<matrix.length; i++) {
			for (int j=0; j<matrix[i].length; j++) {
				matrix[i][j]=baseArray[count];
				count++;
			}
		}
		String fileName = folder + name + ".csv";
		try {
			CSVFileUtils.writeDoubleTwoDimArrayToCSV(fileName, matrix, ',');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
