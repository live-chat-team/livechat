package kr.sparta.livechat.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * 전역적으로 발생하는 예외를 처리하는 핸들러 클래스입니다.
 * 컨트롤러 계층에서 발생하는 검증 오류, 서비스 계층에서 발생하는 예외 등을 통합적으로 처리하여
 * 일관된 형태의 응답을 클라이언트에게 반환합니다.
 * GlobalExceptionHandler.java
 *
 * @author kimsehyun
 * @since 2025. 12. 11.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * DTO의 유효성 검사 실패 시 발생하는 예외를 처리합니다.
	 * 검증 실패한 필드명과 에러 메시지를 Map 형태로 변환하여
	 * 클라이언트에게 400 Bad Request 상태 코드와 함께 반환합니다.
	 *
	 * @param ex 유효성 검증 실패 시 발생하는 예외
	 * @return 필드명과 에러 메시지를 포함하는 400 Bad Request 응답
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error ->
			errors.put(error.getField(), error.getDefaultMessage())
		);
		return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
	}

	/**
	 * 서비스 계층에서 명시적으로 발생시킨 ResponseStatusException을 처리합니다.
	 * 예외에 지정된 HTTP 상태 코드와 메시지를 그대로 클라이언트에게 반환하여
	 * 서비스 로직에서 정의한 에러 흐름을 유지합니다.
	 *
	 * @param ex 서비스 계층에서 발생한 ResponseStatusException
	 * @return 예외에서 정의한 상태 코드와 메시지를 포함한 응답
	 */
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
		return new ResponseEntity<>(ex.getReason(), ex.getStatusCode());
	}

	/**
	 * 서비스 계층에서 흔히 발생하는 IllegalArgumentException를 처리합니다.
	 * 클라이언트에게 400 Bad Request 상태 코드와 예외 메시지를 반환합니다.
	 *
	 * @param ex 서비스 계층에서 발생한 IllegalArgumentException
	 * @return 예외 메시지를 포함하는 400 Bad Request 응답
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
	}
}

