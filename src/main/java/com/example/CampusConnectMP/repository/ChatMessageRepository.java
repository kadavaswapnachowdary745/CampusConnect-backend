package com.example.CampusConnectMP.repository;

import com.example.CampusConnectMP.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    @Query("SELECT c FROM ChatMessage c WHERE (c.sender.id = :user1Id AND c.receiver.id = :user2Id) " +
           "OR (c.sender.id = :user2Id AND c.receiver.id = :user1Id) ORDER BY c.timestamp ASC")
    List<ChatMessage> findConversation(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
    
    @Query("SELECT c FROM ChatMessage c WHERE ((c.sender.id = :user1Id AND c.receiver.id = :user2Id) " +
           "OR (c.sender.id = :user2Id AND c.receiver.id = :user1Id)) AND c.product.id = :productId " +
           "ORDER BY c.timestamp ASC")
    List<ChatMessage> findConversationAboutProduct(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id, @Param("productId") Long productId);

    @Query("SELECT DISTINCT COALESCE(c.sender.id, c.receiver.id) FROM ChatMessage c WHERE c.sender.id = :userId OR c.receiver.id = :userId")
    List<Long> findDistinctChatParticipantIds(@Param("userId") Long userId);

    @Query("SELECT DISTINCT c.product.id FROM ChatMessage c WHERE (c.sender.id = :userId OR c.receiver.id = :userId) AND c.product IS NOT NULL")
    List<Long> findContactedProductIds(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE ChatMessage c SET c.isRead = true WHERE c.receiver.id = :receiverId AND c.sender.id = :senderId AND ((:productId IS NULL AND c.product IS NULL) OR (c.product.id = :productId)) AND c.isRead = false")
    int markMessagesAsRead(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId, @Param("productId") Long productId);

    @Query("SELECT c.sender.id, c.product.id, COUNT(c) FROM ChatMessage c WHERE c.receiver.id = :receiverId AND c.isRead = false GROUP BY c.sender.id, c.product.id")
    List<Object[]> countUnreadBySenderAndProduct(@Param("receiverId") Long receiverId);
}
