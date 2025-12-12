package kr.sparta.livechat.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;

/**
 * CustomExceptionTest 테스트 클래스입니다.
 * <p>
 * 대상 클래스(또는 메서드): CustomException
 * </p>
 *
 * @author 재원
 * @since 2025. 12. 12.
 */
public class CustomExceptionTest {

	/**
	 * CustomException 생성 시 ErrorCode가 정확히 포함되고,
	 * 예외 메시지가 ErrorCode의 기본 메시지와 일치하는지 검증한다.
	 */
	@Test
	@DisplayName("ErrorCode를 포함한 CustomException이 정상적으로 생성된다.")
	void createCustomException_withErrorCode() {
		// given
		ErrorCode errorCode = ErrorCode.CHATROOM_ALREADY_EXISTS;

		// when
		CustomException exception = new CustomException(errorCode);

		// then
		assertEquals(errorCode, exception.getErrorCode()); // 에러 코드가 그대로 들어왔는가
		assertEquals(errorCode.getMessage(), exception.getMessage()); // RuntimeException의 message도 같은가
	}
}
