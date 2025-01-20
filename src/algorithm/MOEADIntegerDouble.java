package algorithm;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.algorithm.multiobjective.moead.AbstractMOEAD;
import org.uma.jmetal.algorithm.multiobjective.moead.MOEAD;
import org.uma.jmetal.algorithm.multiobjective.moead.util.MOEADUtils;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.IntegerDoubleSolution;
import org.uma.jmetal.solution.impl.DefaultIntegerDoubleSolution;
import org.uma.jmetal.util.JMetalException;

import operators.IntegerDoubleDifferentialEvolutionCrossover;
import problem.MWomABMProblem;
import util.CustomPopulationMeasure;

public class MOEADIntegerDouble extends AbstractMOEAD<IntegerDoubleSolution> {

	/**
	 * Generated.
	 */
	private static final long serialVersionUID = 4391453856025782344L;

	protected IntegerDoubleDifferentialEvolutionCrossover differentialEvolutionCrossover;
	protected CustomPopulationMeasure<IntegerDoubleSolution> measure;

	public MOEADIntegerDouble(Problem<IntegerDoubleSolution> problem, int populationSize,
			int resultPopulationSize, int maxEvaluations,
			CrossoverOperator<IntegerDoubleSolution> crossoverOperator,
			MutationOperator<IntegerDoubleSolution> mutation,
			AbstractMOEAD.FunctionType functionType, String dataDirectory,
			double neighborhoodSelectionProbability, int maximumNumberOfReplacedSolutions,
			int neighborSize, CustomPopulationMeasure<IntegerDoubleSolution> measure) {
		super(problem, populationSize, resultPopulationSize, maxEvaluations,
				crossoverOperator, mutation, functionType, dataDirectory,
				neighborhoodSelectionProbability, maximumNumberOfReplacedSolutions,
				neighborSize);
		differentialEvolutionCrossover = (IntegerDoubleDifferentialEvolutionCrossover) crossoverOperator;
		this.measure = measure;
	}

	@Override
	public void run() {
		initializePopulation();
		initializeUniformWeight();
		initializeNeighborhood();
		idealPoint.update(population);

		evaluations = populationSize;
		do {
			int[] permutation = new int[populationSize];
			MOEADUtils.randomPermutation(permutation, populationSize);

			for (int i = 0; i < populationSize; i++) {
				int subProblemId = permutation[i];

				NeighborType neighborType = chooseNeighborType();
				List<IntegerDoubleSolution> parents = parentSelection(subProblemId,
						neighborType);

				differentialEvolutionCrossover
						.setCurrentSolution(population.get(subProblemId));
				List<IntegerDoubleSolution> children = differentialEvolutionCrossover
						.execute(parents);

				IntegerDoubleSolution child = children.get(0);
				mutationOperator.execute(child);
				problem.evaluate(child);
				measure.push(child);

				evaluations++;

				idealPoint.update(child.getObjectives());
				updateNeighborhood(child, subProblemId, neighborType);
			}
		} while (evaluations < maxEvaluations);
	}

	protected void initializePopulation() {
		population = new ArrayList<IntegerDoubleSolution>(populationSize);
		for (int i = 0; i < populationSize; i++) {
			IntegerDoubleSolution newSolution = problem.createSolution();

			problem.evaluate(newSolution);
			population.add(newSolution);
		}
	}

	@Override
	protected void updateNeighborhood(IntegerDoubleSolution individual, int subProblemId,
			NeighborType neighborType) throws JMetalException {
		int size;
		int time;

		time = 0;

		if (neighborType == NeighborType.NEIGHBOR) {
			size = neighborhood[subProblemId].length;
		} else {
			size = population.size();
		}
		int[] perm = new int[size];

		MOEADUtils.randomPermutation(perm, size);

		for (int i = 0; i < size; i++) {
			int k;
			if (neighborType == NeighborType.NEIGHBOR) {
				k = neighborhood[subProblemId][perm[i]];
			} else {
				k = perm[i];
			}
			double f1, f2;

			f1 = customfitnessFunction(population.get(k), lambda[k]);
			f2 = customfitnessFunction(individual, lambda[k]);

			if (f2 < f1) {
				DefaultIntegerDoubleSolution cloned =
						// new
						// DefaultIntegerDoubleSolution((DefaultIntegerDoubleSolution)individual);
						MWomABMProblem.cloneSolution((MWomABMProblem) problem,
								(DefaultIntegerDoubleSolution) individual);
				population.set(k, cloned);
				time++;
			}

			if (time >= maximumNumberOfReplacedSolutions) {
				return;
			}
		}
	}

	/**
	 * "Overrides" not visible method from the superclass.
	 * 
	 * @param individual
	 * @param lambda
	 * @return
	 * @throws JMetalException
	 */
	private double customfitnessFunction(IntegerDoubleSolution individual,
			double[] lambda) throws JMetalException {
		double fitness;

		if (MOEAD.FunctionType.TCHE.equals(functionType)) {
			double maxFun = -1.0e+30;

			for (int n = 0; n < problem.getNumberOfObjectives(); n++) {
				double diff = Math.abs(individual.getObjective(n) - idealPoint.getValue(n));

				double feval;
				if (lambda[n] == 0) {
					feval = 0.0001 * diff;
				} else {
					feval = diff * lambda[n];
				}
				if (feval > maxFun) {
					maxFun = feval;
				}
			}

			fitness = maxFun;
		} else if (MOEAD.FunctionType.AGG.equals(functionType)) {
			double sum = 0.0;
			for (int n = 0; n < problem.getNumberOfObjectives(); n++) {
				sum += (lambda[n]) * individual.getObjective(n);
			}

			fitness = sum;

		} else if (MOEAD.FunctionType.PBI.equals(functionType)) {
			double d1, d2, nl;
			double theta = 5.0;

			d1 = d2 = nl = 0.0;

			for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
				d1 += (individual.getObjective(i) - idealPoint.getValue(i)) * lambda[i];
				nl += Math.pow(lambda[i], 2.0);
			}
			nl = Math.sqrt(nl);
			d1 = Math.abs(d1) / nl;

			for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
				d2 += Math.pow((individual.getObjective(i) - idealPoint.getValue(i))
						- d1 * (lambda[i] / nl), 2.0);
			}
			d2 = Math.sqrt(d2);

			fitness = (d1 + theta * d2);
		} else {
			throw new JMetalException(
					" MOEAD.fitnessFunction: unknown type " + functionType);
		}
		return fitness;
	}

	@Override
	public String getName() {
		return "MOEAD-IntegerDouble";
	}

	@Override
	public String getDescription() {
		return "Multi-Objective Evolutionary Algorithm based on Decomposition. "
				+ "Version for IntegerDouble solutions.";
	}
}
