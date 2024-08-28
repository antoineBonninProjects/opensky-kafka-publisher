import com.opensky.kafka.publisher.AircraftState;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AircraftStateTest {

    @Test
    public void testConstructorWithValidArray() {
        JSONArray stateArray = new JSONArray();
        stateArray.put("ICAO24");
        stateArray.put("CALLSIGN");
        stateArray.put("COUNTRY");
        stateArray.put(1628058760L); // lastUpdate
        stateArray.put(1628058760L); // firstSeen
        stateArray.put(45.0);        // latitude
        stateArray.put(-93.0);       // longitude
        stateArray.put(1500.0);      // barometricAltitude
        stateArray.put(true);        // onGround
        stateArray.put(550.0);       // velocity
        stateArray.put(180.0);       // trueTrack
        stateArray.put(-2.0);        // verticalRate
        stateArray.put("INFO");      // additionalInfo
        stateArray.put(2000.0);      // geometricAltitude
        stateArray.put("SQWK");      // squawkCode
        stateArray.put(false);       // emergency
        stateArray.put(0);           // reserved

        AircraftState aircraftState = new AircraftState(stateArray);

        assertEquals("ICAO24", aircraftState.getIcao24());
        assertEquals("CALLSIGN", aircraftState.getCallsign());
        assertEquals("COUNTRY", aircraftState.getOriginCountry());
        assertEquals(1628058760L, aircraftState.getLastUpdate());
        assertEquals(1628058760L, aircraftState.getFirstSeen());
        assertEquals(45.0, aircraftState.getLatitude());
        assertEquals(-93.0, aircraftState.getLongitude());
        assertEquals(1500.0, aircraftState.getBarometricAltitude());
        assertTrue(aircraftState.getOnGround());
        assertEquals(550.0, aircraftState.getVelocity());
        assertEquals(180.0, aircraftState.getTrueTrack());
        assertEquals(-2.0, aircraftState.getVerticalRate());
        assertEquals("INFO", aircraftState.getAdditionalInfo());
        assertEquals(2000.0, aircraftState.getGeometricAltitude());
        assertEquals("SQWK", aircraftState.getSquawkCode());
        assertFalse(aircraftState.getEmergency());
        assertEquals(0, aircraftState.getReserved());
    }

    @Test
    public void testConstructorWithNullValues() {
        JSONArray stateArray = new JSONArray();
        stateArray.put("ICAO24");
        stateArray.put("CALLSIGN");
        stateArray.put("COUNTRY");
        stateArray.put(1628058760L);     // lastUpdate
        stateArray.put(1628058760L);     // firstSeen
        stateArray.put(45.0);            // latitude
        stateArray.put(-93.0);           // longitude
        stateArray.put(JSONObject.NULL); // barometricAltitude
        stateArray.put(true);            // onGround
        stateArray.put(550.0);           // velocity
        stateArray.put(180.0);           // trueTrack
        stateArray.put(JSONObject.NULL); // verticalRate
        stateArray.put(JSONObject.NULL); // additionalInfo
        stateArray.put(JSONObject.NULL); // geometricAltitude
        stateArray.put(JSONObject.NULL); // squawkCode
        stateArray.put(false);           // emergency
        stateArray.put(0);               // reserved


        AircraftState aircraftState = new AircraftState(stateArray);

        assertEquals("ICAO24", aircraftState.getIcao24());
        assertEquals("CALLSIGN", aircraftState.getCallsign());
        assertEquals("COUNTRY", aircraftState.getOriginCountry());
        assertEquals(1628058760L, aircraftState.getLastUpdate());
        assertEquals(1628058760L, aircraftState.getFirstSeen());
        assertEquals(45.0, aircraftState.getLatitude());
        assertEquals(-93.0, aircraftState.getLongitude());
        assertEquals(-1.0, aircraftState.getBarometricAltitude()); // Default value for null
        assertTrue(aircraftState.getOnGround());
        assertEquals(550.0, aircraftState.getVelocity());
        assertEquals(180.0, aircraftState.getTrueTrack());
        assertEquals(-1.0, aircraftState.getVerticalRate()); // Default value for null
        assertEquals("N/A", aircraftState.getAdditionalInfo()); // Default value for null
        assertEquals(-1.0, aircraftState.getGeometricAltitude()); // Default value for null
        assertEquals("N/A", aircraftState.getSquawkCode()); // Default value for null
        assertFalse(aircraftState.getEmergency());
        assertEquals(0, aircraftState.getReserved());
    }

    @Test
    public void testToString() {
        JSONArray stateArray = new JSONArray()
            .put("ICAO24")
            .put("CALLSIGN")
            .put("COUNTRY")
            .put(1628058760L)
            .put(1628058760L)
            .put(45.0)
            .put(-93.0)
            .put(1500.0)
            .put(true)
            .put(550.0)
            .put(180.0)
            .put(-2.0)
            .put("INFO")
            .put(2000.0)
            .put("SQWK")
            .put(false)
            .put(0);

        AircraftState aircraftState = new AircraftState(stateArray);

        String result = aircraftState.toString();

        assertTrue(result.contains("icao24: ICAO24"));
        assertTrue(result.contains("callsign: CALLSIGN"));
        assertTrue(result.contains("originCountry: COUNTRY"));
        assertTrue(result.contains("lastUpdate: 1628058760"));
        assertTrue(result.contains("firstSeen: 1628058760"));
        assertTrue(result.contains("latitude: 45.0"));
        assertTrue(result.contains("longitude: -93.0"));
        assertTrue(result.contains("barometricAltitude: 1500.0"));
        assertTrue(result.contains("onGround: true"));
        assertTrue(result.contains("velocity: 550.0"));
        assertTrue(result.contains("trueTrack: 180.0"));
        assertTrue(result.contains("verticalRate: -2.0"));
        assertTrue(result.contains("additionalInfo: INFO"));
        assertTrue(result.contains("geometricAltitude: 2000.0"));
        assertTrue(result.contains("squawkCode: SQWK"));
        assertTrue(result.contains("emergency: false"));
        assertTrue(result.contains("reserved: 0"));
    }
}
