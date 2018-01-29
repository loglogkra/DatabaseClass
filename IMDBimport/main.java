package kragt.logan.csci392.importimdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;

public class Main {
	
	//parsed out TSV files
	private static ArrayList<String[]> titles = new ArrayList<String[]>();
	private static ArrayList<String[]> crew = new ArrayList<String[]>();
	private static ArrayList<String[]> names = new ArrayList<String[]>();
	private static ArrayList<String[]> principals = new ArrayList<String[]>();
	
	//maps to store the ID's of movies and actors with tsv ID as key and actual db id as value
	private static HashMap<String,Integer> insertedMovies = new HashMap<String,Integer>();
	private static HashMap<String,Integer> insertedActors = new HashMap<String,Integer>();
	
	//set that contains the tsv file movie id of the movies we need to insert.
	private static HashSet<String> titleIDSet =  new HashSet<String>();

	/**
	 * calls methods to read in .tsv files and fill an arraylist 
	 * with an array of all the lines split by tabs.
	 * 
	 * inserts the items that need to be inserted by calling methods for each.
	 * @param args
	 * @throws IOException
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws IOException, SQLException{
		
		//parse tsv files
		fillTitleList();
		fillCrewList();
		fillNamesList();
		fillPrincipalsList();
		
		//insert stuff
		insertMovies();
		insertActors();
		checkInsertRoles();

	}
	
	/**
	 * Read crew.tsv and turn each line into a String[] where each
	 * item in the array is one of the columns in the tsv.
	 * @throws IOException
	 */
	public static void fillCrewList() throws IOException{
		InputStream is = Main.class.getResourceAsStream("crew.tsv");
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(isr);
		String s = reader.readLine();
		while(s != null){
			String[] line = s.split("\\t");
			
			if(titleIDSet.contains(line[0])){
			crew.add(line);
			}
			
			s = reader.readLine();
		}
	}
	
	
	/**
	 * Read titles.tsv and turn each line into a String[] where each
	 * item in the array is one of the columns in the tsv.
	 * @throws IOException
	 * @throws SQLException 
	 */
	public static void fillTitleList() throws IOException, SQLException{
		InputStream is = Main.class.getResourceAsStream("titles.tsv");
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(isr);
		String s = reader.readLine();
		while(s != null){
			String[] line = s.split("\\t");
			
			//check that the title is not a tvEpisode
			if(!line[1].equals("tvEpisode")){
				//check that title is not an adult film
				if(line[4].equals("0")){
					// try to check if movie is in the database.
					if(!checkMovieExists(line[2], line[5])){
					titles.add(line);
					titleIDSet.add(line[0]);
					}
				}
			}
			//read the next line
			s = reader.readLine();
		}
	}
	
	/**
	 * Read names.tsv and turn each line into a String[] where each
	 * item in the array is one of the columns in the tsv.
	 * @throws IOException
	 * @throws SQLException 
	 */
	public static void fillNamesList() throws IOException, SQLException{
		InputStream is = Main.class.getResourceAsStream("names.tsv");
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(isr);
		
		String s = reader.readLine();
		while(s != null){
			String[] line = s.split("\\t");
			HashMap<String,String> nameMap = formatActorName(line[1]);
			String[] actorName = new String[2];
			actorName[0] = nameMap.get("first");
			actorName[1] = nameMap.get("last");
			if(line[2].equals("\\N")) {
				line[2] = "0";
			}
			boolean yesActor = false;
			String[] isActor = line[4].split(",");
			
			for(String job : isActor){
				if(job.equals("actor") || job.equals("actress")){
					yesActor = true;
				}
			}
			
			if(yesActor){
			if(!checkActorExists(actorName[0], actorName[1], Integer.parseInt(line[2]), line[0])) {
			names.add(line);
			}
			}
			s = reader.readLine();
		}
	}

	/**
	 * Read principals.tsv and turn each line into a String[] where each
	 * item in the array is one of the columns in the tsv.
	 * @throws IOException
	 */
	public static void fillPrincipalsList() throws IOException{
		InputStream is = Main.class.getResourceAsStream("principals.tsv");
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(isr);
		
		String s = reader.readLine();
		while(s != null){
			String[] line = s.split("\\t");
			
			if(titleIDSet.contains(line[0])){
			principals.add(line);
			}
			
			s = reader.readLine();
		}
	}

