package operators;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.problem.impl.AbstractIntegerDoubleProblem;
import org.uma.jmetal.solution.IntegerDoubleSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.pseudorandom.BoundedRandomGenerator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import problem.MWomABMProblem;

public class IntegerDoubleDifferentialEvolutionCrossover implements CrossoverOperator<IntegerDoubleSolution> {

	/**
	 * Generated.
	 */
	private static final long serialVersionUID = 9031134294575352565L;

	private static final double DEFAULT_CR = 0.5;
	private static final double DEFAULT_F = 0.5;
	private static final double DEFAULT_K = 0.5;
	private static final String DEFAULT_DE_VARIANT = "rand/1/bin";

	private double cr;
	private double f;
	private double k;
	// DE variant (rand/1/bin, rand/1/exp, etc.)
	private String variant;

	private IntegerDoubleSolution currentSolution;

	private BoundedRandomGenerator<Integer> jRandomGenerator;
	private BoundedRandomGenerator<Double> crRandomGenerator;
	
	private AbstractIntegerDoubleProblem<IntegerDoubleSolution> problem;

	/** Constructor */
	public IntegerDoubleDifferentialEvolutionCrossover(AbstractIntegerDoubleProblem<IntegerDoubleSolution> problem) {
		this(DEFAULT_CR, DEFAULT_F, DEFAULT_K, DEFAULT_DE_VARIANT, problem);
	}

	/**
	 * Constructor
	 * 
	 * @param cr
	 * @param f
	 * @param variant
	 */
	public IntegerDoubleDifferentialEvolutionCrossover(double cr, double f, String variant, AbstractIntegerDoubleProblem<IntegerDoubleSolution> problem) {
		this(cr, f, variant, (a, b) -> JMetalRandom.getInstance().nextInt(a, b),
				(a, b) -> JMetalRandom.getInstance().nextDouble(a, b), problem);
	}

	/**
	 * Constructor
	 * 
	 * @param cr
	 * @param f
	 * @param variant
	 * @param jRandomGenerator
	 * @param crRandomGenerator
	 */
	public IntegerDoubleDifferentialEvolutionCrossover(double cr, double f, String variant,
			RandomGenerator<Double> randomGenerator, AbstractIntegerDoubleProblem<IntegerDoubleSolution> problem) {
		this(cr, f, variant, BoundedRandomGenerator.fromDoubleToInteger(randomGenerator),
				BoundedRandomGenerator.bound(randomGenerator), problem);
	}

	/**
	 * Constructor
	 * 
	 * @param cr
	 * @param f
	 * @param variant
	 * @param jRandomGenerator
	 * @param crRandomGenerator
	 */
	public IntegerDoubleDifferentialEvolutionCrossover(double cr, double f, String variant,
			BoundedRandomGenerator<Integer> jRandomGenerator, BoundedRandomGenerator<Double> crRandomGenerator, AbstractIntegerDoubleProblem<IntegerDoubleSolution> problem) {
		this.cr = cr;
		this.f = f;
		this.k = DEFAULT_K;
		this.variant = variant;

		this.jRandomGenerator = jRandomGenerator;
		this.crRandomGenerator = crRandomGenerator;
		
		this.problem = problem;
	}

	/** Constructor */
	public IntegerDoubleDifferentialEvolutionCrossover(double cr, double f, double k, String variant, AbstractIntegerDoubleProblem<IntegerDoubleSolution> problem) {
		this(cr, f, variant, problem);
		this.k = k;
	}

	/* Getters */
	public double getCr() {
		return cr;
	}

	public double getF() {
		return f;
	}

	public double getK() {
		return k;
	}

	public String getVariant() {
		return variant;
	}

	/* Setters */
	public void setCurrentSolution(IntegerDoubleSolution current) {
		this.currentSolution = current;
	}

	public void setCr(double cr) {
		this.cr = cr;
	}

	public void setF(double f) {
		this.f = f;
	}

	public void setK(double k) {
		this.k = k;
	}

	private void setValue(IntegerDoubleSolution solution, int varIndex, double value) {
		if (varIndex < solution.getNumberOfIntegerVariables()) {
			solution.setVariableValue(varIndex, (int) value);
		} else {
			solution.setVariableValue(varIndex, value);
		}
	}

