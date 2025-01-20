package algorithm;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.RankingAndCrowdingSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.impl.DefaultIntegerDoubleSolution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

public class NSGAIIIntegerDouble<IntegerDoubleSolution> extends
		AbstractGeneticAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>> {

	/**
	 * Generated.
	 */
	private static final long serialVersionUID = 5221149300436981259L;

	protected final int maxEvaluations;

	protected final SolutionListEvaluator<IntegerDoubleSolution> evaluator;

	protected int evaluations;

	/**
	 * Constructor
	 */
	public NSGAIIIntegerDouble(Problem<IntegerDoubleSolution> problem, int maxEvaluations,
			int populationSize,
			CrossoverOperator<IntegerDoubleSolution> crossoverOperator,
			MutationOperator<IntegerDoubleSolution> mutationOperator,
			SelectionOperator<List<IntegerDoubleSolution>, IntegerDoubleSolution> selectionOperator,
			SolutionListEvaluator<IntegerDoubleSolution> evaluator) {
		super(problem);
		this.maxEvaluations = maxEvaluations;
		setMaxPopulationSize(populationSize);
		;

		this.crossoverOperator = crossoverOperator;
		this.mutationOperator = mutationOperator;
		this.selectionOperator = selectionOperator;

		this.evaluator = evaluator;
	}

	@Override
	protected void initProgress() {
		evaluations = getMaxPopulationSize();
	}

	@Override
	protected void updateProgress() {
		evaluations += getMaxPopulationSize();
	}

	@Override
	protected boolean isStoppingConditionReached() {
		return evaluations >= maxEvaluations;
	}

	@Override
	protected List<IntegerDoubleSolution> evaluatePopulation(
			List<IntegerDoubleSolution> population) {
		population = evaluator.evaluate(population, getProblem());

		return population;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected List<IntegerDoubleSolution> replacement(
			List<IntegerDoubleSolution> population,
			List<IntegerDoubleSolution> offspringPopulation) {
		List<IntegerDoubleSolution> jointPopulation = new ArrayList<>();
		jointPopulation.addAll(population);
		jointPopulation.addAll(offspringPopulation);

		RankingAndCrowdingSelection<DefaultIntegerDoubleSolution> rankingAndCrowdingSelection = new RankingAndCrowdingSelection<DefaultIntegerDoubleSolution>(
				getMaxPopulationSize());

		return (List<IntegerDoubleSolution>) rankingAndCrowdingSelection
				.execute((List<DefaultIntegerDoubleSolution>) jointPopulation);
	}

	@Override
	public List<IntegerDoubleSolution> getResult() {
		@SuppressWarnings("unchecked")
		Ranking<IntegerDoubleSolution> ranking = (Ranking<IntegerDoubleSolution>) new DominanceRanking<DefaultIntegerDoubleSolution>();
		return ranking.computeRanking(getPopulation()).getSubfront(0);
	}

	@Override
	public String getName() {
		return "NSGAII";
	}

	@Override
	public String getDescription() {
		return "Nondominated Sorting Genetic Algorithm version II, "
				+ "version for IntegerDouble solutions";
	}
}
