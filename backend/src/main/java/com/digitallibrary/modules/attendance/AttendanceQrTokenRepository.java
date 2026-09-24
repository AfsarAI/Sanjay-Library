package com.digitallibrary.modules.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttendanceQrTokenRepository extends JpaRepository<AttendanceQrToken, Long> {

    Optional<AttendanceQrToken> findByToken(String token);
}
