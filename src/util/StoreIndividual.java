package util;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.measure.MeasureListener;

public class StoreIndividual<S> implements MeasureListener<S> {

	private List<String> individuals = new ArrayList<String>();

	private String mergeValues(S individual) {
		Solution<?> sol = (Solution<?>) individual;
		String output = "";

		int variables = sol.getNumberOfVariables();
		int objs = sol.getNumberOfObjectives();

		for (int o = 0; o < objs; o++) {
			output += sol.getObjective(o) + " ";
		}

		for (int v = 0; v < variables; v++) {
			output += sol.getVariableValueString(v) + " ";
		}
		return output;
	}

	@Override
	public void measureGenerated(S value) {
		individuals.add(mergeValues(value));
	}

	public List<String> getIndividuals() {
		return individuals;
	}

}
