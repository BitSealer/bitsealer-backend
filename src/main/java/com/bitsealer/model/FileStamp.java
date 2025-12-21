package com.bitsealer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "file_stamps")
public class FileStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_hash_id", nullable = false, unique = true)
    private FileHash fileHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StampStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime sealedAt;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "ots_proof", columnDefinition = "bytea")
    private byte[] otsProof;

    public Long getId() { return id; }

    public FileHash getFileHash() { return fileHash; }
    public void setFileHash(FileHash fileHash) { this.fileHash = fileHash; }

    public StampStatus getStatus() { return status; }
    public void setStatus(StampStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getSealedAt() { return sealedAt; }
    public void setSealedAt(LocalDateTime sealedAt) { this.sealedAt = sealedAt; }

    public byte[] getOtsProof() { return otsProof; }
    public void setOtsProof(byte[] otsProof) { this.otsProof = otsProof; }
}
