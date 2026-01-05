package com.flow.blocker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomExtensionRequest {

    @NotBlank(message = "확장자를 입력해주세요.")
    @Size(max = 20, message = "확장자는 최대 20자까지 입력 가능합니다.")
    private String extension;
}
