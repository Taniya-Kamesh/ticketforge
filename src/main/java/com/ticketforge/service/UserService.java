package com.ticketforge.service;

import com.ticketforge.dao.UserDAO;
import com.ticketforge.exception.EmailAlreadyUsedException;
import com.ticketforge.exception.InvalidCredentialsException;
import com.ticketforge.exception.ValidationException;
import com.ticketforge.model.Role;
import com.ticketforge.model.User;
import com.ticketforge.util.PasswordHasher;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Optional;

public class UserService {
    private final UserDAO userDAO=new UserDAO();

    public User register(String name, String email, String rawPassword)
            throws ValidationException, EmailAlreadyUsedException, SQLException
            {
        if(name==null||name.trim().isEmpty()) throw new ValidationException("Name is required");
        if(email==null||email.trim().isEmpty()) throw new ValidationException("Email is required");
        if(rawPassword==null||rawPassword.trim().isEmpty()) throw new ValidationException(("Password is required"));
        if (rawPassword == null || rawPassword.length() < 8) throw new ValidationException("Password must be at least 8 characters");

        String cleanEmail=email.trim().toLowerCase();
        if(userDAO.findByEmail(cleanEmail).isPresent()){
            throw new EmailAlreadyUsedException("Email already registered: "+ cleanEmail);
        }

        User user=new User(name.trim(),email, PasswordHasher.hash(rawPassword), Role.USER);

       try{
           userDAO.insert(user);
       }catch (SQLIntegrityConstraintViolationException e) {
           throw new EmailAlreadyUsedException("Email already registered: " + cleanEmail);
       }

       return user;
       }

       public User login(String email,String rawPassword) throws InvalidCredentialsException, SQLException {
        if(email==null||rawPassword==null) throw new InvalidCredentialsException();
        Optional<User> found=userDAO.findByEmail(email.trim().toLowerCase());
        if(!found.isPresent()){
            throw new InvalidCredentialsException();
        }
        User user=found.get();
        if(!PasswordHasher.matches(rawPassword, user.getPasswordHash())){
            throw new InvalidCredentialsException();
        }
        return user;
       }

}
