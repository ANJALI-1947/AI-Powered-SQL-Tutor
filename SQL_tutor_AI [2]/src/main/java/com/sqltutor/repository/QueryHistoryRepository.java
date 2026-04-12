package com.sqltutor.repository;

import com.sqltutor.model.QueryHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QueryHistoryRepository extends JpaRepository<QueryHistory, Long> {
    @Query("SELECT q FROM QueryHistory q WHERE q.username = :username ORDER BY q.executedAt DESC")
    List<QueryHistory> findRecentByUsername(@Param("username") String username, Pageable pageable);
    long countByUsername(String username);
    void deleteByUsername(String username);
}
