package supplyChainModel.common;

import repast.simphony.engine.environment.RunEnvironment;

/**
* Logs stuff
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public final class Logger {

	// Initialize variables
	private static boolean logErrors = true;
	private static boolean logMain = true;
	private static boolean logRemove = true;
	private static boolean logInfo = true;
	private static boolean logId = true;
	private static int logIdIndex = -1;
	
	public static void enableLogger() {
		logErrors = true;
		logMain = true;
		logInfo = true;
		logId = true;
	}
	
	public static void disableLogger() {
		logErrors = true;
		logMain = false;
		logInfo = false;
		logId = false;
	}
	
	public static void setLogErrors(boolean logErrors) {
		Logger.logErrors = logErrors;
	}
	
	public static void logError(String error) {
		if (logErrors) {
			System.err.println("Error: " + error);
			new Exception().printStackTrace();
			RunEnvironment.getInstance().endRun();
		}
	}
	
	public static void logMain(String output) {
		if (logMain)
			System.out.println(output);
	}
	
	public static void logRemove(String output) {
		if (logRemove)
			System.out.println("  - " + output);
	}
	
	public static void logInfo(String output) {
		if (logInfo)
			System.out.println(" - " + output);
	}
	
	public static void resetId() {
		logIdIndex = -1;
	}
	
	/**
	 * Log only the output of one specific agent based on id
	 * @param output
	 * @param id
	 */
	public static void logInfoId(int id, String output) {
		if (logId && (logIdIndex == id || logIdIndex == -1)) {
			
			logIdIndex = id;
			System.out.println(" - " + output);
		}
	}
}