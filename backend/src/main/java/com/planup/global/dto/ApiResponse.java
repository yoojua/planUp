package com.planup.global.dto;

import com.planup.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ApiResponse<T> {

    private String status;  // "success" 또는 "error"
    private String message;
    private T data;

    // 성공시 데이터를 담아서 반환
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", "요청이 성공했습니다.", data);
    }

    // 성공시 데이터는 없을 때 (예: 삭제 성공)
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>("success", "요청이 성공했습니다.", null);
    }

    // 실패시 에러 메시지 반환
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>("error", errorCode.getMessage(), null);
    }

    // 실패시 메시지를 직접 넣고 싶을 경우
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", message, null);
    }
}