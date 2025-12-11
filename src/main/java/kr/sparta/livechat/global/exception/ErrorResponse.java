package kr.sparta.livechat.global.exception;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

/**
 * API 오류 응답을 표준화하여 클라이언트에게 전달하기 위한 DTO 클래스입니다.
 * <p>
 * 발생한 예외를 {@link ErrorCode}를 기반으로 변환하여
 * HTTP 상태코드, 에러 코드, 메시지, 발생 시각을 일관된 형태로 제공합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 11.
 */
@Getter
@Builder
public class ErrorResponse {

	private final int status;
	private final String code;
	private final String message;
	private final LocalDateTime timestamp;

	/**
	 * 주어진 {@link ErrorCode} 정보를 기반으로 표준화된 {@code ErrorResponse} 객체를 생성합니다.
	 *
	 * @param errorCode 변환할 에러 코드
	 * @return ErrorCode 값을 포함하고 현재 시각을 timestamp로 갖는 ErrorResponse 객체체
	 */
	public static ErrorResponse of(ErrorCode errorCode) {
		return ErrorResponse.builder()
			.status(errorCode.getStatus().value())
			.code(errorCode.getCode())
			.message(errorCode.getMessage())
			.timestamp(LocalDateTime.now())
			.build();
	}
}
