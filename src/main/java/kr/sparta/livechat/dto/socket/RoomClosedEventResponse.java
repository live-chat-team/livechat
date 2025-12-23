package kr.sparta.livechat.dto.socket;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 채팅방 종료 시스템 이벤트 응답 DTO입니다.
 * 채팅방 상태가 CLOSED로 변경되었을 때, 해당 방의 모든 참여자에게
 * 브로드캐스트되는 ROOM_CLOSED 이벤트의 페이로드를 표현합니다.
 */
@Getter
@Builder
public class RoomClosedEventResponse {

	private String event;
	private Long roomId;
	private Long closedBy;
	private String reason;
	private LocalDateTime closedAt;
}


