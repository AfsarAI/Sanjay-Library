package com.digitallibrary.modules.jobs;

import com.digitallibrary.modules.seat.SeatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledReservationCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(ScheduledReservationCleanupJob.class);

    private final SeatService seatService;

    public ScheduledReservationCleanupJob(SeatService seatService) {
        this.seatService = seatService;
    }

    // Runs every 60 seconds
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredReservations() {
        try {
            seatService.releaseExpiredReservations();
        } catch (Exception e) {
            log.error("Error executing seat reservation cleanup job", e);
        }
    }
}
