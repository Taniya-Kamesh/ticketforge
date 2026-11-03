package com.ticketforge.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class PasswordHasher {
      private static final int ITERATIONS=600000;
      private static final int SALT_BYTES=16;
      private static final int HASH_BITS=256;
      private PasswordHasher(){
      }
      public static String hash(String password){
            byte[] salt=new byte[SALT_BYTES];
            new SecureRandom().nextBytes(salt);//filling the salt with random bytes
            byte[] hash=pbkdf2(password,salt,ITERATIONS);
            return ITERATIONS+":"+ Base64.getEncoder().encodeToString(salt)+ ":"+
                    Base64.getEncoder().encodeToString(hash);

      }
      public static boolean matches(String password, String stored){
            String[] parts=stored.split(":");
            if(parts.length!=3) return false;
            int ITERATIONS=Integer.parseInt(parts[0]);
            byte[] salt=Base64.getDecoder().decode(parts[1]);
            byte[] expected=Base64.getDecoder().decode(parts[2]);

            byte[] actual=pbkdf2(password,salt,ITERATIONS);
            return MessageDigest.isEqual(actual,expected);
      }

      public static byte[] pbkdf2(String password,byte[] salt, int ITERATIONS){
            try{
                  PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, HASH_BITS);
                  SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                  return factory.generateSecret(spec).getEncoded();

            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                  throw new IllegalStateException("Password hashing failed", e);
            }
      }
}
