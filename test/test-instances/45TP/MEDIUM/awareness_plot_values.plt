set terminal pngcairo size 1200,700 enhanced font 'Helvetica, 8' 
set output 'awareness_plotvalues.png'

set style line 1 lc rgb '#000000' pt 2 ps 1.5 lt 2 lw 1 
set style line 2 lc rgb '#df541e' pt 2 ps 1.5 lt 3 lw 1 
set style line 3 lc rgb '#8bc900' pt 2 ps 1.5 lt 4 lw 1 
set style line 4 lc rgb '#e81f3f' pt 6 ps 1.5 lt 5 lw 1 
set style line 5 lc rgb '#f9ae34' pt 6 ps 1.5 lt 1 lw 1 
set style line 6 lc rgb '#11aac4' pt 6 ps 1.5 lt 5 lw 1 
set style line 7 lc rgb '#80ba27' pt 6 ps 1.5 lt 5 lw 1 
set style line 8 lc rgb '#937760' pt 6 ps 1.5 lt 5 lw 1 

set style line 9 lc rgb '#fc8d59' pt 2 ps 1.5 lt 4 lw 1
set style line 10 lc rgb '#e34a33' pt 6 ps 1.5 lt 4 lw 1
set style line 11 lc rgb '#b30000' pt 4 ps 1.5 lt 4 lw 1

#set style fill pattern 2 
set style fill transparent solid 0.15 noborder
set style data lines
#set title "Awareness evolution by Brand" 
unset title
#set xrange [ 10.0000 : * ] noreverse nowriteback
#set yrange [ 0.0 : 1.0 ] noreverse nowriteback

set yrange [0:100]

#DEBUG_TERM_HTIC = 119
#DEBUG_TERM_VTIC = 119
## Last datafile plotted: "silver.dat"

set key font ",16"
set key bottom right
#set key off
unset xtics
#unset ytics
unset xrange

# for using CSV and separator
set datafile separator ","

#set arrow from 38, graph 0 to 38, graph 1 nohead

set multiplot
#plot 'bestAW/awareness_area_b1.csv' u 1:2:3 w filledcurves notitle ls 9
#plot 'bestWOM/awareness_area_b1.csv' u 1:2:3 w filledcurves notitle ls 11 
#plot 'TradeOff/awareness_area_b1.csv' u 1:2:3 w filledcurves notitle ls 10
#unset multiplot
plot 'awareness_area_b1.csv' using 1:4 title "B1" with linespoints ls 11 lw 2 lc 1, \
	'awareness_area_b2.csv' using 1:4 title "B2" with linespoints ls 11 lw 2 lc 2, \
	'awareness_area_b3.csv' using 1:4 title "B3" with linespoints ls 11 lw 2 lc 3, \
	'awareness_area_b4.csv' using 1:4 title "B4" with linespoints ls 11 lw 2 lc 4, \
	'awareness_area_b5.csv' using 1:4 title "B5" with linespoints ls 11 lw 2 lc 5, \
	'awareness_area_b6.csv' using 1:4 title "B6" with linespoints ls 11 lw 2 lc 6, \
	'awareness_area_b7.csv' using 1:4 title "B7" with linespoints ls 11 lw 2 lc 7, \
	'awareness_area_b8.csv' using 1:4 title "B8" with linespoints ls 11 lw 2 lc 8
	