package kr.sparta.livechat.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 애플리케이션 전역에서 발생하는 예외를 처리하여 표준화된 오류 응답을 반환하는 전역 예외 처리 클래스입니다.
 * <p>
 * 컨트롤러 계층에서 발생하는 예외를 가로채 {@link ErrorResponse} 형태로 변환하며,
 * 비즈니스 예외({@link CustomException})와 예상하지 못한 일반 예외를 각각 별도의 핸들러에서 처리합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 11.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 비즈니스 로직 처리 과정에서 발생한 {@link CustomException}을 처리하는 핸들러입니다.
	 * 예외에 포함된 {@link ErrorCode} 정보를 기반으로 {@link ErrorResponse} 를 생성하고,
	 * 해당 에러 코드에 정의된 HTTP 상태 코드로 응답을 반환합니다.
	 *
	 * @param exception 비즈니스 예외 정보를 포함한 CustomException
	 * @return ErrorCode에 정의된 상태 코드와 표준화된 오류 응답 본문
	 */
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleCustomException(CustomException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		ErrorResponse response = ErrorResponse.of(errorCode);
		return ResponseEntity.status(errorCode.getStatus()).body(response);
	}
}
