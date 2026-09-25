package com.digitallibrary.modules.jobs;

import com.digitallibrary.modules.attendance.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledAutoCheckoutJob {

    private static final Logger log = LoggerFactory.getLogger(ScheduledAutoCheckoutJob.class);

    private final AttendanceService attendanceService;

    public ScheduledAutoCheckoutJob(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // Runs every day at 11:15 PM IST (library closing time)
    @Scheduled(cron = "0 15 23 * * *", zone = "Asia/Kolkata")
    public void runNightlyAutoCheckout() {
        log.info("Starting nightly scheduled auto-checkout job for unclosed attendances...");
        try {
            int checkedOutCount = attendanceService.autoCheckoutPendingRecords();
            log.info("Nightly scheduled auto-checkout completed successfully. Total processed: {}", checkedOutCount);
        } catch (Exception e) {
            log.error("Error executing nightly auto-checkout job", e);
        }
    }
}
