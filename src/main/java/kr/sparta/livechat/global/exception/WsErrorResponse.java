package kr.sparta.livechat.global.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * STOMP  오류 응답을 표준화하여 클라이언트에게 전달하기 위한 DTO 클래스입니다.
 * <p>
 *   WebSocket 환경에서는 HTTP Status Code 기반 응답이 아닌 STOMP payload 기반으로 오류를 전달합니다.
 *   발생한 예외를 {@link WsErrorCode}를 기반으로 변환하여 표준화합니다.
 * </p>
 *
 * @author 오정빈
 * @version 1.0
 * @since 2025. 12. 18.
 */
@Getter
@AllArgsConstructor
@Builder
public class WsErrorResponse {

	private final String event;
	private final WsErrorBody message;

	@Getter
	@Builder
	@AllArgsConstructor
	public static class WsErrorBody {
		private final int status;
		private final String code;
		private final String message;
		private final OffsetDateTime timestamp;
	}
}
