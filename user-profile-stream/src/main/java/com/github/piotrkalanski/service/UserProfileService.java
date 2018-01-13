package com.github.piotrkalanski.service;

import com.github.piotrkalanski.*;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class UserProfileService {

    public UserProfile emptyProfile() {
        return UserProfile
                .newBuilder()
                .setUserId("???")
                .build();
    }

    public UserProfile updateUserProfileWithNewClickstreamEvent(String userId, EnrichedClickstream action, UserProfile userProfile) {
        List<DeviceUsage> deviceUsage = calculateDeviceUsage(action.getClickstreamEvent(), userProfile.getDeviceUsage());
        List<CharSequence> listings = calculateVisitedListings(userProfile.getListings(), action.getClickstreamEvent());

        return UserProfile
                .newBuilder()
                .setUserId(userId)
                .setActionsCount(userProfile.getActionsCount() + 1)
                .setLastAction(action.getClickstreamEvent().getEventDate().getMillis())
                .setDeviceUsage(deviceUsage)
                .setFavouriteDevice(calculateFavouriteDevice(deviceUsage))
                .setListings(listings)
                .setJobTitlesScores(updateItemsScores(userProfile.getJobTitlesScores(), action.getListingJobTitle()))
                .setDisciplinesScores(updateItemsScores(userProfile.getDisciplinesScores(), action.getListingDiscipline()))
                .setCitiesScores(updateItemsScores(userProfile.getCitiesScores(), action.getListingCity()))
                .build();
    }

    public List<DeviceUsage> calculateDeviceUsage(Clickstream action, List<DeviceUsage> oldDeviceUsage) {
        List<DeviceUsage> result = new LinkedList<>();
        result.addAll(oldDeviceUsage);

        if(oldDeviceUsage.stream().noneMatch(d -> d.getDevice().equals(action.getDevice()))) {
            if(action.getDevice() != null) {
                result.add(
                        DeviceUsage
                                .newBuilder()
                                .setDevice(action.getDevice())
                                .setActionsCount(1L)
                                .build()
                );
            }
        }
        else {
            result = result
                .stream()
                .peek(du -> {
                    if (du.getDevice().equals(action.getDevice())) {
                        du.setActionsCount(du.getActionsCount() + 1);
                    }
                })
                .collect(Collectors.toList());
        }

        return result;
    }

    public CharSequence calculateFavouriteDevice(List<DeviceUsage> deviceUsage) {
        return deviceUsage.isEmpty() ? null : deviceUsage
                .stream()
                .max(Comparator.comparing(DeviceUsage::getActionsCount))
                .get()
                .getDevice();
    }

    public List<CharSequence> calculateVisitedListings(List<CharSequence> listings, Clickstream action) {
        List<CharSequence> result = listings;
        if(!listings.stream().anyMatch(x -> x.equals(action.getListingId()))) {
            result = new LinkedList<>();
            result.addAll(listings);
            result.add(action.getListingId());
        }

        return result;
    }

    public UserProfile mergeUserProfileWithUser(UserProfile userProfile, User user) {
        userProfile.setUserName(user.getUserName());
        userProfile.setUserTitle(user.getTitle());
        userProfile.setUserCity(user.getCity());
        userProfile.setUserGender(user.getGender());
        return userProfile;
    }

    public EnrichedClickstream enrichClickstreamWithListing(Clickstream clickstream, Listing listing) {
        return EnrichedClickstream.newBuilder()
                .setClickstreamEvent(clickstream)
                .setListingCity(listing.getCity())
                .setListingJobTitle(listing.getJobTitle())
                .setListingDiscipline(listing.getDiscipline())
                .build();
    }

    public List<ItemWithScore> updateItemsScores(List<ItemWithScore> itemWithScoreList, CharSequence element) {
        List<ItemWithScore> result = new LinkedList<>();
        result.addAll(itemWithScoreList);

        if(element != null) {
            result = new LinkedList<>();
            result.addAll(itemWithScoreList);

            if(itemWithScoreList.stream().noneMatch(itemWithScore -> itemWithScore.getItemValue().equals(element))) {
                result.add(
                  ItemWithScore
                          .newBuilder()
                          .setItemValue(element)
                          .setItemScore(1.0)
                          .build()
                );
            }
            else {
                result = result
                    .stream()
                    .peek(itemWithScore -> {
                        if (itemWithScore.getItemValue().equals(element)) {
                            itemWithScore.setItemScore(itemWithScore.getItemScore() + 1.0);
                        }
                    })
                    .collect(Collectors.toList());;
            }
        }

        return result;
    }

}
