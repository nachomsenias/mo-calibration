package script.generation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math.stat.StatUtils;
import org.uma.jmetal.solution.IntegerDoubleSolution;

import com.google.gson.Gson;
import com.zioabm.beans.CalibrationConfig;
import com.zioabm.beans.SimulationConfig;
import com.zioabm.beans.SimulationResult;

import model.ModelDefinition;
import model.ModelRunner;
import problem.MWomABMProblem;
import script.GetTimeSeriesFromOutput;
import util.exception.calibration.CalibrationException;
import util.functions.MatrixFunctions;
import util.io.CSVFileUtils;
import util.io.StatisticsRecordingBean;
import util.random.RandomizerUtils;
import util.statistics.MonteCarloStatistics;

public class GenerateInstanceFromBaseline {

	/*
	 * Ejemplo: cojo el modelo y para la instancia de 45TPs, le pongo un awareness decay muy alto 
	 * y los AW Impact de 3 touchpoints importantes por inversión, muy altos, 
	 * para generar picos puntiagudos. 
	 * Otra instancia con Aw impact medio, pero mucho peso al Aw Impact del WOM y DHeats 
	 * elevados etc...
	 */
	
	private enum Test{
		ALL, DECAY, MEDIUM
	}
	
	public static void main(String[] args) throws IOException, CalibrationException {
		//Problem file
		String jsonFile = args[0];
		
		//Type of modification
		Test mode = Test.valueOf(args[1]);
		
		//Base folder
		String folder = args[2];
		
		String config = CSVFileUtils.readFile(jsonFile);
		
		if(mode.equals(Test.ALL)) {
			for (Test newMode : Test.values()) {
				if(newMode.equals(Test.ALL)) {
					continue;
				}
				modifyBaseline(config, newMode, folder);
			}
		} else {
			modifyBaseline(config, mode, folder);
		}
		
	}
	
	
	private static void modifyBaseline(
			String config, 
			Test mode,
			String outfolder
		) {
		
		Gson gson = new Gson();
		
		// Hay que recargar el baseline cada vez que se va a modificar para
		// no machacar los cambios de unos y otros modelos modificados.
		CalibrationConfig calibrationConfig = gson.fromJson(config, 
				CalibrationConfig.class);
		
		SimulationConfig simConfig = calibrationConfig.getSimConfig();
		String newFolder;
		
		int numSegments = simConfig.getnSegments();
		double[][] awarenessImpactValues;		
		
		// ALL, DECAY, MEDIUM
		switch (mode) {
		case DECAY:
			// le pongo un awareness decay muy alto 
			//* y los AW Impact de 3 touchpoints importantes por inversión, muy altos,
						
			double awarenessDecay = 0.2;
			double[] newAwarenessDecay = new double[numSegments];
			Arrays.fill(newAwarenessDecay, awarenessDecay);
			simConfig.setAwarenessDecay(newAwarenessDecay);
			
			Integer[] mostImportant = getMostImportantTouchPoints(simConfig, 3);
			double highAwarenessValue = 0.2;
			
			awarenessImpactValues = simConfig.getTouchPointsAwarenessImpact();
			for (Integer tp : mostImportant) {
				// By touchpoint by segment
				for (int s=0; s<numSegments; s++) {
					awarenessImpactValues[tp][s] = highAwarenessValue;
				}
			}
			simConfig.setTouchPointsAwarenessImpact(awarenessImpactValues);
			
			newFolder = createFolder(outfolder, String.valueOf(Test.DECAY));
			
			break;
		case MEDIUM:
			// Otra instancia con Aw impact medio, pero mucho peso al Aw Impact del WOM y 
			// DHeats elevados etc...
			
			awarenessDecay = 0.03;
			newAwarenessDecay = new double[numSegments];
			Arrays.fill(newAwarenessDecay, awarenessDecay);
			simConfig.setAwarenessDecay(newAwarenessDecay);
			
			double mediumAwarenessValue = 0.001;
			Random rng = new Random(RandomizerUtils.PRIME_SEEDS[0]);
			
			awarenessImpactValues = simConfig.getTouchPointsAwarenessImpact();
			
			double[][] dhImpactValues = simConfig.getTouchPointsDiscusionHeatImpact();
			double[][] dhDecayValues = simConfig.getTouchPointsDiscusionHeatDecay();
			
			double highDH = 0.2;
			double dhDecay= 0.3;
			
			for (int tp=0; tp<simConfig.getnTp(); tp++) {
				for (int s=0; s<numSegments; s++) {
					double awValue = mediumAwarenessValue*(rng.nextInt(10)+1);
					awarenessImpactValues[tp][s] = awValue;
					dhImpactValues[tp][s] = highDH;
					dhDecayValues[tp][s] = dhDecay;
				}
			}
			
			simConfig.setTouchPointsAwarenessImpact(awarenessImpactValues);
			simConfig.setTouchPointsDiscusionHeatImpact(dhImpactValues);
			simConfig.setTouchPointsDiscusionHeatDecay(dhDecayValues);
			
			double womImpact = 0.03;
			double[] womImpactValues = simConfig.getWomAwarenessImpact();
			Arrays.fill(womImpactValues, womImpact);
			
			simConfig.setWomAwarenessImpact(womImpactValues);
			
			newFolder = createFolder(outfolder, String.valueOf(Test.MEDIUM));
			break;

		default:
			System.out.println("WARNING!: Unrecogniced mode!");
			throw new IllegalArgumentException();
		}
		
		SimulationResult newResult = null;
		
		try {
			newResult=simulateBaseline(calibrationConfig, newFolder);
		} catch (IOException e) {
			System.out.println("ERROR!: Cannot store simulation results!");
			e.printStackTrace();
		} catch (CalibrationException e) {
			System.out.println("ERROR!: Issues simulating the modified model!");
			e.printStackTrace();
		}
		
		try {
			storeModified(calibrationConfig, newFolder, newResult);
		} catch (IOException e) {
			System.out.println("ERROR!: Cannot store modified file!");
			e.printStackTrace();
		} catch (CalibrationException e) {
			System.out.println("ERROR!: Cannot store values!");
			e.printStackTrace();
		}
		
		
	}
	
