package com.ticketforge;

import com.ticketforge.dao.UserDAO;
import com.ticketforge.exception.EmailAlreadyUsedException;
import com.ticketforge.exception.InvalidCredentialsException;
import com.ticketforge.exception.NotAuthorizedException;
import com.ticketforge.exception.ValidationException;
import com.ticketforge.model.Event;
import com.ticketforge.model.Role;
import com.ticketforge.model.Seat;
import com.ticketforge.model.User;
import com.ticketforge.service.AdminService;
import com.ticketforge.service.UserService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static com.ticketforge.service.BookingService.seatDAO;

public class Demo {
    private static final String ADMIN_EMAIL="admin@ticketforge.test";
    private static final String ADMIN_PASSWORD="adminpass123";

    public static void main(String[] args) throws Exception {
        UserService userService = new UserService();
        AdminService adminService = new AdminService();
        UserDAO userDAO = new UserDAO();
        String email = "test" + System.currentTimeMillis() + "@example.com";

        System.out.println("--- 1. Register ---");

            User user = userService.register("TestUser", email, "password123");
            System.out.println("Registered: " + user);
            System.out.println("Password: " + user.getPasswordHash());

        System.out.println("--- 2. Duplicate email ---");
        try {
            userService.register("TestUser2", email, "password123");
            System.out.println("Duplicate email Registered successfully");
        }catch (EmailAlreadyUsedException e) {
            System.out.println("Correctly refused: " + e.getMessage());
        }


        System.out.println("--- 3. Weak password ---");

        try {
            userService.register("Weak", "weak" + System.currentTimeMillis() + "@example.com", "123");
            System.out.println("ERROR: weak password was accepted!");
        } catch(ValidationException e){
            System.out.println(e.getMessage());
        }


        System.out.println("--- 4. Login ---");

        User loggedIn=userService.login(email,"password123");
        System.out.println("user logged in: "+ user);


        System.out.println("---wrong password/ unknown email");

        try {
            userService.login(email, "wrong-password");
            System.out.println("ERROR: wrong password was accepted!");
        } catch (InvalidCredentialsException e) {
            System.out.println("Wrong password: " + e.getMessage());
        }

        try {
            userService.login("nobody@example.com", "password123");
            System.out.println("ERROR: unknown email was accepted!");
        } catch (InvalidCredentialsException  e) {
            System.out.println("Unknown email: " + e.getMessage());
        }

        System.out.println("---User tries to try admin actions---");
        try {
            adminService.createEventWithSeats(loggedIn, "Sneaky Event", "Nowhere",
                    LocalDateTime.now().plusDays(1), 1, 1);
            System.out.println("ERROR: a normal user created an event!");
        } catch (NotAuthorizedException e) {
            System.out.println("Correctly refused: " + e.getMessage());
        }

        System.out.println("--- 7. Admin creates an event ---");
        if(!userDAO.findByEmail(ADMIN_EMAIL).isPresent()){
             userService.register("Admin",ADMIN_EMAIL,ADMIN_PASSWORD);
            System.out.println("Created the admin account.");
        }
        User admin = userService.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        if (admin.getRole() != Role.ADMIN) {
            System.out.println("The account exists but is not an admin yet. Run this SQL in Workbench,");
            System.out.println("then run this demo again:");
            System.out.println("UPDATE users SET role = 'ADMIN' WHERE email = '" + ADMIN_EMAIL + "';");
            return;
        }
        Event event = adminService.createEventWithSeats(admin, "Phase 6 Test Concert", "Main Hall",
                LocalDateTime.now().plusDays(30), 3, 5);

        System.out.println("Created: " + event);
        List<Seat> seats = seatDAO.findByEvent(event.getId());
        System.out.println("Seats created: " + seats.size());
        System.out.println("First seat: " + seats.get(0).getSeatLabel()
                + ", last seat: " + seats.get(seats.size() - 1).getSeatLabel());
    }
}
