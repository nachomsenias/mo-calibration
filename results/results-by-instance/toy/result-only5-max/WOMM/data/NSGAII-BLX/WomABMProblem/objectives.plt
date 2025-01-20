# GNUPLOT WITH ERROR BARS
# FROM http://stackoverflow.com/questions/25512006/gnuplot-smooth-confidence-interval-lines-as-opposed-to-error-bars

# svg
#set terminal svg size 900,700 fname 'Helvetica, Arial, sans-serif' fsize '15' rounded dashed
#set output 'perceptionDeviation.svg'

set terminal png size 1500,900 enhanced font 'Helvetica, Arial, sans-serif' 
set output 'objectives.png'

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

set style line 20 lc rgb "#000000" pt 7 dashtype 7 ps 1.5 lt 1 lw 1

# titles and axis labels
# set title "Pareto frontier approximation: awareness overtime vs word-of-mouth volume"

set xlabel "Awareness error (%)" font ",24" # x-axis label
set ylabel "WOM volume error (%)" font ",20" # y-axis label

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
#set key top right
#set key spacing 2
#set key box
#set key width 3
set key off

# ranges for the plot
# only to show TRA
#set xrange [35:270]
#set yrange [18:28.5]

# for using CSV and separator
#set datafile separator ","


# draw the plot

set style fill transparent solid 0.2 noborder

plot 'Objectives20.tsv' title 'Pareto frontier' with fsteps ls 20

