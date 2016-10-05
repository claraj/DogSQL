package com.company;

import java.sql.*;
import java.sql.Date;
import java.util.*;


//Added a date of vaccination, more queries, and identity column.


public class DogDB {
    
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";        //Configure the driver needed
    static final String DB_CONNECTION_URL = "jdbc:mysql://localhost:3306/vet";     //Connection string – where's the database?
    static final String USER = "clara";   //TODO replace with your username
    static final String PASSWORD = "clara";   //TODO replace with your password


    
    public static void main(String[] args) {
        
        Statement statement = null;
        Connection conn = null;
        ResultSet rs = null;
        
        PreparedStatement psInsert = null;
        PreparedStatement findDog = null;
        PreparedStatement findPuppies = null;
        
        ArrayList<Statement> allStatements = new ArrayList<Statement>();  //Keep track of all these statements so they can be closed


        try {

            Class.forName(JDBC_DRIVER);

        } catch (ClassNotFoundException cnfe) {
            System.out.println("Can't instantiate driver class; check you have drives and classpath configured correctly?");
            cnfe.printStackTrace();
            System.exit(-1);  //No driver? Need to fix before anything else will work. So quit the program
        }


        try{
            conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
            statement = conn.createStatement();
            allStatements.add(statement);
            
            System.out.println("Veterinarian Database Program");
            
            //Create a table in the database. Stores dog's names, ages, weights and whether the dog has been vaccinated or not.

            
            String createTableSQL = "CREATE TABLE IF NOT EXISTS Dog (Name varchar(30), Age int, Weight double, Vaccinated boolean, vaxdate date )";
            statement.executeUpdate(createTableSQL);
            System.out.println("Created dog table");

            //Add some test data
            
            String[] testNames = { "Clifford", "Einstein", "Lassie", "Blue" };
            int[] testAges = { 1, 3, 10, 6};
            double[] testWeights = { 1200, 35, 50, 25 };
            boolean[] testVaccinated = { true, true, false, true, };
            Date[] testVaxDates = { Date.valueOf("2011-11-11"), Date.valueOf("2010-02-02"), null, Date.valueOf("2014-01-01") } ;
            
            
            String prepStatInsert = "INSERT INTO Dog VALUES ( ? , ?, ? , ?, ?)";
            psInsert = conn.prepareStatement(prepStatInsert);
            allStatements.add(psInsert);
            for (int i = 0 ; i < testNames.length ; i++) {
                psInsert.setString(1, testNames[i]);
                psInsert.setInt(2, testAges[i]);
                psInsert.setDouble(3, testWeights[i]);
                psInsert.setBoolean(4, testVaccinated[i]);
                psInsert.setDate(5, testVaxDates[i]);
                psInsert.executeUpdate();
            }
            
            System.out.println("Added test data to database");
            
            prepStatInsert = "INSERT INTO Dog VALUES ( ? , ?, ? , ?, ?)";

            psInsert = conn.prepareStatement(prepStatInsert);
            
            psInsert.setString(1, "Zeke");
            
            //Maybe we don't know how old Zeke is
            //But the PreparedStatement insists that we put something here
            psInsert.setNull(2, Types.INTEGER);
            
            //Use Types.INTEGER or Types.DATE or Types.VARCHAR
            //or whatever the type of the column is
            //http://docs.oracle.com/javase/7/docs/api/java/sql/Types.html
            
            psInsert.setDouble(3, 54.4);
            psInsert.setBoolean(4, true);
            psInsert.setDate(5, Date.valueOf("2012-04-01"));
            psInsert.executeUpdate();
            


            //Fetch all the data and display it.
            String fetchAllDataSQL = "SELECT * FROM Dog";
            rs = statement.executeQuery(fetchAllDataSQL);
            while (rs.next()) {
                String name = rs.getString("name");
                int age = rs.getInt("age");
                double weight = rs.getDouble("weight");
                Date vaxDate = rs.getDate("vaxdate");
                String vaxInfo = (vaxDate != null) ? "Vaccinated on " + vaxDate : "Dog not vaccinated";
                System.out.println("Dog name = " + name  + " age = " + age + " weight = " + weight + " Vaccination info: " + vaxInfo) ;
                
            }
            
            System.out.println();
            System.out.println("Retrived and displayed test data");
            System.out.println();


            Scanner scan = new Scanner(System.in);
            
            while (true) {
                System.out.println("Enter dog name to find in database, or enter to continue program");
                String dogname = scan.nextLine();
                if (dogname.equals("")) { break; }
                //What if we don't care about case? Have to convert all data to uppercase and compare (or convert to lower and compare)
                findDog = conn.prepareStatement("SELECT * FROM DOG where UPPER(name) = UPPER(?)");
                allStatements.add(findDog);
                findDog.setString(1, dogname);
                rs = findDog.executeQuery();
                
                boolean dogfound = false;
                while (rs.next()) {
                    dogfound = true;
                    String name = rs.getString("name");
                    int age = rs.getInt("age");
                    double weight = rs.getDouble("weight");
                    boolean vaccinated = rs.getBoolean("vaccinated");
                    System.out.println("Dog name = " + name  + " age = " + age + " weight = " + weight + " vaccinated? = " + vaccinated) ;
                }
                if (dogfound == false) {
                    System.out.println("Sorry, dog not found in database");
                }
                
            }
            
            //Some more queries...

            //Get the names of all the dogs who are not vaccinated

            System.out.println();
            System.out.println("Names of dogs who are not vaccinated");
            String notVax = "SELECT name FROM dog WHERE vaccinated=FALSE";
            rs = statement.executeQuery(notVax);
            while (rs.next()) {
                System.out.println(rs.getString("name"));
            }
            
            //Find all the dogs who weigh more than 20 pounds
            System.out.println();
            System.out.println("Names, weights, and ages of dogs who weigh over 20 pounds");
            String over20 = "SELECT name, weight, age FROM dog WHERE weight > 20";
            rs = statement.executeQuery(over20);
            while (rs.next()) {
                System.out.println(rs.getString("name") + " is " + rs.getInt("age") + " years old and weighs " + rs.getDouble("weight"));
            }
            
            //Lassie has put on weight.... Update her record
            
            System.out.println();
            System.out.println("Updating Lassie's weight to 30, displaying all data");

            String updateLassie = "UPDATE Dog SET weight=30 WHERE name='Lassie'";
            statement.executeUpdate(updateLassie);
            //Verify changes were made by displaying all data.
            
            rs = statement.executeQuery(fetchAllDataSQL);
            while (rs.next()) {
                String name = rs.getString("name");
                int age = rs.getInt("age");
                double weight = rs.getDouble("weight");
                boolean vaccinated = rs.getBoolean("vaccinated");
                System.out.println("Dog name = " + name  + " age = " + age + " weight = " + weight + " vaccinated? = " + vaccinated) ;
            }
            
            //Another use of parameters. Find all puppies - dogs age 1 or less
            
            System.out.println();
            System.out.println("Finding all puppies, age 1 or less");
            String findPupSQL = "SELECT * FROM Dog WHERE AGE <= ?";
            findPuppies = conn.prepareStatement(findPupSQL);
            allStatements.add(findPuppies);
            int puppyMaxAge = 1;
            findPuppies.setInt(1, puppyMaxAge);
            rs = findPuppies.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                int age = rs.getInt("age");
                double weight = rs.getDouble("weight");
                boolean vaccinated = rs.getBoolean("vaccinated");
                System.out.println("Puppy name = " + name  + " age = " + age + " weight = " + weight + " vaccinated? = " + vaccinated) ;
            }
            
            //Find all dogs who are not called "Blue"

            System.out.println();
            System.out.println("Names of dogs who are not called 'Blue'");
            //Can use the SQL UPPER function here if you need to be case insensitive.
            String notBlue = "SELECT * FROM Dog WHERE name != 'Blue'";
            String alternativeNotEquals = "SELECT * FROM Dog WHERE name <> 'Blue'";  //either of these works
            rs = statement.executeQuery(notBlue);
            while (rs.next()) {
                String name = rs.getString("name");
                System.out.println("This dog is not called Blue, it is called :  " + name) ;
            }
            
            //Run some more queries..
            
            String averageAgeSQL = "SELECT AVG(age) AS avg_age FROM dog";
            rs = statement.executeQuery(averageAgeSQL);
            rs.next();  //Move rs cursor to the first row
            int average = rs.getInt("avg_age");  //this should be in a try-catch in case nothing is returned
            //Also note that the average, max, min of integer data is also an integer
            ///use double or float as your column type if you need more precision
            System.out.println("Average age of all dogs = " + average);

            //Can add a WHERE clause to any of these statements
            String averageAgeNotVaxSQL = "SELECT AVG(age) AS avg_age FROM dog WHERE vaccinated = false";
            rs = statement.executeQuery(averageAgeNotVaxSQL);
            rs.next();  //Move rs cursor to the first row
            int averageNotVax = rs.getInt("avg_age");
            System.out.println("Average age of all un-vaccinated dogs = " + averageNotVax);

            //Oldest dog that weighs less than 40 lbs
            String maxAgeSQL = "SELECT MAX(age) AS max_age FROM dog WHERE weight < 40";
            rs = statement.executeQuery(maxAgeSQL);
            rs.next();  //Move rs cursor to the first row
            int maxAgeVal = rs.getInt("max_age");
            System.out.println("Max age of dogs under 40lbs = " + maxAgeVal);

            //How many dogs? COUNT(*) counts the rows. COUNT(age) will count the unique values in the age column
            String countSQL = "SELECT COUNT(*) AS no_dogs FROM dog";
            rs = statement.executeQuery(countSQL);
            rs.next();  //Move rs cursor to the first row
            int totalDogs = rs.getInt("no_dogs");
            System.out.println("Total number of dogs = " + totalDogs);

            //Sum the data in a column. What do all the dogs weigh altogether?
            String totalWeightSQL = "SELECT SUM(weight) AS total FROM dog";
            rs = statement.executeQuery(totalWeightSQL);
            rs.next();  //Move rs cursor to the first row
            double totalWeight = rs.getDouble("total");    //weight column is a double, so the DB returns a double
            System.out.println("Total weight of all dogs = " + totalWeight);
            

            //Delete the table, so can start afresh next time this program is run. You probably won't do this in a real program.
            String dropTable = "DROP TABLE dog";
            statement.executeUpdate(dropTable);
            System.out.println("Deleted dog table");




        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        finally {
            //A finally block runs whether an exception is thrown or not. Close resources and tidy up whether this code worked or not.
            try {
                if (rs != null) {
                    rs.close();  //Close result set
                    System.out.println("ResultSet closed");
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
            
            //Close all of the statements. Stored a reference to each statement in allStatements so we can loop over all of them and close them all.
            for (Statement s : allStatements) {
                
                if (s != null) {
                    try {
                        s.close();
                        System.out.println("Statement closed");
                    } catch (SQLException se) {
                        System.out.println("Error closing statement");
                        se.printStackTrace();
                    }
                }
            }
            
            try {
                if (conn != null) {
                    conn.close();  //Close connection to database
                    System.out.println("Database connection closed");
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        
        System.out.println("End of program");
    }
}
