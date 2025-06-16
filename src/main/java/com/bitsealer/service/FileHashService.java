package com.bitsealer.service;

import com.bitsealer.model.FileHash;
import com.bitsealer.repository.FileHashRepository;
import com.bitsealer.repository.UserRepository;
import com.bitsealer.user.AppUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileHashService {

    /** Repositorios inyectados por constructor */
    private final FileHashRepository hashRepo;
    private final UserRepository  userRepo;

    public FileHashService(FileHashRepository hashRepo, UserRepository userRepo) {
        this.hashRepo = hashRepo;
        this.userRepo = userRepo;
    }

    /*──────────────────────────── 1) Guardar hash para el usuario logeado ────────────────────────────*/
    public FileHash save(String sha256, String fileName) {
        AppUser owner = getCurrentUser();          // usuario autenticado
        FileHash fh  = new FileHash();

        fh.setSha256(sha256);
        fh.setFileName(fileName);
        fh.setOwner(owner);

        return hashRepo.save(fh);
    }

    /*──────────────────────────── 2) Listar hashes del usuario logeado ──────────────────────────────*/
    public List<FileHash> listMine() {
        AppUser owner = getCurrentUser();
        return hashRepo.findByOwnerOrderByCreatedAtDesc(owner);
    }

    /*──────────────────────────── 3) Métodos utilitarios opcionales ─────────────────────────────────*/
    public FileHash saveForUser(AppUser owner, String sha256, String fileName) {
        FileHash fh = new FileHash();
        fh.setOwner(owner);
        fh.setSha256(sha256);
        fh.setFileName(fileName);
        return hashRepo.save(fh);
    }

    public List<FileHash> findAllByUser(AppUser owner) {
        return hashRepo.findByOwnerOrderByCreatedAtDesc(owner);
    }

    /*──────────────────────────── 4) Helper: usuario autenticado ────────────────────────────────────*/
    private AppUser getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                                               .getAuthentication()
                                               .getName();
        return userRepo.findByUsername(username)
                       .orElseThrow(); // si no existe, algo va mal en seguridad
    }
}
