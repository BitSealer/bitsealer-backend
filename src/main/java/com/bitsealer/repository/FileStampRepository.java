package com.bitsealer.repository;

import com.bitsealer.model.FileStamp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileStampRepository extends JpaRepository<FileStamp, Long> {
    Optional<FileStamp> findByFileHash_Id(Long fileHashId);
    List<FileStamp> findByFileHash_IdIn(List<Long> fileHashIds);
}
