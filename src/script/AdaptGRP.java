package script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.zioabm.beans.CalibrationConfig;
import com.zioabm.beans.SimulationConfig;

import model.touchpoints.TouchPointOwned.InvestmentType;
import util.functions.MatrixFunctions;
import util.io.CSVFileUtils;

public class AdaptGRP {

	public static void main(String[] args) throws IOException {
		String jsonFile = args[0];
		
		//Read the JSON file
		BufferedReader br = new BufferedReader(new FileReader(jsonFile));
		
		StringBuilder buffer = new StringBuilder();
		String line;
		while((line = br.readLine())!=null) {
			buffer.append(line);
		}
		br.close();
		
		String config = buffer.toString();
		
		//Get ModelDefinition
		Gson gson = new Gson();
		
		CalibrationConfig calibrationConfig = gson.fromJson(config, 
				CalibrationConfig.class);

		SimulationConfig simConfig = calibrationConfig.getSimConfig();
		
		double[][][][] grp = simConfig.getTouchPointsGRPMarketingPlan();
		
		double scale = (simConfig.getNumberOfAgents()*1000.0/simConfig.getPopulationSize())
				/(simConfig.getNumberOfAgents()*0.01);
		
		InvestmentType[] investment = simConfig.getTouchPointsInvestment();
		
		for (int tp = 0; tp< grp.length; tp++) {
			investment[tp] = InvestmentType.GRP;
			for (int b=0; b<grp[tp].length; b++) {
				grp[tp][b] = MatrixFunctions.scaleCopyOfDoubleMatrix(grp[tp][b], scale);
			}
		}
		
		CSVFileUtils.writeFile(args[1], 
				gson.toJson(calibrationConfig, CalibrationConfig.class));
	}

}
