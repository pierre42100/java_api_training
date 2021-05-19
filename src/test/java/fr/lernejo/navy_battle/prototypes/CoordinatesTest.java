package fr.lernejo.navy_battle.prototypes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoordinatesTest {

    @Test
    void testFromString() {
        var coordinates = new Coordinates("C7");
        assertEquals(6, coordinates.getY());
        assertEquals(2, coordinates.getX());
    }

    @Test
    void testToString() {
        var coordinates = new Coordinates(2, 6);
        assertEquals("C7", coordinates.toString());
    }
}
