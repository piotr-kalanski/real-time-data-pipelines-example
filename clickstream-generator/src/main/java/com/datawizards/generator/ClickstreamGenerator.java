package com.datawizards.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Date;
import java.util.Random;

public class ClickstreamGenerator {
    public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException {
        Logger log = LoggerFactory.getLogger("ClickstreamGenerator");

        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432", "postgres", "postgres");

        // create table
        connection.createStatement().execute("" +
                "CREATE TABLE IF NOT EXISTS CLICKSTREAM (\n" +
                "  event_id INTEGER NOT NULL PRIMARY KEY,\n" +
                "  event_type VARCHAR(100) NOT NULL,\n" +
                "  event_date TIMESTAMP NOT NULL,\n" +
                "  user_id VARCHAR(100),\n" +
                "  listing_id VARCHAR(100),\n" +
                "  device VARCHAR(100)\n" +
                ");"
        );

        // get last event id
        ResultSet rs = connection.createStatement().executeQuery("SELECT max(event_id) as event_id FROM CLICKSTREAM");
        int eventId = 1;
        if(rs.next()) {
            eventId = rs.getInt("event_id") + 1;
        }

        log.info("First event id: " + eventId);

        Random r = new Random();
        String[] eventTypes = new String[] {"LISTING_VIEW", "APPLICATION", "LOGIN", "REGISTER"};
        String[] devices = new String[] {"DESKTOP", "MOBILE_APP", "MOBILE_WWW", "TABLE_WWW"};

        // Start generating data
        while(true) {
            String event_type = eventTypes[r.nextInt(eventTypes.length-1)];
            Date event_date = new Date();
            String user_id = "" + r.nextInt(100);
            String listing_id = "" + r.nextInt(100);
            String device = devices[r.nextInt(devices.length-1)];

            Statement statement = connection.createStatement();
            String query = "INSERT INTO CLICKSTREAM(" +
                    "event_id," +
                    "event_type," +
                    "event_date," +
                    "user_id," +
                    "listing_id," +
                    "device" +
                    ")" +
                    "VALUES(" +
                    "" + eventId + "," +
                    "'" + event_type + "'," +
                    "'" + event_date + "'," +
                    "'" + user_id + "'," +
                    "'" + listing_id + "'," +
                    "'" + device + "'" +
                    ")";
            statement.execute(query);
            statement.close();
            log.info("Inserted event id: " + eventId);
            Thread.sleep(1000);
            eventId++;
        }
    }
}
