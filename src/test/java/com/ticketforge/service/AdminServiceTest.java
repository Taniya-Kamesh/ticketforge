package com.ticketforge.service;

import com.ticketforge.dao.EventDAO;
import com.ticketforge.dao.SeatDAO;
import com.ticketforge.dao.UserDAO;
import com.ticketforge.exception.NotAuthorizedException;
import com.ticketforge.exception.ValidationException;
import com.ticketforge.model.*;
import com.ticketforge.util.DbTestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

public class AdminServiceTest {
    private final AdminService adminService = new AdminService();
    private final UserDAO userDAO = new UserDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final SeatDAO seatDAO = new SeatDAO();

    @BeforeEach
    public void cleanDatabase() throws Exception {
        DbTestHelper.clean();
    }

    // Helper: saves a user directly. The hash text doesn't matter here, so we skip the slow hashing.
    private User saveUser(String email, Role role) throws Exception {
        User user = new User("Test", email, "x", role);
        return userDAO.insert(user);
    }

    @Test
    public void adminCreatesAnEventWithEverySeat() throws Exception {
        User admin = saveUser("admin@example.com", Role.ADMIN);

        Event event = adminService.createEventWithSeats(admin, "Concert", "Main Hall",
                LocalDateTime.now().plusDays(30), 3, 5);

        List<Seat> seats = seatDAO.findByEvent(event.getId());
        Assertions.assertEquals(15, seats.size());
        Assertions.assertEquals("A1", seats.get(0).getSeatLabel());
        Assertions.assertEquals("C5", seats.get(14).getSeatLabel());
        for (Seat seat : seats) {
            Assertions.assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        }
    }

    @Test
    public void aNormalUserCannotCreateEventsAndNothingIsSaved() throws Exception {
        User normal = saveUser("normal@example.com", Role.USER);

        try {
            adminService.createEventWithSeats(normal, "Sneaky", "Nowhere",
                    LocalDateTime.now().plusDays(1), 1, 1);
            Assertions.fail("Expected NotAuthorizedException");
        } catch (NotAuthorizedException e) {
            // expected
        }

        Assertions.assertEquals(0, eventDAO.findAll().size());
    }

    @Test
    public void anEventInThePastIsRefused() throws Exception {
        User admin = saveUser("admin@example.com", Role.ADMIN);

        try {
            adminService.createEventWithSeats(admin, "Old", "Hall",
                    LocalDateTime.now().minusDays(1), 1, 1);
            Assertions.fail("Expected ValidationException");
        } catch (ValidationException e) {
            // expected
        }

        Assertions.assertEquals(0, eventDAO.findAll().size());
    }

    @Test
    public void tooManyRowsIsRefused() throws Exception {
        User admin = saveUser("admin@example.com", Role.ADMIN);

        try {
            adminService.createEventWithSeats(admin, "Huge", "Hall",
                    LocalDateTime.now().plusDays(1), 27, 5);
            Assertions.fail("Expected ValidationException");
        } catch (ValidationException e) {
            // expected
        }
    }
}
