package supplyChainModel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import repast.simphony.context.Context;
import supplyChainModel.agents.CountryAgent;
import supplyChainModel.common.Constants;
import supplyChainModel.common.Logger;
import supplyChainModel.enums.SCType;

public class ContextDataLoader {

	/**
	 * Function to read the csv data file
	 * @param context
	 * @param filePath
	 * @param fileName
	 */
	public void readFullFile(final Context<Object> context, String filePath, String fileName) {
		
		Logger.logInfo("ContextDataLoader.readFullFile() from " + filePath + "/" + fileName + ".txt");
		List<String> dataAll = readFile(filePath + "/" + fileName + ".txt");
		List<String> dataC = new ArrayList<String>();
		
		for (String datum : dataAll) {
			if (!datum.startsWith("%")) {
				dataC.add(datum);
			}
		}
		
		Logger.logInfo("ContextDataLoader.readFullFile() generate country");
		generateCountry(context, dataC);
	}

	/**
	 * Function to generate a supply chain based on the data
	 * @param context
	 * @param dataSC
	 */
	public void generateCountry(final Context<Object> context, List<String> dataC) {
		
		int consumers = Constants.COUNTRY_CONSUMERS_MAX;
		// Count countries
		for (String nodeString : dataC) {
			
			List<String> vars = Arrays.asList(nodeString.split(","));			
			if (Integer.parseInt(vars.get(1)) == 3)
				consumers ++;
		}
		
		int stepSize = (Constants.GRID_HEIGHT - 2) / consumers;
		Logger.logInfo("Country distance: (" + Constants.GRID_HEIGHT + " - 2) / Consumers = " + stepSize);
		int transitCountry = 0;
		int consumerCountry = 1;
		
		double qualMin = 41.44; //TODO should be read from file
		double qualDif = 57.92 - 41.44;
		
		// Create countries
		for (String nodeString : dataC) {
			
			List<String> vars = Arrays.asList(nodeString.split(","));
			String name = vars.get(0);
			int layer = Integer.parseInt(vars.get(1));
			int countryX = Constants.VSL_COUNTRY_X + layer * Constants.VSL_COUNTRY_WIDTH;
			//double retailPrice = Double.parseDouble(vars.get(2));
			double avgQuality = Double.parseDouble(vars.get(3));
			double countryQuality = qualDif - (avgQuality - qualMin);
			
			HashMap<SCType, Double> interceptProbability = new HashMap<SCType, Double>();
			interceptProbability.put(SCType.INTERNATIONAL, Double.parseDouble(vars.get(4)));
			interceptProbability.put(SCType.WHOLESALER, Double.parseDouble(vars.get(5)));
			interceptProbability.put(SCType.RETAIL, Double.parseDouble(vars.get(6)));
			interceptProbability.put(SCType.CONSUMER, Double.parseDouble(vars.get(7)));
			
			ArrayList<SCType> scTypes = new ArrayList<SCType>(); 
			switch (layer) {
			case 0: // Producer country
				scTypes.add(SCType.PRODUCER);
				new CountryAgent(context, name, scTypes, countryX, Constants.GRID_HEIGHT - 4, Constants.GRID_HEIGHT - 8, countryQuality, interceptProbability); //TODO
				break;
			case 1: // International country
				scTypes.add(SCType.INTERNATIONAL);
				new CountryAgent(context, name, scTypes, countryX, Constants.GRID_HEIGHT - 6, Constants.GRID_HEIGHT - 12, countryQuality, interceptProbability); //TODO
				break;
			case 2: // Transit country
				scTypes.add(SCType.WHOLESALER);
				scTypes.add(SCType.RETAIL);
				scTypes.add(SCType.CONSUMER);
				if (transitCountry == 0)
					new CountryAgent(context, name, scTypes, countryX, Constants.GRID_HEIGHT - 3, stepSize - 1, countryQuality, interceptProbability);
				else
					new CountryAgent(context, name, scTypes, countryX, (Constants.GRID_HEIGHT - 3) - stepSize * (consumers - 1), stepSize - 1, countryQuality, interceptProbability);
				transitCountry ++;
				break;
			case 3: // Consumer country
				scTypes.add(SCType.RETAIL);
				scTypes.add(SCType.CONSUMER);
				new CountryAgent(context, name, scTypes, countryX, (Constants.GRID_HEIGHT - 3) - stepSize * consumerCountry, stepSize - 1, countryQuality, interceptProbability);
				consumerCountry ++;
				break;
			}
		}
	}
	
	/**
	 * Function to generate a supply chain based on the data
	 * @param context
	 * @param dataSC
	 */
	public void generateSupplyChain(final Context<Object> context, List<String> dataSC) {
		
		for (String nodeString : dataSC) {
			
			List<String> vars = Arrays.asList(nodeString.split(","));
			int layer = Integer.parseInt(vars.get(0));

			switch (layer) {
			case 0:
				//new AgentProducer(context, SCType.PRODUCER, Country.SOUTH_AMERICA);
				break;
			}
		}
	}
	
	public List<String> readFile(String filePathAndName) {
		BufferedReader reader;
		List<String> data = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader(filePathAndName));
			String line = reader.readLine();
			while (line != null) {
				data.add(line);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
}