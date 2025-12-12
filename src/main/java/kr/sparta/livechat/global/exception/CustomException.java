package kr.sparta.livechat.global.exception;

import lombok.Getter;

/**
 * 애플리케이션의 비즈니스 로직 처리 과정에서 발생하는 예외를 표현하는 커스텀 예외 클래스입니다.
 * <p>
 * 각 예외는 {@link ErrorCode} 를 포함하며, 전역 예외 처리기{@link GlobalExceptionHandler}를 통해 표준화된 오류 응답으로 변환됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 11.
 */
@Getter
public class CustomException extends RuntimeException {
	private final ErrorCode errorCode;

	public CustomException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
