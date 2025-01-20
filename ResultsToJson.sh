#!/bin/bash

# Options:: 
# --repeate=it		Runs "it" executions for each problem.
# --start=i			Start the algorithm executions from "i".
# --single=i		Runs a single execution using the "i-th" iteration seed.
# --real-coding 	Enables real coding.
# --algorithm 		ECJ file

for i in 0 5 7 10 12 15 17 20 22 25 30 35
do
	for algo in CMA-ES-INT CMA-ES-HC-INT;
		do 
			java -jar /home/ignacio/proyectos/mo-calibration/csvResultsToJson.jar "/home/ignacio/Dropbox/CRO/instances/${i}TP/input_aw_wom_new${i}touchpoints.json" "./${i}TP/" "${algo}"
		done
done
