package com.flow.blocker.exception;

import lombok.Getter;

@Getter
public class ExtensionException extends RuntimeException {

    private final ErrorCode errorCode;

    public ExtensionException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public enum ErrorCode {
        INVALID_EXTENSION("유효하지 않은 확장자입니다. 영문과 숫자만 사용할 수 있습니다."),
        PATH_TRAVERSAL_DETECTED("경로 문자(/, \\, ..)는 사용할 수 없습니다."),
        EMPTY_EXTENSION("확장자를 입력해주세요."),
        EXTENSION_TOO_LONG("확장자는 최대 20자까지 입력 가능합니다."),
        DUPLICATE_EXTENSION("이미 등록된 확장자입니다."),
        MAX_CUSTOM_EXCEEDED("커스텀 확장자는 최대 200개까지 등록할 수 있습니다."),
        EXTENSION_NOT_FOUND("해당 확장자를 찾을 수 없습니다."),
        CANNOT_DELETE_FIXED("고정 확장자는 삭제할 수 없습니다.");

        private final String message;

        ErrorCode(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
