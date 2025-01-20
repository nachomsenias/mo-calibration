package script;

import java.io.IOException;

import com.google.gson.Gson;
import com.zioabm.beans.CalibrationConfig;

import util.io.CSVFileUtils;
import util.random.Randomizer;
import util.random.RandomizerFactory;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import util.random.RandomizerUtils;

public class ReplaceHistory {

	public static void main(String[] args) {
		if(args.length==3) {		
			try {
				String config = CSVFileUtils.readFile(args[0]);
			
				Gson gson = new Gson();
				
				//Read configuration from JSON
				CalibrationConfig calibrationConfig = gson.fromJson(config, CalibrationConfig.class);
	
				double[][] newHistory = CSVFileUtils.readDoubleTwoDimArrayFromCSV(args[1]);
				
				calibrationConfig.setTargetAwareness(newHistory);
							
				CSVFileUtils.writeFile(args[2], 
						gson.toJson(calibrationConfig, CalibrationConfig.class));
			
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (args.length==4) {
			try {
				String config = CSVFileUtils.readFile(args[0]);
			
				Gson gson = new Gson();
				
				//Read configuration from JSON
				CalibrationConfig calibrationConfig = gson.fromJson(config, CalibrationConfig.class);
	
				double[][] newHistory = CSVFileUtils.readDoubleTwoDimArrayFromCSV(args[1]);
				
				Randomizer random = RandomizerFactory.createRandomizer(
						RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST, 
						RandomizerUtils.PRIME_SEEDS[0]
						);
				
				//Translate newHistory to WEEKLY
				double[][] weeklyHistory = new double [newHistory.length][newHistory[0].length*4];
				
				for (int b = 0; b<newHistory.length; b++) {
//					weeklyHistory[b][0] = newHistory[b][0];
					double increment = 0.1;
					
					for (int m = 0; m<newHistory[b].length; m++) {
						
						double initialValue = newHistory[b][m];
						
						weeklyHistory[b][m*4] = initialValue;
						
						if (m+1<newHistory[b].length) {
							double interval = newHistory[b][m+1]-initialValue;
							increment = interval/4.0;
						}
						
						for (int w = 1; w<4; w++) {
							double nextWeekly = initialValue + (random.nextDouble() * increment); 
							weeklyHistory[b][(m*4)+w] = nextWeekly;
							
							initialValue+=increment;
						}						
					}
//					previous = newHistory[b][newHistory[b].length-1];
//					//Last step
//					for (int w = 1; w<4; w++) {
//						double nextWeekly = previous + (random.nextDouble() * increment); 
//						weeklyHistory[b][newHistory[b].length-1+w] = nextWeekly;
//						
//						previous = previous+increment;
//					}
					
				}
				
				CSVFileUtils.writeDoubleTwoDimArrayToCSV(args[1]+"_weekly", weeklyHistory, ';');
				
				calibrationConfig.setTargetAwareness(weeklyHistory);
				calibrationConfig.setTargetAwarenessPeriod("WEEKLY");
							
				CSVFileUtils.writeFile(args[2], 
						gson.toJson(calibrationConfig, CalibrationConfig.class));
			
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Usage: input_json new_history.csv output_json");
			System.out.println("******************* OR *********************");
			System.out.println("Usage: input_json new_history.csv output_json WEEKLY");
			System.exit(1);
		}
		
	}

}
