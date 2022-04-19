package vrptwfl.metaheuristic.alns.removals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import vrptwfl.metaheuristic.Config;
import vrptwfl.metaheuristic.common.Solution;
import vrptwfl.metaheuristic.data.Data;
import vrptwfl.metaheuristic.exceptions.ArgumentOutOfBoundsException;
import vrptwfl.metaheuristic.utils.CalcUtils;
import vrptwfl.metaheuristic.utils.DataUtils;

/**
 * This class implements the KMeans Cluster Removal heuristic.
 * 
 * @author Christian M.M. Frey 
 *
 */
public class ClusterKMeansRemoval extends AbstractRemoval {

	private int k;
	private int[] centroidLocIdx;
	private int[] customerAssignmentToClusters;
	
	public ClusterKMeansRemoval(Data data, int k) {
		super(data);
		this.k = k;
		this.centroidLocIdx = new int[this.k];
		// TODO - check depot assignment
		this.customerAssignmentToClusters = new int[data.getCustomers().length + 1];
		Arrays.fill(this.customerAssignmentToClusters, -1);
	}

	/**
	 * Initialize centroids randomly from all possible locations.
	 */
	private void initCentroids (Solution solution) {
		int[] locationids = DataUtils.getLocationIdxOfAllCustomers(solution);
		List<Integer> shuffledLocs = Arrays.stream(locationids).boxed().collect(Collectors.toList());
		Collections.shuffle(shuffledLocs);
		List<Integer> usedLocs = new ArrayList<Integer>();
		usedLocs.add(0);
		int idx = 0;
		for (int i = 0; i<shuffledLocs.size(); i++) {
			if (shuffledLocs.get(i)!= -1 & !usedLocs.contains(shuffledLocs.get(i))) {
				this.centroidLocIdx[idx] = shuffledLocs.get(i);
				usedLocs.add(shuffledLocs.get(i));
				idx++;
				if (idx == this.k) return;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	List<Integer> operatorSpecificDestroy(Solution solution, int nRemovals) throws ArgumentOutOfBoundsException {
		List<Integer> removedCustomers = new ArrayList<>();

		// Run k-Means algorithm
		runKMeans(solution);
		
		// shuffle cluster ids
        List<Integer> clusterIds = IntStream.rangeClosed(0, this.k-1).boxed().collect(Collectors.toList());
        Collections.shuffle(clusterIds);
        
        clusterIteration:
        for (int c_id = 0; c_id<clusterIds.size(); c_id++) {
        	// Get cluster ids whose datapoints/customers will be removed
        	int currentClusterIdToRemove = clusterIds.get(c_id);
        	// Iterate customers
        	for (int customerId=1; customerId<this.customerAssignmentToClusters.length; customerId++) {
        		// Check if customer is assigned to current cluster
        		if (this.customerAssignmentToClusters[customerId] != currentClusterIdToRemove)
        			continue;
        		
        		// Remove customer assigned to the current cluster from the solution
        		solution.getVehicles().get(solution.getCustomersAssignedToVehicles()[customerId]).applyRemovalForCustomer(customerId, this.data, solution);
        		removedCustomers.add(customerId);
        		nRemovals --;
        		
        		// if n removals have been processed -> exit
        		if (nRemovals == 0) 
        			break clusterIteration;		
        	}
        }
        // return list of removed Customers
		return removedCustomers;
	}
	
	/**
	 * Runs the KMeans algorithm on the attached solution object carrying the 
	 * information of the currently scheduled customers. 
	 * @param solution: solution object
	 */
	private void runKMeans (Solution solution) {
		
		boolean finish = false;
		int maxIter = 1000;
		int iteration = 0;
		
		double sum_squared_err = Double.MAX_VALUE;
		double PRECISION = Config.epsilon;
		
		// initialize & reset kmeans settings
		Arrays.fill(this.customerAssignmentToClusters, -1);
		this.initCentroids(solution);
		
		while (!finish && iteration < maxIter) {
			// assign points to the closest cluster
			assignToCentroids(solution);
			
			// calculate new centroids
			calculateCentroids(solution);
			
			// check for improvement of the clustering
			double new_sum_squared_err = calculateTotalSSE(solution);
			if (sum_squared_err - new_sum_squared_err <= PRECISION)
				finish = true;
			sum_squared_err = new_sum_squared_err;
			
			// increase iteration number
			iteration ++;
		}
	}
	
	/**
	 * Aggregates the total distance between the currently scheduled customers within
	 * a solution and theirs cluster centroids.
	 * @param solution: current solution object
	 * @return total distance between customers and theirs centroids
	 */
	private double calculateTotalSSE(Solution solution) {
		double totalDist = 0.0;
		// Iterate customer
		for (Integer customerID : solution.getAssignedCustomers()) {
			// get location id of customer
			int customerLocId = DataUtils.getLocationIndex(customerID, solution);
			// get location of assigned cluster
			int clusterAssignment = this.customerAssignmentToClusters[customerID];
			int clusterLocation = this.centroidLocIdx[clusterAssignment];
			// Aggregate distance value
			totalDist += solution.getData().getDistanceMatrix()[customerLocId][clusterLocation];
		}
		return totalDist;
	}
	
	/**
	 * Calculate the new centroids after the assignment step.
	 * @param solution: current solution object
	 */
	private void calculateCentroids(Solution solution) {
		for (int clusterId = 0; clusterId < this.k; clusterId++) {
			double[] distances = new double[this.customerAssignmentToClusters.length];
			for (int i = 0; i < solution.getAssignedCustomers().size()-1; i++) {
				int customerI = solution.getAssignedCustomers().get(i);
				if (this.customerAssignmentToClusters[customerI] != clusterId)
					continue;
					
				int locI = DataUtils.getLocationIndex(customerI, solution);

				for (int j=i+1; j < solution.getAssignedCustomers().size(); j++) {
					int customerJ = solution.getAssignedCustomers().get(j);
					if (this.customerAssignmentToClusters[customerJ] != clusterId)
						continue;
					
					int locJ = DataUtils.getLocationIndex(customerJ, solution);
					distances[customerI] += solution.getData().getDistanceMatrix()[locI][locJ];
					distances[customerJ] += solution.getData().getDistanceMatrix()[locJ][locI];
				}
			}
			int minCustomerIdx = findMinCentroidIdx(distances, clusterId);
			this.centroidLocIdx[clusterId] = DataUtils.getLocationIndex(minCustomerIdx, solution);
		}
	}
	
	/**
	 * Find index of the new cluster centroid with the id being attached as parameter.
	 * @param distances aggregated distances of datapoints
	 * @param clusterID cluster id
	 * @return identifier of new cluster centroids
	 */
	private int findMinCentroidIdx(double[] distances, int clusterID) {
		int idx = -1;
		double minVal = Double.MAX_VALUE;
		for (int i = 1; i<distances.length; i++) {
			// check if customer belongs to cluster
			if (this.customerAssignmentToClusters[i] != clusterID)
				continue;
			// check aggregated distance values -> return index of minimal dist.
			if (distances[i] < minVal)  {
				idx = i ;
				minVal = distances[i];
			}
		}
		return idx;
	}
	
	/**
	 * Assigns the scheduled customers to the nearest centroid.
	 * @param solution: solution object the kMeans operates on
	 */
	private void assignToCentroids(Solution solution) {
		// Iterate customer
		for (Integer customerID : solution.getAssignedCustomers()) {
			// get location id of customer
			int customerLocId = DataUtils.getLocationIndex(customerID, solution);
			
			// initialize distance value to max value
			double minVal = Integer.MAX_VALUE;
			// iterate clusters
			for (int clusterId = 0; clusterId<this.centroidLocIdx.length; clusterId++) {
				// Get distance between customer and centroid
				double dist = solution.getData().getDistanceBetweenLocations(customerLocId, this.centroidLocIdx[clusterId]);
				// check for better distance
				if (dist<minVal) {
					// set customer assignment to nearest centroid
					this.customerAssignmentToClusters[customerID] = clusterId;
					minVal = dist;
				}
			}
		}		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getFormattedClassName() {
		return "Cluster k-means (k=" + this.k + ")";
	}
}
