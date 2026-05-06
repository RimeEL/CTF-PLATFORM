package com.ctf.dao;
import com.ctf.model.Competition;
import java.util.List;
import java.util.UUID;

public interface ICompetitionDAO {
	void create(Competition competition);
    Competition findById(UUID id);
    List<Competition> findAll();
    void update(Competition competition);
    void delete(UUID id);
}
