package com.digitallibrary.modules.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByIdAndLibraryId(Long id, Long libraryId);

    Page<User> findByLibraryIdAndRole(Long libraryId, UserRole role, Pageable pageable);

    long countByLibraryIdAndRole(Long libraryId, UserRole role);
}
