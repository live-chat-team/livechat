package kr.sparta.livechat.dto.socket;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 메시지 읽음 이벤트 응답 DTO입니다.
 * 사용자가 메시지를 읽었을 때 다른 참여자들에게 브로드캐스트되는 이벤트 정보를 담는 응답 객체입니다.
 *
 * @author 오정빈
 * @since 2025. 12. 23.
 */
@Getter
@Builder
public class ReadEventResponse {

	private String event;
	private Long roomId;
	private Long readerId;
	private Long lastReadMessageId;
	private LocalDateTime readAt;
}

