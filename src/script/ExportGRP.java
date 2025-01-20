package script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.opencsv.CSVWriter;
import com.zioabm.beans.CalibrationConfig;
import com.zioabm.beans.SimulationConfig;

public class ExportGRP {

	public static void main(String[] args) throws IOException {
		String json = args[0];
		
		File file = new File(json);
		
		//Read the JSON file
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		StringBuilder buffer = new StringBuilder();
		String line;
		while((line = br.readLine())!=null) {
			buffer.append(line);
		}
		br.close();
		
		String config = buffer.toString();
		
		Gson gson = new Gson();
		
		CalibrationConfig resultConfig = gson.fromJson(config, CalibrationConfig.class);
		
		SimulationConfig simConfig = resultConfig.getSimConfig();
		
		double [][][][] grp = simConfig.getTouchPointsGRPMarketingPlan();
		
		String baseDir = file.getParent();
		
		for (int t = 0; t<simConfig.getnTp(); t++) {
			
			String csvfile = baseDir + "/TP_" + t + ".csv";
			
			// Read CSV values
			CSVWriter writer = new CSVWriter(
				new FileWriter(new File(csvfile)), ' ', CSVWriter.NO_QUOTE_CHARACTER
			);
			
			for (int b = 0; b<simConfig.getnBrands(); b++) {
				for (int s = 0; s<simConfig.getnSegments(); s++) {
					double [] investment = grp[t][b][s];
					
					String[] values = new String[investment.length];
					for (int i = 0; i<investment.length; i++) {
						values [i] = String.valueOf(investment[i]);
					}
					
					writer.writeNext(values);
				}
			}
			writer.close();
		}
		
		
		

	}

}
