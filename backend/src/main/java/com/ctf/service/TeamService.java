package com.ctf.service;

import com.ctf.dao.ITeamDAO;
import com.ctf.dao.IUserDAO;
import com.ctf.dao.TeamDAOImpl;
import com.ctf.dao.UserDAOImpl;
import com.ctf.model.Team;
import com.ctf.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TeamService {

    private ITeamDAO teamDAO = new TeamDAOImpl();
    private IUserDAO userDAO = new UserDAOImpl();

    // Créer une équipe
    public Team createTeam(String name, UUID competitionId) {
        Team team = new Team();
        team.setId(UUID.randomUUID());
        team.setName(name);
        team.setCreatedAt(LocalDateTime.now());
        team.setCompetitionId(competitionId);
        teamDAO.save(team);
        return team;
    }

    // Récupérer une équipe par ID
    public Team getTeam(UUID teamId) {
        Team team = teamDAO.findById(teamId);
        if (team == null) throw new RuntimeException("Équipe introuvable");
        return team;
    }

    // Lister toutes les équipes d'une compétition
    public List<Team> getTeamsByCompetition(UUID competitionId) {
        return teamDAO.findByCompetitionId(competitionId);
    }

    // Rejoindre une équipe
    public void joinTeam(UUID userId, UUID teamId) {
        Team team = teamDAO.findById(teamId);
        if (team == null) throw new RuntimeException("Équipe introuvable");

        User user = userDAO.findById(userId);
        if (user == null) throw new RuntimeException("Utilisateur introuvable");
        if (user.getTeamId() != null)
            throw new RuntimeException("Tu es déjà dans une équipe");

        user.setTeamId(teamId);
        userDAO.update(user);
    }

    // Quitter une équipe
    public void leaveTeam(UUID userId) {
        User user = userDAO.findById(userId);
        if (user == null) throw new RuntimeException("Utilisateur introuvable");
        if (user.getTeamId() == null)
            throw new RuntimeException("Tu n'es dans aucune équipe");

        user.setTeamId(null);
        userDAO.update(user);
    }

    // Modifier une équipe — ADMIN
    public Team updateTeam(UUID teamId, String name) {
        Team team = teamDAO.findById(teamId);
        if (team == null) throw new RuntimeException("Équipe introuvable");
        team.setName(name);
        teamDAO.update(team);
        return team;
    }

    // Supprimer une équipe — ADMIN
    public void deleteTeam(UUID teamId) {
        Team team = teamDAO.findById(teamId);
        if (team == null) throw new RuntimeException("Équipe introuvable");
        teamDAO.delete(teamId);
    }
}