package script;

import java.io.IOException;

import com.google.gson.Gson;
import com.zioabm.beans.CalibrationConfig;

import util.io.CSVFileUtils;

public class IncludeWOMVolume {

	public static void main(String[] args) {
		if(args.length==3) {		
			try {
				String config = CSVFileUtils.readFile(args[0]);
			
				Gson gson = new Gson();
				
				//Read configuration from JSON
				CalibrationConfig calibrationConfig = gson.fromJson(config, CalibrationConfig.class);
	
				double[][] volumeHistory = CSVFileUtils.readDoubleTwoDimArrayFromCSV(args[1]);
				
				calibrationConfig.setTargetWOMVolumen(volumeHistory);
				calibrationConfig.setTargetWOMVolumenPeriod("WEEKLY");
				calibrationConfig.setWomVolumeWeight(0.5);
				
				calibrationConfig.setAwarenessWeight(0.5);
				
				CSVFileUtils.writeFile(args[2], 
						gson.toJson(calibrationConfig, CalibrationConfig.class));
			
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Usage: input_json new_history.csv output_json");
			System.exit(1);
		}
	}

}
