package util;

import java.util.List;


import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.measure.impl.SimplePushMeasure;

public class CustomSolutionEvaluator<S> implements SolutionListEvaluator<S> {

	/**
	 * Generated.
	 */
	private static final long serialVersionUID = -4172726125493091489L;

	private SimplePushMeasure<S> measure;

	public CustomSolutionEvaluator(SimplePushMeasure<S> measure) {
		this.measure = measure;
	}

	@Override
	public List<S> evaluate(List<S> solutionList, Problem<S> problem)
			throws JMetalException {
		solutionList.stream().forEach(s -> {
			problem.evaluate(s);
			measure.push(s);
		});

		return solutionList;
	}

	@Override
	public void shutdown() {
	}

}
