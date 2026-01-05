package com.flow.blocker.service;

import com.flow.blocker.domain.BlockedExtension;
import com.flow.blocker.dto.ExtensionResponse;
import com.flow.blocker.dto.ExtensionResponse.CustomExtensionDto;
import com.flow.blocker.dto.ExtensionResponse.FixedExtensionDto;
import com.flow.blocker.exception.ExtensionException;
import com.flow.blocker.exception.ExtensionException.ErrorCode;
import com.flow.blocker.repository.BlockedExtensionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExtensionService {

    private static final int MAX_CUSTOM_COUNT = 200;
    private static final int MAX_EXTENSION_LENGTH = 20;
    private static final Pattern VALID_EXTENSION_PATTERN = Pattern.compile("^[a-z0-9]+$");
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(".*([/\\\\]|\\.\\.).*");

    private final BlockedExtensionRepository repository;

    public ExtensionResponse getAllExtensions() {
        List<BlockedExtension> fixedList = repository.findByFixedTrue();
        List<BlockedExtension> customList = repository.findByFixedFalse();

        return ExtensionResponse.builder()
                .fixedExtensions(fixedList.stream()
                        .map(FixedExtensionDto::from)
                        .toList())
                .customExtensions(customList.stream()
                        .map(CustomExtensionDto::from)
                        .toList())
                .customCount(customList.size())
                .maxCustomCount(MAX_CUSTOM_COUNT)
                .build();
    }

    @Transactional
    public void toggleFixedExtension(String extension) {
        String normalized = normalizeExtension(extension);
        BlockedExtension entity = repository.findByExtension(normalized)
                .orElseThrow(() -> new ExtensionException(ErrorCode.EXTENSION_NOT_FOUND));

        entity.toggleActive();
    }

    @Transactional
    public void addCustomExtension(String extension) {
        String normalized = normalizeExtension(extension);
        validateExtension(normalized);

        if (repository.existsByExtension(normalized)) {
            throw new ExtensionException(ErrorCode.DUPLICATE_EXTENSION);
        }

        if (repository.countByFixedFalse() >= MAX_CUSTOM_COUNT) {
            throw new ExtensionException(ErrorCode.MAX_CUSTOM_EXCEEDED);
        }

        repository.save(BlockedExtension.createCustomExtension(normalized));
    }

    @Transactional
    public void deleteCustomExtension(String extension) {
        String normalized = normalizeExtension(extension);
        BlockedExtension entity = repository.findByExtension(normalized)
                .orElseThrow(() -> new ExtensionException(ErrorCode.EXTENSION_NOT_FOUND));

        if (entity.isFixed()) {
            throw new ExtensionException(ErrorCode.CANNOT_DELETE_FIXED);
        }

        repository.delete(entity);
    }

    private String normalizeExtension(String extension) {
        if (extension == null) {
            throw new ExtensionException(ErrorCode.EMPTY_EXTENSION);
        }

        String normalized = extension.trim().toLowerCase();

        if (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }

        return normalized;
    }

    private void validateExtension(String extension) {
        if (extension.isEmpty()) {
            throw new ExtensionException(ErrorCode.EMPTY_EXTENSION);
        }

        if (extension.length() > MAX_EXTENSION_LENGTH) {
            throw new ExtensionException(ErrorCode.EXTENSION_TOO_LONG);
        }

        if (PATH_TRAVERSAL_PATTERN.matcher(extension).matches()) {
            throw new ExtensionException(ErrorCode.PATH_TRAVERSAL_DETECTED);
        }

        if (!VALID_EXTENSION_PATTERN.matcher(extension).matches()) {
            throw new ExtensionException(ErrorCode.INVALID_EXTENSION);
        }
    }
}
