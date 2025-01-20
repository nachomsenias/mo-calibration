package algorithm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.IntegerDoubleProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.IntegerDoubleSolution;
import org.uma.jmetal.solution.impl.DefaultIntegerDoubleSolution;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

import problem.MWomABMProblem;

public class NelderMeadMO implements Algorithm<List<IntegerDoubleSolution>>{

	private class FitnessCalculator implements MultivariateFunction{

		private Problem<IntegerDoubleSolution> problem;
		private double[] e;
		
		private FitnessCalculator(Problem<IntegerDoubleSolution> problem, 
				double[] e) {
			this.problem = problem;
			this.e = e;
		}
		
		@Override
		public double value(double[] point) {
			IntegerDoubleSolution solution = new DefaultIntegerDoubleSolution(
					(IntegerDoubleProblem<?>) problem);
			
			for (int i=0; i<point.length; i++) {
				solution.setVariableValue(i, point[i]);
			}
			
			problem.evaluate(solution);

			// En princpio optimizamos el primer objetivo y el segundo se usa con epsilon
			double s2 = e[1]-solution.getObjective(1);
			
			// Esto es una suma o una resta? Maximizando el epsilon se suma
			// Minimizando se tendría que restar.
//			double value = solution.getObjective(0) + (epsilon *  s2);
			double value = solution.getObjective(0) - (epsilon *  s2);
			
			return value;
		}
		
		
	}
	
	private Problem<IntegerDoubleSolution> problem; 
	private int numSamples;
	
	private int maxSampleEvaluation;
	private double epsilon;
	
	private List<IntegerDoubleSolution> result;
	
	/**
	 * Generated.
	 */
	private static final long serialVersionUID = -2984847114438108179L;

	public NelderMeadMO(
			Problem<IntegerDoubleSolution> problem, 
			int samples,
			int maxSampleEvaluation,
			double epsilon
			) {
		this.problem = problem;
		this.numSamples = samples;
		this.maxSampleEvaluation = maxSampleEvaluation;
		this.epsilon = epsilon;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run() {
//		JMetalRandom.getInstance()
//			.setSeed(RandomizerUtils.PRIME_SEEDS[iteration]);
		
		result = new ArrayList<IntegerDoubleSolution>();
		
		for (int i=0; i<numSamples; i++) {
//			IntegerDoubleSolution baseline = ((MWomABMProblem)problem).getBaseline();
			IntegerDoubleSolution solution = problem.createSolution();
			problem.evaluate(solution);
			
			NelderMeadSimplex simplex = new NelderMeadSimplex(solution.getNumberOfVariables());
			
			double[] e = solution.getObjectives();
			FitnessCalculator calc = new FitnessCalculator(problem,e);
//			Comparator<PointValuePair> comparator = new Comparator<PointValuePair>() {
//				@Override
//				public int compare(PointValuePair arg0, PointValuePair arg1) {
//					// TODO Auto-generated method stub
//					double v1 = arg0.getValue();
//					double v2 =  arg1.getValue();
//					if (v1<v2) {
//						return -1;
//					} else if (v2<v1) {
//						return 1;
//					} return 0;
//				}
//			};
//			int iteration = 0;
//			while (iteration<maxSampleEvaluation) {
//				simplex.iterate(calc, comparator);
//				iteration++;
//			}
			
			CustomSimplex optimizer = new CustomSimplex(1e-10, 1e-30);
			
			try {
				final PointValuePair best = optimizer.optimize(
						new MaxEval(maxSampleEvaluation),
						new ObjectiveFunction(calc), 
							simplex, 
							GoalType.MINIMIZE, 
								new InitialGuess(((MWomABMProblem)problem).getPrimitive(solution))
	                         );
				result.add(((MWomABMProblem)problem).fromPrimitive(best.getPointRef()));
			} catch (TooManyEvaluationsException exception) {
				System.out.println("Sample failed!");
				exception.printStackTrace();
			}
		}
	}

	@Override
	public List<IntegerDoubleSolution> getResult() {
		// Return the resulting front after removing the dominated solutions.
		Ranking<IntegerDoubleSolution> ranking = 
				(Ranking<IntegerDoubleSolution>) new DominanceRanking<IntegerDoubleSolution>();
		return ranking.computeRanking(result).getSubfront(0);
	}
	
//	private List<IntegerDoubleSolution> initializeSamples() {
//		List<IntegerDoubleSolution> samples = new ArrayList<IntegerDoubleSolution>(this.numSamples);
//		for (int i = 0; i < this.numSamples; i++) {
//			IntegerDoubleSolution solution = problem.createSolution();
//
//			problem.evaluate(solution);
//			samples.add(solution);
//		}
//		return samples;
//	}

}
