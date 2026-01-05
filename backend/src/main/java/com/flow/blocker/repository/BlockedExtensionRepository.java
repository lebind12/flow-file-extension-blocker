package com.flow.blocker.repository;

import com.flow.blocker.domain.BlockedExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlockedExtensionRepository extends JpaRepository<BlockedExtension, Long> {

    Optional<BlockedExtension> findByExtension(String extension);

    boolean existsByExtension(String extension);

    List<BlockedExtension> findByFixedTrue();

    List<BlockedExtension> findByFixedFalse();

    long countByFixedFalse();

    void deleteByExtension(String extension);
}
