package com.flow.blocker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.blocker.dto.CustomExtensionRequest;
import com.flow.blocker.repository.BlockedExtensionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExtensionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BlockedExtensionRepository repository;

    @BeforeEach
    void setUp() {
        repository.findByFixedFalse().forEach(repository::delete);
    }

    @Nested
    @DisplayName("GET /api/extensions")
    class GetAllExtensions {

        @Test
        @DisplayName("전체 확장자 목록을 조회한다")
        void shouldReturnAllExtensions() throws Exception {
            mockMvc.perform(get("/api/extensions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fixedExtensions", hasSize(7)))
                    .andExpect(jsonPath("$.fixedExtensions[*].extension",
                            containsInAnyOrder("bat", "cmd", "com", "cpl", "exe", "scr", "js")))
                    .andExpect(jsonPath("$.customExtensions", hasSize(0)))
                    .andExpect(jsonPath("$.customCount", is(0)))
                    .andExpect(jsonPath("$.maxCustomCount", is(200)));
        }
    }

    @Nested
    @DisplayName("PATCH /api/extensions/fixed/{extension}")
    class ToggleFixedExtension {

        @Test
        @DisplayName("고정 확장자를 토글한다")
        void shouldToggleFixedExtension() throws Exception {
            mockMvc.perform(patch("/api/extensions/fixed/exe"))
                    .andExpect(status().isOk());

            // 상태 확인
            mockMvc.perform(get("/api/extensions"))
                    .andExpect(jsonPath("$.fixedExtensions[?(@.extension=='exe')].active", contains(true)));
        }

        @Test
        @DisplayName("존재하지 않는 확장자 토글 시 400을 반환한다")
        void shouldReturn400ForNonExistent() throws Exception {
            mockMvc.perform(patch("/api/extensions/fixed/notexist"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is("EXTENSION_NOT_FOUND")));
        }
    }

    @Nested
    @DisplayName("POST /api/extensions/custom")
    class AddCustomExtension {

        @Test
        @DisplayName("커스텀 확장자를 추가한다")
        void shouldAddCustomExtension() throws Exception {
            CustomExtensionRequest request = new CustomExtensionRequest("sh");

            mockMvc.perform(post("/api/extensions/custom")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // 추가 확인
            mockMvc.perform(get("/api/extensions"))
                    .andExpect(jsonPath("$.customExtensions", hasSize(1)))
                    .andExpect(jsonPath("$.customExtensions[0].extension", is("sh")))
                    .andExpect(jsonPath("$.customCount", is(1)));
        }

        @Test
        @DisplayName("대문자가 소문자로 변환되어 추가된다")
        void shouldConvertToLowercase() throws Exception {
            CustomExtensionRequest request = new CustomExtensionRequest("PHP");

            mockMvc.perform(post("/api/extensions/custom")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/extensions"))
                    .andExpect(jsonPath("$.customExtensions[0].extension", is("php")));
        }

        @Test
        @DisplayName("빈 문자열은 400을 반환한다")
        void shouldReturn400ForEmptyString() throws Exception {
            CustomExtensionRequest request = new CustomExtensionRequest("");

            mockMvc.perform(post("/api/extensions/custom")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("21자 이상은 400을 반환한다")
        void shouldReturn400ForOver20Chars() throws Exception {
            CustomExtensionRequest request = new CustomExtensionRequest("abcdefghijklmnopqrstu");

            mockMvc.perform(post("/api/extensions/custom")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
            // DTO @Size 검증이 먼저 동작하여 VALIDATION_ERROR 반환
        }

        @Test
        @DisplayName("특수문자 포함 시 400을 반환한다")
        void shouldReturn400ForSpecialChars() throws Exception {
            CustomExtensionRequest request = new CustomExtensionRequest("sh!");

            mockMvc.perform(post("/api/extensions/custom")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is("INVALID_EXTENSION")));
        }

        @Test
        @DisplayName("고정 확장자와 중복 시 400을 반환한다")
        void shouldReturn400ForDuplicateWithFixed() throws Exception {
            CustomExtensionRequest request = new CustomExtensionRequest("exe");

            mockMvc.perform(post("/api/extensions/custom")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is("DUPLICATE_EXTENSION")));
        }

        @Test
        @DisplayName("커스텀 확장자 간 중복 시 400을 반환한다")
        void shouldReturn400ForDuplicateCustom() throws Exception {
            // 첫 번째 추가
            CustomExtensionRequest request = new CustomExtensionRequest("sh");
            mockMvc.perform(post("/api/extensions/custom")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // 두 번째 추가 (중복)
            mockMvc.perform(post("/api/extensions/custom")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is("DUPLICATE_EXTENSION")));
        }

        @Test
        @DisplayName("경로 문자 포함 시 400을 반환한다")
        void shouldReturn400ForPathTraversal() throws Exception {
            CustomExtensionRequest request = new CustomExtensionRequest("../etc");

            mockMvc.perform(post("/api/extensions/custom")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is("PATH_TRAVERSAL_DETECTED")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/extensions/custom/{extension}")
    class DeleteCustomExtension {

        @Test
        @DisplayName("커스텀 확장자를 삭제한다")
        void shouldDeleteCustomExtension() throws Exception {
            // 먼저 추가
            CustomExtensionRequest request = new CustomExtensionRequest("sh");
            mockMvc.perform(post("/api/extensions/custom")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // 삭제
            mockMvc.perform(delete("/api/extensions/custom/sh"))
                    .andExpect(status().isOk());

            // 삭제 확인
            mockMvc.perform(get("/api/extensions"))
                    .andExpect(jsonPath("$.customExtensions", hasSize(0)))
                    .andExpect(jsonPath("$.customCount", is(0)));
        }

        @Test
        @DisplayName("존재하지 않는 확장자 삭제 시 400을 반환한다")
        void shouldReturn400ForNonExistent() throws Exception {
            mockMvc.perform(delete("/api/extensions/custom/notexist"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is("EXTENSION_NOT_FOUND")));
        }

        @Test
        @DisplayName("고정 확장자 삭제 시 400을 반환한다")
        void shouldReturn400ForFixedExtension() throws Exception {
            mockMvc.perform(delete("/api/extensions/custom/exe"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is("CANNOT_DELETE_FIXED")));
        }
    }
}
