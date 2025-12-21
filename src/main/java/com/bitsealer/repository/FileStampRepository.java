package com.bitsealer.repository;

import com.bitsealer.model.FileHash;
import com.bitsealer.model.FileStamp;
import com.bitsealer.model.StampStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileStampRepository extends JpaRepository<FileStamp, Long> {
    Optional<FileStamp> findByFileHash_Id(Long fileHashId);
    List<FileStamp> findByFileHash_IdIn(List<Long> fileHashIds);
    List<FileStamp> findAllByFileHashIn(List<FileHash> fileHashes);

    @Query("""
            select fs from FileStamp fs
            where fs.status in :statuses
              and (fs.nextCheckAt is null or fs.nextCheckAt <= :now)
            order by fs.createdAt asc
            """)
    List<FileStamp> findDueForRecheck(
            @Param("statuses") List<StampStatus> statuses,
            @Param("now") LocalDateTime now,
            org.springframework.data.domain.Pageable pageable
    );
}
