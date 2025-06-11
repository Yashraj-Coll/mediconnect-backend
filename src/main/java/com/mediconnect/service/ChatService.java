package com.mediconnect.service;

import com.mediconnect.model.ChatMessage;
import com.mediconnect.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    /**
     * Save a chat message to the database
     */
    public ChatMessage saveMessage(ChatMessage chatMessage) {
        try {
            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
            System.out.println("💾 CHAT: Message saved to database: " + savedMessage.getId());
            return savedMessage;
        } catch (Exception e) {
            System.err.println("❌ CHAT: Failed to save message: " + e.getMessage());
            throw new RuntimeException("Failed to save chat message", e);
        }
    }

    /**
     * Get chat history for an appointment
     */
    public List<ChatMessage> getChatHistory(Long appointmentId) {
        try {
            List<ChatMessage> messages = chatMessageRepository.findByAppointmentIdOrderByTimestampAsc(appointmentId);
            System.out.println("📜 CHAT: Retrieved " + messages.size() + " messages for appointment " + appointmentId);
            return messages;
        } catch (Exception e) {
            System.err.println("❌ CHAT: Failed to get chat history: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve chat history", e);
        }
    }

    /**
     * Get unread messages for a user in an appointment
     */
    public List<ChatMessage> getUnreadMessages(Long appointmentId, Long userId) {
        try {
            return chatMessageRepository.findUnreadMessages(appointmentId, userId);
        } catch (Exception e) {
            System.err.println("❌ CHAT: Failed to get unread messages: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve unread messages", e);
        }
    }

    /**
     * Count unread messages for a user in an appointment
     */
    public Long countUnreadMessages(Long appointmentId, Long userId) {
        try {
            return chatMessageRepository.countUnreadMessages(appointmentId, userId);
        } catch (Exception e) {
            System.err.println("❌ CHAT: Failed to count unread messages: " + e.getMessage());
            return 0L;
        }
    }

    /**
     * Mark messages as read for a user in an appointment
     */
    public void markMessagesAsRead(Long appointmentId, Long userId) {
        try {
            chatMessageRepository.markMessagesAsRead(appointmentId, userId);
            System.out.println("✅ CHAT: Messages marked as read for user " + userId + " in appointment " + appointmentId);
        } catch (Exception e) {
            System.err.println("❌ CHAT: Failed to mark messages as read: " + e.getMessage());
        }
    }
}