	/**
	 * Exectue a prepared statement to see if the given film is already in the database.
	 * @param title
	 * @param year
	 * @return
	 * @throws SQLException 
	 */
	public static boolean checkMovieExists(String title, String year) throws SQLException{
		
		Integer yearInt = Integer.parseInt(year);
		String selectSQL = "select MovieTitle, Year from Movies where MovieTitle = ? and Year = ?";
		
		Connection conn = null;
		
		//make a connection to the DB
		try {
			conn = ConnectionFactory.getConnection();
			if (conn != null) {
			//System.err.println("Success");
			PreparedStatement preparedStatement = conn.prepareStatement(selectSQL);
			preparedStatement.setString(1, title);
			preparedStatement.setInt(2, yearInt);
			ResultSet rs = preparedStatement.executeQuery();
			while(rs.next()){
				String returnedTitle = rs.getString("MovieTitle");
				int returnedYear = rs.getInt("Year");
				
				if(returnedTitle.equals(title) && returnedYear == yearInt){
					return true;
				}
			}
			}
			
			} catch (SQLException e) {
			System.err.println("Exception closing the connection");
			} finally{
				conn.close();
			}
		
		return false;
	}
	
	public static void insertMovies(){
		String insertSQL = "Insert into Movies (MovieID, MovieTitle, Year, RunningTime, Director, PrimaryGenre)"
				+ "values (?,?,?,?,?,?)";
		Connection conn = null;
		try{
		conn = ConnectionFactory.getConnection();
		if (conn != null) {
			for(String[] movie : titles){
				String directorID = "";
				String director = "";
				
				for(String[] d : crew){ //iterate through crew and find crew for this movie.
					if(d[0].equals(movie[0])){ //found movie
						directorID = d[1]; //director found
						if(directorID.length() > 9){ //has more than one director
							String[] dArray = directorID.split(","); //Separate and take first director
							directorID = dArray[0];
						}
						//find director id in names list and get name.
						for(String[] name : names){
							if(name[0].equals(directorID)){
								director = name[1];
								break;
							}
						}
						break; //no need to keep looping.
					}	
				}
				
				director = formatName(director);
				
				//split the genres and take the first as primary.
				String[] genres = movie[8].split(",");
				String primaryGenre = genres[0];
				int movieID = Integer.parseInt(movie[0].substring(2));
				
				insertedMovies.put(movie[0], movieID);
				
				PreparedStatement preparedStatement = conn.prepareStatement(insertSQL);
				preparedStatement.setInt(1, movieID); //movieId
				preparedStatement.setString(2, movie[2]); //title
				preparedStatement.setString(3, movie[5]); //year
				preparedStatement.setString(4, movie[7]); //running time
				preparedStatement.setString(5, director); //director
				preparedStatement.setString(6, primaryGenre); //primary genre
				
				preparedStatement.executeUpdate();
			}
				conn.close();
			}
			
		}catch (SQLException e) {
			System.err.println("Insert Not working right...");
			} 
	} 
	
	/**
	 * Check if actor already exists in db
	 * @param actor's fullname
	 * @param actor's year/dob
	 * @return
	 * @throws SQLException
	 */
	public static boolean checkActorExists(String firstName, String lastName, Integer year, String tsvID) throws SQLException{
		
		
		Integer dob;
		Date birthdate = null;
		
		String selectSQL = "select ActorID, StageFirstName, StageLastName, DateOfBirth"
				+ " from Actors where StageFirstName = ? and StageLastName = ?";
		
		
		Connection conn = null;
		try{
			conn = ConnectionFactory.getConnection();
			if(conn != null){
				
				PreparedStatement preparedStatement = conn.prepareStatement(selectSQL);
				preparedStatement.setString(1, firstName);
				preparedStatement.setString(2, lastName);
				
				ResultSet rs = preparedStatement.executeQuery();
				while(rs.next()){
					String returnedFirstName = rs.getString("StageFirstName");
					String returnedLastName = rs.getString("StageLastName");
					Timestamp returnedDOB = rs.getTimestamp("DateOfBirth");
					int returnedID = rs.getInt("ActorID"); //use this to map db ActorID to the one we have in tsv files.
					
					
					if(returnedFirstName.equals(firstName) && returnedLastName.equals(lastName)) {
						if(year == 0){
							insertedActors.put(tsvID, returnedID); //map actorID from db to tsv for roles insert.
							return true;
						}
						//if dob is null in DB or in tsv, assume exists if names match
						if(returnedDOB != null){
							birthdate = new Date(returnedDOB.getTime());
							dob = birthdate.getYear() + 1900;
							
							if(dob.equals(year)){
								insertedActors.put(tsvID, returnedID);
								return true;
								
							}else{ 
								return false;
							}
						}
						insertedActors.put(tsvID, returnedID);
						return true;
					}
				}
			}
	} catch (SQLException e) {
		System.err.println("Exception closing the connection");
		} finally{
			conn.close();
		}
	return false;
	}