	private static Integer[] getMostImportantTouchPoints(SimulationConfig simConfig, int howMany) {
		ArrayList<Integer> mostImportant = new ArrayList<Integer>();
		//touch point, brand, segment and week.
		double [][][][] grp = simConfig.getTouchPointsGRPMarketingPlan();
		
		double[] totalValues = new double[simConfig.getnTp()];
		for (int g=0; g<grp.length; g++) {
			totalValues[g] = addMatrix(grp[g]);
		}
		
		int i=0;
		while (i<3) {
			double max = StatUtils.max(totalValues);
			int index = ArrayUtils.indexOf(totalValues,max);
			mostImportant.add(index);
			totalValues[index]=0;
			
			i++;
		}
		
		Integer[] selected = new Integer[0];
		return mostImportant.toArray(selected);
	}

	private static String createFolder(String baselineFolder, String name) {
		String newFolder = baselineFolder+name+"/";
		
		File folderFile = new File(newFolder);
		if(!folderFile.mkdir()) {
			System.out.println("WARNING!: Cannot create folder!");
		}
		
		return newFolder;
	}
	
	private static double[][] transformTarget(double[][][] values) {
		double[][] newValues = new double[values.length][];
		for (int i=0; i<values.length; i++) {
			newValues[i] = new double [values[i][0].length];
			for (int j=0; j<values[i][0].length; j++) {
				newValues[i][j]=values[i][0][j]*100;
			}
		}
		return newValues;
	}
	
