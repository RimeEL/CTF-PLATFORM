package com.ctf.service;

import com.ctf.dao.IUserDAO;
import com.ctf.dao.UserDAOImpl;
import com.ctf.model.User;

import java.util.List;
import java.util.UUID;

public class UserService {

    private IUserDAO userDAO = new UserDAOImpl();

    // Récupérer son propre profil
    public User getProfile(UUID userId) {
        User user = userDAO.findById(userId);
        if (user == null) throw new RuntimeException("Utilisateur introuvable");
        return user;
    }

    // Mettre à jour son profil
    public User updateProfile(UUID userId, String username, String email) {
        User user = userDAO.findById(userId);
        if (user == null) throw new RuntimeException("Utilisateur introuvable");

        // Vérifier que le nouveau username n'est pas pris par quelqu'un d'autre
        User existing = userDAO.findByUsername(username);
        if (existing != null && !existing.getId().equals(userId))
            throw new RuntimeException("Username déjà pris");

        user.setUsername(username);
        user.setEmail(email);
        userDAO.update(user);
        return user;
    }

    // Lister tous les users — ADMIN seulement
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    // Désactiver un compte — ADMIN seulement
    public void deactivateUser(UUID userId) {
        User user = userDAO.findById(userId);
        if (user == null) throw new RuntimeException("Utilisateur introuvable");
        user.setActive(false);
        userDAO.update(user);
    }
}