#=====================================
# README
# 
# This file uses the usefull_functions.R file
# for the function 'getDataFromFile'
#
# Then it uses state data from the agents
#
# To build go to functions: Build First !!!!TODO WRITE THIS
#=====================================

setwd("D://Work//WorkspaceR")
source("usefull_functions.R")

#Data formatted as: "tick","Id","LocationX","LocationY","Money"
name = "ModelData";

batchRunDataRaw  <- getDataFromFile(paste(name, "_BatchRunOutput", sep = ""));
batchRunParametersRaw <- getDataFromFile(paste(name, "_BatchRunOutputParams", sep = ""));

#FROM HERE

batchRunData <- batchRunDataRaw;
batchRunParameters <- batchRunParametersRaw;

batchRunData$runP <- batchRunParameters$run;
batchRunData$interType <- substr(batchRunParameters$pInterventionType,0,1);
batchRunData$interNLPerc <- batchRunParameters$pInterventionWholesalerNLPercentage;
#pInterventionRisk

interPerc = c();
for (row in batchRunParameters$pInterventionWholesalerNLPercentage) {
	if (row < 10) {
		interPerc = c(interPerc, paste("00", row, sep = ""));
	} else if (row < 100) {
		interPerc = c(interPerc, paste("0", row, sep = ""));
	} else {
		interPerc = c(interPerc, toString(row));
	}
}

batchRunData$interNLPerc <- interPerc;
batchRunData$importNL <- batchRunData$Quality.NL.Low + batchRunData$Quality.NL.High;
batchRunData$importES <- batchRunData$Quality.ES.Low + batchRunData$Quality.ES.High;

batchRunData <- batchRunData[batchRunData$interType=="s", ];

batchRunData$interParam <- factor(paste(batchRunData$interType, batchRunData$interNLPerc));
batchRunData$asNumeric <- as.numeric(batchRunData$interParam);
batchRunData <- batchRunData[ , c(1,21,20,17,6,18,19,11,12,13,14)];
batchRunData <- batchRunData[order(batchRunData$asNumeric), ];
print(levels(batchRunData$interParam));
#batchRunData <- batchRunData[ , c(1,6,7,8,9,10,11)];

pdf(file=paste("Consumption_and_import_single.pdf", sep=""), width=16, height=5);
par(mfrow=c(1,3))
boxplot(Consumed~interNLPerc, data=batchRunData, main="Consumption", xaxt="n", ylim = c(0, 11000), cex.axis=1.5, cex.main=1.5);
axis(1, at=c(1:10), labels=levels(factor(batchRunData$interNLPerc)), cex.axis=1.5)
fitConsumption <- aov(Consumed ~ factor(interNLPerc), data=batchRunData);
TukeyHSD(fitConsumption)

boxplot(importNL~interNLPerc, data=batchRunData, main="Import NL", xaxt="n", ylim = c(0, 11000), cex.axis=1.5, cex.main=1.5);
axis(1, at=c(1:10), labels=levels(factor(batchRunData$interNLPerc)), cex.axis=1.5)
fitImportNL <- aov(importNL ~ factor(interNLPerc), data=batchRunData);
TukeyHSD(fitImportNL)

boxplot(importES~interNLPerc, data=batchRunData, main="Import ES", xaxt="n", ylim = c(0, 11000), cex.axis=1.5, cex.main=1.5);
axis(1, at=c(1:10), labels=levels(factor(batchRunData$interNLPerc)), cex.axis=1.5)
fitImportES <- aov(importNL ~ factor(interNLPerc), data=batchRunData);
TukeyHSD(fitImportES)
dev.off();

