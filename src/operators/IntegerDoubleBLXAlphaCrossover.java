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

public class IntegerDoubleBLXAlphaCrossover implements CrossoverOperator<IntegerDoubleSolution> {
	/**
	 * Generated. 
	 */
	private static final long serialVersionUID = -8288236318024459282L;

	private static final double DEFAULT_ALPHA = 0.5;

	private double crossoverProbability;
	private double alpha;

	private RepairDoubleSolution solutionRepair;

	private RandomGenerator<Double> randomGenerator;
	
	private AbstractIntegerDoubleProblem<IntegerDoubleSolution> problem;

	/** Constructor */
	public IntegerDoubleBLXAlphaCrossover(double crossoverProbability) {
		this(crossoverProbability, DEFAULT_ALPHA, new RepairDoubleSolutionAtBounds());
	}

	/** Constructor */
	public IntegerDoubleBLXAlphaCrossover(double crossoverProbability, double alpha, 
			AbstractIntegerDoubleProblem<IntegerDoubleSolution> p) {
		this(crossoverProbability, alpha, new RepairDoubleSolutionAtBounds());
		this.problem = p;
	}

	/** Constructor */
	public IntegerDoubleBLXAlphaCrossover(double crossoverProbability, double alpha,
			RepairDoubleSolution solutionRepair) {
		this(crossoverProbability, alpha, solutionRepair, () -> JMetalRandom.getInstance().nextDouble());
	}

	/** Constructor */
	public IntegerDoubleBLXAlphaCrossover(double crossoverProbability, double alpha,
			RepairDoubleSolution solutionRepair, RandomGenerator<Double> randomGenerator) {
		if (crossoverProbability < 0) {
			throw new JMetalException("Crossover probability is negative: " + crossoverProbability);
		} else if (alpha < 0) {
			throw new JMetalException("Alpha is negative: " + alpha);
		}

		this.crossoverProbability = crossoverProbability;
		this.alpha = alpha;
		this.randomGenerator = randomGenerator;
		this.solutionRepair = solutionRepair;
	}

	/* Getters */
	public double getCrossoverProbability() {
		return crossoverProbability;
	}

	public double getAlpha() {
		return alpha;
	}

	/* Setters */
	public void setCrossoverProbability(double crossoverProbability) {
		this.crossoverProbability = crossoverProbability;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	/** Execute() method */
	@Override
	public List<IntegerDoubleSolution> execute(List<IntegerDoubleSolution> solutions) {
		if (null == solutions) {
			throw new JMetalException("Null parameter");
		} else if (solutions.size() != 2) {
			throw new JMetalException("There must be two parents instead of " + solutions.size());
		}

		return doCrossover(crossoverProbability, solutions.get(0), solutions.get(1));
	}

	/** doCrossover method */
	public List<IntegerDoubleSolution> doCrossover(double probability, IntegerDoubleSolution parent1,
			IntegerDoubleSolution parent2) {
		List<IntegerDoubleSolution> offspring = new ArrayList<IntegerDoubleSolution>(2);

//		offspring.add((IntegerDoubleSolution) parent1.copy());
//		offspring.add((IntegerDoubleSolution) parent2.copy());
	
		offspring.add(MWomABMProblem.cloneSolution(problem, parent1));
		offspring.add(MWomABMProblem.cloneSolution(problem, parent2));

		int i;
		double random;
		double valueY1;
		double valueY2;
		double valueX1;
		double valueX2;
		double upperBound;
		double lowerBound;

		if (randomGenerator.getRandomValue() <= probability) {
			for (i = 0; i < parent1.getNumberOfVariables(); i++) {
				upperBound = parent1.getUpperBound(i).doubleValue();
				lowerBound = parent1.getLowerBound(i).doubleValue();
				valueX1 = parent1.getVariableValue(i).doubleValue();
				valueX2 = parent2.getVariableValue(i).doubleValue();

				double max;
				double min;
				double range;

				if (valueX2 > valueX1) {
					max = valueX2;
					min = valueX1;
				} else {
					max = valueX1;
					min = valueX2;
				}

				range = max - min;

				double minRange;
				double maxRange;

				minRange = min - range * alpha;
				maxRange = max + range * alpha;

				random = randomGenerator.getRandomValue();
				valueY1 = minRange + random * (maxRange - minRange);

				random = randomGenerator.getRandomValue();
				valueY2 = minRange + random * (maxRange - minRange);

				valueY1 = solutionRepair.repairSolutionVariableValue(valueY1, lowerBound, upperBound);
				valueY2 = solutionRepair.repairSolutionVariableValue(valueY2, lowerBound, upperBound);

				offspring.get(0).setVariableValue(i, valueY1);
				offspring.get(1).setVariableValue(i, valueY2);
			}
		}

		return offspring;
	}

	public int getNumberOfRequiredParents() {
		return 2;
	}

	public int getNumberOfGeneratedChildren() {
		return 2;
	}
}