package vrptwfl.metaheuristic.alns.removals;

import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.common.Vehicle;
import vrptwfl.metaheuristic.data.Data;

import java.util.*;

// TODO: if multiple locations
//  option 1: remove customers based on closest current location s
//  option 2: remove customers based on closest possible locations

// Ropke & Pisinger 2006 EJOR 171, pp. 750-775
public class ClusterKruskalRemoval extends AbstractRemoval {

    public ClusterKruskalRemoval(Data data) {
        super(data);
    }

    @Override
    List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) {

        List<Integer> removedCustomers = new ArrayList<>();

        // choose route at random

        ArrayList<Vehicle> vehicles = new ArrayList<>(solution.getVehicles());
        Collections.shuffle(vehicles); // randomly sorted vehicles

        Iterator<Vehicle> vehicleIterator = vehicles.iterator();
        while (vehicleIterator.hasNext()) {
            // get vehicle to process
            Vehicle vehicle = vehicleIterator.next();
            vehicleIterator.remove();

            // only possible to remove customers if vehicle is used
            if (vehicle.isUsed()) {
                if (vehicle.getnCustomersInTour() < 3) {
                    // TODO loesche alle Kunden aus Tour
                } else {
                    // partition customers in route into two clusters
                    // apply Kruskal's algorithm but stop when two disconnected parts are left
                    // choose one cluster at random, and remove customers
                    // TODO nur wenn es mindestens 3 Kunden gibt
                    this.applyKruskal(vehicle);
                }
            }

            if (nRemovals <= 0) break;

            // still more removals needed (nRemovals not yet reached)
            //  - pick one of the removed requests
            //  - find request close to the chosen request (this new request should come from an untouched route)
            //    - route of new request is partitioned into two and one of the clusters is chosen at random and removed
        }




        return removedCustomers;
    }

    // KIT 08: Minimum Spanning Trees
    // slides 257-
    // https://www.youtube.com/watch?v=99FvOZTogzA
    private void applyKruskal(Vehicle vehicle) {
        // T: UnionFind(n)
        UnionFind T = new UnionFind(vehicle);

        // sort E in ascending order of weight
        List<Edge> edges = this.createEdges(vehicle);
        Collections.sort(edges);

        // kruskal(E)
        // PROCEDURE kruskal(E)
        //   FOREACH (u,v) \in E DO:
        for (Edge edge: edges) {
            // u' := T.find(u)
            int u2 = T.find(edge.source); // find representative of u
            int v2 = T.find(edge.destination); // find representative of v
            if (u2 != v2) {
                //       output(u,v) // (u,v) is edge of minimum spanning tree
                //       T.link(u',v')
                T.link(u2, v2);

                // check if only two component are left (not needed when looking for MST but here we want two clusters)
                // more costly than otherwise efficient MST implementation, but routes are rather small
                if (T.getNumberOfClusters() == 2) break;
            }
        }


    }

    // edges consider positions of customers in tour (not the customer id)
    private ArrayList<Edge> createEdges(Vehicle vehicle) {
        ArrayList<Edge> edges = new ArrayList<>();
        for (int i = 0; i < vehicle.getCustomers().size() - 1; i++) {
            for (int j = i + 1; j < vehicle.getCustomers().size(); j++) {
                edges.add(new Edge(i, j, this.data.getDistanceBetweenCustomers(i,j)));
            }
        }
        return edges;
    }

    class Edge implements Comparable<Edge> {

        int source;
        int destination;
        double cost;

        public Edge(int tail, int head, double cost){
            this.source = tail;
            this.destination = head;
            this.cost = cost;
        }

        @Override
        public int compareTo(Edge o) {
            return Double.compare(this.cost, o.cost);
        }
    }

    // union-find data structure
    class UnionFind {

        int numberOfNodes;
        int[] parent; // parent information of each node
        ArrayList<Integer> customers;

        public UnionFind(Vehicle vehicle) {
            this.numberOfNodes = vehicle.getnCustomersInTour();
            this.customers = vehicle.getCustomers();

            this.parent = new int[this.numberOfNodes]; // at the beginning each node is its own parent
            Arrays.fill(this.parent, this.numberOfNodes + 1); // these are all representatives with rank 0
        }

        public int getNumberOfClusters() {
            int nSelfReferences = 0;

            // all occurrences of n+1 numbers (references to node itself (singleton set))
            for (Integer i: this.parent) {
                if (i == this.numberOfNodes + 1) nSelfReferences++;
            }

            // all unique non n+1 numbers
            int nUniques = Arrays.stream(this.parent).distinct().toArray().length;
            if (nSelfReferences > 0) nUniques--; // ignore n+1 in uniques as we have already counted self references

            return nSelfReferences + nUniques;
        }

        // find representative of tree: representative = unique identifier for subtree
        // path compression used
        // values between 1 and n are references to real parents, values greater than n are ranks
        public int find(int i) {
            if (this.parent[i] > this.numberOfNodes) {
                return i;
            } else {
                int i2 = find(this.parent[i]);
                this.parent[i] = i2;
                return i2;
            }
        }

        // link two trees
        public void link(int i, int j) {
            // assert i and j are leaders of different subsets
            if (parent[i] < parent[j]) {
                parent[i] = j;
            } else if (parent[i] > parent[j]) {
                parent[j] = i;
            }
            else {
                // TODO was genau macht dieser Schritt?
                parent[j] = i;
                parent[i]++;
            }
        }

        public void union(int i, int j) {
            if (find(i) != find(j)) link(find(i), find(j));
        }

    }

}
