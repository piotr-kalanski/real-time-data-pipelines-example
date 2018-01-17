package com.github.piotrkalanski.service;

import com.github.piotrkalanski.*;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UserProfileServiceTest {

    private UserProfileService userProfileService = new UserProfileService();

    @Test
    public void testCalculateFavouriteDevice() {
        List<DeviceUsage> devices = Arrays.asList(
                DeviceUsage.newBuilder().setDevice("DESKTOP").setActionsCount(2L).build(),
                DeviceUsage.newBuilder().setDevice("MOBILE").setActionsCount(1L).build()
        );

        assertEquals("DESKTOP", userProfileService.calculateFavouriteDevice(devices));
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

        assertEquals(expectedDevices, userProfileService.calculateDeviceUsage(action, oldDevices));
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

        assertEquals(expectedDevices, userProfileService.calculateDeviceUsage(action, oldDevices));
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

        assertEquals(expectedDevices, userProfileService.calculateDeviceUsage(action, oldDevices));
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

        assertEquals(expectedDevices, userProfileService.calculateDeviceUsage(action, oldDevices));
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

        assertEquals(expected, userProfileService.calculateListings(initListings, action));
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

        assertEquals(expected, userProfileService.calculateListings(initListings, action));
    }

    @Test
    public void testMergeUserProfileWithUser() {
        UserProfile userProfile = UserProfile
                .newBuilder()
                .setUserId("???")
                .build();
        User user = User
                .newBuilder()
                .setUserId(1)
                .setUserName("name")
                .setGender("gender")
                .setTitle("title")
                .setCity("city")
                .build();

        userProfileService.mergeUserProfileWithUser(userProfile, user);

        assertEquals(user.getUserName(), userProfile.getUserName());
        assertEquals(user.getCity(), userProfile.getUserCity());
        assertEquals(user.getTitle(), userProfile.getUserTitle());
        assertEquals(user.getGender(), userProfile.getUserGender());
    }
}
