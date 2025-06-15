package com.bitsealer.service;

import com.bitsealer.model.FileHash;
import com.bitsealer.repository.FileHashRepository;
import org.springframework.stereotype.Service;

@Service
public class FileHashService {

    private final FileHashRepository repo;

    public FileHashService(FileHashRepository repo) {
        this.repo = repo;
    }

    public FileHash save(String sha256, String fileName) {
        FileHash fh = new FileHash();
        fh.setSha256(sha256);
        fh.setFileName(fileName);
        return repo.save(fh);
    }

    public java.util.List<FileHash> findAll() {
        return repo.findAll();
    }
}
