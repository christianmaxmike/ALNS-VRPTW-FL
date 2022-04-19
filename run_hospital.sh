#!/bin/bash
##
## Hospital Instances - patients: 20
##
for b in {1, 2, 6}
do 
	for f in {6, 3, 1}
	do
		for v in {1..5}
		do
			java -jar ALNS-VRPTW-FL-main-0.0.1-SNAPSHOT-jar-with-dependencies.jar "hospital_instance_i020_b$b_f$f_v0$v.json"
		done
	done
done

##
## Hospital Instances - patients: 40
##
# for b in {1, 2, 6}
# do 
# 	for f in {6, 3, 1}
# 	do
# 		for v in {1..5}
# 		do
# 			java -jar ALNS-VRPTW-FL-main-0.0.1-SNAPSHOT-jar-with-dependencies.jar "hospital_instance_i040_b$b_f$f_v0$v.json"
# 		done
# 	done
# done

##
## Hospital Instances - patients: 60
##
#for b in {1, 2, 6}
#do 
#	for f in {6, 3, 1}
#	do
#		for v in {1..5}
#		do
#			java -jar ALNS-VRPTW-FL-main-0.0.1-SNAPSHOT-jar-with-dependencies.jar "hospital_instance_i060_b$b_f$f_v0$v.json"
#		done
#	done
#done


##
## Hospital Instances - patients: 80
##
#for b in {1, 2, 6}
#do 
#	for f in {6, 3, 1}
#	do
#		for v in {1..5}
#		do
#			java -jar ALNS-VRPTW-FL-main-0.0.1-SNAPSHOT-jar-with-dependencies.jar "hospital_instance_i080_b$b_f$f_v0$v.json"
#		done
#	done
#done

##
## Hospital Instances - patients: 100
##
#for b in {1, 2, 6}
#do 
#	for f in {6, 3, 1}
#	do
#		for v in {1..5}
#		do
#			java -jar ALNS-VRPTW-FL-main-0.0.1-SNAPSHOT-jar-with-dependencies.jar "hospital_instance_i100_b$b_f$f_v0$v.json"
#		done
#	done
#done

##
## Hospital Instances - patients: 120
##
#for b in {1, 2, 6}
#do 
#	for f in {6, 3, 1}
#	do
#		for v in {1..5}
#		do
#			java -jar ALNS-VRPTW-FL-main-0.0.1-SNAPSHOT-jar-with-dependencies.jar "hospital_instance_i120_b$b_f$f_v0$v.json"
#		done
#	done
#done
