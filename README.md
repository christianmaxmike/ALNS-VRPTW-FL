# ALNS-VRPTW-FL

> Christian M.M. Frey, Alexander Jungwirth, Markus Frey, Rainer Kolisch 


Implementation of "Adaptive Large Neighborhood Search (ALNS) for the Vehicle Routing Problem with Time Windows, Flexible Service Locations and Time-dependent Location Capacity". 

## Usage
### Import project in eclipse
Download the project (clone git) and import the project in your favorite IDE (eclipse, netbeans, ...).

### Usage .jar
Using the .jar, there are two default modes one can call: instance-specific run or run the parameter tuning with a predefined set of instances. 
The mode is defined by the first integer in the list of arguments.

		java -jar <filename>.jar <mode> ...further args

# Mode: *0* - Instance-specific mode
Using the mode 0, we can run the ALNS framework on a specific instance being defined in the list of arguments. 
The ordering of the arguments are as follows:

		java -jar <filename>.jar 0 <out_dir> <instanceName> <nCustomers> <resultFile> <configFile>

- out_dir: defines the dictionary's name where the output files are written
- instanceName: defines the name of the instance being processed
- nCustomers: defines how many customers are processed (important for the solomon instances; for hospital instances any number is valid (nCustomers is defined by the input file))
- resultFile: A general output file, where the name of the instance is written, the total costs, the elapsed running time and the optimality gap (default: "result.txt")
- configFile: the config file being used (default: "config.yaml")

# Mode *1* - hyperparameter tuning
The other mode is used for the hyperparameter tuning. Here a pre-defined set of instances are used and each run modifies the variables defined
in the configuration file in pre-defined bounds. The ordering of the raguments are as follows:

		java jar <filename>.jar 1 <out_dir> <nCustomers> <numConfigs> <numRunsPerConfig> <resultFile> <configFile>

- out_dir: defines the dictionariy_s name where the output files are written
- nCustomers: defines how many customers are processed (important for the solomon instances;)
- numConfigs: numbeer of config files being generated and tested
- numRunsPerConfig: in order to mesaure statistical significance, x runs are evaluated for each config and the average optimality gap is calculated
- resultFile: A general output file, wher ethe name of the instance is written, the total costs, the elapsed running time and the optimality gap (default: "result.txt")
- configFile: the ocnfig file being used as initial starting point. This defines the configuration file which is modulated

## Logged files
In the output dictionary defined in the arguments list, there are a number of logging files:
- allTours.json
	* this .json files captures tours after each iteration. The outermost key defines the iteration number. The second key defines a vehicle's
	  id whithin the tour. The innermost arrays defines the customer being scheduled for a vehicle. 
	  The ordering of values of the innermost array are as follows:  
		+ 0: customer id
		+ 1: original customer id
		+ 2: position in the route
		+ 3: location id where the customer is served
		+ 4: preferred location id of the customer
		+ 5: location's capacity slot a customer is scheduled
		+ 6: customer's service time
		+ 7: start service time
		+ 8: end service time
		+ 9: start of customer's planning horizon
		+ 10: end of customer's planning horizon
		+ 11: distance from the predecessor's location to the customer's one
		+ 12: distance from the customer's location toe the successor job
		+ 13: distance to the customer's preferred location
	  
- backTrackingLog.csv
	* this .csv file captures the information of the backtracking mechanism. Four values are stored
		+ trial: the current run of the backtracking procedure
		+ noJumps: number of backtracking jumps;
		+ bestCosts: the best observed costs during the backtracking process
		+ time: the elapsed time

- config.json
	* this .json file writes out all the values being defined in the configuration file. 

- finalTour.txt
	* in this .txt file the unformatted tour is written. The final scheduling as well as the costs are written out.

- finalTourCSV.csv
	* this .csv file stores the final scheduling in a formatted output. The separated values in the .csv file store the following information:
		+ vehicleID : vehicle id
		+ customerID : customer id
		+ originalCustomerID : original customer id
		+ servedLoc	: location id where the customer is scheduled
		+ preferredLoc : preferred location id of the customer
		+ capacitySlot : location's capacity slot a customer is scheduled
		+ duration : customer's srevice time
		+ starttime : start service time
		+ endtime : end service time
		+ customersStartTime : start of customer's planning horizon
		+ customersEndTime : end of customer's planning horizon
		+ distFrom : distance from the predecessor's location to the customer's one
		+ distTo : distance from the customer's location toe the successor job
		+ distToPreferredLoc : distance to the customer's preferred location

- initialTourCSV.csv
	* this .csv file stores the scheduling after the construction phase. The sepearated values in the .csv file are the same as the one for the finalTour.csv.

- logCosts.csv
	* this .csv file stores the costs of the current, temporary, global and the best global feasible solution. The columns are as follows:
		+ instanceName : name of the current instance being processed
		+ iteration : the current iteration id
		+ GlobalCosts : the costs of the global solution
		+ TmpCosts : costs of the temporary solution
		+ CurrCosts : costs of the current solution
		+ BestFeasibleCosts : cost of the best feasible solution (if there is a feasible solution)
		+ isFeasible : is a feasible solution found
		+ timeElapsed : elapsed running time 
		+ temperature : value of the temperature variable
		+ simulatedAnnealingRandomVal : value of the simulated annealing
		+ DestroyOp : which destroy operation has been used in the current iteration
		+ InsertionOp : which insertion operation has been used in the current iteration
		+ nRemovals : how many customers have been removed from the current scheudling
		+ GlobalCosts_var : global costs with the dynamic cost function (= cost function with dynamic GLS factors)
		+ TmpCosts_var : temporary costs with the dynamic cost function (= cost function with dynamic GLS factors)
		+ CurrCosts_var : current costs with the dynamic cost function (= cost function with dynamic GLS factors)
		+ BestFeasible_var : best feasible costs with the dynamic cost function (=cost function with dynamic GLS factors)

