package vrptwfl.metaheuristic.alns;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.alns.insertions.AbstractInsertion;
import vrptwfl.metaheuristic.alns.insertions.GreedyInsertion;
import vrptwfl.metaheuristic.alns.insertions.RegretInsertion;
import vrptwfl.metaheuristic.alns.removals.*;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;

import java.util.ArrayList;
import java.util.List;

public class ALNSCore {

    private Data data;

    private AbstractInsertion[] repairOperators;
    private AbstractRemoval[] destroyOperators;

    public ALNSCore(Data data) throws ArgumentOutOfBoundsException {
        this.data = data;

        this.initRepairOperators();
        this.initDestroyOperators();
    }

    private void initDestroyOperators() {
        List<AbstractRemoval> destroyList = new ArrayList<>();

        if (Config.useRandomRemoval) destroyList.add(new RandomRemoval(data));
        if (Config.useWorstRemovalRandom) destroyList.add(new WorstRemoval(data, true));
        if (Config.useWorstRemovalDeterministic) destroyList.add(new WorstRemoval(data, false));
        if (Config.useRandomRouteRemoval) destroyList.add(new RandomRouteRemoval(data));
        if (Config.useShawSimplifiedRandom) destroyList.add(new ShawSimplifiedRemoval(data, true));
        if (Config.useShawSimplifiedDeterministic) destroyList.add(new ShawSimplifiedRemoval(data, false));

        this.destroyOperators = new AbstractRemoval[destroyList.size()];
        this.destroyOperators = destroyList.toArray(this.destroyOperators);
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

    public Solution runALNS(Solution solutionConstr) {

        // init ALNS
        Solution solutionCurrent = solutionConstr.copyDeep();
        Solution solutionBestGlobal = solutionConstr.copyDeep();

        for (int iteration = 1; iteration <= Config.alnsIterations; iteration++) {
//        for (int iteration = 1; iteration <= 10_000; iteration++) {

            Solution solutionTemp = solutionCurrent.copyDeep();

            // TODO random auswaehlen aus Operatoren (geht das irgendwie mit Lambdas besser ?)

            // destroy solution
            getDestroyOperatorAtRandom().destroy(solutionTemp);

            // repair solution
            // returns one repair operator specified in repairOperators
            this.getRepairOperatorAtRandom().solve(solutionTemp);

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

        }

        return solutionBestGlobal;
    }

    // TODO hier brauchen wir auch noch Test cases
    private Solution checkImprovement(Solution solutionTemp, Solution solutionCurrent, Solution solutionBestGlobal) {

        // feasible solution ?
        // TODO hier kommt dann wahrscheinlichkeit etc rein, dass trotzdem schlechtere loesung
        //  angenommen wird
        if (solutionCurrent.isFeasible()) {
            if (solutionTemp.isFeasible()) {
                // improvement
                if (solutionCurrent.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon) {
                    // check if also better than best global
                    if (solutionBestGlobal.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon) {
                        solutionBestGlobal.setSolution(solutionTemp);
                    }
                    return solutionTemp;
                }
            }
        } else { // if no feasible solution found yet
            // improvement
            if (solutionCurrent.getTotalCosts() > solutionTemp.getTotalCosts() + Config.epsilon) {
                return solutionTemp;
            }
        }


        // no improvement
        return solutionCurrent;
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
