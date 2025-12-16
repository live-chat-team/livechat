package kr.sparta.livechat.global.exception.product;

import kr.sparta.livechat.global.exception.CustomException;
import kr.sparta.livechat.global.exception.ErrorCode;

/**
 * 상품 도메인에서 발생하는 비즈니스 예외를 표현하는 예외 클래스입니다.
 * <p>
 * {@link CustomException}을 상속하며,
 * 전역 예외 처리기를 통해 {@link ErrorCode} 기반의 표준 오류 응답으로 처리됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2025. 12. 16.
 */
public class ProductException extends CustomException {

	/**
	 * 지정된 {@link ErrorCode}를 기반으로 상품 도메인 예외를 생성합니다.
	 *
	 * @param errorCode 상품 도메인에서 발생한 오류의 유형을 나타내는 에러 코드
	 */
	public ProductException(ErrorCode errorCode) {
		super(errorCode);
	}

}
