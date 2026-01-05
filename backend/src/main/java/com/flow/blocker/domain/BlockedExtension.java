package com.flow.blocker.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "blocked_extension")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockedExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String extension;

    @Column(name = "is_fixed", nullable = false)
    private boolean fixed;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public BlockedExtension(String extension, boolean fixed, boolean active) {
        this.extension = extension;
        this.fixed = fixed;
        this.active = active;
    }

    public void toggleActive() {
        this.active = !this.active;
    }

    public static BlockedExtension createCustomExtension(String extension) {
        return BlockedExtension.builder()
                .extension(extension)
                .fixed(false)
                .active(true)
                .build();
    }
}
