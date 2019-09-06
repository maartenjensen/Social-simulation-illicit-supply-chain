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

setwd("D://Work//WorkspaceR//SSC2019_DataFinal")
source("usefull_functions.R")

#Data formatted as: "tick","Id","LocationX","LocationY","Money"
name = "SingleLegal";
#name = "Illegal1000";

agentData     <- getDataFromFile(paste(name, "_AgentState", sep = ""));    #Load 'AgentState.YYYY.MMM.DD.HH_MM_SS.txt' here
relationsData <- getDataFromFile(paste(name, "_RelationsData", sep = "")); #Load 'RelationsData.YYYY.MMM.DD.HH_MM_SS.txt' here
orderData 	  <- getDataFromFile(paste(name, "_OrderState", sep = ""));	   #Load 'OrderState.YYYY.MMM.DD.HH_MM_SS.txt' here
shipmentData  <- getDataFromFile(paste(name, "_ShipmentState", sep = "")); #Load 'ShipmentState.YYYY.MMM.DD.HH_MM_SS.txt' here
moneyData     <- getDataFromFile(paste(name, "_AvgWealth", sep = ""));
#plotData(moneyData[, c(1:5)], "Avg wealth", 1000, 50000, "Money average", FALSE, "name");
#function(pData, pLegendNames, pHoriz, pCex, pColors, pTitle, pXLim, pYLim, pYLab, pSavePlot, pSaveName)

agentData <- agentData[agentData$Id!=0, ]
relationsData <- relationsData[relationsData$Id!=0 & relationsData$OtherId!= 0, ]
orderData <- orderData[orderData$IdClient!=0 & orderData$IdSupplier!=0, ]
shipmentData <- shipmentData[shipmentData$IdClient!=0 & shipmentData$IdSupplier!=0, ]

#function(pData, pLegendNames, pHoriz, pCex, pColors, pTitle, pXLim, pYLim, pYLab, pSavePlot, pSaveName)
plotDataExt(moneyData[, c(1:5)], c("Producers","Internationals","Wholesalers","Retailers"),
			TRUE, 1, c("cyan","dodgerblue","blue4","black"), "Avg wealth", 1000, 50000, "Money average", FALSE, "name");

#timeTicks <- c(1,3,5,7,9,11,13,15,20,25,30,40,60,80,100); #The time ticks you want to make plots from
timeTicks <- c(11, 12, 15, 16)#seq(8,25,1)) #!!!!!!!!!!!! FIX THIS SO IT CAN HAVE MULTIPLE TICKS BY APPENDING THE THINGS BELOW
for (i in timeTicks) {
	
	print(paste("Starting graph plot for tick ", i));
	agentDataCropped     <- agentData[agentData$tick==i, ];
	relationsDataCropped <- relationsData[relationsData$tick==i, ];
	orderDataCropped     <- orderData[orderData$tick==i, ];
	shipmentDataCropped  <- shipmentData[shipmentData$tick==i, ];
	
	orderDataCropped     <- convertOrders(orderDataCropped);
	shipmentDataCropped  <- convertShipments(shipmentDataCropped);
	relationsDataCropped <- addCoordinatesToRelations(agentDataCropped, relationsDataCropped);
	
	print(paste("Plotting graph for tick", i));
	pdf(file=paste(name, " Supply Chain - Tick ", i ,".pdf", sep=""), width=7, height=5);
	tTitle <- paste(name, " Supply Chain - Tick ", i, sep = "");
	plotSupplyChain(agentDataCropped[agentDataCropped$tick==i, ],
					relationsDataCropped[relationsDataCropped$tick==i, ],
					orderDataCropped[orderDataCropped$tick==i, ],
					shipmentDataCropped[shipmentDataCropped$tick==i, ],
					tTitle);
	dev.off();
}


