#!/bin/bash
for i in 0 5 10 15 20 25
do
	java -jar RandomEvaluationsWOM_M.jar "${i}TP/input_aw_wom_new${i}touchpoints.json" "${i}TP/sampling_aw_wom_new${i}touchpoints.csv"
	echo " Finished folder ${i}TP"
done
