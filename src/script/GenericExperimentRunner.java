package script;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.gwasfga.GWASFGA;
import org.uma.jmetal.algorithm.multiobjective.ibea.IBEA;
import org.uma.jmetal.algorithm.multiobjective.ibea.mIBEA;
import org.uma.jmetal.algorithm.multiobjective.moead.AbstractMOEAD.FunctionType;
import org.uma.jmetal.algorithm.multiobjective.mombi.MOMBI2;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.pesa2.PESA2;
import org.uma.jmetal.algorithm.multiobjective.pesa2.PESA2Builder;
import org.uma.jmetal.algorithm.multiobjective.smsemoa.SMSEMOABuilder;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.impl.AbstractIntegerDoubleProblem;
import org.uma.jmetal.solution.IntegerDoubleSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentBuilder;
import org.uma.jmetal.util.experiment.component.ExecuteAlgorithms;
import org.uma.jmetal.util.experiment.component.GenerateReferenceParetoSetAndFrontFromDoubleSolutions;
import org.uma.jmetal.util.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.util.experiment.util.ExperimentProblem;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import com.google.gson.Gson;
import com.zioabm.beans.CalibrationConfig;
import com.zioabm.util.ExecuteCalibration.Options;

import algorithm.MOEADIntegerDouble;
import algorithm.NelderMeadMO;
import operators.IntegerDoubleBLXAlphaCrossover;
import operators.IntegerDoubleDifferentialEvolutionCrossover;
import operators.IntegerDoublePolynomialMutation;
import operators.IntegerDoubleSBXCrossover;
import problem.MWomABMProblem;
import util.CustomPopulationMeasure;
import util.CustomSolutionEvaluator;
import util.StoreIndividual;
import util.exception.calibration.CalibrationException;
import util.io.CSVFileUtils;
import util.random.RandomizerUtils;

public class GenericExperimentRunner extends AbstractAlgorithmRunner {

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
		int seedIndex = customOptions.repeatedIterationsStart;
		int mcIterations = customOptions.repeatedIterations;

		int maxEvaluations = 10000;
		if (customOptions.evaluations > 0) {
			maxEvaluations = customOptions.evaluations;
		}

		if (customOptions.holdOut > 0.0) {
			calibrationConfig.setHoldOut(customOptions.holdOut);
		}

		List<ExperimentProblem<IntegerDoubleSolution>> problemList = new ArrayList<>();
		problemList.add(
				new ExperimentProblem<>(new MWomABMProblem(calibrationConfig)));

		List<CustomPopulationMeasure<IntegerDoubleSolution>> measures = new ArrayList<CustomPopulationMeasure<IntegerDoubleSolution>>();

		List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> algorithmList = configureAlgorithmList(
				methodName, problemList, experimentBaseDirectory, measures,
				mcIterations, maxEvaluations);

		JMetalRandom.getInstance()
				.setSeed(RandomizerUtils.PRIME_SEEDS[seedIndex]);

		Experiment<IntegerDoubleSolution, List<IntegerDoubleSolution>> experiment = new ExperimentBuilder<IntegerDoubleSolution, List<IntegerDoubleSolution>>(
				"WOMM").setAlgorithmList(algorithmList)
						.setProblemList(problemList)
						.setExperimentBaseDirectory(experimentBaseDirectory)
						.setOutputParetoFrontFileName("FUN")
						.setOutputParetoSetFileName("VAR")
						.setReferenceFrontDirectory(
								experimentBaseDirectory + "/referenceFronts")
						.setIndependentRuns(mcIterations)
						.build();

		new ExecuteAlgorithms<>(experiment).run();
		new GenerateReferenceParetoSetAndFrontFromDoubleSolutions(experiment)
				.run();

