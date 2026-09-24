package com.digitallibrary.modules.library;

import com.digitallibrary.core.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibraryService {

    private final LibraryRepository libraryRepository;
    private final LibrarySettingsRepository librarySettingsRepository;

    public LibraryService(LibraryRepository libraryRepository, LibrarySettingsRepository librarySettingsRepository) {
        this.libraryRepository = libraryRepository;
        this.librarySettingsRepository = librarySettingsRepository;
    }

    @Transactional(readOnly = true)
    public Library getLibrary(Long libraryId) {
        return libraryRepository.findById(libraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Library", "id", libraryId));
    }

    @Transactional(readOnly = true)
    public LibrarySettings getSettings(Long libraryId) {
        return librarySettingsRepository.findByLibraryId(libraryId)
                .orElseThrow(() -> new ResourceNotFoundException("LibrarySettings", "libraryId", libraryId));
    }

    @Transactional
    public LibrarySettings updateSettings(Long libraryId, LibrarySettings updatedSettings) {
        LibrarySettings settings = getSettings(libraryId);
        if (updatedSettings.getMonthlyFeeAmount() != null) {
            settings.setMonthlyFeeAmount(updatedSettings.getMonthlyFeeAmount());
        }
        if (updatedSettings.getGracePeriodDays() != null) {
            settings.setGracePeriodDays(updatedSettings.getGracePeriodDays());
        }
        if (updatedSettings.getAttendanceBlockAfterDays() != null) {
            settings.setAttendanceBlockAfterDays(updatedSettings.getAttendanceBlockAfterDays());
        }
        if (updatedSettings.getSeatReleaseAfterDays() != null) {
            settings.setSeatReleaseAfterDays(updatedSettings.getSeatReleaseAfterDays());
        }
        if (updatedSettings.getReservationTimeoutMinutes() != null) {
            settings.setReservationTimeoutMinutes(updatedSettings.getReservationTimeoutMinutes());
        }
        if (updatedSettings.getAllowQrAttendance() != null) {
            settings.setAllowQrAttendance(updatedSettings.getAllowQrAttendance());
        }
        return librarySettingsRepository.save(settings);
    }
}