- logPenalties.csv
	*  this .csv files stores information about the penalties which occured whenever a new best global solution is found. The values are as follows:
		+ iteration: the iteration number a penalty occurred
		+ total : the total number of penalties
		+ Unscheduled : number of unscheduled customers
		+ TWViolation : number of time window violations
		+ Predecessor : number of predecessor violations
		+ Capacity : number of capacity violations
		+ SkillLvl : number of skill level violations
		+ cumulatedTWDelta : cumulated delta value of all time window violations
		+ cumulatedSkillDelta : cumulated delta value of all skill violations
		+ swappingCosts : costs for swapping locations (occurs when a customer is not scheduled in its preferred location)

- penaltiesDetailed.json
	* This .json file captures detailed information about the penalties for the current scheduling. The values are captured whenever there is an update in the GLS weights. 
      The outermost key indicate the iteration number. The second key refers to a customer's id. The innermost keys  "TWViolation", "Unscheduled", "Predecessor", "SkillLvl", "Capacity", "originalCustomerID"
	  refers to the respective feature weights for the penalties as well as the original customer's id

- removalProbabilities.csv
	* This .csv file captures the probabilities of picking a specific removal operator.

- repairProbabilities.csv
	* This .csv file captures the probabilities of picking a specific insertion operator.

- summary.csv
	* This .csv file stores a summary of the scheduling after a certain number of iterations (hard-coded as 1000). The following values are captured:
		+ iteration : iteration number
		+ instanceName : name of the instance
		+ nCustomers : number of customers 
		+ nVehicles	: number of available vehicles
		+ nVehiclesUsed : number of vehicles being used 
		+ notScheduledCustomers : number of not scheduled customers
		+ elapsedTime : elapsed running time 
		+ totalCosts : the total cost of the current solution 
		+ RoutingCosts : the vehicle's routing costs
		+ PenaltyCosts : costs for the penalties
		+ SwappingCosts : costs for swapping locations

- unscheduledInfo.csv
	* This .csv file stores the information about unscheduled customers. The columns indicate the following: 
		+ customer : customer's id
		+ originalCustomerID : original customer id
		+ customersStartTime : customer's start of planning horizon
		+ customersEndTime : customer's end of planning horizon
		+ serviceTime : customer's service time
		+ preferredLocation : the customer's preferred location


## Dependencies
Import the project as Maven project by including the following dependencies:
```
  <dependencies>
	<dependency>
	  <groupId>com.google.guava</groupId>
	  <artifactId>guava</artifactId>
	  <version>31.0.1-jre</version>
	  <!-- or, for Android: -->
	  <!--<version>31.0.1-android</version>-->
	</dependency>

	<!-- https://mvnrepository.com/artifact/com.google.code.gson/gson -->
	<dependency>
	    <groupId>com.google.code.gson</groupId>
	    <artifactId>gson</artifactId>
	    <version>2.9.0</version>
	</dependency>

	<dependency>
	    <groupId>org.yaml</groupId>
	    <artifactId>snakeyaml</artifactId>
	    <version>1.30</version>
	</dependency>

	<!-- https://mvnrepository.com/artifact/org.slf4j/slf4j-api -->
	<dependency>
	    <groupId>org.slf4j</groupId>
	    <artifactId>slf4j-api</artifactId>
	    <version>1.7.35</version>
	</dependency>

	<!-- https://mvnrepository.com/artifact/junit/junit -->
	<dependency>
	    <groupId>junit</groupId>
	    <artifactId>junit</artifactId>
	    <version>4.13.2</version>
	    <scope>test</scope>
	</dependency>

	<dependency>
	    <groupId>com.googlecode.json-simple</groupId>
	    <artifactId>json-simple</artifactId>
	    <version>1.1.1</version>
	</dependency>

  </dependencies>
```


## Instances

**Solomon-Instances** were taken from [here](http://web.cba.neu.edu/~msolomon/problems.html).

**Hospital-Instances** are publicly available [here](https://zenodo.org/record/6772201#.YscDBZDP3Ko).


## Cite

Please cite [our paper](https://www.sciencedirect.com/science/article/abs/pii/S0377221722008888) if you use this code in your own work:

```
@article{FREY_2023,
title = {The vehicle routing problem with time windows and flexible delivery locations},
journal = {European Journal of Operational Research},
volume = {308},
number = {3},
pages = {1142-1159},
year = {2023},
issn = {0377-2217},
doi = {https://doi.org/10.1016/j.ejor.2022.11.029},
url = {https://www.sciencedirect.com/science/article/pii/S0377221722008888},
author = {Christian M.M. Frey and Alexander Jungwirth and Markus Frey and Rainer Kolisch},
keywords = {Routing, Location capacity, Metaheuristic, OR in health services}
}
```
