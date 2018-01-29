package kragt.logan.csci392.importimdb;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionFactory {
	private static final String DRIVER_CLASS = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	private static String connectionString = "jdbc:sqlserver://sql.cs.hope.edu\\CSSQL:1433";
	private static String username = System.getenv("392_USERNAME");
	private static String password = System.getenv("392_PASSWORD");
	
	static {
		//  Use class.forName to ensure that the classes implementing the JTDS JDBC driver are loaded
		try {
			Class.forName(DRIVER_CLASS);
			driverFound = true;
		} catch (ClassNotFoundException e) {
			driverFound = false;
		}
	}
	
	static boolean driverFound;
	
	/**
	 * Creates a java.sql.Connection object that is attached to a database.
	 * @return
	 */
	public static Connection getConnection () {
		if (!driverFound) {
			throw new RuntimeException ("Could not find the JDBC driver (" + DRIVER_CLASS + ").  Please ensure you have the appropriate JAR file on your class path");
		}
		
		try {
			return DriverManager.getConnection(connectionString, username, password);
		}
		catch (Exception e) {
			return null;
		}
	}
}
