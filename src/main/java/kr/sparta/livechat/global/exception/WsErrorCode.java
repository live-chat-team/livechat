package kr.sparta.livechat.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 전역에서 발생할 수 있는 오류 상황을 정의한 Enum 클래스입니다.
 * <p>
 *   {@code status}: 서비스 내에서 사용하는 WebSocket 상태값
 *   {@code code}: 클라이언트에서 처리 가능한 문자열 코드
 *   {@code message}: 기본 메시지
 * </p>
 *
 * @author 오정빈
 * @version 1.0
 * @since 2025. 12. 18.
 */
@Getter
@AllArgsConstructor
public enum WsErrorCode {

	AUTH_FAILED(4001, "WS_AUTH_FAILED", "인증 실패"),
	FORBIDDEN(4002, "WS_FORBIDDEN", "권한 없음"),
	INVALID_MESSAGE(4003, "WS_INVALID_MESSAGE", "type/content 형식 오류"),
	CHAT_ROOM_NOT_FOUND( 4004,"WS_CHAT_ROOM_NOT_FOUND","해당 채팅방이 존재하지 않습니다."),
	INTERNAL_ERROR(4005, "WS_INTERNAL_ERROR", "서버에 문제가 있습니다.");

	private final int status;
	private final String code;
	private final String message;
}
