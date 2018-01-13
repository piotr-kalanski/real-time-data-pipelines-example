package com.github.piotrkalanski.service;

import com.github.piotrkalanski.*;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
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

        assertEquals(expected, userProfileService.calculateVisitedListings(initListings, action));
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

        assertEquals(expected, userProfileService.calculateVisitedListings(initListings, action));
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

    @Test
    public void testUpdateItemScoreWithNewItem() {
        List<ItemWithScore> itemWithScores = new LinkedList<>();
        itemWithScores.add( ItemWithScore.newBuilder().setItemValue("item1").setItemScore(1.0).build() );
        itemWithScores.add( ItemWithScore.newBuilder().setItemValue("item2").setItemScore(2.0).build() );

        List<ItemWithScore> expectedItemWithScores = Arrays.asList(
                ItemWithScore.newBuilder().setItemValue("item1").setItemScore(1.0).build(),
                ItemWithScore.newBuilder().setItemValue("item2").setItemScore(2.0).build(),
                ItemWithScore.newBuilder().setItemValue("item3").setItemScore(1.0).build()
        );

        assertEquals(expectedItemWithScores, userProfileService.updateItemsScores(itemWithScores, "item3"));
    }

    @Test
    public void testUpdateItemScoreWithExistingItem() {
        List<ItemWithScore> itemWithScores = new LinkedList<>();
        itemWithScores.add( ItemWithScore.newBuilder().setItemValue("item1").setItemScore(1.0).build() );
        itemWithScores.add( ItemWithScore.newBuilder().setItemValue("item2").setItemScore(2.0).build() );

        List<ItemWithScore> expectedItemWithScores = Arrays.asList(
                ItemWithScore.newBuilder().setItemValue("item1").setItemScore(2.0).build(),
                ItemWithScore.newBuilder().setItemValue("item2").setItemScore(2.0).build()
        );

        assertEquals(expectedItemWithScores, userProfileService.updateItemsScores(itemWithScores, "item1"));
    }

    @Test
    public void testUpdateUserProfileWithNewClickstreamEvent() {
        Long date1 = new Date().getTime();
        DateTime dateTime2 = DateTime.now();
        Long date2 = dateTime2.getMillis();

        UserProfile initUserProfile = UserProfile
                .newBuilder()
                .setUserId("1")
                .setActionsCount(1)
                .setLastAction(date1)
                .setDeviceUsage(
                        Arrays.asList(
                                DeviceUsage.newBuilder().setDevice("DESKTOP").setActionsCount(2L).build(),
                                DeviceUsage.newBuilder().setDevice("MOBILE").setActionsCount(2L).build()
                        )
                )
                .setFavouriteDevice("DESKTOP")
                .setListings(Arrays.asList("listing1", "listing2"))
                .setJobTitlesScores(
                        Arrays.asList(
                                ItemWithScore.newBuilder().setItemValue("jt1").setItemScore(1.0).build(),
                                ItemWithScore.newBuilder().setItemValue("jt2").setItemScore(2.0).build()
                        )
                )
                .setDisciplinesScores(
                        Arrays.asList(
                                ItemWithScore.newBuilder().setItemValue("disc1").setItemScore(2.0).build(),
                                ItemWithScore.newBuilder().setItemValue("disc2").setItemScore(1.0).build()
                        )
                )
                .setCitiesScores(
                        Arrays.asList(
                                ItemWithScore.newBuilder().setItemValue("city1").setItemScore(2.0).build(),
                                ItemWithScore.newBuilder().setItemValue("city2").setItemScore(3.0).build()
                        )
                )
                .build();

        EnrichedClickstream event = EnrichedClickstream.newBuilder()
                .setClickstreamEvent(
                        Clickstream
                                .newBuilder()
                                .setEventId(1)
                                .setEventType("LV")
                                .setEventDate(dateTime2)
                                .setListingId("listing3")
                                .setDevice("MOBILE")
                                .build()
                )
                .setListingJobTitle("jt1")
                .setListingDiscipline("disc2")
                .setListingCity("city3")
                .build();

        UserProfile expectedUserProfile = UserProfile
                .newBuilder()
                .setUserId("1")
                .setActionsCount(2)
                .setLastAction(date2)
                .setDeviceUsage(
                        Arrays.asList(
                                DeviceUsage.newBuilder().setDevice("DESKTOP").setActionsCount(2L).build(),
                                DeviceUsage.newBuilder().setDevice("MOBILE").setActionsCount(3L).build()
                        )
                )
                .setFavouriteDevice("MOBILE")
                .setListings(Arrays.asList("listing1", "listing2", "listing3"))
                .setJobTitlesScores(
                        Arrays.asList(
                                ItemWithScore.newBuilder().setItemValue("jt1").setItemScore(2.0).build(),
                                ItemWithScore.newBuilder().setItemValue("jt2").setItemScore(2.0).build()
                        )
                )
                .setDisciplinesScores(
                        Arrays.asList(
                                ItemWithScore.newBuilder().setItemValue("disc1").setItemScore(2.0).build(),
                                ItemWithScore.newBuilder().setItemValue("disc2").setItemScore(2.0).build()
                        )
                )
                .setCitiesScores(
                        Arrays.asList(
                                ItemWithScore.newBuilder().setItemValue("city1").setItemScore(2.0).build(),
                                ItemWithScore.newBuilder().setItemValue("city2").setItemScore(3.0).build(),
                                ItemWithScore.newBuilder().setItemValue("city3").setItemScore(1.0).build()
                        )
                )
                .build();

        assertEquals(expectedUserProfile, userProfileService.updateUserProfileWithNewClickstreamEvent("1", event, initUserProfile));
    }
}
