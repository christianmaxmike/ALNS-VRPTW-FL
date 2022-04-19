#!/bin/bash
##
## SALOMON INSTANCE R101 - R112
##
for i in {1..9}
do 
	for cnt in {1..10}
	do
		java -jar ALNS-VRPTW-FL-main-0.0.1-SNAPSHOT-jar-with-dependencies.jar "R10$i"
	done
done
for i in {10..12}
do 
	for cnt in {1..10}
	do
		java -jar ALNS-VRPTW-FL-main-0.0.1-SNAPSHOT-jar-with-dependencies.jar "R1$i"
	done
done


##
## SALOMON INSTANCES R201 - R211
##
#for i in {1..9}
#do 
#  	java -jar ALNS-VRPTW-FL-main-0.0.1-SNAPSHOT-jar-with-dependencies.jar "R20$i"
#done
#for i in {10..11}
#do 
#	java -jar ALNS-VRPTW-FL-main-0.0.1-SNAPSHOT-jar-with-dependencies.jar "R2$i"
#done


##
## SALOMON INSTANCE 
##
#for i in {1..8}
#do 
#	java -jar ALNS-VRPTW-FL-main-0.0.1-SNAPSHOT-jar-with-dependencies.jar "RC10$i"
#done
#for i in {1..8}
#do 
#	java -jar ALNS-VRPTW-FL-main-0.0.1-SNAPSHOT-jar-with-dependencies.jar "RC20$i"
#done


##
## SALOMON INSTANCE C101 - C109
##
#for i in {1..9}
#do 
#	java -jar ALNS-VRPTW-FL-main-0.0.1-SNAPSHOT-jar-with-dependencies.jar "C10$i"
#done


##
## SALOMON INSTANCES C201 - C208
##
#for i in {1..8}
#do 
#	java -jar ALNS-VRPTW-FL-main-0.0.1-SNAPSHOT-jar-with-dependencies.jar "C20$i"
#done