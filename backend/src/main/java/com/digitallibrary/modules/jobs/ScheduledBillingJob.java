package com.digitallibrary.modules.jobs;

import com.digitallibrary.modules.subscription.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledBillingJob {

    private static final Logger log = LoggerFactory.getLogger(ScheduledBillingJob.class);

    private final SubscriptionService subscriptionService;

    public ScheduledBillingJob(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    // Runs every day at 00:05 AM IST
    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Kolkata")
    public void runDailyBillingCycle() {
        log.info("Starting daily scheduled billing cycle transition job...");
        try {
            subscriptionService.processDailyBillingTransitions();
            log.info("Daily scheduled billing cycle job completed successfully.");
        } catch (Exception e) {
            log.error("Error executing daily billing cycle job", e);
        }
    }
}
