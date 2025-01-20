#!/bin/bash
urls=(-moar -moar2 -moar3 -moar4)

for iteration in {0..3}
do
	for alg in SPEA2-BLX NSGAII-BLX MOEAD MOMBI2 IBEA SMSEMOA GWASFGA;
	do
		for index in {0..9}
		do
			dest=$((iteration + 1))
			echo "Moving file: test${urls[$iteration]}/WOMM/data/${alg}/MWomABMProblem/FUN${index}.tsv"
			echo "to file: test/WOMM/data/${alg}/MWomABMProblem/FUN${dest}${index}.tsv"

			mv "test${urls[$iteration]}/WOMM/data/${alg}/MWomABMProblem/FUN${index}.tsv" "test/WOMM/data/${alg}/MWomABMProblem/FUN${dest}${index}.tsv"
			mv "test${urls[$iteration]}/WOMM/data/${alg}/MWomABMProblem/VAR${index}.tsv" "test/WOMM/data/${alg}/MWomABMProblem/VAR${dest}${index}.tsv"
		done
	done
done
