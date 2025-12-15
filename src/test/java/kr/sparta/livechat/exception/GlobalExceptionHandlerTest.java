package kr.sparta.livechat.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.global.exception.ErrorResponse;
import kr.sparta.livechat.global.exception.GlobalExceptionHandler;

/**
 * GlobalExceptionHandlerTest 테스트 클래스입니다.
 * <p>
 * 비즈니스 예외를 표현하는 CustomException이 발생했을 때,
 * 전역 예외 처리기가 적절한 HTTP 상태 코드와 ErrorResponse를 반환하는지 검증합니다.
 * </p>
 *
 * @author 재원
 * @since 2025. 12. 12.
 */
public class GlobalExceptionHandlerTest {

	@Test
	@DisplayName("CustomException 발생 시 ErrorCode 기반 ErrorResponse를 반환한다.")
	void handleCustomException_ReturnErrorResponse() {
		// given
		GlobalExceptionHandler handler = new GlobalExceptionHandler();
		ErrorCode errorCode = ErrorCode.PRODUCT_NOT_FOUND;
		CustomException exception = new CustomException(errorCode);

		// when
		ResponseEntity<ErrorResponse> responseResponseEntity = handler.handleCustomException(exception);

		// then
		assertEquals(errorCode.getStatus(), responseResponseEntity.getStatusCode());

		ErrorResponse body = responseResponseEntity.getBody();
		assertNotNull(body);
		assertEquals(errorCode.getStatus().value(), body.getStatus());
		assertEquals(errorCode.getCode(), body.getCode());
		assertEquals(errorCode.getMessage(), body.getMessage());
	}
}
