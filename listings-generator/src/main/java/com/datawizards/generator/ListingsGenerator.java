package com.datawizards.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Random;

public class ListingsGenerator {
    private static Logger log = LoggerFactory.getLogger("ListingsGenerator");

    public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException {
        Class.forName("org.postgresql.Driver");
        String host = System.getenv().getOrDefault("POSTGRES_HOST", "localhost");
        // retry connecting with DB
        Connection connection = tryConnecting(host, 5);

        // create table
        connection.createStatement().execute("" +
                "CREATE TABLE IF NOT EXISTS LISTINGS (\n" +
                "  listing_id INTEGER NOT NULL PRIMARY KEY,\n" +
                "  job_title VARCHAR(100) NOT NULL,\n" +
                "  discipline VARCHAR(100) NOT NULL,\n" +
                "  city VARCHAR(100) NOT NULL\n" +
                ");"
        );

        // get last event id
        ResultSet rs = connection.createStatement().executeQuery("SELECT max(listing_id) as listing_id FROM LISTINGS");
        int listingId = 1;
        if(rs.next()) {
            listingId = rs.getInt("listing_id") + 1;
        }

        log.info("First listing id: " + listingId);

        Random r = new Random();
        String[] cities = new String[] {"Warsaw", "Berlin", "London", "Paris", "Moscow", "Madryt"};
        String[] job_titles = new String[] {"Big Data Developer", "Scala Developer", "Team Leader", "Java Developer", "Software Architect", "DevOps"};
        String[] disciplines = new String[] {"IT", "Finance", "Marketing", "Sales"};
        // Start generating data
        while(true) {
            String city = cities[r.nextInt(cities.length)];
            String job_title = job_titles[r.nextInt(job_titles.length)];
            String discipline = disciplines[r.nextInt(disciplines.length)];

            Statement statement = connection.createStatement();
            String query = "INSERT INTO LISTINGS(" +
                    "listing_id," +
                    "job_title," +
                    "discipline," +
                    "city" +
                    ")" +
                    "VALUES(" +
                    "" + listingId + "," +
                    "'" + job_title + "'," +
                    "'" + discipline + "'," +
                    "'" + city + "'" +
                    ")";
            statement.execute(query);
            statement.close();
            log.info("Inserted listing id: " + listingId);
            Thread.sleep(1000);
            listingId++;
        }
    }

    private static Connection tryConnecting(String host, int retries) throws InterruptedException {
        if (retries == 0)
            throw new RuntimeException("Connection with DB not working!");

        try {
            log.info("Connecting with DB " + host);
            return DriverManager.getConnection("jdbc:postgresql://" + host + ":5432", "postgres", "postgres");
        } catch (Exception e) {
            Thread.sleep(1000 * retries);
            return tryConnecting(host, retries-1);
        }
    }
}
