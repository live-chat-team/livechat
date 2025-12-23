package kr.sparta.livechat.global.exception;


import lombok.Getter;

/**
 * STOMP 처리과정에서 발생하는 예외를 표현하기 위한 커스텀 클래스입니다.
 *
 * {@link WsErrorCode}를 기반으로 에러를 구분합니다.
 * {@link GlobalStompErrorHandler}에서 가로채 표준화된 에러 프레임으로 변환됩니다.
 *
 * @author 오정빈
 * @version 1.0
 * @since 2025. 12. 22.
 */
@Getter
public class WsCustomException extends RuntimeException {

	private final WsErrorCode errorCode;

	/**
	 * {@link WsErrorCode}에 정의된 기본 메시지를 사용하는 예외를 생성합니다.
	 */
	public WsCustomException(WsErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	/**
	 * 상세 메시지를 포함하는 예외를 생성합니다.
	 */
	public WsCustomException(WsErrorCode errorCode, String detailMessage) {
		super(detailMessage);
		this.errorCode = errorCode;
	}
}
