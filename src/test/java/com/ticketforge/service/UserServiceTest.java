package com.ticketforge.service;

import com.ticketforge.exception.EmailAlreadyUsedException;
import com.ticketforge.exception.InvalidCredentialsException;
import com.ticketforge.exception.ValidationException;
import com.ticketforge.model.Role;
import com.ticketforge.model.User;
import com.ticketforge.util.DbTestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserServiceTest {
    private final UserService userService = new UserService();

    // Runs before EVERY test method, so each test starts with empty tables
    @BeforeEach
    public void cleanDatabase() throws Exception {
        DbTestHelper.clean();
    }

    @Test
    public void registerSavesAHashAndMakesARegularUser() throws Exception {
        User user = userService.register("Asha", "asha@example.com", "password123");

        Assertions.assertTrue(user.getId() > 0);
        Assertions.assertEquals(Role.USER, user.getRole());
        Assertions.assertNotEquals("password123", user.getPasswordHash());
    }

    @Test
    public void registeringTheSameEmailTwiceIsRefused() throws Exception {
        userService.register("Asha", "asha@example.com", "password123");

        try {
            userService.register("Someone Else", "asha@example.com", "password123");
            Assertions.fail("Expected EmailAlreadyUsedException");
        } catch (EmailAlreadyUsedException e) {
            // expected: the test passes
        }
    }

    @Test
    public void aTooShortPasswordIsRefused() throws Exception {
        try {
            userService.register("Asha", "asha@example.com", "123");
            Assertions.fail("Expected ValidationException");
        } catch (ValidationException e) {
            // expected
        }
    }

    @Test
    public void anEmailWithoutAtSignIsRefused() throws Exception {
        try {
            userService.register("Asha", "not-an-email", "password123");
            Assertions.fail("Expected ValidationException");
        } catch (ValidationException e) {
            // expected
        }
    }

    @Test
    public void loginWithTheRightPasswordWorks() throws Exception {
        User registered = userService.register("Asha", "asha@example.com", "password123");

        User loggedIn = userService.login("asha@example.com", "password123");

        Assertions.assertEquals(registered.getId(), loggedIn.getId());
    }

    @Test
    public void loginIgnoresUpperAndLowerCaseInTheEmail() throws Exception {
        User registered = userService.register("Asha", "asha@example.com", "password123");

        User loggedIn = userService.login("ASHA@Example.COM", "password123");

        Assertions.assertEquals(registered.getId(), loggedIn.getId());
    }

    @Test
    public void loginWithAWrongPasswordIsRefused() throws Exception {
        userService.register("Asha", "asha@example.com", "password123");

        try {
            userService.login("asha@example.com", "wrong-password");
            Assertions.fail("Expected InvalidCredentialsException");
        } catch (InvalidCredentialsException e) {
            // expected
        }
    }

    @Test
    public void loginWithAnUnknownEmailGivesTheSameError() throws Exception {
        try {
            userService.login("nobody@example.com", "password123");
            Assertions.fail("Expected InvalidCredentialsException");
        } catch (InvalidCredentialsException e) {
            Assertions.assertEquals("Invalid email or password", e.getMessage());
        }
    }

}
