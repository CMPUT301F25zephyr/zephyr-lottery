package com.example.zephyr_lottery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Event class, focusing on the sampleSize field.
 * This uses JUnit 5 (Jupiter API).
 */
public class EventTest {

    private Event event;

    @BeforeEach
    void setup() {
        // create a basic event for testing
        event = new Event("Swim Lessons", "5pm Mondays", "organizer@example.com");
    }

    @Test
    @DisplayName("Default sampleSize should be 0 when not set")
    void testDefaultSampleSizeIsZero() {
        assertEquals(0, event.getSampleSize(),
                "Default sampleSize should be initialized to 0");
    }

    @Test
    @DisplayName("Setting a positive sample size should store correctly")
    void testSetPositiveSampleSize() {
        event.setSampleSize(25);
        assertEquals(25, event.getSampleSize(),
                "Sample size should match the assigned positive value");
    }

    @Test
    @DisplayName("Setting a negative sample size should reset to 0")
    void testSetNegativeSampleSize() {
        event.setSampleSize(-5);
        assertEquals(0, event.getSampleSize(),
                "Sample size should never be negative; must reset to 0");
    }

    @Test
    @DisplayName("Two events with same name and time should be equal")
    void testEventsEquality() {
        Event another = new Event("Swim Lessons", "5pm Mondays", "organizer@example.com");
        assertEquals(event, another,
                "Events with the same name and time should be considered equal");
    }

    @Test
    @DisplayName("Events with different sample sizes should still be equal")
    void testSampleSizeDoesNotAffectEquality() {
        Event another = new Event("Swim Lessons", "5pm Mondays", "organizer@example.com");
        another.setSampleSize(10);
        assertEquals(event, another,
                "Changing sample size should not affect Event equality");
    }

    @Test
    @DisplayName("Event hashCode remains consistent after setting sampleSize")
    void testHashCodeStable() {
        int initialHash = event.hashCode();
        event.setSampleSize(50);
        assertEquals(initialHash, event.hashCode(),
                "Event hashCode should not depend on sampleSize field");
    }
}
