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

    // Para polling/reintentos desde backend (robustez ante reinicios)
    private LocalDateTime lastCheckedAt;
    private LocalDateTime nextCheckAt;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(length = 64)
    private String txid;

    @Column(length = 2048)
    private String lastError;

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

    public LocalDateTime getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(LocalDateTime lastCheckedAt) { this.lastCheckedAt = lastCheckedAt; }

    public LocalDateTime getNextCheckAt() { return nextCheckAt; }
    public void setNextCheckAt(LocalDateTime nextCheckAt) { this.nextCheckAt = nextCheckAt; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public String getTxid() { return txid; }
    public void setTxid(String txid) { this.txid = txid; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }

    public byte[] getOtsProof() { return otsProof; }
    public void setOtsProof(byte[] otsProof) { this.otsProof = otsProof; }
}
