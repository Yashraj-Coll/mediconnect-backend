package com.mediconnect.repository;

import com.mediconnect.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    /**
     * Find all messages for a specific appointment ordered by timestamp
     */
    List<ChatMessage> findByAppointmentIdOrderByTimestampAsc(Long appointmentId);
    
    /**
     * Find unread messages for a specific appointment and user
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.appointmentId = :appointmentId " +
           "AND cm.senderId != :userId AND cm.isRead = false ORDER BY cm.timestamp ASC")
    List<ChatMessage> findUnreadMessages(@Param("appointmentId") Long appointmentId, 
                                       @Param("userId") Long userId);
    
    /**
     * Count unread messages for a specific appointment and user
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.appointmentId = :appointmentId " +
           "AND cm.senderId != :userId AND cm.isRead = false")
    Long countUnreadMessages(@Param("appointmentId") Long appointmentId, 
                            @Param("userId") Long userId);
    
    /**
     * Mark messages as read
     */
    @Query("UPDATE ChatMessage cm SET cm.isRead = true WHERE cm.appointmentId = :appointmentId " +
           "AND cm.senderId != :userId AND cm.isRead = false")
    void markMessagesAsRead(@Param("appointmentId") Long appointmentId, 
                           @Param("userId") Long userId);
}