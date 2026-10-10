package com.redisworkshop.leaderboard;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PlayerScoreRepository extends JpaRepository<PlayerScore, Long> {

    @Query("select p from PlayerScore p order by p.score desc")
    List<PlayerScore> findOrdered(Pageable pageable);

    @Modifying
    @Transactional
    @Query("update PlayerScore p set p.score = p.score + :delta where p.username = :username")
    void addPoints(@Param("username") String username, @Param("delta") int delta);
}