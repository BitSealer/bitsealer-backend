package com.bitsealer.repository;

import com.bitsealer.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    /** Búsqueda principal: usamos el e-mail para el login */
    Optional<AppUser> findByEmail(String email);

    /** También podemos seguir buscando por username si lo necesitas */
    Optional<AppUser> findByUsername(String username);

    /** Útil para validar que no haya e-mails duplicados antes de registrar */
    boolean existsByEmail(String email);
}
