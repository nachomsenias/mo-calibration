package script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.uma.jmetal.solution.impl.DefaultIntegerDoubleSolution;

import com.google.gson.Gson;
import com.zioabm.beans.CalibrationConfig;
import com.zioabm.beans.SimulationConfig;

import model.ModelDefinition;
import problem.MWomABMProblem;
import util.exception.calibration.CalibrationException;

public class EvaluateJSON {

	public static void main(String[] args) throws IOException, CalibrationException {
		if(args.length!=3) {
			throw new IllegalArgumentException("JSON to Zio uses 3 arguments: "
					+ "calibration_config base_sol output_json");
		}
		
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
		
		jsonFile = args[1];

		//Start simulation
		
		//Read the JSON file
		br = new BufferedReader(new FileReader(jsonFile));
		
		buffer = new StringBuilder();
		while((line = br.readLine())!=null) {
			buffer.append(line);
		}
		br.close();
		
		config = buffer.toString();
		
		SimulationConfig givenConfig = gson.fromJson(config, 
				SimulationConfig.class);
		
		ModelDefinition md = givenConfig.getModelDefinition();
		System.out.println(md.hashCode());
		
//		calibrationConfig.setSimConfig(givenConfig);
//		CalibrationResponse response = new CalibrationResponse();
//		
//		EvaluationWorker ew = new EvaluationWorker(calibrationConfig, response);
//		
//		ew.run();
		
//		String output = gson.toJson(response, CalibrationResponse.class);
//			
//		//Export to File
//		FileWriter fw = new FileWriter(args[2]);
//		fw.write(output);
//		fw.close();
		
		
//		SimpleABMProblem sproblem = new SimpleABMProblem(calibrationConfig);
//		WomABMProblem sproblem = new WomABMProblem(calibrationConfig);
		MWomABMProblem sproblem = new MWomABMProblem(calibrationConfig);
		
		DefaultIntegerDoubleSolution solution = new DefaultIntegerDoubleSolution(sproblem);
		
//		double [] values = {
//				0.0512614873, 0.949812892, 0.5500566719, 0.3010279372,
//				0.5107623641, 0.7536654314, 0.5924732364,
//				0.9588757666, 0.0731757697, 0.1260277908, 0.0074051519,
//				0.4650436572, 0.1953538844, 0.2418689469,
//				0.0004924114, 0.712037026, 0.6329656796, 0.4409744043,
//				0.9410214509, 0.122327619, 0.7734814574,
//				0.3197134392, 0.1093886772, 0.3260665508};
		
		//0.7497980605	0.14242914	0.90611675	0.227022675	0.6921522307	0.2520640672	0.6195844977	0.6511223572	0.338838414	0.3775502429	0.18663871	0.6517062217	0.4684143664	0.8018704855	0.9034512234	0.0462029852	0.9164151047	0.5725108691	0.059139347	0.990323471	0.600952122	0.6607004516	0.7470118804	0.8863300532

//		double [] values = {
//				0.7497980605, 0.14242914, 0.90611675, 0.227022675,
//				0.6921522307, 0.2520640672, 0.6195844977, 
//				0.6511223572, 0.338838414, 0.3775502429, 0.18663871, 
//				0.6517062217, 0.4684143664, 0.8018704855, 
//				0.9034512234, 0.0462029852, 0.9164151047, 0.5725108691,
//				0.059139347, 0.990323471, 0.600952122,
//				0.6607004516, 0.7470118804, 0.8863300532};

		
//		double [] values = {
//				0.9227831698389907, 0.19158528467669722, 0.16785983433300622, 0.9154097712143734,
//				0.4054108370346027, 0.0742035499756154, 0.8750631987887855, 
//				0.5275823423169621, 0.441954419042382, 0.056076964818811526, 0.11687339512619155,
//				0.03461474560409028, 0.17959507654720142, 0.08632001435258663,
//				0.07040737850670564, 0.9370837111836181, 0.05078494931775979, 0.5156214508192527,
//				0.3092938622996775, 0.5623554016212138, 0.8017448694520076,
//				0.4798655353855973, 0.09211054477603142, 0.18742886507745438
//				};
		
//		double [] values = {
//				0.0652948594, 0.5239636447, 0.8432888603, 0.0105321077,
//				0.0391046408, 0.7147644858, 0.1360091032, 
//				0.2546520732, 0.8235618851, 0.5173262811, 0.621714644,
//				0.1471173669, 0.2431265685, 0.0989505524, 
//				0.5480161152, 0.5193753319, 0.6102859817, 0.8895400672,
//				0.336681619, 0.2213156633, 0.6969431207, 
//				0.2694220131, 0.0699813032, 0.1668268506
//				};
		
//		double [] values = {
//				0.0982162897, 0.8608860321, 0.3917326511, 0.9590978979,
//				0.3340329427, 0.8889001891, 0.3096957319, 
//				0.4635701419, 0.5018636485, 0.7534919008, 0.7765925879,
//				0.7497070853, 0.7149970188, 0.3731643181, 
//				0.1520528594, 0.6947870584, 0.2647759072, 0.7102883016, 
//				0.2794295902, 0.7969743195, 0.3985628807,
//				0.4865434096, 0.073295379, 0.2901688253
//				};
		
//		for (int v=0; v<values.length; v++) {
//			solution.setVariableValue(v, values[v]);
//		}
		
//		// 3
//		double [] values = {
//				0.0329435626, 0.9262656905, 0.7559379951, 0.834319818,
//				0.0567241968, 0.2887307705, 0.3786622496,
//				0.4863814844, 0.9705473033, 0.5347412737, 0.476895335,
//				0.2880365396, 0.4166752828, 0.9866831613,
//				0.1987306075, 0.7680794143, 0.655877835, 0.5447477284, 
//				0.4085911408, 0.3236694806, 0.3505099816,
//				0.2772616974, 0.1045893842, 0.1712193034
//				};
//		solution.setVariableValue(0, 3);
//		for (int v=0; v<values.length; v++) {
//			solution.setVariableValue(v+1, values[v]);
//		}
		
		//5
		double [] values = {
				0.0155671939, 0.2072618651, 0.7580125375, 0.8873458342,
				0.7481034272, 0.3408743272, 0.8255902246, 
				0.4695916627, 0.2017006295, 0.1440579155, 0.5093200249,
				0.2997653074, 0.3029915025, 0.1458670754, 
				0.7643448414, 0.6865040645, 0.0749589562, 0.2952464672,
				0.4341983738, 0.9899704208, 0.3299098701, 
				0.923291105, 0.3613053161, 0.2928261966
				};
		solution.setVariableValue(0, 5);
		for (int v=0; v<values.length; v++) {
			solution.setVariableValue(v+1, values[v]);
		}
		sproblem.evaluate(solution);
		
		System.out.println(
				sproblem.getMD().hashCode());
		
		System.out.println(
				md.equals(sproblem.getMD()));
		
		System.out.println(solution.getObjective(0)+" "+solution.getObjective(1));
		
//		String out=sproblem.getMD().export();
//		
//		//Export to File
//		FileWriter fw = new FileWriter("out_eval.zio");
//		fw.write(out);
//		fw.close();
	}

}
