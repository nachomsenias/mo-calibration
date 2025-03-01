package operators;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.problem.impl.AbstractIntegerDoubleProblem;
import org.uma.jmetal.solution.IntegerDoubleSolution;
import org.uma.jmetal.solution.util.RepairDoubleSolution;
import org.uma.jmetal.solution.util.RepairDoubleSolutionAtBounds;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import problem.MWomABMProblem;

public class IntegerDoubleSBXCrossover
		implements CrossoverOperator<IntegerDoubleSolution> {

	/**
	 * Generated.
	 */
	private static final long serialVersionUID = -8581692642911853035L;

	/** EPS defines the minimum difference allowed between real values */
	private static final double EPS = 1.0e-14;

	private double distributionIndex;
	private double crossoverProbability;
	private RepairDoubleSolution solutionRepair;

	private RandomGenerator<Double> randomGenerator;

	private AbstractIntegerDoubleProblem<IntegerDoubleSolution> problem;

	/** Constructor */
	public IntegerDoubleSBXCrossover(double crossoverProbability,
			double distributionIndex,
			AbstractIntegerDoubleProblem<IntegerDoubleSolution> problem) {
		this(crossoverProbability, distributionIndex, new RepairDoubleSolutionAtBounds(),
				problem);
	}

	/** Constructor */
	public IntegerDoubleSBXCrossover(double crossoverProbability,
			double distributionIndex, RandomGenerator<Double> randomGenerator,
			AbstractIntegerDoubleProblem<IntegerDoubleSolution> problem) {
		this(crossoverProbability, distributionIndex, new RepairDoubleSolutionAtBounds(),
				randomGenerator, problem);
	}

	/** Constructor */
	public IntegerDoubleSBXCrossover(double crossoverProbability,
			double distributionIndex, RepairDoubleSolution solutionRepair,
			AbstractIntegerDoubleProblem<IntegerDoubleSolution> problem) {
		this(crossoverProbability, distributionIndex, solutionRepair,
				() -> JMetalRandom.getInstance().nextDouble(), problem);
	}

	/** Constructor */
	public IntegerDoubleSBXCrossover(double crossoverProbability,
			double distributionIndex, RepairDoubleSolution solutionRepair,
			RandomGenerator<Double> randomGenerator,
			AbstractIntegerDoubleProblem<IntegerDoubleSolution> problem) {
		if (crossoverProbability < 0) {
			throw new JMetalException(
					"Crossover probability is negative: " + crossoverProbability);
		} else if (distributionIndex < 0) {
			throw new JMetalException(
					"Distribution index is negative: " + distributionIndex);
		}

		this.crossoverProbability = crossoverProbability;
		this.distributionIndex = distributionIndex;
		this.solutionRepair = solutionRepair;

		this.randomGenerator = randomGenerator;

		this.problem = problem;
	}

	@Override
	public List<IntegerDoubleSolution> execute(List<IntegerDoubleSolution> source) {
		if (null == source) {
			throw new JMetalException("Null parameter");
		} else if (source.size() != 2) {
			throw new JMetalException(
					"There must be two parents instead of " + source.size());
		}

		return doCrossover(crossoverProbability, source.get(0), source.get(1));
	}

	/* Getters */
	public double getCrossoverProbability() {
		return crossoverProbability;
	}

	public double getDistributionIndex() {
		return distributionIndex;
	}

	/* Setters */
	public void setCrossoverProbability(double probability) {
		this.crossoverProbability = probability;
	}

	public void setDistributionIndex(double distributionIndex) {
		this.distributionIndex = distributionIndex;
	}

	/** doCrossover method */
	public List<IntegerDoubleSolution> doCrossover(double probability,
			IntegerDoubleSolution parent1, IntegerDoubleSolution parent2) {
		List<IntegerDoubleSolution> offspring = new ArrayList<IntegerDoubleSolution>(2);

		// offspring.add((IntegerDoubleSolution) parent1.copy());
		// offspring.add((IntegerDoubleSolution) parent2.copy());

		offspring.add(MWomABMProblem.cloneSolution(problem, parent1));
		offspring.add(MWomABMProblem.cloneSolution(problem, parent2));

		int i;
		double rand;
		double y1, y2, lowerBound, upperBound;
		double c1, c2;
		double alpha, beta, betaq;
		double valueX1, valueX2;

		if (randomGenerator.getRandomValue() <= probability) {
			for (i = 0; i < parent1.getNumberOfVariables(); i++) {
				valueX1 = parent1.getVariableValue(i).doubleValue();
				valueX2 = parent2.getVariableValue(i).doubleValue();
				if (randomGenerator.getRandomValue() <= 0.5) {
					if (Math.abs(valueX1 - valueX2) > EPS) {

						if (valueX1 < valueX2) {
							y1 = valueX1;
							y2 = valueX2;
						} else {
							y1 = valueX2;
							y2 = valueX1;
						}

						lowerBound = parent1.getLowerBound(i).doubleValue();
						upperBound = parent1.getUpperBound(i).doubleValue();

						rand = randomGenerator.getRandomValue();
						beta = 1.0 + (2.0 * (y1 - lowerBound) / (y2 - y1));
						alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));

						if (rand <= (1.0 / alpha)) {
							betaq = Math.pow(rand * alpha,
									(1.0 / (distributionIndex + 1.0)));
						} else {
							betaq = Math.pow(1.0 / (2.0 - rand * alpha),
									1.0 / (distributionIndex + 1.0));
						}
						c1 = 0.5 * (y1 + y2 - betaq * (y2 - y1));

						beta = 1.0 + (2.0 * (upperBound - y2) / (y2 - y1));
						alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));

						if (rand <= (1.0 / alpha)) {
							betaq = Math.pow((rand * alpha),
									(1.0 / (distributionIndex + 1.0)));
						} else {
							betaq = Math.pow(1.0 / (2.0 - rand * alpha),
									1.0 / (distributionIndex + 1.0));
						}
						c2 = 0.5 * (y1 + y2 + betaq * (y2 - y1));

						c1 = solutionRepair.repairSolutionVariableValue(c1, lowerBound,
								upperBound);
						c2 = solutionRepair.repairSolutionVariableValue(c2, lowerBound,
								upperBound);

						if (randomGenerator.getRandomValue() <= 0.5) {
							// offspring.get(0).setVariableValue(i, c2);
							// offspring.get(1).setVariableValue(i, c1);
							setValue(offspring.get(0), i, c2);
							setValue(offspring.get(1), i, c1);
						} else {
							// offspring.get(0).setVariableValue(i, c1);
							// offspring.get(1).setVariableValue(i, c2);
							setValue(offspring.get(0), i, c1);
							setValue(offspring.get(1), i, c2);
						}
					} else {
						// offspring.get(0).setVariableValue(i, valueX1);
						// offspring.get(1).setVariableValue(i, valueX2);
						setValue(offspring.get(0), i, valueX1);
						setValue(offspring.get(1), i, valueX2);
					}
				} else {
					// offspring.get(0).setVariableValue(i, valueX1);
					// offspring.get(1).setVariableValue(i, valueX2);
					setValue(offspring.get(0), i, valueX1);
					setValue(offspring.get(1), i, valueX2);
				}
			}
		}

		return offspring;
	}

	private void setValue(IntegerDoubleSolution solution, int varIndex, double value) {
		if (varIndex < solution.getNumberOfIntegerVariables()) {
			solution.setVariableValue(varIndex, (int) value);
		} else {
			solution.setVariableValue(varIndex, value);
		}
	}

	@Override
	public int getNumberOfRequiredParents() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public int getNumberOfGeneratedChildren() {
		// TODO Auto-generated method stub
		return 2;
	}
}