	private static void storeModified(CalibrationConfig calibrationConfig, 
			String folder, SimulationResult newResult) 
					throws IOException, CalibrationException {
		Gson gson = new Gson();
		
		double[][] newTargetValues = transformTarget(newResult.awarenessByBrandBySegByStepAvg);
		calibrationConfig.setTargetAwareness(newTargetValues);
		
		String output = gson.toJson(calibrationConfig, CalibrationConfig.class);
		
		//Export JSON results to File
		FileWriter fw = new FileWriter(folder+"test-instance.json");
		fw.write(output);
		fw.close();
		
		MWomABMProblem problem = new MWomABMProblem(calibrationConfig);
		IntegerDoubleSolution solution = problem.getBaseline();
		List<Number> values = solution.getVariables();
		
		output = values.toString();
		output = StringUtils.substring(output, 1, output.length()-1);
		fw = new FileWriter(folder+"target-values.out");
		fw.write(output);
		fw.close();
		
	}
	
	private static SimulationResult simulateBaseline(CalibrationConfig calibrationConfig, String folder) 
			throws CalibrationException, IOException {
		Gson gson = new Gson();
		
		MWomABMProblem problem = new MWomABMProblem(calibrationConfig);
		
		StatisticsRecordingBean recordingBean = 
				calibrationConfig.getSimConfig().getStatisticRecordingConfiguration();
		
		ModelDefinition md = problem.getMD();
		
//		int mc = calibrationConfig.getSimConfig().getnMC();
		// Fixed to 1 MC
		int mc = 1;
		calibrationConfig.getSimConfig().setnMC(1);
		
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
		
		int brands = calibrationConfig.getSimConfig().getnBrands();
		int steps = calibrationConfig.getSimConfig().getnWeeks();
		storeTimeSeries(newResult, folder, brands, steps);
		
		return newResult;
	}

	private static void storeTimeSeries(SimulationResult newResult, 
			String folder, int brands, int steps) {
		
		double[][][] minAwareness = MatrixFunctions.scaleCopyOfDouble3dMatrix(
				newResult.awarenessByBrandBySegByStepMin, 100);
		double[][][] maxAwareness = MatrixFunctions.scaleCopyOfDouble3dMatrix(
				newResult.awarenessByBrandBySegByStepMax,100);
		double[][][] avgAwareness = MatrixFunctions.scaleCopyOfDouble3dMatrix(
				newResult.awarenessByBrandBySegByStepAvg,100);
		
		//Load values
		for (int b=0; b<brands; b++) {
			double[][] simValues = new double[4][]; 
			
			simValues[0] = GetTimeSeriesFromOutput.getStepArray(steps);
			simValues[1] = minAwareness[b][0];
			simValues[2] = maxAwareness[b][0];
			simValues[3] = avgAwareness[b][0];
			
			String name = "awareness_area_b"+(b+1);
			GetTimeSeriesFromOutput.exportValues(folder, name, simValues);
			
		}
		
		double[][][] minWOM = newResult.womVolumeByBrandBySegByStepMin;
		double[][][] maxWOM = newResult.womVolumeByBrandBySegByStepMax;
		double[][][] avgWOM = newResult.womVolumeByBrandBySegByStepAvg;
		
		for (int b=0; b<brands; b++) {
			double[][] simValues = new double[4][]; 
			
			simValues[0] = GetTimeSeriesFromOutput.getStepArray(steps);
			simValues[1] = minWOM[b][0];
			simValues[2] = maxWOM[b][0];
			simValues[3] = avgWOM[b][0];
			
			String name = "wom_area_b"+(b+1);
			GetTimeSeriesFromOutput.exportValues(folder, name, simValues);
		}
	}
	
	private static double addMatrix(double[][][] values) {
		double sum = 0;
		for (int i=0; i<values.length; i++) {
			for (int j=0; j<values[i].length; j++) {
				sum += StatUtils.sum(values[i][j]);
			}
		}
		return sum;
	}
}
