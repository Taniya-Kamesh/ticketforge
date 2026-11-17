package com.ticketforge.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PasswordHasherTest {
    @Test
    public void storedValueDoesNotContainThePlainPassword() {
        String stored = PasswordHasher.hash("password123");

        Assertions.assertFalse(stored.contains("password123"));
    }

    @Test
    public void correctPasswordMatches() {
        String stored = PasswordHasher.hash("password123");

        Assertions.assertTrue(PasswordHasher.matches("password123", stored));
    }

    @Test
    public void wrongPasswordDoesNotMatch() {
        String stored = PasswordHasher.hash("password123");

        Assertions.assertFalse(PasswordHasher.matches("wrong-password", stored));
    }

    @Test
    public void samePasswordGivesDifferentHashesBecauseOfTheSalt() {
        String first = PasswordHasher.hash("password123");
        String second = PasswordHasher.hash("password123");

        Assertions.assertNotEquals(first, second);
    }

    @Test
    public void textNotInOurFormatNeverMatches() {
        Assertions.assertFalse(PasswordHasher.matches("anything", "not-a-real-hash"));
    }
}
