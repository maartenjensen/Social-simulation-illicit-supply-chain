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

/**
 * This class can load data to generate a simulation state with
 * appropriate classes.
 * @author Maarten Jensen
 *
 */
public class ContextDataLoader {

	/**
	 * Function to read a csv data file
	 * @param context
	 * @param filePath
	 * @param fileName
	 */
	public void readFullFile(final Context<Object> context, String filePath, String fileName, String fileName2) {
		
		Logger.logInfo("ContextDataLoader.readFullFile() file path and name: " + filePath + "/" + fileName);
		List<String> dataAll = readFile(filePath + "/" + fileName);
		List<String> dataC = new ArrayList<String>();
		
		for (String datum : dataAll) {
			if (!datum.startsWith("%")) {
				dataC.add(datum);
			}
		}
		
		Logger.logInfo("ContextDataLoader.readFullFile() read another file: " + filePath + "/" + fileName2);
		List<String> dataBorders = readFile(filePath + "/" + fileName2);
		HashMap<String, String> dataB = new HashMap<String, String>();
		for (String datum : dataBorders) {
			if (!datum.startsWith("%")) {
				String[] splitDatum = datum.split(",", 2);
				Logger.logInfo(splitDatum[0] + ":" + splitDatum[1]);
				dataB.put(splitDatum[0], splitDatum[1]);
			}
		}
		
		Logger.logInfo("ContextDataLoader.readFullFile() generate countries");
		generateCountries(context, dataC, dataB);
	}
	
	/**
	 * Function to generate the plain countries based on the data file.
	 * @param context
	 * @param dataSC
	 */
	public void generateCountries(final Context<Object> context, List<String> dataC, HashMap<String, String> dataB) {

		double qualMin = getCountryQualityMin(dataC);
		double qualDif = getCountryQualityMax(dataC) - qualMin;
		
		//double europeMapX = Constants.GRID_WIDTH - 150 * Constants.VSL_EUROPEAN_MAP_SCALE;
		//Logger.logInfo("Map of Europe starting at X: " + europeMapX);
		
		// Create countries
		for (String nodeString : dataC) {
			
			List<String> vars = Arrays.asList(nodeString.split(","));
			String name 	= vars.get(0);
			double x 		= Constants.VSL_EUROPEAN_MAP_X_ADD + Double.parseDouble(vars.get(1)) * Constants.VSL_EUROPEAN_MAP_SCALE;
			double y 		= Constants.GRID_HEIGHT - Double.parseDouble(vars.get(2)) * Constants.VSL_EUROPEAN_MAP_SCALE;
			double radius 	= Double.parseDouble(vars.get(3)) * Constants.VSL_EUROPEAN_MAP_SCALE;
			int layer 		= Integer.parseInt(vars.get(4));
			
			//double retailPrice = Double.parseDouble(vars.get(5));
			double avgQuality = Double.parseDouble(vars.get(6));
			double countryQuality = qualDif - (avgQuality - qualMin);
	
			ArrayList<SCType> scTypes = new ArrayList<SCType>();
			switch (layer) {
			case 0: // Producer country
				scTypes.add(SCType.PRODUCER);
				break;
			case 1: // International country
				scTypes.add(SCType.INTERNATIONAL);
				break;
			case 2: // Transit country
				scTypes.add(SCType.WHOLESALER);
				scTypes.add(SCType.RETAIL);
				scTypes.add(SCType.CONSUMER);
				break;
			case 3: // Retail country
				scTypes.add(SCType.RETAIL);
				scTypes.add(SCType.CONSUMER);
				break;
			case 4: // Consumer country
				scTypes.add(SCType.CONSUMER);
				break;
			}

			if (dataB.containsKey("Countries") && dataB.containsKey(name)) {
				HashMap<String, Integer> countryBorders = dataToHashMap(dataB.get("Countries"), dataB.get(name));
				new CountryAgent(context, name, scTypes, countryBorders, x, y, radius, countryQuality);
			}
			else
				Logger.logError("generateCountries: dataB does not contain the key:\"Countries\" or \"" + name + "\"");
			
		}
	}

	/**
	 * Return the minimum quality value of all countries.
	 * This variable is found at position 3 in the string.
	 * @param dataC
	 * @return
	 */
	public double getCountryQualityMin(List<String> dataC) {
		
		double minQuality = Double.MAX_VALUE;
		for (String nodeString : dataC) {
			
			List<String> vars = Arrays.asList(nodeString.split(","));
			double countryQuality = Double.parseDouble(vars.get(3));
			if (countryQuality < minQuality)
				minQuality = countryQuality;
		}
		return minQuality;
	}
	
	/**
	 * Return the maximum quality value of all countries.
	 * This variable is found at position 3 in the string.
	 * @param dataC
	 * @return
	 */
	public double getCountryQualityMax(List<String> dataC) {
		
		double maxQuality = Double.MIN_VALUE;
		for (String nodeString : dataC) {
			
			List<String> vars = Arrays.asList(nodeString.split(","));
			double countryQuality = Double.parseDouble(vars.get(3));
			if (countryQuality > maxQuality)
				maxQuality = countryQuality;
		}
		return maxQuality;
	}
	
	/**
	 * Convert separate headers and borders to combined HashMap
	 */
	public HashMap<String, Integer> dataToHashMap(String pKeys, String pValues) {
		
		HashMap<String, Integer> countryBorders = new HashMap<String, Integer>();
		
		List<String> keys   = Arrays.asList(pKeys.split(","));
		List<String> values = Arrays.asList(pValues.split(","));
		
		for (int i = 0; i < keys.size(); i ++) {
			countryBorders.put(keys.get(i), Integer.parseInt(values.get(i)));
		}
		
		return countryBorders;
	}
	
	/**
	 * Technical function that converts a text file (given with the filePathAndName) to 
	 * a list of strings where each element in the list is a line in the text file
	 * @param filePathAndName
	 * @return
	 */
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