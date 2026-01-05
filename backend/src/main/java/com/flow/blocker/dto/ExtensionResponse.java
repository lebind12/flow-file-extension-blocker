package com.flow.blocker.dto;

import com.flow.blocker.domain.BlockedExtension;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExtensionResponse {

    private List<FixedExtensionDto> fixedExtensions;
    private List<CustomExtensionDto> customExtensions;
    private int customCount;
    private int maxCustomCount;

    @Getter
    @Builder
    public static class FixedExtensionDto {
        private String extension;
        private boolean active;

        public static FixedExtensionDto from(BlockedExtension entity) {
            return FixedExtensionDto.builder()
                    .extension(entity.getExtension())
                    .active(entity.isActive())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class CustomExtensionDto {
        private String extension;

        public static CustomExtensionDto from(BlockedExtension entity) {
            return CustomExtensionDto.builder()
                    .extension(entity.getExtension())
                    .build();
        }
    }
}