	/**
	 * insert Actors into the db
	 */
	public static void insertActors(){
		String insertSQL = "Insert into Actors (ActorId, StageFirstName, StageLastName, DateOfBirth)"
				+ "values (?,?,?,?)";
		Connection conn = null;
		boolean nullYear = false;
		int actorID = 243548; // max actorID in DB
		try{
		conn = ConnectionFactory.getConnection();
		if (conn != null) {
			for(String[] actor : names){
				nullYear = false;
				//Integer actorID = Integer.parseInt((actor[0].substring(2)), 10);
				//actorID += 243548; // max id in DB, guarentee no duplicates.
				actorID ++;
				
				HashMap<String,String> nameMap = formatActorName(actor[1]);
				String[] actorName = new String[2];
				actorName[0] = nameMap.get("first");
				actorName[1] = nameMap.get("last");
				Integer year = Integer.parseInt(actor[2]);
				if(year.equals(0)){
					nullYear = true;
				}
			
				insertedActors.put(actor[0], actorID);
				
				PreparedStatement preparedStatement = conn.prepareStatement(insertSQL);
				preparedStatement.setInt(1, actorID);
				preparedStatement.setString(2, actorName[0]); //stagefirstname
				preparedStatement.setString(3, actorName[1]); //Stagelastname
				if(nullYear == false){
					if(year < 1900){
						preparedStatement.setDate(4, null);
					}else{
					java.sql.Date dob = new java.sql.Date(year - 1900, 1, 1);
					preparedStatement.setDate(4, dob); //DOB
					}
				}else{
					preparedStatement.setDate(4, null);
				}
				
				
				preparedStatement.executeUpdate();
			}
				conn.close();
			}
			
		}catch (SQLException e) {
			System.err.println("Insert actors Not working right...");
			System.out.println(e.toString());
			e.printStackTrace();
			} 
	}

	/**
	 * add roles for the movies we inserted
	 */
	public static void checkInsertRoles() {
		int roleID = 503552;
		for (String[] role : principals) {
			int creditNumber = 1;
			
			int movieID = insertedMovies.get(role[0]);

			String[] people = role[1].split(",");
			for (String person : people) {
				if (insertedActors.containsKey(person)) {
					insertRoles(roleID, movieID, insertedActors.get(person), creditNumber);
					creditNumber++;
					roleID++;
				}
			}
		}

	}

	/**
	 * insert into the Roles table
	 * @param RoleID
	 * @param MovieID
	 * @param ActorID
	 * @param creditNumber
	 */
	public static void insertRoles(int RoleID, int MovieID, int ActorID, int creditNumber){
		String insertSQL = "Insert into Roles (RoleID, ActorID, MovieID, CreditNumber) values (?, ?,?,?)";
		
		Connection conn = null;
		try{
			conn = ConnectionFactory.getConnection();
			if(conn != null){
				PreparedStatement preparedStatement = conn.prepareStatement(insertSQL);
				
				preparedStatement.setInt(1, RoleID);
				preparedStatement.setInt(2, ActorID);
				preparedStatement.setInt(3, MovieID);
				preparedStatement.setInt(4, creditNumber);
				
				preparedStatement.executeUpdate();
			}
			conn.close();
		}catch(SQLException e){
			System.err.println("Insert Roles not working.");
			System.out.println(e);
		}
	
	}	
	
	/*
	 * takes a name and formats in into Last, First.
	 */
	public static String formatName(String name){
		//break the name into its parts
		String[] nameParts = name.split(" ");
		String returnName = "";
		
		//check name length and format according to length
		if(nameParts.length == 1){
			returnName = nameParts[0];
		}else if(nameParts.length == 2){
			returnName = nameParts[1] + ", " + nameParts[0];
		}else if(nameParts.length == 3){
			returnName = nameParts[2] + ", " + nameParts[0];
		}else{
			for(int i = 2; i < nameParts.length; i++){
				returnName = returnName + " " + nameParts[i];
			}
			returnName = returnName + ", " + nameParts[0];
		}
		
		//return the formatted name.
		return returnName;
	}
	
	/*
	 * takes a name and formats in into stagefirstname and stagelastname.
	 */
	public static HashMap<String,String> formatActorName(String name){
		//break the name into its parts
		String[] nameParts = name.split(" ");
		HashMap<String,String> returnName = new HashMap<String,String>();
		
		//check name length and format according to length
		if(nameParts.length == 1){
			returnName.put("first", nameParts[0]);
		}else if(nameParts.length == 2){
			returnName.put("first", nameParts[0]);
			returnName.put("last", nameParts[1]);
		}else if(nameParts.length == 3){
			returnName.put("first", nameParts[0]);
			returnName.put("last", nameParts[2]);
		}else{
			String lastName ="";
			for(int i = 2; i < nameParts.length; i++){
				lastName = lastName + " " + nameParts[i];
			}
			returnName.put("first", nameParts[0]);
			returnName.put("last", lastName);
		}
		
		//return the formatted name.
		return returnName;
	}
	
	
}
