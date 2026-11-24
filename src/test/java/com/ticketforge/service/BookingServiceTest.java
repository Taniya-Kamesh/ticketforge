package com.ticketforge.service;

import com.ticketforge.dao.BookingDAO;
import com.ticketforge.dao.SeatDAO;
import com.ticketforge.dao.UserDAO;
import com.ticketforge.model.*;
import com.ticketforge.util.DbTestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingServiceTest {
    private final BookingService bookingService = new BookingService();
    private final AdminService adminService = new AdminService();
    private final UserDAO userDAO = new UserDAO();
    private final SeatDAO seatDAO = new SeatDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    @BeforeEach
    public void cleanDatabase() throws Exception {
        DbTestHelper.clean();
    }

    // ---------- helpers ----------

    private User newUser(String email) throws Exception {
        User user = new User("Test User", email, "x", Role.USER);
        return userDAO.insert(user);
    }

    // Creates an event with 3 seats and returns the first one
    private Seat createSeat() throws Exception {
        User admin = userDAO.insert(new User("Admin", "admin@example.com", "x", Role.ADMIN));
        Event event = adminService.createEventWithSeats(admin, "Concert", "Hall",
                LocalDateTime.now().plusDays(1), 1, 3);
        List<Seat> seats = seatDAO.findByEvent(event.getId());
        return seats.get(0);
    }

    // Reads the seat's CURRENT state from the database
    private Seat reload(Seat seat) throws Exception {
        return seatDAO.findById(seat.getId()).get();
    }

    // ---------- tests ----------

    @Test
    public void holdingAnAvailableSeatSucceeds() throws Exception {
        Seat seat = createSeat();
        User alice = newUser("alice@example.com");

        boolean held = bookingService.holdSeat(seat.getId(), alice.getId());

        Assertions.assertTrue(held);
        Seat after = reload(seat);
        Assertions.assertEquals(SeatStatus.HOLD, after.getStatus());
        Assertions.assertEquals(alice.getId(), after.getHeldBy().longValue());
    }

    @Test
    public void aSecondUserCannotHoldASeatThatIsAlreadyHeld() throws Exception {
        Seat seat = createSeat();
        User alice = newUser("alice@example.com");
        User bob = newUser("bob@example.com");

        Assertions.assertTrue(bookingService.holdSeat(seat.getId(), alice.getId()));
        Assertions.assertFalse(bookingService.holdSeat(seat.getId(), bob.getId()));

        Assertions.assertEquals(alice.getId(), reload(seat).getHeldBy().longValue());
    }

    @Test
    public void theHolderCanConfirmAndABookingIsSaved() throws Exception {
        Seat seat = createSeat();
        User alice = newUser("alice@example.com");
        bookingService.holdSeat(seat.getId(), alice.getId());

        Booking booking = bookingService.confirmBooking(seat.getId(), alice.getId());

        Assertions.assertTrue(booking.getId() > 0);
        Assertions.assertEquals(SeatStatus.BOOKED, reload(seat).getStatus());
        List<Booking> bookings = bookingDAO.findByUser(alice.getId());
        Assertions.assertEquals(1, bookings.size());
        Assertions.assertEquals(seat.getId(), bookings.get(0).getSeatId());
    }

    @Test
    public void someoneWhoDoesNotHoldTheSeatCannotConfirmIt() throws Exception {
        Seat seat = createSeat();
        User alice = newUser("alice@example.com");
        User bob = newUser("bob@example.com");
        bookingService.holdSeat(seat.getId(), alice.getId());

        try {
            bookingService.confirmBooking(seat.getId(), bob.getId());
            Assertions.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }

        Assertions.assertEquals(0, bookingDAO.findByUser(bob.getId()).size());
        Assertions.assertEquals(SeatStatus.HOLD, reload(seat).getStatus());
    }

    @Test
    public void anExpiredHoldCanBeTakenByAnotherUser() throws Exception {
        Seat seat = createSeat();
        User alice = newUser("alice@example.com");
        User bob = newUser("bob@example.com");
        bookingService.holdSeat(seat.getId(), alice.getId());
        DbTestHelper.expireHold(seat.getId());

        boolean bobHeld = bookingService.holdSeat(seat.getId(), bob.getId());

        Assertions.assertTrue(bobHeld);
        Assertions.assertEquals(bob.getId(), reload(seat).getHeldBy().longValue());
    }

    @Test
    public void anExpiredHoldCannotBeConfirmed() throws Exception {
        Seat seat = createSeat();
        User alice = newUser("alice@example.com");
        bookingService.holdSeat(seat.getId(), alice.getId());
        DbTestHelper.expireHold(seat.getId());

        try {
            bookingService.confirmBooking(seat.getId(), alice.getId());
            Assertions.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }

        Assertions.assertEquals(0, bookingDAO.findByUser(alice.getId()).size());
    }

    @Test
    public void aBookedSeatCannotBeHeldByAnyoneElse() throws Exception {
        Seat seat = createSeat();
        User alice = newUser("alice@example.com");
        User bob = newUser("bob@example.com");
        bookingService.holdSeat(seat.getId(), alice.getId());
        bookingService.confirmBooking(seat.getId(), alice.getId());

        Assertions.assertFalse(bookingService.holdSeat(seat.getId(), bob.getId()));
    }

    // ---------- the concurrency test ----------

    /** One Racer = one user trying to hold the seat, on its own thread. */
    static class Racer extends Thread {
        private final BookingService service;
        private final long seatId;
        private final long userId;
        private boolean won = false;
        private Exception error = null;

        Racer(BookingService service, long seatId, long userId) {
            this.service = service;
            this.seatId = seatId;
            this.userId = userId;
        }

        @Override
        public void run() {
            try {
                won = service.holdSeat(seatId, userId);
            } catch (Exception e) {
                error = e;
            }
        }

        public boolean didWin() {
            return won;
        }

        public long getUserId() {
            return userId;
        }

        public Exception getError() {
            return error;
        }
    }

    @Test
    public void exactlyOneOfTwentyUsersWinsTheSeat() throws Exception {
        Seat seat = createSeat();

        List<Racer> racers = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            User user = newUser("racer" + i + "@example.com");
            racers.add(new Racer(bookingService, seat.getId(), user.getId()));
        }

        for (Racer racer : racers) {
            racer.start();
        }
        for (Racer racer : racers) {
            racer.join();
        }

        int winners = 0;
        long winnerId = -1;
        for (Racer racer : racers) {
            Assertions.assertNull(racer.getError());   // nobody crashed
            if (racer.didWin()) {
                winners++;
                winnerId = racer.getUserId();
            }
        }

        Assertions.assertEquals(1, winners);

        // The database must agree with the person who was told they won
        Seat after = reload(seat);
        Assertions.assertEquals(SeatStatus.HOLD, after.getStatus());
        Assertions.assertEquals(winnerId, after.getHeldBy().longValue());
    }
}
