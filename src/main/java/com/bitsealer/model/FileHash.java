package com.bitsealer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_hashes")
public class FileHash {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sha256;
    private String fileName;
    private LocalDateTime createdAt = LocalDateTime.now();

    /* NUEVO 👉 cada hash pertenece a un usuario */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser owner;

    /* getters / setters */

    public Long getId() { return id; }

    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /* 👉 nuevos getter/setter */
    public AppUser getOwner() { return owner; }
    public void setOwner(AppUser owner) { this.owner = owner; }
}
