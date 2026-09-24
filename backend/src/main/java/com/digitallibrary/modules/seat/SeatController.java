package com.digitallibrary.modules.seat;

import com.digitallibrary.core.dto.ApiResponse;
import com.digitallibrary.core.security.SecurityUtils;
import com.digitallibrary.modules.seat.dto.SeatDto;
import com.digitallibrary.modules.seat.dto.SeatLayoutDto;
import com.digitallibrary.modules.seat.dto.SeatReservationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seats")
@Tag(name = "Seat Management", description = "Endpoints for visual seat layout, real-time availability, and reservations")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/layout")
    @Operation(summary = "Get full visual seat layout map", description = "Returns 50 desks grid with real-time status and occupancy counts")
    public ResponseEntity<ApiResponse<SeatLayoutDto>> getSeatLayout(@RequestParam(defaultValue = "1") Long libraryId) {
        SeatLayoutDto layout = seatService.getSeatLayout(libraryId);
        return ResponseEntity.ok(ApiResponse.success(layout));
    }

    @PostMapping("/{seatId}/reserve")
    @Operation(summary = "Temporarily reserve a seat (10-minute hold)", description = "Pessimistically locks seat during student admission checkout flow")
    public ResponseEntity<ApiResponse<SeatReservationDto>> reserveSeat(
            @PathVariable Long seatId,
            @RequestParam(defaultValue = "1") Long libraryId) {
        Long userId = SecurityUtils.getCurrentUserId();
        SeatReservationDto reservation = seatService.reserveSeat(libraryId, seatId, userId);
        return ResponseEntity.ok(ApiResponse.success(reservation, "Seat reserved successfully for 10 minutes"));
    }

    @DeleteMapping("/reservations/{reservationId}")
    @Operation(summary = "Cancel temporary seat reservation", description = "Releases temporary hold and resets seat to AVAILABLE")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(@PathVariable Long reservationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        seatService.cancelReservation(reservationId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Reservation cancelled successfully"));
    }

    @PatchMapping("/{seatId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin update seat status", description = "Place seat in MAINTENANCE or restore to AVAILABLE")
    public ResponseEntity<ApiResponse<SeatDto>> updateSeatStatus(
            @PathVariable Long seatId,
            @RequestParam SeatStatus status) {
        SeatDto updatedSeat = seatService.adminUpdateSeatStatus(seatId, status);
        return ResponseEntity.ok(ApiResponse.success(updatedSeat, "Seat status updated to " + status));
    }
}
