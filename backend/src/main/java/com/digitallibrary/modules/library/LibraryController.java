package com.digitallibrary.modules.library;

import com.digitallibrary.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/libraries")
@Tag(name = "Library & Settings", description = "Endpoints for library details and configurable business settings")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping("/{libraryId}")
    @Operation(summary = "Get library details", description = "Returns library name, address, contact phone, and operating hours")
    public ResponseEntity<ApiResponse<Library>> getLibrary(@PathVariable Long libraryId) {
        Library library = libraryService.getLibrary(libraryId);
        return ResponseEntity.ok(ApiResponse.success(library));
    }

    @GetMapping("/{libraryId}/settings")
    @Operation(summary = "Get library configurable settings", description = "Returns fee amounts, grace periods, block days, and timeouts")
    public ResponseEntity<ApiResponse<LibrarySettings>> getSettings(@PathVariable Long libraryId) {
        LibrarySettings settings = libraryService.getSettings(libraryId);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping("/{libraryId}/settings")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update library settings (Admin only)", description = "Allows owner to modify monthly fee, grace period, or attendance block days")
    public ResponseEntity<ApiResponse<LibrarySettings>> updateSettings(
            @PathVariable Long libraryId,
            @RequestBody LibrarySettings settings) {
        LibrarySettings updated = libraryService.updateSettings(libraryId, settings);
        return ResponseEntity.ok(ApiResponse.success(updated, "Library settings updated successfully"));
    }
}
