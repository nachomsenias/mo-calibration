#!/bin/bash

tp=(0 5 7 10 12 15 17 20 22 25 30 35)

algos=(SPEA2 SMSEMOA PESA2 NSGAII MOMBI2 MOEAD mIBEA IBEA GWASFGA)

for (( i = 0; i < 12; i++ )); do
	for (( j = 0; j < 9; j++ )); do
		for index in {0..9}
		do
			mv "${tp[i]}TP/moar/WOMM/data/${algos[j]}/MWomABMProblem/FUN${index}.tsv" "${tp[i]}TP/WOMM/data/${algos[j]}/MWomABMProblem/FUN1${index}.tsv"
			mv "${tp[i]}TP/moar/WOMM/data/${algos[j]}/MWomABMProblem/VAR${index}.tsv" "${tp[i]}TP/WOMM/data/${algos[j]}/MWomABMProblem/VAR1${index}.tsv"
		done	
	done
done

#for index in {0..9}
#do
#	mv "FUN${index}.tsv" "FUN1${index}.tsv"
#done	
