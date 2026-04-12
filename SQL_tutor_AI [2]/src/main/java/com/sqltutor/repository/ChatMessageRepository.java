package com.sqltutor.repository;

import com.sqltutor.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT c FROM ChatMessage c WHERE c.username = :username ORDER BY c.timestamp DESC")
    List<ChatMessage> findRecentByUsername(@Param("username") String username, Pageable pageable);

    @Query("SELECT c FROM ChatMessage c WHERE c.username = :username ORDER BY c.timestamp ASC")
    List<ChatMessage> findAllByUsernameAsc(@Param("username") String username);

    void deleteByUsername(String username);
    long countByUsername(String username);
}
