package com.datawizards.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Random;

public class UsersGenerator {
    private static Logger log = LoggerFactory.getLogger("UsersGenerator");

    public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException {
        Class.forName("org.postgresql.Driver");
        String host = System.getenv().getOrDefault("POSTGRES_HOST", "localhost");
        // retry connecting with DB
        Connection connection = tryConnecting(host, 5);

        // create table
        connection.createStatement().execute("" +
                "CREATE TABLE IF NOT EXISTS USERS (\n" +
                "  user_id INTEGER NOT NULL PRIMARY KEY,\n" +
                "  user_name VARCHAR(100) NOT NULL,\n" +
                "  title VARCHAR(10) NOT NULL,\n" +
                "  gender CHAR(1) NOT NULL,\n" +
                "  city VARCHAR(100)\n" +
                ");"
        );

        // get last event id
        ResultSet rs = connection.createStatement().executeQuery("SELECT max(user_id) as user_id FROM USERS");
        int userId = 1;
        if(rs.next()) {
            userId = rs.getInt("user_id") + 1;
        }

        log.info("First user id: " + userId);

        Random r = new Random();
        String[] cities = new String[] {"Warsaw", "Berlin", "London", "Paris", "Moscow", "Madryt"};
        String[] genders = new String[] {"M", "F"};
        String[] titles = new String[] {"Dr", "", "Msc", "Mr"};
        // Start generating data
        while(true) {
            String city = cities[r.nextInt(cities.length)];
            String gender = genders[r.nextInt(genders.length)];
            String title = titles[r.nextInt(titles.length)];
            String user_name = "user_" + userId;

            Statement statement = connection.createStatement();
            String query = "INSERT INTO USERS(" +
                    "user_id," +
                    "user_name," +
                    "title," +
                    "gender," +
                    "city" +
                    ")" +
                    "VALUES(" +
                    "" + userId + "," +
                    "'" + user_name + "'," +
                    "'" + title + "'," +
                    "'" + gender + "'," +
                    "'" + city + "'" +
                    ")";
            statement.execute(query);
            statement.close();
            log.info("Inserted user id: " + userId);
            Thread.sleep(1000);
            userId++;
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
