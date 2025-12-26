package kr.sparta.livechat.dto.socket;

import lombok.Builder;
import lombok.Getter;

/**
 * 공통 이벤트 응답 DTO 클래스입니다.
 *
 * 소켓 이벤트는 {@code event} 필드를 통해 이벤트 유형을 구분하고
 * {@code message} 필드에 실제 데이터를 포함하는 구조입니다.
 *
 * 제네릭 타입 {@code T}는 이벤트 유형에 따라 DTO({@link MessageResponse}를 받을 수 있습니다.
 *
 * @author 오정빈
 * @version 1.0
 * @since 2025. 12. 22.
 */
@Getter
@Builder
public class ChatEventResponse<T> {
	private String event;
	private T message;
}

