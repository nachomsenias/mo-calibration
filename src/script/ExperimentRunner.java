package script;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.gwasfga.GWASFGA;
import org.uma.jmetal.algorithm.multiobjective.ibea.IBEA;
import org.uma.jmetal.algorithm.multiobjective.ibea.mIBEA;
import org.uma.jmetal.algorithm.multiobjective.moead.MOEADBuilder;
import org.uma.jmetal.algorithm.multiobjective.moead.MOEADBuilder.Variant;
import org.uma.jmetal.algorithm.multiobjective.mombi.MOMBI2;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.pesa2.PESA2;
import org.uma.jmetal.algorithm.multiobjective.pesa2.PESA2Builder;
import org.uma.jmetal.algorithm.multiobjective.smsemoa.SMSEMOABuilder;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.operator.impl.crossover.BLXAlphaCrossover;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.solution.DoubleSolution;
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

import problem.WomABMProblem;
import util.CustomPopulationMeasure;
import util.CustomSolutionEvaluator;
import util.StoreIndividual;
import util.exception.calibration.CalibrationException;
import util.io.CSVFileUtils;
import util.random.RandomizerUtils;

public class ExperimentRunner extends AbstractAlgorithmRunner {

	public static void main(String[] args) throws IOException, CalibrationException {
		if(args.length<3) {
			System.out.println("Usage: input_json result_folder --algorithm=method "
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
		if(customOptions.evaluations>0) {
			maxEvaluations = customOptions.evaluations;
		}
		
		if(customOptions.holdOut>0.0) {
			calibrationConfig.setHoldOut(customOptions.holdOut);
		}

		
		List<ExperimentProblem<DoubleSolution>> problemList = new ArrayList<>();
		problemList.add(new ExperimentProblem<>(new WomABMProblem(calibrationConfig)));

		List<CustomPopulationMeasure<DoubleSolution>> measures = 
				new ArrayList<CustomPopulationMeasure<DoubleSolution>>();
		
		List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithmList = 
				configureAlgorithmList(methodName, problemList, 
							experimentBaseDirectory, measures, mcIterations, maxEvaluations);
		
		JMetalRandom.getInstance().setSeed(RandomizerUtils.PRIME_SEEDS[seedIndex]);
		
		Experiment<DoubleSolution, List<DoubleSolution>> experiment = 
				new ExperimentBuilder<DoubleSolution, List<DoubleSolution>>("WOM")
						.setAlgorithmList(algorithmList).setProblemList(problemList)
						.setExperimentBaseDirectory(experimentBaseDirectory)
						.setOutputParetoFrontFileName("FUN")
						.setOutputParetoSetFileName("VAR")
						.setReferenceFrontDirectory(
								experimentBaseDirectory + "/referenceFronts")
						.setIndependentRuns(mcIterations).build();

		new ExecuteAlgorithms<>(experiment).run();
		new GenerateReferenceParetoSetAndFrontFromDoubleSolutions(experiment).run();

		for (CustomPopulationMeasure<DoubleSolution> m : measures) {
			m.writePopulation();
		}
	}

	private static List<ExperimentAlgorithm<DoubleSolution, 
		List<DoubleSolution>>> configureAlgorithmList(
				String name,
			List<ExperimentProblem<DoubleSolution>> problemList,
			String baseDirectory,
			List<CustomPopulationMeasure<DoubleSolution>> measures,
			int mcIterations, 
			int maxEvaluations) {
		List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithms = new ArrayList<>();

		int populationSize = 100;
		int generations = maxEvaluations / populationSize;
		
		//For IBEA and mIBEA algorithms
		int archiveSize = populationSize;

		for (int i = 0; i < problemList.size(); i++) {
			
			CustomPopulationMeasure<DoubleSolution> measure = null;
			Algorithm<List<DoubleSolution>> algorithm = null;
			
			switch (name) {
			case "MOEAD":
				
				//20 for 100 individuals
				int neighborSize = populationSize / 5;
				
				for (int mc=0; mc<mcIterations; mc++) {
				
					measure = new CustomPopulationMeasure<DoubleSolution>(
									baseDirectory + "/" + name, String.valueOf(i),
										new StoreIndividual<DoubleSolution>());
					measures.add(measure);
					
					algorithm = new MOEADBuilder(problemList.get(i).getProblem(),Variant.MOEAD)
								.setMaxEvaluations(maxEvaluations)
								.setPopulationSize(populationSize)
								.setResultPopulationSize(populationSize)
								.setMutation(
										new PolynomialMutation(
												1.0 / problemList.get(i).getProblem().getNumberOfVariables(),
												10.0))
								.setNeighborSize(neighborSize)
								.build();
				
					algorithms.add(new ExperimentAlgorithm<>(algorithm, name,
							problemList.get(i),mc));
				}
				break;
			case "NSGAII":
				
				for (int mc=0; mc<mcIterations; mc++) {
					measure = new CustomPopulationMeasure<DoubleSolution>(
							baseDirectory + "/" + name, String.valueOf(i),
								new StoreIndividual<DoubleSolution>());
					measures.add(measure);
					
					algorithm = new NSGAIIBuilder<DoubleSolution>(
							problemList.get(i).getProblem(),
							new SBXCrossover(1.0, 5),
							new PolynomialMutation(
									1.0 / problemList.get(i).getProblem().getNumberOfVariables(),
									10.0),populationSize)
							.setMaxEvaluations(maxEvaluations)
							.setSolutionListEvaluator(
								new CustomSolutionEvaluator<DoubleSolution>(
									measure))
							.build();
					
					algorithms.add(new ExperimentAlgorithm<>(algorithm, name,
							problemList.get(i),mc));
				}
				
				break;
				
			case "NSGAII-BLX":
				
				for (int mc=0; mc<mcIterations; mc++) {
					measure = new CustomPopulationMeasure<DoubleSolution>(
							baseDirectory + "/" + name, String.valueOf(i),
								new StoreIndividual<DoubleSolution>());
					measures.add(measure);
				
					algorithm = new NSGAIIBuilder<DoubleSolution>(
							problemList.get(i).getProblem(),
							new BLXAlphaCrossover(0.6, 0.5),
							new PolynomialMutation(
									1.0 / problemList.get(i).getProblem().getNumberOfVariables(),
									10.0),populationSize)
							.setMaxEvaluations(maxEvaluations)
							.setSolutionListEvaluator(
								new CustomSolutionEvaluator<DoubleSolution>(
									measure))
							.build();
					
					algorithms.add(new ExperimentAlgorithm<>(algorithm, name,
							problemList.get(i),mc));
				}
				
				break;
				
			case "NSGAII-exp-blx":
				
				double[] probabilities = {0.8, 1.0};
				double[] alphas = {0.4, 0.5, 0.6};
				
				for (double prob : probabilities) {
					for (double alpha : alphas) {
						
						for (int mc=0; mc<mcIterations; mc++) {
							measure = new CustomPopulationMeasure<DoubleSolution>(
									baseDirectory + "/" + name, String.valueOf(i),
										new StoreIndividual<DoubleSolution>());
							measures.add(measure);
							
							algorithm = new NSGAIIBuilder<DoubleSolution>(
									problemList.get(i).getProblem(),
									new BLXAlphaCrossover(prob, alpha),
									new PolynomialMutation(
											1.0 / problemList.get(i).getProblem().getNumberOfVariables(),
											10.0),populationSize)
									.setMaxEvaluations(maxEvaluations)
									.setSolutionListEvaluator(
										new CustomSolutionEvaluator<DoubleSolution>(
											measure))
									.build();
						
							algorithms.add(new ExperimentAlgorithm<>(algorithm, name,
									problemList.get(i),mc));
						}
					}
				}
				
				return algorithms;
				
			case "NSGAII-exp-sbx":
				
//				double[] values = {3,4,6,7};
				double[] values = {4, 7, 9};
				
				for (double value : values) {
					
					for (int mc=0; mc<mcIterations; mc++) {
						measure = new CustomPopulationMeasure<DoubleSolution>(
								baseDirectory + "/" + name, String.valueOf(i),
									new StoreIndividual<DoubleSolution>());
						measures.add(measure);
					
						algorithm = new NSGAIIBuilder<DoubleSolution>(
								problemList.get(i).getProblem(),
								new SBXCrossover(1.0, value),
								new PolynomialMutation(
										1.0 / problemList.get(i).getProblem().getNumberOfVariables(),
										10.0),populationSize)
								.setMaxEvaluations(maxEvaluations)
								.setSolutionListEvaluator(
									new CustomSolutionEvaluator<DoubleSolution>(
										measure))
								.build();
						
						algorithms.add(new ExperimentAlgorithm<>(algorithm, name,
								problemList.get(i),mc));
					}
				}
				
				return algorithms;
				
			case "NSGAII-exp-di":
				
				final double distIndex = 10.0;
				double[] increments = {10,30,50, 90};
				
				for (double inc : increments) {
					
					double dist = distIndex+inc;
					
					for (int mc=0; mc<mcIterations; mc++) {
						measure = new CustomPopulationMeasure<DoubleSolution>(
								baseDirectory + "/" + name, String.valueOf(i),
									new StoreIndividual<DoubleSolution>());
						measures.add(measure);
					
						algorithm = new NSGAIIBuilder<DoubleSolution>(
								problemList.get(i).getProblem(),
								new SBXCrossover(1.0, 5),
								new PolynomialMutation(
										1.0 / problemList.get(i).getProblem().getNumberOfVariables(),
										dist),
								populationSize)
								.setMaxEvaluations(maxEvaluations)
								.setSolutionListEvaluator(
									new CustomSolutionEvaluator<DoubleSolution>(
										measure))
								.build();
						
						algorithms.add(new ExperimentAlgorithm<>(algorithm, name,
								problemList.get(i),mc));
					}
				}
				
				return algorithms;
				
			case "SPEA2":
				
				for (int mc=0; mc<mcIterations; mc++) {
					measure = new CustomPopulationMeasure<DoubleSolution>(
							baseDirectory + "/" + name, String.valueOf(i),
								new StoreIndividual<DoubleSolution>());
					measures.add(measure);
				
					algorithm = new SPEA2Builder<>(
							problemList.get(i).getProblem(),
							new SBXCrossover(1.0, 5),
							new PolynomialMutation(
									1.0 / problemList.get(i).getProblem().getNumberOfVariables(),
									10.0))
							.setMaxIterations(generations)
							.setSolutionListEvaluator(
									new CustomSolutionEvaluator<DoubleSolution>(
											measure))
							.setPopulationSize(populationSize)
							.build();
					
					algorithms.add(new ExperimentAlgorithm<>(algorithm, name,
							problemList.get(i),mc));
				}
				break;
			case "PESA2":
				
				for (int mc=0; mc<mcIterations; mc++) {
					measure = new CustomPopulationMeasure<DoubleSolution>(
							baseDirectory + "/" + name, String.valueOf(i),
								new StoreIndividual<DoubleSolution>());
					measures.add(measure);
				
					algorithm = new PESA2Builder<DoubleSolution>(
							problemList.get(i).getProblem(),
							new SBXCrossover(1.0, 5),
							new PolynomialMutation(
									1.0 / problemList.get(i).getProblem().getNumberOfVariables(),
									10.0))
							.setMaxEvaluations(maxEvaluations)
							.setPopulationSize(populationSize)
							.setArchiveSize(archiveSize)
							.setSolutionListEvaluator(
									new CustomSolutionEvaluator<DoubleSolution>(
											measure))
							.build();
					((PESA2<DoubleSolution>)algorithm)
							.setMaxPopulationSize(populationSize);
					algorithms.add(new ExperimentAlgorithm<>(algorithm, name,
							problemList.get(i),mc));
				}
				
				break;
			case "MOMBI2":
								
				for (int mc=0; mc<mcIterations; mc++) {
					measure = new CustomPopulationMeasure<DoubleSolution>(
							baseDirectory + "/" + name, String.valueOf(i),
								new StoreIndividual<DoubleSolution>());
					measures.add(measure);
					
					algorithm = new MOMBI2<>(
							problemList.get(i).getProblem(),
							generations, 
							new SBXCrossover(1.0, 5),
							new PolynomialMutation(
									1.0 / problemList.get(i).getProblem().getNumberOfVariables(),
									10.0), 
							new BinaryTournamentSelection<DoubleSolution>(),
							new CustomSolutionEvaluator<DoubleSolution>(measure), 
							"resources/mombi2-weights/weight/weight_02D_152.sld");
					//This url is needed for the exported jar file.
					//Running from Eclipse requires: mombi2-weights/weight/weight_02D_152.sld" 
					((MOMBI2<DoubleSolution>)algorithm)
						.setMaxPopulationSize(populationSize);
					
					algorithms.add(new ExperimentAlgorithm<>(algorithm, name,
							problemList.get(i),mc));
				}
				break;
			case "IBEA":

				for (int mc=0; mc<mcIterations; mc++) {
					measure = new CustomPopulationMeasure<DoubleSolution>(
							baseDirectory + "/" + name, String.valueOf(i),
								new StoreIndividual<DoubleSolution>());
					measures.add(measure);
					
					algorithm = new IBEA<>(
							problemList.get(i).getProblem(),
							populationSize, 
							archiveSize, 
							maxEvaluations, 
							new BinaryTournamentSelection<DoubleSolution>(), 
							new SBXCrossover(1.0, 5),
							new PolynomialMutation(
									1.0 / problemList.get(i).getProblem().getNumberOfVariables(),
									10.0)
							);
					
					algorithms.add(new ExperimentAlgorithm<>(algorithm, name,
							problemList.get(i),mc));
				}
				break;
			case "mIBEA":
				for (int mc=0; mc<mcIterations; mc++) {
					measure = new CustomPopulationMeasure<DoubleSolution>(
							baseDirectory + "/" + name, String.valueOf(i),
								new StoreIndividual<DoubleSolution>());
					measures.add(measure);
					
					algorithm = new mIBEA<>(
							problemList.get(i).getProblem(),
							populationSize, 
							archiveSize, 
							maxEvaluations, 
							new BinaryTournamentSelection<DoubleSolution>(), 
							new SBXCrossover(1.0, 5),
							new PolynomialMutation(
									1.0 / problemList.get(i).getProblem().getNumberOfVariables(),
									10.0)
							);
					algorithms.add(new ExperimentAlgorithm<>(algorithm, name,
							problemList.get(i),mc));
				}
				break;
			case "SMSEMOA":
				for (int mc=0; mc<mcIterations; mc++) {
					measure = new CustomPopulationMeasure<DoubleSolution>(
							baseDirectory + "/" + name, String.valueOf(i),
								new StoreIndividual<DoubleSolution>());
					measures.add(measure);
					algorithm = new SMSEMOABuilder<>(
							problemList.get(i).getProblem(), 
							new SBXCrossover(1.0, 5),
							new PolynomialMutation(
									1.0 / problemList.get(i).getProblem().getNumberOfVariables(),
									10.0))
							.setPopulationSize(populationSize)
							.setMaxEvaluations(maxEvaluations)
							.build();
	
					algorithms.add(new ExperimentAlgorithm<>(algorithm, name,
							problemList.get(i),mc));
				}
				break;
			case "GWASFGA":
				
				double epsilon = 0.01;
				
				for (int mc=0; mc<mcIterations; mc++) {
					measure = new CustomPopulationMeasure<DoubleSolution>(
							baseDirectory + "/" + name, String.valueOf(i),
								new StoreIndividual<DoubleSolution>());
					measures.add(measure);
					
					algorithm = new GWASFGA<>(
							problemList.get(i).getProblem(),
							populationSize, 
							generations, 
							new SBXCrossover(1.0, 5),
							new PolynomialMutation(
									1.0 / problemList.get(i).getProblem().getNumberOfVariables(),
									10.0), 
							new BinaryTournamentSelection<DoubleSolution>(
									new RankingAndCrowdingDistanceComparator<DoubleSolution>()), 
							new CustomSolutionEvaluator<DoubleSolution>(
									measure), 
							epsilon);
					
					algorithms.add(new ExperimentAlgorithm<>(algorithm, name,
							problemList.get(i),mc));
				}
				break;
			default:
				break;
			}
			
			
			
		}

		return algorithms;
	}
}
