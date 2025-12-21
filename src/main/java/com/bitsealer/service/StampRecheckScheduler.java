package com.bitsealer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler que revisa los stamps PENDING/ANCHORING.
 *
 * Frecuencia controlada por: stamps.recheck.fixed-delay-ms (por defecto 60000)
 */
@Component
public class StampRecheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(StampRecheckScheduler.class);

    private final StampRecheckService stampRecheckService;

    public StampRecheckScheduler(StampRecheckService stampRecheckService) {
        this.stampRecheckService = stampRecheckService;
    }

    @Scheduled(fixedDelayString = "${stamps.recheck.fixed-delay-ms:60000}")
    public void run() {
        int processed = stampRecheckService.recheckDue();
        if (processed > 0) {
            log.info("Recheck processed {} stamp(s)", processed);
        }
    }
}
