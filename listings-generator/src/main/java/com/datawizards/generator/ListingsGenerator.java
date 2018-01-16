package com.datawizards.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Random;

public class ListingsGenerator {
    public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException {
        Logger log = LoggerFactory.getLogger("com.datawizards.generator.ListingsGenerator");

        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432", "postgres", "postgres");

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
}
