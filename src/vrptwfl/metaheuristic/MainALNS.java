package vrptwfl.metaheuristic;

import vrptwfl.metaheuristic.alns.ALNSCore;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.data.OptimalSolutions;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.instanceGeneration.SolomonInstanceGenerator;
import vrptwfl.metaheuristic.utils.CalcUtils;

import java.io.FileWriter;
import java.io.IOException;

public class MainALNS {
	
	private FileWriter writer;
	
	public MainALNS() {
		try {
			this.writer = new FileWriter("./results.txt", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    // returns the objective function value of the ALNS solution
    public double runALNS(String instanceName, int nCustomers) throws ArgumentOutOfBoundsException {

        SolomonInstanceGenerator generator = new SolomonInstanceGenerator();
        Data data = null;
        try {
            data = generator.loadInstance(instanceName + ".txt", nCustomers);
        } catch (ArgumentOutOfBoundsException | IOException e) {
            e.printStackTrace();
        }

        this.setInstanceSpecificParameters(nCustomers, data.getMaxDistanceInGraph());

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
        
        try {
			writer.append(instanceName+","+solutionALNS.getTotalCosts()+","+gap+","+timeElapsed+"\n");
	        writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

        // TODO 23.02.2022 ; Chris
        // ###
        // - simulated annealing  [Ropke&Pisinger, p.2416 COR]  // alter code könnte helfen
        // - adaptive Komponente: Wahrscheinlichkeit von destroy und insertion 
        //     nicht mehr uniformly distributed, sondern Wahrscheinlichkeit nach historischem 
        //     Erfolg p- und p+ (sigma-Werte)
        // - hashcode für einzelne solutions
        // 
        // !!Tracking für die profs; Was sind meine contributions!!
        // - Prüfen ob alle Operatoren Von Pisinger&Ropke mit aufgenommen worden sind für VRPTW
        //   - wenn ja: top
        //   - wenn nein, Implementieren!
        // ###

        // TODO morgen früh 28.05.2021 ; Alex
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

    private void setInstanceSpecificParameters(int nCustomers, double maxDistance) {

        // set upper bound for number of removals in each ALNS iteration
        // (see Pisinger & Ropke 2007, C&OR §6.1.1 p. 2417)

        int lb1 = Config.lowerBoundRemovalsMax;
        int lb2 = (int) Math.round(nCustomers * Config.lowerBoundRemovalsFactor);
        Config.lowerBoundRemovals = Math.min(lb1,  lb2);

        int ub1 = Config.upperBoundRemovalsMax;
        int ub2 = (int) Math.round(nCustomers * Config.upperBoundRemovalsFactor);
        Config.upperBoundRemovals = Math.min(ub1,  ub2);

        // set penalty (costs) for unserved customers
        Config.penaltyUnservedCustomer = maxDistance * Config.costFactorUnservedCustomer;

    }

    public static void main(String[] args) throws ArgumentOutOfBoundsException {
        final MainALNS algo = new MainALNS();
        algo.runALNS(args[0], 100);


        // Add TimeLimit (?)
    }

    // TODO performance
    // - LRU cache (last recent usage)
}
