package com.bitsealer.repository;

import com.bitsealer.model.AppUser;
import com.bitsealer.model.FileHash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileHashRepository extends JpaRepository<FileHash, Long> {
    // Historial del usuario
    List<FileHash> findByOwnerOrderByCreatedAtDesc(AppUser owner);

    // (Opcional) Top 5 recientes
    List<FileHash> findTop5ByOwnerOrderByCreatedAtDesc(AppUser owner);

    // (Opcional) Contadores
    long countByOwner(AppUser owner);
    long countByOwnerAndCreatedAtAfter(AppUser owner, java.time.LocalDateTime date);
}
