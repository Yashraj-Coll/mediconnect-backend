package com.mediconnect.controller;

import com.mediconnect.model.ChatMessage;
import com.mediconnect.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ChatService chatService;

    /**
     * Handle incoming chat messages via WebSocket
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload Map<String, Object> payload) {
        try {
            System.out.println("📨 CHAT: Raw payload received: " + payload);
            
            // Manually create ChatMessage to handle timestamp parsing
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setAppointmentId(Long.valueOf(payload.get("appointmentId").toString()));
            chatMessage.setSenderId(Long.valueOf(payload.get("senderId").toString()));
            chatMessage.setSenderType(payload.get("senderType").toString());
            chatMessage.setSenderName(payload.get("senderName").toString());
            chatMessage.setMessageText(payload.get("messageText").toString());
            
            // Handle timestamp manually
            String timestampStr = payload.get("timestamp").toString();
            if (timestampStr.endsWith("Z")) {
                timestampStr = timestampStr.substring(0, timestampStr.length() - 1);
            }
            chatMessage.setTimestamp(LocalDateTime.parse(timestampStr));
            
            System.out.println("📨 CHAT: Parsed message: " + chatMessage.getMessageText());
            
            // Save message to database
            ChatMessage savedMessage = chatService.saveMessage(chatMessage);
            
            // Send to specific appointment room
            String destination = "/topic/appointment/" + chatMessage.getAppointmentId();
            System.out.println("📤 CHAT: Broadcasting message to: " + destination);
            
            messagingTemplate.convertAndSend(destination, savedMessage);
            
        } catch (Exception e) {
            System.err.println("❌ CHAT: Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * REST endpoint to get chat history
     */
    @GetMapping("/api/chat/{appointmentId}/messages")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getChatHistory(@PathVariable Long appointmentId) {
        try {
            System.out.println("📜 CHAT: Getting chat history for appointment: " + appointmentId);
            
            List<ChatMessage> messages = chatService.getChatHistory(appointmentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", messages);
            response.put("count", messages.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ CHAT: Error getting chat history: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve chat history");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * REST endpoint to get unread message count
     */
    @GetMapping("/api/chat/{appointmentId}/unread/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount(
            @PathVariable Long appointmentId, 
            @PathVariable Long userId) {
        try {
            Long unreadCount = chatService.countUnreadMessages(appointmentId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("unreadCount", unreadCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get unread count");
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * REST endpoint to mark messages as read
     */
    @PostMapping("/api/chat/{appointmentId}/markRead/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long appointmentId, 
            @PathVariable Long userId) {
        try {
            chatService.markMessagesAsRead(appointmentId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Messages marked as read");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to mark messages as read");
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}