		for (CustomPopulationMeasure<IntegerDoubleSolution> m : measures) {
			m.writePopulation();
		}
	}

	private static List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> configureAlgorithmList(
			String name,
			List<ExperimentProblem<IntegerDoubleSolution>> problemList,
			String baseDirectory,
			List<CustomPopulationMeasure<IntegerDoubleSolution>> measures,
			int mcIterations, int maxEvaluations) {
		List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> algorithms = new ArrayList<>();

		int populationSize = 100;
		int generations = maxEvaluations / populationSize;

		for (int i = 0; i < problemList.size(); i++) {

			CustomPopulationMeasure<IntegerDoubleSolution> measure = new CustomPopulationMeasure<IntegerDoubleSolution>(
					baseDirectory + "/" + name, String.valueOf(i),
					new StoreIndividual<IntegerDoubleSolution>());
			measures.add(measure);

			switch (name) {
			case "MOEAD":
				algorithms.addAll(getMOEAD(mcIterations, problemList.get(i),
						populationSize, maxEvaluations, baseDirectory,
						measures));
				break;
			case "NSGAII":
				algorithms.addAll(getNSGAII(mcIterations, problemList.get(i),
						populationSize, maxEvaluations, baseDirectory,
						measures));
				break;
			case "NSGAII-BLX":
				algorithms.addAll(getNSGAIIBLX(mcIterations, problemList.get(i),
						populationSize, maxEvaluations, baseDirectory,
						measures));
				break;
			case "NSGAII-BLX-exp":
				algorithms.addAll(getNSGAIIBLXExperimentAlpha(mcIterations,
						problemList.get(i), populationSize, maxEvaluations,
						baseDirectory, measures));
				break;
			case "SPEA2":
				algorithms.addAll(getSPEA2(mcIterations, problemList.get(i),
						populationSize, generations, baseDirectory, measures));
				break;
			case "SPEA2-BLX":
				algorithms.addAll(getSPEA2BLX(mcIterations, problemList.get(i),
						populationSize, generations, baseDirectory, measures));
				break;
			case "PESA2":
				algorithms.addAll(getPESA2(mcIterations, problemList.get(i),
						populationSize, maxEvaluations, baseDirectory,
						measures));
				break;
			case "MOMBI2":
				algorithms.addAll(getMOMBI2(mcIterations, problemList.get(i),
						populationSize, generations, baseDirectory, measures));
				break;
			case "IBEA":
				algorithms.addAll(getIBEA(mcIterations, problemList.get(i),
						populationSize, maxEvaluations, baseDirectory,
						measures));
				break;
			case "mIBEA":
				algorithms.addAll(getmIBEA(mcIterations, problemList.get(i),
						populationSize, maxEvaluations, baseDirectory,
						measures));
				break;
			case "SMSEMOA":
				algorithms.addAll(getSMSEMOA(mcIterations, problemList.get(i),
						populationSize, maxEvaluations, baseDirectory,
						measures));
				break;
			case "GWASFGA":
				algorithms.addAll(getGWASFGA(mcIterations, problemList.get(i),
						populationSize, generations, baseDirectory, measures));
				break;
			case "NELDER-MEAD":
				algorithms.addAll(getNelderMead(mcIterations, problemList.get(i),
						populationSize, generations, baseDirectory, measures));
				break;
			default:
				throw new IllegalArgumentException(
						"Unknown method name: " + name);
			}
		}

		return algorithms;
	}

	private static List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> getMOEAD(
			int mcIterations, ExperimentProblem<IntegerDoubleSolution> problem,
			int populationSize, int maxEvaluations, String baseDirectory,
			List<CustomPopulationMeasure<IntegerDoubleSolution>> measures) {
		List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> algorithms = new ArrayList<>();

		for (int mc = 0; mc < mcIterations; mc++) {
			CustomPopulationMeasure<IntegerDoubleSolution> measure = new CustomPopulationMeasure<IntegerDoubleSolution>(
					baseDirectory + "/MOEAD", String.valueOf(mc),
					new StoreIndividual<IntegerDoubleSolution>());
			measures.add(measure);
			Algorithm<List<IntegerDoubleSolution>> algorithm = new MOEADIntegerDouble(
					problem.getProblem(), populationSize, populationSize,
					maxEvaluations,
					new IntegerDoubleDifferentialEvolutionCrossover(
							(AbstractIntegerDoubleProblem<IntegerDoubleSolution>) (problem
									.getProblem())),
					new IntegerDoublePolynomialMutation(
							1.0 / problem.getProblem().getNumberOfVariables(),
							10.0),
					FunctionType.TCHE, "", 0.1, 2, 20, measure); // Neighborhood:
																	// 20
			// XXX Neighborhood value should be lower for testing
			algorithms.add(
					new ExperimentAlgorithm<>(algorithm, "MOEAD", problem, mc));
		}

		return algorithms;
	}

	private static List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> getNSGAII(
			int mcIterations, ExperimentProblem<IntegerDoubleSolution> problem,
			int populationSize, int maxEvaluations, String baseDirectory,
			List<CustomPopulationMeasure<IntegerDoubleSolution>> measures) {
		List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> algorithms = new ArrayList<>();

		for (int mc = 0; mc < mcIterations; mc++) {
			CustomPopulationMeasure<IntegerDoubleSolution> measure = new CustomPopulationMeasure<IntegerDoubleSolution>(
					baseDirectory + "/NSGAII", String.valueOf(mc),
					new StoreIndividual<IntegerDoubleSolution>());
			measures.add(measure);
			Algorithm<List<IntegerDoubleSolution>> algorithm = new NSGAIIBuilder<IntegerDoubleSolution>(
					problem.getProblem(),
					new IntegerDoubleSBXCrossover(1.0, 5,
							(AbstractIntegerDoubleProblem<IntegerDoubleSolution>) problem
									.getProblem()),
					new IntegerDoublePolynomialMutation(
							1.0 / problem.getProblem().getNumberOfVariables(),
							10.0),
					populationSize)
							.setMaxEvaluations(maxEvaluations)
							.setSolutionListEvaluator(
									new CustomSolutionEvaluator<IntegerDoubleSolution>(
											measure))
							.build();
			assert (algorithm != null);
			algorithms.add(new ExperimentAlgorithm<>(algorithm, "NSGAII",
					problem, mc));
		}

		return algorithms;
	}

	private static List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> getNSGAIIBLX(
			int mcIterations, ExperimentProblem<IntegerDoubleSolution> problem,
			int populationSize, int maxEvaluations, String baseDirectory,
			List<CustomPopulationMeasure<IntegerDoubleSolution>> measures) {
		List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> algorithms = new ArrayList<>();

		for (int mc = 0; mc < mcIterations; mc++) {
			CustomPopulationMeasure<IntegerDoubleSolution> measure = new CustomPopulationMeasure<IntegerDoubleSolution>(
					baseDirectory + "/NSGAII-BLX", String.valueOf(mc),
					new StoreIndividual<IntegerDoubleSolution>());
			measures.add(measure);
			Algorithm<List<IntegerDoubleSolution>> algorithm = new NSGAIIBuilder<IntegerDoubleSolution>(
					problem.getProblem(),
					new IntegerDoubleBLXAlphaCrossover(0.6, 0.5,
							(AbstractIntegerDoubleProblem<IntegerDoubleSolution>) problem
									.getProblem()),
					new IntegerDoublePolynomialMutation(
							1.0 / problem.getProblem().getNumberOfVariables(),
							10.0),
					populationSize)
							.setMaxEvaluations(maxEvaluations)
							.setSolutionListEvaluator(
									new CustomSolutionEvaluator<IntegerDoubleSolution>(
											measure))
							.build();
			assert (algorithm != null);
			algorithms.add(new ExperimentAlgorithm<>(algorithm, "NSGAII-BLX",
					problem, mc));
		}

		return algorithms;
	}

	private static List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> getNSGAIIBLXExperimentAlpha(
			int mcIterations, ExperimentProblem<IntegerDoubleSolution> problem,
			int populationSize, int maxEvaluations, String baseDirectory,
			List<CustomPopulationMeasure<IntegerDoubleSolution>> measures) {
		List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> algorithms = new ArrayList<>();
		double[] values = { 0.4, 0.6 };

		for (double value : values) {
			for (int mc = 0; mc < mcIterations; mc++) {
				CustomPopulationMeasure<IntegerDoubleSolution> measure = new CustomPopulationMeasure<IntegerDoubleSolution>(
						baseDirectory + "/NSGAII-BLX" + value,
						String.valueOf(mc),
						new StoreIndividual<IntegerDoubleSolution>());
				measures.add(measure);
				Algorithm<List<IntegerDoubleSolution>> algorithm = new NSGAIIBuilder<IntegerDoubleSolution>(
						problem.getProblem(),
						new IntegerDoubleBLXAlphaCrossover(0.6, value,
								(AbstractIntegerDoubleProblem<IntegerDoubleSolution>) problem
										.getProblem()),
						new IntegerDoublePolynomialMutation(1.0
								/ problem.getProblem().getNumberOfVariables(),
								10.0),
						populationSize)
								.setMaxEvaluations(maxEvaluations)
								.setSolutionListEvaluator(
										new CustomSolutionEvaluator<IntegerDoubleSolution>(
												measure))
								.build();
				assert (algorithm != null);
				algorithms.add(new ExperimentAlgorithm<>(algorithm,
						"NSGAII-BLX-" + value, problem, mc));
			}
		}

		return algorithms;
	}

	private static List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> getSPEA2(
			int mcIterations, ExperimentProblem<IntegerDoubleSolution> problem,
			int populationSize, int generations, String baseDirectory,
			List<CustomPopulationMeasure<IntegerDoubleSolution>> measures) {
		List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> algorithms = new ArrayList<>();

		for (int mc = 0; mc < mcIterations; mc++) {
			CustomPopulationMeasure<IntegerDoubleSolution> measure = new CustomPopulationMeasure<IntegerDoubleSolution>(
					baseDirectory + "/SPEA2", String.valueOf(mc),
					new StoreIndividual<IntegerDoubleSolution>());
			measures.add(measure);
			Algorithm<List<IntegerDoubleSolution>> algorithm = new SPEA2Builder<>(
					problem.getProblem(),
					new IntegerDoubleSBXCrossover(1.0, 5,
							(AbstractIntegerDoubleProblem<IntegerDoubleSolution>) problem
									.getProblem()),
					new IntegerDoublePolynomialMutation(
							1.0 / problem.getProblem().getNumberOfVariables(),
							10.0)).setMaxIterations(generations)
									.setSolutionListEvaluator(
											new CustomSolutionEvaluator<IntegerDoubleSolution>(
													measure))
									.setPopulationSize(populationSize).build();
			assert (algorithm != null);
			algorithms.add(
					new ExperimentAlgorithm<>(algorithm, "SPEA2", problem, mc));
		}

		return algorithms;
	}
	
	private static List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> getSPEA2BLX(
			int mcIterations, ExperimentProblem<IntegerDoubleSolution> problem,
			int populationSize, int generations, String baseDirectory,
			List<CustomPopulationMeasure<IntegerDoubleSolution>> measures) {
		List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> algorithms = new ArrayList<>();

		for (int mc = 0; mc < mcIterations; mc++) {
			CustomPopulationMeasure<IntegerDoubleSolution> measure = new CustomPopulationMeasure<IntegerDoubleSolution>(
					baseDirectory + "/SPEA2", String.valueOf(mc),
					new StoreIndividual<IntegerDoubleSolution>());
			measures.add(measure);
			Algorithm<List<IntegerDoubleSolution>> algorithm = new SPEA2Builder<>(
					problem.getProblem(),
					new IntegerDoubleBLXAlphaCrossover(0.6, 0.5,
							(AbstractIntegerDoubleProblem<IntegerDoubleSolution>) problem
									.getProblem()),
					new IntegerDoublePolynomialMutation(
							1.0 / problem.getProblem().getNumberOfVariables(),
							10.0)).setMaxIterations(generations)
									.setSolutionListEvaluator(
											new CustomSolutionEvaluator<IntegerDoubleSolution>(
													measure))
									.setPopulationSize(populationSize).build();
			assert (algorithm != null);
			algorithms.add(
					new ExperimentAlgorithm<>(algorithm, "SPEA2-BLX", problem, mc));
		}

		return algorithms;
	}

	private static List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> getPESA2(
			int mcIterations, ExperimentProblem<IntegerDoubleSolution> problem,
			int populationSize, int maxEvaluations, String baseDirectory,
			List<CustomPopulationMeasure<IntegerDoubleSolution>> measures) {
		List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> algorithms = new ArrayList<>();

		for (int mc = 0; mc < mcIterations; mc++) {
			CustomPopulationMeasure<IntegerDoubleSolution> measure = new CustomPopulationMeasure<IntegerDoubleSolution>(
					baseDirectory + "/PESA2", String.valueOf(mc),
					new StoreIndividual<IntegerDoubleSolution>());
			measures.add(measure);
			Algorithm<List<IntegerDoubleSolution>> algorithm = new PESA2Builder<IntegerDoubleSolution>(
					problem.getProblem(),
					new IntegerDoubleSBXCrossover(1.0, 5,
							(AbstractIntegerDoubleProblem<IntegerDoubleSolution>) problem
									.getProblem()),
					new IntegerDoublePolynomialMutation(
							1.0 / problem.getProblem().getNumberOfVariables(),
							10.0)).setMaxEvaluations(maxEvaluations)
									.setPopulationSize(populationSize)
									.setArchiveSize(populationSize)
									.setSolutionListEvaluator(
											new CustomSolutionEvaluator<IntegerDoubleSolution>(
													measure))
									.build();
			((PESA2<IntegerDoubleSolution>) algorithm)
					.setMaxPopulationSize(populationSize);
			assert (algorithm != null);
			algorithms.add(
					new ExperimentAlgorithm<>(algorithm, "PESA2", problem, mc));
		}

		return algorithms;
	}

	private static List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> getMOMBI2(
			int mcIterations, ExperimentProblem<IntegerDoubleSolution> problem,
			int populationSize, int generations, String baseDirectory,
			List<CustomPopulationMeasure<IntegerDoubleSolution>> measures) {
		List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> algorithms = new ArrayList<>();

		for (int mc = 0; mc < mcIterations; mc++) {
			CustomPopulationMeasure<IntegerDoubleSolution> measure = new CustomPopulationMeasure<IntegerDoubleSolution>(
					baseDirectory + "/MOMBI2", String.valueOf(mc),
					new StoreIndividual<IntegerDoubleSolution>());
			measures.add(measure);
			Algorithm<List<IntegerDoubleSolution>> algorithm = new MOMBI2<>(
					problem.getProblem(), generations,
					new IntegerDoubleSBXCrossover(1.0, 5,
							(AbstractIntegerDoubleProblem<IntegerDoubleSolution>) problem
									.getProblem()),
					new IntegerDoublePolynomialMutation(
							1.0 / problem.getProblem().getNumberOfVariables(),
							10.0),
					new BinaryTournamentSelection<IntegerDoubleSolution>(),
					new CustomSolutionEvaluator<IntegerDoubleSolution>(measure),
					"resources/mombi2-weights/weight/weight_02D_152.sld");
			// This url is needed for the exported jar file.
			// Running from Eclipse requires:
			// mombi2-weights/weight/weight_02D_152.sld"
			((MOMBI2<IntegerDoubleSolution>) algorithm)
					.setMaxPopulationSize(populationSize);
			assert (algorithm != null);
			algorithms.add(new ExperimentAlgorithm<>(algorithm, "MOMBI2",
					problem, mc));
		}

		return algorithms;
	}

	private static List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> getIBEA(
			int mcIterations, ExperimentProblem<IntegerDoubleSolution> problem,
			int populationSize, int maxEvaluations, String baseDirectory,
			List<CustomPopulationMeasure<IntegerDoubleSolution>> measures) {
		List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> algorithms = new ArrayList<>();

		for (int mc = 0; mc < mcIterations; mc++) {
			CustomPopulationMeasure<IntegerDoubleSolution> measure = new CustomPopulationMeasure<IntegerDoubleSolution>(
					baseDirectory + "/IBEA", String.valueOf(mc),
					new StoreIndividual<IntegerDoubleSolution>());
			measures.add(measure);
			Algorithm<List<IntegerDoubleSolution>> algorithm = new IBEA<>(
					problem.getProblem(), populationSize, populationSize,
					maxEvaluations,
					new BinaryTournamentSelection<IntegerDoubleSolution>(),
					new IntegerDoubleSBXCrossover(1.0, 5,
							(AbstractIntegerDoubleProblem<IntegerDoubleSolution>) problem
									.getProblem()),
					new IntegerDoublePolynomialMutation(
							1.0 / problem.getProblem().getNumberOfVariables(),
							10.0));
			assert (algorithm != null);
			algorithms.add(
					new ExperimentAlgorithm<>(algorithm, "IBEA", problem, mc));
		}

		return algorithms;
	}

	private static List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> getmIBEA(
			int mcIterations, ExperimentProblem<IntegerDoubleSolution> problem,
			int populationSize, int maxEvaluations, String baseDirectory,
			List<CustomPopulationMeasure<IntegerDoubleSolution>> measures) {
		List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> algorithms = new ArrayList<>();

		for (int mc = 0; mc < mcIterations; mc++) {
			CustomPopulationMeasure<IntegerDoubleSolution> measure = new CustomPopulationMeasure<IntegerDoubleSolution>(
					baseDirectory + "/IBEA", String.valueOf(mc),
					new StoreIndividual<IntegerDoubleSolution>());
			measures.add(measure);
			Algorithm<List<IntegerDoubleSolution>> algorithm = new mIBEA<>(
					problem.getProblem(), populationSize, populationSize,
					maxEvaluations,
					new BinaryTournamentSelection<IntegerDoubleSolution>(),
					new IntegerDoubleSBXCrossover(1.0, 5,
							(AbstractIntegerDoubleProblem<IntegerDoubleSolution>) problem
									.getProblem()),
					new IntegerDoublePolynomialMutation(
							1.0 / problem.getProblem().getNumberOfVariables(),
							10.0));
			assert (algorithm != null);
			algorithms.add(
					new ExperimentAlgorithm<>(algorithm, "mIBEA", problem, mc));
		}

		return algorithms;
	}

	private static List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> getSMSEMOA(
			int mcIterations, ExperimentProblem<IntegerDoubleSolution> problem,
			int populationSize, int maxEvaluations, String baseDirectory,
			List<CustomPopulationMeasure<IntegerDoubleSolution>> measures) {
		List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> algorithms = new ArrayList<>();

		for (int mc = 0; mc < mcIterations; mc++) {
			CustomPopulationMeasure<IntegerDoubleSolution> measure = new CustomPopulationMeasure<IntegerDoubleSolution>(
					baseDirectory + "/SMSEMOA", String.valueOf(mc),
					new StoreIndividual<IntegerDoubleSolution>());
			measures.add(measure);
			Algorithm<List<IntegerDoubleSolution>> algorithm = new SMSEMOABuilder<>(
					problem.getProblem(),
					new IntegerDoubleSBXCrossover(1.0, 5,
							(AbstractIntegerDoubleProblem<IntegerDoubleSolution>) problem
									.getProblem()),
					new IntegerDoublePolynomialMutation(
							1.0 / problem.getProblem().getNumberOfVariables(),
							10.0)).setPopulationSize(populationSize)
									.setMaxEvaluations(maxEvaluations).build();
			assert (algorithm != null);
			algorithms.add(new ExperimentAlgorithm<>(algorithm, "SMSEMOA",
					problem, mc));
		}

		return algorithms;
	}

	private static List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> getGWASFGA(
			int mcIterations, ExperimentProblem<IntegerDoubleSolution> problem,
			int populationSize, int generations, String baseDirectory,
			List<CustomPopulationMeasure<IntegerDoubleSolution>> measures) {
		List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> algorithms = new ArrayList<>();
		double epsilon = 0.01;
		for (int mc = 0; mc < mcIterations; mc++) {
			CustomPopulationMeasure<IntegerDoubleSolution> measure = new CustomPopulationMeasure<IntegerDoubleSolution>(
					baseDirectory + "/GWASFGA", String.valueOf(mc),
					new StoreIndividual<IntegerDoubleSolution>());
			measures.add(measure);
			Algorithm<List<IntegerDoubleSolution>> algorithm = new GWASFGA<>(
					problem.getProblem(), populationSize, generations,
					new IntegerDoubleSBXCrossover(1.0, 5,
							(AbstractIntegerDoubleProblem<IntegerDoubleSolution>) problem
									.getProblem()),
					new IntegerDoublePolynomialMutation(
							1.0 / problem.getProblem().getNumberOfVariables(),
							10.0),
					new BinaryTournamentSelection<IntegerDoubleSolution>(
							new RankingAndCrowdingDistanceComparator<IntegerDoubleSolution>()),
					new CustomSolutionEvaluator<IntegerDoubleSolution>(measure),
					epsilon);
			// This url is needed for the exported jar file.
			// Running from Eclipse requires:
			// mombi2-weights/weight/weight_02D_152.sld"
			((GWASFGA<IntegerDoubleSolution>) algorithm)
					.setMaxPopulationSize(populationSize);
			assert (algorithm != null);
			algorithms.add(new ExperimentAlgorithm<>(algorithm, "GWASFGA",
					problem, mc));
		}

		return algorithms;
	}
	
	private static List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> getNelderMead(
			int mcIterations, ExperimentProblem<IntegerDoubleSolution> problem,
			int samples, int sampleEvaluations, String baseDirectory,
			List<CustomPopulationMeasure<IntegerDoubleSolution>> measures) {
		// TODO Chapuza para aumentar las evaluaciones y reducir los samples.
		samples /= 2;
		sampleEvaluations *=2; 
		// *******
		List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> algorithms = new ArrayList<>();
		double epsilon = 0.01;
		for (int mc = 0; mc < mcIterations; mc++) {
			CustomPopulationMeasure<IntegerDoubleSolution> measure = 
					new CustomPopulationMeasure<IntegerDoubleSolution>(
						baseDirectory + "/NELDER-MEAD", String.valueOf(mc),
						new StoreIndividual<IntegerDoubleSolution>());
			measures.add(measure);
			Algorithm<List<IntegerDoubleSolution>> algorithm = 
					new NelderMeadMO(problem.getProblem(), samples, sampleEvaluations, epsilon);
			assert (algorithm != null);
			algorithms.add(new ExperimentAlgorithm<>(algorithm, "NELDER-MEAD",
					problem, mc));
		}

		return algorithms;
	}
}

