package com.bitsealer.repository;

import com.bitsealer.model.AppUser;
import com.bitsealer.model.FileHash;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileHashRepository extends JpaRepository<FileHash, Long> {

    /* hashes sólo del propietario, último primero */
    List<FileHash> findByOwnerOrderByCreatedAtDesc(AppUser owner);
}
