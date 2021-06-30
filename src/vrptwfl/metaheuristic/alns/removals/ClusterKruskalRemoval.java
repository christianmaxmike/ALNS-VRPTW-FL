package vrptwfl.metaheuristic.alns.removals;

import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;

import java.util.List;

// Ropke & Pisinger 2006 EJOR 171, pp. 750-775
public class ClusterKruskalRemoval extends AbstractRemoval {

    public ClusterKruskalRemoval(Data data) {
        super(data);
    }

    @Override
    List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) throws ArgumentOutOfBoundsException {
        return null;
        // TODO hier geht's weiter
    }


}