agentData     <- getDataFromFile(paste(name, "_AgentState", sep = ""));    #Load 'AgentState.YYYY.MMM.DD.HH_MM_SS.txt' here
relationsData <- getDataFromFile(paste(name, "_RelationsData", sep = "")); #Load 'RelationsData.YYYY.MMM.DD.HH_MM_SS.txt' here
orderData 	  <- getDataFromFile(paste(name, "_OrderState", sep = ""));	   #Load 'OrderState.YYYY.MMM.DD.HH_MM_SS.txt' here
shipmentData  <- getDataFromFile(paste(name, "_ShipmentState", sep = "")); #Load 'ShipmentState.YYYY.MMM.DD.HH_MM_SS.txt' here
moneyData     <- getDataFromFile(paste(name, "_AvgWealth", sep = ""));
#plotData(moneyData[, c(1:5)], "Avg wealth", 1000, 50000, "Money average", FALSE, "name");
#function(pData, pLegendNames, pHoriz, pCex, pColors, pTitle, pXLim, pYLim, pYLab, pSavePlot, pSaveName)
plotDataExt(moneyData[, c(1:5)], c("Producers","Internationals","Wholesalers","Retailers"),
			TRUE, 1, c(), "Avg wealth", 1000, 50000, "Money average", FALSE, "name");

#timeTicks <- c(1,3,5,7,9,11,13,15,20,25,30,40,60,80,100); #The time ticks you want to make plots from
timeTicks <- c(1000) #!!!!!!!!!!!! FIX THIS SO IT CAN HAVE MULTIPLE TICKS BY APPENDING THE THINGS BELOW
for (i in timeTicks) {
	agentDataCropped     <- agentData[agentData$tick==i, ];
	relationsDataCropped <- relationsData[relationsData$tick==i, ];
	orderDataCropped     <- orderData[orderData$tick==i, ];
	shipmentDataCropped  <- shipmentData[shipmentData$tick==i, ];
}

orderDataCropped     <- convertOrders(orderDataCropped);
shipmentDataCropped  <- convertShipments(shipmentDataCropped);
relationsDataCropped <- addCoordinatesToRelations(agentDataCropped, relationsDataCropped);

for (i in timeTicks) {
	print(paste("Plotting graph for tick", i));
	pdf(file=paste("Legal Supply Chain - Tick ", i ,".pdf", sep=""), width=14, height=10);
	tTitle <- paste("Legal Supply Chain - Tick ", i, sep = "");
	plotSupplyChain(agentDataCropped[agentDataCropped$tick==i, ],
					relationsDataCropped[relationsDataCropped$tick==i, ],
					orderDataCropped[orderDataCropped$tick==i, ],
					shipmentDataCropped[shipmentDataCropped$tick==i, ],
					tTitle);
	dev.off();
}

for (i in timeTicks) {
	print(paste("Plotting graph for tick", i));
	pdf(file=paste("Legal Supply Chain Active - Tick ", i ,".pdf", sep=""), width=14, height=10);
	tTitle <- paste("Legal Supply Chain Active - Tick ", i, sep = "");
	plotSupplyChain(agentDataCropped[agentDataCropped$tick==i & agentDataCropped$Connected=="true", ],
					relationsDataCropped[relationsDataCropped$tick==i & relationsDataCropped$Connected=="true", ],
					orderDataCropped[orderDataCropped$tick==i & orderDataCropped$Connected=="true", ],
					shipmentDataCropped[shipmentDataCropped$tick==i & shipmentDataCropped$Connected=="true", ],
					tTitle);
	dev.off();
}

#PDF EXPORT

