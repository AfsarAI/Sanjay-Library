package com.digitallibrary.modules.library;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LibrarySettingsRepository extends JpaRepository<LibrarySettings, Long> {

    Optional<LibrarySettings> findByLibraryId(Long libraryId);
}
