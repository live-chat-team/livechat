package kr.sparta.livechat.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 애플리케이션 전역에서 발생하는 예외를 처리하여 표준화된 오류 응답을 반환하는 전역 예외 처리 클래스입니다.
 *
 * <p>
 *
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 11.
 */
@RestControllerAdvice // @ControllerAdvice + @ResponseBody로 반환 객체를 JSON으로 직렬화
public class GlobalExceptionHandler {

	/**
	 * 예상하지 못한 모든 예외를 처리하기 위한 기본 핸들러
	 * CustomException이 생성되면 핸들러를 분리합니다.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		ErrorResponse response = ErrorResponse.builder()
			.status(500)
			.code("INTERNAL_SERVER_ERROR")
			.message("서버 내부 오류가 발생했습니다.")
			.build();

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
