package vrptwfl.metaheuristic;

import vrptwfl.metaheuristic.alns.ALNSCore;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.data.OptimalSolutions;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.instanceGeneration.SolomonInstanceGenerator;
import vrptwfl.metaheuristic.utils.CalcUtils;

import java.io.IOException;

public class MainALNS {

    // returns the objective function value of the ALNS solution
    public double runALNS(String instanceName, int nCustomers) throws ArgumentOutOfBoundsException {

        SolomonInstanceGenerator generator = new SolomonInstanceGenerator();
        Data data = null;
        try {
            data = generator.loadInstance(instanceName + ".txt", nCustomers);
        } catch (ArgumentOutOfBoundsException | IOException e) {
            e.printStackTrace();
        }

        this.setInstanceSpecificParameters(nCustomers);

        ConstructionHeuristicRegret construction = new ConstructionHeuristicRegret(data);
        long startTimeConstruction = System.currentTimeMillis();
        Solution solutionConstr = construction.constructSolution(2);

        // TODO wieder raus
        System.out.println(solutionConstr.getNotAssignedCustomers());
        System.out.println(solutionConstr.getTempInfeasibleCustomers());
        solutionConstr.printSolution();

        // ALNS
        ALNSCore alns = new ALNSCore(data);
        Solution solutionALNS = alns.runALNS(solutionConstr);

        long finishTimeConstruction = System.currentTimeMillis();
        long timeElapsed = (finishTimeConstruction - startTimeConstruction);
        System.out.println("Time for construction " + timeElapsed + " ms.");

        // TODO WICHTIG FEHLER SUCHEN BEI REMOVAL !!!

        // TODO brauchen irgendwas, um Lösung zu speichern (ZF und Touren startzeiten etc.)

        // TODO wieder raus
        System.out.println(solutionALNS.getNotAssignedCustomers());
        System.out.println(solutionALNS.getTempInfeasibleCustomers());
        solutionALNS.printSolution();

        // TODO check, ob es key ueberhaupt gibt, auch checken, ob es 25, 50 oder 100 Kunden sind

        int i = -1;
        if (nCustomers == 100) i = 2;
        else if (nCustomers == 50) i = 1;
        else if (nCustomers == 25) i = 0;
        double optimalObjFuncVal = OptimalSolutions.optimalObjFuncValue.get(instanceName)[i];
        double gap = CalcUtils.calculateGap(optimalObjFuncVal, solutionALNS.getTotalCosts());
        System.out.println("Gap: " + gap);



        // TODO morgen früh 28.05.2021
        //  1) Min- und Max-Anzahl removals pro iteration (siehe ALNS Paper)
        //  2) Test Vehicles
        //  3) Test Construction
        //  4) ggf. weiter Tests, wenn Solution object anders aussieht nach ALNS

        // TODO 2: tests für geladene instanzen
        // TODO 3: Logik ALNS anfangen (50_000 iteration random destroy, und regret repairs)

        // TODO 4: greedy repair

        // TODO moegliches hashing
        //  - bereits generierte Loesungen
        //  - ggf. earliest, latest possible starts in partial routes (pred_id, pred_time,)
        return 0.0;
    }

    private void setInstanceSpecificParameters(int nCustomers) {

        // set upper bound for number of removals in each ALNS iteration
        // (see Ropke & Pisinger 2006, p. 465 (An ALNS Heuristic for the PDPTW))
        int ub1 = Config.upperBoundRemovalsMax;
        int ub2 = (int) Math.round(nCustomers * Config.upperBoundRemovalsFactor);
        Config.upperBoundRemovals = Math.min(ub1,  ub2);

    }

    public static void main(String[] args) throws ArgumentOutOfBoundsException {

        final MainALNS algo = new MainALNS();
        algo.runALNS("R104", 100);


        // Add TimeLimit (?)
    }

    // TODO performance
    // - LRU cache (last recent usage)
}
