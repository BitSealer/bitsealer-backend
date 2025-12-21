package com.bitsealer.service;

import com.bitsealer.dto.StamperUpgradeResponse;
import com.bitsealer.model.FileStamp;
import com.bitsealer.model.StampStatus;
import com.bitsealer.repository.FileStampRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class StampRecheckService {

    private static final Logger log = LoggerFactory.getLogger(StampRecheckService.class);

    private final FileStampRepository fileStampRepository;
    private final StamperClient stamperClient;

    private final int defaultBatchSize;
    private final int baseDelaySeconds;
    private final int maxDelaySeconds;

    public StampRecheckService(FileStampRepository fileStampRepository,
                              StamperClient stamperClient,
                              @Value("${stamps.recheck.batch-size:50}") int defaultBatchSize,
                              @Value("${stamps.recheck.base-delay-seconds:60}") int baseDelaySeconds,
                              @Value("${stamps.recheck.max-delay-seconds:3600}") int maxDelaySeconds) {
        this.fileStampRepository = fileStampRepository;
        this.stamperClient = stamperClient;
        this.defaultBatchSize = defaultBatchSize;
        this.baseDelaySeconds = baseDelaySeconds;
        this.maxDelaySeconds = maxDelaySeconds;
    }

    public int recheckDue() {
        return recheckDue(defaultBatchSize);
    }

    public int recheckDue(int limit) {
        LocalDateTime now = LocalDateTime.now();

        List<FileStamp> due = fileStampRepository.findDueForRecheck(
                List.of(StampStatus.PENDING, StampStatus.ANCHORING),
                now,
                PageRequest.of(0, Math.max(1, limit))
        );

        if (due.isEmpty()) return 0;

        for (FileStamp stamp : due) {
            recheckOne(stamp, now);
        }

        return due.size();
    }

    private void recheckOne(FileStamp stamp, LocalDateTime now) {
        stamp.setLastCheckedAt(now);
        stamp.setAttempts(stamp.getAttempts() + 1);

        if (stamp.getOtsProof() == null || stamp.getOtsProof().length == 0) {
            stamp.setStatus(StampStatus.ERROR);
            stamp.setLastError("No hay ots_proof guardado; imposible hacer upgrade");
            stamp.setNextCheckAt(null);
            fileStampRepository.save(stamp);
            return;
        }

        try {
            StamperUpgradeResponse resp = stamperClient.upgrade(stamp.getId(), stamp.getOtsProof());

            if (resp != null) {
                if (resp.otsProofB64() != null && !resp.otsProofB64().isBlank()) {
                    stamp.setOtsProof(Base64.getDecoder().decode(resp.otsProofB64()));
                }
                if (resp.txid() != null && !resp.txid().isBlank()) {
                    stamp.setTxid(resp.txid());
                }

                StampStatus newStatus = mapStatus(resp.status());
                stamp.setStatus(newStatus);

                if (newStatus == StampStatus.SEALED) {
                    if (stamp.getSealedAt() == null) stamp.setSealedAt(now);
                    stamp.setNextCheckAt(null);
                    stamp.setLastError(null);
                } else {
                    stamp.setNextCheckAt(now.plusSeconds(computeBackoffSeconds(stamp.getAttempts())));
                    stamp.setLastError(null);
                }
            }

        } catch (Exception e) {
            log.warn("Recheck failed stampId={}: {}", stamp.getId(), e.getMessage());
            stamp.setStatus(StampStatus.ERROR);
            stamp.setLastError(e.getMessage());
            stamp.setNextCheckAt(now.plusSeconds(computeBackoffSeconds(stamp.getAttempts())));
        }

        fileStampRepository.save(stamp);
    }

    private StampStatus mapStatus(String status) {
        if (status == null) return StampStatus.PENDING;
        String s = status.trim().toUpperCase();
        return switch (s) {
            case "SEALED" -> StampStatus.SEALED;
            case "ANCHORING" -> StampStatus.ANCHORING;
            case "PENDING" -> StampStatus.PENDING;
            default -> StampStatus.PENDING;
        };
    }

    private int computeBackoffSeconds(int attempts) {
        int cappedPow = Math.min(Math.max(attempts - 1, 0), 6); // 2^0..2^6
        long delay = (long) baseDelaySeconds * (1L << cappedPow);
        return (int) Math.min(delay, (long) maxDelaySeconds);
    }
}
