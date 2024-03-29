package vrptwfl.metaheuristic.alns.removals;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.utils.DataUtils;

import java.util.*;

// TODO Alex - if multiple locations
//  option 1: remove customers based on closest current location s
//  option 2: remove customers based on closest possible locations
/**
 * This class implements the Cluster Kruskal removal heuristic.
 * (cf. Ropke & Pisinger 2006 EJOR 171, pp. 750-775)
 * 
 * @author: Alexander Jungwirth, Christian M.M. Frey
 */
public class ClusterKruskalRemoval extends AbstractRemoval {

    /**
     * Constructor for the cluster kruskal removal heuristic.
     * @param data: data object
     */
    public ClusterKruskalRemoval(Data data) {
        super(data);
    }

	/**
	 * {@inheritDoc}
	 * Executes the removal.
	 */
    @Override
    List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) {        
    	// Get location ids before any removals are processed (assignment to locations will also be deleted for a removal)
    	int[] customerLocations = DataUtils.getLocationIdxOfAllCustomers(solution);
        
    	List<Integer> removedCustomers = new ArrayList<>();

        // choose route at random
        ArrayList<Vehicle> vehicles = new ArrayList<>(solution.getUsedVehicles()); // only vehicle which contain customers
        if (vehicles.isEmpty()) 
        	return removedCustomers; // if no vehicle contains customers

        boolean[] triedVehicle = new boolean[solution.getVehicles().size()];

        Collections.shuffle(vehicles); // randomly sorted vehicles
        Vehicle firstVehicle = vehicles.get(0);
        triedVehicle[firstVehicle.getId()] = true;

        // create first cluster
        ArrayList<Integer> customersToRemove = getCustomersToRemove(firstVehicle, solution);
        
        // apply removals
        for (Integer customer: customersToRemove) {
            removedCustomers.add(customer);
            firstVehicle.applyRemovalForCustomer(customer, this.data, solution);
            nRemovals--;
        }

        // still more removals needed (nRemovals not yet reached) and there is vehicles left from which customers can be removed
        while (nRemovals > 0 && !solution.getUsedVehicles().isEmpty()) {
            // randomly select customer already removed
            int idxC = Config.getInstance().randomGenerator.nextInt(removedCustomers.size());
            Integer referenceCustomer = removedCustomers.get(idxC);
            // int firstCustomerPreferencedLocation = customerLocationReferences[referenceCustomer];
            // int referenceCustomerLocationIdx = DataUtils.getLocationIndex(referenceCustomer, solution);
            int referenceCustomerLocationIdx = customerLocations[referenceCustomer];

            // int referenceCustomerLocationIdx = DataUtils.getLocationIndex(referenceCustomer.intValue(), customerLocationReferences[referenceCustomer.intValue()], solution.getData());
            
            // find customer close to reference customer (however, preferably one from a tour that has not yet been processed)
            // TODO_DONE Chris - adapt to multiple locations
            // double[] distanceToFirstCustomer = this.data.getDistanceMatrix()[referenceCustomer];
            double[] distanceToFirstCustomer = this.data.getDistanceMatrix()[referenceCustomerLocationIdx];
            ArrayList<double[]> closest = new ArrayList<>();

            // add all customers already assigned to the vehicles
            // TODO Alex: check if vehicle schon angefasst wurde (this new request should come from an untouched route)
            for (Vehicle vehicle: solution.getVehicles()) {
                if (!vehicle.isUsed()) continue; // if vehicle is not used
                if (triedVehicle[vehicle.getId()]) continue; // if cluster has been build from this vehicle already
                for (int customer: vehicle.getCustomers()) {
                    if (customer == 0) 
                    	continue;
                    // int customerPreferencedLocation = customerLocationReferences[customer];
                    // int customersLocation = DataUtils.getLocationIndex(customer, solution);
                    // int customersLocation = DataUtils.getLocationIndex(customer, solution);
                    int customersLocation = customerLocations[customer];
                    closest.add(new double[] {customer, vehicle.getId(), distanceToFirstCustomer[customersLocation]});
                    // closest.add(new double[] {customer, vehicle.getId(), distanceToFirstCustomer[customer]});
                }
            }

            // if no closest customer has been found
            if (closest.isEmpty()) {
                // reset tried vehicles (now also vehicles already processed can be used to build another cluster)
                triedVehicle = new boolean[solution.getVehicles().size()];
                continue;
            }

            // --- remove customers from vehicle ---
            closest.sort(Comparator.comparing(v->v[2]));  // sort according to distance (smallest distance first)
            Vehicle vehicle = solution.getVehicles().get((int) closest.get(0)[1]); // get vehicle of closest customer
            triedVehicle[vehicle.getId()] = true;

            // create  cluster
            customersToRemove = getCustomersToRemove(vehicle, solution);
            // apply removals
            for (Integer customer: customersToRemove) {
                removedCustomers.add(customer);
                vehicle.applyRemovalForCustomer(customer, this.data, solution);
                nRemovals--;
                if (nRemovals <= 0) break;
            }
        }

        return removedCustomers;
    }

    /**
     * Retrieve customer ids which are shall be removed.
     * @param vehicle: vehicle object to get customers scheduled within a tour
     * @param solution: solution object
     * @return list of customers to be removed
     */
    private ArrayList<Integer> getCustomersToRemove(Vehicle vehicle, Solution solution) {
        ArrayList<Integer> customersToRemove = new ArrayList<>();
        if (vehicle.getnCustomersInTour() < 3) {
            // remove all customers in tour
            customersToRemove.addAll(vehicle.getRealCustomers());
        } else {
            // partition customers in route into two clusters
            // apply Kruskal's algorithm but stop when two disconnected parts are left
            // choose one cluster at random, and remove customers
            customersToRemove = this.applyKruskalToGetCustomersToBeRemoved(vehicle, solution);
        }
        return customersToRemove;
    }

    /**
     * Applies the kruskal algorithm to get the customer which shall be removed.
     * The idea is to generate two connected graphs defined with the kruskal algorithm
     * and remove the customers belonging to one of them.
     *  src: KIT 08: Minimum Spanning Trees
     *       slides 257-
     *       https://www.youtube.com/watch?v=99FvOZTogzA
     * @param vehicle: vehicle object
     * @param solution: solution object
     * @return list of customers identifiers which are removed
     */
    private ArrayList<Integer> applyKruskalToGetCustomersToBeRemoved(Vehicle vehicle, Solution solution) {

        int nNodes = vehicle.getnCustomersInTour();
        // DEBUG Alex: wieder raus
        //  System.out.println("Vehicle " + vehicle.getId());
        //  System.out.println("nNodes " + nNodes);

        // T: UnionFind(n)
        UnionFind T = new UnionFind(nNodes);

        // sort E in ascending order of weight
        List<Edge> edges = this.createEdges(vehicle, solution);
        Collections.sort(edges);

        // kruskal(E)
        // PROCEDURE kruskal(E)
        int nClusters = nNodes; // initially each node has it own cluster
        //   FOREACH (u,v) \in E DO:
        for (Edge edge: edges) {
            // u' := T.find(u)
            int u2 = T.find(edge.source); // find representative of u
            int v2 = T.find(edge.destination); // find representative of v
            if (u2 != v2) {
                //       output(u,v) // (u,v) is edge of minimum spanning tree (if we needed the edges, we could e.g. add them to a list)
                //       T.link(u',v')
                T.link(u2, v2);

                nClusters--; // when two clusters are linked, there is one cluster less

                // check if only two component are left (not needed when looking for MST but here we want two clusters)
                if (nClusters == 2) break;
            }
        }

        ArrayList<Integer> positionsToBeRemoved = T.getTourPositionsToBeRemoved();

        ArrayList<Integer> customersToRemove = new ArrayList<>();
        for (Integer pos: positionsToBeRemoved) {
            customersToRemove.add(vehicle.getRealCustomers().get(pos));
        }

        return customersToRemove;
    }

    /**
     * create edges between customers.
     * @param vehicle: vehicle object
     * @param solution: solution object
     * @return list of edges
     */
    // edges consider positions of customers in tour (not the customer id)
    private ArrayList<Edge> createEdges(Vehicle vehicle, Solution solution) {
        ArrayList<Edge> edges = new ArrayList<>();
        for (int i = 0; i < vehicle.getRealCustomers().size() - 1; i++) { // customer 0 and n are dummies
            for (int j = i + 1; j < vehicle.getRealCustomers().size(); j++) {
//                edges.add(new Edge(i, j, this.data.getDistanceBetweenCustomers(i,j)));
                int customerI = vehicle.getRealCustomers().get(i);
                int customerJ = vehicle.getRealCustomers().get(j);

                // TODO: Chris - Datautils get location
                int locCustomerI = solution.getData().getCustomersToLocations().get(solution.getData().getOriginalCustomerIds()[customerI]).get(solution.getCustomerAffiliationToLocations()[customerI]);
                int locCustomerJ = solution.getData().getCustomersToLocations().get(solution.getData().getOriginalCustomerIds()[customerJ]).get(solution.getCustomerAffiliationToLocations()[customerJ]);

                // graph assumes that nodes start at 0
                // Edge edge = new Edge(i, j, this.data.getDistanceBetweenCustomers(customerI,customerJ));
                // Edge edge = new Edge(i, j, solution.getDistanceBetweenCustomersByAffiliations(customerI,customerJ));
                Edge edge = new Edge(i, j, solution.getData().getDistanceBetweenLocations(locCustomerI,locCustomerJ));
//                edge.print();
                edges.add(edge);
            }
        }
        return edges;
    }

    /**
     * This class implements an edge used in the kruskal algorithm.
     * An edge as a source and destination point with costs being defined
     * for this connection.
     * 
     * @author Alexander Jungwirth
     */
    class Edge implements Comparable<Edge> {

        int source;
        int destination;
        double cost;

        /**
         * Constructor for an edge.
         * @param source: source node
         * @param destination: destination node
         * @param cost: cost for edge
         */
        public Edge(int source, int destination, double cost){
            this.source = source;
            this.destination = destination;
            this.cost = cost;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(Edge o) {
            return Double.compare(this.cost, o.cost);
        }

        /**
         * Prints the edge information.
         */
        public void print() {
            System.out.println("<" + this.source + ", " + this.destination + ">");
        }
    }

    /**
     * This class implements the Union-fin data structure.
     *  src: KIT 08: Minimum Spanning Trees
     *       slides 257-
     *       https://www.youtube.com/watch?v=99FvOZTogzA
	 *
     * @author Alexander Jungwirth
     */
    // union-find data structure (uses path compression and ranks)
    class UnionFind {

        int numberOfNodes;
        int[] parent; // parent information of each node

        /**
         * Constructor for the UnionFind data structure
         * @param nNodes: number of nodes
         */
        public UnionFind(int nNodes) {
            this.numberOfNodes = nNodes;
            this.parent = new int[this.numberOfNodes]; // at the beginning each node is its own parent
            Arrays.fill(this.parent, this.numberOfNodes + 1); // these are all representatives with rank 0
        }

        /**
         * find representative of tree: representative = unique identifier for subtree
         * path compression used
         * values between 1 and n are references to real parents, values greater than n are ranks
         * @param i: index
         * @return
         */
        public int find(int i) {
            if (this.parent[i] > this.numberOfNodes) {
                return i;
            } else {
                int i2 = find(this.parent[i]);
                this.parent[i] = i2;
                return i2;
            }
        }

        /**
         * Link two trees.
         * @param i: first node
         * @param j: second node
         */
        // 
        public void link(int i, int j) {
            // assert i and j are leaders of different subsets
            if (parent[i] < parent[j]) {
                parent[i] = j;
            } else if (parent[i] > parent[j]) {
                parent[j] = i;
            }
            else {
                // if both parents are equal (e.g. same rank)
                // if clusters are joined
                parent[j] = i;  // set parent of j to id of i
                parent[i]++;    // raise rank of i
            }
        }

        /**
         * Unions two subtrees.
         * @param i: first node
         * @param j: second node
         */
        public void union(int i, int j) {
            if (find(i) != find(j)) link(find(i), find(j));
        }

        /**
         * Returns the position within the routes which are removed
         * @return list of integers which are removed from the tours.
         */
        public ArrayList<Integer> getTourPositionsToBeRemoved() {

            ArrayList<Integer> positions = new ArrayList<>();
            // randomly choose either cluster 0 or 1
            int targetId = (Config.getInstance().randomGenerator.nextBoolean()) ? 0 : 1;


            int[] clusterIds = new int[parent.length];
            // process position 0 (determine representative for cluster 0)
//            clusterIds[0] = find(0); // wieder raus?
//            int origValueFirstCluster = clusterIds[0];
            int origValueFirstCluster = find(0); // find representative for cluster
            clusterIds[0] = 0; // set as belonging to cluster 0
            if (clusterIds[0] == targetId) positions.add(0);

            for (int i = 1; i < this.parent.length; i++) {
                // call find on each node such that the value is parent is the representative
//                clusterIds[i] = find(i);
//                clusterIds[i] = (clusterIds[i] == origValueFirstCluster) ? 0 : 1;
                clusterIds[i] = (find(i) == origValueFirstCluster) ? 0 : 1;
                if (clusterIds[i] == targetId) positions.add(i);
            }

            // DEBUG Alex: wieder raus
//            // set values for the two clusters to either 0 or 1
//            int origValueFirstCluster = clusterIds[0];
//            for (int i = 0; i < clusterIds.length; i++) {
//                clusterIds[i] = (clusterIds[i] == origValueFirstCluster) ? 0 : 1;
//            }
//
//            // add positions to list
//            for (int i = 0; i < clusterIds.length; i++) {
//                if (clusterIds[i] == targetId) positions.add(i);
//            }

            return  positions;
        }
    }
    
	/**
	 * {@inheritDoc}
	 */
	public String getFormattedClassName() {
		return "Cluster Kruskal";
	}
}
