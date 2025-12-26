package kr.sparta.livechat.dto.socket;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 클라이언트로 브로드캐스트되는 메시지 응답 DTO입니다.
 *
 * 메시지가 DB에 저장된 이후 {@code /sub/chat/room/{roomId}} 구독자들에게
 * 전달되는 데이터 구조를 정의합니다.
 *
 * @author 오정빈
 * @version 1.0
 * @since 2025. 12. 22.
 */
@Getter
@Builder
public class MessageResponse {
	private Long id;
	private Long roomId;
	private Long writerId;
	private String type;
	private String content;
	private LocalDateTime sentAt;
	private int readCount;
}

