package com.ctf.dao;
import com.ctf.model.Team;
import java.util.List;
import java.util.UUID;

public interface ITeamDAO {
    Team findById(UUID id);
    List<Team> findByCompetitionId(UUID competitionId);
    void save(Team team);
    void update(Team team);
    void delete(UUID id);
}