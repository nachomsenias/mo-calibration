#!/bin/bash

root=$(pwd)
#echo ${root}

for i in 0 5 7 10 12 15 17 20 22 25 30 35 40 45 50
do
	cd "./${i}TP/WOMM/data/"
	gnuplot front_comparison_short_small.plt
	cd ${root}
	cp "./${i}TP/WOMM/data/front_comparison_small.png" "./${i}TP_front_comparison_small.png"
done