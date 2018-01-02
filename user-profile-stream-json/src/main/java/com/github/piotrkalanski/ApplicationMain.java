package com.github.piotrkalanski;

import com.datawizards.kafka.streams.app.KafkaStreamsApplicationBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;

public class ApplicationMain extends KafkaStreamsApplicationBase {
    private static final String CLICKSTREAM_TOPIC = "json-clickstream";
    private static final String OUTPUT_TOPIC = "user-profile-json-v01";

    public static void main(String[] args) {
        ApplicationMain app = new ApplicationMain();
        app.run();
    }

    protected void buildTopology(KStreamBuilder builder) {
        // json Serde
        final Serializer<JsonNode> jsonSerializer = new JsonSerializer();
        final Deserializer<JsonNode> jsonDeserializer = new JsonDeserializer();
        final Serde<JsonNode> jsonSerde = Serdes.serdeFrom(jsonSerializer, jsonDeserializer);

        KStream<String, JsonNode> clickstream = builder.stream(Serdes.String(), jsonSerde, CLICKSTREAM_TOPIC);
        KStream<String, JsonNode> clickstreamByUser = clickstream.selectKey((k,v) -> v.get("user_id").asText());

        KTable<String, JsonNode> userProfile = clickstreamByUser
                .groupByKey(Serdes.String(), jsonSerde)
                .aggregate(
                        this::emptyProfile,
                        this::aggregateProfile,
                        jsonSerde
                );

        userProfile.to(Serdes.String(), jsonSerde, OUTPUT_TOPIC);
    }

    private JsonNode emptyProfile() {
        ObjectNode emptyProfile = JsonNodeFactory.instance.objectNode();
        emptyProfile.put("user_id", "null");
        emptyProfile.put("actions_count", 0);
        return emptyProfile;
    }

    private JsonNode aggregateProfile(String userId, JsonNode action, JsonNode userProfile) {
        ObjectNode resultProfile = JsonNodeFactory.instance.objectNode();

        resultProfile.put("user_id", userId);
        resultProfile.put("actions_count", userProfile.get("actions_count").asInt() + 1);
        resultProfile.put("last_action", action.get("event_date").asLong());

        return resultProfile;
    }
}