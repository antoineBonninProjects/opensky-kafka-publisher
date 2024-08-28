package com.opensky.kafka.publisher;

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

public class OpenSkyKafkaPublisher {

    private static final String KAFKA_BROKER = System.getenv("KAFKA_BROKER");
    private static final String KAFKA_TOPIC = System.getenv("KAFKA_TOPIC");
    private static final String IS_DRY_RUN = System.getenv("IS_DRY_RUN");

    private static final String HTTP_PATH = "https://opensky-network.org/api/states/all";

    private static final String LATITUDE_MIN = System.getenv("LATITUDE_MIN");
    private static final String LATITUDE_MAX = System.getenv("LATITUDE_MAX");
    private static final String LONGITUDE_MIN = System.getenv("LONGITUDE_MIN");
    private static final String LONGITUDE_MAX = System.getenv("LONGITUDE_MAX");

    // Default area to scan is the south west of France
    private static final Double DEFAULT_LATITUDE_MIN = 42.5;
    private static final Double DEFAULT_LATITUDE_MAX = 45.0;
    private static final Double DEFAULT_LONGITUDE_MIN = -1.5;
    private static final Double DEFAULT_LONGITUDE_MAX = 1.5;

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
            // Scan for aircrafts and handle the response
            JSONObject jsonObject = performHttpQuery();

            if (jsonObject == null) {
                System.out.println("Failed to fetch data.");
            } else if (!jsonObject.has("states") || jsonObject.isNull("states")) {
                System.out.println("No aircrafts in the scanned zone.");
            } else  {
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
                        ProducerRecord<String, String> record = new ProducerRecord<>(KAFKA_TOPIC, key, message);
                        Future<RecordMetadata> future = producer.send(record);
                        RecordMetadata metadata = future.get();
                        System.out.printf("Produced record to topic %s partition %d with offset %d%n",
                            metadata.topic(), metadata.partition(), metadata.offset());
                    }
                }
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

        Double lamin = (LATITUDE_MIN != null) ? Double.parseDouble(LATITUDE_MIN) : DEFAULT_LATITUDE_MIN;
        Double lomin = (LONGITUDE_MIN) != null ? Double.parseDouble(LONGITUDE_MIN) : DEFAULT_LONGITUDE_MIN;
        Double lamax = (LATITUDE_MAX) != null ? Double.parseDouble(LATITUDE_MAX) : DEFAULT_LATITUDE_MAX;
        Double lomax = (LONGITUDE_MAX) != null ? Double.parseDouble(LONGITUDE_MAX) : DEFAULT_LONGITUDE_MAX;

        System.out.println("Area to scan is: ");
        System.out.println("\tLATITUDE_MIN: " + lamin);
        System.out.println("\tLATITUDE_MAX: " + lamax);
        System.out.println("\tLONGITUDE_MIN: " + lomin);
        System.out.println("\tLONGITUDE_MAX: " + lomax);

        String url = String.format(
            "%s?lamin=%.1f&lomin=%.1f&lamax=%.1f&lomax=%.1f", 
            HTTP_PATH, lamin, lomin, lamax, lomax
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