	@Override
	public List<IntegerDoubleSolution> execute(List<IntegerDoubleSolution> parentSolutions) {
		IntegerDoubleSolution child;

		int jrand;

//		child = (IntegerDoubleSolution) currentSolution.copy();
		child = MWomABMProblem.cloneSolution(problem, currentSolution);

		int numberOfVariables = parentSolutions.get(0).getNumberOfVariables();
		jrand = jRandomGenerator.getRandomValue(0, numberOfVariables - 1);

		// STEP 4. Checking the DE variant
		if (("rand/1/bin".equals(variant)) || "best/1/bin".equals(variant)) {
			for (int j = 0; j < numberOfVariables; j++) {
				if (crRandomGenerator.getRandomValue(0.0, 1.0) < cr || j == jrand) {
					double value;
					value = parentSolutions.get(2).getVariableValue(j).doubleValue() + f
							* (parentSolutions.get(0).getVariableValue(j).doubleValue() 
									- parentSolutions.get(1).getVariableValue(j).doubleValue());

					if (value < child.getLowerBound(j).doubleValue()) {
						value = child.getLowerBound(j).doubleValue();
					}
					if (value > child.getUpperBound(j).doubleValue()) {
						value = child.getUpperBound(j).doubleValue();
					}
//					child.setVariableValue(j, value);
					setValue(child, j, value);
				} else {
//					double value;
					Number value = currentSolution.getVariableValue(j);
					if(value!=null){
//						child.setVariableValue(j, value);
						setValue(child, j, value.doubleValue());
					}
				}
			}
		} else if ("rand/1/exp".equals(variant) || "best/1/exp".equals(variant)) {
			for (int j = 0; j < numberOfVariables; j++) {
				if (crRandomGenerator.getRandomValue(0.0, 1.0) < cr || j == jrand) {
					double value;
					value = parentSolutions.get(2).getVariableValue(j).doubleValue() + f
							* (parentSolutions.get(0).getVariableValue(j).doubleValue() 
									- parentSolutions.get(1).getVariableValue(j).doubleValue());

					if (value < child.getLowerBound(j).doubleValue()) {
						value = child.getLowerBound(j).doubleValue();
					}
					if (value > child.getUpperBound(j).doubleValue()) {
						value = child.getUpperBound(j).doubleValue();
					}
//					child.setVariableValue(j, value);
					setValue(child, j, value);
				} else {
					cr = 0.0;
					double value;
					value = currentSolution.getVariableValue(j).doubleValue();
//					child.setVariableValue(j, value);
					setValue(child, j, value);
				}
			}
		} else if ("current-to-rand/1".equals(variant) || "current-to-best/1".equals(variant)) {
			for (int j = 0; j < numberOfVariables; j++) {
				double value;
				value = currentSolution.getVariableValue(j).doubleValue()
						+ k * (parentSolutions.get(2).getVariableValue(j).doubleValue() 
								- currentSolution.getVariableValue(j).doubleValue())
						+ f * (parentSolutions.get(0).getVariableValue(j).doubleValue() 
								- parentSolutions.get(1).getVariableValue(j).doubleValue());

				if (value < child.getLowerBound(j).doubleValue()) {
					value = child.getLowerBound(j).doubleValue();
				}
				if (value > child.getUpperBound(j).doubleValue()) {
					value = child.getUpperBound(j).doubleValue();
				}

//				child.setVariableValue(j, value);
				setValue(child, j, value);
			}
		} else if ("current-to-rand/1/bin".equals(variant) || "current-to-best/1/bin".equals(variant)) {
			for (int j = 0; j < numberOfVariables; j++) {
				if (crRandomGenerator.getRandomValue(0.0, 1.0) < cr || j == jrand) {
					double value;
					value = currentSolution.getVariableValue(j).doubleValue()
							+ k * (parentSolutions.get(2).getVariableValue(j).doubleValue() 
									- currentSolution.getVariableValue(j).doubleValue())
							+ f * (parentSolutions.get(0).getVariableValue(j).doubleValue()
									- parentSolutions.get(1).getVariableValue(j).doubleValue());

					if (value < child.getLowerBound(j).doubleValue()) {
						value = child.getLowerBound(j).doubleValue();
					}
					if (value > child.getUpperBound(j).doubleValue()) {
						value = child.getUpperBound(j).doubleValue();
					}
//					child.setVariableValue(j, value);
					setValue(child, j, value);
				} else {
					double value;
					value = currentSolution.getVariableValue(j).doubleValue();
//					child.setVariableValue(j, value);
					setValue(child, j, value);
				}
			}
		} else if ("current-to-rand/1/exp".equals(variant) || "current-to-best/1/exp".equals(variant)) {
			for (int j = 0; j < numberOfVariables; j++) {
				if (crRandomGenerator.getRandomValue(0.0, 1.0) < cr || j == jrand) {
					double value;
					value = currentSolution.getVariableValue(j).doubleValue()
							+ k * (parentSolutions.get(2).getVariableValue(j).doubleValue() 
									- currentSolution.getVariableValue(j).doubleValue())
							+ f * (parentSolutions.get(0).getVariableValue(j).doubleValue()
									- parentSolutions.get(1).getVariableValue(j).doubleValue());

					if (value < child.getLowerBound(j).doubleValue()) {
						value = child.getLowerBound(j).doubleValue();
					}
					if (value > child.getUpperBound(j).doubleValue()) {
						value = child.getUpperBound(j).doubleValue();
					}
//					child.setVariableValue(j, value);
					setValue(child, j, value);
				} else {
					cr = 0.0;
					double value;
					value = currentSolution.getVariableValue(j).doubleValue();
//					child.setVariableValue(j, value);
					setValue(child, j, value);
				}
			}
		} else {
			JMetalLogger.logger
					.severe("DifferentialEvolutionCrossover.execute: " 
							+ " unknown DE variant (" + variant + ")");
			Class<String> cls = String.class;
			String name = cls.getName();
			throw new JMetalException("Exception in " + name + ".execute()");
		}

		List<IntegerDoubleSolution> result = new ArrayList<>(1);
		result.add(child);
		return result;
	}

	@Override
	public int getNumberOfRequiredParents() {
		// TODO Auto-generated method stub
		return 3;
	}

	@Override
	public int getNumberOfGeneratedChildren() {
		return 1;
	}

}