#=====================================
# Functions: Build first
#=====================================
plotSupplyChain <- function(pAgentData, pRelationsData, pOrderData, pShipmentData, pTitle) {
	
	plot(pAgentData$LocationX, pAgentData$LocationY, main = pTitle, xlab = "", ylab = "", xaxt="n", yaxt="n", xlim=c(2.5,30), ylim=c(1,18))
	
	grid(25, 18);
	
	rect(1.5,3.5,6.5,46.5,col="gray88",border = NA);
	text(4,2,"Source\ncountries", cex = 0.8);
	rect(15,5.5,19,44.5,col="gray88",border = NA);
	text(17,2,"International\ntransport", cex = 0.8);
	rect(27,0.5,55,7.5,col="gray88",border = NA);  #Spain
	text(57,4,"Spain", cex = 0.8);
	text(29,8.5,"Spain", cex = 0.8);
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
	
	segments(x0=pRelationsData$x, y0=pRelationsData$y, x1=pRelationsData$otherX, y1=pRelationsData$otherY, col=pRelationsData$color, lwd=0.1);
	
	points(pOrderData$LocationX, pOrderData$LocationY, cex=0.75 + pOrderData$Size * 0.5, col=pOrderData$Color, pch=16);
	points(pShipmentData$LocationX, pShipmentData$LocationY, cex=0.75 + pShipmentData$Size * 0.5, col=pShipmentData$Color, pch=16);
	
	points(pAgentData$LocationX, pAgentData$LocationY, col="black", pch=16);
	
	text(pAgentData$LocationX + 0.25, pAgentData$LocationY - 0.25, labels = pAgentData$Id, cex = 0.7, adj = c(0,0.5));
	
	axis(1, at=c(5,17,29,41,53),labels=c("P","I","W","R","C"), col.axis="black", las=0);
	
	box(lty = 'solid', col = 'gray')
	
	#draw points with color
	#rm(point_1,point_2)
	#points(graph[p_solution==FALSE,2], graph[p_solution==FALSE,3], col=2, pch=16)
	#points(graph[p_solution==TRUE,2], graph[p_solution==TRUE,3], col=4, pch=16)
	#text(x=0, y=1, label=paste("f : ",count, sep =""), col = "magenta")
}

convertOrders <- function(pOrderData) {
	
	print(paste("Processing orders, adding color ", nrow(pOrderData), "rows ....."));
	
	color <- c();
	if (nrow(pOrderData)>0) {
		for (i in 1:nrow(pOrderData)) {
			if (pOrderData$LargestQuality[i] == 40) {
				color <- c(color, "blue4");
			} else {
				color <- c(color, "dodgerblue");
			}
		}
	}
	pOrderData$Size  <- pOrderData$Size/7;
	pOrderData$Color <- color;
	return(pOrderData);
}

convertShipments <- function(pShipmentData) {
	print(paste("Processing orders, adding color ", nrow(pShipmentData), "rows ....."));
	if (nrow(pShipmentData)==0) { return(pShipmentData); }
	
	color <- c();
	if (nrow(pShipmentData)>0) {
		for (i in 1:nrow(pShipmentData)) {
			if (pShipmentData$LargestQuality[i] == 40) {
				color <- c(color, "yellow4");
			} else {
				color <- c(color, "yellow2");
			}
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



#for (i in timeTicks) {
#	print(paste("Plotting graph for tick", i));
#	pdf(file=paste(name, " Supply Chain Active - Tick ", i ,".pdf", sep=""), width=12, height=6);
#	tTitle <- paste(name, " Supply Chain Active - Tick ", i, sep = "");
#	plotSupplyChain(agentDataCropped[agentDataCropped$tick==i & agentDataCropped$Connected=="true", ],
#					relationsDataCropped[relationsDataCropped$tick==i & relationsDataCropped$Connected=="true", ],
#					orderDataCropped[orderDataCropped$tick==i & orderDataCropped$Connected=="true", ],
#					shipmentDataCropped[shipmentDataCropped$tick==i & shipmentDataCropped$Connected=="true", ],
#					tTitle);
#	dev.off();
#}