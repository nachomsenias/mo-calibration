package script;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.singleobjective.HC;
import org.uma.jmetal.algorithm.singleobjective.evolutionstrategy.IntCovarianceMatrixAdaptationEvolutionStrategy.IntCMAESBuilder;
import org.uma.jmetal.problem.IntegerProblem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import com.google.gson.Gson;
import com.opencsv.CSVWriter;
import com.zioabm.beans.CalibrationConfig;
import com.zioabm.util.ExecuteCalibration.Options;

import problem.SingleABMProblem;
import util.exception.calibration.CalibrationException;
import util.io.CSVFileUtils;
import util.random.RandomizerUtils;

public class IntegerExperimentRunner extends AbstractAlgorithmRunner {

	public static void main(String[] args)
			throws IOException, CalibrationException {
		if (args.length < 3) {
			System.out.println(
					"Usage: input_json result_folder --algorithm=method "
							+ "{ --start=seedIndex --repeate=mcIterations --hold-out=[0, 1]}");
			System.exit(1);
		}

		String ccFile = CSVFileUtils.readFile(args[0]);
		String experimentBaseDirectory = args[1];
		Gson gson = new Gson();

		// Read configuration from JSON
		CalibrationConfig calibrationConfig = gson.fromJson(ccFile,
				CalibrationConfig.class);

		// Options
		Options customOptions = Options.parseOptions(args);
		String methodName = customOptions.algorithm;
		int iterationStart = customOptions.repeatedIterationsStart;
		int iterations = customOptions.repeatedIterations;

		int maxEvaluations = 10000;
		if (customOptions.evaluations > 0) {
			maxEvaluations = customOptions.evaluations;
		}

		if (customOptions.holdOut > 0.0) {
			calibrationConfig.setHoldOut(customOptions.holdOut);
		}

		SingleABMProblem problem = new SingleABMProblem(calibrationConfig);
		
		List<Algorithm<IntegerSolution>> algorithms = new ArrayList<>();
		
		switch (methodName) {
		case "CMA-ES":
			algorithms.addAll(
					getCMAES(iterations, iterationStart, problem, 
							maxEvaluations, false));
			break;
		case "CMA-ES-HC":
			algorithms.addAll(
					getCMAES(iterations, iterationStart, problem, 
							maxEvaluations, true));
			break;
		case "HC":
			algorithms.addAll(
					getHC(iterations, iterationStart, problem, 
							maxEvaluations));
			break;
		default:
			throw new IllegalArgumentException(
					"Unknown method name: " + methodName);
		}
		
		String signature =experimentBaseDirectory +"/"+ methodName +
				"_"+String.valueOf(System.currentTimeMillis())+ ".csv";
		CSVWriter writer = new CSVWriter(new FileWriter(signature));
		
		for (int it=0; it<iterations; it++) {
			
			int currentIteration = it+iterationStart;
			
			JMetalRandom.getInstance().setSeed(RandomizerUtils.PRIME_SEEDS[currentIteration]);
			
			System.out.println("Beginning iteration: "+currentIteration);
			
			Algorithm<IntegerSolution> algorithm = algorithms.get(it); 
			AlgorithmRunner algorithmRunner = 
					new AlgorithmRunner.Executor(algorithm).execute() ;
			
			IntegerSolution solution = algorithm.getResult();
			
			String parsed[] = parseResult(solution, currentIteration); 
			
			System.out.println("Total execution time: "+algorithmRunner.getComputingTime());
			System.out.println(Arrays.toString(parsed));
			
			writer.writeNext(parsed);
			writer.flush();
		}
		writer.close();
	}
	
	private static String[] parseResult(IntegerSolution sol, int it) {
		List<String> values = new ArrayList<String>();
		
		values.add(String.valueOf(it));
		values.add(String.valueOf(sol.getObjectives()[0]));
		List<Integer> chromosome = sol.getVariables();
		for (Integer c : chromosome) {
			values.add(String.valueOf(c));
		}
		
		String[] empty = {};
		
		return values.toArray(empty);
	}

//	private static List<ExperimentAlgorithm<IntegerSolution, IntegerSolution>> configureAlgorithmList(
//			String name,
//			List<ExperimentProblem<IntegerSolution>> problemList,
//			String baseDirectory,
//			int mcIterations, int maxEvaluations) {
//		List<ExperimentAlgorithm<IntegerSolution, IntegerSolution>> algorithms = new ArrayList<>();
//
//		int populationSize = 100;
//		for (int i = 0; i < problemList.size(); i++) {
//
//			switch (name) {
//			case "CMA-ES":
//				algorithms.addAll(getCMAES(mcIterations, problemList.get(i),
//						populationSize, maxEvaluations, baseDirectory));
//				break;
//			default:
//				throw new IllegalArgumentException(
//						"Unknown method name: " + name);
//			}
//		}
//
//		return algorithms;
//	}

	private static List<Algorithm<IntegerSolution>> getCMAES(
			int mcIterations, int mcOffset, IntegerProblem problem,
			int maxEvaluations, boolean hc) {
		List<Algorithm<IntegerSolution>> algorithms = new ArrayList<>();

		for (int mc = 0; mc < mcIterations; mc++) {
			
			IntCMAESBuilder builder = new IntCMAESBuilder(problem);
			//TODO More CMAES options
			builder.setMaxEvaluations(maxEvaluations);
			
			// Since the integer optimization transforms the space from 0,1 into 0,1000,
			// I include this constant for adjusting the behavior of the algorithm
			int integerRange = 1000;
			
			double sig0 = 0.0567475774*integerRange;
			
			//Include initial solution
			double[] initial = ((SingleABMProblem)problem).getInitial();
			
			Algorithm<IntegerSolution> algorithm = 
					builder.setMaxEvaluations(maxEvaluations)
					.setLambda(15)
					.setMu(0.4048636553)
					.setCs(0.568013196)
					.setCc(0.6962138721)
					.setCcov(0.4897816442)
					.setDamps(4.2939071682)
					.setSigma(sig0)
					.setTypicalX(initial)
					.setHC(hc)
					.build(RandomizerUtils.PRIME_SEEDS[mcOffset+mc]);

			// XXX Neighborhood value should be lower for testing
//			algorithms.add(
//					new ExperimentAlgorithm<>(algorithm, "CMAES", problem, mc));
			
			algorithms.add(algorithm);
		}

		return algorithms;
	}
	
	private static List<Algorithm<IntegerSolution>> getHC(
			int mcIterations, int mcOffset, IntegerProblem problem,
			int maxEvaluations) {
		List<Algorithm<IntegerSolution>> algorithms = new ArrayList<>();
		
		for (int mc = 0; mc < mcIterations; mc++) {			
			int evals = 50;
			int rounds = maxEvaluations / evals;
			
			Algorithm<IntegerSolution> algorithm = new HC(mcOffset+mc,rounds, evals, problem);
			algorithms.add(algorithm);
		}

		return algorithms;
	}
}