package com.bitsealer.repository;

import com.bitsealer.model.FileHash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileHashRepository extends JpaRepository<FileHash, Long> {
    // No es necesario agregar métodos por ahora.
    // JpaRepository ya provee métodos CRUD básicos (save, findAll, findById, delete, etc.)
}
