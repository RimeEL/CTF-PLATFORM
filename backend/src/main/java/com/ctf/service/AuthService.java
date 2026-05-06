package com.ctf.service;

import com.ctf.dao.IUserDAO;
import com.ctf.dao.UserDAOImpl;
import com.ctf.model.User;
import com.ctf.model.UserRole;
import com.ctf.util.JwtUtil;
import com.ctf.util.PasswordUtil;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuthService {

    private IUserDAO userDAO = new UserDAOImpl();

    public String login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user == null) throw new RuntimeException("Utilisateur introuvable");
        if (!user.isActive()) throw new RuntimeException("Compte désactivé");
        if (!PasswordUtil.verify(password, user.getPasswordHash()))
            throw new RuntimeException("Mot de passe incorrect");
        return JwtUtil.generateToken(user.getId(), user.getRole().name());
    }

    public User register(String username, String email, String password) {
        if (userDAO.findByUsername(username) != null)
            throw new RuntimeException("Username déjà pris");
        if (userDAO.findByEmail(email) != null)
            throw new RuntimeException("Email déjà utilisé");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(PasswordUtil.hash(password));
        user.setRole(UserRole.USER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        userDAO.save(user);
        return user;
    }
}