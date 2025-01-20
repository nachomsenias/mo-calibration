#Read from csv

#moeadspea2 = read.csv("0TP/WOMM/data/p_MOEADvsSPEA2.csv", dec=".", header=TRUE)
#moead <- as.numeric(moeadspea2)

#spea2moead = read.csv("0TP/WOMM/data/p_SPEA2vsMOEAD.csv", dec=".", header=TRUE)
#spea2 <- as.numeric(spea2moead)

#stest = wilcox.test(spea2,alternative="greater", conf.level = 0.95)
#mtest = wilcox.test(moead,alternative="greater", conf.level = 0.95)

#file.create("0TP/WOMM/data/p_MOEADvsSPEA2.out")
#fileConn <- file("0TP/WOMM/data/p_MOEADvsSPEA2.out")
#txt <- c(stest, mtest)
#writeLines(as.character(txt), "0TP/WOMM/data/p_MOEADvsSPEA2.out")
#close(fileConn)
#file.show("0TP/WOMM/data/p_MOEADvsSPEA2.out")


#algs<-c("SPEA2","MOEAD","NSGAII","PESA2","mIBEA","IBEA","SMSEMOA")
#algs<-c("SPEA2", "SPEA2-BLX", "NSGAII", "NSGAII-BLX", "MOEAD", "MOMBI2", "PESA2", "mIBEA", "IBEA", "SMSEMOA", "GWASFGA")
algs<-c("MOEAD", "SPEA2-BLX", "SMSEMOA", "IBEA", "NSGAII-BLX","GWASFGA", "MOMBI2")

for (tp in c(0,5,7,10,12,15,17,20,22,25,30,35,40,45,50)){

#	for (algo1 in c("SPEA2","NSGAII","MOEAD","PESA2","mIBEA","IBEA","SMSEMOA")){
#		for (algo2 in c("SPEA2","NSGAII","MOEAD","PESA2","mIBEA","IBEA","SMSEMOA")){
	for (i in 1:length(algs)){
		for (j in i:length(algs)){
			algo1 = algs[i]
			algo2 = algs[j]
			if (algo1 != algo2) {
				print(paste(algo1,"vs",algo2, sep=" "))

				csvfile = paste(tp,"TP/WOMM/data/p_",algo1,"vs",algo2,".csv", sep="")
				csvAlgo1 = read.csv(csvfile, dec=".", header=TRUE)
				valuesAlgo1 <- as.numeric(csvAlgo1)

				csvfile = paste(tp,"TP/WOMM/data/p_",algo2,"vs",algo1,".csv", sep="")
				csvAlgo2 = read.csv(csvfile, dec=".", header=TRUE)
				valuesAlgo2 <- as.numeric(csvAlgo2)

				test1 = wilcox.test(valuesAlgo1,valuesAlgo2,alternative="greater", conf.level = 0.95)
				test2 = wilcox.test(valuesAlgo2,valuesAlgo1,alternative="greater", conf.level = 0.95)

				txt <- c(test1, test2)
				writeLines(as.character(txt), paste(tp,"TP/WOMM/data/sum_",algo1,"vs",algo2,".out", sep=""))
			}
		}
	}	

	#SPEA2 vs MOEAD
	#csvfile = paste(tp,"TP/WOMM/data/p_MOEADvsSPEA2.csv", sep="")
	#moeadspea2 = read.csv(csvfile, dec=".", header=TRUE)
	#moead <- as.numeric(moeadspea2)

	#csvfile = paste(tp,"TP/WOMM/data/p_SPEA2vsMOEAD.csv", sep="")
	#spea2moead = read.csv(csvfile, dec=".", header=TRUE)
	#spea2 <- as.numeric(spea2moead)

	#stest = wilcox.test(spea2,moead,alternative="greater", conf.level = 0.95)
	#mtest = wilcox.test(moead,spea2,alternative="greater", conf.level = 0.95)

	#txt <- c(stest, mtest)
	#writeLines(as.character(txt), paste(tp,"TP/WOMM/data/sum_SPEA2vsMOEAD.out", sep=""))


	#SPEA2 vs NSGAII	
	#csvfile = paste(tp,"TP/WOMM/data/p_SPEA2vsNSGAII.csv", sep="")
	#spea2nsgaii = read.csv(csvfile, dec=".", header=TRUE)
	#spea2 <- as.numeric(spea2nsgaii)

	#csvfile = paste(tp,"TP/WOMM/data/p_NSGAIIvsSPEA2.csv", sep="")
	#nsgaiispea2 = read.csv(csvfile, dec=".", header=TRUE)
	#nsgaii <- as.numeric(nsgaiispea2)
	

	#stest = wilcox.test(spea2,nsgaii,alternative="greater", conf.level = 0.95)
	#ntest = wilcox.test(nsgaii,spea2,alternative="greater", conf.level = 0.95)

	#txt <- c(stest, ntest)
	#writeLines(as.character(txt), paste(tp,"TP/WOMM/data/sum_SPEA2vsNSGAII.out", sep=""))


	#NSGAII vs MOEAD
	#csvfile = paste(tp,"TP/WOMM/data/p_NSGAIIvsMOEAD.csv", sep="")
	#nsgaiimoead = read.csv(csvfile, dec=".", header=TRUE)
	#nsgaii <- as.numeric(nsgaiimoead)

	#csvfile = paste(tp,"TP/WOMM/data/p_MOEADvsNSGAII.csv", sep="")
	#moeadnsgaii = read.csv(csvfile, dec=".", header=TRUE)
	#moead <- as.numeric(moeadnsgaii)

	#ntest = wilcox.test(nsgaii,moead,alternative="greater", conf.level = 0.95)
	#mtest = wilcox.test(moead,nsgaii,alternative="greater", conf.level = 0.95)

	#txt <- c(ntest, mtest)
	#writeLines(as.character(txt), paste(tp,"TP/WOMM/data/sum_NSGAIIvsMOEAD.out", sep=""))

	#close(fileConn)
	#file.show("0TP/WOMM/data/p_MOEADvsSPEA2.out")
}