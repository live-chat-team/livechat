package kr.sparta.livechat.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kr.sparta.livechat.global.exception.ErrorCode;
import kr.sparta.livechat.global.exception.ErrorResponse;

/**
 * ErrorResponseTest 테스트 클래스입니다.
 * <p>
 * 대상 클래스(또는 메서드): ErrorResponse
 * </p>
 *
 * @author 재원
 * @since 2025. 12. 12.
 */
public class ErrorResponseTest {

	/**
	 * ErrorCode로부터 생성된 ErrorResponse의 필드 매핑을 검증한다.
	 */
	@Test
	@DisplayName("ErrorCode 기반으로 ErrorResponse가 올바르게 생성된다.")
	void test() {
		// given
		ErrorCode errorCode = ErrorCode.PRODUCT_NOT_FOUND;

		// when
		ErrorResponse response = ErrorResponse.of(errorCode);

		// then
		assertEquals(errorCode.getStatus().value(), response.getStatus());
		assertEquals(errorCode.getCode(), response.getCode());
		assertEquals(errorCode.getMessage(), response.getMessage());
		assertNotNull(response.getTimestamp());
	}
}
