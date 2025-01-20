# GNUPLOT WITH ERROR BARS
# FROM http://stackoverflow.com/questions/25512006/gnuplot-smooth-confidence-interval-lines-as-opposed-to-error-bars

# svg
#set terminal svg size 900,700 fname 'Helvetica, Arial, sans-serif' fsize '15' rounded dashed
#set output 'perceptionDeviation.svg'

set terminal png size 1500,900 enhanced font 'Helvetica, Arial, sans-serif' 
set output 'front_comparison.png'

# color definitions
#set style line 1 lc rgb '#FF0000' pt 13 ps 1.5 lt 2 lw 1 
#set style line 2 lc rgb '#98AD1F' pt 13 ps 1.5 lt 3 lw 1 
#set style line 3 lc rgb '#336633' pt 13 ps 1.5 lt 4 lw 1 

set style line 1 lc rgb '#d7191c' pt 13 ps 1.5 lt 3 lw 3 
set style line 2 lc rgb '#fdae61' pt 13 ps 1.5 lt 3 lw 3 
set style line 3 lc rgb '#2c7bb6' pt 13 ps 1.5 lt 3 lw 3 

set style line 4 lc rgb '#664D33' pt 6 ps 1.5 lt 4 lw 3 
set style line 5 lc rgb '#463366' pt 7 ps 1.5 lt 4 lw 3 
set style line 6 lc rgb '#808080' pt 7 ps 1.5 lt 4 lw 3 
set style line 7 lc rgb '#7fcdbb' pt 7 ps 1.5 lt 4 lw 3 

set style line 8 lc rgb '#c994c7' pt 6 ps 1.5 lt 3 lw 3
set style line 9 lc rgb '#98AD1F' pt 6 ps 1.5 lt 4 lw 3
set style line 16 lc rgb '#2380C2' pt 6 ps 1.5 lt 4 lw 3

set style line 11 lc rgb "#bbbbbb" lw 1 lt 0

# titles and axis labels
#set title "Pareto front awareness overtime vs word-of-mouth volume"

set xlabel "Awareness error (%)" font ",24" # x-axis label
set ylabel "WOM Volume error (%)" font ",20" # y-axis label

# remove border on top and right and set color to gray
set border 3 back ls 11
set tics nomirror

# define grid
#set grid back ls 12

set grid ytics lc rgb "#bbbbbb" lw 1 lt 0
set grid xtics lc rgb "#bbbbbb" lw 1 lt 0

#set grid xtics ytics mytics mytics ls 20

#set mxtics 4
#set mytics 4

# move legend to the correct place: available options are, left, right, top, bottom, outside, and below
#set key bottom right
set key top right
set key font ",24"
#set key spacing 2
#set key box
#set key width 3

# ranges for the plot
# only to show TRA
#set xrange [15:460]
#set yrange [18:55]

# for using CSV and separator
#set datafile separator ","


# draw the plot

set style fill transparent solid 0.2 noborder

plot 'SPEA2/MWomABMProblem/Objectives.tsv' title 'SPEA2' with points ls 1 pointtype 5, \
	'SPEA2-BLX/MWomABMProblem/Objectives.tsv' title 'SPEA2-BLX' with points ls 10 pointtype 5, \
	'MOEAD/MWomABMProblem/Objectives.tsv' title 'MOEAD' with points ls 3 pointtype 7, \
	'NSGAII/MWomABMProblem/Objectives.tsv' title 'NSGAII' with points ls 2 pointtype 9, \
	'NSGAII-BLX/MWomABMProblem/Objectives.tsv' title 'NSGAII-BLX' with points ls 11 pointtype 9, \
	'PESA2/MWomABMProblem/Objectives.tsv' title 'PESA2' with points ls 4 pointtype 4, \
	'MOMBI2/MWomABMProblem/Objectives.tsv' title 'MOMBI2' with points ls 5 pointtype 6, \
	'mIBEA/MWomABMProblem/Objectives.tsv' title 'mIBEA' with points ls 6 pointtype 8, \
	'IBEA/MWomABMProblem/Objectives.tsv' title 'IBEA' with points ls 7 pointtype 3, \
	'GWASFGA/MWomABMProblem/Objectives.tsv' title 'GWASFGA' with points ls 8 pointtype 2, \
	'SMSEMOA/MWomABMProblem/Objectives.tsv' title 'SMSEMOA' with points ls 9 pointtype 1, \
	'filtered_pareto_random.csv' title 'Random' with points ls 8
