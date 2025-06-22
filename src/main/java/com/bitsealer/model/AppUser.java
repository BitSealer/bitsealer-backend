package com.bitsealer.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "users")
public class AppUser implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Opcional que sea único; si quieres permitir duplicados quita unique=true */
    @Column(nullable = true, unique = true)
    private String username;

    /** El email es el identificador de login */
    @Column(nullable = false, unique = true)
    private String email;

    /** Contraseña ya cifrada con BCrypt */
    @Column(nullable = false)
    private String password;

    /** Rol por defecto */
    @Column(nullable = false)
    private String role = "ROLE_USER";

    /* ---------- constructores ---------- */

    public AppUser() { }

    public AppUser(String username,
                   String email,
                   String password,
                   String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role     = (role == null || role.isBlank()) ? "ROLE_USER" : role;
    }

    /* ---------- getters / setters ---------- */

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    /* ---------- override ---------- */

    @Override
    public String toString() {
        return "AppUser{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", role='" + role + '\'' +
               '}';
    }
}
