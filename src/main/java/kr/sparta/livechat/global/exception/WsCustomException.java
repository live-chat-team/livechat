package kr.sparta.livechat.global.exception;


import lombok.Getter;

@Getter
public class WsCustomException extends RuntimeException {

	private final WsErrorCode errorCode;

	public WsCustomException(WsErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public WsCustomException(WsErrorCode errorCode, String detailMessage) {
		super(detailMessage);
		this.errorCode = errorCode;
	}
}
