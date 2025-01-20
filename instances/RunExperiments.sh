#!/bin/bash
for i in 0 5 10 15 20 25
do
	java -jar ExperimentRunnerWOMM.jar "${i}TP/input_aw_wom_new${i}touchpoints.json" "${i}TP" > "${i}TP/jmetal.log"
	echo " Finished folder ${i}TP"
done
