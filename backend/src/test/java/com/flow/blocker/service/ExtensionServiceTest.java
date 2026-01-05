package com.flow.blocker.service;

import com.flow.blocker.domain.BlockedExtension;
import com.flow.blocker.dto.ExtensionResponse;
import com.flow.blocker.exception.ExtensionException;
import com.flow.blocker.exception.ExtensionException.ErrorCode;
import com.flow.blocker.repository.BlockedExtensionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class ExtensionServiceTest {

    @Autowired
    private ExtensionService extensionService;

    @Autowired
    private BlockedExtensionRepository repository;

    @BeforeEach
    void setUp() {
        // 커스텀 확장자만 삭제 (고정 확장자 유지)
        repository.findByFixedFalse().forEach(repository::delete);
    }

    @Nested
    @DisplayName("전체 확장자 조회")
    class GetAllExtensions {

        @Test
        @DisplayName("고정 확장자 7개가 조회되어야 한다")
        void shouldReturnSevenFixedExtensions() {
            ExtensionResponse response = extensionService.getAllExtensions();

            assertThat(response.getFixedExtensions()).hasSize(7);
            assertThat(response.getFixedExtensions())
                    .extracting("extension")
                    .containsExactlyInAnyOrder("bat", "cmd", "com", "cpl", "exe", "scr", "js");
        }

        @Test
        @DisplayName("커스텀 확장자 개수와 최대 개수가 반환되어야 한다")
        void shouldReturnCustomCountAndMaxCount() {
            ExtensionResponse response = extensionService.getAllExtensions();

            assertThat(response.getCustomCount()).isZero();
            assertThat(response.getMaxCustomCount()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("고정 확장자 토글")
    class ToggleFixedExtension {

        @Test
        @DisplayName("고정 확장자 활성화 토글이 동작해야 한다")
        void shouldToggleFixedExtension() {
            // 초기 상태 확인 (비활성화)
            BlockedExtension before = repository.findByExtension("exe").orElseThrow();
            assertThat(before.isActive()).isFalse();

            // 토글
            extensionService.toggleFixedExtension("exe");

            // 활성화 확인
            BlockedExtension after = repository.findByExtension("exe").orElseThrow();
            assertThat(after.isActive()).isTrue();

            // 다시 토글
            extensionService.toggleFixedExtension("exe");

            // 비활성화 확인
            BlockedExtension afterAgain = repository.findByExtension("exe").orElseThrow();
            assertThat(afterAgain.isActive()).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 확장자 토글 시 예외가 발생해야 한다")
        void shouldThrowExceptionForNonExistentExtension() {
            assertThatThrownBy(() -> extensionService.toggleFixedExtension("notexist"))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.EXTENSION_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("커스텀 확장자 추가 - 정상 케이스")
    class AddCustomExtensionSuccess {

        @Test
        @DisplayName("정상적인 확장자가 추가되어야 한다")
        void shouldAddValidExtension() {
            extensionService.addCustomExtension("sh");

            assertThat(repository.existsByExtension("sh")).isTrue();
            BlockedExtension saved = repository.findByExtension("sh").orElseThrow();
            assertThat(saved.isFixed()).isFalse();
            assertThat(saved.isActive()).isTrue();
        }

        @Test
        @DisplayName("대문자가 소문자로 변환되어야 한다")
        void shouldConvertToLowercase() {
            extensionService.addCustomExtension("SH");

            assertThat(repository.existsByExtension("sh")).isTrue();
            assertThat(repository.existsByExtension("SH")).isFalse();
        }

        @Test
        @DisplayName("앞뒤 공백이 제거되어야 한다")
        void shouldTrimWhitespace() {
            extensionService.addCustomExtension("  py  ");

            assertThat(repository.existsByExtension("py")).isTrue();
        }

        @Test
        @DisplayName("앞의 점이 제거되어야 한다")
        void shouldRemoveLeadingDot() {
            extensionService.addCustomExtension(".java");

            assertThat(repository.existsByExtension("java")).isTrue();
            assertThat(repository.existsByExtension(".java")).isFalse();
        }

        @Test
        @DisplayName("하나의 앞 점이 제거되어야 한다")
        void shouldRemoveSingleLeadingDot() {
            extensionService.addCustomExtension(".txt");

            assertThat(repository.existsByExtension("txt")).isTrue();
        }

        @Test
        @DisplayName("숫자만으로 된 확장자가 추가되어야 한다")
        void shouldAddNumericExtension() {
            extensionService.addCustomExtension("123");

            assertThat(repository.existsByExtension("123")).isTrue();
        }

        @Test
        @DisplayName("영숫자 혼합 확장자가 추가되어야 한다")
        void shouldAddAlphanumericExtension() {
            extensionService.addCustomExtension("mp3");

            assertThat(repository.existsByExtension("mp3")).isTrue();
        }

        @Test
        @DisplayName("20자 확장자가 추가되어야 한다 (경계값)")
        void shouldAddTwentyCharExtension() {
            String ext = "abcdefghijklmnopqrst"; // 20자
            extensionService.addCustomExtension(ext);

            assertThat(repository.existsByExtension(ext)).isTrue();
        }

        @Test
        @DisplayName("1자 확장자가 추가되어야 한다 (경계값)")
        void shouldAddOneCharExtension() {
            extensionService.addCustomExtension("a");

            assertThat(repository.existsByExtension("a")).isTrue();
        }
    }

    @Nested
    @DisplayName("커스텀 확장자 추가 - 실패 케이스")
    class AddCustomExtensionFailure {

        @Test
        @DisplayName("빈 문자열은 거부되어야 한다")
        void shouldRejectEmptyString() {
            assertThatThrownBy(() -> extensionService.addCustomExtension(""))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.EMPTY_EXTENSION.getMessage());
        }

        @Test
        @DisplayName("공백만 있는 문자열은 거부되어야 한다")
        void shouldRejectWhitespaceOnly() {
            assertThatThrownBy(() -> extensionService.addCustomExtension("   "))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.EMPTY_EXTENSION.getMessage());
        }

        @Test
        @DisplayName("null은 거부되어야 한다")
        void shouldRejectNull() {
            assertThatThrownBy(() -> extensionService.addCustomExtension(null))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.EMPTY_EXTENSION.getMessage());
        }

        @Test
        @DisplayName("21자 이상은 거부되어야 한다")
        void shouldRejectOver20Chars() {
            String ext = "abcdefghijklmnopqrstu"; // 21자
            assertThatThrownBy(() -> extensionService.addCustomExtension(ext))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.EXTENSION_TOO_LONG.getMessage());
        }

        @Test
        @DisplayName("특수문자가 포함되면 거부되어야 한다")
        void shouldRejectSpecialChars() {
            assertThatThrownBy(() -> extensionService.addCustomExtension("sh!"))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.INVALID_EXTENSION.getMessage());

            assertThatThrownBy(() -> extensionService.addCustomExtension("py@"))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.INVALID_EXTENSION.getMessage());

            assertThatThrownBy(() -> extensionService.addCustomExtension("a#b"))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.INVALID_EXTENSION.getMessage());
        }

        @Test
        @DisplayName("한글이 포함되면 거부되어야 한다")
        void shouldRejectKorean() {
            assertThatThrownBy(() -> extensionService.addCustomExtension("한글"))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.INVALID_EXTENSION.getMessage());
        }

        @Test
        @DisplayName("중간 공백이 있으면 거부되어야 한다")
        void shouldRejectMiddleSpace() {
            assertThatThrownBy(() -> extensionService.addCustomExtension("te st"))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.INVALID_EXTENSION.getMessage());
        }

        @Test
        @DisplayName("고정 확장자와 중복되면 거부되어야 한다")
        void shouldRejectDuplicateWithFixed() {
            assertThatThrownBy(() -> extensionService.addCustomExtension("exe"))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.DUPLICATE_EXTENSION.getMessage());

            assertThatThrownBy(() -> extensionService.addCustomExtension("js"))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.DUPLICATE_EXTENSION.getMessage());
        }

        @Test
        @DisplayName("커스텀 확장자 간 중복되면 거부되어야 한다")
        void shouldRejectDuplicateCustom() {
            extensionService.addCustomExtension("sh");

            assertThatThrownBy(() -> extensionService.addCustomExtension("sh"))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.DUPLICATE_EXTENSION.getMessage());
        }

        @Test
        @DisplayName("대소문자가 달라도 중복으로 처리되어야 한다")
        void shouldRejectCaseInsensitiveDuplicate() {
            extensionService.addCustomExtension("sh");

            assertThatThrownBy(() -> extensionService.addCustomExtension("SH"))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.DUPLICATE_EXTENSION.getMessage());
        }
    }

    @Nested
    @DisplayName("경로 문자 차단 (보안)")
    class PathTraversalPrevention {

        @Test
        @DisplayName("슬래시(/)가 포함되면 거부되어야 한다")
        void shouldRejectSlash() {
            assertThatThrownBy(() -> extensionService.addCustomExtension("a/b"))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.PATH_TRAVERSAL_DETECTED.getMessage());
        }

        @Test
        @DisplayName("백슬래시(\\)가 포함되면 거부되어야 한다")
        void shouldRejectBackslash() {
            assertThatThrownBy(() -> extensionService.addCustomExtension("a\\b"))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.PATH_TRAVERSAL_DETECTED.getMessage());
        }

        @Test
        @DisplayName("상위 경로(..)가 포함되면 거부되어야 한다")
        void shouldRejectParentPath() {
            assertThatThrownBy(() -> extensionService.addCustomExtension("../etc"))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.PATH_TRAVERSAL_DETECTED.getMessage());

            assertThatThrownBy(() -> extensionService.addCustomExtension("..\\win"))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.PATH_TRAVERSAL_DETECTED.getMessage());
        }
    }

    @Nested
    @DisplayName("커스텀 확장자 삭제")
    class DeleteCustomExtension {

        @Test
        @DisplayName("커스텀 확장자가 삭제되어야 한다")
        void shouldDeleteCustomExtension() {
            extensionService.addCustomExtension("sh");
            assertThat(repository.existsByExtension("sh")).isTrue();

            extensionService.deleteCustomExtension("sh");

            assertThat(repository.existsByExtension("sh")).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 확장자 삭제 시 예외가 발생해야 한다")
        void shouldThrowExceptionForNonExistent() {
            assertThatThrownBy(() -> extensionService.deleteCustomExtension("notexist"))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.EXTENSION_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("고정 확장자 삭제 시 예외가 발생해야 한다")
        void shouldThrowExceptionForFixedExtension() {
            assertThatThrownBy(() -> extensionService.deleteCustomExtension("exe"))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.CANNOT_DELETE_FIXED.getMessage());

            assertThatThrownBy(() -> extensionService.deleteCustomExtension("js"))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.CANNOT_DELETE_FIXED.getMessage());
        }
    }

    @Nested
    @DisplayName("개수 제한")
    class MaxCountLimit {

        @Test
        @DisplayName("200개 초과 시 예외가 발생해야 한다")
        void shouldThrowExceptionWhenExceedMax() {
            // 200개 추가
            for (int i = 0; i < 200; i++) {
                extensionService.addCustomExtension("ext" + i);
            }

            // 201번째 추가 시도
            assertThatThrownBy(() -> extensionService.addCustomExtension("ext200"))
                    .isInstanceOf(ExtensionException.class)
                    .hasMessage(ErrorCode.MAX_CUSTOM_EXCEEDED.getMessage());
        }

        @Test
        @DisplayName("200개 삭제 후 다시 추가 가능해야 한다")
        void shouldAllowAddAfterDelete() {
            // 200개 추가
            for (int i = 0; i < 200; i++) {
                extensionService.addCustomExtension("ext" + i);
            }

            // 1개 삭제
            extensionService.deleteCustomExtension("ext0");

            // 다시 1개 추가 가능
            assertThatCode(() -> extensionService.addCustomExtension("newext"))
                    .doesNotThrowAnyException();
        }
    }
}
