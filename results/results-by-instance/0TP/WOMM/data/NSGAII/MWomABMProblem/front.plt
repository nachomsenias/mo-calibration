# GNUPLOT WITH ERROR BARS
# FROM http://stackoverflow.com/questions/25512006/gnuplot-smooth-confidence-interval-lines-as-opposed-to-error-bars

# svg
#set terminal svg size 900,700 fname 'Helvetica, Arial, sans-serif' fsize '15' rounded dashed
#set output 'perceptionDeviation.svg'

set terminal png size 1500,900 enhanced font 'Helvetica, Arial, sans-serif' 
set output 'font.png'

# color definitions
set style line 1 lc rgb '#FF0000' pt 1 ps 1.5 lt 2 lw 3 
set style line 2 lc rgb '#98AD1F' pt 2 ps 1.5 lt 3 lw 3 
set style line 3 lc rgb '#336633' pt 8 ps 1.5 lt 4 lw 3 
set style line 4 lc rgb '#664D33' pt 6 ps 1.5 lt 5 lw 3 
set style line 5 lc rgb '#663366' pt 7 ps 1.5 lt 1 lw 3 
set style line 12 lc rgb '#808080' lt 5 lw 0.5 
set style line 11 lc rgb '#808080' lt 1

set style line 14 lc rgb '#FF0000' pt 6 ps 1.5 lt 5 lw 3
set style line 15 lc rgb '#98AD1F' pt 6 ps 1.5 lt 5 lw 3
set style line 16 lc rgb '#2380C2' pt 6 ps 1.5 lt 5 lw 3

set style line 20 lc rgb "#bbbbbb" lw 1 lt 0

# titles and axis labels
set title "Pareto front (NSGAII) awareness overtime vs word-of-mouth volume"

set xlabel "Awareness" # x-axis label
set ylabel "Volume" # y-axis label

# remove border on top and right and set color to gray
set border 3 back ls 11
set tics nomirror

# define grid
#set grid back ls 12

#set grid ytics lc rgb "#bbbbbb" lw 1 lt 0
#set grid xtics lc rgb "#bbbbbb" lw 1 lt 0

#set grid xtics ytics mytics mytics ls 20

#set mxtics 4
#set mytics 4

# move legend to the correct place: available options are, left, right, top, bottom, outside, and below
#set key bottom right
set key outside right
#set key spacing 2
#set key box
#set key width 3

# ranges for the plot
# only to show TRA
#set xrange [0:72]
set yrange [17:30]

# for using CSV and separator
#set datafile separator ","


# draw the plot

set style fill transparent solid 0.2 noborder

#plot for [i=1:1000] 'data'.i.'.txt' using 1:2 title 'Flow '.i


#plot 'SPEA2a_front.tsv' title 'SPEA2a' with points ls 1, \
#	 'SPEA2b_front.tsv' title 'SPEA2b' with points ls 2, \
#	 'SPEA2c_front.tsv' title 'SPEA2c' with points ls 3,\
#	 'SPEA2d_front.tsv' title 'SPEA2d' with points ls 4


#plot for [i=0:1] 'SPEA2a/SimpleABMProblem/FUN'.i.'.tsv' title 'SPEA2a '.i with points ls i+1, \
#	 for [i=0:2] 'SPEA2b/SimpleABMProblem/FUN'.i.'.tsv' title 'SPEA2b '.i with points ls i+3, \
#	 for [i=0:2] 'SPEA2c/SimpleABMProblem/FUN'.i.'.tsv' title 'SPEA2c '.i with points ls i+6,\
#	 for [i=0:1] 'SPEA2d/SimpleABMProblem/FUN'.i.'.tsv' title 'SPEA2d '.i with points ls i+9,

plot for [i=0:29] 'FUN'.i.'.tsv' title 'it '.i with points ls i+1

#plot "front_input_aw_sales_1seg_1000.json_configNSGA2.ecj_1509732980653_it0.stats" title 'it 1' with points ls 1, \
#	"front_input_aw_sales_1seg_1000.json_configNSGA2.ecj_1509732980653_it1.stats" title 'it 2' with points ls 2, \
#	"front_input_aw_sales_1seg_1000.json_configNSGA2.ecj_1509732980653_it2.stats" title 'it 3' with points ls 3, \
#	"front_input_aw_sales_1seg_1000.json_configNSGA2.ecj_1509732980653_it3.stats" title 'it 4' with points ls 4, \
#	"front_input_aw_sales_1seg_1000.json_configNSGA2.ecj_1509732980653_it4.stats" title 'it 5' with points ls 5, \
#	"front_input_aw_sales_1seg_1000.json_configNSGA2.ecj_1509732980653_it5.stats" title 'it 6' with points ls 6, \
#	"front_input_aw_sales_1seg_1000.json_configNSGA2.ecj_1509732980653_it6.stats" title 'it 7' with points ls 7, \
#	"front_input_aw_sales_1seg_1000.json_configNSGA2.ecj_1509732980653_it7.stats" title 'it 8' with points ls 8, \
#	"front_input_aw_sales_1seg_1000.json_configNSGA2.ecj_1509732980653_it8.stats" title 'it 9' with points ls 9, \
#	"front_input_aw_sales_1seg_1000.json_configNSGA2.ecj_1509732980653_it9.stats" title 'it 10' with points ls 10, \
#	"front_input_aw_sales_1seg_1000.json_configNSGA2.ecj_1509732980653_it10.stats" title 'it 11' with points ls 11, \
#	"front_input_aw_sales_1seg_1000.json_configNSGA2.ecj_1509732980653_it11.stats" title 'it 12' with points ls 12, \
#	"front_input_aw_sales_1seg_1000.json_configNSGA2.ecj_1509732980653_it12.stats" title 'it 13' with points ls 13, \
#	"front_input_aw_sales_1seg_1000.json_configNSGA2.ecj_1509732980653_it13.stats" title 'it 14' with points ls 14, \
#	"front_input_aw_sales_1seg_1000.json_configNSGA2.ecj_1509732980653_it14.stats" title 'it 15' with points ls 15
#plot "pressUpperLower.csv" using 1:3 title 'ABS' with lines ls 2

#plot "pressUpperLower.csv" using 1:2 title 'PSOE' with lines ls 1, \
#	1:3 title 'ABS' with lines ls 2, \
#	1:4 title 'PP' with lines ls 3, \

#plot "pressUpperLower.csv"
