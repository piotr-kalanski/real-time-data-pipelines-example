package com.github.piotrkalanski;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ApplicationMainTest {

    private ApplicationMain app = new ApplicationMain();

    @Test
    public void testCalculateFavouriteDevice() {
        List<DeviceUsage> devices = Arrays.asList(
                DeviceUsage.newBuilder().setDevice("DESKTOP").setActionsCount(2L).build(),
                DeviceUsage.newBuilder().setDevice("MOBILE").setActionsCount(1L).build()
        );

        assertEquals("DESKTOP", app.calculateFavouriteDevice(devices));
    }

    @Test
    public void testCalculateDeviceUsageStartingWithEmptyList() {
        List<DeviceUsage> oldDevices = new LinkedList<>();
        List<DeviceUsage> expectedDevices = Arrays.asList(
                DeviceUsage.newBuilder().setDevice("DESKTOP").setActionsCount(1L).build()
        );
        Clickstream action = Clickstream
                .newBuilder()
                .setEventId(1)
                .setEventType("LV")
                .setEventDate(DateTime.now())
                .setDevice("DESKTOP")
                .build();

        assertEquals(expectedDevices, app.calculateDeviceUsage(action, oldDevices));
    }

    @Test
    public void testCalculateDeviceUsageWithEventWithoutDevice() {
        List<DeviceUsage> oldDevices = Arrays.asList(
                DeviceUsage.newBuilder().setDevice("DESKTOP").setActionsCount(1L).build()
        );
        List<DeviceUsage> expectedDevices = Arrays.asList(
                DeviceUsage.newBuilder().setDevice("DESKTOP").setActionsCount(1L).build()
        );
        Clickstream action = Clickstream
                .newBuilder()
                .setEventId(1)
                .setEventType("LV")
                .setEventDate(DateTime.now())
                .build();

        assertEquals(expectedDevices, app.calculateDeviceUsage(action, oldDevices));
    }

    @Test
    public void testCalculateDeviceUsageWithNewDevice() {
        List<DeviceUsage> oldDevices = new LinkedList<>();
        oldDevices.add(
                DeviceUsage.newBuilder().setDevice("DESKTOP").setActionsCount(1L).build()
        );
        List<DeviceUsage> expectedDevices = Arrays.asList(
                DeviceUsage.newBuilder().setDevice("DESKTOP").setActionsCount(1L).build(),
                DeviceUsage.newBuilder().setDevice("MOBILE").setActionsCount(1L).build()
        );
        Clickstream action = Clickstream
                .newBuilder()
                .setEventId(1)
                .setEventType("LV")
                .setEventDate(DateTime.now())
                .setDevice("MOBILE")
                .build();

        assertEquals(expectedDevices, app.calculateDeviceUsage(action, oldDevices));
    }

    @Test
    public void testCalculateDeviceUsageWithExistingDevice() {
        List<DeviceUsage> oldDevices = Arrays.asList(
                DeviceUsage.newBuilder().setDevice("DESKTOP").setActionsCount(1L).build(),
                DeviceUsage.newBuilder().setDevice("MOBILE").setActionsCount(1L).build()
        );
        List<DeviceUsage> expectedDevices = Arrays.asList(
                DeviceUsage.newBuilder().setDevice("DESKTOP").setActionsCount(2L).build(),
                DeviceUsage.newBuilder().setDevice("MOBILE").setActionsCount(1L).build()
        );
        Clickstream action = Clickstream
                .newBuilder()
                .setEventId(1)
                .setEventType("LV")
                .setEventDate(DateTime.now())
                .setDevice("DESKTOP")
                .build();

        assertEquals(expectedDevices, app.calculateDeviceUsage(action, oldDevices));
    }

    @Test
    public void testAddNewListing() {
        List<CharSequence> initListings = new LinkedList<>();
        initListings.add("listing1");
        initListings.add("listing2");
        Clickstream action = Clickstream
                .newBuilder()
                .setEventId(1)
                .setEventType("LV")
                .setEventDate(DateTime.now())
                .setListingId("listing3")
                .build();
        List<String> expected = Arrays.asList("listing1", "listing2", "listing3");

        assertEquals(expected, app.calculateListings(initListings, action));
    }

    @Test
    public void testAddExistingListing() {
        List<CharSequence> initListings = Arrays.asList("listing1", "listing2");
        Clickstream action = Clickstream
                .newBuilder()
                .setEventId(1)
                .setEventType("LV")
                .setEventDate(DateTime.now())
                .setListingId("listing2")
                .build();
        List<String> expected = Arrays.asList("listing1", "listing2");

        assertEquals(expected, app.calculateListings(initListings, action));
    }
}
