package com.company;

import java.sql.*;
import java.sql.Date;
import java.util.*;

// Demonstrating some example JDBC/MySQL interactions.

public class DogDB {

    //TODO create vet database,
    //   mysql > create database vet;
    //TODO grant create, insert, select, update, drop permissions to your user
    //   mysql > grant create, insert, select, update, drop on vet.* to 'YOURUSERNAME'@'localhost';


    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";        //Configure the driver needed
    static final String DB_CONNECTION_URL = "jdbc:mysql://localhost:3306/vet";     //Connection string – where's the database?
    static final String USER = "clara";   //TODO replace with your username
    static final String PASSWORD = System.getenv("MYSQL_PW");   //TODO remember to set the environment variable
    // static final String PASSWORD = "password";   // If on lab PC, uncomment this line and replace "password" with your own password

    public static void main(String[] args) {

        System.out.println("Veterinarian Database Program");

        try {

            Class.forName(JDBC_DRIVER);

        } catch (ClassNotFoundException cnfe) {
            System.out.println("Can't instantiate driver class; check you have drives and classpath configured correctly?");
            cnfe.printStackTrace();
            System.exit(-1);  //No driver? Need to fix before anything else will work. So quit the program
        }


        /* Create a table, and insert some test data */

        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
            Statement statement = conn.createStatement()) {

            /* Create a table in the database. Stores dog's names, ages, weights and whether the dog has been vaccinated or not. */

            String createTableSQL = "CREATE TABLE IF NOT EXISTS Dog (Name varchar(30), Age int, Weight double, Vaccinated boolean, vaxdate date )";
            statement.executeUpdate(createTableSQL);
            System.out.println("Created dog table");

            /* Add one row of test data, using a prepared statement */

            String prepStatInsert = "INSERT INTO Dog VALUES ( ? , ?, ? , ?, ?)";
            PreparedStatement psInsert = conn.prepareStatement(prepStatInsert);

            psInsert.setString(1, "Sam");
            psInsert.setInt(2, 6);
            psInsert.setDouble(3, 54.4);
            psInsert.setBoolean(4, true);
            psInsert.setDate(5, Date.valueOf("2012-04-01"));
            psInsert.executeUpdate();

            System.out.println("Added a row of test data to the database");

            /* Add another row of test data. Can re-use the same PreparedStatement. */

            psInsert.setString(1, "Zeke");
            //Maybe we don't know how old Zeke is
            //But the PreparedStatement insists that we put something here
            psInsert.setNull(2, Types.INTEGER);
            //Use Types.INTEGER or Types.DATE or Types.VARCHAR or whatever the type of the column is
            //http://docs.oracle.com/javase/7/docs/api/java/sql/Types.html

            psInsert.setDouble(3, 54.4);
            psInsert.setBoolean(4, true);
            psInsert.setDate(5, Date.valueOf("2012-04-01"));
            psInsert.executeUpdate();

            System.out.println("Added another row of test data to the database");


            /* And add some more data, this time the data comes from arrays. Loop over arrays and use data in the PreparedStatement */

            String[] testNames = {"Clifford", "Einstein", "Lassie", "Blue"};
            int[] testAges = {1, 3, 10, 6};
            double[] testWeights = {1200, 35, 50, 25};
            boolean[] testVaccinated = {true, true, false, true,};
            Date[] testVaxDates = {Date.valueOf("2011-11-11"), Date.valueOf("2010-02-02"), null, Date.valueOf("2014-01-01")};

            for (int i = 0; i < testNames.length; i++) {
                psInsert.setString(1, testNames[i]);
                psInsert.setInt(2, testAges[i]);
                psInsert.setDouble(3, testWeights[i]);
                psInsert.setBoolean(4, testVaccinated[i]);
                psInsert.setDate(5, testVaxDates[i]);
                psInsert.executeUpdate();
            }

            System.out.println("Added test data to database");

            //close connection, statement, prepared statement
            psInsert.close();
            statement.close();
            conn.close();

        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.exit(-1);
        }



