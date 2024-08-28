package com.opensky_puller;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.Future;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class OpenSkyQuery {

    private static final String KAFKA_BROKER = "kafka-service:9092";
    private static final String TOPIC = "aircraft-states";
    private static final String IS_DRY_RUN = System.getenv("IS_DRY_RUN");

    public static void main(String[] args) {

        // Check if the application is in dry-run mode
        boolean isDryRun = IS_DRY_RUN != null && IS_DRY_RUN.equalsIgnoreCase("true");

        // Kafka Producer Configuration (only if not in dry-run mode)
        Properties props = null;
        KafkaProducer<String, String> producer = null;
        if (!isDryRun) {
            props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            producer = new KafkaProducer<>(props);
        }

        try {
            // Perform the HTTP query and get the response
            JSONObject jsonObject = performHttpQuery();

            if (jsonObject != null) {
                JSONArray statesArray = jsonObject.getJSONArray("states");
                Long time = jsonObject.getLong("time");

                for (int i = 0; i < statesArray.length(); i++) {
                    JSONArray stateArray = statesArray.getJSONArray(i);
                    AircraftState aircraftState = new AircraftState(stateArray);

                    String key = aircraftState.getIcao24();
                    String message = aircraftState.toString();

                    // Print aircraft states in both modes
                    System.out.println("Time: " + time);
                    System.out.println("key: " + key);
                    System.out.println("message: " + message);

                    // If not in dry-run mode, send the message to Kafka
                    if (!isDryRun) {
                        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, key, message);
                        Future<RecordMetadata> future = producer.send(record);
                        RecordMetadata metadata = future.get();
                        System.out.printf("Produced record to topic %s partition %d with offset %d%n",
                            metadata.topic(), metadata.partition(), metadata.offset());
                    }
                }
            } else {
                System.out.println("Failed to fetch data.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the producer if it was instantiated
            if (producer != null) {
                producer.close();
            }
        }
    }

    private static JSONObject performHttpQuery() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        Double lamin = 42.5;
        Double lomin = -1.5;
        Double lamax = 45.0;
        Double lomax = 1.5;
        String url = String.format(
            "https://opensky-network.org/api/states/all?lamin=%.1f&lomin=%.1f&lamax=%.1f&lomax=%.1f", 
            lamin, lomin, lamax, lomax
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return new JSONObject(response.body());
        } else {
            System.out.println("Failed to fetch data. HTTP Status Code: " + response.statusCode());
            return null;
        }
    }
}
