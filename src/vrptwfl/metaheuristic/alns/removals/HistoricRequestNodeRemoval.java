package vrptwfl.metaheuristic.alns.removals;

import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;

import java.util.List;

public class HistoricRequestNodeRemoval extends AbstractRemoval {

    public HistoricRequestNodeRemoval(Data data) {
        super(data);
    }

	/**
	 * {@inheritDoc}
	 * Executes the removal.
	 */
    @Override
    public List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) {
        return null;
    }
}
