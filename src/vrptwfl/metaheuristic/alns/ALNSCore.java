package vrptwfl.metaheuristic.alns;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.alns.insertions.AbstractInsertion;
import vrptwfl.metaheuristic.alns.insertions.GreedyInsertion;
import vrptwfl.metaheuristic.alns.insertions.RegretInsertion;
import vrptwfl.metaheuristic.alns.removals.*;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.utils.DebugUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ALNSCore {

    private Data data;

    private AbstractInsertion[] repairOperators;
    private AbstractRemoval[] destroyOperators;

    //if useNeighborGraphRemoval, then this graph contains information about the best solution in which the edge (i,j) was used
    private double[][] neighborGraph;

    public double[][] getNeighborGraph() {
        return neighborGraph;
    }

    public ALNSCore(Data data) throws ArgumentOutOfBoundsException {
        this.data = data;

        this.initRepairOperators();
        this.initDestroyOperators();
    }

    private void initDestroyOperators() {
        List<AbstractRemoval> destroyList = new ArrayList<>();

        if (Config.useNeighborGraphRemovalDeterministic) destroyList.add(new NeighborGraphRemoval(data, this,false));
        if (Config.useNeighborGraphRemovalRandom) destroyList.add(new NeighborGraphRemoval(data, this, true));
        if (Config.useNeighborGraphRemovalDeterministic || Config.useNeighborGraphRemovalRandom) this.initNeighborGraph();

        if (Config.useRandomRemoval) destroyList.add(new RandomRemoval(data));
        if (Config.useRandomRouteRemoval) destroyList.add(new RandomRouteRemoval(data));
        if (Config.useRequestGraphRemoval) destroyList.add(new RequestGraphRemoval(data));
        if (Config.useShawSimplifiedDeterministic) destroyList.add(new ShawSimplifiedRemoval(data, false));
        if (Config.useShawSimplifiedRandom) destroyList.add(new ShawSimplifiedRemoval(data, true));
        if (Config.useTimeOrientedDeterministic) destroyList.add(new TimeOrientedRemovalPisinger(data, false));
        if (Config.useTimeOrientedRandom) destroyList.add(new TimeOrientedRemovalPisinger(data, true));
        if (Config.useWorstRemovalDeterministic) destroyList.add(new WorstRemoval(data, false));
        if (Config.useWorstRemovalRandom) destroyList.add(new WorstRemoval(data, true));

        this.destroyOperators = new AbstractRemoval[destroyList.size()];
        this.destroyOperators = destroyList.toArray(this.destroyOperators);
    }

    private void initNeighborGraph() {
        // complete, directed, weighted graph
        this.neighborGraph = new double[this.data.getnCustomers() + 1][this.data.getnCustomers() + 1];

        // edges are initially set to infinity (or a reasonably high value)
        Arrays.stream(this.neighborGraph).forEach(row -> Arrays.fill(row, Config.bigMRegret));
    }


    private void initRepairOperators() throws ArgumentOutOfBoundsException {
        List<AbstractInsertion> repairList = new ArrayList<>();

        if (Config.useGreedyInsert) repairList.add(new GreedyInsertion(data));
        if (Config.useNRegret2) repairList.add(new RegretInsertion(2 ,data));
        if (Config.useNRegret3) repairList.add(new RegretInsertion(3 ,data));
        if (Config.useNRegret4) repairList.add(new RegretInsertion(4 ,data));
        if (Config.useNRegret5) repairList.add(new RegretInsertion(5 ,data));
        if (Config.useNRegret6) repairList.add(new RegretInsertion(6 ,data));

        this.repairOperators = new AbstractInsertion[repairList.size()];
        this.repairOperators = repairList.toArray(this.repairOperators);
    }

    public Solution runALNS(Solution solutionConstr) throws ArgumentOutOfBoundsException {

        // init ALNS
        Solution solutionCurrent = solutionConstr.copyDeep();
        Solution solutionBestGlobal = solutionConstr.copyDeep();

        // add information from construction to neighbor graph
        if (Config.useNeighborGraphRemovalRandom || Config.useNeighborGraphRemovalDeterministic) this.updateNeighborGraph(solutionConstr);


        for (int iteration = 1; iteration <= Config.alnsIterations; iteration++) {
//        for (int iteration = 1; iteration <= 10_000; iteration++) {

            Solution solutionTemp = solutionCurrent.copyDeep();

            // TODO random auswaehlen aus Operatoren (geht das irgendwie mit Lambdas besser ?)

            // destroy solution
            getDestroyOperatorAtRandom().destroy(solutionTemp);

            // repair solution
            // returns one repair operator specified in repairOperators
            this.getRepairOperatorAtRandom().solve(solutionTemp);

            // update neighbor graph if new solution was found (TODO check if the solution is really a new one (hashtable?)
            if (Config.useNeighborGraphRemovalRandom || Config.useNeighborGraphRemovalDeterministic) this.updateNeighborGraph(solutionTemp);

            if (iteration % 1000 == 0) {
                System.out.println("\n\nIteration " + iteration);
                System.out.println("Cost temp " + solutionTemp.getTotalCosts());
                System.out.println("Cost curr " + solutionCurrent.getTotalCosts());
                System.out.println("Cost glob " + solutionBestGlobal.getTotalCosts());
            }

            solutionCurrent = this.checkImprovement(solutionTemp, solutionCurrent, solutionBestGlobal);

            if (iteration % 1000 == 0) {
                System.out.println();
                System.out.println("Cost curr " + solutionCurrent.getTotalCosts());
                System.out.println("Cost glob " + solutionBestGlobal.getTotalCosts());
            }

//            if (iteration % 5000 == 0) {
//                DebugUtils.printNumericMatrix(this.neighborGraph);
//            }

        }

        return solutionBestGlobal;
    }

    // TODO Testcase um zu checken, ob auch die richtigen werte upgdated werden
    private void updateNeighborGraph(Solution solution) {

        double obj = solution.getTotalCosts();

        for (Vehicle vehicle: solution.getVehicles()) {
            if (vehicle.isUsed()) {
                ArrayList<Integer> customers = vehicle.getCustomers();

                int pred = customers.get(0);
                int succ = -1;
                for (int c = 1; c < customers.size(); c++) {
                    succ = customers.get(c);

                    if (this.neighborGraph[pred][succ] > obj + Config.epsilon) this.neighborGraph[pred][succ] = obj;

                    pred = succ;
                }
            }
        }

    }

    // TODO hier brauchen wir auch noch Test cases
    private Solution checkImprovement(Solution solutionTemp, Solution solutionCurrent, Solution solutionBestGlobal) {

        // check if improvement of global best
        if (solutionTemp.isFeasible()) {
            if (solutionBestGlobal.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon) {
                solutionBestGlobal.setSolution(solutionTemp);
            }
        }

        // check if temporary solution become new current solution
        if (this.tempSolutionIsAccepted(solutionTemp, solutionCurrent)) {
            return solutionTemp;
        }
        return solutionCurrent;

//        // check if temporary solution become new current solution
//        if (solutionCurrent.isFeasible()) {
//            if (solutionTemp.isFeasible()) {
//                // improvement
//                if (solutionCurrent.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon) {
//                    // check if also better than best global
////                    if (solutionBestGlobal.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon) {
////                        solutionBestGlobal.setSolution(solutionTemp);
////                    }
//                    return solutionTemp;
//                }
//            }
//        } else { // if no feasible solution found yet
//            // improvement
//            if (solutionCurrent.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon) {
//                return solutionTemp;
//            }
//        }
//
//        // TODO: feasible nur relevant fuer beste globale loesung
//        // was current solution feasible ?
//        // TODO hier kommt dann wahrscheinlichkeit etc rein, dass trotzdem schlechtere loesung
//        //  angenommen wird
//
//        // no improvement
//        return solutionCurrent;
    }

    private boolean tempSolutionIsAccepted(Solution solutionTemp, Solution solutionCurrent) {
        // improvement ?
        return solutionCurrent.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon;
    }

    private AbstractInsertion getRepairOperatorAtRandom() {
        int idx = Config.randomGenerator.nextInt(this.repairOperators.length);
        return this.repairOperators[idx];
    }

    private AbstractRemoval getDestroyOperatorAtRandom() {
        int idx = Config.randomGenerator.nextInt(this.destroyOperators.length);
        return this.destroyOperators[idx];
    }

}
