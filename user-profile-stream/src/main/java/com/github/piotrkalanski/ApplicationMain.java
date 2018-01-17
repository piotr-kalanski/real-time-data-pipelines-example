package com.github.piotrkalanski;

import com.datawizards.kafka.streams.app.KafkaStreamsApplicationBase;
import com.github.piotrkalanski.service.UserProfileService;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import java.util.Collections;

public class ApplicationMain extends KafkaStreamsApplicationBase {
    private static final String CLICKSTREAM_TOPIC = "raw-avro-clickstream";
    private static final String USERS_TOPIC = "raw-avro-users";
    private static final String LISTINGS_TOPIC = "raw-avro-listings";
    private static final String OUTPUT_TOPIC = "user-profile-v01";

    private UserProfileService userProfileService = new UserProfileService();

    private SpecificAvroSerde<UserProfile> userProfileSpecificAvroSerde;
    private SpecificAvroSerde<Clickstream> clickstreamSpecificAvroSerde;
    private SpecificAvroSerde<User> usersSpecificAvroSerde;
    private SpecificAvroSerde<Listing> listingsSpecificAvroSerde;

    private KStream<String, Clickstream> userActions;
    private KTable<String, User> users;
    private GlobalKTable<String, Listing> listings;

    public static void main(String[] args) {
        ApplicationMain app = new ApplicationMain();
        app.run();
    }

    protected void buildTopology(KStreamBuilder builder) {
        initSerdes();
        readFromKafka(builder);
        KTable<String, UserProfile> userProfiles = aggregateClickstream();
        userProfiles = enrichWithUserData(userProfiles);
        writeResult(userProfiles);
    }

    private void initSerdes() {
        userProfileSpecificAvroSerde = new SpecificAvroSerde<>();
        userProfileSpecificAvroSerde.configure(Collections.singletonMap(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"), false);

        clickstreamSpecificAvroSerde = new SpecificAvroSerde<>();
        clickstreamSpecificAvroSerde.configure(Collections.singletonMap(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"), false);

        usersSpecificAvroSerde = new SpecificAvroSerde<>();
        usersSpecificAvroSerde.configure(Collections.singletonMap(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"), false);

        listingsSpecificAvroSerde = new SpecificAvroSerde<>();
        listingsSpecificAvroSerde.configure(Collections.singletonMap(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"), false);
    }

    private void readFromKafka(KStreamBuilder builder) {
        userActions = builder.stream(Serdes.String(), clickstreamSpecificAvroSerde, CLICKSTREAM_TOPIC);
        users = builder.table(Serdes.String(), usersSpecificAvroSerde, USERS_TOPIC);
        listings = builder.globalTable(Serdes.String(), listingsSpecificAvroSerde, LISTINGS_TOPIC);
    }

    private KTable<String, UserProfile> aggregateClickstream() {
        return userActions
                .groupByKey(Serdes.String(), clickstreamSpecificAvroSerde)
                .aggregate(
                        userProfileService::emptyProfile,
                        userProfileService::aggregateProfile,
                        userProfileSpecificAvroSerde
                );
    }

    private KTable<String, UserProfile> enrichWithUserData(KTable<String, UserProfile> usersProfiles) {
        return usersProfiles.join(users, userProfileService::mergeUserProfileWithUser);
    }

    private void writeResult(KTable<String, UserProfile> usersProfiles) {
        usersProfiles.to(Serdes.String(), userProfileSpecificAvroSerde, OUTPUT_TOPIC);
    }

}
