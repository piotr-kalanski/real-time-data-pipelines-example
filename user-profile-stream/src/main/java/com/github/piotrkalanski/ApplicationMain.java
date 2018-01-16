package com.github.piotrkalanski;

import com.datawizards.kafka.streams.app.KafkaStreamsApplicationBase;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationMain extends KafkaStreamsApplicationBase {
    private static final String CLICKSTREAM_TOPIC = "raw-avro-clickstream";
    private static final String USERS_TOPIC = "raw-avro-users";
    private static final String OUTPUT_TOPIC = "user-profile-v01";

    public static void main(String[] args) {
        ApplicationMain app = new ApplicationMain();
        app.run();
    }

    protected void buildTopology(KStreamBuilder builder) {
        SpecificAvroSerde<UserProfile> userProfileSpecificAvroSerde = new SpecificAvroSerde<>();
        userProfileSpecificAvroSerde.configure(Collections.singletonMap(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"), false);

        SpecificAvroSerde<Clickstream> clickstreamSpecificAvroSerde = new SpecificAvroSerde<>();
        clickstreamSpecificAvroSerde.configure(Collections.singletonMap(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"), false);

        SpecificAvroSerde<User> usersSpecificAvroSerde = new SpecificAvroSerde<>();
        usersSpecificAvroSerde.configure(Collections.singletonMap(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"), false);

        KStream<String, Clickstream> userActions = builder.stream(Serdes.String(), clickstreamSpecificAvroSerde, CLICKSTREAM_TOPIC);
        KTable<String, User> users = builder.table(Serdes.String(), usersSpecificAvroSerde, USERS_TOPIC);

        KTable<String, UserProfile> usersProfiles = userActions
                .groupByKey(Serdes.String(), clickstreamSpecificAvroSerde)
                .aggregate(
                        this::emptyProfile,
                        this::aggregateProfile,
                        userProfileSpecificAvroSerde
                );

        usersProfiles.join(users, this::mergeUserProfileWithUser);

        usersProfiles.to(Serdes.String(), userProfileSpecificAvroSerde, OUTPUT_TOPIC);
    }

    private UserProfile emptyProfile() {
        return UserProfile
                .newBuilder()
                .setUserId("???")
                .build();
    }

    private UserProfile aggregateProfile(String userId, Clickstream action, UserProfile userProfile) {
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

    protected List<DeviceUsage> calculateDeviceUsage(Clickstream action, List<DeviceUsage> oldDeviceUsage) {
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

    protected CharSequence calculateFavouriteDevice(List<DeviceUsage> deviceUsage) {
        return deviceUsage.isEmpty() ? null : deviceUsage
                .stream()
                .max(Comparator.comparing(DeviceUsage::getActionsCount))
                .get()
                .getDevice();
    }

    protected List<CharSequence> calculateListings(List<CharSequence> listings, Clickstream action) {
        if(!listings.stream().anyMatch(x -> x.equals(action.getListingId())))
            listings.add(action.getListingId());
        return listings;
    }

    protected UserProfile mergeUserProfileWithUser(UserProfile userProfile, User user) {
        userProfile.setUserName(user.getUserName());
        userProfile.setUserTitle(user.getTitle());
        userProfile.setUserCity(user.getCity());
        userProfile.setUserGender(user.getGender());
        return userProfile;
    }
}