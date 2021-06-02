package vrptwfl.metaheuristic.alns.removals;

import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;

import java.util.List;

public class RequestGraphRemoval extends AbstractRemoval {

    public RequestGraphRemoval(Data data) {
        super(data);
    }

    @Override
    public List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) {
        return null;
    }
}
