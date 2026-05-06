package com.ctf.dao;
import com.ctf.model.User;
import java.util.List;
import java.util.UUID;

public interface IUserDAO {
    User findById(UUID id);
    User findByUsername(String username);
    User findByEmail(String email);
    List<User> findByTeamId(UUID teamId);
    void save(User user);
    void update(User user);
    void delete(UUID id);
    
    List<User> findAll();
}