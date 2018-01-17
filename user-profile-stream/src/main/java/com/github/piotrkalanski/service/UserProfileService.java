package com.github.piotrkalanski.service;

import com.github.piotrkalanski.Clickstream;
import com.github.piotrkalanski.DeviceUsage;
import com.github.piotrkalanski.User;
import com.github.piotrkalanski.UserProfile;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class UserProfileService {

    public UserProfile emptyProfile() {
        return UserProfile
                .newBuilder()
                .setUserId("???")
                .build();
    }

    public UserProfile aggregateProfile(String userId, Clickstream action, UserProfile userProfile) {
        List<DeviceUsage> deviceUsage = calculateDeviceUsage(action, userProfile.getDeviceUsage());

        return UserProfile
                .newBuilder()
                .setUserId(userId)
                .setActionsCount(userProfile.getActionsCount() + 1)
                .setLastAction(action.getEventDate().getMillis())
                .setDeviceUsage(deviceUsage)
                .setFavouriteDevice(calculateFavouriteDevice(deviceUsage))
                .setListings(calculateListings(userProfile.getListings(), action))
                .build();
    }

    public List<DeviceUsage> calculateDeviceUsage(Clickstream action, List<DeviceUsage> oldDeviceUsage) {
        if(oldDeviceUsage.stream().allMatch(d -> !d.getDevice().equals(action.getDevice()))) {
            if(action.getDevice() != null) {
                oldDeviceUsage.add(
                        DeviceUsage
                                .newBuilder()
                                .setDevice(action.getDevice())
                                .setActionsCount(1L)
                                .build()
                );
            }
            return oldDeviceUsage;
        }
        else {
            return oldDeviceUsage
                    .stream()
                    .map(du -> {
                        if (du.getDevice().equals(action.getDevice())) {
                            du.setActionsCount(du.getActionsCount() + 1);
                        }
                        return du;
                    })
                    .collect(Collectors.toList());
        }
    }

    public CharSequence calculateFavouriteDevice(List<DeviceUsage> deviceUsage) {
        return deviceUsage.isEmpty() ? null : deviceUsage
                .stream()
                .max(Comparator.comparing(DeviceUsage::getActionsCount))
                .get()
                .getDevice();
    }

    public List<CharSequence> calculateListings(List<CharSequence> listings, Clickstream action) {
        if(!listings.stream().anyMatch(x -> x.equals(action.getListingId())))
            listings.add(action.getListingId());
        return listings;
    }

    public UserProfile mergeUserProfileWithUser(UserProfile userProfile, User user) {
        userProfile.setUserName(user.getUserName());
        userProfile.setUserTitle(user.getTitle());
        userProfile.setUserCity(user.getCity());
        userProfile.setUserGender(user.getGender());
        return userProfile;
    }
}
