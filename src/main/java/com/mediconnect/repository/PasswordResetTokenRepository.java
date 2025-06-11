package com.mediconnect.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mediconnect.model.PasswordResetToken;
import com.mediconnect.model.User;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    /**
     * Find the most recent valid token for a user
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user = :user " +
           "AND prt.isUsed = false AND prt.expiryDate > :now " +
           "AND prt.attemptCount < prt.maxAttempts " +
           "ORDER BY prt.createdAt DESC")
    Optional<PasswordResetToken> findValidTokenByUser(@Param("user") User user, 
                                                     @Param("now") LocalDateTime now);
    
    /**
     * FIXED: Find token by user and OTP - Get the most recent one only
     */
    @Query(value = "SELECT * FROM password_reset_tokens prt WHERE prt.user_id = :#{#user.id} " +
           "AND prt.otp = :otp AND prt.is_used = false " +
           "ORDER BY prt.created_at DESC LIMIT 1", nativeQuery = true)
    Optional<PasswordResetToken> findByUserAndOtp(@Param("user") User user, 
                                                  @Param("otp") String otp);
    
    /**
     * FIXED: Find the most recent token for a user - Use native query with LIMIT
     */
    @Query(value = "SELECT * FROM password_reset_tokens prt WHERE prt.user_id = :#{#user.id} " +
           "ORDER BY prt.created_at DESC LIMIT 1", nativeQuery = true)
    Optional<PasswordResetToken> findMostRecentTokenByUser(@Param("user") User user);
    
    /**
     * Find all tokens for a user
     */
    List<PasswordResetToken> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Find tokens by user and token type
     */
    List<PasswordResetToken> findByUserAndTokenTypeOrderByCreatedAtDesc(User user, 
                                                                        PasswordResetToken.TokenType tokenType);
    
    /**
     * Count active tokens for a user within a time period
     */
    @Query("SELECT COUNT(prt) FROM PasswordResetToken prt WHERE prt.user = :user " +
           "AND prt.createdAt > :since")
    int countTokensCreatedSince(@Param("user") User user, 
                               @Param("since") LocalDateTime since);
    
    /**
     * Delete expired tokens (cleanup)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiryDate < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * Delete used tokens older than specified date
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.isUsed = true " +
           "AND prt.usedAt < :cutoffDate")
    int deleteOldUsedTokens(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * FIXED: Mark all user's tokens as used - Use native query for better performance
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE password_reset_tokens SET is_used = true, used_at = :now " +
           "WHERE user_id = :#{#user.id} AND is_used = false", nativeQuery = true)
    int markAllUserTokensAsUsed(@Param("user") User user, 
                               @Param("now") LocalDateTime now);
    
    /**
     * Find tokens that are about to expire (for cleanup notifications)
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.isUsed = false " +
           "AND prt.expiryDate BETWEEN :now AND :soonThreshold")
    List<PasswordResetToken> findTokensExpiringSoon(@Param("now") LocalDateTime now,
                                                   @Param("soonThreshold") LocalDateTime soonThreshold);
    
    /**
     * Check if user has too many recent attempts (rate limiting)
     */
    @Query("SELECT COUNT(prt) FROM PasswordResetToken prt WHERE prt.user = :user " +
           "AND prt.createdAt > :since AND prt.tokenType = :tokenType")
    int countRecentAttempts(@Param("user") User user, 
                           @Param("since") LocalDateTime since,
                           @Param("tokenType") PasswordResetToken.TokenType tokenType);
    
    /**
     * Find valid token by OTP only (for verification without user context)
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.otp = :otp " +
           "AND prt.isUsed = false AND prt.expiryDate > :now " +
           "AND prt.attemptCount < prt.maxAttempts")
    Optional<PasswordResetToken> findValidTokenByOtp(@Param("otp") String otp,
                                                    @Param("now") LocalDateTime now);
}