package com.ticketforge.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PasswordHasherTest {
   @Test
    public void storedValueDoesNotContainPlainPassword(){
     String stored=PasswordHasher.hash("password123");
     Assertions.assertFalse(stored.contains("password123"));
   }

   @Test
    public void correctPasswordMatches(){
       String stored=PasswordHasher.hash("password123");
       Assertions.assertTrue(PasswordHasher.matches("password123",stored));
   }

   @Test
    public void wrongPasswordDoesNotMatch(){
       String stored=PasswordHasher.hash("password123");
       Assertions.assertFalse(PasswordHasher.matches("wrongpassword",stored));
   }

   @Test
    public void notMatchingBecozOfDiffHash(){
       String first=PasswordHasher.hash("password123");
       String sec=PasswordHasher.hash("password123");
       Assertions.assertNotEquals(first,sec);
   }

   @Test
    public void wrongPasswordFormat(){
       Assertions.assertFalse(PasswordHasher.matches("password123","not-real-hash"));
   }
}