        /** Fetch all of the data from the database */

        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement()) {

            /* Fetch all the data and display it. */

            String fetchAllDataSQL = "SELECT * FROM Dog";
            ResultSet rs = statement.executeQuery(fetchAllDataSQL);

            while (rs.next()) {
                String name = rs.getString("name");
                int age = rs.getInt("age");
                double weight = rs.getDouble("weight");
                Date vaxDate = rs.getDate("vaxdate");
                String vaxInfo = (vaxDate != null) ? "Vaccinated on " + vaxDate : "Dog not vaccinated";
                System.out.println("Dog name = " + name + " age = " + age + " weight = " + weight + " Vaccination info: " + vaxInfo);
            }


            System.out.println();
            System.out.println("Retrieved and displayed test data");
            System.out.println();

            rs.close();  //and close result set when done
            statement.close();  //and statement
            conn.close();   //and connection

        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.exit(-1);
        }


        /* query the database for a dog's name */

        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD)) {

            Scanner scan = new Scanner(System.in);

            while (true) {
                System.out.println("Enter dog name to find in database, or enter to continue program");
                String dogname = scan.nextLine();
                if (dogname.equals("")) {
                    break;    //break out of loop, on to the next task.
                }

                //What if we don't care about case? Have to convert all data to uppercase and compare (or convert to lower and compare)
                PreparedStatement findDog = conn.prepareStatement("SELECT * FROM DOG where UPPER(name) = UPPER(?)");

                findDog.setString(1, dogname);

                ResultSet rs = findDog.executeQuery();

                boolean dogfound = false;
                while (rs.next()) {
                    dogfound = true;
                    String name = rs.getString("name");
                    int age = rs.getInt("age");
                    double weight = rs.getDouble("weight");
                    boolean vaccinated = rs.getBoolean("vaccinated");
                    System.out.println("Dog name = " + name + " age = " + age + " weight = " + weight + " vaccinated? = " + vaccinated);
                }
                if (!dogfound) {
                    System.out.println("Sorry, dog not found in database");
                }

                rs.close();
            }

            conn.close();

        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.exit(-1);
        }



        /* And some more queries...  */

        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement()) {

            //Get the names of all the dogs who are not vaccinated

            System.out.println();
            System.out.println("Names of dogs who are not vaccinated");
            String notVax = "SELECT name FROM dog WHERE vaccinated = FALSE";

            ResultSet rsNotVax = statement.executeQuery(notVax);
            while (rsNotVax.next()) {
                System.out.println(rsNotVax.getString("name"));
            }

            //Find all the dogs who weigh more than 20 pounds
            System.out.println();
            System.out.println("Names, weights, and ages of dogs who weigh over 20 pounds");
            String over20lbs = "SELECT name, weight, age FROM dog WHERE weight > 20";
            ResultSet rsOver20 = statement.executeQuery(over20lbs);
            while (rsOver20.next()) {
                System.out.println(rsOver20.getString("name") + " is " + rsOver20.getInt("age") + " years old and weighs " + rsOver20.getDouble("weight"));
            }


            //Another use of parameters. Find all puppies - dogs age less than a particular value

            System.out.println();
            System.out.println("Finding all puppies, age 1 or less");
            String findPupSQL = "SELECT * FROM Dog WHERE AGE <= ?";
            PreparedStatement psFindPuppies = conn.prepareStatement(findPupSQL);

            int puppyMaxAge = 1;
            psFindPuppies.setInt(1, puppyMaxAge);

            ResultSet rsPuppies = psFindPuppies.executeQuery();
            while (rsPuppies.next()) {
                String name = rsPuppies.getString("name");
                int age = rsPuppies.getInt("age");
                double weight = rsPuppies.getDouble("weight");
                boolean vaccinated = rsPuppies.getBoolean("vaccinated");
                System.out.println("Puppy name = " + name  + " age = " + age + " weight = " + weight + " vaccinated? = " + vaccinated) ;
            }

            //Find all dogs who are not called "Blue". Use a regular statement since this query is hard-coded.
            System.out.println();
            System.out.println("Names of dogs who are not called 'Blue'");
            //Can use the SQL UPPER function here if you need to be case insensitive.
            String notBlue = "SELECT * FROM Dog WHERE name != 'Blue'";

            //You can also use <> instead of != to test if something is not equal, like this.
            String alternativeNotEqualsBlue = "SELECT * FROM Dog WHERE name <> 'Blue'";  //either of these works

            ResultSet rsNotBlue = statement.executeQuery(notBlue);
            while (rsNotBlue.next()) {
                String name = rsNotBlue.getString("name");
                System.out.println("This dog is not called Blue, it is called :  " + name) ;
            }

            //Close ALL the things
            rsNotVax.close();
            rsOver20.close();
            rsNotBlue.close();
            rsPuppies.close();

            psFindPuppies.close();

            statement.close();
            conn.close();


        } catch (SQLException sqle) {
                sqle.printStackTrace();
                System.exit(-1);
        }


        /* Update database */

        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement()) {

            //Lassie has put on weight.... Update her record

            System.out.println();
            System.out.println("Updating Lassie's weight to 30, displaying all data");

            String updateLassie = "UPDATE Dog SET weight=30 WHERE name='Lassie'";
            statement.executeUpdate(updateLassie);

            //Optional - verify changes were made by displaying all data.

            String fetchAllDataSQL = "SELECT * FROM Dog";

            ResultSet rs = statement.executeQuery(fetchAllDataSQL);
            while (rs.next()) {
                String name = rs.getString("name");
                int age = rs.getInt("age");
                double weight = rs.getDouble("weight");
                boolean vaccinated = rs.getBoolean("vaccinated");
                System.out.println("Dog name = " + name + " age = " + age + " weight = " + weight + " vaccinated? = " + vaccinated);
            }

            //and close all of the things
            rs.close();
            statement.close();
            conn.close();

        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.exit(-1);
        }

            

        /* Some aggregate queries - average, min, max */

        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement()) {

            String averageAgeSQL = "SELECT AVG(age) AS avg_age FROM dog";
            ResultSet rsAvg = statement.executeQuery(averageAgeSQL);
            rsAvg.next();  //Move rs cursor to the first row
            int average = rsAvg.getInt("avg_age");  //this should be in a try-catch in case nothing is returned
            //Also note that the average, max, min of integer data is also an integer
            ///use double or float as your column type if you need more precision
            System.out.println("Average age of all dogs = " + average);

            //close things as you get done with them
            rsAvg.close();

            //Can add a WHERE clause to any of these statements. Average age of non-vaccinated dogs
            String averageAgeNotVaxSQL = "SELECT AVG(age) AS avg_age FROM dog WHERE vaccinated = false";
            ResultSet rsAvgNonVax = statement.executeQuery(averageAgeNotVaxSQL);
            rsAvgNonVax.next();  //Move rs cursor to the first row
            int averageNotVax = rsAvgNonVax.getInt("avg_age");
            System.out.println("Average age of all un-vaccinated dogs = " + averageNotVax);
            rsAvgNonVax.close();

            //Oldest dog that weighs less than 40 lbs
            String maxAgeSQL = "SELECT MAX(age) AS max_age FROM dog WHERE weight < 40";
            ResultSet rsMaxAge = statement.executeQuery(maxAgeSQL);
            rsMaxAge.next();  //Move rs cursor to the first row
            int maxAgeVal = rsMaxAge.getInt("max_age");
            System.out.println("Max age of dogs under 40lbs = " + maxAgeVal);
            rsMaxAge.close();

            //How many dogs? COUNT(*) counts the rows. COUNT(age) will count the unique values in the age column
            //and store the result in a new, temporary column called num_dogs
            String countSQL = "SELECT COUNT(*) AS num_dogs FROM dog";
            ResultSet rsCount = statement.executeQuery(countSQL);
            rsCount.next();  //Move rs cursor to the first row
            int totalDogs = rsCount.getInt("num_dogs");   //and get the value of this temporary num_dogs column
            System.out.println("Total number of dogs = " + totalDogs);
            rsCount.close();

            //Sum the data in a column. What do all the dogs weigh altogether?
            String totalWeightSQL = "SELECT SUM(weight) AS total FROM dog";
            ResultSet rsTotal = statement.executeQuery(totalWeightSQL);
            rsTotal.next();  //Move rs cursor to the first row
            double totalWeight = rsTotal.getDouble("total");    //weight column is a double, so the DB returns a double
            System.out.println("Total weight of all dogs = " + totalWeight);
            rsTotal.close();

            //Each ResultSet closed as soon as code is done with it
            //A statement can be used to make many ResultSets, but creating a new ResultSet will close the previous one
            //If you need two ResultSets in use at once, you need to create two statements, and create a ResultSet from each.
            statement.close();
            conn.close();

        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.exit(-1);
        }



        /* Delete the table, so can start afresh next time this program is run. You probably won't do this in a real program. */

        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement()) {

            String dropTable = "DROP TABLE dog";
            statement.executeUpdate(dropTable);
            System.out.println("Deleted dog table");

            statement.close();
            conn.close();

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }


        System.out.println("End of program");
    }
}
