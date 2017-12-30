package com.github.piotrkalanski;

import com.datawizards.kafka.streams.app.KafkaStreamsApplicationBase;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationMain extends KafkaStreamsApplicationBase {
    private static final String CLICKSTREAM_TOPIC = "raw-clickstream";
    private static final String OUTPUT_TOPIC = "user-profile-v1";

    public static void main(String[] args) {
        ApplicationMain app = new ApplicationMain();
        app.run();
    }

    protected void buildTopology(KStreamBuilder builder) {
        SpecificAvroSerde<UserProfile> userProfileSpecificAvroSerde = new SpecificAvroSerde<>();
        userProfileSpecificAvroSerde.configure(Collections.singletonMap(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"), false);

        KStream<String, clickstream> userActions = builder.stream(CLICKSTREAM_TOPIC);

        KTable<String, UserProfile> userProfile = userActions
                .groupByKey()
                .aggregate(
                        this::emptyProfile,
                        this::aggregateProfile,
                        userProfileSpecificAvroSerde
                );

        userProfile.to(OUTPUT_TOPIC);
    }

    private UserProfile emptyProfile() {
        return UserProfile
                .newBuilder()
                .setUserId("???")
                .build();
    }

    private UserProfile aggregateProfile(String userId, clickstream action, UserProfile userProfile) {
        List<DeviceUsage> deviceUsage = calculateDeviceUsage(action, userProfile.getDeviceUsage());

        return UserProfile
                .newBuilder()
                .setUserId(userId)
                .setActionsCount(userProfile.getActionsCount() + 1)
                .setLastAction(action.getEventDate())
                .setDeviceUsage(deviceUsage)
                .setFavouriteDevice(calculateFavouriteDevice(deviceUsage))
                .build();
    }

    protected List<DeviceUsage> calculateDeviceUsage(clickstream action, List<DeviceUsage> oldDeviceUsage) {
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

}