#=====================================
# Functions: Build first
#=====================================
plotSupplyChain <- function(pAgentData, pRelationsData, pOrderData, pShipmentData, pTitle) {
	
	plot(pAgentData$LocationX, pAgentData$LocationY, main = pTitle, xlab = "", ylab = "", xaxt="n", yaxt="n", xlim=c(1,59), ylim=c(0,50))

	rect(1.5,3.5,6.5,46.5,col="gray88",border = NA);
	text(4,2,"Source\ncountries", cex = 0.8);
	rect(15,5.5,19,44.5,col="gray88",border = NA);
	text(17,2,"International\ntransport", cex = 0.8);
	rect(27,0.5,55,7.5,col="gray88",border = NA);  #Spain
	text(57,4,"Spain", cex = 0.8);
	rect(39,8.5,55,15.5,col="gray88",border = NA); #France
	text(57,12,"France", cex = 0.8);
	rect(39,16.5,55,23.5,col="gray88",border = NA); #Italy
	text(57,20,"Italy", cex = 0.8);
	rect(39,24.5,55,31.5,col="gray88",border = NA); #United Kingdom
	text(57,28,"United\nKingdom", cex = 0.8);
	rect(39,32.5,55,39.5,col="gray88",border = NA); #Germany
	text(57,36,"Germany", cex = 0.8);
	rect(27,40.5,55,47.5,col="gray88",border = NA); #The Netherlands
	text(57,44,"The\nNetherlands", cex = 0.8);
	
	points(pAgentData$LocationX, pAgentData$LocationY, col="black", pch=16);
	
	segments(x0=pRelationsData$x, y0=pRelationsData$y, x1=pRelationsData$otherX, y1=pRelationsData$otherY, col=pRelationsData$color);
	
	points(pOrderData$LocationX, pOrderData$LocationY, cex=pOrderData$Size, col=pOrderData$Color, pch=16);
	points(pShipmentData$LocationX, pShipmentData$LocationY, cex=pShipmentData$Size, col=pShipmentData$Color, pch=16);
	
	text(pAgentData$LocationX + 0.25, pAgentData$LocationY - 0.25, labels = pAgentData$Id, cex = 0.7, adj = c(0,0.5));
	
	axis(1, at=c(5,17,29,41,53),labels=c("P","I","W","R","C"), col.axis="black", las=0);
	
	#draw points with color
	#rm(point_1,point_2)
	#points(graph[p_solution==FALSE,2], graph[p_solution==FALSE,3], col=2, pch=16)
	#points(graph[p_solution==TRUE,2], graph[p_solution==TRUE,3], col=4, pch=16)
	#text(x=0, y=1, label=paste("f : ",count, sep =""), col = "magenta")
}

convertOrders <- function(pOrderData) {
	
	print(paste("Processing orders, adding color ", nrow(pOrderData), "rows ....."));
	color <- c();
	for (i in 1:nrow(pOrderData)) {
		if (pOrderData$LargestQuality[i] == 40) {
			color <- c(color, "blue4");
		} else {
			color <- c(color, "blue");
		}
	}
	pOrderData$Size  <- pOrderData$Size/7;
	pOrderData$Color <- color;
	return(pOrderData);
}

convertShipments <- function(pShipmentData) {
	print(paste("Processing orders, adding color ", nrow(pShipmentData), "rows ....."));
	color <- c();
	for (i in 1:nrow(pShipmentData)) {
		if (pShipmentData$LargestQuality[i] == 40) {
			color <- c(color, "yellow4");
		} else {
			color <- c(color, "yellow2");
		}
	}
	pShipmentData$Size  <- pShipmentData$Size/7;
	pShipmentData$Color <- color;
	return(pShipmentData);
}

addCoordinatesToRelations <- function(pAgentData, pRelationsData) {
	
	relationsDataNew <- pRelationsData;
	x <- c();
	y <- c();
	otherX <- c();
	otherY <- c();
	color <- c();
	print(paste("Processing coordinates to relations ", nrow(pRelationsData), "rows ....."));
	for (i in 1:nrow(pRelationsData)) {
		#print(paste("  row [", i, "/", nrow(pRelationsData), "]"));
		id <- pRelationsData$Id[i]
		x <- c(x, pAgentData[pAgentData$Id==id, ]$LocationX[1]) #Getting the first location of the vector of all locations for the given agent id
		y <- c(y, pAgentData[pAgentData$Id==id, ]$LocationY[1]) 
		otherId <- pRelationsData$OtherId[i]
		otherX <- c(otherX, pAgentData[pAgentData$Id==otherId, ]$LocationX[1])
		otherY <- c(otherY, pAgentData[pAgentData$Id==otherId, ]$LocationY[1])
		color <- c(color, rgb(1 - pRelationsData$Trust[i],pRelationsData$Trust[i],0))
	}
	relationsDataNew$x <- x
	relationsDataNew$y <- y
	relationsDataNew$otherX <- otherX
	relationsDataNew$otherY <- otherY
	relationsDataNew$color <- color
	print("Done processing!");
	return(relationsDataNew)
}