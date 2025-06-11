package com.mediconnect.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.mediconnect.model.Session;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByJwtToken(String jwtToken);
    Optional<Session> findBySessionToken(String sessionToken);
    List<Session> findByUser_IdAndLogoutTimeIsNull(Long userId);
}
