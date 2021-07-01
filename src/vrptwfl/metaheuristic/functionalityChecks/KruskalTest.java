package vrptwfl.metaheuristic.functionalityChecks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class KruskalTest {

    ArrayList<Edge> edges;
    int nNodes;

    public KruskalTest() {

        // graph taken from: https://www.youtube.com/watch?v=Yo7sddEVONg
        // note that node ids are decreased by one, i.e. nodes start here with id 0 instead of 1
        this.nNodes = 9;

        this.edges = new ArrayList<>();
        this.edges.add(new Edge(0,1,4.));
        this.edges.add(new Edge(0,7,8.));
        this.edges.add(new Edge(1,2,8.));
        this.edges.add(new Edge(1,7,11.));
        this.edges.add(new Edge(2,3,7.));
        this.edges.add(new Edge(2,5,4.));
        this.edges.add(new Edge(2,8,2.));
        this.edges.add(new Edge(3,4,9.));
        this.edges.add(new Edge(3,5,14.));
        this.edges.add(new Edge(4,5,10.));
        this.edges.add(new Edge(5,6,2.));
        this.edges.add(new Edge(6,7,1.));
        this.edges.add(new Edge(6,8,6.));
        this.edges.add(new Edge(7,8,7.));

    }

    public static void main(String[] args) {

        KruskalTest kruskal = new KruskalTest();

        System.out.println("Kruskal as per definition");
        kruskal.runKruskalsAlgo(false);
        System.out.println("\nKruskal but stop if only two clusters are left");
        kruskal.runKruskalsAlgo(true);
    }

    private void runKruskalsAlgo(boolean stopWhenTwoClustersAreLeft) {

        ArrayList<Edge> mst = new ArrayList<>();

        // T: UnionFind(n)
        UnionFind T = new UnionFind(this.nNodes);

        // sort edges
        Collections.sort(this.edges);

        // kruskal(E)
        // PROCEDURE kruskal(E)
        //   FOREACH (u,v) \in E DO:
        for (Edge edge: this.edges) {
            // u' := T.find(u)
            int u2 = T.find(edge.source); // find representative of u
            int v2 = T.find(edge.destination); // find representative of v
            if (u2 != v2) {
                // print addition information
                System.out.println("\n");
                System.out.println("u2: " + u2);
                System.out.println("v2: " + v2);

                edge.print();
                T.printParents();

                //       output(u,v) // (u,v) is edge of minimum spanning tree
                mst.add(edge);
                //       T.link(u',v')
                T.link(u2, v2);

                // print addition information
                System.out.println("nClusters:\t" + T.getNumberOfClusters());
                T.printParents();

                if (stopWhenTwoClustersAreLeft) {
                    // check if only two component are left (not needed when looking for MST but here we want two clusters)
                    // more costly than otherwise efficient MST implementation, but routes are rather small
                    if (T.getNumberOfClusters() == 2) break;
                }
            }
        }

        System.out.println("Print edges in MST");
        for (Edge edge: mst) {
            edge.print();
        }

        T.postprocessTwoClusterInformation();

    }

    // union-find data structure
    class UnionFind {

        int numberOfNodes;
        int[] parent; // parent information of each node

        public UnionFind(int nNodes) {
            this.numberOfNodes = nNodes;

            this.parent = new int[this.numberOfNodes]; // at the beginning each node is its own parent
            Arrays.fill(this.parent, this.numberOfNodes + 1); // these are all representatives with rank 0
        }

        public void postprocessTwoClusterInformation() {
            int[] clusterIds = new int[parent.length];
            for (int i = 0; i < this.parent.length; i++) {
                clusterIds[i] = find(i);
            }

            int origValueFirstCluster = clusterIds[0];
            for (int i = 0; i < clusterIds.length; i++) {
                clusterIds[i] = (clusterIds[i] == origValueFirstCluster) ? 0 : 1;
            }

            System.out.println(Arrays.toString(clusterIds));
        }

        public void printParents() {
            System.out.println(Arrays.toString(this.parent));
        }

        public int getNumberOfClusters() {
            int nClusters = 0;

            // all occurrences of n+1 numbers (references to node itself (singleton set))
            for (Integer i: this.parent) {
                if (i > this.numberOfNodes) nClusters++;
            }

            return nClusters;
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
                // if both parents are equal (e.g. same rank)
                // if clusters are joined
                parent[j] = i;  // set parent of j to id of i
                parent[i]++;    // raise rank of i
            }
        }

        public void union(int i, int j) {
            if (find(i) != find(j)) link(find(i), find(j));
        }

    }

    class Edge implements Comparable<Edge> {

        int source;
        int destination;
        double cost;

        public Edge(int source, int destination, double cost){
            this.source = source;
            this.destination = destination;
            this.cost = cost;
        }

        public void print() {
            System.out.println("<" + this.source + ", " + this.destination + ">");
        }

        @Override
        public int compareTo(Edge o) {
            return Double.compare(this.cost, o.cost);
        }
    }
}
